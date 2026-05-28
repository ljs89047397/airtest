package kr.go.molit.icas.emp.plan.co2;

import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.DataScopeValidator;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.emp.plan.EmpPlanMapper;
import kr.go.molit.icas.emp.plan.co2.domain.EmpCo2CalcVO;
import kr.go.molit.icas.emp.plan.domain.EmpPlanVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Set;

/**
 * 배출량 계산방법 비즈니스 서비스 (SFR-004).
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
public class EmpCo2CalcService {

    /** 허용된 모니터링 방법론 코드 집합 (ICAO CORSIA ETM Annex 기준) */
    private static final Set<String> ALLOWED_MNTR_MTHD_CD =
            Set.of("MTHD_A", "MTHD_B", "BLOCK_ON_OFF", "REFUEL", "BLOCK_ALLOC");

    private final EmpCo2CalcMapper   empCo2CalcMapper;
    private final EmpPlanMapper      empPlanMapper;
    private final DataScopeValidator dataScopeValidator;

    // ══════════════════════════════════════════════════════
    // 조회
    // ══════════════════════════════════════════════════════

    /**
     * 배출량 계산방법 단건 조회.
     *
     * <p>자식 1:1 은 미작성 정상 — 없으면 null 반환.
     *
     * @param empPlanId EMP Plan ID
     * @param user      로그인 사용자
     * @return 배출량 계산방법 VO (미작성 시 null)
     * @throws BusinessException NOT_FOUND — 부모 plan 미존재
     * @throws BusinessException FORBIDDEN — 가시범위 밖
     */
    public EmpCo2CalcVO selectByPlanId(String empPlanId, IcasUser user) {
        EmpPlanVO plan = loadPlan(empPlanId);
        dataScopeValidator.assertOprtrAccessible(user, plan.getOprtrId(), plan.getRprtYr());
        return empCo2CalcMapper.selectByPlanId(empPlanId);
    }

    // ══════════════════════════════════════════════════════
    // Upsert
    // ══════════════════════════════════════════════════════

    /**
     * 배출량 계산방법 Upsert (insert or update).
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
    public void upsertCo2Calc(String empPlanId, EmpCo2CalcVO vo, IcasUser user) {
        // 1. 부모 plan 조회
        EmpPlanVO plan = loadPlan(empPlanId);

        // 2. 가시범위 검증 (본인 항공사만)
        dataScopeValidator.assertOwnAirline(user, plan.getOprtrId());

        // 3. 부모 plan 상태 검증 — DRAFT 만 허용
        assertPlanDraft(plan);

        // 4. 도메인 비즈니스 검증
        validateCo2Calc(vo);

        // 5. Upsert
        vo.setEmpPlanId(empPlanId);
        if (empCo2CalcMapper.existsByPlanId(empPlanId)) {
            vo.setLastChgUserId(user.getUserId());
            empCo2CalcMapper.updateEmpCo2Calc(vo);
        } else {
            vo.setFrstRegUserId(user.getUserId());
            vo.setLastChgUserId(user.getUserId());
            empCo2CalcMapper.insertEmpCo2Calc(vo);
        }
    }

    // ══════════════════════════════════════════════════════
    // 소프트삭제
    // ══════════════════════════════════════════════════════

    /**
     * 배출량 계산방법 소프트삭제.
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
        empCo2CalcMapper.softDeleteByPlanId(empPlanId, user.getUserId());
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
     * 배출량 계산방법 도메인 검증.
     *
     * <ul>
     *   <li>mntr_mthd_cd 화이트리스트 검증 (MTHD_A/MTHD_B/BLOCK_ON_OFF/REFUEL/BLOCK_ALLOC)</li>
     *   <li>fuel_dnsty_se_cd 필수</li>
     *   <li>cert_use_yn Y/N 만 허용</li>
     *   <li>est_co2_emsn 음수 금지</li>
     * </ul>
     *
     * @throws BusinessException BAD_REQUEST — 검증 실패
     */
    private void validateCo2Calc(EmpCo2CalcVO vo) {
        if (isBlank(vo.getMntrMthdCd())) {
            throw BusinessException.badRequest("모니터링 방법론 코드(mntrMthdCd)는 필수입니다.");
        }
        if (!ALLOWED_MNTR_MTHD_CD.contains(vo.getMntrMthdCd())) {
            throw BusinessException.badRequest(
                    "허용되지 않는 모니터링 방법론 코드입니다: " + vo.getMntrMthdCd()
                    + ". 허용 값: " + ALLOWED_MNTR_MTHD_CD);
        }
        if (isBlank(vo.getFuelDnstySecd())) {
            throw BusinessException.badRequest("연료 밀도 구분 코드(fuelDnstySecd)는 필수입니다.");
        }
        if (!isBlank(vo.getCertUseYn()) && !"Y".equals(vo.getCertUseYn()) && !"N".equals(vo.getCertUseYn())) {
            throw BusinessException.badRequest(
                    "CERT 사용 여부(certUseYn)는 Y 또는 N 만 허용됩니다. 입력값: " + vo.getCertUseYn());
        }
        if (vo.getEstCo2Emsn() != null && vo.getEstCo2Emsn().compareTo(BigDecimal.ZERO) < 0) {
            throw BusinessException.badRequest("추정 CO2 배출량(estCo2Emsn)은 음수를 입력할 수 없습니다.");
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
