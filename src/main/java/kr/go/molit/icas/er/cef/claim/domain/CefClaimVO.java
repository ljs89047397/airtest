package kr.go.molit.icas.er.cef.claim.domain;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * CEF 청구건 VO — er.tn_cef_claim 1:1 매핑 (SFR-017).
 *
 * <p>복합 PK: (cef_id, claim_no). batch_id_no 는 이중청구 스캔 대상.
 */
@Data
public class CefClaimVO {

    /** PK 부분 — FK → er.tn_cef.cef_id */
    private String cefId;

    /** PK 부분 — 청구건 번호 (사용자 채번 가능, 영문/숫자) */
    private String claimNo;

    /** 순수 연료 구매일 (필수) */
    private LocalDate pureFuelPurchDt;

    /** 연료 생산사명 */
    private String fuelPrdcCoNm;

    /** 연료 생산사 주소 */
    private String fuelPrdcAddr;

    /** 연료 생산일 */
    private LocalDate fuelPrdcDt;

    /** 연료 생산지 */
    private String fuelPrdcLc;

    /** 연료 유형 코드 (FK → com 공통코드 FUEL_TYPE_CD, 필수) */
    private String fuelTypeCd;

    /** 원료 유형 코드 (FK → com 공통코드 FDSTK_TYPE_CD) */
    private String fdstkTypeCd;

    /** 전환 공정 코드 (FK → com 공통코드 CONV_PROC_CD) */
    private String convProcCd;

    /**
     * 배치 ID 번호 (필수, 최대 100자).
     * 이중청구 스캔 키. 시스템 내 전 DB 교차 검색 대상.
     */
    private String batchIdNo;

    /** 순수 연료 질량 (필수, numeric 20,4, kg) */
    private BigDecimal pureFuelMass;

    /** 배치 구매 비율 (numeric 7,4, 0~1.0) */
    private BigDecimal batchPurchRatio;

    /** 배치 구매 질량 (numeric 20,4, kg) — pure_fuel_mass / batch_purch_ratio 산출 가능 */
    private BigDecimal batchPurchMass;

    // ── 공통 유효구간 / 감사 ─────────────────────────────────
    private LocalDateTime useBgngDt;
    private LocalDateTime useEndDt;
    private LocalDateTime frstRegDt;
    private String        frstRegUserId;
    private LocalDateTime lastChgDt;
    private String        lastChgUserId;
}
