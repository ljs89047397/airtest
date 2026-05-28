package kr.go.molit.icas.ptl.stat;

import kr.go.molit.icas.common.dto.ApiResponse;
import kr.go.molit.icas.common.security.IcasUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 연도별 통계 REST API.
 *
 * <pre>
 * GET  /api/ptl/stat/{rprtYr}/{oprtrId}              — 단건 조회
 * GET  /api/ptl/stat/{rprtYr}                        — 연도 기준 목록
 * GET  /api/ptl/stat/oprtr/{oprtrId}                 — 운영사 기준 다년 추이
 * POST /api/ptl/stat/aggregate/{rprtYr}              — 전체 집계 (MOLIT/KOTSA)
 * POST /api/ptl/stat/aggregate/{rprtYr}/{oprtrId}    — 단일 집계 (MOLIT/KOTSA)
 * </pre>
 */
@RestController
@RequestMapping("/api/ptl/stat")
@RequiredArgsConstructor
public class PtlStatController {

    private final PtlStatService statService;

    /** 단건 조회 */
    @GetMapping("/{rprtYr}/{oprtrId}")
    public ApiResponse<PtlStatYearlyVO> getStat(
            @PathVariable String rprtYr,
            @PathVariable String oprtrId,
            @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(statService.getStat(rprtYr, oprtrId, user));
    }

    /** 연도 기준 전체 목록 */
    @GetMapping("/{rprtYr}")
    public ApiResponse<List<PtlStatYearlyVO>> listByRprtYr(
            @PathVariable String rprtYr,
            @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(statService.listByRprtYr(rprtYr, user));
    }

    /** 운영사 기준 다년 추이 */
    @GetMapping("/oprtr/{oprtrId}")
    public ApiResponse<List<PtlStatYearlyVO>> listByOprtr(
            @PathVariable String oprtrId,
            @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(statService.listByOprtr(oprtrId, user));
    }

    /** 전체 집계 수동 실행 (MOLIT/KOTSA 전용) */
    @PostMapping("/aggregate/{rprtYr}")
    public ApiResponse<Void> aggregateAll(
            @PathVariable String rprtYr,
            @AuthenticationPrincipal IcasUser user) {
        statService.aggregateAll(rprtYr, user);
        return ApiResponse.ok(null, "전체 집계가 완료되었습니다.");
    }

    /** 단일 집계 실행 (MOLIT/KOTSA 전용) */
    @PostMapping("/aggregate/{rprtYr}/{oprtrId}")
    public ApiResponse<PtlStatYearlyVO> aggregateOne(
            @PathVariable String rprtYr,
            @PathVariable String oprtrId,
            @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(
                statService.aggregateOne(rprtYr, oprtrId, user),
                "집계가 완료되었습니다.");
    }
}
