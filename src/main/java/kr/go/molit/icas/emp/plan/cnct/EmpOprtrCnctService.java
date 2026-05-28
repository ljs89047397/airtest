package kr.go.molit.icas.emp.plan.cnct;

import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.DataScopeValidator;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.emp.plan.EmpPlanMapper;
import kr.go.molit.icas.emp.plan.cnct.domain.EmpOprtrCnctVO;
import kr.go.molit.icas.emp.plan.domain.EmpPlanVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * 담당자 연락처 비즈니스 서비스 (emp.TN_EMP_OPRTR_CNCT).
 *
 * <h2>핵심 규칙</h2>
 * <ul>
 *   <li>부모 plan 이 DRAFT 상태일 때만 추가/수정/삭제 가능</li>
 *   <li>cnct_se_cd 허용값: PRIMARY / SUB</li>
 *   <li>같은 plan 내 PRIMARY 는 최대 1명</li>
 *   <li>user_nm 필수, mblphn_no 또는 eml_addr 중 1개 이상 필수</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmpOprtrCnctService {

    private static final Set<String> VALID_CNCT_SE_CD = Set.of("PRIMARY", "SUB");

    private final EmpOprtrCnctMapper empOprtrCnctMapper;
    private final EmpPlanMapper      empPlanMapper;
    private final DataScopeValidator dataScopeValidator;

    // ══════════════════════════════════════════════════════
    // 조회
    // ══════════════════════════════════════════════════════

    /**
     * 특정 EMP Plan 의 담당자 연락처 전체 목록 조회.
     *
     * @param empPlanId EMP Plan ID
     * @param user      로그인 사용자
     * @return 연락처 목록 (cnct_sn ASC)
     * @throws BusinessException NOT_FOUND — plan 미존재
     * @throws BusinessException FORBIDDEN — 가시범위 밖
     */
    public List<EmpOprtrCnctVO> listByPlan(String empPlanId, IcasUser user) {
        EmpPlanVO plan = loadPlan(empPlanId);
        dataScopeValidator.assertOprtrAccessible(user, plan.getOprtrId(), plan.getRprtYr());
        return empOprtrCnctMapper.selectByPlanId(empPlanId);
    }

    /**
     * 담당자 연락처 단건 조회.
     *
     * @param empPlanId EMP Plan ID
     * @param sn        연락처 일련번호
     * @param user      로그인 사용자
     * @return 연락처 VO
     * @throws BusinessException NOT_FOUND — plan 또는 연락처 미존재
     */
    public EmpOprtrCnctVO getOne(String empPlanId, int sn, IcasUser user) {
        EmpPlanVO plan = loadPlan(empPlanId);
        dataScopeValidator.assertOprtrAccessible(user, plan.getOprtrId(), plan.getRprtYr());
        EmpOprtrCnctVO cnct = empOprtrCnctMapper.selectOne(empPlanId, sn);
        if (cnct == null) throw BusinessException.notFound("담당자 연락처");
        return cnct;
    }

    // ══════════════════════════════════════════════════════
    // 추가
    // ══════════════════════════════════════════════════════

    /**
     * 담당자 연락처 추가.
     *
     * <p>부모 plan 이 DRAFT 상태여야 하며, PRIMARY 는 plan 당 최대 1명.
     *
     * @param empPlanId EMP Plan ID
     * @param vo        등록 데이터 (cnctSeCd, userNm, mblphnNo/emlAddr)
     * @param user      로그인 사용자
     * @return 생성된 연락처 VO
     * @throws BusinessException BAD_REQUEST — 검증 실패
     * @throws BusinessException CONFLICT    — PRIMARY 중복
     */
    @Transactional
    public EmpOprtrCnctVO addChild(String empPlanId, EmpOprtrCnctVO vo, IcasUser user) {
        EmpPlanVO plan = loadPlan(empPlanId);
        dataScopeValidator.assertOwnAirline(user, plan.getOprtrId());
        assertDraft(plan);

        // 도메인 검증
        validateCnctSeCd(vo.getCnctSeCd());
        validateUserNm(vo.getUserNm());
        validateContact(vo.getMblphnNo(), vo.getEmlAddr());

        // PRIMARY 유일성 체크 (insert 직전)
        if ("PRIMARY".equals(vo.getCnctSeCd())) {
            int primaryCount = empOprtrCnctMapper.countByPlanAndSeCd(empPlanId, "PRIMARY");
            if (primaryCount > 0) {
                throw BusinessException.conflict("담당자(PRIMARY) 는 EMP Plan 당 최대 1명만 등록할 수 있습니다.");
            }
        }

        // sn 채번
        int nextSn = empOprtrCnctMapper.selectNextSn(empPlanId);

        vo.setEmpPlanId(empPlanId);
        vo.setCnctSn(nextSn);
        vo.setFrstRegUserId(user.getUserId());
        vo.setLastChgUserId(user.getUserId());

        empOprtrCnctMapper.insertEmpOprtrCnct(vo);
        return empOprtrCnctMapper.selectOne(empPlanId, nextSn);
    }

    // ══════════════════════════════════════════════════════
    // 수정
    // ══════════════════════════════════════════════════════

    /**
     * 담당자 연락처 수정.
     *
     * <p>부모 plan 이 DRAFT 상태여야 함.
     * PRIMARY 로 변경 시 기존 다른 PRIMARY 와 중복 체크 (자신 제외).
     *
     * @param empPlanId EMP Plan ID
     * @param sn        연락처 일련번호
     * @param vo        수정 데이터
     * @param user      로그인 사용자
     * @throws BusinessException NOT_FOUND — 연락처 미존재
     * @throws BusinessException CONFLICT  — PRIMARY 중복
     */
    @Transactional
    public void updateChild(String empPlanId, int sn, EmpOprtrCnctVO vo, IcasUser user) {
        EmpPlanVO plan = loadPlan(empPlanId);
        dataScopeValidator.assertOwnAirline(user, plan.getOprtrId());
        assertDraft(plan);

        EmpOprtrCnctVO existing = empOprtrCnctMapper.selectOne(empPlanId, sn);
        if (existing == null) throw BusinessException.notFound("담당자 연락처");

        // 도메인 검증
        validateCnctSeCd(vo.getCnctSeCd());
        validateUserNm(vo.getUserNm());
        validateContact(vo.getMblphnNo(), vo.getEmlAddr());

        // PRIMARY 로 변경 시 기존 PRIMARY 확인 (자신이 이미 PRIMARY 가 아닌 경우만 체크)
        if ("PRIMARY".equals(vo.getCnctSeCd()) && !"PRIMARY".equals(existing.getCnctSeCd())) {
            int primaryCount = empOprtrCnctMapper.countByPlanAndSeCd(empPlanId, "PRIMARY");
            if (primaryCount > 0) {
                throw BusinessException.conflict("담당자(PRIMARY) 는 EMP Plan 당 최대 1명만 등록할 수 있습니다.");
            }
        }

        vo.setEmpPlanId(empPlanId);
        vo.setCnctSn(sn);
        vo.setLastChgUserId(user.getUserId());

        int affected = empOprtrCnctMapper.updateEmpOprtrCnct(vo);
        if (affected == 0) throw BusinessException.conflict("수정 대상 연락처가 존재하지 않거나 이미 만료되었습니다.");
    }

    // ══════════════════════════════════════════════════════
    // 소프트삭제
    // ══════════════════════════════════════════════════════

    /**
     * 담당자 연락처 소프트삭제.
     *
     * <p>부모 plan 이 DRAFT 상태여야 함.
     *
     * @param empPlanId EMP Plan ID
     * @param sn        연락처 일련번호
     * @param user      로그인 사용자
     * @throws BusinessException NOT_FOUND — 연락처 미존재
     */
    @Transactional
    public void softDeleteChild(String empPlanId, int sn, IcasUser user) {
        EmpPlanVO plan = loadPlan(empPlanId);
        dataScopeValidator.assertOwnAirline(user, plan.getOprtrId());
        assertDraft(plan);

        int affected = empOprtrCnctMapper.softDeleteOne(empPlanId, sn, user.getUserId());
        if (affected == 0) throw BusinessException.notFound("담당자 연락처");
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

    private void validateCnctSeCd(String cnctSeCd) {
        if (cnctSeCd == null || !VALID_CNCT_SE_CD.contains(cnctSeCd)) {
            throw BusinessException.badRequest("연락처 구분코드(cnctSeCd) 허용값: PRIMARY, SUB. 입력값: " + cnctSeCd);
        }
    }

    private void validateUserNm(String userNm) {
        if (userNm == null || userNm.isBlank()) {
            throw BusinessException.badRequest("성명(userNm)은 필수입니다.");
        }
    }

    private void validateContact(String mblphnNo, String emlAddr) {
        boolean hasMblphn = mblphnNo != null && !mblphnNo.isBlank();
        boolean hasEml    = emlAddr  != null && !emlAddr.isBlank();
        if (!hasMblphn && !hasEml) {
            throw BusinessException.badRequest("휴대전화번호(mblphnNo) 또는 이메일(emlAddr) 중 1개 이상은 필수입니다.");
        }
    }
}
