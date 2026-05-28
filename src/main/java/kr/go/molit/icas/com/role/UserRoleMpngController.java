package kr.go.molit.icas.com.role;

import kr.go.molit.icas.com.role.domain.UserRoleMpngVO;
import kr.go.molit.icas.common.dto.ApiResponse;
import kr.go.molit.icas.common.security.IcasUser;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 사용자-역할 시계열 매핑 REST API.
 * Base URL: /api/com/user-role
 */
@RestController
@RequestMapping("/api/com/user-role")
@RequiredArgsConstructor
public class UserRoleMpngController {

    private final UserRoleMpngService userRoleMpngService;

    /** GET /api/com/user-role/user/{userId} — 현재 유효 역할 목록 */
    @GetMapping("/user/{userId}")
    public ApiResponse<List<UserRoleMpngVO>> activeRoles(@PathVariable String userId) {
        return ApiResponse.ok(userRoleMpngService.selectActiveRolesByUser(userId));
    }

    /** GET /api/com/user-role/user/{userId}/history — 역할 부여/회수 전체 이력 */
    @GetMapping("/user/{userId}/history")
    public ApiResponse<List<UserRoleMpngVO>> roleHistory(@PathVariable String userId) {
        return ApiResponse.ok(userRoleMpngService.selectRoleHistory(userId));
    }

    /**
     * POST /api/com/user-role — 역할 부여 (MOLIT/KOTSA 전용).
     * body: { userId, roleId, userOgnzSeCd }
     */
    @PostMapping
    public ApiResponse<Void> grantRole(@RequestBody GrantRequest req,
                                       @AuthenticationPrincipal IcasUser user) {
        userRoleMpngService.grantRole(req.getUserId(), req.getRoleId(),
                req.getUserOgnzSeCd(), user);
        return ApiResponse.ok(null, "역할이 부여되었습니다.");
    }

    /**
     * DELETE /api/com/user-role?userId=..&amp;roleId=.. — 역할 회수 (MOLIT/KOTSA 전용).
     */
    @DeleteMapping
    public ApiResponse<Void> revokeRole(@RequestParam String userId,
                                        @RequestParam String roleId,
                                        @AuthenticationPrincipal IcasUser user) {
        userRoleMpngService.revokeRole(userId, roleId, user);
        return ApiResponse.ok(null, "역할이 회수되었습니다.");
    }

    /* ── 요청 DTO ─────────────────────────────────── */

    @Data
    public static class GrantRequest {
        /** 역할을 부여받을 대상 사용자 ID */
        private String userId;
        /** 부여할 역할 ID */
        private String roleId;
        /** 대상 사용자의 조직구분코드 (호환성 검증용) */
        private String userOgnzSeCd;
    }
}
