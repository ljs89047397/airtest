package kr.go.molit.icas.emp.plan.acft;

import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.DataScopeValidator;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.emp.plan.EmpPlanMapper;
import kr.go.molit.icas.emp.plan.acft.domain.EmpAcftVO;
import kr.go.molit.icas.emp.plan.domain.EmpPlanVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 항공기 비즈니스 서비스 (emp.TN_EMP_ACFT).
 *
 * <h2>핵심 규칙</h2>
 * <ul>
 *   <li>부모 plan 이 DRAFT 상태일 때만 추가/수정/삭제 가능</li>
 *   <li>acft_type_cd 필수 (FK 검증은 DB 담당)</li>
 *   <li>fuel_type_cd 필수</li>
 *   <li>acft_cnt 는 1 이상이어야 함</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmpAcftService {

    private final EmpAcftMapper      empAcftMapper;
    private final EmpPlanMapper      empPlanMapper;
    private final DataScopeValidator dataScopeValidator;

    // ══════════════════════════════════════════════════════
    // 조회
    // ══════════════════════════════════════════════════════

    /**
     * 특정 EMP Plan 의 항공기 목록 조회.
     *
     * @param empPlanId EMP Plan ID
     * @param user      로그인 사용자
     * @return 항공기 목록 (acft_sn ASC)
     * @throws BusinessException NOT_FOUND — plan 미존재
     * @throws BusinessException FORBIDDEN — 가시범위 밖
     */
    public List<EmpAcftVO> listByPlan(String empPlanId, IcasUser user) {
        EmpPlanVO plan = loadPlan(empPlanId);
        dataScopeValidator.assertOprtrAccessible(user, plan.getOprtrId(), plan.getRprtYr());
        return empAcftMapper.selectByPlanId(empPlanId);
    }

    /**
     * 항공기 단건 조회.
     *
     * @param empPlanId EMP Plan ID
     * @param sn        항공기 일련번호
     * @param user      로그인 사용자
     * @return 항공기 VO
     * @throws BusinessException NOT_FOUND — plan 또는 항공기 미존재
     */
    public EmpAcftVO getOne(String empPlanId, int sn, IcasUser user) {
        EmpPlanVO plan = loadPlan(empPlanId);
        dataScopeValidator.assertOprtrAccessible(user, plan.getOprtrId(), plan.getRprtYr());
        EmpAcftVO acft = empAcftMapper.selectOne(empPlanId, sn);
        if (acft == null) throw BusinessException.notFound("항공기");
        return acft;
    }

    // ══════════════════════════════════════════════════════
    // 추가
    // ══════════════════════════════════════════════════════

    /**
     * 항공기 추가.
     *
     * @param empPlanId EMP Plan ID
     * @param vo        등록 데이터 (acftTypeCd, fuelTypeCd, acftCnt 필수)
     * @param user      로그인 사용자
     * @return 생성된 항공기 VO
     * @throws BusinessException BAD_REQUEST — 검증 실패
     */
    @Transactional
    public EmpAcftVO addChild(String empPlanId, EmpAcftVO vo, IcasUser user) {
        EmpPlanVO plan = loadPlan(empPlanId);
        dataScopeValidator.assertOwnAirline(user, plan.getOprtrId());
        assertDraft(plan);

        validateAcftTypeCd(vo.getAcftTypeCd());
        validateFuelTypeCd(vo.getFuelTypeCd());
        validateAcftCnt(vo.getAcftCnt());

        int nextSn = empAcftMapper.selectNextSn(empPlanId);

        vo.setEmpPlanId(empPlanId);
        vo.setAcftSn(nextSn);
        vo.setFrstRegUserId(user.getUserId());
        vo.setLastChgUserId(user.getUserId());

        empAcftMapper.insertEmpAcft(vo);
        return empAcftMapper.selectOne(empPlanId, nextSn);
    }

    // ══════════════════════════════════════════════════════
    // 수정
    // ══════════════════════════════════════════════════════

    /**
     * 항공기 수정.
     *
     * @param empPlanId EMP Plan ID
     * @param sn        항공기 일련번호
     * @param vo        수정 데이터
     * @param user      로그인 사용자
     * @throws BusinessException NOT_FOUND — 항공기 미존재
     */
    @Transactional
    public void updateChild(String empPlanId, int sn, EmpAcftVO vo, IcasUser user) {
        EmpPlanVO plan = loadPlan(empPlanId);
        dataScopeValidator.assertOwnAirline(user, plan.getOprtrId());
        assertDraft(plan);

        EmpAcftVO existing = empAcftMapper.selectOne(empPlanId, sn);
        if (existing == null) throw BusinessException.notFound("항공기");

        validateAcftTypeCd(vo.getAcftTypeCd());
        validateFuelTypeCd(vo.getFuelTypeCd());
        validateAcftCnt(vo.getAcftCnt());

        vo.setEmpPlanId(empPlanId);
        vo.setAcftSn(sn);
        vo.setLastChgUserId(user.getUserId());

        int affected = empAcftMapper.updateEmpAcft(vo);
        if (affected == 0) throw BusinessException.conflict("수정 대상 항공기가 존재하지 않거나 이미 만료되었습니다.");
    }

    // ══════════════════════════════════════════════════════
    // 소프트삭제
    // ══════════════════════════════════════════════════════

    /**
     * 항공기 소프트삭제.
     *
     * @param empPlanId EMP Plan ID
     * @param sn        항공기 일련번호
     * @param user      로그인 사용자
     * @throws BusinessException NOT_FOUND — 항공기 미존재
     */
    @Transactional
    public void softDeleteChild(String empPlanId, int sn, IcasUser user) {
        EmpPlanVO plan = loadPlan(empPlanId);
        dataScopeValidator.assertOwnAirline(user, plan.getOprtrId());
        assertDraft(plan);

        int affected = empAcftMapper.softDeleteOne(empPlanId, sn, user.getUserId());
        if (affected == 0) throw BusinessException.notFound("항공기");
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

    private void validateAcftTypeCd(String acftTypeCd) {
        if (acftTypeCd == null || acftTypeCd.isBlank()) {
            throw BusinessException.badRequest("항공기 유형 코드(acftTypeCd)는 필수입니다.");
        }
    }

    private void validateFuelTypeCd(String fuelTypeCd) {
        if (fuelTypeCd == null || fuelTypeCd.isBlank()) {
            throw BusinessException.badRequest("연료 유형 코드(fuelTypeCd)는 필수입니다.");
        }
    }

    private void validateAcftCnt(int acftCnt) {
        if (acftCnt < 1) {
            throw BusinessException.badRequest("항공기 대수(acftCnt)는 1 이상이어야 합니다. 입력값: " + acftCnt);
        }
    }
}
