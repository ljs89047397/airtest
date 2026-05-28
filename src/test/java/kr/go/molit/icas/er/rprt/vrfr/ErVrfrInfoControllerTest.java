package kr.go.molit.icas.er.rprt.vrfr;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.exception.GlobalExceptionHandler;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.er.rprt.vrfr.domain.ErVrfrInfoVO;
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
 * ErVrfrInfoController 슬라이스 테스트.
 * Spring Boot 없이 순수 MockMvcBuilders.standaloneSetup 사용.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ErVrfrInfoController 슬라이스 테스트")
class ErVrfrInfoControllerTest {

    MockMvc mockMvc;

    @Mock
    ErVrfrInfoService erVrfrInfoService;

    ObjectMapper objectMapper;

    // ── fixture ──
    IcasUser molitUser;
    IcasUser kotsaUser;
    IcasUser airlineUserOP0001;

    @BeforeEach
    void setUp() {
        ErVrfrInfoController controller = new ErVrfrInfoController(erVrfrInfoService);
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
                .roleIds(List.of("KOTSA_USER")).build();

        airlineUserOP0001 = IcasUser.builder()
                .userId("airline01").userNm("항공사A 담당자")
                .ognzSeCd("AIRLINE").ognzId("ORG_AIR01").oprtrId("OP0001").master(false)
                .roleIds(List.of("AIRLINE_USER")).build();
    }

    private UsernamePasswordAuthenticationToken authToken(IcasUser user) {
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }

    private ErVrfrInfoVO sampleVrfrInfo(String erId, int vrfrSn, String vrfcnInstId) {
        ErVrfrInfoVO vo = new ErVrfrInfoVO();
        vo.setErId(erId);
        vo.setVrfrSn(vrfrSn);
        vo.setVrfcnInstId(vrfcnInstId);
        vo.setCnctDesc("참여 개요");
        vo.setAccrdDtl("인증 상세");
        return vo;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/er/rprt/{erId}/vrfr-info — 목록 조회
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("목록 조회 — MOLIT 사용자 정상 200 응답, data 배열 포함")
    void list_MOLIT_정상조회_200() throws Exception {
        given(erVrfrInfoService.listByErId(eq("ER0001"), any(IcasUser.class)))
                .willReturn(List.of(
                        sampleVrfrInfo("ER0001", 1, "VI0001"),
                        sampleVrfrInfo("ER0001", 2, "VI0002")));

        mockMvc.perform(get("/api/er/rprt/ER0001/vrfr-info")
                        .with(authentication(authToken(molitUser)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].erId").value("ER0001"))
                .andExpect(jsonPath("$.data[0].vrfrSn").value(1))
                .andExpect(jsonPath("$.data[0].vrfcnInstId").value("VI0001"));
    }

    @Test
    @DisplayName("목록 조회 — Service NOT_FOUND 시 404 응답 (부모 ER 미존재)")
    void list_Service_NOT_FOUND_404() throws Exception {
        given(erVrfrInfoService.listByErId(eq("ER9999"), any(IcasUser.class)))
                .willThrow(BusinessException.notFound("ER"));

        mockMvc.perform(get("/api/er/rprt/ER9999/vrfr-info")
                        .with(authentication(authToken(molitUser)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    @DisplayName("목록 조회 — Service FORBIDDEN 시 403 응답 (가시범위 위반)")
    void list_Service_FORBIDDEN_403() throws Exception {
        given(erVrfrInfoService.listByErId(eq("ER0001"), any(IcasUser.class)))
                .willThrow(BusinessException.forbidden("본인 항공사 데이터만 접근할 수 있습니다."));

        mockMvc.perform(get("/api/er/rprt/ER0001/vrfr-info")
                        .with(authentication(authToken(airlineUserOP0001)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/er/rprt/{erId}/vrfr-info/{vrfrSn} — 단건 조회
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("단건 조회 — 정상 200 응답, $.data.vrfcnInstId 검증")
    void getOne_단건조회_정상_200() throws Exception {
        given(erVrfrInfoService.getOne(eq("ER0001"), eq(1), any(IcasUser.class)))
                .willReturn(sampleVrfrInfo("ER0001", 1, "VI0001"));

        mockMvc.perform(get("/api/er/rprt/ER0001/vrfr-info/1")
                        .with(authentication(authToken(molitUser)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.vrfcnInstId").value("VI0001"))
                .andExpect(jsonPath("$.data.vrfrSn").value(1));
    }

    @Test
    @DisplayName("단건 조회 — Service NOT_FOUND 시 404 응답")
    void getOne_Service_NOT_FOUND_404() throws Exception {
        given(erVrfrInfoService.getOne(eq("ER0001"), eq(99), any(IcasUser.class)))
                .willThrow(BusinessException.notFound("참여 검증기관 정보"));

        mockMvc.perform(get("/api/er/rprt/ER0001/vrfr-info/99")
                        .with(authentication(authToken(molitUser)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/er/rprt/{erId}/vrfr-info — 검증기관 추가
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("참여 검증기관 추가 — AIRLINE 정상 등록 200 응답, $.data.vrfcnInstId / $.message 검증")
    void add_AIRLINE_정상등록_200() throws Exception {
        ErVrfrInfoVO reqVo = new ErVrfrInfoVO();
        reqVo.setVrfcnInstId("VI0001");
        reqVo.setCnctDesc("참여 개요");

        given(erVrfrInfoService.addVrfr(eq("ER0001"), any(ErVrfrInfoVO.class), any(IcasUser.class)))
                .willReturn(sampleVrfrInfo("ER0001", 1, "VI0001"));

        mockMvc.perform(post("/api/er/rprt/ER0001/vrfr-info")
                        .with(authentication(authToken(airlineUserOP0001)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqVo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("참여 검증기관이 추가되었습니다."))
                .andExpect(jsonPath("$.data.vrfcnInstId").value("VI0001"))
                .andExpect(jsonPath("$.data.vrfrSn").value(1));
    }

    @Test
    @DisplayName("참여 검증기관 추가 — Service BAD_REQUEST 시 400 응답 (부모 ER DRAFT 아님)")
    void add_Service_BAD_REQUEST_400() throws Exception {
        ErVrfrInfoVO reqVo = new ErVrfrInfoVO();
        reqVo.setVrfcnInstId("VI0001");

        given(erVrfrInfoService.addVrfr(eq("ER0001"), any(ErVrfrInfoVO.class), any(IcasUser.class)))
                .willThrow(BusinessException.badRequest("DRAFT 상태의 ER 에서만 수정할 수 있습니다. 현재 상태: SBMTD"));

        mockMvc.perform(post("/api/er/rprt/ER0001/vrfr-info")
                        .with(authentication(authToken(airlineUserOP0001)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqVo)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    @Test
    @DisplayName("참여 검증기관 추가 — Service FORBIDDEN 시 403 응답 (가시범위 위반)")
    void add_Service_FORBIDDEN_403() throws Exception {
        ErVrfrInfoVO reqVo = new ErVrfrInfoVO();
        reqVo.setVrfcnInstId("VI0001");

        given(erVrfrInfoService.addVrfr(eq("ER0001"), any(ErVrfrInfoVO.class), any(IcasUser.class)))
                .willThrow(BusinessException.forbidden("본인 항공사 데이터만 등록·수정할 수 있습니다."));

        mockMvc.perform(post("/api/er/rprt/ER0001/vrfr-info")
                        .with(authentication(authToken(kotsaUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqVo)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUT /api/er/rprt/{erId}/vrfr-info/{vrfrSn} — 검증기관 수정
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("참여 검증기관 수정 — AIRLINE 정상 수정 200 응답, $.message 검증")
    void update_AIRLINE_정상수정_200() throws Exception {
        ErVrfrInfoVO reqVo = new ErVrfrInfoVO();
        reqVo.setCnctDesc("수정된 참여 개요");

        willDoNothing().given(erVrfrInfoService)
                .updateVrfr(eq("ER0001"), eq(1), any(ErVrfrInfoVO.class), any(IcasUser.class));

        mockMvc.perform(put("/api/er/rprt/ER0001/vrfr-info/1")
                        .with(authentication(authToken(airlineUserOP0001)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqVo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("참여 검증기관 정보가 수정되었습니다."));
    }

    @Test
    @DisplayName("참여 검증기관 수정 — Service NOT_FOUND 시 404 응답")
    void update_Service_NOT_FOUND_404() throws Exception {
        ErVrfrInfoVO reqVo = new ErVrfrInfoVO();
        reqVo.setCnctDesc("수정된 개요");

        willThrow(BusinessException.notFound("참여 검증기관 정보"))
                .given(erVrfrInfoService)
                .updateVrfr(eq("ER0001"), eq(99), any(ErVrfrInfoVO.class), any(IcasUser.class));

        mockMvc.perform(put("/api/er/rprt/ER0001/vrfr-info/99")
                        .with(authentication(authToken(airlineUserOP0001)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqVo)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    @DisplayName("참여 검증기관 수정 — Service BAD_REQUEST 시 400 응답 (부모 ER DRAFT 아님)")
    void update_Service_BAD_REQUEST_400() throws Exception {
        ErVrfrInfoVO reqVo = new ErVrfrInfoVO();
        reqVo.setCnctDesc("수정된 개요");

        willThrow(BusinessException.badRequest("DRAFT 상태의 ER 에서만 수정할 수 있습니다. 현재 상태: SBMTD"))
                .given(erVrfrInfoService)
                .updateVrfr(eq("ER0001"), eq(1), any(ErVrfrInfoVO.class), any(IcasUser.class));

        mockMvc.perform(put("/api/er/rprt/ER0001/vrfr-info/1")
                        .with(authentication(authToken(airlineUserOP0001)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqVo)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE /api/er/rprt/{erId}/vrfr-info/{vrfrSn} — 소프트삭제
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("소프트삭제 — AIRLINE 정상 삭제 200 응답, $.message 검증")
    void delete_AIRLINE_정상삭제_200() throws Exception {
        willDoNothing().given(erVrfrInfoService)
                .softDeleteVrfr(eq("ER0001"), eq(1), any(IcasUser.class));

        mockMvc.perform(delete("/api/er/rprt/ER0001/vrfr-info/1")
                        .with(authentication(authToken(airlineUserOP0001))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("참여 검증기관 정보가 삭제되었습니다."));
    }

    @Test
    @DisplayName("소프트삭제 — Service NOT_FOUND 시 404 응답")
    void delete_Service_NOT_FOUND_404() throws Exception {
        willThrow(BusinessException.notFound("참여 검증기관 정보"))
                .given(erVrfrInfoService)
                .softDeleteVrfr(eq("ER0001"), eq(99), any(IcasUser.class));

        mockMvc.perform(delete("/api/er/rprt/ER0001/vrfr-info/99")
                        .with(authentication(authToken(airlineUserOP0001))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    @DisplayName("소프트삭제 — Service FORBIDDEN 시 403 응답 (가시범위 위반)")
    void delete_Service_FORBIDDEN_403() throws Exception {
        willThrow(BusinessException.forbidden("본인 항공사 데이터만 등록·수정할 수 있습니다."))
                .given(erVrfrInfoService)
                .softDeleteVrfr(eq("ER0001"), eq(1), any(IcasUser.class));

        mockMvc.perform(delete("/api/er/rprt/ER0001/vrfr-info/1")
                        .with(authentication(authToken(kotsaUser))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }
}
