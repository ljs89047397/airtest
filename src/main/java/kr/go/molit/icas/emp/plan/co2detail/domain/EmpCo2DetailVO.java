package kr.go.molit.icas.emp.plan.co2detail.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 방법별 CO2 측정 상세 VO (emp.TN_EMP_CO2_DETAIL).
 *
 * <p>복합 PK: (emp_plan_id, mntr_mthd_cd). sn 없이 mntr_mthd_cd 가 자연키.
 * SFR-004 배출량 계산방법 상세 섹션에 해당.
 *
 * <p>MyBatis {@code mapUnderscoreToCamelCase=true} 설정에 의해
 * snake_case 컬럼이 camelCase 필드로 자동 매핑된다.
 */
@Getter
@Setter
public class EmpCo2DetailVO {

    // ── PK / FK ──────────────────────────────────────────
    /** EMP Plan ID (PK, FK → emp.tn_emp_plan) */
    private String empPlanId;

    /**
     * 모니터링 방법 코드 (PK, 자연키).
     * 허용값: MTHD_A / MTHD_B / BLOCK_ON_OFF / REFUEL / BLOCK_ALLOC
     */
    private String mntrMthdCd;

    // ── 업무 컬럼 ─────────────────────────────────────────
    /** 측정 시점 설명 (최대 2000자, nullable) */
    private String msrTmingDesc;

    /** 측정 장치 설명 (최대 2000자, nullable) */
    private String msrDeviceDesc;

    /** 측정 절차 설명 (최대 2000자, nullable) */
    private String msrProcDesc;

    /** 연료 밀도 설명 (최대 2000자, nullable) */
    private String fuelDnstyDesc;

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
