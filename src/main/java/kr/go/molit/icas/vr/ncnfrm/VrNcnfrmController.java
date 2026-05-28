package kr.go.molit.icas.vr.ncnfrm;

import kr.go.molit.icas.common.dto.ApiResponse;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.vr.ncnfrm.domain.VrNcnfrmVO;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 부적합·허위진술 REST API.
 *
 * <pre>
 * GET    /api/vr/{vrId}/ncnfrm                         — 목록 조회
 * POST   /api/vr/{vrId}/ncnfrm                         — 등록 (VERIFIER, DRAFT)
 * PUT    /api/vr/{vrId}/ncnfrm/{itemNo}                — 수정
 * PUT    /api/vr/{vrId}/ncnfrm/{itemNo}/resolve        — 해결 처리 (resolDt 기록)
 * DELETE /api/vr/{vrId}/ncnfrm/{itemNo}                — 삭제
 * </pre>
 */
@RestController
@RequestMapping("/api/vr/{vrId}/ncnfrm")
@RequiredArgsConstructor
public class VrNcnfrmController {

    private final VrNcnfrmService ncnfrmService;

    @GetMapping
    public ApiResponse<List<VrNcnfrmVO>> list(@PathVariable String vrId,
                                               @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(ncnfrmService.list(vrId, user));
    }

    @PostMapping
    public ApiResponse<VrNcnfrmVO> add(@PathVariable String vrId,
                                       @RequestBody VrNcnfrmVO vo,
                                       @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(ncnfrmService.add(vrId, vo, user), "부적합이 등록되었습니다.");
    }

    @PutMapping("/{itemNo}")
    public ApiResponse<VrNcnfrmVO> update(@PathVariable String vrId,
                                           @PathVariable int itemNo,
                                           @RequestBody VrNcnfrmVO vo,
                                           @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(ncnfrmService.update(vrId, itemNo, vo, user));
    }

    @PutMapping("/{itemNo}/resolve")
    public ApiResponse<Void> resolve(@PathVariable String vrId,
                                     @PathVariable int itemNo,
                                     @RequestBody ResolveRequest body,
                                     @AuthenticationPrincipal IcasUser user) {
        ncnfrmService.resolve(vrId, itemNo, body.getResolDescCn(), body.getResolDt(), user);
        return ApiResponse.ok(null, "해결 처리가 완료되었습니다.");
    }

    @DeleteMapping("/{itemNo}")
    public ApiResponse<Void> delete(@PathVariable String vrId,
                                    @PathVariable int itemNo,
                                    @AuthenticationPrincipal IcasUser user) {
        ncnfrmService.delete(vrId, itemNo, user);
        return ApiResponse.ok(null);
    }

    @Getter @Setter
    public static class ResolveRequest {
        private String resolDescCn;
        private String resolDt;
    }
}
