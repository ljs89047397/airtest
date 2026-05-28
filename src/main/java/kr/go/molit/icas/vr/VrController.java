package kr.go.molit.icas.vr;

import kr.go.molit.icas.common.dto.ApiResponse;
import kr.go.molit.icas.common.dto.PageResponse;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.vr.domain.VrSearch;
import kr.go.molit.icas.vr.domain.VrVO;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * VR 마스터 REST API.
 *
 * <pre>
 * GET    /api/vr                        — VR 목록 (페이징)
 * GET    /api/vr/{vrId}                 — VR 단건 조회
 * POST   /api/vr                        — VR 신규 등록 (VERIFIER)
 * PUT    /api/vr/{vrId}/links           — ER/EUCR 연계 수정
 * POST   /api/vr/{vrId}/submit          — 제출 (VERIFIER, DRAFT → SBMTD)
 * POST   /api/vr/{vrId}/recommend       — 권고 (KOTSA, SBMTD → RCMDD)
 * POST   /api/vr/{vrId}/approve         — 승인 (MOLIT, RCMDD → APRVD)
 * POST   /api/vr/{vrId}/reject          — 반려 (KOTSA, SBMTD → DRAFT)
 * DELETE /api/vr/{vrId}                 — 소프트삭제 (VERIFIER, DRAFT)
 * </pre>
 */
@RestController
@RequestMapping("/api/vr")
@RequiredArgsConstructor
public class VrController {

    private final VrService vrService;

    @GetMapping
    public ApiResponse<PageResponse<VrVO>> list(VrSearch search,
                                                @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(vrService.searchVrs(search, user));
    }

    @GetMapping("/{vrId}")
    public ApiResponse<VrVO> get(@PathVariable String vrId,
                                 @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(vrService.getVr(vrId, user));
    }

    @PostMapping
    public ApiResponse<VrVO> create(@RequestBody VrVO vo,
                                    @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(vrService.createVr(vo, user), "VR 이 등록되었습니다.");
    }

    @PutMapping("/{vrId}/links")
    public ApiResponse<VrVO> updateLinks(@PathVariable String vrId,
                                         @RequestBody LinksRequest body,
                                         @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(vrService.updateLinks(vrId, body.getErId(), body.getEucrId(), user));
    }

    @PostMapping("/{vrId}/submit")
    public ApiResponse<Void> submit(@PathVariable String vrId,
                                    @AuthenticationPrincipal IcasUser user) {
        vrService.submit(vrId, user);
        return ApiResponse.ok(null, "VR 이 제출되었습니다.");
    }

    @PostMapping("/{vrId}/recommend")
    public ApiResponse<Void> recommend(@PathVariable String vrId,
                                       @AuthenticationPrincipal IcasUser user) {
        vrService.recommend(vrId, user);
        return ApiResponse.ok(null, "VR 권고 처리가 완료되었습니다.");
    }

    @PostMapping("/{vrId}/approve")
    public ApiResponse<Void> approve(@PathVariable String vrId,
                                     @AuthenticationPrincipal IcasUser user) {
        vrService.approve(vrId, user);
        return ApiResponse.ok(null, "VR 이 승인되었습니다.");
    }

    @PostMapping("/{vrId}/reject")
    public ApiResponse<Void> reject(@PathVariable String vrId,
                                    @RequestBody RejectRequest body,
                                    @AuthenticationPrincipal IcasUser user) {
        vrService.reject(vrId, body.getRjctRsn(), user);
        return ApiResponse.ok(null, "VR 이 반려되었습니다.");
    }

    @DeleteMapping("/{vrId}")
    public ApiResponse<Void> delete(@PathVariable String vrId,
                                    @AuthenticationPrincipal IcasUser user) {
        vrService.softDelete(vrId, user);
        return ApiResponse.ok(null);
    }

    @Getter @Setter
    public static class LinksRequest {
        private String erId;
        private String eucrId;
    }

    @Getter @Setter
    public static class RejectRequest {
        private String rjctRsn;
    }
}
