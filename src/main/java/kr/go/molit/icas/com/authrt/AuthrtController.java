package kr.go.molit.icas.com.authrt;

import kr.go.molit.icas.com.authrt.domain.AuthrtVO;
import kr.go.molit.icas.common.dto.ApiResponse;
import kr.go.molit.icas.common.security.IcasUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 시스템 권한 관리 REST API.
 * Base URL: /api/com/authrt
 */
@RestController
@RequestMapping("/api/com/authrt")
@RequiredArgsConstructor
public class AuthrtController {

    private final AuthrtService authrtService;

    /** GET /api/com/authrt — 유효한 권한 전체 목록 */
    @GetMapping
    public ApiResponse<List<AuthrtVO>> listAuthrts() {
        return ApiResponse.ok(authrtService.listAuthrts());
    }

    /** GET /api/com/authrt/{authrtId} — 단건 조회 */
    @GetMapping("/{authrtId}")
    public ApiResponse<AuthrtVO> getAuthrt(@PathVariable String authrtId) {
        return ApiResponse.ok(authrtService.getAuthrt(authrtId));
    }

    /** POST /api/com/authrt — 권한 등록 (MOLIT/KOTSA 전용) */
    @PostMapping
    public ApiResponse<AuthrtVO> createAuthrt(@RequestBody AuthrtVO vo,
                                              @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(authrtService.createAuthrt(vo, user), "권한이 등록되었습니다.");
    }

    /** PUT /api/com/authrt/{authrtId} — 권한 수정 (MOLIT/KOTSA 전용) */
    @PutMapping("/{authrtId}")
    public ApiResponse<Void> updateAuthrt(@PathVariable String authrtId,
                                          @RequestBody AuthrtVO vo,
                                          @AuthenticationPrincipal IcasUser user) {
        authrtService.updateAuthrt(authrtId, vo, user);
        return ApiResponse.ok(null, "권한이 수정되었습니다.");
    }

    /** DELETE /api/com/authrt/{authrtId} — 권한 소프트 삭제 (MOLIT/KOTSA 전용) */
    @DeleteMapping("/{authrtId}")
    public ApiResponse<Void> deleteAuthrt(@PathVariable String authrtId,
                                          @AuthenticationPrincipal IcasUser user) {
        authrtService.softDeleteAuthrt(authrtId, user);
        return ApiResponse.ok(null, "권한이 삭제되었습니다.");
    }
}
