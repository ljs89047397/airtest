package kr.go.molit.icas.er.cef;

import kr.go.molit.icas.common.dto.ApiResponse;
import kr.go.molit.icas.common.dto.PageResponse;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.er.cef.domain.CefSearch;
import kr.go.molit.icas.er.cef.domain.CefVO;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * CEF 마스터 REST API (SFR-017/020).
 *
 * <pre>
 * GET    /api/er/cef                       — 목록 검색 (페이징)
 * GET    /api/er/cef/{cefId}               — 단건 조회
 * GET    /api/er/cef/by-er/{erId}          — ER ID 로 단건 조회
 * POST   /api/er/cef                       — 신규 DRAFT 생성 (AIRLINE, body: erId)
 * DELETE /api/er/cef/{cefId}               — 소프트삭제 (DRAFT, AIRLINE)
 * POST   /api/er/cef/{cefId}/recalc        — 합계 재계산 (AIRLINE)
 * POST   /api/er/cef/{cefId}/submit        — 제출 (AIRLINE)
 * POST   /api/er/cef/{cefId}/approve       — 승인 (KOTSA)
 * POST   /api/er/cef/{cefId}/reject        — 반려 (KOTSA, SBMTD→DRAFT)
 * POST   /api/er/cef/{cefId}/cancel        — 취소 (MOLIT, 사유 필수)
 * </pre>
 */
@RestController
@RequestMapping("/api/er/cef")
@RequiredArgsConstructor
public class CefController {

    private final CefService cefService;

    @GetMapping
    public ApiResponse<PageResponse<CefVO>> list(
            @ModelAttribute CefSearch search,
            @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(cefService.searchCefs(search, user));
    }

    @GetMapping("/{cefId}")
    public ApiResponse<CefVO> get(@PathVariable String cefId,
                                  @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(cefService.getCef(cefId, user));
    }

    @GetMapping("/by-er/{erId}")
    public ApiResponse<CefVO> getByErId(@PathVariable String erId,
                                        @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(cefService.getByErId(erId, user));
    }

    @PostMapping
    public ApiResponse<CefVO> create(@RequestBody CreateRequest body,
                                     @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(cefService.createCef(body.getErId(), user), "CEF 가 등록되었습니다.");
    }

    @DeleteMapping("/{cefId}")
    public ApiResponse<Void> delete(@PathVariable String cefId,
                                    @AuthenticationPrincipal IcasUser user) {
        cefService.softDeleteCef(cefId, user);
        return ApiResponse.ok(null, "CEF 가 삭제되었습니다.");
    }

    @PostMapping("/{cefId}/recalc")
    public ApiResponse<BigDecimal> recalc(@PathVariable String cefId,
                                          @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(cefService.recalcTtlRedu(cefId, user), "합계가 재계산되었습니다.");
    }

    @PostMapping("/{cefId}/submit")
    public ApiResponse<Void> submit(@PathVariable String cefId,
                                    @AuthenticationPrincipal IcasUser user) {
        cefService.submit(cefId, user);
        return ApiResponse.ok(null, "CEF 가 제출되었습니다.");
    }

    @PostMapping("/{cefId}/approve")
    public ApiResponse<Void> approve(@PathVariable String cefId,
                                     @AuthenticationPrincipal IcasUser user) {
        cefService.approve(cefId, user);
        return ApiResponse.ok(null, "CEF 가 승인되었습니다.");
    }

    @PostMapping("/{cefId}/reject")
    public ApiResponse<Void> reject(@PathVariable String cefId,
                                    @AuthenticationPrincipal IcasUser user) {
        cefService.reject(cefId, user);
        return ApiResponse.ok(null, "CEF 가 반려되었습니다.");
    }

    @PostMapping("/{cefId}/cancel")
    public ApiResponse<Void> cancel(@PathVariable String cefId,
                                    @RequestBody ReasonRequest body,
                                    @AuthenticationPrincipal IcasUser user) {
        cefService.cancel(cefId, body.getReason(), user);
        return ApiResponse.ok(null, "CEF 가 취소되었습니다.");
    }

    // ── 요청 DTO ──
    @Getter
    @Setter
    public static class CreateRequest {
        private String erId;
    }

    @Getter
    @Setter
    public static class ReasonRequest {
        private String reason;
    }
}
