package kr.go.molit.icas.com.role;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import kr.go.molit.icas.com.role.domain.RoleVO;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * RoleController 슬라이스 테스트.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RoleController 슬라이스 테스트")
class RoleControllerTest {

    MockMvc mockMvc;

    @Mock
    RoleService roleService;

    ObjectMapper objectMapper;

    IcasUser molitUser;
    IcasUser airlineUser;

    @BeforeEach
    void setUp() {
        RoleController controller = new RoleController(roleService);
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
                .roleIds(List.of("ADMIN")).build();

        airlineUser = IcasUser.builder()
                .userId("airline01").userNm("항공사 담당자")
                .ognzSeCd("AIRLINE").ognzId("ORG_AIR01").oprtrId("OP0001").master(false)
                .roleIds(List.of("AIRLINE_USER")).build();
    }

    private UsernamePasswordAuthenticationToken authToken(IcasUser user) {
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }

    private RoleVO sampleVO() {
        RoleVO vo = new RoleVO();
        vo.setRoleId("ROLE_TEST");
        vo.setRoleNm("테스트 역할");
        vo.setOgnzSeCdAllowed("MOLIT,KOTSA");
        return vo;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/com/role
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("역할 목록 조회 — 인증된 사용자 200 반환")
    void listRoles_200() throws Exception {
        given(roleService.listRoles()).willReturn(List.of(sampleVO()));

        mockMvc.perform(get("/api/com/role")
                        .with(authentication(authToken(molitUser)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/com/role/{roleId}
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("단건 조회 — 존재하는 roleId 200 반환")
    void getRole_존재하는ID_200() throws Exception {
        given(roleService.getRole("ROLE_TEST")).willReturn(sampleVO());

        mockMvc.perform(get("/api/com/role/ROLE_TEST")
                        .with(authentication(authToken(molitUser)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roleId").value("ROLE_TEST"));
    }

    @Test
    @DisplayName("단건 조회 — 존재하지 않는 roleId 시 Service NOT_FOUND → 404")
    void getRole_없는ID_NOT_FOUND_404() throws Exception {
        given(roleService.getRole("NO_ROLE")).willThrow(BusinessException.notFound("역할"));

        mockMvc.perform(get("/api/com/role/NO_ROLE")
                        .with(authentication(authToken(molitUser)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/com/role
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("역할 등록 — MOLIT 사용자 정상 등록 200 반환")
    void createRole_MOLIT_정상등록_200() throws Exception {
        RoleVO reqVO = sampleVO();
        given(roleService.createRole(any(RoleVO.class), any(IcasUser.class))).willReturn(sampleVO());

        mockMvc.perform(post("/api/com/role")
                        .with(authentication(authToken(molitUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqVO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("역할이 등록되었습니다."))
                .andExpect(jsonPath("$.data.roleId").value("ROLE_TEST"));
    }

    @Test
    @DisplayName("역할 등록 — 잘못된 ognzSeCdAllowed(MOLIT,XYZ) → Service BAD_REQUEST → 400")
    void createRole_잘못된토큰_BAD_REQUEST_400() throws Exception {
        RoleVO reqVO = sampleVO();
        reqVO.setOgnzSeCdAllowed("MOLIT,XYZ");
        given(roleService.createRole(any(RoleVO.class), any(IcasUser.class)))
                .willThrow(BusinessException.badRequest("허용되지 않는 조직구분코드입니다: XYZ"));

        mockMvc.perform(post("/api/com/role")
                        .with(authentication(authToken(molitUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqVO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    @Test
    @DisplayName("역할 등록 — 중복 roleId → Service CONFLICT → 409")
    void createRole_중복ID_CONFLICT_409() throws Exception {
        RoleVO reqVO = sampleVO();
        given(roleService.createRole(any(RoleVO.class), any(IcasUser.class)))
                .willThrow(BusinessException.conflict("이미 존재하는 역할 ID입니다: ROLE_TEST"));

        mockMvc.perform(post("/api/com/role")
                        .with(authentication(authToken(molitUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqVO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"));
    }

    @Test
    @DisplayName("역할 등록 — AIRLINE 사용자 시도 시 Service FORBIDDEN → 403")
    void createRole_AIRLINE_FORBIDDEN_403() throws Exception {
        RoleVO reqVO = sampleVO();
        given(roleService.createRole(any(RoleVO.class), any(IcasUser.class)))
                .willThrow(BusinessException.forbidden("역할 관리는 MOLIT/KOTSA 사용자만 가능합니다."));

        mockMvc.perform(post("/api/com/role")
                        .with(authentication(authToken(airlineUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqVO)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUT /api/com/role/{roleId}
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("역할 수정 — 존재하지 않는 roleId 시 Service NOT_FOUND → 404")
    void updateRole_없는ID_NOT_FOUND_404() throws Exception {
        RoleVO reqVO = sampleVO();
        willThrow(BusinessException.notFound("역할"))
                .given(roleService).updateRole(eq("NO_ROLE"), any(RoleVO.class), any(IcasUser.class));

        mockMvc.perform(put("/api/com/role/NO_ROLE")
                        .with(authentication(authToken(molitUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqVO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE /api/com/role/{roleId}
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("역할 소프트 삭제 — MOLIT 사용자 정상 삭제 200 반환")
    void deleteRole_MOLIT_정상삭제_200() throws Exception {
        willDoNothing().given(roleService).softDeleteRole(eq("ROLE_TEST"), any(IcasUser.class));

        mockMvc.perform(delete("/api/com/role/ROLE_TEST")
                        .with(authentication(authToken(molitUser))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("역할이 삭제되었습니다."));
    }

    @Test
    @DisplayName("역할 소프트 삭제 — 존재하지 않는 roleId 시 Service NOT_FOUND → 404")
    void deleteRole_없는ID_NOT_FOUND_404() throws Exception {
        willThrow(BusinessException.notFound("역할"))
                .given(roleService).softDeleteRole(eq("NO_ROLE"), any(IcasUser.class));

        mockMvc.perform(delete("/api/com/role/NO_ROLE")
                        .with(authentication(authToken(molitUser))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }
}
