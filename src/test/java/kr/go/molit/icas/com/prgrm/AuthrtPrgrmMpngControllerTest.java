package kr.go.molit.icas.com.prgrm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import kr.go.molit.icas.com.prgrm.domain.AuthrtPrgrmMpngVO;
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
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AuthrtPrgrmMpngController 슬라이스 테스트.
 * Spring Boot 없이 순수 MockMvcBuilders.standaloneSetup 사용.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthrtPrgrmMpngController 슬라이스 테스트")
class AuthrtPrgrmMpngControllerTest {

    MockMvc mockMvc;

    @Mock
    AuthrtPrgrmMpngService authrtPrgrmMpngService;

    ObjectMapper objectMapper;

    // ── fixture ──
    IcasUser molitUser;
    IcasUser airlineUser;

    @BeforeEach
    void setUp() {
        AuthrtPrgrmMpngController controller = new AuthrtPrgrmMpngController(authrtPrgrmMpngService);
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
                .prgrmPathsInq(Set.of("/api/com/authrt-prgrm"))
                .prgrmPathsInpt(Set.of("/api/com/authrt-prgrm"))
                .roleIds(List.of("ADMIN")).build();

        airlineUser = IcasUser.builder()
                .userId("airline01").userNm("항공사 담당자")
                .ognzSeCd("AIRLINE").ognzId("ORG_AIR01").oprtrId("OP0001").master(false)
                .prgrmPathsInq(Set.of())
                .prgrmPathsInpt(Set.of())
                .roleIds(List.of("AIRLINE_USER")).build();
    }

    private UsernamePasswordAuthenticationToken authToken(IcasUser user) {
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }

    private AuthrtPrgrmMpngVO sampleMpngVO() {
        AuthrtPrgrmMpngVO vo = new AuthrtPrgrmMpngVO();
        vo.setAuthrtId("ROLE_ADMIN");
        vo.setPrgrmId("PG001");
        vo.setInqAuthrtYn("Y");
        vo.setInptAuthrtYn("N");
        vo.setPrgrmNm("공통 프로그램");
        vo.setSysSeCd("COM");
        return vo;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/com/authrt-prgrm/authrt/{authrtId}
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("권한별 프로그램 목록 조회 — 정상 200 반환")
    void listByAuthrt_정상_200() throws Exception {
        given(authrtPrgrmMpngService.selectByAuthrt("ROLE_ADMIN"))
                .willReturn(List.of(sampleMpngVO()));

        mockMvc.perform(get("/api/com/authrt-prgrm/authrt/ROLE_ADMIN")
                        .with(authentication(authToken(molitUser)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].authrtId").value("ROLE_ADMIN"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/com/authrt-prgrm/prgrm/{prgrmId}
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("프로그램별 권한 목록 조회 — 정상 200 반환")
    void listByPrgrm_정상_200() throws Exception {
        given(authrtPrgrmMpngService.selectByPrgrm("PG001"))
                .willReturn(List.of(sampleMpngVO()));

        mockMvc.perform(get("/api/com/authrt-prgrm/prgrm/PG001")
                        .with(authentication(authToken(molitUser)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].prgrmId").value("PG001"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/com/authrt-prgrm
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("매핑 upsert — MOLIT 사용자 inqYn=Y, inptYn=N 정상 저장 200 반환")
    void setAuthority_정상저장_200() throws Exception {
        Map<String, String> body = Map.of(
                "authrtId", "ROLE_ADMIN",
                "prgrmId", "PG001",
                "inqAuthrtYn", "Y",
                "inptAuthrtYn", "N"
        );
        willDoNothing().given(authrtPrgrmMpngService)
                .setAuthority(eq("ROLE_ADMIN"), eq("PG001"), eq("Y"), eq("N"), any(IcasUser.class));

        mockMvc.perform(post("/api/com/authrt-prgrm")
                        .with(authentication(authToken(molitUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("권한 매핑이 저장되었습니다."));
    }

    @Test
    @DisplayName("매핑 upsert — Service에서 BAD_REQUEST(유효하지 않은 YN값) → 400 반환")
    void setAuthority_잘못된YN_400() throws Exception {
        Map<String, String> body = Map.of(
                "authrtId", "ROLE_ADMIN",
                "prgrmId", "PG001",
                "inqAuthrtYn", "X",
                "inptAuthrtYn", "N"
        );
        willThrow(BusinessException.badRequest("조회 권한 여부는 Y 또는 N 이어야 합니다."))
                .given(authrtPrgrmMpngService)
                .setAuthority(any(), any(), any(), any(), any(IcasUser.class));

        mockMvc.perform(post("/api/com/authrt-prgrm")
                        .with(authentication(authToken(molitUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    @Test
    @DisplayName("매핑 upsert — Service에서 FORBIDDEN(권한 없는 사용자) → 403 반환")
    void setAuthority_권한없음_403() throws Exception {
        Map<String, String> body = Map.of(
                "authrtId", "ROLE_ADMIN",
                "prgrmId", "PG001",
                "inqAuthrtYn", "Y",
                "inptAuthrtYn", "N"
        );
        willThrow(BusinessException.forbidden("MOLIT/KOTSA 사용자만 권한 매핑을 변경할 수 있습니다."))
                .given(authrtPrgrmMpngService)
                .setAuthority(any(), any(), any(), any(), any(IcasUser.class));

        mockMvc.perform(post("/api/com/authrt-prgrm")
                        .with(authentication(authToken(airlineUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE /api/com/authrt-prgrm
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("매핑 삭제 — MOLIT 사용자 정상 삭제 200 반환")
    void removeMapping_정상삭제_200() throws Exception {
        willDoNothing().given(authrtPrgrmMpngService)
                .removeMapping(eq("ROLE_ADMIN"), eq("PG001"), any(IcasUser.class));

        mockMvc.perform(delete("/api/com/authrt-prgrm")
                        .param("authrtId", "ROLE_ADMIN")
                        .param("prgrmId", "PG001")
                        .with(authentication(authToken(molitUser))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("삭제되었습니다."));
    }

    @Test
    @DisplayName("매핑 삭제 — 존재하지 않는 매핑 → 404 반환")
    void removeMapping_없는매핑_404() throws Exception {
        willThrow(BusinessException.notFound("권한-프로그램 매핑"))
                .given(authrtPrgrmMpngService)
                .removeMapping(eq("ROLE_ADMIN"), eq("PG999"), any(IcasUser.class));

        mockMvc.perform(delete("/api/com/authrt-prgrm")
                        .param("authrtId", "ROLE_ADMIN")
                        .param("prgrmId", "PG999")
                        .with(authentication(authToken(molitUser))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }
}
