package kr.go.molit.icas.er.rprt.vrfr.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * ER 참여 검증기관 정보 VO (er.tn_er_vrfr_info).
 *
 * <p>복합 PK: (er_id, vrfr_sn).
 * 1개 ER 에 복수의 검증기관이 참여할 수 있음 (1:N).
 *
 * <p>MyBatis {@code mapUnderscoreToCamelCase=true} 설정에 의해
 * snake_case 컬럼이 camelCase 필드로 자동 매핑된다.
 */
@Getter
@Setter
public class ErVrfrInfoVO {

    // ── PK / FK ──────────────────────────────────────────

    /** ER ID (PK, FK → er.tn_er) */
    private String erId;

    /**
     * 검증기관 일련번호 (PK).
     * max(vrfr_sn) + 1 자동 채번.
     */
    private int vrfrSn;

    /** 검증기관 ID (FK → com.tn_vrfcn_inst) */
    private String vrfcnInstId;

    // ── 업무 컬럼 ─────────────────────────────────────────

    /**
     * 참여 개요 설명 (최대 500자).
     */
    private String cnctDesc;

    /**
     * 인증 상세 설명 (최대 1000자).
     */
    private String accrdDtl;

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
