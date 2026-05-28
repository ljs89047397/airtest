package kr.go.molit.icas.vr.cncls;

import kr.go.molit.icas.common.dto.ApiResponse;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.vr.cncls.domain.VrCnclsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * VR 결론·검증 의견 REST API.
 *
 * <pre>
 * GET /api/vr/{vrId}/cncls  — 조회
 * PUT /api/vr/{vrId}/cncls  — 저장/수정 (VERIFIER, DRAFT)
 *                              ※ final_opnn_cd = REASONABLE: 미해결 부적합 시 400
 * </pre>
 */
@RestController
@RequestMapping("/api/vr/{vrId}/cncls")
@RequiredArgsConstructor
public class VrCnclsController {

    private final VrCnclsService cnclsService;

    @GetMapping
    public ApiResponse<VrCnclsVO> get(@PathVariable String vrId,
                                      @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(cnclsService.get(vrId, user));
    }

    @PutMapping
    public ApiResponse<VrCnclsVO> save(@PathVariable String vrId,
                                       @RequestBody VrCnclsVO vo,
                                       @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(cnclsService.saveOrUpdate(vrId, vo, user), "결론·검증 의견이 저장되었습니다.");
    }
}
