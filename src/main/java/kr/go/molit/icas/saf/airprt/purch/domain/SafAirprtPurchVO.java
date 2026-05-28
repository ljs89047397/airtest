package kr.go.molit.icas.saf.airprt.purch.domain;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 공항별 SAF 구매 VO — saf.tn_saf_airprt_purch.
 * PK: (airprt_id, rprt_yr, oprtr_id, purch_sn)
 */
@Data
public class SafAirprtPurchVO {
    private String airprtId;
    private String rprtYr;
    private String oprtrId;
    private Integer purchSn;
    /** 공급사 정보 */
    private String splyCoInfo;
    /** SAF 배치 ID (FK → saf.tn_saf_batch) */
    private String batchId;
    /** 구매량 (kg) */
    private BigDecimal purchQty;
    /** 연료 유형 코드 */
    private String fuelTypeCd;
    /** 원산지 정보 */
    private String orgnInfo;
    private LocalDateTime frstRegDt;
    private String frstRegUserId;
    private LocalDateTime lastChgDt;
    private String lastChgUserId;
}
