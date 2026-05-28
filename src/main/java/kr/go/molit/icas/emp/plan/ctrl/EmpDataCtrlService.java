package kr.go.molit.icas.emp.plan.ctrl;

import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.DataScopeValidator;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.emp.plan.EmpPlanMapper;
import kr.go.molit.icas.emp.plan.ctrl.domain.EmpDataCtrlVO;
import kr.go.molit.icas.emp.plan.domain.EmpPlanVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 데이터 품질 통제 비즈니스 서비스 (SFR-005).
 *
 * <h2>Upsert 패턴</h2>
 * <ol>
 *   <li>부모 plan 조회 → 없으면 NOT_FOUND</li>
 *   <li>가시범위 검증</li>
 *   <li>부모 plan 상태 DRAFT 확인</li>
 *   <li>도메인별 비즈니스 검증</li>
 *   <li>existsByPlanId → insert 또는 update</li>
 * </ol>
 *
 * <h2>클래스 레벨 트랜잭션</h2>
 * <ul>
 *   <li>기본 {@code readOnly=true}</li>
 *   <li>변경 메서드({@code upsert*}, {@code softDelete*})는 {@code @Transactional} 오버라이드</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmpDataCtrlService {

    private final EmpDataCtrlMapper  empDataCtrlMapper;
    private final EmpPlanMapper      empPlanMapper;
    private final DataScopeValidator dataScopeValidator;

    // ══════════════════════════════════════════════════════
    // 조회
    // ══════════════════════════════════════════════════════

    /**
     * 데이터 품질 통제 단건 조회.
     *
     * <p>자식 1:1 은 미작성 정상 — 없으면 null 반환.
     *
     * @param empPlanId EMP Plan ID
     * @param user      로그인 사용자
     * @return 데이터 품질 통제 VO (미작성 시 null)
     * @throws BusinessException NOT_FOUND — 부모 plan 미존재
     * @throws BusinessException FORBIDDEN — 가시범위 밖
     */
    public EmpDataCtrlVO selectByPlanId(String empPlanId, IcasUser user) {
        EmpPlanVO plan = loadPlan(empPlanId);
        dataScopeValidator.assertOprtrAccessible(user, plan.getOprtrId(), plan.getRprtYr());
        return empDataCtrlMapper.selectByPlanId(empPlanId);
    }

    // ══════════════════════════════════════════════════════
    // Upsert
    // ══════════════════════════════════════════════════════

    /**
     * 데이터 품질 통제 Upsert (insert or update).
     *
     * <p>권한: 본인 항공사(AIRLINE), 부모 plan DRAFT 상태 한정.
     *
     * @param empPlanId EMP Plan ID (path variable)
     * @param vo        입력 데이터
     * @param user      로그인 사용자
     * @throws BusinessException NOT_FOUND  — 부모 plan 미존재
     * @throws BusinessException FORBIDDEN  — 가시범위 밖
     * @throws BusinessException CONFLICT   — 부모 plan 이 DRAFT 아님
     * @throws BusinessException BAD_REQUEST — 도메인 검증 실패
     */
    @Transactional
    public void upsertDataCtrl(String empPlanId, EmpDataCtrlVO vo, IcasUser user) {
        // 1. 부모 plan 조회
        EmpPlanVO plan = loadPlan(empPlanId);

        // 2. 가시범위 검증 (본인 항공사만)
        dataScopeValidator.assertOwnAirline(user, plan.getOprtrId());

        // 3. 부모 plan 상태 검증 — DRAFT 만 허용
        assertPlanDraft(plan);

        // 4. 도메인 비즈니스 검증
        validateDataCtrl(vo);

        // 5. Upsert
        vo.setEmpPlanId(empPlanId);
        if (empDataCtrlMapper.existsByPlanId(empPlanId)) {
            vo.setLastChgUserId(user.getUserId());
            empDataCtrlMapper.updateEmpDataCtrl(vo);
        } else {
            vo.setFrstRegUserId(user.getUserId());
            vo.setLastChgUserId(user.getUserId());
            empDataCtrlMapper.insertEmpDataCtrl(vo);
        }
    }

    // ══════════════════════════════════════════════════════
    // 소프트삭제
    // ══════════════════════════════════════════════════════

    /**
     * 데이터 품질 통제 소프트삭제.
     *
     * <p>권한: 본인 항공사, 부모 plan DRAFT 상태 한정.
     *
     * @param empPlanId EMP Plan ID
     * @param user      로그인 사용자
     * @throws BusinessException NOT_FOUND — 부모 plan 미존재
     * @throws BusinessException FORBIDDEN — 가시범위 밖
     * @throws BusinessException CONFLICT  — 부모 plan 이 DRAFT 아님
     */
    @Transactional
    public void softDeleteByPlanId(String empPlanId, IcasUser user) {
        EmpPlanVO plan = loadPlan(empPlanId);
        dataScopeValidator.assertOwnAirline(user, plan.getOprtrId());
        assertPlanDraft(plan);
        empDataCtrlMapper.softDeleteByPlanId(empPlanId, user.getUserId());
    }

    // ══════════════════════════════════════════════════════
    // Private Helpers
    // ══════════════════════════════════════════════════════

    /**
     * 부모 plan 조회 (유효구간 필터 포함).
     *
     * @throws BusinessException NOT_FOUND — plan 미존재
     */
    private EmpPlanVO loadPlan(String empPlanId) {
        EmpPlanVO plan = empPlanMapper.selectByEmpPlanId(empPlanId);
        if (plan == null) {
            throw BusinessException.notFound("EMP Plan");
        }
        return plan;
    }

    /**
     * 부모 plan 상태가 DRAFT 인지 검증.
     *
     * @throws BusinessException CONFLICT — DRAFT 아닌 경우
     */
    private void assertPlanDraft(EmpPlanVO plan) {
        if (!"DRAFT".equals(plan.getEmpStCd())) {
            throw BusinessException.conflict(
                    "DRAFT 상태에서만 자식 정보를 수정할 수 있습니다. 현재 상태: " + plan.getEmpStCd());
        }
    }

    /**
     * 데이터 품질 통제 도메인 검증.
     *
     * <ul>
     *   <li>gap_thrshld_5pct Y/N 만 허용 (null 이면 기본값 Y 적용 — DB 에서 COALESCE 처리)</li>
     * </ul>
     *
     * @throws BusinessException BAD_REQUEST — 검증 실패
     */
    private void validateDataCtrl(EmpDataCtrlVO vo) {
        String gap = vo.getGapThrshld5pct();
        if (gap != null && !"Y".equals(gap) && !"N".equals(gap)) {
            throw BusinessException.badRequest(
                    "5% 격차 임계값 적용 여부(gapThrshld5pct)는 Y 또는 N 만 허용됩니다. 입력값: " + gap);
        }
    }
}
