package kr.go.molit.icas.emp.plan.risk;

import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.DataScopeValidator;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.emp.plan.EmpPlanMapper;
import kr.go.molit.icas.emp.plan.domain.EmpPlanVO;
import kr.go.molit.icas.emp.plan.risk.domain.EmpRiskVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 위험·통제 항목 비즈니스 서비스 (emp.TN_EMP_RISK).
 *
 * <h2>핵심 규칙</h2>
 * <ul>
 *   <li>부모 plan 이 DRAFT 상태일 때만 추가/수정/삭제 가능</li>
 *   <li>risk_desc 필수, 최대 2000자</li>
 *   <li>ctrl_actv nullable, 최대 2000자</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmpRiskService {

    private static final int MAX_DESC_LENGTH = 2000;

    private final EmpRiskMapper      empRiskMapper;
    private final EmpPlanMapper      empPlanMapper;
    private final DataScopeValidator dataScopeValidator;

    // ══════════════════════════════════════════════════════
    // 조회
    // ══════════════════════════════════════════════════════

    /**
     * 특정 EMP Plan 의 위험·통제 항목 전체 목록 조회.
     *
     * @param empPlanId EMP Plan ID
     * @param user      로그인 사용자
     * @return 위험·통제 목록 (risk_sn ASC)
     * @throws BusinessException NOT_FOUND — plan 미존재
     * @throws BusinessException FORBIDDEN — 가시범위 밖
     */
    public List<EmpRiskVO> listByPlan(String empPlanId, IcasUser user) {
        EmpPlanVO plan = loadPlan(empPlanId);
        dataScopeValidator.assertOprtrAccessible(user, plan.getOprtrId(), plan.getRprtYr());
        return empRiskMapper.selectByPlanId(empPlanId);
    }

    /**
     * 위험·통제 항목 단건 조회.
     *
     * @param empPlanId EMP Plan ID
     * @param sn        위험 항목 일련번호
     * @param user      로그인 사용자
     * @return 위험·통제 VO
     * @throws BusinessException NOT_FOUND — plan 또는 항목 미존재
     */
    public EmpRiskVO getOne(String empPlanId, int sn, IcasUser user) {
        EmpPlanVO plan = loadPlan(empPlanId);
        dataScopeValidator.assertOprtrAccessible(user, plan.getOprtrId(), plan.getRprtYr());
        EmpRiskVO risk = empRiskMapper.selectOne(empPlanId, sn);
        if (risk == null) throw BusinessException.notFound("위험·통제 항목");
        return risk;
    }

    // ══════════════════════════════════════════════════════
    // 추가
    // ══════════════════════════════════════════════════════

    /**
     * 위험·통제 항목 추가.
     *
     * @param empPlanId EMP Plan ID
     * @param vo        등록 데이터 (riskDesc 필수)
     * @param user      로그인 사용자
     * @return 생성된 위험·통제 항목 VO
     * @throws BusinessException BAD_REQUEST — 검증 실패
     */
    @Transactional
    public EmpRiskVO addChild(String empPlanId, EmpRiskVO vo, IcasUser user) {
        EmpPlanVO plan = loadPlan(empPlanId);
        dataScopeValidator.assertOwnAirline(user, plan.getOprtrId());
        assertDraft(plan);

        validateRiskDesc(vo.getRiskDesc());
        validateCtrlActv(vo.getCtrlActv());

        int nextSn = empRiskMapper.selectNextSn(empPlanId);

        vo.setEmpPlanId(empPlanId);
        vo.setRiskSn(nextSn);
        vo.setFrstRegUserId(user.getUserId());
        vo.setLastChgUserId(user.getUserId());

        empRiskMapper.insertEmpRisk(vo);
        return empRiskMapper.selectOne(empPlanId, nextSn);
    }

    // ══════════════════════════════════════════════════════
    // 수정
    // ══════════════════════════════════════════════════════

    /**
     * 위험·통제 항목 수정.
     *
     * @param empPlanId EMP Plan ID
     * @param sn        위험 항목 일련번호
     * @param vo        수정 데이터
     * @param user      로그인 사용자
     * @throws BusinessException NOT_FOUND — 항목 미존재
     */
    @Transactional
    public void updateChild(String empPlanId, int sn, EmpRiskVO vo, IcasUser user) {
        EmpPlanVO plan = loadPlan(empPlanId);
        dataScopeValidator.assertOwnAirline(user, plan.getOprtrId());
        assertDraft(plan);

        EmpRiskVO existing = empRiskMapper.selectOne(empPlanId, sn);
        if (existing == null) throw BusinessException.notFound("위험·통제 항목");

        validateRiskDesc(vo.getRiskDesc());
        validateCtrlActv(vo.getCtrlActv());

        vo.setEmpPlanId(empPlanId);
        vo.setRiskSn(sn);
        vo.setLastChgUserId(user.getUserId());

        int affected = empRiskMapper.updateEmpRisk(vo);
        if (affected == 0) throw BusinessException.conflict("수정 대상 위험·통제 항목이 존재하지 않거나 이미 만료되었습니다.");
    }

    // ══════════════════════════════════════════════════════
    // 소프트삭제
    // ══════════════════════════════════════════════════════

    /**
     * 위험·통제 항목 소프트삭제.
     *
     * @param empPlanId EMP Plan ID
     * @param sn        위험 항목 일련번호
     * @param user      로그인 사용자
     * @throws BusinessException NOT_FOUND — 항목 미존재
     */
    @Transactional
    public void softDeleteChild(String empPlanId, int sn, IcasUser user) {
        EmpPlanVO plan = loadPlan(empPlanId);
        dataScopeValidator.assertOwnAirline(user, plan.getOprtrId());
        assertDraft(plan);

        int affected = empRiskMapper.softDeleteOne(empPlanId, sn, user.getUserId());
        if (affected == 0) throw BusinessException.notFound("위험·통제 항목");
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

    private void validateRiskDesc(String riskDesc) {
        if (riskDesc == null || riskDesc.isBlank()) {
            throw BusinessException.badRequest("위험 설명(riskDesc)은 필수입니다.");
        }
        if (riskDesc.length() > MAX_DESC_LENGTH) {
            throw BusinessException.badRequest("위험 설명(riskDesc)은 최대 " + MAX_DESC_LENGTH + "자입니다. 입력 길이: " + riskDesc.length());
        }
    }

    private void validateCtrlActv(String ctrlActv) {
        if (ctrlActv != null && ctrlActv.length() > MAX_DESC_LENGTH) {
            throw BusinessException.badRequest("통제 활동(ctrlActv)은 최대 " + MAX_DESC_LENGTH + "자입니다. 입력 길이: " + ctrlActv.length());
        }
    }
}
