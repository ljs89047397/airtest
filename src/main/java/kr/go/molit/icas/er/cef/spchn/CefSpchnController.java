package kr.go.molit.icas.er.cef.spchn;

import kr.go.molit.icas.common.dto.ApiResponse;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.er.cef.spchn.domain.CefSpchnVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CEF 공급망 REST API (SFR-019).
 *
 * <p>Base URL: {@code /api/er/cef/{cefId}/claim/{claimNo}/spchn}
 */
@RestController
@RequestMapping("/api/er/cef/{cefId}/claim/{claimNo}/spchn")
@RequiredArgsConstructor
public class CefSpchnController {

    private final CefSpchnService cefSpchnService;

    @GetMapping
    public ApiResponse<List<CefSpchnVO>> list(@PathVariable String cefId,
                                              @PathVariable String claimNo,
                                              @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(cefSpchnService.list(cefId, claimNo, user));
    }

    @GetMapping("/{chnSn}")
    public ApiResponse<CefSpchnVO> get(@PathVariable String cefId,
                                       @PathVariable String claimNo,
                                       @PathVariable int chnSn,
                                       @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(cefSpchnService.getOne(cefId, claimNo, chnSn, user));
    }

    @PostMapping
    public ApiResponse<CefSpchnVO> add(@PathVariable String cefId,
                                       @PathVariable String claimNo,
                                       @RequestBody CefSpchnVO vo,
                                       @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(cefSpchnService.add(cefId, claimNo, vo, user), "공급망 항목이 등록되었습니다.");
    }

    @PutMapping("/{chnSn}")
    public ApiResponse<Void> update(@PathVariable String cefId,
                                    @PathVariable String claimNo,
                                    @PathVariable int chnSn,
                                    @RequestBody CefSpchnVO vo,
                                    @AuthenticationPrincipal IcasUser user) {
        cefSpchnService.update(cefId, claimNo, chnSn, vo, user);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/{chnSn}")
    public ApiResponse<Void> delete(@PathVariable String cefId,
                                    @PathVariable String claimNo,
                                    @PathVariable int chnSn,
                                    @AuthenticationPrincipal IcasUser user) {
        cefSpchnService.softDelete(cefId, claimNo, chnSn, user);
        return ApiResponse.ok(null);
    }
}
