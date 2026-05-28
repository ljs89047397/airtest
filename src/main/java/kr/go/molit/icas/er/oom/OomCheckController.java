package kr.go.molit.icas.er.oom;

import kr.go.molit.icas.common.dto.ApiResponse;
import kr.go.molit.icas.common.dto.PageResponse;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.er.oom.domain.OomCheckVO;
import kr.go.molit.icas.er.oom.domain.OomSearch;
import kr.go.molit.icas.er.oom.validate.CorsiaQuantValidator;
import kr.go.molit.icas.er.oom.validate.domain.QuantCheckRunResult;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * OoM-check 마스터 REST API (SFR-033/034).
 *
 * <pre>
 * GET    /api/er/oom                       — 목록 검색
 * GET    /api/er/oom/{oomId}               — 단건 조회
 * POST   /api/er/oom                       — 생성 (KOTSA)
 * PUT    /api/er/oom/{oomId}/links         — er_id / vr_id 링크 수정 (KOTSA)
 * DELETE /api/er/oom/{oomId}               — 삭제 (KOTSA, INPRG)
 * POST   /api/er/oom/{oomId}/run-quant     — 18종 정량 검증 실행 (KOTSA)
 * POST   /api/er/oom/{oomId}/finalize      — 결과 확정 (KOTSA, PASS/FAIL/HOLD)
 * POST   /api/er/oom/{oomId}/hold          — HOLD 처리 (KOTSA)
 * </pre>
 */
@RestController
@RequestMapping("/api/er/oom")
@RequiredArgsConstructor
public class OomCheckController {

    private final OomCheckService      oomCheckService;
    private final CorsiaQuantValidator corsiaQuantValidator;

    @GetMapping
    public ApiResponse<PageResponse<OomCheckVO>> list(@ModelAttribute OomSearch search,
                                                      @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(oomCheckService.searchOoms(search, user));
    }

    @GetMapping("/{oomId}")
    public ApiResponse<OomCheckVO> get(@PathVariable String oomId,
                                       @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(oomCheckService.getOom(oomId, user));
    }

    @PostMapping
    public ApiResponse<OomCheckVO> create(@RequestBody OomCheckVO vo,
                                          @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(oomCheckService.createOom(vo, user), "OoM 이 등록되었습니다.");
    }

    @PutMapping("/{oomId}/links")
    public ApiResponse<Void> updateLinks(@PathVariable String oomId,
                                         @RequestBody LinksRequest body,
                                         @AuthenticationPrincipal IcasUser user) {
        oomCheckService.updateLinks(oomId, body.getErId(), body.getVrId(), user);
        return ApiResponse.ok(null, "링크가 갱신되었습니다.");
    }

    @DeleteMapping("/{oomId}")
    public ApiResponse<Void> delete(@PathVariable String oomId,
                                    @AuthenticationPrincipal IcasUser user) {
        oomCheckService.softDelete(oomId, user);
        return ApiResponse.ok(null);
    }

    /** 18종 정량 검증 일괄 실행 */
    @PostMapping("/{oomId}/run-quant")
    public ApiResponse<QuantCheckRunResult> runQuant(@PathVariable String oomId,
                                                     @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(corsiaQuantValidator.runAll(oomId, user), "18종 자동 검증이 실행되었습니다.");
    }

    @PostMapping("/{oomId}/finalize")
    public ApiResponse<Void> finalizeOom(@PathVariable String oomId,
                                         @RequestBody FinalizeRequest body,
                                         @AuthenticationPrincipal IcasUser user) {
        oomCheckService.finalizeOom(oomId, body.getRsltCd(), user);
        return ApiResponse.ok(null, "OoM 이 확정되었습니다 (" + body.getRsltCd() + ").");
    }

    @PostMapping("/{oomId}/hold")
    public ApiResponse<Void> hold(@PathVariable String oomId,
                                  @AuthenticationPrincipal IcasUser user) {
        oomCheckService.hold(oomId, user);
        return ApiResponse.ok(null, "OoM 이 HOLD 처리되었습니다.");
    }

    // ── 요청 DTO ──
    @Getter
    @Setter
    public static class LinksRequest {
        private String erId;
        private String vrId;
    }

    @Getter
    @Setter
    public static class FinalizeRequest {
        private String rsltCd;
    }
}
