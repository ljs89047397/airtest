package kr.go.molit.icas.com.menu;

import kr.go.molit.icas.com.menu.domain.MenuTreeNode;
import kr.go.molit.icas.com.menu.domain.MenuVO;
import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.IcasUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * MenuService 단위 테스트.
 * Mapper 는 @Mock, Service 는 @InjectMocks.
 *
 * 트리 픽스처 구성:
 *   ROOT1 (menuOrd=1) ── CHILD_1_1 (menuOrd=1) ── LEAF_1_1_1 (menuOrd=1, prgrmId=PG001)
 *                     └─ CHILD_1_2 (menuOrd=2, prgrmId=PG002)
 *   ROOT2 (menuOrd=2, prgrmId=PG003)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MenuService 단위 테스트")
class MenuServiceTest {

    @Mock
    MenuMapper menuMapper;

    @InjectMocks
    MenuService menuService;

    // ── fixture: 사용자 ──
    IcasUser masterUser;
    IcasUser molitUser;
    IcasUser airlineUser;
    IcasUser limitedUser; // prgrmPathsInq 일부만 보유

    // ── fixture: 메뉴 VO ──
    MenuVO root1;
    MenuVO root2;
    MenuVO child11;
    MenuVO child12;
    MenuVO leaf111;

    @BeforeEach
    void setUp() {
        masterUser = IcasUser.builder()
                .userId("master01").userNm("마스터 관리자")
                .ognzSeCd("MOLIT").ognzId("ORG_MOLIT").master(true)
                .prgrmPathsInq(Set.of())
                .prgrmPathsInpt(Set.of())
                .roleIds(List.of("MASTER")).build();

        molitUser = IcasUser.builder()
                .userId("molit01").userNm("국토부 담당자")
                .ognzSeCd("MOLIT").ognzId("ORG_MOLIT").master(false)
                .prgrmPathsInq(Set.of("/api/com/menu", "/api/com/prgrm", "/api/em/prgrm"))
                .prgrmPathsInpt(Set.of("/api/com/menu"))
                .roleIds(List.of("ADMIN")).build();

        airlineUser = IcasUser.builder()
                .userId("airline01").userNm("항공사 담당자")
                .ognzSeCd("AIRLINE").ognzId("ORG_AIR01").oprtrId("OP0001").master(false)
                .prgrmPathsInq(Set.of())
                .prgrmPathsInpt(Set.of())
                .roleIds(List.of("AIRLINE_USER")).build();

        limitedUser = IcasUser.builder()
                .userId("limited01").userNm("제한 사용자")
                .ognzSeCd("MOLIT").ognzId("ORG_MOLIT").master(false)
                .prgrmPathsInq(Set.of("/api/em/prgrm")) // LEAF_1_1_1(/api/com/prgrm) 권한 없음
                .prgrmPathsInpt(Set.of())
                .roleIds(List.of("LIMITED")).build();

        // 평면 목록 — 부모-자식 관계
        root1 = menuVO("ROOT1", "COM", "공통메뉴1", null, 1, null, null);
        root2 = menuVO("ROOT2", "COM", "공통메뉴2", null, 2, "PG003", "/api/em/prgrm");
        child11 = menuVO("CHILD_1_1", "COM", "공통1-1", "ROOT1", 1, null, null);
        child12 = menuVO("CHILD_1_2", "COM", "공통1-2", "ROOT1", 2, "PG002", "/api/com/prgrm");
        leaf111 = menuVO("LEAF_1_1_1", "COM", "공통1-1-1", "CHILD_1_1", 1, "PG001", "/api/com/menu");
    }

    /** MenuVO 편의 생성 메서드 */
    private MenuVO menuVO(String menuId, String sysSeCd, String menuNm,
                          String upperMenuId, int menuOrd,
                          String prgrmId, String apiPathPrefix) {
        MenuVO vo = new MenuVO();
        vo.setMenuId(menuId);
        vo.setSysSeCd(sysSeCd);
        vo.setMenuNm(menuNm);
        vo.setUpperMenuId(upperMenuId);
        vo.setMenuOrd(menuOrd);
        vo.setPrgrmId(prgrmId);
        vo.setApiPathPrefix(apiPathPrefix);
        return vo;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // selectMenu (단건 조회)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("단건 조회 — 존재하는 ID 정상 반환")
    void selectMenu_정상반환() {
        given(menuMapper.selectMenu("ROOT1")).willReturn(root1);

        MenuVO result = menuService.selectMenu("ROOT1");

        assertThat(result).isNotNull();
        assertThat(result.getMenuId()).isEqualTo("ROOT1");
    }

    @Test
    @DisplayName("단건 조회 — 존재하지 않는 ID → NOT_FOUND 예외")
    void selectMenu_없는ID_예외() {
        given(menuMapper.selectMenu("NONE")).willReturn(null);

        assertThatThrownBy(() -> menuService.selectMenu("NONE"))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getCode()).isEqualTo("NOT_FOUND"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // insertMenu
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("메뉴 등록 — MOLIT 사용자 정상 등록")
    void insertMenu_정상등록() {
        MenuVO vo = menuVO("NEW_MENU", "COM", "신규메뉴", null, 99, null, null);
        given(menuMapper.existsMenu("NEW_MENU")).willReturn(false);
        given(menuMapper.insertMenu(any(MenuVO.class))).willReturn(1);
        given(menuMapper.selectMenu("NEW_MENU")).willReturn(vo);

        MenuVO result = menuService.insertMenu(vo, molitUser);

        assertThat(result.getMenuId()).isEqualTo("NEW_MENU");
        then(menuMapper).should().insertMenu(any(MenuVO.class));
    }

    @Test
    @DisplayName("메뉴 등록 — AIRLINE 사용자 → FORBIDDEN 예외")
    void insertMenu_권한없는사용자_예외() {
        MenuVO vo = menuVO("NEW_MENU", "COM", "신규메뉴", null, 99, null, null);

        assertThatThrownBy(() -> menuService.insertMenu(vo, airlineUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getCode()).isEqualTo("FORBIDDEN"));

        then(menuMapper).should(never()).insertMenu(any());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // validateNoCycle — 사이클 검출
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("사이클 검출 — 자기 자신을 upper로 지정 → BAD_REQUEST 예외")
    void validateNoCycle_자기자신_예외() {
        // menuId == upperMenuId → 즉시 예외
        assertThatThrownBy(() -> menuService.validateNoCycle("MENU_A", "MENU_A"))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException be = (BusinessException) ex;
                    assertThat(be.getCode()).isEqualTo("BAD_REQUEST");
                    assertThat(be.getMessage()).contains("자기 자신");
                });
    }

    @Test
    @DisplayName("사이클 검출 — A→B→C→A 깊은 사이클 → BAD_REQUEST 예외")
    void validateNoCycle_깊은사이클_예외() {
        /*
         * 시나리오: MENU_A 의 upper 를 MENU_C 로 지정하려 한다.
         * DB 상태: MENU_C.upperMenuId = MENU_B, MENU_B.upperMenuId = MENU_A
         *   → MENU_A 를 체인 중 재발견 → 사이클
         */
        MenuVO menuC = menuVO("MENU_C", "COM", "메뉴C", "MENU_B", 1, null, null);
        MenuVO menuB = menuVO("MENU_B", "COM", "메뉴B", "MENU_A", 1, null, null);

        // validateNoCycle("MENU_A", "MENU_C") 호출:
        //   cursor=MENU_C → selectMenu("MENU_C") → upperMenuId=MENU_B (≠MENU_A) → depth=0
        //   cursor=MENU_B → selectMenu("MENU_B") → upperMenuId=MENU_A (==MENU_A) → 사이클!
        given(menuMapper.selectMenu("MENU_C")).willReturn(menuC);
        given(menuMapper.selectMenu("MENU_B")).willReturn(menuB);

        assertThatThrownBy(() -> menuService.validateNoCycle("MENU_A", "MENU_C"))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException be = (BusinessException) ex;
                    assertThat(be.getCode()).isEqualTo("BAD_REQUEST");
                    assertThat(be.getMessage()).contains("순환 참조");
                });
    }

    @Test
    @DisplayName("사이클 검출 — 깊이 10 초과 → BAD_REQUEST 예외")
    void validateNoCycle_깊이초과_예외() {
        /*
         * 12단계 깊이 체인: MENU_0 의 upper 를 MENU_1 로 지정,
         * MENU_1 → MENU_2 → ... → MENU_11(루트 아님, upperMenuId 계속 존재)
         * → MAX_CYCLE_DEPTH(10) 에서 예외 발생
         */
        // MENU_1 ~ MENU_11까지 each가 다음을 상위로 가리킴
        for (int i = 1; i <= 10; i++) {
            String cur = "MENU_" + i;
            String next = "MENU_" + (i + 1);
            MenuVO vo = menuVO(cur, "COM", "메뉴" + i, next, i, null, null);
            given(menuMapper.selectMenu(cur)).willReturn(vo);
        }
        // MENU_11 은 상위가 있어서 루트가 아님 (한 번 더 탐색 유도)
        MenuVO menu11 = menuVO("MENU_11", "COM", "메뉴11", "MENU_12", 11, null, null);
        given(menuMapper.selectMenu("MENU_11")).willReturn(menu11);

        // MENU_0 의 upper 를 MENU_1 로 지정 시 깊이 초과
        assertThatThrownBy(() -> menuService.validateNoCycle("MENU_0", "MENU_1"))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException be = (BusinessException) ex;
                    assertThat(be.getCode()).isEqualTo("BAD_REQUEST");
                    assertThat(be.getMessage()).contains("깊이");
                });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // softDeleteMenu
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("메뉴 삭제 — 하위 메뉴 존재 시 → CONFLICT 예외")
    void softDeleteMenu_하위존재_예외() {
        given(menuMapper.countChildren("ROOT1")).willReturn(2);

        assertThatThrownBy(() -> menuService.softDeleteMenu("ROOT1", molitUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException be = (BusinessException) ex;
                    assertThat(be.getCode()).isEqualTo("CONFLICT");
                    assertThat(be.getMessage()).contains("하위 메뉴");
                });
    }

    @Test
    @DisplayName("메뉴 삭제 — 정상 삭제 (하위 없음)")
    void softDeleteMenu_정상삭제() {
        given(menuMapper.countChildren("LEAF_1_1_1")).willReturn(0);
        given(menuMapper.softDeleteMenu("LEAF_1_1_1", "molit01")).willReturn(1);

        assertThatCode(() -> menuService.softDeleteMenu("LEAF_1_1_1", molitUser))
                .doesNotThrowAnyException();

        then(menuMapper).should().softDeleteMenu("LEAF_1_1_1", "molit01");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // selectMenuTree — 트리 조립 + 권한 필터링
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("트리 조립 — 평면 목록 → 트리 변환, 루트 2개 menuOrd 기준 정렬 검증")
    void selectMenuTree_트리조립_정렬검증() {
        /*
         * 기대 트리:
         *   ROOT1 (ord=1) ── CHILD_1_1 (ord=1) ── LEAF_1_1_1 (ord=1, prgrmId=PG001)
         *                 └─ CHILD_1_2 (ord=2, prgrmId=PG002)
         *   ROOT2 (ord=2, prgrmId=PG003)
         */
        List<MenuVO> flat = List.of(root1, root2, child11, child12, leaf111);
        given(menuMapper.selectAllBySysSeCd("COM")).willReturn(flat);

        // masterUser → 필터링 없이 전체 반환
        List<MenuTreeNode> roots = menuService.selectMenuTree("COM", masterUser);

        assertThat(roots).hasSize(2);
        // menuOrd 기준 정렬 확인 (DB가 이미 정렬해서 돌려줌)
        assertThat(roots.get(0).getMenuId()).isEqualTo("ROOT1");
        assertThat(roots.get(1).getMenuId()).isEqualTo("ROOT2");
        // ROOT1 의 직접 자식: CHILD_1_1, CHILD_1_2
        MenuTreeNode root1Node = roots.get(0);
        assertThat(root1Node.getChildren()).hasSize(2);
        // CHILD_1_1 의 자식: LEAF_1_1_1
        MenuTreeNode child11Node = root1Node.getChildren().stream()
                .filter(n -> "CHILD_1_1".equals(n.getMenuId()))
                .findFirst().orElseThrow();
        assertThat(child11Node.getChildren()).hasSize(1);
        assertThat(child11Node.getChildren().get(0).getMenuId()).isEqualTo("LEAF_1_1_1");
    }

    @Test
    @DisplayName("권한 필터링 — master=true 이면 모든 메뉴 노출")
    void selectMenuTree_master_모든메뉴노출() {
        /*
         * 픽스처 트리 구조:
         *   ROOT1 ── CHILD_1_1 ── LEAF_1_1_1
         *         └─ CHILD_1_2
         *   ROOT2
         *
         * master 사용자 → 필터링 없이 전체 반환
         *   ROOT1.children = [CHILD_1_1, CHILD_1_2]
         *   ROOT2 는 단독 루트
         */
        List<MenuVO> flat = List.of(root1, root2, child11, child12, leaf111);
        given(menuMapper.selectAllBySysSeCd("COM")).willReturn(flat);

        List<MenuTreeNode> roots = menuService.selectMenuTree("COM", masterUser);

        assertThat(roots).hasSize(2);
        MenuTreeNode root1Node = roots.get(0);
        // ROOT1 직접 자식: CHILD_1_1, CHILD_1_2
        assertThat(root1Node.getChildren()).hasSize(2);
        // CHILD_1_1 의 자식: LEAF_1_1_1
        MenuTreeNode child11Node = root1Node.getChildren().stream()
                .filter(n -> "CHILD_1_1".equals(n.getMenuId()))
                .findFirst().orElseThrow();
        assertThat(child11Node.getChildren()).hasSize(1);
        assertThat(child11Node.getChildren().get(0).getMenuId()).isEqualTo("LEAF_1_1_1");
    }

    @Test
    @DisplayName("권한 필터링 — master=false, prgrmPathsInq 빈 set → 메뉴 없음")
    void selectMenuTree_빈prgrmPaths_메뉴없음() {
        List<MenuVO> flat = List.of(root1, root2, child11, child12, leaf111);
        given(menuMapper.selectAllBySysSeCd("COM")).willReturn(flat);

        // prgrmPathsInq = 빈 Set
        List<MenuTreeNode> roots = menuService.selectMenuTree("COM", airlineUser);

        // 모든 leaf 가 필터링되어 빈 부모도 제거 → 빈 결과
        assertThat(roots).isEmpty();
    }

    @Test
    @DisplayName("권한 필터링 — limitedUser는 일부 경로만 보유 → 해당 메뉴만 노출")
    void selectMenuTree_제한사용자_일부노출() {
        /*
         * limitedUser.prgrmPathsInq = {"/api/em/prgrm"}
         * - ROOT2 (apiPathPrefix=/api/em/prgrm) → 노출
         * - CHILD_1_2 (apiPathPrefix=/api/com/prgrm) → 미노출
         * - LEAF_1_1_1 (apiPathPrefix=/api/com/menu) → 미노출
         * → ROOT1(자식 전부 필터됨) 제거, ROOT2만 남음
         */
        List<MenuVO> flat = List.of(root1, root2, child11, child12, leaf111);
        given(menuMapper.selectAllBySysSeCd("COM")).willReturn(flat);

        List<MenuTreeNode> roots = menuService.selectMenuTree("COM", limitedUser);

        assertThat(roots).hasSize(1);
        assertThat(roots.get(0).getMenuId()).isEqualTo("ROOT2");
    }

    @Test
    @DisplayName("빈 부모(자식 다 필터됨) 자동 제거 검증")
    void selectMenuTree_빈부모_자동제거() {
        /*
         * molitUser.prgrmPathsInq = {"/api/com/menu", "/api/com/prgrm", "/api/em/prgrm"}
         * - LEAF_1_1_1 (apiPathPrefix=/api/com/menu) → 노출
         * - CHILD_1_2 (apiPathPrefix=/api/com/prgrm) → 노출
         * - ROOT2 (apiPathPrefix=/api/em/prgrm) → 노출
         * - CHILD_1_1(prgrmId=null) : 자식 leaf111이 남으므로 유지
         * - ROOT1(prgrmId=null) : 자식 child11, child12가 남으므로 유지
         */
        List<MenuVO> flat = List.of(root1, root2, child11, child12, leaf111);
        given(menuMapper.selectAllBySysSeCd("COM")).willReturn(flat);

        List<MenuTreeNode> roots = menuService.selectMenuTree("COM", molitUser);

        // ROOT1, ROOT2 모두 노출
        assertThat(roots).hasSize(2);
        MenuTreeNode root1Node = roots.stream()
                .filter(n -> "ROOT1".equals(n.getMenuId()))
                .findFirst()
                .orElseThrow();
        // ROOT1 아래 child11(자식 leaf111 있음) 유지 검증
        assertThat(root1Node.getChildren()).isNotEmpty();
    }
}
