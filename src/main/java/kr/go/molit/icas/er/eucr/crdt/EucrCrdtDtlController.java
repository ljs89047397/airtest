package kr.go.molit.icas.er.eucr.crdt;

import kr.go.molit.icas.common.dto.ApiResponse;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.er.eucr.crdt.domain.EucrCrdtDtlVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * EUCR 일련번호 상세 REST API.
 *
 * <pre>
 * GET    /api/er/eucr/{eucrId}/crdt                      — EUCR 전체 일련번호 목록
 * GET    /api/er/eucr/{eucrId}/crdt/by-batch/{batchNo}   — 배치 내 일련번호 목록
 * POST   /api/er/eucr/{eucrId}/crdt                      — 단건 등록 (이중사용 BLOCKED 차단)
 * POST   /api/er/eucr/{eucrId}/crdt/bulk                 — 일괄 등록 (CSV / 범위 expand)
 * DELETE /api/er/eucr/{eucrId}/crdt/{crdtNo}             — 소프트삭제
 * </pre>
 */
@RestController
@RequestMapping("/api/er/eucr/{eucrId}/crdt")
@RequiredArgsConstructor
public class EucrCrdtDtlController {

    private final EucrCrdtDtlService eucrCrdtDtlService;

    @GetMapping
    public ApiResponse<List<EucrCrdtDtlVO>> listByEucr(@PathVariable String eucrId,
                                                       @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(eucrCrdtDtlService.listByEucr(eucrId, user));
    }

    @GetMapping("/by-batch/{batchNo}")
    public ApiResponse<List<EucrCrdtDtlVO>> listByBatch(@PathVariable String eucrId,
                                                        @PathVariable String batchNo,
                                                        @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(eucrCrdtDtlService.listByBatch(eucrId, batchNo, user));
    }

    @PostMapping
    public ApiResponse<EucrCrdtDtlVO> add(@PathVariable String eucrId,
                                          @RequestBody EucrCrdtDtlVO vo,
                                          @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(eucrCrdtDtlService.add(eucrId, vo, user), "일련번호가 등록되었습니다.");
    }

    @PostMapping("/bulk")
    public ApiResponse<List<EucrCrdtDtlVO>> addBulk(@PathVariable String eucrId,
                                                    @RequestBody List<EucrCrdtDtlVO> rows,
                                                    @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(eucrCrdtDtlService.addBulk(eucrId, rows, user),
                "일련번호 " + rows.size() + " 건이 일괄 등록되었습니다.");
    }

    @DeleteMapping("/{crdtNo}")
    public ApiResponse<Void> delete(@PathVariable String eucrId,
                                    @PathVariable String crdtNo,
                                    @AuthenticationPrincipal IcasUser user) {
        eucrCrdtDtlService.softDelete(eucrId, crdtNo, user);
        return ApiResponse.ok(null);
    }
}
