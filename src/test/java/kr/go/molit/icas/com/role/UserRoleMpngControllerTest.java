package kr.go.molit.icas.com.role;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import kr.go.molit.icas.com.role.domain.UserRoleMpngVO;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * UserRoleMpngController 슬라이스 테스트.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserRoleMpngController 슬라이스 테스트")
class UserRoleMpngControllerTest {

    MockMvc mockMvc;

    @Mock
    UserRoleMpngService userRoleMpngService;

    ObjectMapper objectMapper;

    IcasUser molitUser;
    IcasUser airlineUser;

    @BeforeEach
    void setUp() {
        UserRoleMpngController controller = new UserRoleMpngController(userRoleMpngService);
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

    private UserRoleMpngVO sampleMpng() {
        UserRoleMpngVO vo = new UserRoleMpngVO();
        vo.setUserId("user01");
        vo.setRoleId("ROLE_AIRLINE");
        vo.setUseBgngDt(LocalDateTime.now());
        vo.setUseEndDt(LocalDateTime.of(9999, 12, 31, 23, 59, 59));
        vo.setRoleNm("항공사 역할");
        return vo;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/com/user-role/user/{userId}
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("활성 역할 조회 — 정상 200 반환")
    void activeRoles_200() throws Exception {
        given(userRoleMpngService.selectActiveRolesByUser("user01")).willReturn(List.of(sampleMpng()));

        mockMvc.perform(get("/api/com/user-role/user/user01")
                        .with(authentication(authToken(molitUser)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].userId").value("user01"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/com/user-role/user/{userId}/history
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("역할 이력 조회 — 정상 200 반환")
    void roleHistory_200() throws Exception {
        given(userRoleMpngService.selectRoleHistory("user01")).willReturn(List.of(sampleMpng()));

        mockMvc.perform(get("/api/com/user-role/user/user01/history")
                        .with(authentication(authToken(molitUser)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/com/user-role
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("역할 부여 — MOLIT 사용자 정상 부여 200 반환")
    void grantRole_MOLIT_정상부여_200() throws Exception {
        willDoNothing().given(userRoleMpngService)
                .grantRole(anyString(), anyString(), anyString(), any(IcasUser.class));

        Map<String, String> req = Map.of(
                "userId", "user01",
                "roleId", "ROLE_AIRLINE",
                "userOgnzSeCd", "AIRLINE"
        );

        mockMvc.perform(post("/api/com/user-role")
                        .with(authentication(authToken(molitUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("역할이 부여되었습니다."));
    }

    @Test
    @DisplayName("역할 부여 — 호환 안되는 역할(AIRLINE 사용자에 MOLIT 전용 역할) → Service FORBIDDEN → 403")
    void grantRole_호환안됨_FORBIDDEN_403() throws Exception {
        willThrow(BusinessException.forbidden("해당 사용자의 조직구분(AIRLINE)은 역할의 허용 범위에 포함되지 않습니다."))
                .given(userRoleMpngService).grantRole(anyString(), anyString(), anyString(), any(IcasUser.class));

        Map<String, String> req = Map.of(
                "userId", "user01",
                "roleId", "ROLE_MOLIT",
                "userOgnzSeCd", "AIRLINE"
        );

        mockMvc.perform(post("/api/com/user-role")
                        .with(authentication(authToken(molitUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    @DisplayName("역할 부여 — 이미 유효한 매핑 존재 시 Service CONFLICT → 409")
    void grantRole_이미부여_CONFLICT_409() throws Exception {
        willThrow(BusinessException.conflict("이미 활성화된 역할이 존재합니다."))
                .given(userRoleMpngService).grantRole(anyString(), anyString(), anyString(), any(IcasUser.class));

        Map<String, String> req = Map.of(
                "userId", "user01",
                "roleId", "ROLE_AIRLINE",
                "userOgnzSeCd", "AIRLINE"
        );

        mockMvc.perform(post("/api/com/user-role")
                        .with(authentication(authToken(molitUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE /api/com/user-role
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("역할 회수 — MOLIT 사용자 정상 회수 200 반환 (use_end_dt 갱신 verify)")
    void revokeRole_MOLIT_정상회수_200() throws Exception {
        willDoNothing().given(userRoleMpngService)
                .revokeRole(eq("user01"), eq("ROLE_AIRLINE"), any(IcasUser.class));

        mockMvc.perform(delete("/api/com/user-role")
                        .with(authentication(authToken(molitUser)))
                        .param("userId", "user01")
                        .param("roleId", "ROLE_AIRLINE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("역할이 회수되었습니다."));

        then(userRoleMpngService).should().revokeRole(eq("user01"), eq("ROLE_AIRLINE"), any(IcasUser.class));
    }

    @Test
    @DisplayName("역할 회수 — 활성 매핑 미존재 시 Service NOT_FOUND → 404")
    void revokeRole_매핑없음_NOT_FOUND_404() throws Exception {
        willThrow(BusinessException.notFound("사용자의 활성 역할 매핑"))
                .given(userRoleMpngService).revokeRole(anyString(), anyString(), any(IcasUser.class));

        mockMvc.perform(delete("/api/com/user-role")
                        .with(authentication(authToken(molitUser)))
                        .param("userId", "user01")
                        .param("roleId", "NO_ROLE"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }
}
