package kr.go.molit.icas.er.oom.item;

import kr.go.molit.icas.common.dto.ApiResponse;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.er.oom.item.domain.OomCheckItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * OoM 점검 항목 REST API.
 *
 * <p>Base URL: {@code /api/er/oom/{oomId}/item}
 *
 * <pre>
 * GET    /api/er/oom/{oomId}/item               — 1~18 자동 + 100+ 사용자 추가 항목 전체 조회
 * GET    /api/er/oom/{oomId}/item/{itemNo}      — 단건 조회
 * POST   /api/er/oom/{oomId}/item               — 사용자 항목 추가 (KOTSA, item_no=100+)
 * PUT    /api/er/oom/{oomId}/item/{itemNo}      — 항목 수정 (KOTSA, 자동/추가 모두)
 * DELETE /api/er/oom/{oomId}/item/{itemNo}      — 사용자 항목 삭제 (자동은 검증 재실행으로)
 * </pre>
 */
@RestController
@RequestMapping("/api/er/oom/{oomId}/item")
@RequiredArgsConstructor
public class OomCheckItemController {

    private final OomCheckItemService oomCheckItemService;

    @GetMapping
    public ApiResponse<List<OomCheckItemVO>> list(@PathVariable String oomId,
                                                  @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(oomCheckItemService.list(oomId, user));
    }

    @GetMapping("/{itemNo}")
    public ApiResponse<OomCheckItemVO> get(@PathVariable String oomId,
                                           @PathVariable int itemNo,
                                           @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(oomCheckItemService.getOne(oomId, itemNo, user));
    }

    @PostMapping
    public ApiResponse<OomCheckItemVO> add(@PathVariable String oomId,
                                           @RequestBody OomCheckItemVO vo,
                                           @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(oomCheckItemService.addUserItem(oomId, vo, user), "사용자 항목이 등록되었습니다.");
    }

    @PutMapping("/{itemNo}")
    public ApiResponse<Void> update(@PathVariable String oomId,
                                    @PathVariable int itemNo,
                                    @RequestBody OomCheckItemVO vo,
                                    @AuthenticationPrincipal IcasUser user) {
        oomCheckItemService.update(oomId, itemNo, vo, user);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/{itemNo}")
    public ApiResponse<Void> delete(@PathVariable String oomId,
                                    @PathVariable int itemNo,
                                    @AuthenticationPrincipal IcasUser user) {
        oomCheckItemService.softDelete(oomId, itemNo, user);
        return ApiResponse.ok(null);
    }
}
