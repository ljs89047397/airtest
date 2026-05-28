package kr.go.molit.icas.er.eucr.domain;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * EUCR(Emission Unit Cancellation Report) 마스터 VO — er.tn_eucr 1:1 매핑 (SFR-030).
 *
 * <p>UK: (oprtr_id, rprt_yr, eucr_ver).
 * 라이프사이클: DRAFT → SBMTD → RVWNG → RCMDD → APRVD → CNCLD (ER 동일 패턴).
 *
 * <h2>자동 산출 컬럼</h2>
 * <ul>
 *   <li>{@code ttl_qty} — 자식 batch.sub_qty 합계 (자동)</li>
 *   <li>{@code fulfilled_yn} — ttl_qty ≥ ofst_req_qty 시 'Y' (자동)</li>
 * </ul>
 */
@Data
public class EucrVO {

    /** PK — EUCR + 4자리 */
    private String eucrId;

    /** FK → com.tn_oprtr.oprtr_id */
    private String oprtrId;

    /** 운영사명 (JOIN 조회용) */
    private String oprtrNm;

    /** 보고연도 (char 4) */
    private String rprtYr;

    /** EUCR 버전 (기본 '1.0') */
    private String eucrVer;

    /**
     * 상태 코드.
     * DRAFT / SBMTD / RVWNG / RCMDD / APRVD / CNCLD
     */
    private String eucrStCd;

    /** 총 취소량 (자동 합계) */
    private BigDecimal ttlQty;

    /** 상쇄 의무량 (국가통보값, 사용자 입력) */
    private BigDecimal ofstReqQty;

    /** 의무 충족 여부 (Y/N, ttl_qty ≥ ofst_req_qty 시 'Y') */
    private String fulfilledYn;

    /** 제출일시 */
    private LocalDateTime sbmtDt;

    /** 승인일시 */
    private LocalDateTime aprvDt;

    // ── 공통 유효구간 / 감사 ──
    private LocalDateTime useBgngDt;
    private LocalDateTime useEndDt;
    private LocalDateTime frstRegDt;
    private String        frstRegUserId;
    private LocalDateTime lastChgDt;
    private String        lastChgUserId;
}
