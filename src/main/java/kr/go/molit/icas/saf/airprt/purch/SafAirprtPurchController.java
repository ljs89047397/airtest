package kr.go.molit.icas.saf.airprt.purch;

import kr.go.molit.icas.common.dto.ApiResponse;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.saf.airprt.purch.domain.SafAirprtPurchVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 공항별 SAF 구매 REST API.
 * GET    /api/saf/airprt-purch?oprtrId=&rprtYr=
 * POST   /api/saf/airprt-purch
 * PUT    /api/saf/airprt-purch/{airprtId}/{rprtYr}/{oprtrId}/{purchSn}
 * DELETE /api/saf/airprt-purch/{airprtId}/{rprtYr}/{oprtrId}/{purchSn}
 */
@RestController
@RequestMapping("/api/saf/airprt-purch")
@RequiredArgsConstructor
public class SafAirprtPurchController {

    private final SafAirprtPurchService purchService;

    @GetMapping
    public ApiResponse<List<SafAirprtPurchVO>> list(@RequestParam(required = false) String oprtrId,
                                                    @RequestParam(required = false) String rprtYr,
                                                    @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(purchService.listByOprtrYr(
                (oprtrId == null || oprtrId.isBlank()) ? null : oprtrId,
                (rprtYr  == null || rprtYr.isBlank())  ? null : rprtYr,
                user));
    }

    @PostMapping
    public ApiResponse<SafAirprtPurchVO> add(@RequestBody SafAirprtPurchVO vo,
                                             @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(purchService.add(vo, user), "SAF 구매 실적이 등록되었습니다.");
    }

    @PutMapping("/{airprtId}/{rprtYr}/{oprtrId}/{purchSn}")
    public ApiResponse<SafAirprtPurchVO> update(@PathVariable String airprtId,
                                                @PathVariable String rprtYr,
                                                @PathVariable String oprtrId,
                                                @PathVariable int purchSn,
                                                @RequestBody SafAirprtPurchVO vo,
                                                @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(purchService.update(airprtId, rprtYr, oprtrId, purchSn, vo, user));
    }

    @DeleteMapping("/{airprtId}/{rprtYr}/{oprtrId}/{purchSn}")
    public ApiResponse<Void> delete(@PathVariable String airprtId,
                                    @PathVariable String rprtYr,
                                    @PathVariable String oprtrId,
                                    @PathVariable int purchSn,
                                    @AuthenticationPrincipal IcasUser user) {
        purchService.delete(airprtId, rprtYr, oprtrId, purchSn, user);
        return ApiResponse.ok(null);
    }
}
