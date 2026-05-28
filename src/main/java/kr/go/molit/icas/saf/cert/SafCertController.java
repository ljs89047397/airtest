package kr.go.molit.icas.saf.cert;

import kr.go.molit.icas.common.dto.ApiResponse;
import kr.go.molit.icas.common.dto.PageResponse;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.saf.cert.domain.SafCertAuditVO;
import kr.go.molit.icas.saf.cert.domain.SafCertSearch;
import kr.go.molit.icas.saf.cert.domain.SafCertVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * SAF 인증서 REST API.
 *
 * <pre>
 * GET    /api/saf/cert                       — 목록
 * GET    /api/saf/cert/{certId}              — 단건 (VIEW 감사 기록)
 * GET    /api/saf/cert/{certId}/audit        — 감사 이력
 * POST   /api/saf/cert                       — 등록 (AIRLINE)
 * PUT    /api/saf/cert/{certId}              — 수정 (미회수만)
 * POST   /api/saf/cert/{certId}/surrender    — 회수 처리
 * DELETE /api/saf/cert/{certId}              — 소프트삭제 (미회수만)
 * </pre>
 */
@RestController
@RequestMapping("/api/saf/cert")
@RequiredArgsConstructor
public class SafCertController {

    private final SafCertService certService;

    @GetMapping
    public ApiResponse<PageResponse<SafCertVO>> list(SafCertSearch search,
                                                     @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(certService.search(search, user));
    }

    @GetMapping("/{certId}")
    public ApiResponse<SafCertVO> get(@PathVariable String certId,
                                      @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(certService.get(certId, user));
    }

    @GetMapping("/{certId}/audit")
    public ApiResponse<List<SafCertAuditVO>> audits(@PathVariable String certId,
                                                     @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(certService.getAudits(certId, user));
    }

    @PostMapping
    public ApiResponse<SafCertVO> register(@RequestBody SafCertVO vo,
                                           @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(certService.register(vo, user), "SAF 인증서가 등록되었습니다.");
    }

    @PutMapping("/{certId}")
    public ApiResponse<SafCertVO> update(@PathVariable String certId,
                                         @RequestBody SafCertVO vo,
                                         @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(certService.update(certId, vo, user));
    }

    @PostMapping("/{certId}/surrender")
    public ApiResponse<Void> surrender(@PathVariable String certId,
                                       @AuthenticationPrincipal IcasUser user) {
        certService.surrender(certId, user);
        return ApiResponse.ok(null, "인증서가 회수(Surrender) 처리되었습니다.");
    }

    @DeleteMapping("/{certId}")
    public ApiResponse<Void> delete(@PathVariable String certId,
                                    @AuthenticationPrincipal IcasUser user) {
        certService.softDelete(certId, user);
        return ApiResponse.ok(null);
    }
}
