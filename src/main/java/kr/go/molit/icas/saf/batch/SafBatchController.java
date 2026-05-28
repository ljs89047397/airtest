package kr.go.molit.icas.saf.batch;

import kr.go.molit.icas.common.dto.ApiResponse;
import kr.go.molit.icas.common.dto.PageResponse;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.saf.batch.domain.SafBatchSearch;
import kr.go.molit.icas.saf.batch.domain.SafBatchVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * SAF 배치 마스터 REST API.
 *
 * <pre>
 * GET    /api/saf/batch              — 목록 (페이징)
 * GET    /api/saf/batch/{batchId}    — 단건
 * POST   /api/saf/batch              — 신규 (AIRLINE)
 * PUT    /api/saf/batch/{batchId}    — 수정
 * DELETE /api/saf/batch/{batchId}    — 삭제
 * </pre>
 */
@RestController
@RequestMapping("/api/saf/batch")
@RequiredArgsConstructor
public class SafBatchController {

    private final SafBatchService batchService;

    @GetMapping
    public ApiResponse<PageResponse<SafBatchVO>> list(SafBatchSearch search,
                                                      @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(batchService.search(search, user));
    }

    @GetMapping("/{batchId}")
    public ApiResponse<SafBatchVO> get(@PathVariable String batchId,
                                       @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(batchService.get(batchId, user));
    }

    @PostMapping
    public ApiResponse<SafBatchVO> create(@RequestBody SafBatchVO vo,
                                          @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(batchService.create(vo, user), "SAF 배치가 등록되었습니다.");
    }

    @PutMapping("/{batchId}")
    public ApiResponse<SafBatchVO> update(@PathVariable String batchId,
                                          @RequestBody SafBatchVO vo,
                                          @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(batchService.update(batchId, vo, user));
    }

    @DeleteMapping("/{batchId}")
    public ApiResponse<Void> delete(@PathVariable String batchId,
                                    @AuthenticationPrincipal IcasUser user) {
        batchService.softDelete(batchId, user);
        return ApiResponse.ok(null);
    }
}
