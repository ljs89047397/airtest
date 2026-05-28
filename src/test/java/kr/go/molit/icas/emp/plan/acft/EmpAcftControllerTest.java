package kr.go.molit.icas.emp.plan.acft;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.exception.GlobalExceptionHandler;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.emp.plan.acft.domain.EmpAcftVO;
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
@DisplayName("EmpAcftController 슬라이스 테스트")
class EmpAcftControllerTest {

    MockMvc mockMvc;

    @Mock
    EmpAcftService empAcftService;

    ObjectMapper objectMapper;

    IcasUser airlineUserOP0001;
    IcasUser molitUser;

    @BeforeEach
    void setUp() {
        EmpAcftController controller = new EmpAcftController(empAcftService);
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

        molitUser = IcasUser.builder()
                .userId("molit01").userNm("국토부 담당자")
                .ognzSeCd("MOLIT").ognzId("ORG_MOLIT").master(false)
                .roleIds(List.of("ADMIN")).build();
    }

    private UsernamePasswordAuthenticationToken authToken(IcasUser user) {
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }

    private EmpAcftVO sampleAcft(int sn) {
        EmpAcftVO vo = new EmpAcftVO();
        vo.setEmpPlanId("EP0001");
        vo.setAcftSn(sn);
        vo.setAcftTypeCd("B737");
        vo.setFuelTypeCd("JET_A");
        vo.setAcftCnt(5);
        return vo;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET 목록
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET 목록 — 정상 200, data 배열 + acftSn 검증")
    void list_정상_200() throws Exception {
        given(empAcftService.listByPlan(eq("EP0001"), any(IcasUser.class)))
                .willReturn(List.of(sampleAcft(1), sampleAcft(2)));

        mockMvc.perform(get("/api/emp/plan/EP0001/acft")
                        .with(authentication(authToken(molitUser)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].acftSn").value(1))
                .andExpect(jsonPath("$.data[0].acftTypeCd").value("B737"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET 단건
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET 단건 — 정상 200, data.acftCnt 검증")
    void getOne_정상_200() throws Exception {
        given(empAcftService.getOne(eq("EP0001"), eq(1), any(IcasUser.class)))
                .willReturn(sampleAcft(1));

        mockMvc.perform(get("/api/emp/plan/EP0001/acft/1")
                        .with(authentication(authToken(molitUser)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.acftCnt").value(5))
                .andExpect(jsonPath("$.data.fuelTypeCd").value("JET_A"));
    }

    @Test
    @DisplayName("GET 단건 — Service NOT_FOUND → 404")
    void getOne_NOT_FOUND_404() throws Exception {
        given(empAcftService.getOne(eq("EP0001"), eq(99), any(IcasUser.class)))
                .willThrow(BusinessException.notFound("항공기"));

        mockMvc.perform(get("/api/emp/plan/EP0001/acft/99")
                        .with(authentication(authToken(molitUser)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST 추가
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST 추가 — 정상 200, data.acftSn 검증")
    void add_정상_200() throws Exception {
        EmpAcftVO req = new EmpAcftVO();
        req.setAcftTypeCd("B737");
        req.setFuelTypeCd("JET_A");
        req.setAcftCnt(5);

        given(empAcftService.addChild(eq("EP0001"), any(EmpAcftVO.class), any(IcasUser.class)))
                .willReturn(sampleAcft(1));

        mockMvc.perform(post("/api/emp/plan/EP0001/acft")
                        .with(authentication(authToken(airlineUserOP0001)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.acftSn").value(1));
    }

    @Test
    @DisplayName("POST 추가 — Service BAD_REQUEST(acftCnt=0) → 400")
    void add_validation실패_400() throws Exception {
        EmpAcftVO req = new EmpAcftVO();
        req.setAcftTypeCd("B737");
        req.setFuelTypeCd("JET_A");
        req.setAcftCnt(0);

        given(empAcftService.addChild(eq("EP0001"), any(EmpAcftVO.class), any(IcasUser.class)))
                .willThrow(BusinessException.badRequest("항공기 대수(acftCnt)는 1 이상이어야 합니다."));

        mockMvc.perform(post("/api/emp/plan/EP0001/acft")
                        .with(authentication(authToken(airlineUserOP0001)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    @Test
    @DisplayName("POST 추가 — Service FORBIDDEN(타 운영사) → 403")
    void add_권한위반_403() throws Exception {
        EmpAcftVO req = new EmpAcftVO();
        req.setAcftTypeCd("B737");
        req.setFuelTypeCd("JET_A");
        req.setAcftCnt(2);

        given(empAcftService.addChild(eq("EP0001"), any(EmpAcftVO.class), any(IcasUser.class)))
                .willThrow(BusinessException.forbidden("본인 항공사 데이터만 등록·수정할 수 있습니다."));

        mockMvc.perform(post("/api/emp/plan/EP0001/acft")
                        .with(authentication(authToken(airlineUserOP0001)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUT 수정
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("PUT 수정 — 정상 200")
    void update_정상_200() throws Exception {
        EmpAcftVO req = new EmpAcftVO();
        req.setAcftTypeCd("A320");
        req.setFuelTypeCd("JET_A");
        req.setAcftCnt(3);

        willDoNothing().given(empAcftService)
                .updateChild(eq("EP0001"), eq(1), any(EmpAcftVO.class), any(IcasUser.class));

        mockMvc.perform(put("/api/emp/plan/EP0001/acft/1")
                        .with(authentication(authToken(airlineUserOP0001)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("PUT 수정 — Service NOT_FOUND → 404")
    void update_NOT_FOUND_404() throws Exception {
        EmpAcftVO req = new EmpAcftVO();
        req.setAcftTypeCd("A320");
        req.setFuelTypeCd("JET_A");
        req.setAcftCnt(3);

        willThrow(BusinessException.notFound("항공기"))
                .given(empAcftService).updateChild(eq("EP0001"), eq(99), any(EmpAcftVO.class), any(IcasUser.class));

        mockMvc.perform(put("/api/emp/plan/EP0001/acft/99")
                        .with(authentication(authToken(airlineUserOP0001)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("DELETE — 정상 200")
    void delete_정상_200() throws Exception {
        willDoNothing().given(empAcftService)
                .softDeleteChild(eq("EP0001"), eq(1), any(IcasUser.class));

        mockMvc.perform(delete("/api/emp/plan/EP0001/acft/1")
                        .with(authentication(authToken(airlineUserOP0001))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("DELETE — Service NOT_FOUND → 404")
    void delete_NOT_FOUND_404() throws Exception {
        willThrow(BusinessException.notFound("항공기"))
                .given(empAcftService).softDeleteChild(eq("EP0001"), eq(99), any(IcasUser.class));

        mockMvc.perform(delete("/api/emp/plan/EP0001/acft/99")
                        .with(authentication(authToken(airlineUserOP0001))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }
}
