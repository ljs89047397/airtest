package kr.go.molit.icas.ptl.workflow.domain;

import lombok.Data;

/**
 * 통합 워크플로우 검색 조건 VO.
 * 전체 운영사 매트릭스 조회용 — PageRequest 미상속 (전체 반환).
 * rprtYr 는 필수값.
 */
@Data
public class WorkflowSearch {
    /** 보고연도 (필수) */
    private String rprtYr;
    /** 특정 운영사 ID (null 이면 전사 조회) */
    private String oprtrId;
}
