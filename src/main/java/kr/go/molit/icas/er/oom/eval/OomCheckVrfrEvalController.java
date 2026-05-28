package kr.go.molit.icas.er.oom.eval;

import kr.go.molit.icas.common.dto.ApiResponse;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.er.oom.eval.domain.OomCheckVrfrEvalVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * OoM 검증기관 품질 평가 REST API.
 *
 * <pre>
 * GET    /api/er/oom/{oomId}/eval                       — 평가 목록 조회
 * PUT    /api/er/oom/{oomId}/eval                       — 본인 검증기관 평가 saveOrUpdate (VERIFIER)
 * DELETE /api/er/oom/{oomId}/eval/{vrfcnInstId}         — 평가 삭제 (VERIFIER 본인)
 * </pre>
 */
@RestController
@RequestMapping("/api/er/oom/{oomId}/eval")
@RequiredArgsConstructor
public class OomCheckVrfrEvalController {

    private final OomCheckVrfrEvalService evalService;

    @GetMapping
    public ApiResponse<List<OomCheckVrfrEvalVO>> list(@PathVariable String oomId,
                                                      @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(evalService.list(oomId, user));
    }

    @PutMapping
    public ApiResponse<OomCheckVrfrEvalVO> save(@PathVariable String oomId,
                                                @RequestBody OomCheckVrfrEvalVO vo,
                                                @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(evalService.saveByVerifier(oomId, vo, user), "평가가 저장되었습니다.");
    }

    @DeleteMapping("/{vrfcnInstId}")
    public ApiResponse<Void> delete(@PathVariable String oomId,
                                    @PathVariable String vrfcnInstId,
                                    @AuthenticationPrincipal IcasUser user) {
        evalService.softDelete(oomId, vrfcnInstId, user);
        return ApiResponse.ok(null);
    }
}
