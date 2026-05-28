package kr.go.molit.icas.emp.plan.domain;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * EMP 마스터 VO — emp.tn_emp_plan 1:1 매핑.
 *
 * <ul>
 *   <li>상태 코드 ({@code emp_st_cd}): DRAFT / SBMTD / RVWNG / RCMDD / APRVD / CNCLD</li>
 *   <li>UK 제약: (oprtr_id, emp_ver)</li>
 *   <li>운영사별 1개 활성 버전 — approve 시 직전 버전 자동 만료</li>
 * </ul>
 */
@Data
public class EmpPlanVO {

    /** PK — EP + 4자리 (예: EP0001) */
    private String empPlanId;

    /** FK → com.tn_oprtr.oprtr_id */
    private String oprtrId;

    /** 운영사명 (JOIN 조회용, INSERT/UPDATE 미포함) */
    private String oprtrNm;

    /** EMP 버전 (예: 1.0, 2.0) */
    private String empVer;

    /**
     * EMP 상태 코드.
     * DRAFT(작성중) / SBMTD(제출됨) / RVWNG(검토중) / RCMDD(권고됨) / APRVD(승인) / CNCLD(취소)
     * 기본값: DRAFT
     */
    private String empStCd;

    /** 보고연도 (char 4, 예: 2026) */
    private String rprtYr;

    /** 제출일시 */
    private LocalDateTime sbmtDt;

    /** 승인일시 */
    private LocalDateTime aprvDt;

    /** 승인 사용자 ID */
    private String aprvUserId;

    /** 반려일시 */
    private LocalDateTime rjctDt;

    /** 반려 사유 (최대 2000자) */
    private String rjctRsn;

    /**
     * 중대 변경 여부 (char 1).
     * Y: 중대 변경 (재승인 대상) / N: 비중대 변경.
     * 기본값: N
     */
    private String sigChgYn;

    /** 직전 버전 EMP Plan ID (같은 oprtr 의 직전 버전, nullable) */
    private String prevEmpPlanId;

    /** 비고 (text) */
    private String rmrk;

    /** 유효 시작일시 */
    private LocalDateTime useBgngDt;

    /** 유효 종료일시 */
    private LocalDateTime useEndDt;

    /** 최초 등록일시 */
    private LocalDateTime frstRegDt;

    /** 최초 등록 사용자 ID */
    private String frstRegUserId;

    /** 최종 변경일시 */
    private LocalDateTime lastChgDt;

    /** 최종 변경 사용자 ID */
    private String lastChgUserId;
}
