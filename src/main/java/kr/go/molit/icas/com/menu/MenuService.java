package kr.go.molit.icas.com.menu;

import kr.go.molit.icas.com.menu.domain.MenuTreeNode;
import kr.go.molit.icas.com.menu.domain.MenuVO;
import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.IcasUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 메뉴 관리 서비스.
 *
 * <h3>트리 조립 핵심 결정 사항</h3>
 * <ol>
 *   <li><b>1회 쿼리</b>: selectAllBySysSeCd 에서 LEFT JOIN tn_prgrm 으로
 *       api_path_prefix 까지 포함해 가져온다. N+1 없음.</li>
 *   <li><b>권한 필터링 기준</b>: IcasUser.prgrmPathsInq (로그인 시 캐시된
 *       api_path_prefix 집합) 와 MenuVO.apiPathPrefix (JOIN 컬럼) 를 비교.
 *       AuthMapper 재호출 없이 세션 캐시 재사용.</li>
 *   <li><b>트리 조립</b>: Java 단에서 upper_menu_id 기준 LinkedHashMap 으로
 *       O(n) 처리. DB 정렬(menu_ord ASC, menu_id ASC) 로 순서 보장.</li>
 *   <li><b>빈 부모 제거</b>: filterTree 재귀(bottom-up) — leaf 가 모두 제거되면
 *       부모도 자동 제거.</li>
 *   <li><b>master 우회</b>: isMaster() = true 이면 allowedPaths = null 처리,
 *       filterTree 생략.</li>
 * </ol>
 *
 * <h3>사이클 검출</h3>
 * upper_menu_id 체인을 따라 최대 10단계 탐색.
 * menuId 재등장 또는 깊이 초과 시 BAD_REQUEST.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MenuService {

    private static final int MAX_CYCLE_DEPTH = 10;

    private final MenuMapper menuMapper;

    // ------------------------------------------------------------------ //
    //  조회 — 평면 목록 (관리용)
    // ------------------------------------------------------------------ //

    /**
     * sysSeCd 기준 유효 메뉴 평면 목록 (관리 화면용).
     * sysSeCd 가 null 이면 전체 시스템 메뉴 반환.
     */
    public List<MenuVO> selectMenus(String sysSeCd) {
        return menuMapper.selectAllBySysSeCd(sysSeCd);
    }

    /**
     * 단건 조회. 없으면 404.
     */
    public MenuVO selectMenu(String menuId) {
        MenuVO vo = menuMapper.selectMenu(menuId);
        if (vo == null) throw BusinessException.notFound("메뉴");
        return vo;
    }

    // ------------------------------------------------------------------ //
    //  트리 조회 (권한 필터링 적용)
    // ------------------------------------------------------------------ //

    /**
     * 권한 필터링이 적용된 메뉴 트리 반환.
     *
     * @param sysSeCd 시스템 구분 코드 (COM/EMP/ER/VR/SAF/PTL)
     * @param user    로그인 사용자
     * @return 루트 메뉴 트리 노드 목록 (menu_ord ASC, menu_id ASC)
     */
    public List<MenuTreeNode> selectMenuTree(String sysSeCd, IcasUser user) {

        // 1. DB 1회 조회 (LEFT JOIN tn_prgrm → apiPathPrefix 포함)
        List<MenuVO> allMenus = menuMapper.selectAllBySysSeCd(sysSeCd);

        // 2. menuId → apiPathPrefix 맵 구성 (권한 필터링용)
        //    prgrm 미연결 메뉴(폴더) 는 apiPathPrefix = null
        Map<String, String> apiPathByMenuId = new HashMap<>(allMenus.size());
        for (MenuVO vo : allMenus) {
            apiPathByMenuId.put(vo.getMenuId(), vo.getApiPathPrefix());
        }

        // 3. 트리 노드 생성 (삽입 순서 = DB 정렬 순서 유지)
        Map<String, MenuTreeNode> nodeMap = new LinkedHashMap<>(allMenus.size());
        for (MenuVO vo : allMenus) {
            nodeMap.put(vo.getMenuId(), MenuTreeNode.from(vo));
        }

        // 4. 부모-자식 연결 + 루트 수집
        List<MenuTreeNode> roots = new ArrayList<>();
        for (MenuVO vo : allMenus) {
            MenuTreeNode node = nodeMap.get(vo.getMenuId());
            if (vo.getUpperMenuId() == null) {
                roots.add(node);
            } else {
                MenuTreeNode parent = nodeMap.get(vo.getUpperMenuId());
                if (parent != null) {
                    parent.getChildren().add(node);
                } else {
                    // 상위 메뉴가 비활성화된 경우 — 고아 노드를 루트로 처리
                    roots.add(node);
                }
            }
        }

        // 5. 권한 필터링 (master 는 모든 메뉴 노출)
        if (!user.isMaster()) {
            Set<String> allowedPaths = user.getPrgrmPathsInq();
            roots = filterTree(roots, apiPathByMenuId, allowedPaths);
        }

        return roots;
    }

    // ------------------------------------------------------------------ //
    //  트리 필터링 (재귀 bottom-up)
    // ------------------------------------------------------------------ //

    /**
     * 권한 없는 leaf 메뉴 제거 후 빈 부모를 재귀적으로 제거(bottom-up).
     *
     * <ul>
     *   <li>자식이 남아 있는 노드(폴더) → 유지</li>
     *   <li>leaf + prgrmId 있음: apiPathPrefix 가 allowedPaths 에 포함되면 유지</li>
     *   <li>leaf + prgrmId 없음 (빈 폴더): 제거</li>
     * </ul>
     */
    private List<MenuTreeNode> filterTree(List<MenuTreeNode> nodes,
                                          Map<String, String> apiPathByMenuId,
                                          Set<String> allowedPaths) {
        List<MenuTreeNode> result = new ArrayList<>();
        for (MenuTreeNode node : nodes) {
            // 자식 먼저 재귀 처리 (bottom-up)
            node.setChildren(filterTree(node.getChildren(), apiPathByMenuId, allowedPaths));

            final boolean keep;
            if (!node.getChildren().isEmpty()) {
                // 자식이 남아 있는 폴더 → 유지
                keep = true;
            } else if (node.getPrgrmId() != null) {
                // leaf + prgrm 연결 메뉴: api_path_prefix 로 권한 확인
                String apiPath = apiPathByMenuId.get(node.getMenuId());
                keep = apiPath != null && allowedPaths.contains(apiPath);
            } else {
                // prgrm 없는 빈 폴더 → 제거
                keep = false;
            }

            if (keep) result.add(node);
        }
        return result;
    }

    // ------------------------------------------------------------------ //
    //  변경 (MOLIT/KOTSA 전용)
    // ------------------------------------------------------------------ //

    /**
     * 메뉴 등록.
     */
    @Transactional
    public MenuVO insertMenu(MenuVO vo, IcasUser user) {
        assertMolitOrKotsa(user);

        if (vo.getMenuId() == null || vo.getMenuId().isBlank())
            throw BusinessException.badRequest("메뉴 ID 는 필수입니다.");
        if (vo.getMenuNm() == null || vo.getMenuNm().isBlank())
            throw BusinessException.badRequest("메뉴 명칭은 필수입니다.");

        if (menuMapper.existsMenu(vo.getMenuId()))
            throw BusinessException.conflict("이미 존재하는 메뉴 ID 입니다: " + vo.getMenuId());

        if (vo.getUpperMenuId() != null) {
            validateNoCycle(vo.getMenuId(), vo.getUpperMenuId());
        }

        vo.setFrstRegUserId(user.getUserId());
        vo.setLastChgUserId(user.getUserId());
        menuMapper.insertMenu(vo);
        return menuMapper.selectMenu(vo.getMenuId());
    }

    /**
     * 메뉴 수정.
     */
    @Transactional
    public void updateMenu(String menuId, MenuVO vo, IcasUser user) {
        assertMolitOrKotsa(user);
        vo.setMenuId(menuId);

        if (vo.getUpperMenuId() != null) {
            validateNoCycle(menuId, vo.getUpperMenuId());
        }

        vo.setLastChgUserId(user.getUserId());
        int affected = menuMapper.updateMenu(vo);
        if (affected == 0) throw BusinessException.notFound("메뉴");
    }

    /**
     * 메뉴 소프트 삭제.
     * 하위 메뉴 존재 시 cascade 금지 → CONFLICT(hasChildren).
     */
    @Transactional
    public void softDeleteMenu(String menuId, IcasUser user) {
        assertMolitOrKotsa(user);

        int childCount = menuMapper.countChildren(menuId);
        if (childCount > 0) {
            throw BusinessException.conflict(
                    "하위 메뉴(" + childCount + "개)가 존재하여 삭제할 수 없습니다. 하위 메뉴를 먼저 삭제하세요.");
        }

        int affected = menuMapper.softDeleteMenu(menuId, user.getUserId());
        if (affected == 0) throw BusinessException.notFound("메뉴");
    }

    // ------------------------------------------------------------------ //
    //  사이클 검출
    // ------------------------------------------------------------------ //

    /**
     * upper_menu_id 체인을 따라 올라가며 menuId 재등장 시 사이클 → BAD_REQUEST.
     * 깊이 제한 MAX_CYCLE_DEPTH(10).
     *
     * @param menuId      등록/수정 대상 메뉴 ID
     * @param upperMenuId 지정할 상위 메뉴 ID
     */
    void validateNoCycle(String menuId, String upperMenuId) {
        if (menuId.equals(upperMenuId)) {
            throw BusinessException.badRequest("메뉴가 자기 자신을 상위 메뉴로 지정할 수 없습니다.");
        }

        String cursor = upperMenuId;
        for (int depth = 0; depth < MAX_CYCLE_DEPTH; depth++) {
            MenuVO parent = menuMapper.selectMenu(cursor);
            if (parent == null || parent.getUpperMenuId() == null) {
                return; // 루트 도달 — 사이클 없음
            }
            if (menuId.equals(parent.getUpperMenuId())) {
                throw BusinessException.badRequest(
                        "메뉴 계층 구조에 순환 참조가 발생합니다. menuId=" + menuId);
            }
            cursor = parent.getUpperMenuId();
        }
        throw BusinessException.badRequest(
                "메뉴 계층 깊이가 허용 한계(" + MAX_CYCLE_DEPTH + ")를 초과합니다.");
    }

    // ------------------------------------------------------------------ //
    //  내부 검증
    // ------------------------------------------------------------------ //

    private void assertMolitOrKotsa(IcasUser user) {
        if (!user.isMolitOrKotsa()) {
            throw BusinessException.forbidden("MOLIT/KOTSA 사용자만 메뉴를 변경할 수 있습니다.");
        }
    }
}
