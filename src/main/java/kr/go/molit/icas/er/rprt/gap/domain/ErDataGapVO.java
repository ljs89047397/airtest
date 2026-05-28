package kr.go.molit.icas.er.rprt.gap.domain;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 데이터 갭 VO (er.tn_er_data_gap).
 *
 * <p>복합 PK: (er_id, gap_sn).
 * 1개 ER 에 복수의 데이터 갭이 등록됨 (1:N).
 *
 * <p>{@code thrshld_5pct_xc_yn} 은 사용자 입력값을 무시하고
 * insert/update 시점에 자동 계산된다 (SFR-014).
 * 계산식: afct_co2_emsn / sum(cntry_pair.co2_emsn) ≥ 5% → 'Y', 아니면 'N'.
 * 총 CO₂ = 0 이면 정보 부족으로 안전한 쪽(Y) 으로 판정.
 *
 * <p>MyBatis {@code mapUnderscoreToCamelCase=true} 설정에 의해
 * snake_case 컬럼이 camelCase 필드로 자동 매핑된다.
 */
@Getter
@Setter
public class ErDataGapVO {

    // ── PK / FK ──────────────────────────────────────────

    /** ER ID (PK, FK → er.tn_er) */
    private String erId;

    /**
     * 데이터 갭 일련번호 (PK).
     * 같은 er_id 의 max(gap_sn) + 1 자동 채번.
     */
    private int gapSn;

    // ── 업무 컬럼 ─────────────────────────────────────────

    /** 갭 발생일 */
    private LocalDate gapDt;

    /** 참조 정보 (varchar 500) */
    private String refInfo;

    /** 갭 원인 코드 (varchar 20, nullable, 자유 입력) */
    private String gapCauseCd;

    /** 갭 유형 코드 (varchar 20, nullable, 자유 입력) */
    private String gapTypeCd;

    /** 대체 방법 설명 (text) */
    private String replMthdDesc;

    /** 갭 영향 CO₂ 배출량 (numeric 20,4, 0 이상) */
    private BigDecimal afctCo2Emsn;

    /**
     * 5% 임계치 초과 여부 (char 1, 자동 판정).
     * Y: afct_co2_emsn / total_co2 ≥ 5%  또는  total_co2 = 0 (정보 부족)
     * N: afct_co2_emsn / total_co2 < 5%
     * 사용자 입력값 무시 — insert/update 시점에 서비스 레이어에서 강제 설정.
     */
    private String thrshld5pctXcYn;

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
