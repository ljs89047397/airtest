package kr.go.molit.icas.ptl.workflow;

import kr.go.molit.icas.common.dto.ApiResponse;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.ptl.workflow.domain.WorkflowRowVO;
import kr.go.molit.icas.ptl.workflow.domain.WorkflowSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 통합 워크플로우 REST API.
 *
 * <pre>
 * GET /api/ptl/workflow?rprtYr=2026&amp;oprtrId=OP0001 — 워크플로우 조회
 * </pre>
 */
@RestController
@RequestMapping("/api/ptl/workflow")
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowService workflowService;

    /** 통합 워크플로우 매트릭스 조회 */
    @GetMapping
    public ApiResponse<List<WorkflowRowVO>> getWorkflow(
            WorkflowSearch search,
            @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(workflowService.getWorkflow(search, user));
    }
}
