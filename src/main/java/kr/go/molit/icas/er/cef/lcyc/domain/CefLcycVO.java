package kr.go.molit.icas.er.cef.lcyc.domain;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * CEF 수명주기 배출량 VO — er.tn_cef_lcyc 1:1 매핑 (SFR-018).
 *
 * <p>복합 PK: (cef_id, claim_no). 각 청구건당 0..1 행 (옵셔널).
 * 단위: gCO2eq/MJ.
 */
@Data
public class CefLcycVO {

    /** PK 부분 — FK → er.tn_cef_claim.cef_id */
    private String cefId;

    /** PK 부분 — FK → er.tn_cef_claim.claim_no */
    private String claimNo;

    /**
     * LCA 값 구분 코드 (필수).
     * 허용값: DEFAULT (ICAO 기본값) / ACTUAL (실측값)
     */
    private String lcaValueSeCd;

    /** 핵심 LCA 값 (gCO2eq/MJ) */
    private BigDecimal coreLcaVal;

    /** ILUC(간접 토지이용 변화) 배출량 */
    private BigDecimal ilucEmsn;

    /** 총 LCA 값 (core + iluc) */
    private BigDecimal ttlLcaVal;

    /** 지속가능성 증빙 파일 ID (FK → com.tn_file) */
    private String susEvidFileId;

    // ── 공통 유효구간 / 감사 ─────────────────────────────────
    private LocalDateTime useBgngDt;
    private LocalDateTime useEndDt;
    private LocalDateTime frstRegDt;
    private String        frstRegUserId;
    private LocalDateTime lastChgDt;
    private String        lastChgUserId;
}
