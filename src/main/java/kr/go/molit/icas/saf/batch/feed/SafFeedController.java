package kr.go.molit.icas.saf.batch.feed;

import kr.go.molit.icas.common.dto.ApiResponse;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.saf.batch.feed.domain.SafFeedVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/saf/batch/{batchId}/feed")
@RequiredArgsConstructor
public class SafFeedController {

    private final SafFeedService feedService;

    @GetMapping
    public ApiResponse<SafFeedVO> get(@PathVariable String batchId,
                                      @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(feedService.get(batchId, user));
    }

    @PutMapping
    public ApiResponse<SafFeedVO> save(@PathVariable String batchId,
                                       @RequestBody SafFeedVO vo,
                                       @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(feedService.saveOrUpdate(batchId, vo, user), "원료·제품 정보가 저장되었습니다.");
    }
}
