package kr.go.molit.icas.saf.airprt.fuel;

import kr.go.molit.icas.common.dto.ApiResponse;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.saf.airprt.fuel.domain.SafAirprtFuelVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 공항별 급유 실적 REST API.
 *
 * <pre>
 * GET /api/saf/airprt-fuel?oprtrId=&rprtYr=          — 목록
 * GET /api/saf/airprt-fuel/{airprtId}/{rprtYr}/{oprtrId} — 단건
 * PUT /api/saf/airprt-fuel                           — saveOrUpdate (AIRLINE)
 * DELETE /api/saf/airprt-fuel/{airprtId}/{rprtYr}/{oprtrId} — 삭제
 * </pre>
 */
@RestController
@RequestMapping("/api/saf/airprt-fuel")
@RequiredArgsConstructor
public class SafAirprtFuelController {

    private final SafAirprtFuelService fuelService;

    @GetMapping
    public ApiResponse<List<SafAirprtFuelVO>> list(@RequestParam(required = false) String oprtrId,
                                                   @RequestParam(required = false) String rprtYr,
                                                   @AuthenticationPrincipal IcasUser user) {
        // 빈 값이면 전체 조회 — 비즈룰 가시범위는 Service 단에서 처리
        return ApiResponse.ok(fuelService.listByOprtrYr(
                (oprtrId == null || oprtrId.isBlank()) ? null : oprtrId,
                (rprtYr  == null || rprtYr.isBlank())  ? null : rprtYr,
                user));
    }

    @GetMapping("/{airprtId}/{rprtYr}/{oprtrId}")
    public ApiResponse<SafAirprtFuelVO> get(@PathVariable String airprtId,
                                            @PathVariable String rprtYr,
                                            @PathVariable String oprtrId,
                                            @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(fuelService.get(airprtId, rprtYr, oprtrId, user));
    }

    @PutMapping
    public ApiResponse<SafAirprtFuelVO> save(@RequestBody SafAirprtFuelVO vo,
                                             @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(fuelService.saveOrUpdate(vo, user), "공항별 급유 실적이 저장되었습니다.");
    }

    @DeleteMapping("/{airprtId}/{rprtYr}/{oprtrId}")
    public ApiResponse<Void> delete(@PathVariable String airprtId,
                                    @PathVariable String rprtYr,
                                    @PathVariable String oprtrId,
                                    @AuthenticationPrincipal IcasUser user) {
        fuelService.softDelete(airprtId, rprtYr, oprtrId, user);
        return ApiResponse.ok(null);
    }
}
