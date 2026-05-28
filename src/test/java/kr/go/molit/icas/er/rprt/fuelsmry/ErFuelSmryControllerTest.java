package kr.go.molit.icas.er.rprt.fuelsmry;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.exception.GlobalExceptionHandler;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.er.rprt.fuelsmry.domain.ErFuelSmryVO;
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

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ErFuelSmryController 슬라이스 테스트")
class ErFuelSmryControllerTest {

    MockMvc mockMvc;

    @Mock
    ErFuelSmryService erFuelSmryService;

    ObjectMapper objectMapper;

    IcasUser airlineUserOP0001;
    IcasUser airlineUserOP0002;

    @BeforeEach
    void setUp() {
        ErFuelSmryController controller = new ErFuelSmryController(erFuelSmryService);
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

    private ErFuelSmryVO sampleFuelSmry(String erId, String fuelTypeCd,
                                        BigDecimal ttlFuelWght, BigDecimal ttlCo2Emsn) {
        ErFuelSmryVO vo = new ErFuelSmryVO();
        vo.setErId(erId);
        vo.setFuelTypeCd(fuelTypeCd);
        vo.setTtlFuelWght(ttlFuelWght);
        vo.setTtlCo2Emsn(ttlCo2Emsn);
        return vo;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/er/rprt/{erId}/fuel-smry
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("목록 조회 — 정상 200, data 배열 + fuelTypeCd / ttlFuelWght 검증")
    void list_정상_200() throws Exception {
        given(erFuelSmryService.list(eq("ER0001"), any(IcasUser.class)))
                .willReturn(List.of(
                        sampleFuelSmry("ER0001", "JET_A", new BigDecimal("10000.0000"), new BigDecimal("31500.0000")),
                        sampleFuelSmry("ER0001", "JET_B", new BigDecimal("5000.0000"), new BigDecimal("15750.0000"))));

        mockMvc.perform(get("/api/er/rprt/ER0001/fuel-smry")
                        .with(authentication(authToken(airlineUserOP0001)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].erId").value("ER0001"))
                .andExpect(jsonPath("$.data[0].fuelTypeCd").value("JET_A"))
                .andExpect(jsonPath("$.data[1].fuelTypeCd").value("JET_B"));
    }

    @Test
    @DisplayName("목록 조회 — Service NOT_FOUND 시 404 응답")
    void list_Service_NOT_FOUND_404() throws Exception {
        given(erFuelSmryService.list(eq("ER9999"), any(IcasUser.class)))
                .willThrow(BusinessException.notFound("ER"));

        mockMvc.perform(get("/api/er/rprt/ER9999/fuel-smry")
                        .with(authentication(authToken(airlineUserOP0001)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    @DisplayName("목록 조회 — Service FORBIDDEN 시 403 응답")
    void list_Service_FORBIDDEN_403() throws Exception {
        given(erFuelSmryService.list(eq("ER0001"), any(IcasUser.class)))
                .willThrow(BusinessException.forbidden("본인 항공사 데이터만 접근할 수 있습니다."));

        mockMvc.perform(get("/api/er/rprt/ER0001/fuel-smry")
                        .with(authentication(authToken(airlineUserOP0002)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUT /api/er/rprt/{erId}/fuel-smry/{fuelTypeCd}
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("upsert — 정상 200, $.data.fuelTypeCd / $.data.ttlFuelWght 검증")
    void upsert_정상_200() throws Exception {
        ErFuelSmryVO req = new ErFuelSmryVO();
        req.setTtlFuelWght(new BigDecimal("10000"));
        req.setTtlCo2Emsn(new BigDecimal("31500"));

        given(erFuelSmryService.upsert(eq("ER0001"), eq("JET_A"), any(ErFuelSmryVO.class), any(IcasUser.class)))
                .willReturn(sampleFuelSmry("ER0001", "JET_A", new BigDecimal("10000"), new BigDecimal("31500")));

        mockMvc.perform(put("/api/er/rprt/ER0001/fuel-smry/JET_A")
                        .with(authentication(authToken(airlineUserOP0001)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fuelTypeCd").value("JET_A"))
                .andExpect(jsonPath("$.data.ttlFuelWght").value(10000));
    }

    @Test
    @DisplayName("upsert — Service BAD_REQUEST 시 400 응답 (ttl_fuel_wght 음수)")
    void upsert_Service_BAD_REQUEST_400() throws Exception {
        ErFuelSmryVO req = new ErFuelSmryVO();
        req.setTtlFuelWght(new BigDecimal("-1.00"));

        given(erFuelSmryService.upsert(eq("ER0001"), eq("JET_A"), any(ErFuelSmryVO.class), any(IcasUser.class)))
                .willThrow(BusinessException.badRequest("총 연료 중량(ttlFuelWght)은 0 이상이어야 합니다."));

        mockMvc.perform(put("/api/er/rprt/ER0001/fuel-smry/JET_A")
                        .with(authentication(authToken(airlineUserOP0001)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    @Test
    @DisplayName("upsert — Service FORBIDDEN 시 403 응답 (타 운영사)")
    void upsert_Service_FORBIDDEN_403() throws Exception {
        ErFuelSmryVO req = new ErFuelSmryVO();
        req.setTtlFuelWght(new BigDecimal("10000"));

        given(erFuelSmryService.upsert(eq("ER0001"), eq("JET_A"), any(ErFuelSmryVO.class), any(IcasUser.class)))
                .willThrow(BusinessException.forbidden("본인 항공사 데이터만 등록·수정할 수 있습니다."));

        mockMvc.perform(put("/api/er/rprt/ER0001/fuel-smry/JET_A")
                        .with(authentication(authToken(airlineUserOP0002)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    @DisplayName("upsert — Service BAD_REQUEST 시 400 응답 (DRAFT 아닌 ER 상태)")
    void upsert_부모ER_DRAFT아님_BAD_REQUEST_400() throws Exception {
        ErFuelSmryVO req = new ErFuelSmryVO();
        req.setTtlFuelWght(new BigDecimal("10000"));

        given(erFuelSmryService.upsert(eq("ER0001"), eq("JET_A"), any(ErFuelSmryVO.class), any(IcasUser.class)))
                .willThrow(BusinessException.badRequest("DRAFT 상태의 ER 에서만 수정할 수 있습니다."));

        mockMvc.perform(put("/api/er/rprt/ER0001/fuel-smry/JET_A")
                        .with(authentication(authToken(airlineUserOP0001)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE /api/er/rprt/{erId}/fuel-smry/{fuelTypeCd}
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("소프트삭제 — 정상 200, $.success true")
    void delete_정상_200() throws Exception {
        willDoNothing().given(erFuelSmryService)
                .softDelete(eq("ER0001"), eq("JET_A"), any(IcasUser.class));

        mockMvc.perform(delete("/api/er/rprt/ER0001/fuel-smry/JET_A")
                        .with(authentication(authToken(airlineUserOP0001))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("소프트삭제 — Service NOT_FOUND 시 404 응답 (항목 미존재)")
    void delete_Service_NOT_FOUND_404() throws Exception {
        willThrow(BusinessException.notFound("연료 유형별 총사용량 항목"))
                .given(erFuelSmryService).softDelete(eq("ER0001"), eq("NONE_CD"), any(IcasUser.class));

        mockMvc.perform(delete("/api/er/rprt/ER0001/fuel-smry/NONE_CD")
                        .with(authentication(authToken(airlineUserOP0001))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }
}
