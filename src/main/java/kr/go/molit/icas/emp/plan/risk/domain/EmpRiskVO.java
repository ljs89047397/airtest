package kr.go.molit.icas.emp.plan.risk.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 위험·통제 항목 VO (emp.TN_EMP_RISK).
 *
 * <p>복합 PK: (emp_plan_id, risk_sn).
 * SFR-005 데이터 품질 통제 - 위험·통제 항목 섹션에 해당.
 *
 * <p>MyBatis {@code mapUnderscoreToCamelCase=true} 설정에 의해
 * snake_case 컬럼이 camelCase 필드로 자동 매핑된다.
 */
@Getter
@Setter
public class EmpRiskVO {

    // ── PK / FK ──────────────────────────────────────────
    /** EMP Plan ID (PK, FK → emp.tn_emp_plan) */
    private String empPlanId;

    /**
     * 위험 항목 일련번호 (PK).
     * max(risk_sn) + 1 자동 채번.
     */
    private int riskSn;

    // ── 업무 컬럼 ─────────────────────────────────────────
    /**
     * 위험 설명 (필수, 최대 2000자).
     */
    private String riskDesc;

    /**
     * 통제 활동 설명 (최대 2000자, nullable).
     */
    private String ctrlActv;

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
