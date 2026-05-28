package kr.go.molit.icas.er.oom.rqst.domain;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * OoM 추가 설명 요청 VO — er.tn_oom_check_addl_rqst 1:1 매핑 (SFR-033).
 *
 * <p>복합 PK: (oom_id, rqst_sn). KOTSA 가 요청 → AIRLINE 이 응답.
 */
@Data
public class OomCheckAddlRqstVO {

    /** PK 부분 — FK → er.tn_oom_check.oom_id */
    private String oomId;

    /** PK 부분 — 요청 일련번호 (max+1 자동 채번) */
    private int rqstSn;

    /** 요청일시 (자동 NOW) */
    private LocalDateTime rqstDt;

    /** 요청자 사용자 ID (KOTSA) */
    private String rqstUserId;

    /** 요청 내용 (text) */
    private String rqstCn;

    /** 응답일시 (AIRLINE 응답 시 NOW) */
    private LocalDateTime respDt;

    /** 응답자 사용자 ID (AIRLINE) */
    private String respUserId;

    /** 응답 내용 (text) */
    private String respCn;

    // ── 공통 ──
    private LocalDateTime useBgngDt;
    private LocalDateTime useEndDt;
    private LocalDateTime frstRegDt;
    private String        frstRegUserId;
    private LocalDateTime lastChgDt;
    private String        lastChgUserId;
}
