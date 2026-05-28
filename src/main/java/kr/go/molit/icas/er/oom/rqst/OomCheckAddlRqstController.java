package kr.go.molit.icas.er.oom.rqst;

import kr.go.molit.icas.common.dto.ApiResponse;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.er.oom.rqst.domain.OomCheckAddlRqstVO;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * OoM 추가 설명 요청 REST API (게시판 스타일).
 *
 * <pre>
 * GET    /api/er/oom/{oomId}/rqst                  — 요청 목록 조회 (양측)
 * POST   /api/er/oom/{oomId}/rqst                  — 요청 등록 (KOTSA)
 * PUT    /api/er/oom/{oomId}/rqst/{rqstSn}/respond — 응답 입력 (AIRLINE)
 * DELETE /api/er/oom/{oomId}/rqst/{rqstSn}         — 요청 삭제 (KOTSA)
 * </pre>
 */
@RestController
@RequestMapping("/api/er/oom/{oomId}/rqst")
@RequiredArgsConstructor
public class OomCheckAddlRqstController {

    private final OomCheckAddlRqstService rqstService;

    @GetMapping
    public ApiResponse<List<OomCheckAddlRqstVO>> list(@PathVariable String oomId,
                                                      @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(rqstService.list(oomId, user));
    }

    @PostMapping
    public ApiResponse<OomCheckAddlRqstVO> add(@PathVariable String oomId,
                                               @RequestBody RqstRequest body,
                                               @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(rqstService.add(oomId, body.getRqstCn(), user), "요청이 등록되었습니다.");
    }

    @PutMapping("/{rqstSn}/respond")
    public ApiResponse<Void> respond(@PathVariable String oomId,
                                     @PathVariable int rqstSn,
                                     @RequestBody RespRequest body,
                                     @AuthenticationPrincipal IcasUser user) {
        rqstService.respond(oomId, rqstSn, body.getRespCn(), user);
        return ApiResponse.ok(null, "응답이 등록되었습니다.");
    }

    @DeleteMapping("/{rqstSn}")
    public ApiResponse<Void> delete(@PathVariable String oomId,
                                    @PathVariable int rqstSn,
                                    @AuthenticationPrincipal IcasUser user) {
        rqstService.softDelete(oomId, rqstSn, user);
        return ApiResponse.ok(null);
    }

    @Getter
    @Setter
    public static class RqstRequest {
        private String rqstCn;
    }

    @Getter
    @Setter
    public static class RespRequest {
        private String respCn;
    }
}
