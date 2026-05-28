package kr.go.molit.icas.com.menu;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import kr.go.molit.icas.com.menu.domain.MenuTreeNode;
import kr.go.molit.icas.com.menu.domain.MenuVO;
import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.exception.GlobalExceptionHandler;
import kr.go.molit.icas.common.security.IcasUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MenuController 슬라이스 테스트.
 * Spring Boot 없이 순수 MockMvcBuilders.standaloneSetup 사용.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MenuController 슬라이스 테스트")
class MenuControllerTest {

    MockMvc mockMvc;

    @Mock
    MenuService menuService;

    ObjectMapper objectMapper;

    // ── fixture ──
    IcasUser molitUser;
    IcasUser airlineUser;
    IcasUser masterUser;

    @BeforeEach
    void setUp() {
        MenuController controller = new MenuController(menuService);
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver())
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        molitUser = IcasUser.builder()
                .userId("molit01").userNm("국토부 담당자")
                .ognzSeCd("MOLIT").ognzId("ORG_MOLIT").master(false)
                .prgrmPathsInq(Set.of("/api/com/menu"))
                .prgrmPathsInpt(Set.of("/api/com/menu"))
                .roleIds(List.of("ADMIN")).build();

        airlineUser = IcasUser.builder()
                .userId("airline01").userNm("항공사 담당자")
                .ognzSeCd("AIRLINE").ognzId("ORG_AIR01").oprtrId("OP0001").master(false)
                .prgrmPathsInq(Set.of())
                .prgrmPathsInpt(Set.of())
                .roleIds(List.of("AIRLINE_USER")).build();

        masterUser = IcasUser.builder()
                .userId("master01").userNm("마스터 관리자")
                .ognzSeCd("MOLIT").ognzId("ORG_MOLIT").master(true)
                .prgrmPathsInq(Set.of())
                .prgrmPathsInpt(Set.of())
                .roleIds(List.of("MASTER")).build();
    }

    private UsernamePasswordAuthenticationToken authToken(IcasUser user) {
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }

    private MenuVO sampleVO() {
        MenuVO vo = new MenuVO();
        vo.setMenuId("MENU_001");
        vo.setSysSeCd("COM");
        vo.setMenuNm("공통 메뉴");
        vo.setMenuOrd(1);
        return vo;
    }

    private MenuTreeNode sampleTreeNode() {
        MenuTreeNode node = new MenuTreeNode();
        node.setMenuId("MENU_001");
        node.setMenuNm("공통 메뉴");
        node.setMenuOrd(1);
        return node;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/com/menu
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("평면 목록 조회 — 전체 목록 200 반환")
    void list_전체목록_200() throws Exception {
        given(menuService.selectMenus(null)).willReturn(List.of(sampleVO()));

        mockMvc.perform(get("/api/com/menu")
                        .with(authentication(authToken(molitUser)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());

        then(menuService).should().selectMenus(null);
    }

    @Test
    @DisplayName("평면 목록 조회 — sysSeCd 필터 적용 200 반환")
    void list_sysSeCd_필터_200() throws Exception {
        given(menuService.selectMenus("COM")).willReturn(List.of(sampleVO()));

        mockMvc.perform(get("/api/com/menu")
                        .param("sysSeCd", "COM")
                        .with(authentication(authToken(molitUser)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].sysSeCd").value("COM"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/com/menu/tree
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("트리 조회 — master 사용자 모든 메뉴 트리 200 반환")
    void tree_master_200() throws Exception {
        given(menuService.selectMenuTree(eq("COM"), any(IcasUser.class)))
                .willReturn(List.of(sampleTreeNode()));

        mockMvc.perform(get("/api/com/menu/tree")
                        .param("sysSeCd", "COM")
                        .with(authentication(authToken(masterUser)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].menuId").value("MENU_001"));
    }

    @Test
    @DisplayName("트리 조회 — 권한 없는 사용자 빈 트리 200 반환")
    void tree_빈prgrmPaths_빈트리_200() throws Exception {
        given(menuService.selectMenuTree(isNull(), any(IcasUser.class)))
                .willReturn(List.of());

        mockMvc.perform(get("/api/com/menu/tree")
                        .with(authentication(authToken(airlineUser)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/com/menu/{menuId}
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("단건 조회 — 존재하는 ID 200 반환")
    void get_단건조회_200() throws Exception {
        given(menuService.selectMenu("MENU_001")).willReturn(sampleVO());

        mockMvc.perform(get("/api/com/menu/MENU_001")
                        .with(authentication(authToken(molitUser)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.menuId").value("MENU_001"));
    }

    @Test
    @DisplayName("단건 조회 — 존재하지 않는 ID → 404 반환")
    void get_없는ID_404() throws Exception {
        given(menuService.selectMenu("NONE"))
                .willThrow(BusinessException.notFound("메뉴"));

        mockMvc.perform(get("/api/com/menu/NONE")
                        .with(authentication(authToken(molitUser)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/com/menu
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("메뉴 등록 — MOLIT 사용자 정상 등록 200 반환")
    void create_MOLIT_정상등록_200() throws Exception {
        given(menuService.insertMenu(any(MenuVO.class), any(IcasUser.class))).willReturn(sampleVO());

        mockMvc.perform(post("/api/com/menu")
                        .with(authentication(authToken(molitUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleVO())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("등록되었습니다."))
                .andExpect(jsonPath("$.data.menuId").value("MENU_001"));
    }

    @Test
    @DisplayName("메뉴 등록 — Service에서 BAD_REQUEST(사이클) → 400 반환")
    void create_사이클_400() throws Exception {
        given(menuService.insertMenu(any(MenuVO.class), any(IcasUser.class)))
                .willThrow(BusinessException.badRequest("메뉴가 자기 자신을 상위 메뉴로 지정할 수 없습니다."));

        mockMvc.perform(post("/api/com/menu")
                        .with(authentication(authToken(molitUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleVO())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    @Test
    @DisplayName("메뉴 등록 — AIRLINE 사용자 → 403 반환")
    void create_권한없음_403() throws Exception {
        given(menuService.insertMenu(any(MenuVO.class), any(IcasUser.class)))
                .willThrow(BusinessException.forbidden("MOLIT/KOTSA 사용자만 메뉴를 변경할 수 있습니다."));

        mockMvc.perform(post("/api/com/menu")
                        .with(authentication(authToken(airlineUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleVO())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUT /api/com/menu/{menuId}
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("메뉴 수정 — MOLIT 사용자 정상 수정 200 반환")
    void update_MOLIT_정상수정_200() throws Exception {
        willDoNothing().given(menuService).updateMenu(eq("MENU_001"), any(MenuVO.class), any(IcasUser.class));

        mockMvc.perform(put("/api/com/menu/MENU_001")
                        .with(authentication(authToken(molitUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleVO())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("수정되었습니다."));
    }

    @Test
    @DisplayName("메뉴 수정 — 존재하지 않는 ID → 404 반환")
    void update_없는ID_404() throws Exception {
        willThrow(BusinessException.notFound("메뉴"))
                .given(menuService).updateMenu(eq("NONE"), any(MenuVO.class), any(IcasUser.class));

        mockMvc.perform(put("/api/com/menu/NONE")
                        .with(authentication(authToken(molitUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleVO())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE /api/com/menu/{menuId}
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("메뉴 삭제 — MOLIT 사용자 정상 삭제 200 반환")
    void delete_MOLIT_정상삭제_200() throws Exception {
        willDoNothing().given(menuService).softDeleteMenu(eq("MENU_001"), any(IcasUser.class));

        mockMvc.perform(delete("/api/com/menu/MENU_001")
                        .with(authentication(authToken(molitUser))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("삭제되었습니다."));
    }

    @Test
    @DisplayName("메뉴 삭제 — 하위 메뉴 존재 시 → 409 반환")
    void delete_하위존재_409() throws Exception {
        willThrow(BusinessException.conflict("하위 메뉴(2개)가 존재하여 삭제할 수 없습니다. 하위 메뉴를 먼저 삭제하세요."))
                .given(menuService).softDeleteMenu(eq("ROOT_MENU"), any(IcasUser.class));

        mockMvc.perform(delete("/api/com/menu/ROOT_MENU")
                        .with(authentication(authToken(molitUser))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"));
    }
}
