package kr.go.molit.icas.er.oom.domain;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * OoM-check 마스터 VO — er.tn_oom_check 1:1 매핑 (SFR-033).
 *
 * <p>UK: (oprtr_id, rprt_yr) — 운영사+보고연도당 1행.
 *
 * <h2>상태 / 결과 코드</h2>
 * <ul>
 *   <li>oom_st_cd  — INPRG(진행중) / DONE(완료)</li>
 *   <li>oom_rslt_cd — PASS / FAIL / HOLD</li>
 * </ul>
 */
@Data
public class OomCheckVO {

    /** PK — OOM + 4자리 */
    private String oomId;

    /** FK → com.tn_oprtr.oprtr_id */
    private String oprtrId;

    /** 운영사명 (JOIN 조회용) */
    private String oprtrNm;

    /** 보고연도 (char 4) */
    private String rprtYr;

    /** 점검 대상 ER ID (옵션, FK → er.tn_er) */
    private String erId;

    /** 점검 대상 VR ID (옵션) */
    private String vrId;

    /** 진행 상태: INPRG / DONE */
    private String oomStCd;

    /** 결과 코드: PASS / FAIL / HOLD (DONE 시 필수) */
    private String oomRsltCd;

    /** 점검일 */
    private LocalDate inspnDt;

    /** 점검자 사용자 ID */
    private String inspctrUserId;

    // ── 공통 ──
    private LocalDateTime useBgngDt;
    private LocalDateTime useEndDt;
    private LocalDateTime frstRegDt;
    private String        frstRegUserId;
    private LocalDateTime lastChgDt;
    private String        lastChgUserId;
}
