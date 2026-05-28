package kr.go.molit.icas.er.cef.claim;

import kr.go.molit.icas.common.dto.ApiResponse;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.er.cef.claim.domain.CefClaimVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CEF 청구건 REST API (SFR-017).
 *
 * <p>Base URL: {@code /api/er/cef/{cefId}/claim}
 */
@RestController
@RequestMapping("/api/er/cef/{cefId}/claim")
@RequiredArgsConstructor
public class CefClaimController {

    private final CefClaimService cefClaimService;

    @GetMapping
    public ApiResponse<List<CefClaimVO>> list(@PathVariable String cefId,
                                              @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(cefClaimService.list(cefId, user));
    }

    @GetMapping("/{claimNo}")
    public ApiResponse<CefClaimVO> get(@PathVariable String cefId,
                                       @PathVariable String claimNo,
                                       @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(cefClaimService.getOne(cefId, claimNo, user));
    }

    @PostMapping
    public ApiResponse<CefClaimVO> add(@PathVariable String cefId,
                                       @RequestBody CefClaimVO vo,
                                       @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(cefClaimService.add(cefId, vo, user), "청구건이 등록되었습니다.");
    }

    @PutMapping("/{claimNo}")
    public ApiResponse<Void> update(@PathVariable String cefId,
                                    @PathVariable String claimNo,
                                    @RequestBody CefClaimVO vo,
                                    @AuthenticationPrincipal IcasUser user) {
        cefClaimService.update(cefId, claimNo, vo, user);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/{claimNo}")
    public ApiResponse<Void> delete(@PathVariable String cefId,
                                    @PathVariable String claimNo,
                                    @AuthenticationPrincipal IcasUser user) {
        cefClaimService.softDelete(cefId, claimNo, user);
        return ApiResponse.ok(null);
    }
}
