package kr.go.molit.icas.emp.plan.cntry;

import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.DataScopeValidator;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.emp.plan.EmpPlanMapper;
import kr.go.molit.icas.emp.plan.cntry.domain.EmpCntryPairVO;
import kr.go.molit.icas.emp.plan.domain.EmpPlanVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * 운항 국가 쌍 비즈니스 서비스 (emp.TN_EMP_CNTRY_PAIR).
 *
 * <h2>핵심 규칙</h2>
 * <ul>
 *   <li>부모 plan 이 DRAFT 상태일 때만 추가/수정/삭제 가능</li>
 *   <li>국가 코드: 2자 필수 (FK 검증은 DB 담당)</li>
 *   <li>intl_yn = 'Y' 이면서 출발==도착 국가 금지</li>
 *   <li>exempt_cd 화이트리스트: HUMANITARIAN / MEDICAL / FIRE / null</li>
 *   <li>같은 (출발, 도착) 조합 중복 금지</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmpCntryPairService {

    private static final Set<String> VALID_EXEMPT_CD = Set.of("HUMANITARIAN", "MEDICAL", "FIRE");

    private final EmpCntryPairMapper empCntryPairMapper;
    private final EmpPlanMapper      empPlanMapper;
    private final DataScopeValidator dataScopeValidator;

    // ══════════════════════════════════════════════════════
    // 조회
    // ══════════════════════════════════════════════════════

    /**
     * 특정 EMP Plan 의 운항 국가 쌍 전체 목록 조회.
     *
     * @param empPlanId EMP Plan ID
     * @param user      로그인 사용자
     * @return 국가쌍 목록 (pair_sn ASC)
     * @throws BusinessException NOT_FOUND — plan 미존재
     * @throws BusinessException FORBIDDEN — 가시범위 밖
     */
    public List<EmpCntryPairVO> listByPlan(String empPlanId, IcasUser user) {
        EmpPlanVO plan = loadPlan(empPlanId);
        dataScopeValidator.assertOprtrAccessible(user, plan.getOprtrId(), plan.getRprtYr());
        return empCntryPairMapper.selectByPlanId(empPlanId);
    }

    /**
     * 운항 국가 쌍 단건 조회.
     *
     * @param empPlanId EMP Plan ID
     * @param sn        국가쌍 일련번호
     * @param user      로그인 사용자
     * @return 국가쌍 VO
     * @throws BusinessException NOT_FOUND — plan 또는 국가쌍 미존재
     */
    public EmpCntryPairVO getOne(String empPlanId, int sn, IcasUser user) {
        EmpPlanVO plan = loadPlan(empPlanId);
        dataScopeValidator.assertOprtrAccessible(user, plan.getOprtrId(), plan.getRprtYr());
        EmpCntryPairVO pair = empCntryPairMapper.selectOne(empPlanId, sn);
        if (pair == null) throw BusinessException.notFound("운항 국가 쌍");
        return pair;
    }

    // ══════════════════════════════════════════════════════
    // 추가
    // ══════════════════════════════════════════════════════

    /**
     * 운항 국가 쌍 추가.
     *
     * @param empPlanId EMP Plan ID
     * @param vo        등록 데이터 (dprtrCntryCd, arvlCntryCd, intlYn 필수)
     * @param user      로그인 사용자
     * @return 생성된 국가쌍 VO
     * @throws BusinessException BAD_REQUEST — 검증 실패
     * @throws BusinessException CONFLICT    — 중복 국가쌍
     */
    @Transactional
    public EmpCntryPairVO addChild(String empPlanId, EmpCntryPairVO vo, IcasUser user) {
        EmpPlanVO plan = loadPlan(empPlanId);
        dataScopeValidator.assertOwnAirline(user, plan.getOprtrId());
        assertDraft(plan);

        validateCntryCd(vo.getDprtrCntryCd(), "출발 국가 코드(dprtrCntryCd)");
        validateCntryCd(vo.getArvlCntryCd(), "도착 국가 코드(arvlCntryCd)");
        validateIntlCntry(vo.getIntlYn(), vo.getDprtrCntryCd(), vo.getArvlCntryCd());
        validateExemptCd(vo.getExemptCd());

        // (출발, 도착) 중복 체크
        int dupCount = empCntryPairMapper.existsByCntryPair(
                empPlanId, vo.getDprtrCntryCd(), vo.getArvlCntryCd(), -1);
        if (dupCount > 0) {
            throw BusinessException.conflict(
                    "동일한 출발·도착 국가 조합(" + vo.getDprtrCntryCd() + " → " + vo.getArvlCntryCd() + ")이 이미 등록되어 있습니다.");
        }

        int nextSn = empCntryPairMapper.selectNextSn(empPlanId);

        vo.setEmpPlanId(empPlanId);
        vo.setPairSn(nextSn);
        if (vo.getIntlYn() == null) vo.setIntlYn("Y");
        vo.setFrstRegUserId(user.getUserId());
        vo.setLastChgUserId(user.getUserId());

        empCntryPairMapper.insertEmpCntryPair(vo);
        return empCntryPairMapper.selectOne(empPlanId, nextSn);
    }

    // ══════════════════════════════════════════════════════
    // 수정
    // ══════════════════════════════════════════════════════

    /**
     * 운항 국가 쌍 수정.
     *
     * @param empPlanId EMP Plan ID
     * @param sn        국가쌍 일련번호
     * @param vo        수정 데이터
     * @param user      로그인 사용자
     * @throws BusinessException NOT_FOUND — 국가쌍 미존재
     * @throws BusinessException CONFLICT  — 중복 국가쌍
     */
    @Transactional
    public void updateChild(String empPlanId, int sn, EmpCntryPairVO vo, IcasUser user) {
        EmpPlanVO plan = loadPlan(empPlanId);
        dataScopeValidator.assertOwnAirline(user, plan.getOprtrId());
        assertDraft(plan);

        EmpCntryPairVO existing = empCntryPairMapper.selectOne(empPlanId, sn);
        if (existing == null) throw BusinessException.notFound("운항 국가 쌍");

        validateCntryCd(vo.getDprtrCntryCd(), "출발 국가 코드(dprtrCntryCd)");
        validateCntryCd(vo.getArvlCntryCd(), "도착 국가 코드(arvlCntryCd)");
        validateIntlCntry(vo.getIntlYn(), vo.getDprtrCntryCd(), vo.getArvlCntryCd());
        validateExemptCd(vo.getExemptCd());

        // 자신(sn) 제외 중복 체크
        int dupCount = empCntryPairMapper.existsByCntryPair(
                empPlanId, vo.getDprtrCntryCd(), vo.getArvlCntryCd(), sn);
        if (dupCount > 0) {
            throw BusinessException.conflict(
                    "동일한 출발·도착 국가 조합(" + vo.getDprtrCntryCd() + " → " + vo.getArvlCntryCd() + ")이 이미 등록되어 있습니다.");
        }

        vo.setEmpPlanId(empPlanId);
        vo.setPairSn(sn);
        vo.setLastChgUserId(user.getUserId());

        int affected = empCntryPairMapper.updateEmpCntryPair(vo);
        if (affected == 0) throw BusinessException.conflict("수정 대상 국가 쌍이 존재하지 않거나 이미 만료되었습니다.");
    }

    // ══════════════════════════════════════════════════════
    // 소프트삭제
    // ══════════════════════════════════════════════════════

    /**
     * 운항 국가 쌍 소프트삭제.
     *
     * @param empPlanId EMP Plan ID
     * @param sn        국가쌍 일련번호
     * @param user      로그인 사용자
     * @throws BusinessException NOT_FOUND — 국가쌍 미존재
     */
    @Transactional
    public void softDeleteChild(String empPlanId, int sn, IcasUser user) {
        EmpPlanVO plan = loadPlan(empPlanId);
        dataScopeValidator.assertOwnAirline(user, plan.getOprtrId());
        assertDraft(plan);

        int affected = empCntryPairMapper.softDeleteOne(empPlanId, sn, user.getUserId());
        if (affected == 0) throw BusinessException.notFound("운항 국가 쌍");
    }

    // ══════════════════════════════════════════════════════
    // Private Helpers
    // ══════════════════════════════════════════════════════

    private EmpPlanVO loadPlan(String empPlanId) {
        EmpPlanVO plan = empPlanMapper.selectByEmpPlanId(empPlanId);
        if (plan == null) throw BusinessException.notFound("EMP Plan");
        return plan;
    }

    private void assertDraft(EmpPlanVO plan) {
        if (!"DRAFT".equals(plan.getEmpStCd())) {
            throw BusinessException.badRequest("DRAFT 상태의 EMP Plan 에서만 수정할 수 있습니다. 현재 상태: " + plan.getEmpStCd());
        }
    }

    private void validateCntryCd(String cntryCd, String fieldLabel) {
        if (cntryCd == null || cntryCd.length() != 2) {
            throw BusinessException.badRequest(fieldLabel + "는 2자리 국가 코드여야 합니다. 입력값: " + cntryCd);
        }
    }

    private void validateIntlCntry(String intlYn, String dprtr, String arvl) {
        if ("Y".equals(intlYn) && dprtr != null && dprtr.equalsIgnoreCase(arvl)) {
            throw BusinessException.badRequest("국제 운항은 출발·도착 국가가 달라야 합니다. 출발: " + dprtr + ", 도착: " + arvl);
        }
    }

    private void validateExemptCd(String exemptCd) {
        if (exemptCd != null && !exemptCd.isBlank() && !VALID_EXEMPT_CD.contains(exemptCd)) {
            throw BusinessException.badRequest("면제 코드(exemptCd) 허용값: HUMANITARIAN, MEDICAL, FIRE, null. 입력값: " + exemptCd);
        }
    }
}
