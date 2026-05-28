package kr.go.molit.icas.ptl.stat;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 연도별 탄소배출 통계 집계 VO — ptl.tn_ptl_stat_yearly 1:1 매핑.
 * PK: (rprt_yr, oprtr_id)
 * 배치 갱신용 집계 테이블 — 이력 컬럼(use_bgng_dt 등) 없음.
 */
@Data
public class PtlStatYearlyVO {
    /** 보고연도 (4자리) — PK */
    private String rprtYr;
    /** 운영사 ID — PK + FK → com.tn_oprtr */
    private String oprtrId;
    /** 운영사 명 (JOIN 조회 시 사용) */
    private String oprtrNm;
    /** ICAO 항공사 코드 (JOIN 조회 시 사용) */
    private String icaoDesig;
    /** 총 CO2 배출량 (tCO2) */
    private BigDecimal ttlCo2Emsn;
    /** 총 상쇄 신청량 */
    private BigDecimal ttlOfstReq;
    /** 총 CEF 감축량 */
    private BigDecimal ttlCefRedu;
    /** 총 운항 횟수 */
    private Integer ttlFltCnt;
    /** 총 연료 중량 (ton) */
    private BigDecimal ttlFuelWght;
    /** 총 SAF 사용량 (ton) */
    private BigDecimal ttlSafQty;
    /** 데이터 갭 운항 수 */
    private Integer dataGapCnt;
    /** 마지막 집계 일시 */
    private LocalDateTime lastAggrDt;
}
