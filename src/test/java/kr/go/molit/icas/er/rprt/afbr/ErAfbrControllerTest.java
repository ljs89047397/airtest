package kr.go.molit.icas.er.rprt.afbr;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.exception.GlobalExceptionHandler;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.er.rprt.afbr.domain.ErAfbrVO;
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
@DisplayName("ErAfbrController 슬라이스 테스트")
class ErAfbrControllerTest {

    MockMvc mockMvc;

    @Mock
    ErAfbrService erAfbrService;

    ObjectMapper objectMapper;

    IcasUser airlineUserOP0001;
    IcasUser airlineUserOP0002;

    @BeforeEach
    void setUp() {
        ErAfbrController controller = new ErAfbrController(erAfbrService);
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

    private ErAfbrVO sampleAfbr(String erId, String acftTypeCd, BigDecimal afbrVal) {
        ErAfbrVO vo = new ErAfbrVO();
        vo.setErId(erId);
        vo.setAcftTypeCd(acftTypeCd);
        vo.setAfbrVal(afbrVal);
        vo.setAfbrUnit("kg/min");
        return vo;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/er/rprt/{erId}/afbr
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("목록 조회 — 정상 200, data 배열 + acftTypeCd 검증")
    void list_정상_200() throws Exception {
        given(erAfbrService.list(eq("ER0001"), any(IcasUser.class)))
                .willReturn(List.of(
                        sampleAfbr("ER0001", "B737", new BigDecimal("2.50")),
                        sampleAfbr("ER0001", "B747", new BigDecimal("3.80"))));

        mockMvc.perform(get("/api/er/rprt/ER0001/afbr")
                        .with(authentication(authToken(airlineUserOP0001)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].erId").value("ER0001"))
                .andExpect(jsonPath("$.data[0].acftTypeCd").value("B737"))
                .andExpect(jsonPath("$.data[1].acftTypeCd").value("B747"));
    }

    @Test
    @DisplayName("목록 조회 — Service NOT_FOUND 시 404 응답")
    void list_Service_NOT_FOUND_404() throws Exception {
        given(erAfbrService.list(eq("ER9999"), any(IcasUser.class)))
                .willThrow(BusinessException.notFound("ER"));

        mockMvc.perform(get("/api/er/rprt/ER9999/afbr")
                        .with(authentication(authToken(airlineUserOP0001)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    @DisplayName("목록 조회 — Service FORBIDDEN 시 403 응답")
    void list_Service_FORBIDDEN_403() throws Exception {
        given(erAfbrService.list(eq("ER0001"), any(IcasUser.class)))
                .willThrow(BusinessException.forbidden("본인 항공사 데이터만 접근할 수 있습니다."));

        mockMvc.perform(get("/api/er/rprt/ER0001/afbr")
                        .with(authentication(authToken(airlineUserOP0002)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUT /api/er/rprt/{erId}/afbr/{acftTypeCd}
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("upsert — 정상 200, $.data.acftTypeCd / $.data.afbrVal 검증")
    void upsert_정상_200() throws Exception {
        ErAfbrVO req = new ErAfbrVO();
        req.setAfbrVal(new BigDecimal("2.50"));
        req.setAfbrUnit("kg/min");

        given(erAfbrService.upsert(eq("ER0001"), eq("B737"), any(ErAfbrVO.class), any(IcasUser.class)))
                .willReturn(sampleAfbr("ER0001", "B737", new BigDecimal("2.50")));

        mockMvc.perform(put("/api/er/rprt/ER0001/afbr/B737")
                        .with(authentication(authToken(airlineUserOP0001)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.acftTypeCd").value("B737"))
                .andExpect(jsonPath("$.data.afbrVal").value(2.50));
    }

    @Test
    @DisplayName("upsert — Service BAD_REQUEST 시 400 응답 (afbrVal <= 0)")
    void upsert_Service_BAD_REQUEST_400() throws Exception {
        ErAfbrVO req = new ErAfbrVO();
        req.setAfbrVal(new BigDecimal("-1.00"));

        given(erAfbrService.upsert(eq("ER0001"), eq("B737"), any(ErAfbrVO.class), any(IcasUser.class)))
                .willThrow(BusinessException.badRequest("평균 연료연소율(afbrVal)은 0 보다 커야 합니다."));

        mockMvc.perform(put("/api/er/rprt/ER0001/afbr/B737")
                        .with(authentication(authToken(airlineUserOP0001)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    @Test
    @DisplayName("upsert — Service FORBIDDEN 시 403 응답 (타 운영사)")
    void upsert_Service_FORBIDDEN_403() throws Exception {
        ErAfbrVO req = new ErAfbrVO();
        req.setAfbrVal(new BigDecimal("2.50"));

        given(erAfbrService.upsert(eq("ER0001"), eq("B737"), any(ErAfbrVO.class), any(IcasUser.class)))
                .willThrow(BusinessException.forbidden("본인 항공사 데이터만 등록·수정할 수 있습니다."));

        mockMvc.perform(put("/api/er/rprt/ER0001/afbr/B737")
                        .with(authentication(authToken(airlineUserOP0002)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE /api/er/rprt/{erId}/afbr/{acftTypeCd}
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("소프트삭제 — 정상 200, $.success true")
    void delete_정상_200() throws Exception {
        willDoNothing().given(erAfbrService)
                .softDelete(eq("ER0001"), eq("B737"), any(IcasUser.class));

        mockMvc.perform(delete("/api/er/rprt/ER0001/afbr/B737")
                        .with(authentication(authToken(airlineUserOP0001))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("소프트삭제 — Service NOT_FOUND 시 404 응답 (항목 미존재)")
    void delete_Service_NOT_FOUND_404() throws Exception {
        willThrow(BusinessException.notFound("평균 연료연소율 항목"))
                .given(erAfbrService).softDelete(eq("ER0001"), eq("B999"), any(IcasUser.class));

        mockMvc.perform(delete("/api/er/rprt/ER0001/afbr/B999")
                        .with(authentication(authToken(airlineUserOP0001))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }
}
