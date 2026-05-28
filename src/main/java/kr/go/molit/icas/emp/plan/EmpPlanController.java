package kr.go.molit.icas.emp.plan;

import kr.go.molit.icas.common.dto.ApiResponse;
import kr.go.molit.icas.common.dto.PageResponse;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.emp.plan.domain.EmpChgHstryVO;
import kr.go.molit.icas.emp.plan.domain.EmpPlanSearch;
import kr.go.molit.icas.emp.plan.domain.EmpPlanVO;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * EMP Plan REST API Controller.
 *
 * <pre>
 * GET    /api/emp/plan              — 목록 검색 (페이징)
 * GET    /api/emp/plan/{id}         — 단건 조회
 * GET    /api/emp/plan/{id}/history — 변경 이력 목록
 * POST   /api/emp/plan              — 신규 DRAFT 생성 (AIRLINE)
 * PUT    /api/emp/plan/{id}         — DRAFT 수정 (AIRLINE)
 * DELETE /api/emp/plan/{id}         — 소프트삭제 (DRAFT 한정, AIRLINE)
 * POST   /api/emp/plan/{id}/submit      — 제출 (AIRLINE)
 * POST   /api/emp/plan/{id}/review      — 검토 진입 (KOTSA)
 * POST   /api/emp/plan/{id}/reject      — 반려 (KOTSA, 사유 필수)
 * POST   /api/emp/plan/{id}/recommend   — 권고 (KOTSA)
 * POST   /api/emp/plan/{id}/approve     — 승인 (MOLIT)
 * POST   /api/emp/plan/{id}/cancel      — 취소 (MOLIT, 사유 필수)
 * POST   /api/emp/plan/{id}/new-version — 신버전 생성 (AIRLINE)
 * </pre>
 */
@RestController
@RequestMapping("/api/emp/plan")
@RequiredArgsConstructor
public class EmpPlanController {

    private final EmpPlanService empPlanService;

    // ──────────────────────────────────────────────
    // 조회
    // ──────────────────────────────────────────────

    /**
     * EMP Plan 목록 검색 (페이징).
     * 역할별 가시범위는 Service 에서 자동 적용.
     */
    @GetMapping
    public ApiResponse<PageResponse<EmpPlanVO>> list(
            @ModelAttribute EmpPlanSearch search,
            @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(empPlanService.searchEmpPlans(search, user));
    }

    /** EMP Plan 단건 조회 */
    @GetMapping("/{id}")
    public ApiResponse<EmpPlanVO> get(
            @PathVariable("id") String empPlanId,
            @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(empPlanService.getEmpPlan(empPlanId, user));
    }

    /** EMP Plan 변경 이력 목록 (최신순) */
    @GetMapping("/{id}/history")
    public ApiResponse<List<EmpChgHstryVO>> history(
            @PathVariable("id") String empPlanId,
            @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(empPlanService.getEmpPlanHistory(empPlanId, user));
    }

    // ──────────────────────────────────────────────
    // 등록 / 수정 / 삭제
    // ──────────────────────────────────────────────

    /** DRAFT 신규 생성 (AIRLINE) */
    @PostMapping
    public ApiResponse<EmpPlanVO> create(
            @RequestBody EmpPlanVO vo,
            @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(empPlanService.createEmpPlan(vo, user), "EMP Plan 이 등록되었습니다.");
    }

    /** DRAFT 마스터 수정 (AIRLINE) */
    @PutMapping("/{id}")
    public ApiResponse<Void> update(
            @PathVariable("id") String empPlanId,
            @RequestBody EmpPlanVO vo,
            @AuthenticationPrincipal IcasUser user) {
        empPlanService.updateEmpPlan(empPlanId, vo, user);
        return ApiResponse.ok(null, "EMP Plan 이 수정되었습니다.");
    }

    /** 소프트삭제 (DRAFT 한정, AIRLINE) */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(
            @PathVariable("id") String empPlanId,
            @AuthenticationPrincipal IcasUser user) {
        empPlanService.softDeleteEmpPlan(empPlanId, user);
        return ApiResponse.ok(null, "EMP Plan 이 삭제되었습니다.");
    }

    // ──────────────────────────────────────────────
    // 상태 전이
    // ──────────────────────────────────────────────

    /** 제출 (DRAFT → SBMTD, AIRLINE) */
    @PostMapping("/{id}/submit")
    public ApiResponse<Void> submit(
            @PathVariable("id") String empPlanId,
            @AuthenticationPrincipal IcasUser user) {
        empPlanService.submit(empPlanId, user);
        return ApiResponse.ok(null, "EMP Plan 이 제출되었습니다.");
    }

    /** 검토 진입 (SBMTD → RVWNG, KOTSA) */
    @PostMapping("/{id}/review")
    public ApiResponse<Void> review(
            @PathVariable("id") String empPlanId,
            @AuthenticationPrincipal IcasUser user) {
        empPlanService.review(empPlanId, user);
        return ApiResponse.ok(null, "EMP Plan 검토가 시작되었습니다.");
    }

    /** 반려 (RVWNG → DRAFT, KOTSA, 사유 필수) */
    @PostMapping("/{id}/reject")
    public ApiResponse<Void> reject(
            @PathVariable("id") String empPlanId,
            @RequestBody ReasonRequest body,
            @AuthenticationPrincipal IcasUser user) {
        empPlanService.reject(empPlanId, body.getReason(), user);
        return ApiResponse.ok(null, "EMP Plan 이 반려되었습니다.");
    }

    /** 권고 (RVWNG → RCMDD, KOTSA) */
    @PostMapping("/{id}/recommend")
    public ApiResponse<Void> recommend(
            @PathVariable("id") String empPlanId,
            @AuthenticationPrincipal IcasUser user) {
        empPlanService.recommend(empPlanId, user);
        return ApiResponse.ok(null, "EMP Plan 이 권고 처리되었습니다.");
    }

    /** 승인 (RVWNG 또는 RCMDD → APRVD, MOLIT) */
    @PostMapping("/{id}/approve")
    public ApiResponse<Void> approve(
            @PathVariable("id") String empPlanId,
            @AuthenticationPrincipal IcasUser user) {
        empPlanService.approve(empPlanId, user);
        return ApiResponse.ok(null, "EMP Plan 이 승인되었습니다.");
    }

    /** 취소 (APRVD → CNCLD, MOLIT, 사유 필수) */
    @PostMapping("/{id}/cancel")
    public ApiResponse<Void> cancel(
            @PathVariable("id") String empPlanId,
            @RequestBody ReasonRequest body,
            @AuthenticationPrincipal IcasUser user) {
        empPlanService.cancel(empPlanId, body.getReason(), user);
        return ApiResponse.ok(null, "EMP Plan 이 취소되었습니다.");
    }

    /** 신버전 생성 (AIRLINE) */
    @PostMapping("/{id}/new-version")
    public ApiResponse<EmpPlanVO> newVersion(
            @PathVariable("id") String baseEmpPlanId,
            @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(empPlanService.createNewVersion(baseEmpPlanId, user),
                "EMP Plan 신버전이 생성되었습니다.");
    }

    // ──────────────────────────────────────────────
    // 내부 요청 DTO
    // ──────────────────────────────────────────────

    /** reject / cancel 공통 사유 요청 바디 */
    @Getter
    @Setter
    public static class ReasonRequest {
        private String reason;
    }
}
