package kr.go.molit.icas.com.ognz;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import kr.go.molit.icas.com.ognz.domain.OgnzVO;
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
 * OgnzController 슬라이스 테스트.
 * standaloneSetup + SecurityMockMvcRequestPostProcessors.authentication() 사용.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OgnzController 슬라이스 테스트")
class OgnzControllerTest {

    MockMvc mockMvc;

    @Mock
    OgnzService ognzService;

    ObjectMapper objectMapper;

    IcasUser molitUser;
    IcasUser kotsaUser;
    IcasUser airlineUser;

    @BeforeEach
    void setUp() {
        OgnzController controller = new OgnzController(ognzService);
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

        kotsaUser = IcasUser.builder()
                .userId("kotsa01").userNm("교통안전공단 담당자")
                .ognzSeCd("KOTSA").ognzId("ORG_KOTSA").master(false)
                .roleIds(List.of("ADMIN")).build();

        airlineUser = IcasUser.builder()
                .userId("airline01").userNm("항공사 담당자")
                .ognzSeCd("AIRLINE").ognzId("ORG_AIR01").oprtrId("OP0001").master(false)
                .roleIds(List.of("AIRLINE_USER")).build();
    }

    private UsernamePasswordAuthenticationToken authToken(IcasUser user) {
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }

    private OgnzVO sampleVO() {
        OgnzVO vo = new OgnzVO();
        vo.setOgnzId("ORG_TEST01");
        vo.setOgnzSeCd("AIRLINE");
        vo.setOgnzNm("테스트항공사");
        return vo;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/com/ognz
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("목록 조회 — 인증된 사용자 200 반환")
    void listAll_인증된사용자_200() throws Exception {
        given(ognzService.listAll()).willReturn(List.of(sampleVO()));

        mockMvc.perform(get("/api/com/ognz")
                        .with(authentication(authToken(molitUser)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());

        then(ognzService).should().listAll();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/com/ognz/{ognzId}
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("단건 조회 — 존재하는 ID 200 반환")
    void getOne_존재하는ID_200() throws Exception {
        given(ognzService.getOgnz("ORG_TEST01")).willReturn(sampleVO());

        mockMvc.perform(get("/api/com/ognz/ORG_TEST01")
                        .with(authentication(authToken(molitUser)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.ognzId").value("ORG_TEST01"));
    }

    @Test
    @DisplayName("단건 조회 — 존재하지 않는 ID 시 Service NOT_FOUND → 404")
    void getOne_없는ID_NOT_FOUND_404() throws Exception {
        given(ognzService.getOgnz("NO_EXIST"))
                .willThrow(BusinessException.notFound("기관"));

        mockMvc.perform(get("/api/com/ognz/NO_EXIST")
                        .with(authentication(authToken(molitUser)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/com/ognz
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("기관 등록 — MOLIT 사용자 정상 등록 200 반환")
    void create_MOLIT_정상등록_200() throws Exception {
        OgnzVO reqVO = sampleVO();
        given(ognzService.createOgnz(any(OgnzVO.class), any(IcasUser.class))).willReturn(sampleVO());

        mockMvc.perform(post("/api/com/ognz")
                        .with(authentication(authToken(molitUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqVO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("기관이 등록되었습니다."))
                .andExpect(jsonPath("$.data.ognzId").value("ORG_TEST01"));
    }

    @Test
    @DisplayName("기관 등록 — KOTSA 사용자도 정상 등록 가능")
    void create_KOTSA_정상등록_200() throws Exception {
        OgnzVO reqVO = sampleVO();
        given(ognzService.createOgnz(any(OgnzVO.class), any(IcasUser.class))).willReturn(sampleVO());

        mockMvc.perform(post("/api/com/ognz")
                        .with(authentication(authToken(kotsaUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqVO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("기관 등록 — AIRLINE 사용자는 Controller 에서 403 반환")
    void create_AIRLINE_FORBIDDEN_403() throws Exception {
        OgnzVO reqVO = sampleVO();

        mockMvc.perform(post("/api/com/ognz")
                        .with(authentication(authToken(airlineUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqVO)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));

        then(ognzService).should(never()).createOgnz(any(), any());
    }

    @Test
    @DisplayName("기관 등록 — ognzSeCd 잘못된 값(XXX) → Service BAD_REQUEST → 400")
    void create_잘못된ognzSeCd_BAD_REQUEST_400() throws Exception {
        OgnzVO reqVO = sampleVO();
        reqVO.setOgnzSeCd("XXX");
        given(ognzService.createOgnz(any(OgnzVO.class), any(IcasUser.class)))
                .willThrow(BusinessException.badRequest("기관 구분 코드가 유효하지 않습니다."));

        mockMvc.perform(post("/api/com/ognz")
                        .with(authentication(authToken(molitUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqVO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    @Test
    @DisplayName("기관 등록 — 중복 ognzId → Service CONFLICT → 409")
    void create_중복ID_CONFLICT_409() throws Exception {
        OgnzVO reqVO = sampleVO();
        given(ognzService.createOgnz(any(OgnzVO.class), any(IcasUser.class)))
                .willThrow(BusinessException.conflict("이미 사용 중인 기관 ID 입니다: ORG_TEST01"));

        mockMvc.perform(post("/api/com/ognz")
                        .with(authentication(authToken(molitUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqVO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUT /api/com/ognz/{ognzId}
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("기관 수정 — 존재하지 않는 ID 수정 시 Service NOT_FOUND → 404")
    void update_없는ID_NOT_FOUND_404() throws Exception {
        OgnzVO reqVO = sampleVO();
        given(ognzService.updateOgnz(eq("NO_EXIST"), any(OgnzVO.class), any(IcasUser.class)))
                .willThrow(BusinessException.notFound("기관"));

        mockMvc.perform(put("/api/com/ognz/NO_EXIST")
                        .with(authentication(authToken(molitUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqVO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE /api/com/ognz/{ognzId}
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("소프트 삭제 — MOLIT 사용자 정상 삭제 200 반환")
    void delete_MOLIT_정상삭제_200() throws Exception {
        willDoNothing().given(ognzService).softDeleteOgnz(eq("ORG_TEST01"), any(IcasUser.class));

        mockMvc.perform(delete("/api/com/ognz/ORG_TEST01")
                        .with(authentication(authToken(molitUser))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("기관이 삭제되었습니다."));
    }

    @Test
    @DisplayName("소프트 삭제 — AIRLINE 사용자 시도 시 403 반환")
    void delete_AIRLINE_FORBIDDEN_403() throws Exception {
        mockMvc.perform(delete("/api/com/ognz/ORG_TEST01")
                        .with(authentication(authToken(airlineUser))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));

        then(ognzService).should(never()).softDeleteOgnz(any(), any());
    }

    @Test
    @DisplayName("소프트 삭제 — 존재하지 않는 ID 시 Service NOT_FOUND → 404")
    void delete_없는ID_NOT_FOUND_404() throws Exception {
        willThrow(BusinessException.notFound("기관"))
                .given(ognzService).softDeleteOgnz(eq("NO_EXIST"), any(IcasUser.class));

        mockMvc.perform(delete("/api/com/ognz/NO_EXIST")
                        .with(authentication(authToken(molitUser))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }
}
