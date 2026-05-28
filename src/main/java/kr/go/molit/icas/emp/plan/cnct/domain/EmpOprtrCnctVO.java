package kr.go.molit.icas.emp.plan.cnct.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 담당자 연락처 VO (emp.TN_EMP_OPRTR_CNCT).
 *
 * <p>복합 PK: (emp_plan_id, cnct_sn).
 * SFR-002 담당자·대체담당자 연락처 섹션에 해당.
 *
 * <p>MyBatis {@code mapUnderscoreToCamelCase=true} 설정에 의해
 * snake_case 컬럼이 camelCase 필드로 자동 매핑된다.
 */
@Getter
@Setter
public class EmpOprtrCnctVO {

    // ── PK / FK ──────────────────────────────────────────
    /** EMP Plan ID (PK, FK → emp.tn_emp_plan) */
    private String empPlanId;

    /**
     * 연락처 일련번호 (PK).
     * max(cnct_sn) + 1 자동 채번.
     */
    private int cnctSn;

    // ── 업무 컬럼 ─────────────────────────────────────────
    /**
     * 연락처 구분 코드.
     * 허용값: PRIMARY(담당자) / SUB(대체담당자).
     */
    private String cnctSeCd;

    /** 성명 (필수) */
    private String userNm;

    /** 휴대전화번호 (mblphn_no, eml_addr 중 1개 이상 필수) */
    private String mblphnNo;

    /** 이메일 주소 (mblphn_no, eml_addr 중 1개 이상 필수) */
    private String emlAddr;

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
