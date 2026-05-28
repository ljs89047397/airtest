package kr.go.molit.icas.com.role;

import kr.go.molit.icas.com.role.domain.RoleVO;
import kr.go.molit.icas.common.dto.ApiResponse;
import kr.go.molit.icas.common.security.IcasUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 역할 관리 REST API.
 * Base URL: /api/com/role
 */
@RestController
@RequestMapping("/api/com/role")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    /** GET /api/com/role — 유효한 역할 전체 목록 */
    @GetMapping
    public ApiResponse<List<RoleVO>> listRoles() {
        return ApiResponse.ok(roleService.listRoles());
    }

    /** GET /api/com/role/{roleId} — 단건 조회 */
    @GetMapping("/{roleId}")
    public ApiResponse<RoleVO> getRole(@PathVariable String roleId) {
        return ApiResponse.ok(roleService.getRole(roleId));
    }

    /** POST /api/com/role — 역할 등록 (MOLIT/KOTSA 전용) */
    @PostMapping
    public ApiResponse<RoleVO> createRole(@RequestBody RoleVO vo,
                                          @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(roleService.createRole(vo, user), "역할이 등록되었습니다.");
    }

    /** PUT /api/com/role/{roleId} — 역할 수정 (MOLIT/KOTSA 전용) */
    @PutMapping("/{roleId}")
    public ApiResponse<Void> updateRole(@PathVariable String roleId,
                                        @RequestBody RoleVO vo,
                                        @AuthenticationPrincipal IcasUser user) {
        roleService.updateRole(roleId, vo, user);
        return ApiResponse.ok(null, "역할이 수정되었습니다.");
    }

    /** DELETE /api/com/role/{roleId} — 역할 소프트 삭제 (MOLIT/KOTSA 전용) */
    @DeleteMapping("/{roleId}")
    public ApiResponse<Void> deleteRole(@PathVariable String roleId,
                                        @AuthenticationPrincipal IcasUser user) {
        roleService.softDeleteRole(roleId, user);
        return ApiResponse.ok(null, "역할이 삭제되었습니다.");
    }
}
