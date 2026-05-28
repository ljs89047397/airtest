package kr.go.molit.icas.saf.batch.ghg;

import kr.go.molit.icas.common.dto.ApiResponse;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.saf.batch.ghg.domain.SafGhgVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/saf/batch/{batchId}/ghg")
@RequiredArgsConstructor
public class SafGhgController {

    private final SafGhgService ghgService;

    @GetMapping
    public ApiResponse<SafGhgVO> get(@PathVariable String batchId,
                                     @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(ghgService.get(batchId, user));
    }

    @PutMapping
    public ApiResponse<SafGhgVO> save(@PathVariable String batchId,
                                      @RequestBody SafGhgVO vo,
                                      @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(ghgService.saveOrUpdate(batchId, vo, user), "GHG 배출 정보가 저장되었습니다.");
    }
}
