package kr.go.molit.icas.er.cef.spchn.domain;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * CEF 공급망 VO — er.tn_cef_spchn 1:1 매핑 (SFR-019).
 *
 * <p>복합 PK: (cef_id, claim_no, chn_sn). 청구건당 1:N (중간 거래처, 운송사, 혼합사).
 */
@Data
public class CefSpchnVO {

    /** PK 부분 — FK → er.tn_cef_claim.cef_id */
    private String cefId;

    /** PK 부분 — FK → er.tn_cef_claim.claim_no */
    private String claimNo;

    /**
     * PK 부분 — 공급망 일련번호.
     * 같은 (cef_id, claim_no) 내 max(chn_sn) + 1 자동 채번.
     */
    private int chnSn;

    /**
     * 공급망 역할 코드 (필수).
     * 허용값: MID_BUYER (중간 구매자) / SHIPPER (운송사) / BLENDER (혼합사)
     */
    private String splyChnRoleCd;

    /** 회사명 */
    private String coNm;

    /** 회사 주소 */
    private String coAddr;

    /** 지점/하역지 주소 */
    private String lcAddr;

    /** 인수일 */
    private LocalDate recvDt;

    /** 인수 질량 (kg) */
    private BigDecimal recvMass;

    /** 혼합비율 (0~1.0, BLENDER 인 경우) */
    private BigDecimal blndRatio;

    /** 혼합 증빙 파일 ID (FK → com.tn_file) */
    private String blndEvidFileId;

    // ── 공통 유효구간 / 감사 ─────────────────────────────────
    private LocalDateTime useBgngDt;
    private LocalDateTime useEndDt;
    private LocalDateTime frstRegDt;
    private String        frstRegUserId;
    private LocalDateTime lastChgDt;
    private String        lastChgUserId;
}
