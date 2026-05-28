package kr.go.molit.icas.com.menu.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 메뉴 트리 응답 노드.
 * children 에 하위 노드를 중첩하여 반환한다.
 */
@Getter
@Setter
public class MenuTreeNode {

    /** 메뉴 ID */
    private String         menuId;

    /** 메뉴 명칭 */
    private String         menuNm;

    /** 정렬 순서 */
    private Integer        menuOrd;

    /** 연결 프로그램 ID (leaf 메뉴만 non-null) */
    private String         prgrmId;

    /** 화면 URL (JOIN) */
    private String         prgrmUrl;

    /** 아이콘 명칭 */
    private String         iconNm;

    /** 하위 메뉴 (재귀) */
    private List<MenuTreeNode> children = new ArrayList<>();

    public static MenuTreeNode from(MenuVO vo) {
        MenuTreeNode node = new MenuTreeNode();
        node.setMenuId(vo.getMenuId());
        node.setMenuNm(vo.getMenuNm());
        node.setMenuOrd(vo.getMenuOrd());
        node.setPrgrmId(vo.getPrgrmId());
        node.setPrgrmUrl(vo.getPrgrmUrl());
        node.setIconNm(vo.getIconNm());
        return node;
    }
}
