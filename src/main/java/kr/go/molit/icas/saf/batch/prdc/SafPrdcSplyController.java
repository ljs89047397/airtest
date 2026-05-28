package kr.go.molit.icas.saf.batch.prdc;

import kr.go.molit.icas.common.dto.ApiResponse;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.saf.batch.prdc.domain.SafPrdcSplyVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * SAF 생산사·공급사 REST API.
 * GET /api/saf/batch/{batchId}/prdc
 * PUT /api/saf/batch/{batchId}/prdc
 */
@RestController
@RequestMapping("/api/saf/batch/{batchId}/prdc")
@RequiredArgsConstructor
public class SafPrdcSplyController {

    private final SafPrdcSplyService prdcService;

    @GetMapping
    public ApiResponse<SafPrdcSplyVO> get(@PathVariable String batchId,
                                          @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(prdcService.get(batchId, user));
    }

    @PutMapping
    public ApiResponse<SafPrdcSplyVO> save(@PathVariable String batchId,
                                           @RequestBody SafPrdcSplyVO vo,
                                           @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(prdcService.saveOrUpdate(batchId, vo, user), "생산사·공급사 정보가 저장되었습니다.");
    }
}
