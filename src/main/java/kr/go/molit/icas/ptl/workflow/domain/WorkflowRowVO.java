package kr.go.molit.icas.ptl.workflow.domain;

import lombok.Data;

/**
 * 통합 워크플로우 행 VO.
 * 별도 테이블 없음 — com.tn_oprtr 기준으로 각 도메인 최신 상태를 LEFT JOIN/서브쿼리로 조합.
 * null 상태 코드 = 해당 연도 미작성.
 */
@Data
public class WorkflowRowVO {
    private String oprtrId;
    private String oprtrNm;
    private String icaoDesig;
    private String rprtYr;

    // 각 도메인 최신 상태코드 (null = 미작성)
    /** emp.tn_emp_plan.emp_st_cd */
    private String empStCd;
    /** er.tn_er.er_st_cd */
    private String erStCd;
    /** er.tn_cef.cef_st_cd */
    private String cefStCd;
    /** er.tn_eucr.eucr_st_cd */
    private String eucrStCd;
    /** vr.tn_vr.vr_st_cd */
    private String vrStCd;
    /** er.tn_oom_check.oom_st_cd */
    private String oomStCd;

    // 각 도메인 최신 ID (드릴다운용)
    private String empPlanId;
    private String erId;
    private String cefId;
    private String eucrId;
    private String vrId;
    private String oomId;
}
