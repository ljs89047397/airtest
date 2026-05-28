package kr.go.molit.icas.er.cef.domain;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * CEF(CORSIA Eligible Fuel) 마스터 VO — er.tn_cef 1:1 매핑 (SFR-017/020).
 *
 * <p>ER 의 부속(1:0..1). UK 제약: er_id 단일.
 *
 * <p>라이프사이클: DRAFT → SBMTD → APRVD → CNCLD.
 * ttl_redu_amt 는 자식 청구건의 pure_fuel_mass 합계로 자동 재계산.
 */
@Data
public class CefVO {

    /** PK — CEF + 4자리 (예: CEF0001) */
    private String cefId;

    /** FK → er.tn_er.er_id (UK, 1 ER : 0..1 CEF) */
    private String erId;

    /** FK → com.tn_oprtr.oprtr_id */
    private String oprtrId;

    /** 운영사명 (JOIN 조회용, INSERT/UPDATE 미포함) */
    private String oprtrNm;

    /** 보고연도 (char 4) */
    private String rprtYr;

    /**
     * CEF 상태 코드.
     * DRAFT(작성중) / SBMTD(제출됨) / APRVD(승인) / CNCLD(취소)
     */
    private String cefStCd;

    /** 총 감축량 (numeric 20,4). 청구건 자동 합계 */
    private BigDecimal ttlReduAmt;

    /** 제출일시 */
    private LocalDateTime sbmtDt;

    /** 승인일시 */
    private LocalDateTime aprvDt;

    // ── 공통 유효구간 / 감사 ─────────────────────────────────
    private LocalDateTime useBgngDt;
    private LocalDateTime useEndDt;
    private LocalDateTime frstRegDt;
    private String        frstRegUserId;
    private LocalDateTime lastChgDt;
    private String        lastChgUserId;
}
