package kr.go.molit.icas.er.rprt.acft;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.exception.GlobalExceptionHandler;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.er.rprt.acft.domain.ErAcftFuelVO;
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

@ExtendWith(MockitoExtension.class)
@DisplayName("ErAcftFuelController 슬라이스 테스트")
class ErAcftFuelControllerTest {

    MockMvc mockMvc;

    @Mock
    ErAcftFuelService erAcftFuelService;

    ObjectMapper objectMapper;

    IcasUser airlineUserOP0001;
    IcasUser airlineUserOP0002;

    @BeforeEach
    void setUp() {
        ErAcftFuelController controller = new ErAcftFuelController(erAcftFuelService);
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver())
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        airlineUserOP0001 = IcasUser.builder()
                .userId("airline01").userNm("항공사A 담당자")
                .ognzSeCd("AIRLINE").ognzId("ORG_AIR01").oprtrId("OP0001").master(false)
                .roleIds(List.of("AIRLINE_USER")).build();

        airlineUserOP0002 = IcasUser.builder()
                .userId("airline02").userNm("항공사B 담당자")
                .ognzSeCd("AIRLINE").ognzId("ORG_AIR02").oprtrId("OP0002").master(false)
                .roleIds(List.of("AIRLINE_USER")).build();
    }

    private UsernamePasswordAuthenticationToken authToken(IcasUser user) {
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }

    private ErAcftFuelVO sampleAcftFuel(String erId, int acftSn, String regisMark) {
        ErAcftFuelVO vo = new ErAcftFuelVO();
        vo.setErId(erId);
        vo.setAcftSn(acftSn);
        vo.setRegisMark(regisMark);
        vo.setFuelTypeCd("JET_A");
        return vo;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/er/rprt/{erId}/acft-fuel
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("목록 조회 — 정상 200, data 배열 + erId 검증")
    void list_정상_200() throws Exception {
        given(erAcftFuelService.list(eq("ER0001"), any(IcasUser.class)))
                .willReturn(List.of(
                        sampleAcftFuel("ER0001", 1, "HL1234"),
                        sampleAcftFuel("ER0001", 2, "HL5678")));

        mockMvc.perform(get("/api/er/rprt/ER0001/acft-fuel")
                        .with(authentication(authToken(airlineUserOP0001)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].erId").value("ER0001"))
                .andExpect(jsonPath("$.data[0].regisMark").value("HL1234"))
                .andExpect(jsonPath("$.data[1].acftSn").value(2));
    }

    @Test
    @DisplayName("목록 조회 — Service NOT_FOUND 시 404 응답")
    void list_Service_NOT_FOUND_404() throws Exception {
        given(erAcftFuelService.list(eq("ER9999"), any(IcasUser.class)))
                .willThrow(BusinessException.notFound("ER"));

        mockMvc.perform(get("/api/er/rprt/ER9999/acft-fuel")
                        .with(authentication(authToken(airlineUserOP0001)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    @DisplayName("목록 조회 — Service FORBIDDEN 시 403 응답")
    void list_Service_FORBIDDEN_403() throws Exception {
        given(erAcftFuelService.list(eq("ER0001"), any(IcasUser.class)))
                .willThrow(BusinessException.forbidden("본인 항공사 데이터만 접근할 수 있습니다."));

        mockMvc.perform(get("/api/er/rprt/ER0001/acft-fuel")
                        .with(authentication(authToken(airlineUserOP0002)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/er/rprt/{erId}/acft-fuel
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("추가 — 정상 200, $.data.acftSn / $.data.regisMark 검증")
    void add_정상_200() throws Exception {
        ErAcftFuelVO req = new ErAcftFuelVO();
        req.setRegisMark("HL1234");
        req.setFuelTypeCd("JET_A");

        given(erAcftFuelService.add(eq("ER0001"), any(ErAcftFuelVO.class), any(IcasUser.class)))
                .willReturn(sampleAcftFuel("ER0001", 1, "HL1234"));

        mockMvc.perform(post("/api/er/rprt/ER0001/acft-fuel")
                        .with(authentication(authToken(airlineUserOP0001)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.acftSn").value(1))
                .andExpect(jsonPath("$.data.regisMark").value("HL1234"));
    }

    @Test
    @DisplayName("추가 — Service BAD_REQUEST 시 400 응답 (regisMark 누락)")
    void add_Service_BAD_REQUEST_400() throws Exception {
        ErAcftFuelVO req = new ErAcftFuelVO();
        req.setFuelTypeCd("JET_A");

        given(erAcftFuelService.add(eq("ER0001"), any(ErAcftFuelVO.class), any(IcasUser.class)))
                .willThrow(BusinessException.badRequest("항공기 등록기호(regisMark)는 필수입니다."));

        mockMvc.perform(post("/api/er/rprt/ER0001/acft-fuel")
                        .with(authentication(authToken(airlineUserOP0001)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    @Test
    @DisplayName("추가 — Service CONFLICT 시 409 응답 (regis_mark 중복)")
    void add_Service_CONFLICT_409() throws Exception {
        ErAcftFuelVO req = new ErAcftFuelVO();
        req.setRegisMark("HL1234");
        req.setFuelTypeCd("JET_A");

        given(erAcftFuelService.add(eq("ER0001"), any(ErAcftFuelVO.class), any(IcasUser.class)))
                .willThrow(BusinessException.conflict("이미 동일한 등록기호가 해당 ER 에 등록되어 있습니다."));

        mockMvc.perform(post("/api/er/rprt/ER0001/acft-fuel")
                        .with(authentication(authToken(airlineUserOP0001)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUT /api/er/rprt/{erId}/acft-fuel/{acftSn}
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("수정 — 정상 200, $.success true")
    void update_정상_200() throws Exception {
        ErAcftFuelVO req = new ErAcftFuelVO();
        req.setRegisMark("HL1234");
        req.setFuelTypeCd("JET_A");

        willDoNothing().given(erAcftFuelService)
                .update(eq("ER0001"), eq(1), any(ErAcftFuelVO.class), any(IcasUser.class));

        mockMvc.perform(put("/api/er/rprt/ER0001/acft-fuel/1")
                        .with(authentication(authToken(airlineUserOP0001)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("수정 — Service NOT_FOUND 시 404 응답 (항목 미존재)")
    void update_Service_NOT_FOUND_404() throws Exception {
        ErAcftFuelVO req = new ErAcftFuelVO();
        req.setRegisMark("HL9999");
        req.setFuelTypeCd("JET_A");

        willThrow(BusinessException.notFound("항공기·연료 항목"))
                .given(erAcftFuelService)
                .update(eq("ER0001"), eq(99), any(ErAcftFuelVO.class), any(IcasUser.class));

        mockMvc.perform(put("/api/er/rprt/ER0001/acft-fuel/99")
                        .with(authentication(authToken(airlineUserOP0001)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE /api/er/rprt/{erId}/acft-fuel/{acftSn}
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("소프트삭제 — 정상 200, $.success true")
    void delete_정상_200() throws Exception {
        willDoNothing().given(erAcftFuelService)
                .softDelete(eq("ER0001"), eq(1), any(IcasUser.class));

        mockMvc.perform(delete("/api/er/rprt/ER0001/acft-fuel/1")
                        .with(authentication(authToken(airlineUserOP0001))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("소프트삭제 — Service NOT_FOUND 시 404 응답")
    void delete_Service_NOT_FOUND_404() throws Exception {
        willThrow(BusinessException.notFound("항공기·연료 항목"))
                .given(erAcftFuelService).softDelete(eq("ER0001"), eq(99), any(IcasUser.class));

        mockMvc.perform(delete("/api/er/rprt/ER0001/acft-fuel/99")
                        .with(authentication(authToken(airlineUserOP0001))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }
}
