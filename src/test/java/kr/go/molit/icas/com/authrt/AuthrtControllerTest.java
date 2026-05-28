package kr.go.molit.icas.com.authrt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import kr.go.molit.icas.com.authrt.domain.AuthrtVO;
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
 * AuthrtController 슬라이스 테스트.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthrtController 슬라이스 테스트")
class AuthrtControllerTest {

    MockMvc mockMvc;

    @Mock
    AuthrtService authrtService;

    ObjectMapper objectMapper;

    IcasUser molitUser;
    IcasUser airlineUser;

    @BeforeEach
    void setUp() {
        AuthrtController controller = new AuthrtController(authrtService);
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

    private AuthrtVO sampleVO() {
        AuthrtVO vo = new AuthrtVO();
        vo.setAuthrtId("AUTHRT_TEST");
        vo.setAuthrtNm("테스트 권한");
        vo.setAuthrtDesc("테스트 용도");
        return vo;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/com/authrt
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("권한 목록 조회 — 인증된 사용자 200 반환")
    void listAuthrts_200() throws Exception {
        given(authrtService.listAuthrts()).willReturn(List.of(sampleVO()));

        mockMvc.perform(get("/api/com/authrt")
                        .with(authentication(authToken(molitUser)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/com/authrt/{authrtId}
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("단건 조회 — 존재하는 authrtId 200 반환")
    void getAuthrt_존재하는ID_200() throws Exception {
        given(authrtService.getAuthrt("AUTHRT_TEST")).willReturn(sampleVO());

        mockMvc.perform(get("/api/com/authrt/AUTHRT_TEST")
                        .with(authentication(authToken(molitUser)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.authrtId").value("AUTHRT_TEST"));
    }

    @Test
    @DisplayName("단건 조회 — 존재하지 않는 ID 시 Service NOT_FOUND → 404")
    void getAuthrt_없는ID_NOT_FOUND_404() throws Exception {
        given(authrtService.getAuthrt("NO_AUTHRT")).willThrow(BusinessException.notFound("권한"));

        mockMvc.perform(get("/api/com/authrt/NO_AUTHRT")
                        .with(authentication(authToken(molitUser)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/com/authrt
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("권한 등록 — MOLIT 사용자 정상 등록 200 반환")
    void createAuthrt_MOLIT_정상등록_200() throws Exception {
        AuthrtVO reqVO = sampleVO();
        given(authrtService.createAuthrt(any(AuthrtVO.class), any(IcasUser.class))).willReturn(sampleVO());

        mockMvc.perform(post("/api/com/authrt")
                        .with(authentication(authToken(molitUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqVO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("권한이 등록되었습니다."))
                .andExpect(jsonPath("$.data.authrtId").value("AUTHRT_TEST"));
    }

    @Test
    @DisplayName("권한 등록 — 중복 authrtId → Service CONFLICT → 409")
    void createAuthrt_중복ID_CONFLICT_409() throws Exception {
        AuthrtVO reqVO = sampleVO();
        given(authrtService.createAuthrt(any(AuthrtVO.class), any(IcasUser.class)))
                .willThrow(BusinessException.conflict("이미 존재하는 권한 ID입니다: AUTHRT_TEST"));

        mockMvc.perform(post("/api/com/authrt")
                        .with(authentication(authToken(molitUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqVO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"));
    }

    @Test
    @DisplayName("권한 등록 — AIRLINE 사용자 시도 시 Service FORBIDDEN → 403")
    void createAuthrt_AIRLINE_FORBIDDEN_403() throws Exception {
        AuthrtVO reqVO = sampleVO();
        given(authrtService.createAuthrt(any(AuthrtVO.class), any(IcasUser.class)))
                .willThrow(BusinessException.forbidden("권한 관리는 MOLIT/KOTSA 사용자만 가능합니다."));

        mockMvc.perform(post("/api/com/authrt")
                        .with(authentication(authToken(airlineUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqVO)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUT /api/com/authrt/{authrtId}
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("권한 수정 — MOLIT 사용자 정상 수정 200 반환")
    void updateAuthrt_MOLIT_정상수정_200() throws Exception {
        AuthrtVO reqVO = sampleVO();
        willDoNothing().given(authrtService).updateAuthrt(eq("AUTHRT_TEST"), any(AuthrtVO.class), any(IcasUser.class));

        mockMvc.perform(put("/api/com/authrt/AUTHRT_TEST")
                        .with(authentication(authToken(molitUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqVO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("권한이 수정되었습니다."));
    }

    @Test
    @DisplayName("권한 수정 — 존재하지 않는 ID 시 Service NOT_FOUND → 404")
    void updateAuthrt_없는ID_NOT_FOUND_404() throws Exception {
        AuthrtVO reqVO = sampleVO();
        willThrow(BusinessException.notFound("권한"))
                .given(authrtService).updateAuthrt(eq("NO_AUTHRT"), any(AuthrtVO.class), any(IcasUser.class));

        mockMvc.perform(put("/api/com/authrt/NO_AUTHRT")
                        .with(authentication(authToken(molitUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqVO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE /api/com/authrt/{authrtId}
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("권한 소프트 삭제 — MOLIT 사용자 정상 삭제 200 반환")
    void deleteAuthrt_MOLIT_정상삭제_200() throws Exception {
        willDoNothing().given(authrtService).softDeleteAuthrt(eq("AUTHRT_TEST"), any(IcasUser.class));

        mockMvc.perform(delete("/api/com/authrt/AUTHRT_TEST")
                        .with(authentication(authToken(molitUser))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("권한이 삭제되었습니다."));
    }

    @Test
    @DisplayName("권한 소프트 삭제 — 존재하지 않는 ID 시 Service NOT_FOUND → 404")
    void deleteAuthrt_없는ID_NOT_FOUND_404() throws Exception {
        willThrow(BusinessException.notFound("권한"))
                .given(authrtService).softDeleteAuthrt(eq("NO_AUTHRT"), any(IcasUser.class));

        mockMvc.perform(delete("/api/com/authrt/NO_AUTHRT")
                        .with(authentication(authToken(molitUser))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }
}
