package kr.go.molit.icas.ptl.ccr;

import kr.go.molit.icas.common.dto.ApiResponse;
import kr.go.molit.icas.common.dto.PageResponse;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.ptl.ccr.domain.PtlCcrExtrSearch;
import kr.go.molit.icas.ptl.ccr.domain.PtlCcrExtrVO;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * CCR 추출 REST API.
 *
 * <pre>
 * POST /api/ptl/ccr/extract          — 추출 요청 (MOLIT 전용)
 * GET  /api/ptl/ccr/{extrId}         — 단건 조회
 * GET  /api/ptl/ccr                  — 목록 조회 (페이징)
 * </pre>
 */
@RestController
@RequestMapping("/api/ptl/ccr")
@RequiredArgsConstructor
public class PtlCcrExtrController {

    private final PtlCcrExtrService ccrExtrService;

    /** CCR 추출 요청 */
    @PostMapping("/extract")
    public ApiResponse<PtlCcrExtrVO> requestExtraction(
            @RequestBody ExtrRequest body,
            @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(
                ccrExtrService.requestExtraction(body.getRprtYr(), body.getExtrScopeCd(), user),
                "CCR 추출이 완료되었습니다.");
    }

    /** 단건 조회 */
    @GetMapping("/{extrId}")
    public ApiResponse<PtlCcrExtrVO> getExtraction(
            @PathVariable String extrId,
            @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(ccrExtrService.getExtraction(extrId, user));
    }

    /** 목록 조회 (페이징) */
    @GetMapping
    public ApiResponse<PageResponse<PtlCcrExtrVO>> listExtractions(
            PtlCcrExtrSearch search,
            @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(ccrExtrService.listExtractions(search, user));
    }

    @Getter @Setter
    public static class ExtrRequest {
        private String rprtYr;
        private String extrScopeCd;
    }
}
