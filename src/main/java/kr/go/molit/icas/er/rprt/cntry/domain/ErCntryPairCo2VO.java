package kr.go.molit.icas.er.rprt.cntry.domain;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 국가 쌍 배출량 VO (er.tn_er_cntry_pair_co2).
 *
 * <p>복합 PK: (er_id, pair_sn).
 * 1개 ER 에 복수의 국가 쌍 배출량이 등록됨 (1:N).
 *
 * <p>MyBatis {@code mapUnderscoreToCamelCase=true} 설정에 의해
 * snake_case 컬럼이 camelCase 필드로 자동 매핑된다.
 */
@Getter
@Setter
public class ErCntryPairCo2VO {

    // ── PK / FK ──────────────────────────────────────────

    /** ER ID (PK, FK → er.tn_er) */
    private String erId;

    /**
     * 국가 쌍 일련번호 (PK).
     * 같은 er_id 의 max(pair_sn) + 1 자동 채번.
     */
    private int pairSn;

    // ── 업무 컬럼 ─────────────────────────────────────────

    /** 출발 국가 코드 (char 2, ICAO 2-letter) */
    private String dprtrCntryCd;

    /** 도착 국가 코드 (char 2, ICAO 2-letter) */
    private String arvlCntryCd;

    /**
     * CORSIA 인증 추정 여부 (char 1, Y/N, 기본값 N).
     * 화이트리스트: Y / N
     */
    private String cerEstmYn;

    /** 항공편 수 (int, 0 이상) */
    private Integer fltCnt;

    /** 연료 유형 코드 (varchar 20, 필수) */
    private String fuelTypeCd;

    /** 연료 중량 (numeric 20,4, 0 이상) */
    private BigDecimal fuelWght;

    /** 변환 계수 (numeric 10,4, 필수, 0 초과) */
    private BigDecimal convFctr;

    /** CO₂ 배출량 (numeric 20,4, 0 이상) */
    private BigDecimal co2Emsn;

    /**
     * 상쇄 요건 여부 (char 1, Y/N, 기본값 N).
     * 화이트리스트: Y / N
     */
    private String ofstReqYn;

    /** CEF 감축량 (numeric 20,4, 0 이상) */
    private BigDecimal cefReduAmt;

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
