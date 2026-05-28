package kr.go.molit.icas.vr.inpt;

import kr.go.molit.icas.common.dto.ApiResponse;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.vr.inpt.domain.VrInputInfoVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 검증 입력자료 REST API.
 *
 * <pre>
 * GET    /api/vr/{vrId}/inpt             — 목록 조회
 * POST   /api/vr/{vrId}/inpt             — 자료 추가 (VERIFIER, DRAFT)
 * PUT    /api/vr/{vrId}/inpt/{inputSn}   — 자료 수정
 * DELETE /api/vr/{vrId}/inpt/{inputSn}   — 자료 삭제
 * </pre>
 */
@RestController
@RequestMapping("/api/vr/{vrId}/inpt")
@RequiredArgsConstructor
public class VrInputInfoController {

    private final VrInputInfoService inptService;

    @GetMapping
    public ApiResponse<List<VrInputInfoVO>> list(@PathVariable String vrId,
                                                  @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(inptService.list(vrId, user));
    }

    @PostMapping
    public ApiResponse<VrInputInfoVO> add(@PathVariable String vrId,
                                          @RequestBody VrInputInfoVO vo,
                                          @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(inptService.add(vrId, vo, user), "자료가 추가되었습니다.");
    }

    @PutMapping("/{inputSn}")
    public ApiResponse<VrInputInfoVO> update(@PathVariable String vrId,
                                              @PathVariable int inputSn,
                                              @RequestBody VrInputInfoVO vo,
                                              @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(inptService.update(vrId, inputSn, vo, user));
    }

    @DeleteMapping("/{inputSn}")
    public ApiResponse<Void> delete(@PathVariable String vrId,
                                    @PathVariable int inputSn,
                                    @AuthenticationPrincipal IcasUser user) {
        inptService.delete(vrId, inputSn, user);
        return ApiResponse.ok(null);
    }
}
