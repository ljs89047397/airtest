package kr.go.molit.icas.er.rprt.acft.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 항공기·연료 VO (er.tn_er_acft_fuel).
 *
 * <p>복합 PK: (er_id, acft_sn).
 * SFR-011 운항 항공기·연료 섹션에 해당.
 *
 * <p>MyBatis {@code mapUnderscoreToCamelCase=true} 설정에 의해
 * snake_case 컬럼이 camelCase 필드로 자동 매핑된다.
 */
@Getter
@Setter
public class ErAcftFuelVO {

    // ── PK / FK ──────────────────────────────────────────
    /** ER ID (PK, FK → er.tn_er) */
    private String erId;

    /**
     * 항공기 일련번호 (PK).
     * 같은 er_id 내 max(acft_sn) + 1 자동 채번.
     */
    private int acftSn;

    // ── 업무 컬럼 ─────────────────────────────────────────
    /** 항공기 유형 코드 (FK → com 공통코드 ACFT_TYPE_CD) */
    private String acftTypeCd;

    /** 항공기 등록기호 (필수, 최대 20자) */
    private String regisMark;

    /**
     * 소유/리스 구분 코드 (nullable).
     * 허용값: OWN (자가 소유) / LEASE (리스)
     */
    private String ownrLsSeCd;

    /** 연료 유형 코드 (필수, FK → com 공통코드 FUEL_TYPE_CD) */
    private String fuelTypeCd;

    /**
     * 밀도 구분 코드 (nullable).
     * 허용값: STD (표준 밀도) / ACT (실측 밀도)
     */
    private String dnstySecCd;

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
