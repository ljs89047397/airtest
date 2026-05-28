package kr.go.molit.icas.com.prgrm;

import kr.go.molit.icas.com.prgrm.domain.PrgrmVO;
import kr.go.molit.icas.common.dto.ApiResponse;
import kr.go.molit.icas.common.security.IcasUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 프로그램 관리 API.
 * Base URL: /api/com/prgrm
 */
@RestController
@RequestMapping("/api/com/prgrm")
@RequiredArgsConstructor
public class PrgrmController {

    private final PrgrmService prgrmService;

    /**
     * GET /api/com/prgrm?sysSeCd=COM
     * 전체 목록 또는 시스템 구분 필터 목록 조회.
     */
    @GetMapping
    public ApiResponse<List<PrgrmVO>> list(
            @RequestParam(required = false) String sysSeCd) {
        return ApiResponse.ok(prgrmService.selectPrgrms(sysSeCd));
    }

    /**
     * GET /api/com/prgrm/{prgrmId}
     * 단건 조회.
     */
    @GetMapping("/{prgrmId}")
    public ApiResponse<PrgrmVO> get(@PathVariable String prgrmId) {
        return ApiResponse.ok(prgrmService.selectPrgrm(prgrmId));
    }

    /**
     * POST /api/com/prgrm
     * 프로그램 등록 (MOLIT/KOTSA 전용).
     */
    @PostMapping
    public ApiResponse<PrgrmVO> create(
            @RequestBody PrgrmVO vo,
            @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(prgrmService.insertPrgrm(vo, user), "등록되었습니다.");
    }

    /**
     * PUT /api/com/prgrm/{prgrmId}
     * 프로그램 수정 (MOLIT/KOTSA 전용).
     */
    @PutMapping("/{prgrmId}")
    public ApiResponse<Void> update(
            @PathVariable String prgrmId,
            @RequestBody PrgrmVO vo,
            @AuthenticationPrincipal IcasUser user) {
        prgrmService.updatePrgrm(prgrmId, vo, user);
        return ApiResponse.ok(null, "수정되었습니다.");
    }

    /**
     * DELETE /api/com/prgrm/{prgrmId}
     * 프로그램 소프트 삭제 (MOLIT/KOTSA 전용).
     */
    @DeleteMapping("/{prgrmId}")
    public ApiResponse<Void> delete(
            @PathVariable String prgrmId,
            @AuthenticationPrincipal IcasUser user) {
        prgrmService.softDeletePrgrm(prgrmId, user);
        return ApiResponse.ok(null, "삭제되었습니다.");
    }
}
