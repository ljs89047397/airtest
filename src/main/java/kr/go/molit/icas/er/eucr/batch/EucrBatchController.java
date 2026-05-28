package kr.go.molit.icas.er.eucr.batch;

import kr.go.molit.icas.common.dto.ApiResponse;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.er.eucr.batch.domain.EucrBatchVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * EUCR 배출권 배치 REST API (SFR-031).
 *
 * <p>Base URL: {@code /api/er/eucr/{eucrId}/batch}
 */
@RestController
@RequestMapping("/api/er/eucr/{eucrId}/batch")
@RequiredArgsConstructor
public class EucrBatchController {

    private final EucrBatchService eucrBatchService;

    @GetMapping
    public ApiResponse<List<EucrBatchVO>> list(@PathVariable String eucrId,
                                               @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(eucrBatchService.list(eucrId, user));
    }

    @GetMapping("/{batchNo}")
    public ApiResponse<EucrBatchVO> get(@PathVariable String eucrId,
                                        @PathVariable String batchNo,
                                        @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(eucrBatchService.getOne(eucrId, batchNo, user));
    }

    @PostMapping
    public ApiResponse<EucrBatchVO> add(@PathVariable String eucrId,
                                        @RequestBody EucrBatchVO vo,
                                        @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(eucrBatchService.add(eucrId, vo, user), "배치가 등록되었습니다.");
    }

    @PutMapping("/{batchNo}")
    public ApiResponse<Void> update(@PathVariable String eucrId,
                                    @PathVariable String batchNo,
                                    @RequestBody EucrBatchVO vo,
                                    @AuthenticationPrincipal IcasUser user) {
        eucrBatchService.update(eucrId, batchNo, vo, user);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/{batchNo}")
    public ApiResponse<Void> delete(@PathVariable String eucrId,
                                    @PathVariable String batchNo,
                                    @AuthenticationPrincipal IcasUser user) {
        eucrBatchService.softDelete(eucrId, batchNo, user);
        return ApiResponse.ok(null);
    }
}
