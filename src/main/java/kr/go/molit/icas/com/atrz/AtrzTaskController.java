package kr.go.molit.icas.com.atrz;

import kr.go.molit.icas.com.atrz.domain.AtrzTaskVO;
import kr.go.molit.icas.common.dto.ApiResponse;
import kr.go.molit.icas.common.security.IcasUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 결재 업무 마스터 REST Controller.
 *
 * <pre>
 * GET    /api/com/atrz-task               — 목록 (sysSeCd 필터)
 * GET    /api/com/atrz-task/{taskId}      — 단건 조회
 * POST   /api/com/atrz-task               — 등록 (MOLIT/KOTSA 전용)
 * PUT    /api/com/atrz-task/{taskId}      — 수정 (MOLIT/KOTSA 전용)
 * DELETE /api/com/atrz-task/{taskId}      — 소프트삭제 (MOLIT/KOTSA 전용)
 * </pre>
 */
@RestController
@RequestMapping("/api/com/atrz-task")
@RequiredArgsConstructor
public class AtrzTaskController {

    private final AtrzTaskService atrzTaskService;

    /**
     * 결재 업무 목록 조회.
     *
     * @param sysSeCd 시스템 구분 코드 필터 (선택, 예: EMP)
     * @param user    로그인 사용자
     */
    @GetMapping
    public ApiResponse<List<AtrzTaskVO>> listTasks(
            @RequestParam(required = false) String sysSeCd,
            @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(atrzTaskService.selectAtrzTasks(sysSeCd));
    }

    /**
     * 결재 업무 단건 조회.
     *
     * @param taskId 결재 업무 ID
     * @param user   로그인 사용자
     */
    @GetMapping("/{taskId}")
    public ApiResponse<AtrzTaskVO> getTask(
            @PathVariable String taskId,
            @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(atrzTaskService.selectByTaskId(taskId));
    }

    /**
     * 결재 업무 등록. MOLIT/KOTSA 전용.
     *
     * @param vo   등록 VO (atrzTaskId 수동 입력 필수)
     * @param user 로그인 사용자
     */
    @PostMapping
    public ApiResponse<AtrzTaskVO> createTask(
            @RequestBody AtrzTaskVO vo,
            @AuthenticationPrincipal IcasUser user) {
        AtrzTaskVO created = atrzTaskService.createAtrzTask(vo, user);
        return ApiResponse.ok(created, "결재 업무가 등록되었습니다.");
    }

    /**
     * 결재 업무 수정. MOLIT/KOTSA 전용.
     *
     * @param taskId 결재 업무 ID
     * @param vo     수정 VO
     * @param user   로그인 사용자
     */
    @PutMapping("/{taskId}")
    public ApiResponse<Void> updateTask(
            @PathVariable String taskId,
            @RequestBody AtrzTaskVO vo,
            @AuthenticationPrincipal IcasUser user) {
        atrzTaskService.updateAtrzTask(taskId, vo, user);
        return ApiResponse.ok(null, "결재 업무가 수정되었습니다.");
    }

    /**
     * 결재 업무 소프트삭제. MOLIT/KOTSA 전용.
     *
     * @param taskId 결재 업무 ID
     * @param user   로그인 사용자
     */
    @DeleteMapping("/{taskId}")
    public ApiResponse<Void> deleteTask(
            @PathVariable String taskId,
            @AuthenticationPrincipal IcasUser user) {
        atrzTaskService.softDeleteAtrzTask(taskId, user);
        return ApiResponse.ok(null, "결재 업무가 삭제되었습니다.");
    }
}
