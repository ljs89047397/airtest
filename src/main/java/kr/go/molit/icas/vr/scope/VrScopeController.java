package kr.go.molit.icas.vr.scope;

import kr.go.molit.icas.common.dto.ApiResponse;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.vr.scope.domain.VrScopeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * VR 범위·식별 정보 REST API.
 *
 * <pre>
 * GET /api/vr/{vrId}/scope     — 조회
 * PUT /api/vr/{vrId}/scope     — 저장/수정 (VERIFIER, DRAFT)
 * </pre>
 */
@RestController
@RequestMapping("/api/vr/{vrId}/scope")
@RequiredArgsConstructor
public class VrScopeController {

    private final VrScopeService scopeService;

    @GetMapping
    public ApiResponse<VrScopeVO> get(@PathVariable String vrId,
                                      @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(scopeService.get(vrId, user));
    }

    @PutMapping
    public ApiResponse<VrScopeVO> save(@PathVariable String vrId,
                                       @RequestBody VrScopeVO vo,
                                       @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(scopeService.saveOrUpdate(vrId, vo, user), "범위 정보가 저장되었습니다.");
    }
}
