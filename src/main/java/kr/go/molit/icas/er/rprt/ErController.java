package kr.go.molit.icas.er.rprt;

import kr.go.molit.icas.common.dto.ApiResponse;
import kr.go.molit.icas.common.dto.PageResponse;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.er.rprt.domain.ErSearch;
import kr.go.molit.icas.er.rprt.domain.ErVO;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * ER(Emission Report) 마스터 REST API 컨트롤러.
 *
 * <pre>
 * GET    /api/er/rprt              — 목록 검색 (페이징)
 * GET    /api/er/rprt/{erId}       — 단건 조회
 * POST   /api/er/rprt              — 신규 DRAFT 생성 (AIRLINE)
 * PUT    /api/er/rprt/{erId}       — DRAFT 수정 (AIRLINE)
 * DELETE /api/er/rprt/{erId}       — 소프트삭제 (DRAFT 한정, AIRLINE)
 * POST   /api/er/rprt/{erId}/submit     — 제출 (AIRLINE)
 * POST   /api/er/rprt/{erId}/review     — 검토 진입 (KOTSA)
 * POST   /api/er/rprt/{erId}/reject     — 반려 (KOTSA, 사유 필수)
 * POST   /api/er/rprt/{erId}/recommend  — 권고 (KOTSA)
 * POST   /api/er/rprt/{erId}/approve    — 승인 (MOLIT)
 * POST   /api/er/rprt/{erId}/cancel     — 취소 (MOLIT, 사유 필수)
 * </pre>
 */
@RestController
@RequestMapping("/api/er/rprt")
@RequiredArgsConstructor
public class ErController {

    private final ErService erService;

    // ──────────────────────────────────────────────
    // 조회
    // ──────────────────────────────────────────────

    /**
     * ER 목록 검색 (페이징).
     * 역할별 가시범위는 Service 에서 자동 적용.
     */
    @GetMapping
    public ApiResponse<PageResponse<ErVO>> list(
            @ModelAttribute ErSearch search,
            @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(erService.searchErs(search, user));
    }

    /** ER 단건 조회 */
    @GetMapping("/{erId}")
    public ApiResponse<ErVO> get(
            @PathVariable String erId,
            @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(erService.getEr(erId, user));
    }

    // ──────────────────────────────────────────────
    // 등록 / 수정 / 삭제
    // ──────────────────────────────────────────────

    /** DRAFT 신규 생성 (AIRLINE) */
    @PostMapping
    public ApiResponse<ErVO> create(
            @RequestBody ErVO vo,
            @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(erService.createEr(vo, user), "ER 이 등록되었습니다.");
    }

    /** DRAFT 마스터 수정 (AIRLINE) */
    @PutMapping("/{erId}")
    public ApiResponse<Void> update(
            @PathVariable String erId,
            @RequestBody ErVO vo,
            @AuthenticationPrincipal IcasUser user) {
        erService.updateEr(erId, vo, user);
        return ApiResponse.ok(null, "ER 이 수정되었습니다.");
    }

    /** 소프트삭제 (DRAFT 한정, AIRLINE) */
    @DeleteMapping("/{erId}")
    public ApiResponse<Void> delete(
            @PathVariable String erId,
            @AuthenticationPrincipal IcasUser user) {
        erService.softDeleteEr(erId, user);
        return ApiResponse.ok(null, "ER 이 삭제되었습니다.");
    }

    // ──────────────────────────────────────────────
    // 상태 전이
    // ──────────────────────────────────────────────

    /** 제출 (DRAFT → SBMTD, AIRLINE) */
    @PostMapping("/{erId}/submit")
    public ApiResponse<Void> submit(
            @PathVariable String erId,
            @AuthenticationPrincipal IcasUser user) {
        erService.submit(erId, user);
        return ApiResponse.ok(null, "ER 이 제출되었습니다.");
    }

    /** 검토 진입 (SBMTD → RVWNG, KOTSA) */
    @PostMapping("/{erId}/review")
    public ApiResponse<Void> review(
            @PathVariable String erId,
            @AuthenticationPrincipal IcasUser user) {
        erService.review(erId, user);
        return ApiResponse.ok(null, "ER 검토가 시작되었습니다.");
    }

    /** 반려 (RVWNG → DRAFT, KOTSA, 사유 필수) */
    @PostMapping("/{erId}/reject")
    public ApiResponse<Void> reject(
            @PathVariable String erId,
            @RequestBody ReasonRequest body,
            @AuthenticationPrincipal IcasUser user) {
        erService.reject(erId, body.getReason(), user);
        return ApiResponse.ok(null, "ER 이 반려되었습니다.");
    }

    /** 권고 (RVWNG → RCMDD, KOTSA) */
    @PostMapping("/{erId}/recommend")
    public ApiResponse<Void> recommend(
            @PathVariable String erId,
            @AuthenticationPrincipal IcasUser user) {
        erService.recommend(erId, user);
        return ApiResponse.ok(null, "ER 이 권고 처리되었습니다.");
    }

    /** 승인 (RVWNG 또는 RCMDD → APRVD, MOLIT) */
    @PostMapping("/{erId}/approve")
    public ApiResponse<Void> approve(
            @PathVariable String erId,
            @AuthenticationPrincipal IcasUser user) {
        erService.approve(erId, user);
        return ApiResponse.ok(null, "ER 이 승인되었습니다.");
    }

    /** 취소 (APRVD → CNCLD, MOLIT, 사유 필수) */
    @PostMapping("/{erId}/cancel")
    public ApiResponse<Void> cancel(
            @PathVariable String erId,
            @RequestBody ReasonRequest body,
            @AuthenticationPrincipal IcasUser user) {
        erService.cancel(erId, body.getReason(), user);
        return ApiResponse.ok(null, "ER 이 취소되었습니다.");
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
