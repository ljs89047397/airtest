package kr.go.molit.icas.er.rprt.aerdrm.domain;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 비행장 쌍 배출량 VO (er.tn_er_aerdrm_pair_co2).
 *
 * <p>복합 PK: (er_id, pair_sn).
 * 1개 ER 에 복수의 비행장 쌍 배출량이 등록됨 (1:N).
 *
 * <p>MyBatis {@code mapUnderscoreToCamelCase=true} 설정에 의해
 * snake_case 컬럼이 camelCase 필드로 자동 매핑된다.
 */
@Getter
@Setter
public class ErAerdrmPairCo2VO {

    // ── PK / FK ──────────────────────────────────────────

    /** ER ID (PK, FK → er.tn_er) */
    private String erId;

    /**
     * 비행장 쌍 일련번호 (PK).
     * 같은 er_id 의 max(pair_sn) + 1 자동 채번.
     */
    private int pairSn;

    // ── 업무 컬럼 ─────────────────────────────────────────

    /** 출발 비행장 코드 (char 4, ICAO 4-letter) */
    private String dprtrAerdrmCd;

    /** 도착 비행장 코드 (char 4, ICAO 4-letter) */
    private String arvlAerdrmCd;

    /** 출발 국가 코드 (char 2, ICAO 2-letter) */
    private String dprtrCntryCd;

    /** 도착 국가 코드 (char 2, ICAO 2-letter) */
    private String arvlCntryCd;

    /** 항공편 수 (int, 0 이상) */
    private Integer fltCnt;

    /** 연료 유형 코드 (varchar 20, 필수) */
    private String fuelTypeCd;

    /** 연료 중량 (numeric 20,4, 0 이상) */
    private BigDecimal fuelWght;

    /** CO₂ 배출량 (numeric 20,4, 0 이상) */
    private BigDecimal co2Emsn;

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
