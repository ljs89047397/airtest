package kr.go.molit.icas.saf.batch.ghg.domain;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * SAF 온실가스 배출 VO — saf.tn_saf_ghg (1:1 with tn_saf_batch).
 * PK: batch_id
 * ghg_val_se_cd: DEFAULT / ACTUAL
 * 단위: gCO2eq/MJ
 */
@Data
public class SafGhgVO {
    private String batchId;
    /** GHG 값 구분: DEFAULT / ACTUAL */
    private String ghgValSeCd;
    /** 핵심 LCA 기본값 (gCO2eq/MJ) */
    private BigDecimal coreLcaDefVal;
    /** ILUC 배출량 (gCO2eq/MJ) */
    private BigDecimal ilucEmsn;
    /** 총 LCA 기본값 = coreLcaDefVal + ilucEmsn */
    private BigDecimal ttlLcaDefVal;
    private LocalDateTime frstRegDt;
    private String frstRegUserId;
    private LocalDateTime lastChgDt;
    private String lastChgUserId;
}
