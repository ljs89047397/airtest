package kr.go.molit.icas.er.eucr;

import kr.go.molit.icas.common.dto.ApiResponse;
import kr.go.molit.icas.common.dto.PageResponse;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.er.eucr.domain.EucrSearch;
import kr.go.molit.icas.er.eucr.domain.EucrVO;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * EUCR 마스터 REST API (SFR-030).
 *
 * <pre>
 * GET    /api/er/eucr                       — 목록 검색
 * GET    /api/er/eucr/{eucrId}              — 단건 조회
 * POST   /api/er/eucr                       — 신규 DRAFT (AIRLINE)
 * PUT    /api/er/eucr/{eucrId}/ofst-req-qty — 의무량 갱신 (AIRLINE)
 * DELETE /api/er/eucr/{eucrId}              — 소프트삭제 (AIRLINE)
 * POST   /api/er/eucr/{eucrId}/recalc       — 합계 재계산 (AIRLINE)
 * POST   /api/er/eucr/{eucrId}/submit       — 제출 (AIRLINE)
 * POST   /api/er/eucr/{eucrId}/review       — 검토 진입 (KOTSA)
 * POST   /api/er/eucr/{eucrId}/reject       — 반려 (KOTSA, 사유 필수)
 * POST   /api/er/eucr/{eucrId}/recommend    — 권고 (KOTSA)
 * POST   /api/er/eucr/{eucrId}/approve      — 승인 (MOLIT)
 * POST   /api/er/eucr/{eucrId}/cancel       — 취소 (MOLIT, 사유 필수)
 * </pre>
 */
@RestController
@RequestMapping("/api/er/eucr")
@RequiredArgsConstructor
public class EucrController {

    private final EucrService eucrService;

    @GetMapping
    public ApiResponse<PageResponse<EucrVO>> list(@ModelAttribute EucrSearch search,
                                                  @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(eucrService.searchEucrs(search, user));
    }

    @GetMapping("/{eucrId}")
    public ApiResponse<EucrVO> get(@PathVariable String eucrId,
                                   @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(eucrService.getEucr(eucrId, user));
    }

    @PostMapping
    public ApiResponse<EucrVO> create(@RequestBody EucrVO vo,
                                      @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(eucrService.createEucr(vo, user), "EUCR 이 등록되었습니다.");
    }

    @PutMapping("/{eucrId}/ofst-req-qty")
    public ApiResponse<Void> updateOfstReqQty(@PathVariable String eucrId,
                                              @RequestBody OfstReqRequest body,
                                              @AuthenticationPrincipal IcasUser user) {
        eucrService.updateOfstReqQty(eucrId, body.getOfstReqQty(), user);
        return ApiResponse.ok(null, "상쇄 의무량이 갱신되었습니다.");
    }

    @DeleteMapping("/{eucrId}")
    public ApiResponse<Void> delete(@PathVariable String eucrId,
                                    @AuthenticationPrincipal IcasUser user) {
        eucrService.softDeleteEucr(eucrId, user);
        return ApiResponse.ok(null, "EUCR 이 삭제되었습니다.");
    }

    @PostMapping("/{eucrId}/recalc")
    public ApiResponse<EucrVO> recalc(@PathVariable String eucrId,
                                      @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(eucrService.recalcTtlAndFulfilled(eucrId, user), "합계가 재계산되었습니다.");
    }

    @PostMapping("/{eucrId}/submit")
    public ApiResponse<Void> submit(@PathVariable String eucrId,
                                    @AuthenticationPrincipal IcasUser user) {
        eucrService.submit(eucrId, user);
        return ApiResponse.ok(null, "EUCR 이 제출되었습니다.");
    }

    @PostMapping("/{eucrId}/review")
    public ApiResponse<Void> review(@PathVariable String eucrId,
                                    @AuthenticationPrincipal IcasUser user) {
        eucrService.review(eucrId, user);
        return ApiResponse.ok(null, "검토가 시작되었습니다.");
    }

    @PostMapping("/{eucrId}/reject")
    public ApiResponse<Void> reject(@PathVariable String eucrId,
                                    @RequestBody ReasonRequest body,
                                    @AuthenticationPrincipal IcasUser user) {
        eucrService.reject(eucrId, body.getReason(), user);
        return ApiResponse.ok(null, "EUCR 이 반려되었습니다.");
    }

    @PostMapping("/{eucrId}/recommend")
    public ApiResponse<Void> recommend(@PathVariable String eucrId,
                                       @AuthenticationPrincipal IcasUser user) {
        eucrService.recommend(eucrId, user);
        return ApiResponse.ok(null, "EUCR 이 권고 처리되었습니다.");
    }

    @PostMapping("/{eucrId}/approve")
    public ApiResponse<Void> approve(@PathVariable String eucrId,
                                     @AuthenticationPrincipal IcasUser user) {
        eucrService.approve(eucrId, user);
        return ApiResponse.ok(null, "EUCR 이 승인되었습니다.");
    }

    @PostMapping("/{eucrId}/cancel")
    public ApiResponse<Void> cancel(@PathVariable String eucrId,
                                    @RequestBody ReasonRequest body,
                                    @AuthenticationPrincipal IcasUser user) {
        eucrService.cancel(eucrId, body.getReason(), user);
        return ApiResponse.ok(null, "EUCR 이 취소되었습니다.");
    }

    // ── 요청 DTO ──
    @Getter
    @Setter
    public static class OfstReqRequest {
        private BigDecimal ofstReqQty;
    }

    @Getter
    @Setter
    public static class ReasonRequest {
        private String reason;
    }
}
