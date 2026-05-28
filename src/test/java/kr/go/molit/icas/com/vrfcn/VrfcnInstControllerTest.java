package kr.go.molit.icas.com.vrfcn;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import kr.go.molit.icas.com.vrfcn.domain.VrfcnInstVO;
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
 * VrfcnInstController 슬라이스 테스트.
 *
 * 권한 분기가 Controller 에 있으므로 인증 컨텍스트 주입 필수.
 * SecurityMockMvcRequestPostProcessors.authentication() 사용.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("VrfcnInstController 슬라이스 테스트")
class VrfcnInstControllerTest {

    MockMvc mockMvc;

    @Mock
    VrfcnInstService vrfcnInstService;

    ObjectMapper objectMapper;

    // ── fixture ──
    IcasUser molitUser;
    IcasUser airlineUser;
    IcasUser verifierUserVI0001;
    IcasUser verifierUserVI0002;

    @BeforeEach
    void setUp() {
        VrfcnInstController controller = new VrfcnInstController(vrfcnInstService);
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

    private VrfcnInstVO sampleVO() {
        VrfcnInstVO vo = new VrfcnInstVO();
        vo.setVrfcnInstId("VI0001");
        vo.setOgnzId("ORG_VRF01");
        vo.setVrfcnInstNm("한국탄소검증원");
        vo.setVrfcnInstNmEn("Korea Carbon Verification Institute");
        vo.setIcaoCcrAccrdYn("Y");
        return vo;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/com/vrfcn/inst
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("목록 조회 — 인증된 사용자 (MOLIT) 전체 목록 200 반환")
    void listAll_MOLIT_200() throws Exception {
        given(vrfcnInstService.selectAll()).willReturn(List.of(sampleVO()));

        mockMvc.perform(get("/api/com/vrfcn/inst")
                        .with(authentication(authToken(molitUser)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/com/vrfcn/inst/{vrfcnInstId}
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("단건 조회 — VERIFIER 는 본인 기관 조회 200 반환")
    void getOne_VERIFIER_본인기관_200() throws Exception {
        given(vrfcnInstService.selectByVrfcnInstId("VI0001")).willReturn(sampleVO());

        mockMvc.perform(get("/api/com/vrfcn/inst/VI0001")
                        .with(authentication(authToken(verifierUserVI0001)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.vrfcnInstId").value("VI0001"));
    }

    @Test
    @DisplayName("단건 조회 — VERIFIER 가 타 기관 ID 조회 시도 시 403 반환")
    void getOne_VERIFIER_타기관_403() throws Exception {
        // verifierUserVI0002 는 VI0002 소속 → VI0001 조회 불가
        mockMvc.perform(get("/api/com/vrfcn/inst/VI0001")
                        .with(authentication(authToken(verifierUserVI0002)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));

        then(vrfcnInstService).should(never()).selectByVrfcnInstId(any());
    }

    @Test
    @DisplayName("단건 조회 — 존재하지 않는 ID 조회 시 Service NOT_FOUND → 404")
    void getOne_없는ID_NOT_FOUND_404() throws Exception {
        given(vrfcnInstService.selectByVrfcnInstId("VI9999"))
                .willThrow(BusinessException.notFound("검증기관"));

        mockMvc.perform(get("/api/com/vrfcn/inst/VI9999")
                        .with(authentication(authToken(molitUser)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/com/vrfcn/inst
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("검증기관 등록 — MOLIT 사용자 정상 등록 200 반환")
    void create_MOLIT_정상등록_200() throws Exception {
        VrfcnInstVO reqVO = sampleVO();
        reqVO.setVrfcnInstId(null);
        given(vrfcnInstService.createVrfcnInst(any(VrfcnInstVO.class), any(IcasUser.class)))
                .willReturn(sampleVO());

        mockMvc.perform(post("/api/com/vrfcn/inst")
                        .with(authentication(authToken(molitUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqVO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("검증기관이 등록되었습니다."))
                .andExpect(jsonPath("$.data.vrfcnInstId").value("VI0001"));
    }

    @Test
    @DisplayName("검증기관 등록 — AIRLINE 사용자 시도 시 403 반환")
    void create_AIRLINE_FORBIDDEN_403() throws Exception {
        VrfcnInstVO reqVO = sampleVO();
        reqVO.setVrfcnInstId(null);

        mockMvc.perform(post("/api/com/vrfcn/inst")
                        .with(authentication(authToken(airlineUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqVO)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));

        then(vrfcnInstService).should(never()).createVrfcnInst(any(), any());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE /api/com/vrfcn/inst/{vrfcnInstId}
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("소프트삭제 — VERIFIER 사용자 시도 시 403 반환")
    void delete_VERIFIER_FORBIDDEN_403() throws Exception {
        mockMvc.perform(delete("/api/com/vrfcn/inst/VI0001")
                        .with(authentication(authToken(verifierUserVI0001))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));

        then(vrfcnInstService).should(never()).softDeleteVrfcnInst(any(), any());
    }

    @Test
    @DisplayName("소프트삭제 — MOLIT 사용자 정상 삭제 200 반환")
    void delete_MOLIT_정상삭제_200() throws Exception {
        willDoNothing().given(vrfcnInstService).softDeleteVrfcnInst(eq("VI0001"), any(IcasUser.class));

        mockMvc.perform(delete("/api/com/vrfcn/inst/VI0001")
                        .with(authentication(authToken(molitUser))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("검증기관이 삭제되었습니다."));
    }
}
