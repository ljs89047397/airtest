package kr.go.molit.icas.emp.plan.cntry.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 운항 국가 쌍 VO (emp.TN_EMP_CNTRY_PAIR).
 *
 * <p>복합 PK: (emp_plan_id, pair_sn).
 * SFR-003 운항 국가 쌍 목록 섹션에 해당.
 *
 * <p>MyBatis {@code mapUnderscoreToCamelCase=true} 설정에 의해
 * snake_case 컬럼이 camelCase 필드로 자동 매핑된다.
 */
@Getter
@Setter
public class EmpCntryPairVO {

    // ── PK / FK ──────────────────────────────────────────
    /** EMP Plan ID (PK, FK → emp.tn_emp_plan) */
    private String empPlanId;

    /**
     * 국가쌍 일련번호 (PK).
     * max(pair_sn) + 1 자동 채번.
     */
    private int pairSn;

    // ── 업무 컬럼 ─────────────────────────────────────────
    /**
     * 출발 국가 코드 (char 2, 필수).
     * FK → com.tc_cntry_cd
     */
    private String dprtrCntryCd;

    /**
     * 도착 국가 코드 (char 2, 필수).
     * FK → com.tc_cntry_cd
     */
    private String arvlCntryCd;

    /**
     * 국제선 여부 (char 1, Y/N, default Y).
     * intl_yn = 'Y' 이면서 dprtrCntryCd == arvlCntryCd 이면 badRequest.
     */
    private String intlYn;

    /**
     * 면제 코드 (nullable).
     * 허용값: HUMANITARIAN / MEDICAL / FIRE / null
     */
    private String exemptCd;

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
