package kr.go.molit.icas.saf.batch.prdc.domain;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * SAF 생산사·공급사 정보 VO — saf.tn_saf_prdc_sply (1:1 with tn_saf_batch).
 * PK: batch_id (shared with batch master)
 */
@Data
public class SafPrdcSplyVO {
    private String batchId;
    private String prdcCoInfo;
    private String prdcPosBatchId;
    private LocalDate prdcPosIsueDt;
    private BigDecimal orgnSafQty;
    private LocalDate safPrdcDt;
    private LocalDate acqstnDt;
    private String prdcLcAddr;
    private String splyCoInfo;
    private LocalDateTime frstRegDt;
    private String frstRegUserId;
    private LocalDateTime lastChgDt;
    private String lastChgUserId;
}
