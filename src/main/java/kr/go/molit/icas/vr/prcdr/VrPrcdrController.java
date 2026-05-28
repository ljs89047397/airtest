package kr.go.molit.icas.vr.prcdr;

import kr.go.molit.icas.common.dto.ApiResponse;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.vr.prcdr.domain.VrPrcdrVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 검증 절차·분석 REST API.
 *
 * <pre>
 * GET /api/vr/{vrId}/prcdr  — 조회
 * PUT /api/vr/{vrId}/prcdr  — 저장/수정 (VERIFIER, DRAFT)
 * </pre>
 */
@RestController
@RequestMapping("/api/vr/{vrId}/prcdr")
@RequiredArgsConstructor
public class VrPrcdrController {

    private final VrPrcdrService prcdrService;

    @GetMapping
    public ApiResponse<VrPrcdrVO> get(@PathVariable String vrId,
                                      @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(prcdrService.get(vrId, user));
    }

    @PutMapping
    public ApiResponse<VrPrcdrVO> save(@PathVariable String vrId,
                                       @RequestBody VrPrcdrVO vo,
                                       @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(prcdrService.saveOrUpdate(vrId, vo, user), "절차·분석 정보가 저장되었습니다.");
    }
}
