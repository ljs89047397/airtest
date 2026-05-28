package kr.go.molit.icas.ptl.workflow;

import kr.go.molit.icas.ptl.workflow.domain.WorkflowRowVO;
import kr.go.molit.icas.ptl.workflow.domain.WorkflowSearch;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface WorkflowMapper {
    /**
     * 운영사별 전 도메인 최신 상태 매트릭스 조회.
     * search.rprtYr 는 필수. search.oprtrId 가 null 이면 전사 반환.
     */
    List<WorkflowRowVO> selectWorkflow(WorkflowSearch search);
}
