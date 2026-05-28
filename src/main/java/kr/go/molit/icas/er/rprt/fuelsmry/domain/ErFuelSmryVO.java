package kr.go.molit.icas.er.rprt.fuelsmry.domain;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 연료 유형별 총사용량 요약 VO (er.tn_er_fuel_smry).
 *
 * <p>자연키 PK: (er_id, fuel_type_cd) — sn 채번 없음.
 * SFR-012 연료 유형별 총사용량 섹션에 해당.
 *
 * <p>MyBatis {@code mapUnderscoreToCamelCase=true} 설정에 의해
 * snake_case 컬럼이 camelCase 필드로 자동 매핑된다.
 */
@Getter
@Setter
public class ErFuelSmryVO {

    // ── PK / FK ──────────────────────────────────────────
    /** ER ID (PK, FK → er.tn_er) */
    private String erId;

    /** 연료 유형 코드 (PK, FK → com 공통코드 FUEL_TYPE_CD) */
    private String fuelTypeCd;

    // ── 업무 컬럼 ─────────────────────────────────────────
    /**
     * 총 연료 중량 (기본값: 0, >= 0).
     * numeric(20,4)
     */
    private BigDecimal ttlFuelWght;

    /**
     * 총 CO2 배출량 (기본값: 0, >= 0).
     * numeric(20,4)
     */
    private BigDecimal ttlCo2Emsn;

    // ── 공통 유효구간 ──────────────────────────────────────
    /** 유효 시작일시 */
    private LocalDateTime useBgngDt;

    /** 유효 종료일시 */
    private LocalDateTime useEndDt;

    // ── 공통 감사 컬럼 ─────────────────────────────────────
    /** 최초 등록일시 */
    private LocalDateTime frstRegDt;

    /** 최초 등록 사용자 ID */
    private String frstRegUserId;

    /** 최종 변경일시 */
    private LocalDateTime lastChgDt;

    /** 최종 변경 사용자 ID */
    private String lastChgUserId;
}
