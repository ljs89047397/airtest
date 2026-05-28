package kr.go.molit.icas.com.rglt;

import kr.go.molit.icas.com.rglt.domain.RgltSearch;
import kr.go.molit.icas.com.rglt.domain.RgltVO;
import kr.go.molit.icas.common.dto.ApiResponse;
import kr.go.molit.icas.common.dto.PageResponse;
import kr.go.molit.icas.common.security.IcasUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 규정 게시판 REST API.
 *
 * <pre>
 * GET    /api/com/rglt              — 목록 조회
 * GET    /api/com/rglt/{rgltId}     — 단건 조회
 * POST   /api/com/rglt              — 등록 (MOLIT 전용)
 * PUT    /api/com/rglt/{rgltId}     — 수정 (MOLIT 전용)
 * DELETE /api/com/rglt/{rgltId}     — 비공개 처리 (MOLIT 전용)
 * </pre>
 */
@RestController
@RequestMapping("/api/com/rglt")
@RequiredArgsConstructor
public class RgltController {

    private final RgltService rgltService;

    /** 목록 조회 */
    @GetMapping
    public ApiResponse<PageResponse<RgltVO>> listRglts(
            RgltSearch search,
            @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(rgltService.listRglts(search, user));
    }

    /** 단건 조회 */
    @GetMapping("/{rgltId}")
    public ApiResponse<RgltVO> getRglt(
            @PathVariable String rgltId,
            @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(rgltService.getRglt(rgltId, user));
    }

    /** 등록 (MOLIT 전용) */
    @PostMapping
    public ApiResponse<RgltVO> createRglt(
            @RequestBody RgltVO vo,
            @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(rgltService.createRglt(vo, user), "규정이 등록되었습니다.");
    }

    /** 수정 (MOLIT 전용) */
    @PutMapping("/{rgltId}")
    public ApiResponse<RgltVO> updateRglt(
            @PathVariable String rgltId,
            @RequestBody RgltVO vo,
            @AuthenticationPrincipal IcasUser user) {
        vo.setRgltId(rgltId);
        return ApiResponse.ok(rgltService.updateRglt(vo, user), "규정이 수정되었습니다.");
    }

    /** 비공개 처리 (MOLIT 전용) */
    @DeleteMapping("/{rgltId}")
    public ApiResponse<Void> archiveRglt(
            @PathVariable String rgltId,
            @AuthenticationPrincipal IcasUser user) {
        rgltService.archiveRglt(rgltId, user);
        return ApiResponse.ok(null, "규정이 비공개 처리되었습니다.");
    }
}
