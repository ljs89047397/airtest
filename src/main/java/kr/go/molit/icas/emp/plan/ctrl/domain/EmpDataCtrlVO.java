package kr.go.molit.icas.emp.plan.ctrl.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 데이터 품질 통제 VO (emp.TN_EMP_DATA_CTRL).
 *
 * <p>emp_plan_id 가 PK 이자 FK (→ emp.TN_EMP_PLAN). 1 plan당 1 행.
 * SFR-005 데이터 품질 통제 섹션에 해당.
 *
 * <p>MyBatis {@code mapUnderscoreToCamelCase=true} 설정에 의해
 * snake_case 컬럼이 camelCase 필드로 자동 매핑된다.
 */
@Getter
@Setter
public class EmpDataCtrlVO {

    // ── PK / FK ──────────────────────────────────────────
    /** EMP Plan ID (PK, FK → emp.tn_emp_plan) */
    private String empPlanId;

    // ── 데이터 흐름 ─────────────────────────────────────────
    /**
     * 데이터 흐름 기술 (text).
     * 연료 소비 데이터의 수집·처리·저장 흐름을 서술.
     */
    private String flowDesc;

    // ── 5% 격차 임계값 ──────────────────────────────────────
    /**
     * 5% 격차 임계값 적용 여부 (char 1, Y/N, default Y).
     * ICAO 5% 데이터 갭 임계값 기준 적용 여부.
     */
    private String gapThrshld5pct;

    // ── 보조 데이터 ─────────────────────────────────────────
    /**
     * 보조 데이터 소스 사용 기술 (text).
     * 주 데이터 소스 외 보조 데이터 소스 사용 현황 서술.
     */
    private String sndSrcUseDesc;

    // ── 위험 분석 ───────────────────────────────────────────
    /**
     * 위험 분석 서술 (text).
     * 데이터 품질에 영향을 미칠 수 있는 위험 요소 분석.
     */
    private String riskAnlys;

    // ── 중대 변경 승인 절차 ─────────────────────────────────
    /**
     * 중대 변경 승인 절차 서술 (text).
     * 운영 중 EMP 중대 변경 시 내부 승인 프로세스 기술.
     */
    private String sigChgAprvProc;

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
