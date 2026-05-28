package kr.go.molit.icas.ptl.sim;

import kr.go.molit.icas.common.dto.ApiResponse;
import kr.go.molit.icas.common.dto.PageResponse;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.ptl.sim.domain.PtlSimSearch;
import kr.go.molit.icas.ptl.sim.domain.PtlSimVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 시뮬레이션 REST API.
 *
 * <pre>
 * POST   /api/ptl/sim              — 시뮬레이션 생성
 * POST   /api/ptl/sim/{simId}/run  — 재계산 실행
 * PUT    /api/ptl/sim/{simId}      — 수정
 * DELETE /api/ptl/sim/{simId}      — 삭제
 * GET    /api/ptl/sim/{simId}      — 단건 조회
 * GET    /api/ptl/sim              — 목록 조회
 * </pre>
 */
@RestController
@RequestMapping("/api/ptl/sim")
@RequiredArgsConstructor
public class PtlSimController {

    private final PtlSimService simService;

    /** 시뮬레이션 신규 생성 */
    @PostMapping
    public ApiResponse<PtlSimVO> createSim(
            @RequestBody PtlSimVO vo,
            @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(simService.createSim(vo, user), "시뮬레이션이 생성되었습니다.");
    }

    /** 계산 재실행 */
    @PostMapping("/{simId}/run")
    public ApiResponse<PtlSimVO> runSim(
            @PathVariable String simId,
            @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(simService.runSim(simId, user), "시뮬레이션 계산이 완료되었습니다.");
    }

    /** 수정 */
    @PutMapping("/{simId}")
    public ApiResponse<PtlSimVO> updateSim(
            @PathVariable String simId,
            @RequestBody PtlSimVO vo,
            @AuthenticationPrincipal IcasUser user) {
        vo.setSimId(simId);
        return ApiResponse.ok(simService.updateSim(vo, user), "시뮬레이션이 수정되었습니다.");
    }

    /** 삭제 */
    @DeleteMapping("/{simId}")
    public ApiResponse<Void> deleteSim(
            @PathVariable String simId,
            @AuthenticationPrincipal IcasUser user) {
        simService.deleteSim(simId, user);
        return ApiResponse.ok(null, "시뮬레이션이 삭제되었습니다.");
    }

    /** 단건 조회 */
    @GetMapping("/{simId}")
    public ApiResponse<PtlSimVO> getSim(
            @PathVariable String simId,
            @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(simService.getSim(simId, user));
    }

    /** 목록 조회 */
    @GetMapping
    public ApiResponse<PageResponse<PtlSimVO>> listSims(
            PtlSimSearch search,
            @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(simService.listSims(search, user));
    }
}
