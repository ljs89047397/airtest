package kr.go.molit.icas.er.rprt.afbr.domain;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 항공기 유형별 평균 연료연소율 VO (er.tn_er_afbr).
 *
 * <p>자연키 PK: (er_id, acft_type_cd) — sn 채번 없음.
 * SFR-010 평균 연료연소율 섹션에 해당.
 *
 * <p>MyBatis {@code mapUnderscoreToCamelCase=true} 설정에 의해
 * snake_case 컬럼이 camelCase 필드로 자동 매핑된다.
 */
@Getter
@Setter
public class ErAfbrVO {

    // ── PK / FK ──────────────────────────────────────────
    /** ER ID (PK, FK → er.tn_er) */
    private String erId;

    /** 항공기 유형 코드 (PK, FK → com 공통코드 ACFT_TYPE_CD) */
    private String acftTypeCd;

    // ── 업무 컬럼 ─────────────────────────────────────────
    /**
     * 평균 연료연소율 값 (필수, > 0).
     * numeric(10,4)
     */
    private BigDecimal afbrVal;

    /**
     * 연소율 단위 (기본값: 'kg/min').
     * 허용값: kg/min / lb/min / kg/hr
     */
    private String afbrUnit;

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
