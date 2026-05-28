package kr.go.molit.icas.com.menu;

import kr.go.molit.icas.com.menu.domain.MenuTreeNode;
import kr.go.molit.icas.com.menu.domain.MenuVO;
import kr.go.molit.icas.common.dto.ApiResponse;
import kr.go.molit.icas.common.security.IcasUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 메뉴 관리 API.
 * Base URL: /api/com/menu
 */
@RestController
@RequestMapping("/api/com/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    /**
     * GET /api/com/menu?sysSeCd=COM
     * 평면 목록 조회 (관리용).
     */
    @GetMapping
    public ApiResponse<List<MenuVO>> list(
            @RequestParam(required = false) String sysSeCd) {
        return ApiResponse.ok(menuService.selectMenus(sysSeCd));
    }

    /**
     * GET /api/com/menu/tree?sysSeCd=COM
     * 권한 필터링 적용 메뉴 트리 조회 (로그인 사용자 기준).
     */
    @GetMapping("/tree")
    public ApiResponse<List<MenuTreeNode>> tree(
            @RequestParam(required = false) String sysSeCd,
            @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(menuService.selectMenuTree(sysSeCd, user));
    }

    /**
     * GET /api/com/menu/{menuId}
     * 단건 조회.
     */
    @GetMapping("/{menuId}")
    public ApiResponse<MenuVO> get(@PathVariable String menuId) {
        return ApiResponse.ok(menuService.selectMenu(menuId));
    }

    /**
     * POST /api/com/menu
     * 메뉴 등록 (MOLIT/KOTSA 전용).
     */
    @PostMapping
    public ApiResponse<MenuVO> create(
            @RequestBody MenuVO vo,
            @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(menuService.insertMenu(vo, user), "등록되었습니다.");
    }

    /**
     * PUT /api/com/menu/{menuId}
     * 메뉴 수정 (MOLIT/KOTSA 전용).
     */
    @PutMapping("/{menuId}")
    public ApiResponse<Void> update(
            @PathVariable String menuId,
            @RequestBody MenuVO vo,
            @AuthenticationPrincipal IcasUser user) {
        menuService.updateMenu(menuId, vo, user);
        return ApiResponse.ok(null, "수정되었습니다.");
    }

    /**
     * DELETE /api/com/menu/{menuId}
     * 메뉴 소프트 삭제 (MOLIT/KOTSA 전용).
     */
    @DeleteMapping("/{menuId}")
    public ApiResponse<Void> delete(
            @PathVariable String menuId,
            @AuthenticationPrincipal IcasUser user) {
        menuService.softDeleteMenu(menuId, user);
        return ApiResponse.ok(null, "삭제되었습니다.");
    }
}
