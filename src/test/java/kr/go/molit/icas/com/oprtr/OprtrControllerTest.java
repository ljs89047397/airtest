package kr.go.molit.icas.com.oprtr;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import kr.go.molit.icas.com.oprtr.domain.OprtrVO;
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
 * OprtrController 슬라이스 테스트.
 *
 * Spring Boot 없이 순수 MockMvcBuilders.standaloneSetup 사용.
 * SecurityMockMvcRequestPostProcessors.authentication() 으로 IcasUser 컨텍스트를 주입한다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OprtrController 슬라이스 테스트")
class OprtrControllerTest {

    MockMvc mockMvc;

    @Mock
    OprtrService oprtrService;

    ObjectMapper objectMapper;

    // ── fixture ──
    IcasUser molitUser;
    IcasUser airlineUserOP0001;
    IcasUser airlineUserOP0002;
    IcasUser verifierUser;

    @BeforeEach
    void setUp() {
        OprtrController controller = new OprtrController(oprtrService);
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

        airlineUserOP0001 = IcasUser.builder()
                .userId("airline01").userNm("항공사A 담당자")
                .ognzSeCd("AIRLINE").ognzId("ORG_AIR01").oprtrId("OP0001").master(false)
                .roleIds(List.of("AIRLINE_USER")).build();

        airlineUserOP0002 = IcasUser.builder()
                .userId("airline02").userNm("항공사B 담당자")
                .ognzSeCd("AIRLINE").ognzId("ORG_AIR02").oprtrId("OP0002").master(false)
                .roleIds(List.of("AIRLINE_USER")).build();

        verifierUser = IcasUser.builder()
                .userId("verifier01").userNm("검증기관 담당자")
                .ognzSeCd("VERIFIER").ognzId("ORG_VRF01").vrfcnInstId("VI0001").master(false)
                .roleIds(List.of("VERIFIER_USER")).build();
    }

    private UsernamePasswordAuthenticationToken authToken(IcasUser user) {
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }

    private OprtrVO sampleVO() {
        OprtrVO vo = new OprtrVO();
        vo.setOprtrId("OP0001");
        vo.setOprtrNm("대한항공");
        vo.setOprtrNmEn("Korean Air");
        vo.setIcaoDesig("KAL");
        vo.setOgnzId("ORG_AIR01");
        return vo;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/com/oprtr
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("목록 조회 — MOLIT 사용자는 전체 목록 200 반환")
    void list_MOLIT_전체목록_200() throws Exception {
        given(oprtrService.selectAll()).willReturn(List.of(sampleVO()));

        mockMvc.perform(get("/api/com/oprtr")
                        .with(authentication(authToken(molitUser)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());

        then(oprtrService).should().selectAll();
    }

    @Test
    @DisplayName("목록 조회 — AIRLINE 사용자는 본인 oprtrId 기준 목록 200 반환")
    void list_AIRLINE_본인목록_200() throws Exception {
        given(oprtrService.selectAccessibleForUser(eq("AIRLINE"), eq("OP0001"), isNull(), isNull()))
                .willReturn(List.of(sampleVO()));

        mockMvc.perform(get("/api/com/oprtr")
                        .with(authentication(authToken(airlineUserOP0001)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("목록 조회 — VERIFIER 사용자는 배정된 운영사 목록 200 반환")
    void list_VERIFIER_배정목록_200() throws Exception {
        given(oprtrService.selectAccessibleForUser(eq("VERIFIER"), isNull(), eq("VI0001"), anyString()))
                .willReturn(List.of(sampleVO()));

        mockMvc.perform(get("/api/com/oprtr")
                        .with(authentication(authToken(verifierUser)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/com/oprtr/{oprtrId}
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("단건 조회 — 존재하는 ID MOLIT 조회 200 반환")
    void get_MOLIT_단건조회_200() throws Exception {
        given(oprtrService.selectByOprtrId(eq("OP0001"), any(IcasUser.class))).willReturn(sampleVO());

        mockMvc.perform(get("/api/com/oprtr/OP0001")
                        .with(authentication(authToken(molitUser)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.oprtrId").value("OP0001"));
    }

    @Test
    @DisplayName("단건 조회 — Service 에서 FORBIDDEN 발생 시 403 반환")
    void get_Service_FORBIDDEN_403() throws Exception {
        given(oprtrService.selectByOprtrId(eq("OP0001"), any(IcasUser.class)))
                .willThrow(BusinessException.forbidden("본인 항공사 데이터만 조회할 수 있습니다."));

        mockMvc.perform(get("/api/com/oprtr/OP0001")
                        .with(authentication(authToken(airlineUserOP0002)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/com/oprtr
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("운영사 등록 — MOLIT 사용자 정상 등록 200 반환")
    void create_MOLIT_정상등록_200() throws Exception {
        OprtrVO reqVO = sampleVO();
        reqVO.setOprtrId(null);
        given(oprtrService.insert(any(OprtrVO.class), any(IcasUser.class))).willReturn(sampleVO());

        mockMvc.perform(post("/api/com/oprtr")
                        .with(authentication(authToken(molitUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqVO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("등록되었습니다"))
                .andExpect(jsonPath("$.data.oprtrId").value("OP0001"));
    }

    @Test
    @DisplayName("운영사 등록 — AIRLINE 사용자는 Controller 에서 403 반환")
    void create_AIRLINE_FORBIDDEN_403() throws Exception {
        OprtrVO reqVO = sampleVO();
        reqVO.setOprtrId(null);

        mockMvc.perform(post("/api/com/oprtr")
                        .with(authentication(authToken(airlineUserOP0001)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqVO)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));

        then(oprtrService).should(never()).insert(any(), any());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE /api/com/oprtr/{oprtrId}
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("소프트삭제 — AIRLINE 사용자는 Controller 에서 403 반환")
    void delete_AIRLINE_FORBIDDEN_403() throws Exception {
        mockMvc.perform(delete("/api/com/oprtr/OP0001")
                        .with(authentication(authToken(airlineUserOP0001))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));

        then(oprtrService).should(never()).softDelete(any(), any());
    }

    @Test
    @DisplayName("소프트삭제 — 존재하지 않는 ID 삭제 시 Service NOT_FOUND → 404")
    void delete_존재않는ID_NOT_FOUND_404() throws Exception {
        willThrow(BusinessException.notFound("항공기 운영사"))
                .given(oprtrService).softDelete(eq("OP9999"), any(IcasUser.class));

        mockMvc.perform(delete("/api/com/oprtr/OP9999")
                        .with(authentication(authToken(molitUser))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }
}
