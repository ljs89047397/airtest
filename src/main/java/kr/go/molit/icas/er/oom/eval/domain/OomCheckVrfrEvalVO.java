package kr.go.molit.icas.er.oom.eval.domain;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * OoM 검증기관 품질 평가 VO — er.tn_oom_check_vrfr_eval 1:1 매핑 (SFR-033).
 *
 * <p>복합 PK: (oom_id, vrfcn_inst_id). VERIFIER 가 자신의 vrfcn_inst_id 로 1행 등록.
 */
@Data
public class OomCheckVrfrEvalVO {

    /** PK 부분 — FK → er.tn_oom_check.oom_id */
    private String oomId;

    /** PK 부분 — FK → com.tn_vrfcn_inst.vrfcn_inst_id */
    private String vrfcnInstId;

    /** 검증기관명 (JOIN 조회용) */
    private String vrfcnInstNm;

    /** 평가 등급: GOOD / AVG / POOR (필수) */
    private String evalGrdCd;

    /** 평가 비고 (text) */
    private String evalRmrk;

    // ── 공통 ──
    private LocalDateTime useBgngDt;
    private LocalDateTime useEndDt;
    private LocalDateTime frstRegDt;
    private String        frstRegUserId;
    private LocalDateTime lastChgDt;
    private String        lastChgUserId;
}
