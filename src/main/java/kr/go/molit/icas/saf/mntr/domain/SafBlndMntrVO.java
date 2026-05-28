package kr.go.molit.icas.saf.mntr.domain;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * SAF 혼합비율 의무 모니터링 VO — saf.tn_saf_blnd_mntr.
 * PK: (oprtr_id, rprt_yr)
 * 자동 산출 — SafBlndMntrService.runCalc() 가 갱신.
 */
@Data
public class SafBlndMntrVO {
    private String oprtrId;
    private String rprtYr;
    /** 연간 총 급유량 (kg) */
    private BigDecimal totalFuelQty;
    /** SAF 인증서 구매량 (kg) */
    private BigDecimal safCertPurchQty;
    /** 혼합비율 (%) = safCertPurchQty / totalFuelQty × 100 */
    private BigDecimal blndRatio;
    /** 국가 고시 의무비율 (%, 예: 1.0) */
    private BigDecimal oblgRatio;
    /** 의무 이행 여부: Y / N */
    private String fulfilledYn;
    private LocalDateTime lastCalcDt;
    private LocalDateTime frstRegDt;
    private String frstRegUserId;
    private LocalDateTime lastChgDt;
    private String lastChgUserId;
}
