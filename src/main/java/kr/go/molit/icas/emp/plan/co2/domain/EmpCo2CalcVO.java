package kr.go.molit.icas.emp.plan.co2.domain;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 배출량 계산방법 VO (emp.TN_EMP_CO2_CALC).
 *
 * <p>emp_plan_id 가 PK 이자 FK (→ emp.TN_EMP_PLAN). 1 plan당 1 행.
 * SFR-004 배출량 계산방법 섹션에 해당.
 *
 * <p>MyBatis {@code mapUnderscoreToCamelCase=true} 설정에 의해
 * snake_case 컬럼이 camelCase 필드로 자동 매핑된다.
 */
@Getter
@Setter
public class EmpCo2CalcVO {

    // ── PK / FK ──────────────────────────────────────────
    /** EMP Plan ID (PK, FK → emp.tn_emp_plan) */
    private String empPlanId;

    // ── 모니터링 방법론 ─────────────────────────────────────
    /**
     * 모니터링 방법론 코드 (필수).
     *
     * <p>허용 값: MTHD_A, MTHD_B, BLOCK_ON_OFF, REFUEL, BLOCK_ALLOC
     * ICAO CORSIA ETM Annex 에서 정의된 5가지 방법론.
     */
    private String mntrMthdCd;

    // ── CERT 사용 여부 ──────────────────────────────────────
    /**
     * CERT 사용 여부 (char 1, Y/N, default N).
     * CORSIA Eligible Fuels 관련 인증 연료 사용 여부.
     */
    private String certUseYn;

    /**
     * CERT 등록 방법 코드 (nullable, varchar 20).
     * certUseYn = 'Y' 인 경우에 한해 입력.
     */
    private String certRegisMthdCd;

    // ── 연료 밀도 ───────────────────────────────────────────
    /**
     * 연료 밀도 구분 코드 (필수, varchar 20).
     * 실측/표준값 중 선택 사용.
     */
    private String fuelDnstySecd;

    // ── CO2 추정치 ──────────────────────────────────────────
    /**
     * 추정 CO2 배출량 (numeric 20,4, 단위: tonne).
     * 항공기·국가쌍·연료 기반 ICAO CERT 또는 수식으로 산출.
     */
    private BigDecimal estCo2Emsn;

    /**
     * 추정 CO2 산출 근거 서술 (varchar 2000).
     * 추정치 산출 방식·가정 등을 서술.
     */
    private String estCo2Basis;

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
