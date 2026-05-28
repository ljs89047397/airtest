package kr.go.molit.icas.er.rprt.domain;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ER(Emission Report) 마스터 VO — er.tn_er 1:1 매핑.
 *
 * <ul>
 *   <li>상태 코드 ({@code er_st_cd}): DRAFT / SBMTD / RVWNG / RCMDD / APRVD / CNCLD</li>
 *   <li>UK 제약: (oprtr_id, rprt_yr, er_ver)</li>
 *   <li>EMP Plan 참조: emp_plan_id_apld 입력 시 APRVD 상태 EMP 검증 + 자동 prefill</li>
 * </ul>
 */
@Data
public class ErVO {

    // ── PK ───────────────────────────────────────────────────────

    /** PK — ER + 4자리 (예: ER0001) */
    private String erId;

    // ── UK 구성 컬럼 ──────────────────────────────────────────────

    /** FK → com.tn_oprtr.oprtr_id */
    private String oprtrId;

    /** 운영사명 (JOIN 조회용, INSERT/UPDATE 미포함) */
    private String oprtrNm;

    /** 보고연도 (char 4, 예: 2026) */
    private String rprtYr;

    /** ER 버전 (기본값: '1.0') */
    private String erVer;

    // ── 상태 ──────────────────────────────────────────────────────

    /**
     * ER 상태 코드.
     * DRAFT(작성중) / SBMTD(제출됨) / RVWNG(검토중) / RCMDD(권고됨) / APRVD(승인) / CNCLD(취소)
     * 기본값: DRAFT
     */
    private String erStCd;

    // ── 보고 기간 ─────────────────────────────────────────────────

    /** 보고기간 종료일 */
    private LocalDate rprtPrdEndDt;

    /** 발행일 */
    private LocalDate isueDt;

    // ── 적용 EMP Plan 참조 (자동 prefill) ─────────────────────────

    /** 적용 EMP Plan ID (FK → emp.tn_emp_plan) */
    private String empPlanIdApld;

    /** 적용 EMP 버전 (emp_plan 에서 prefill) */
    private String empVerApld;

    /** EMP 승인일 (emp_plan.aprv_dt 에서 prefill) */
    private LocalDate empAprvDt;

    /** EMP 적용 시작일 (emp_plan.use_bgng_dt 에서 prefill) */
    private LocalDate empEffDt;

    /** EMP 최종 수정일 (emp_plan.last_chg_dt 에서 prefill) */
    private LocalDate empUpdtDt;

    // ── 업무 플래그 ───────────────────────────────────────────────

    /**
     * CORSIA 인증 사용 여부 (char 1, Y/N).
     * 기본값: N
     */
    private String certUseYn;

    /**
     * 상쇄 배분 사용 여부 (char 1, Y/N).
     * 기본값: N
     */
    private String allcUseYn;

    // ── 상태 전이 일시 ────────────────────────────────────────────

    /** 제출일시 */
    private LocalDateTime sbmtDt;

    /** 승인일시 */
    private LocalDateTime aprvDt;

    /** 승인 사용자 ID */
    private String aprvUserId;

    /** 반려일시 */
    private LocalDateTime rjctDt;

    /** 반려/취소 사유 (최대 2000자) */
    private String rjctRsn;

    // ── 공통 유효구간 ─────────────────────────────────────────────

    /** 유효 시작일시 */
    private LocalDateTime useBgngDt;

    /** 유효 종료일시 */
    private LocalDateTime useEndDt;

    // ── 공통 감사 컬럼 ────────────────────────────────────────────

    /** 최초 등록일시 */
    private LocalDateTime frstRegDt;

    /** 최초 등록 사용자 ID */
    private String frstRegUserId;

    /** 최종 변경일시 */
    private LocalDateTime lastChgDt;

    /** 최종 변경 사용자 ID */
    private String lastChgUserId;
}
