package kr.go.molit.icas.com.vrfcn;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * VrfcnAssgnController 슬라이스 테스트.
 *
 * 권한 분기가 Controller 에 있으므로 인증 컨텍스트 주입 필수.
 * - AIRLINE: 배정 목록/배정 정보 조회 불가 (FORBIDDEN)
 * - VERIFIER: 본인 기관 배정만 조회 가능
 * - MOLIT/KOTSA: 배정 등록·삭제 전용
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("VrfcnAssgnController 슬라이스 테스트")
class VrfcnAssgnControllerTest {

    MockMvc mockMvc;

    @Mock
    VrfcnAssgnService vrfcnAssgnService;

    ObjectMapper objectMapper;

    // ── fixture ──
    IcasUser molitUser;
    IcasUser airlineUser;
    IcasUser verifierUserVI0001;
    IcasUser verifierUserVI0002;  // 다른 기관 소속

    @BeforeEach
    void setUp() {
        VrfcnAssgnController controller = new VrfcnAssgnController(vrfcnAssgnService);
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
                .userId("airline01").userNm("항공사A 담당자")
                .ognzSeCd("AIRLINE").ognzId("ORG_AIR01").oprtrId("OP0001").master(false)
                .roleIds(List.of("AIRLINE_USER")).build();

        verifierUserVI0001 = IcasUser.builder()
                .userId("verifier01").userNm("검증기관A 담당자")
                .ognzSeCd("VERIFIER").ognzId("ORG_VRF01").vrfcnInstId("VI0001").master(false)
                .roleIds(List.of("VERIFIER_USER")).build();

        verifierUserVI0002 = IcasUser.builder()
                .userId("verifier02").userNm("검증기관B 담당자")
                .ognzSeCd("VERIFIER").ognzId("ORG_VRF02").vrfcnInstId("VI0002").master(false)
                .roleIds(List.of("VERIFIER_USER")).build();
    }

    private UsernamePasswordAuthenticationToken authToken(IcasUser user) {
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/com/vrfcn/assgn
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("전체 배정 목록 조회 — MOLIT 사용자 전체 목록 200 반환")
    void listAll_MOLIT_전체목록_200() throws Exception {
        List<Map<String, Object>> rows = List.of(
                Map.of("vrfcnInstId", "VI0001", "oprtrId", "OP0001", "rprtYr", "2025")
        );
        given(vrfcnAssgnService.selectAssgnList("2025")).willReturn(rows);

        mockMvc.perform(get("/api/com/vrfcn/assgn")
                        .param("rprtYr", "2025")
                        .with(authentication(authToken(molitUser)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("전체 배정 목록 조회 — AIRLINE 사용자는 403 반환")
    void listAll_AIRLINE_FORBIDDEN_403() throws Exception {
        mockMvc.perform(get("/api/com/vrfcn/assgn")
                        .with(authentication(authToken(airlineUser)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));

        then(vrfcnAssgnService).should(never()).selectAssgnList(any());
    }

    @Test
    @DisplayName("전체 배정 목록 조회 — VERIFIER 는 본인 기관 배정만 필터링하여 200 반환")
    void listAll_VERIFIER_본인기관_필터_200() throws Exception {
        List<String> oprtrIds = List.of("OP0001");
        List<Map<String, Object>> allRows = List.of(
                Map.of("vrfcnInstId", "VI0001", "oprtrId", "OP0001"),
                Map.of("vrfcnInstId", "VI0002", "oprtrId", "OP0002")
        );
        given(vrfcnAssgnService.selectAssignedOprtrIds(eq("VI0001"), isNull()))
                .willReturn(oprtrIds);
        given(vrfcnAssgnService.selectAssgnList(isNull())).willReturn(allRows);

        mockMvc.perform(get("/api/com/vrfcn/assgn")
                        .with(authentication(authToken(verifierUserVI0001)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/com/vrfcn/assgn/inst/{vrfcnInstId}
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("특정 검증기관 배정 조회 — VERIFIER 본인 기관 조회 200 반환")
    void listByInst_VERIFIER_본인기관_200() throws Exception {
        given(vrfcnAssgnService.selectAssignedOprtrIds(eq("VI0001"), eq("2025")))
                .willReturn(List.of("OP0001"));

        mockMvc.perform(get("/api/com/vrfcn/assgn/inst/VI0001")
                        .param("rprtYr", "2025")
                        .with(authentication(authToken(verifierUserVI0001)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0]").value("OP0001"));
    }

    @Test
    @DisplayName("특정 검증기관 배정 조회 — VERIFIER 가 타 기관 조회 시도 시 403 반환")
    void listByInst_VERIFIER_타기관_403() throws Exception {
        // verifierUserVI0002 가 VI0001 조회 시도
        mockMvc.perform(get("/api/com/vrfcn/assgn/inst/VI0001")
                        .param("rprtYr", "2025")
                        .with(authentication(authToken(verifierUserVI0002)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));

        then(vrfcnAssgnService).should(never()).selectAssignedOprtrIds(any(), any());
    }

    @Test
    @DisplayName("특정 검증기관 배정 조회 — AIRLINE 사용자는 403 반환")
    void listByInst_AIRLINE_FORBIDDEN_403() throws Exception {
        mockMvc.perform(get("/api/com/vrfcn/assgn/inst/VI0001")
                        .param("rprtYr", "2025")
                        .with(authentication(authToken(airlineUser)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/com/vrfcn/assgn
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("배정 등록 — MOLIT 사용자 정상 등록 200 반환")
    void create_MOLIT_정상등록_200() throws Exception {
        Map<String, String> body = new HashMap<>();
        body.put("vrfcnInstId", "VI0001");
        body.put("oprtrId", "OP0001");
        body.put("rprtYr", "2025");

        willDoNothing().given(vrfcnAssgnService)
                .createAssgn(eq("VI0001"), eq("OP0001"), eq("2025"), any(IcasUser.class));

        mockMvc.perform(post("/api/com/vrfcn/assgn")
                        .with(authentication(authToken(molitUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("배정이 등록되었습니다."));
    }

    @Test
    @DisplayName("배정 등록 — VERIFIER 사용자 시도 시 403 반환")
    void create_VERIFIER_FORBIDDEN_403() throws Exception {
        Map<String, String> body = new HashMap<>();
        body.put("vrfcnInstId", "VI0001");
        body.put("oprtrId", "OP0001");
        body.put("rprtYr", "2025");

        mockMvc.perform(post("/api/com/vrfcn/assgn")
                        .with(authentication(authToken(verifierUserVI0001)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));

        then(vrfcnAssgnService).should(never()).createAssgn(any(), any(), any(), any());
    }

    @Test
    @DisplayName("배정 등록 — Service 에서 중복 배정 CONFLICT → 409 반환")
    void create_중복배정_CONFLICT_409() throws Exception {
        Map<String, String> body = new HashMap<>();
        body.put("vrfcnInstId", "VI0001");
        body.put("oprtrId", "OP0001");
        body.put("rprtYr", "2025");

        willThrow(BusinessException.conflict("해당 검증기관과 항공사의 배정이 이미 존재합니다. (보고연도: 2025)"))
                .given(vrfcnAssgnService)
                .createAssgn(eq("VI0001"), eq("OP0001"), eq("2025"), any(IcasUser.class));

        mockMvc.perform(post("/api/com/vrfcn/assgn")
                        .with(authentication(authToken(molitUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE /api/com/vrfcn/assgn
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("배정 삭제 — MOLIT 사용자 정상 삭제 200 반환")
    void delete_MOLIT_정상삭제_200() throws Exception {
        willDoNothing().given(vrfcnAssgnService)
                .softDeleteAssgn(eq("VI0001"), eq("OP0001"), eq("2025"), any(IcasUser.class));

        mockMvc.perform(delete("/api/com/vrfcn/assgn")
                        .param("vrfcnInstId", "VI0001")
                        .param("oprtrId", "OP0001")
                        .param("rprtYr", "2025")
                        .with(authentication(authToken(molitUser))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("배정이 삭제되었습니다."));
    }

    @Test
    @DisplayName("배정 삭제 — AIRLINE 사용자 시도 시 403 반환")
    void delete_AIRLINE_FORBIDDEN_403() throws Exception {
        mockMvc.perform(delete("/api/com/vrfcn/assgn")
                        .param("vrfcnInstId", "VI0001")
                        .param("oprtrId", "OP0001")
                        .param("rprtYr", "2025")
                        .with(authentication(authToken(airlineUser))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));

        then(vrfcnAssgnService).should(never()).softDeleteAssgn(any(), any(), any(), any());
    }
}
