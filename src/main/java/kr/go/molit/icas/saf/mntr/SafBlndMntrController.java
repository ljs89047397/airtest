package kr.go.molit.icas.saf.mntr;

import kr.go.molit.icas.common.dto.ApiResponse;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.saf.mntr.domain.SafBlndMntrVO;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * SAF 혼합비율 모니터링 REST API.
 *
 * <pre>
 * GET  /api/saf/mntr/blnd?oprtrId=&rprtYr=       — 단건 조회
 * GET  /api/saf/mntr/blnd/all?rprtYr=             — 전체 조회 (KOTSA/MOLIT)
 * POST /api/saf/mntr/blnd/calc                   — 자동 산출 실행
 * </pre>
 */
@RestController
@RequestMapping("/api/saf/mntr/blnd")
@RequiredArgsConstructor
public class SafBlndMntrController {

    private final SafBlndMntrService mntrService;

    @GetMapping
    public ApiResponse<SafBlndMntrVO> get(@RequestParam String oprtrId,
                                          @RequestParam String rprtYr,
                                          @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(mntrService.get(oprtrId, rprtYr, user));
    }

    @GetMapping("/all")
    public ApiResponse<List<SafBlndMntrVO>> listAll(@RequestParam String rprtYr,
                                                     @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(mntrService.listByRprtYr(rprtYr, user));
    }

    @PostMapping("/calc")
    public ApiResponse<SafBlndMntrVO> calc(@RequestBody CalcRequest body,
                                           @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(
                mntrService.runCalc(body.getOprtrId(), body.getRprtYr(), user),
                "혼합비율이 산출되었습니다.");
    }

    @Getter @Setter
    public static class CalcRequest {
        private String oprtrId;
        private String rprtYr;
    }
}
