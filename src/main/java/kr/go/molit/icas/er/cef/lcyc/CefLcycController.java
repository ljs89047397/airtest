package kr.go.molit.icas.er.cef.lcyc;

import kr.go.molit.icas.common.dto.ApiResponse;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.er.cef.lcyc.domain.CefLcycVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * CEF 수명주기 배출량 REST API (SFR-018).
 *
 * <p>Base URL: {@code /api/er/cef/{cefId}/claim/{claimNo}/lcyc}
 * <p>claim 당 0..1 행 — save 는 upsert.
 */
@RestController
@RequestMapping("/api/er/cef/{cefId}/claim/{claimNo}/lcyc")
@RequiredArgsConstructor
public class CefLcycController {

    private final CefLcycService cefLcycService;

    @GetMapping
    public ApiResponse<CefLcycVO> get(@PathVariable String cefId,
                                      @PathVariable String claimNo,
                                      @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(cefLcycService.getOne(cefId, claimNo, user));
    }

    /** Upsert (PUT 시멘틱: 존재하지 않으면 INSERT, 있으면 UPDATE) */
    @PutMapping
    public ApiResponse<CefLcycVO> save(@PathVariable String cefId,
                                       @PathVariable String claimNo,
                                       @RequestBody CefLcycVO vo,
                                       @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(cefLcycService.save(cefId, claimNo, vo, user), "수명주기 정보가 저장되었습니다.");
    }

    @DeleteMapping
    public ApiResponse<Void> delete(@PathVariable String cefId,
                                    @PathVariable String claimNo,
                                    @AuthenticationPrincipal IcasUser user) {
        cefLcycService.softDelete(cefId, claimNo, user);
        return ApiResponse.ok(null);
    }
}
