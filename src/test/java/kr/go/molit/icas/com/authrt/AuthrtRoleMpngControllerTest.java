package kr.go.molit.icas.com.authrt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import kr.go.molit.icas.com.authrt.domain.AuthrtRoleMpngVO;
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
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AuthrtRoleMpngController 슬라이스 테스트.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthrtRoleMpngController 슬라이스 테스트")
class AuthrtRoleMpngControllerTest {

    MockMvc mockMvc;

    @Mock
    AuthrtRoleMpngService authrtRoleMpngService;

    ObjectMapper objectMapper;

    IcasUser molitUser;
    IcasUser airlineUser;

    @BeforeEach
    void setUp() {
        AuthrtRoleMpngController controller = new AuthrtRoleMpngController(authrtRoleMpngService);
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

    private AuthrtRoleMpngVO sampleMpng() {
        AuthrtRoleMpngVO vo = new AuthrtRoleMpngVO();
        vo.setAuthrtId("AUTHRT_TEST");
        vo.setRoleId("ROLE_TEST");
        vo.setAuthrtNm("테스트 권한");
        vo.setRoleNm("테스트 역할");
        return vo;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/com/authrt-role/role/{roleId}
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("역할 기준 권한 목록 조회 — 정상 200 반환")
    void byRole_200() throws Exception {
        given(authrtRoleMpngService.selectByRole("ROLE_TEST")).willReturn(List.of(sampleMpng()));

        mockMvc.perform(get("/api/com/authrt-role/role/ROLE_TEST")
                        .with(authentication(authToken(molitUser)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].roleId").value("ROLE_TEST"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/com/authrt-role/authrt/{authrtId}
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("권한 기준 역할 목록 조회 — 정상 200 반환")
    void byAuthrt_200() throws Exception {
        given(authrtRoleMpngService.selectByAuthrt("AUTHRT_TEST")).willReturn(List.of(sampleMpng()));

        mockMvc.perform(get("/api/com/authrt-role/authrt/AUTHRT_TEST")
                        .with(authentication(authToken(molitUser)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].authrtId").value("AUTHRT_TEST"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/com/authrt-role
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("권한-역할 매핑 추가 — MOLIT 사용자 정상 추가 200 반환")
    void addMapping_MOLIT_정상추가_200() throws Exception {
        willDoNothing().given(authrtRoleMpngService)
                .addMapping(anyString(), anyString(), any(IcasUser.class));

        Map<String, String> req = Map.of("authrtId", "AUTHRT_TEST", "roleId", "ROLE_TEST");

        mockMvc.perform(post("/api/com/authrt-role")
                        .with(authentication(authToken(molitUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("권한-역할 매핑이 추가되었습니다."));
    }

    @Test
    @DisplayName("권한-역할 매핑 추가 — 중복 매핑 → Service CONFLICT → 409")
    void addMapping_중복매핑_CONFLICT_409() throws Exception {
        willThrow(BusinessException.conflict("권한과 역할의 매핑이 이미 존재합니다."))
                .given(authrtRoleMpngService).addMapping(anyString(), anyString(), any(IcasUser.class));

        Map<String, String> req = Map.of("authrtId", "AUTHRT_TEST", "roleId", "ROLE_TEST");

        mockMvc.perform(post("/api/com/authrt-role")
                        .with(authentication(authToken(molitUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"));
    }

    @Test
    @DisplayName("권한-역할 매핑 추가 — 권한 없는 사용자(AIRLINE) → Service FORBIDDEN → 403")
    void addMapping_AIRLINE_FORBIDDEN_403() throws Exception {
        willThrow(BusinessException.forbidden("권한-역할 매핑 관리는 MOLIT/KOTSA 사용자만 가능합니다."))
                .given(authrtRoleMpngService).addMapping(anyString(), anyString(), any(IcasUser.class));

        Map<String, String> req = Map.of("authrtId", "AUTHRT_TEST", "roleId", "ROLE_TEST");

        mockMvc.perform(post("/api/com/authrt-role")
                        .with(authentication(authToken(airlineUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE /api/com/authrt-role
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("권한-역할 매핑 소프트 삭제 — MOLIT 사용자 정상 삭제 200 반환")
    void removeMapping_MOLIT_정상삭제_200() throws Exception {
        willDoNothing().given(authrtRoleMpngService)
                .removeMapping(eq("AUTHRT_TEST"), eq("ROLE_TEST"), any(IcasUser.class));

        mockMvc.perform(delete("/api/com/authrt-role")
                        .with(authentication(authToken(molitUser)))
                        .param("authrtId", "AUTHRT_TEST")
                        .param("roleId", "ROLE_TEST"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("권한-역할 매핑이 해제되었습니다."));
    }

    @Test
    @DisplayName("권한-역할 매핑 소프트 삭제 — 미존재 매핑 삭제 시 Service NOT_FOUND → 404")
    void removeMapping_미존재_NOT_FOUND_404() throws Exception {
        willThrow(BusinessException.notFound("권한과 역할의 활성 매핑"))
                .given(authrtRoleMpngService).removeMapping(anyString(), anyString(), any(IcasUser.class));

        mockMvc.perform(delete("/api/com/authrt-role")
                        .with(authentication(authToken(molitUser)))
                        .param("authrtId", "AUTHRT_TEST")
                        .param("roleId", "NO_ROLE"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }
}
