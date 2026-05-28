package kr.go.molit.icas.emp.plan.acft.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 항공기 VO (emp.TN_EMP_ACFT).
 *
 * <p>복합 PK: (emp_plan_id, acft_sn).
 * SFR-003 항공기 유형·연료·대수 섹션에 해당.
 *
 * <p>MyBatis {@code mapUnderscoreToCamelCase=true} 설정에 의해
 * snake_case 컬럼이 camelCase 필드로 자동 매핑된다.
 */
@Getter
@Setter
public class EmpAcftVO {

    // ── PK / FK ──────────────────────────────────────────
    /** EMP Plan ID (PK, FK → emp.tn_emp_plan) */
    private String empPlanId;

    /**
     * 항공기 일련번호 (PK).
     * max(acft_sn) + 1 자동 채번.
     */
    private int acftSn;

    // ── 업무 컬럼 ─────────────────────────────────────────
    /**
     * 항공기 유형 코드 (필수).
     * FK → com.tc_acft_type_cd
     */
    private String acftTypeCd;

    /**
     * 연료 유형 코드 (필수).
     * FK → com.tc_fuel_type_cd
     */
    private String fuelTypeCd;

    /**
     * 항공기 대수 (필수, 1 이상).
     */
    private int acftCnt;

    /** 비고 (최대 1000자, nullable) */
    private String rmrk;

    // ── 공통 유효구간 ──────────────────────────────────────
    /** 유효 시작일시 */
    private LocalDateTime useBgngDt;

    /** 유효 종료일시 */
    private LocalDateTime useEndDt;

    // ── 공통 감사 컬럼 ─────────────────────────────────────
    /** 최초 등록일시 */
    private LocalDateTime frstRegDt;

    /** 최초 등록 사용자 ID */
    private String frstRegUserId;

    /** 최종 변경일시 */
    private LocalDateTime lastChgDt;

    /** 최종 변경 사용자 ID */
    private String lastChgUserId;
}
