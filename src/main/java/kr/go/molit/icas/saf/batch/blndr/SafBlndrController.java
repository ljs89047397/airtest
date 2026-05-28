package kr.go.molit.icas.saf.batch.blndr;

import kr.go.molit.icas.common.dto.ApiResponse;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.saf.batch.blndr.domain.SafBlndrVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/saf/batch/{batchId}/blndr")
@RequiredArgsConstructor
public class SafBlndrController {

    private final SafBlndrService blndrService;

    @GetMapping
    public ApiResponse<SafBlndrVO> get(@PathVariable String batchId,
                                       @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(blndrService.get(batchId, user));
    }

    @PutMapping
    public ApiResponse<SafBlndrVO> save(@PathVariable String batchId,
                                        @RequestBody SafBlndrVO vo,
                                        @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(blndrService.saveOrUpdate(batchId, vo, user), "혼합사 정보가 저장되었습니다.");
    }
}
