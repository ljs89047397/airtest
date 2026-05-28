package kr.go.molit.icas.saf.airprt.fuel.domain;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 공항별 급유 실적 VO — saf.tn_saf_airprt_fuel.
 * PK: (airprt_id, rprt_yr, oprtr_id)
 * 탱커링 90% 규제 계산 기반 데이터.
 */
@Data
public class SafAirprtFuelVO {
    /** ICAO 4자리 공항코드 */
    private String airprtId;
    /** 보고연도 */
    private String rprtYr;
    /** 항공사 운영사 ID */
    private String oprtrId;
    /** 항공편 수 */
    private Integer fltCnt;
    /** 비행 시간 (hours) */
    private BigDecimal fltTime;
    /** 필요 연료량 (kg) */
    private BigDecimal reqFuelQty;
    /** 실제 급유량 (kg) */
    private BigDecimal actlFuelQty;
    /** 연간 탱커링하지 않은 양 (kg) */
    private BigDecimal yrNonTankedQty;
    /** 안전 탱커링 허용량 (kg) */
    private BigDecimal yrTankedSafetyQty;
    private LocalDateTime useBgngDt;
    private LocalDateTime useEndDt;
    private LocalDateTime frstRegDt;
    private String frstRegUserId;
    private LocalDateTime lastChgDt;
    private String lastChgUserId;
}
