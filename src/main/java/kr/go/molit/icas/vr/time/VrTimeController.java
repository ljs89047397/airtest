package kr.go.molit.icas.vr.time;

import kr.go.molit.icas.common.dto.ApiResponse;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.vr.time.domain.VrTimeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 검증 시간 REST API.
 *
 * <pre>
 * GET /api/vr/{vrId}/time  — 조회
 * PUT /api/vr/{vrId}/time  — 저장/수정 (total_hrs 자동 계산)
 * </pre>
 */
@RestController
@RequestMapping("/api/vr/{vrId}/time")
@RequiredArgsConstructor
public class VrTimeController {

    private final VrTimeService timeService;

    @GetMapping
    public ApiResponse<VrTimeVO> get(@PathVariable String vrId,
                                     @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(timeService.get(vrId, user));
    }

    @PutMapping
    public ApiResponse<VrTimeVO> save(@PathVariable String vrId,
                                      @RequestBody VrTimeVO vo,
                                      @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(timeService.saveOrUpdate(vrId, vo, user), "검증 시간이 저장되었습니다.");
    }
}
