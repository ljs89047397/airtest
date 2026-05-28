package kr.go.molit.icas.com.authrt;

import kr.go.molit.icas.com.authrt.domain.AuthrtRoleMpngVO;
import kr.go.molit.icas.common.dto.ApiResponse;
import kr.go.molit.icas.common.security.IcasUser;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 권한-역할 매핑 관리 REST API.
 * Base URL: /api/com/authrt-role
 */
@RestController
@RequestMapping("/api/com/authrt-role")
@RequiredArgsConstructor
public class AuthrtRoleMpngController {

    private final AuthrtRoleMpngService authrtRoleMpngService;

    /** GET /api/com/authrt-role/role/{roleId} — 역할에 부여된 권한 목록 */
    @GetMapping("/role/{roleId}")
    public ApiResponse<List<AuthrtRoleMpngVO>> byRole(@PathVariable String roleId) {
        return ApiResponse.ok(authrtRoleMpngService.selectByRole(roleId));
    }

    /** GET /api/com/authrt-role/authrt/{authrtId} — 권한을 보유한 역할 목록 */
    @GetMapping("/authrt/{authrtId}")
    public ApiResponse<List<AuthrtRoleMpngVO>> byAuthrt(@PathVariable String authrtId) {
        return ApiResponse.ok(authrtRoleMpngService.selectByAuthrt(authrtId));
    }

    /**
     * POST /api/com/authrt-role — 권한-역할 매핑 추가 (MOLIT/KOTSA 전용).
     * body: { authrtId, roleId }
     */
    @PostMapping
    public ApiResponse<Void> addMapping(@RequestBody MappingRequest req,
                                        @AuthenticationPrincipal IcasUser user) {
        authrtRoleMpngService.addMapping(req.getAuthrtId(), req.getRoleId(), user);
        return ApiResponse.ok(null, "권한-역할 매핑이 추가되었습니다.");
    }

    /**
     * DELETE /api/com/authrt-role?authrtId=..&amp;roleId=.. — 매핑 소프트 삭제 (MOLIT/KOTSA 전용).
     */
    @DeleteMapping
    public ApiResponse<Void> removeMapping(@RequestParam String authrtId,
                                           @RequestParam String roleId,
                                           @AuthenticationPrincipal IcasUser user) {
        authrtRoleMpngService.removeMapping(authrtId, roleId, user);
        return ApiResponse.ok(null, "권한-역할 매핑이 해제되었습니다.");
    }

    /* ── 요청 DTO ─────────────────────────────────── */

    @Data
    public static class MappingRequest {
        private String authrtId;
        private String roleId;
    }
}
