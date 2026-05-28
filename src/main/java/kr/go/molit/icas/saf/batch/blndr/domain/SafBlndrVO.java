package kr.go.molit.icas.saf.batch.blndr.domain;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * SAF 혼합사 정보 VO — saf.tn_saf_blndr (1:1 with tn_saf_batch).
 * PK: batch_id
 */
@Data
public class SafBlndrVO {
    private String batchId;
    private String blndrCoInfo;
    private String blndLcAddr;
    private LocalDate recvDt;
    private BigDecimal recvMass;
    private String fuelTypeCd;
    private BigDecimal blndRatio;
    private String trnsprtCoInfo;
    private String midBuyerCoInfo;
    private LocalDateTime frstRegDt;
    private String frstRegUserId;
    private LocalDateTime lastChgDt;
    private String lastChgUserId;
}
