package kr.go.molit.icas.emp.plan.co2detail;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.exception.GlobalExceptionHandler;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.emp.plan.co2detail.domain.EmpCo2DetailVO;
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
@DisplayName("EmpCo2DetailController 슬라이스 테스트")
class EmpCo2DetailControllerTest {

    MockMvc mockMvc;

    @Mock
    EmpCo2DetailService empCo2DetailService;

    ObjectMapper objectMapper;

    IcasUser airlineUserOP0001;
    IcasUser molitUser;

    @BeforeEach
    void setUp() {
        EmpCo2DetailController controller = new EmpCo2DetailController(empCo2DetailService);
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

    private EmpCo2DetailVO sampleDetail(String mntrMthdCd) {
        EmpCo2DetailVO vo = new EmpCo2DetailVO();
        vo.setEmpPlanId("EP0001");
        vo.setMntrMthdCd(mntrMthdCd);
        vo.setMsrTmingDesc("측정 시점 설명");
        return vo;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET 목록
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET 목록 — 정상 200, data 배열 + mntrMthdCd 검증")
    void list_정상_200() throws Exception {
        given(empCo2DetailService.listByPlan(eq("EP0001"), any(IcasUser.class)))
                .willReturn(List.of(sampleDetail("MTHD_A"), sampleDetail("MTHD_B")));

        mockMvc.perform(get("/api/emp/plan/EP0001/co2-detail")
                        .with(authentication(authToken(molitUser)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].mntrMthdCd").value("MTHD_A"))
                .andExpect(jsonPath("$.data[1].mntrMthdCd").value("MTHD_B"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET 단건 (path variable: mthdCd)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET 단건 — 정상 200, data.mntrMthdCd 검증")
    void getOne_정상_200() throws Exception {
        given(empCo2DetailService.getOne(eq("EP0001"), eq("MTHD_A"), any(IcasUser.class)))
                .willReturn(sampleDetail("MTHD_A"));

        mockMvc.perform(get("/api/emp/plan/EP0001/co2-detail/MTHD_A")
                        .with(authentication(authToken(molitUser)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.mntrMthdCd").value("MTHD_A"))
                .andExpect(jsonPath("$.data.msrTmingDesc").value("측정 시점 설명"));
    }

    @Test
    @DisplayName("GET 단건 — Service NOT_FOUND → 404")
    void getOne_NOT_FOUND_404() throws Exception {
        given(empCo2DetailService.getOne(eq("EP0001"), eq("BLOCK_ALLOC"), any(IcasUser.class)))
                .willThrow(BusinessException.notFound("CO2 측정 상세"));

        mockMvc.perform(get("/api/emp/plan/EP0001/co2-detail/BLOCK_ALLOC")
                        .with(authentication(authToken(molitUser)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST 추가
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST 추가 — 정상 200, data.mntrMthdCd 검증")
    void add_정상_200() throws Exception {
        EmpCo2DetailVO req = new EmpCo2DetailVO();
        req.setMntrMthdCd("MTHD_A");
        req.setMsrTmingDesc("시점 설명");

        given(empCo2DetailService.addChild(eq("EP0001"), any(EmpCo2DetailVO.class), any(IcasUser.class)))
                .willReturn(sampleDetail("MTHD_A"));

        mockMvc.perform(post("/api/emp/plan/EP0001/co2-detail")
                        .with(authentication(authToken(airlineUserOP0001)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.mntrMthdCd").value("MTHD_A"));
    }

    @Test
    @DisplayName("POST 추가 — Service BAD_REQUEST(화이트리스트 외) → 400")
    void add_validation실패_400() throws Exception {
        EmpCo2DetailVO req = new EmpCo2DetailVO();
        req.setMntrMthdCd("INVALID");

        given(empCo2DetailService.addChild(eq("EP0001"), any(EmpCo2DetailVO.class), any(IcasUser.class)))
                .willThrow(BusinessException.badRequest("모니터링 방법 코드 허용값: MTHD_A, MTHD_B, BLOCK_ON_OFF, REFUEL, BLOCK_ALLOC."));

        mockMvc.perform(post("/api/emp/plan/EP0001/co2-detail")
                        .with(authentication(authToken(airlineUserOP0001)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    @Test
    @DisplayName("POST 추가 — Service FORBIDDEN(타 운영사) → 403")
    void add_권한위반_403() throws Exception {
        EmpCo2DetailVO req = new EmpCo2DetailVO();
        req.setMntrMthdCd("MTHD_B");

        given(empCo2DetailService.addChild(eq("EP0001"), any(EmpCo2DetailVO.class), any(IcasUser.class)))
                .willThrow(BusinessException.forbidden("본인 항공사 데이터만 등록·수정할 수 있습니다."));

        mockMvc.perform(post("/api/emp/plan/EP0001/co2-detail")
                        .with(authentication(authToken(airlineUserOP0001)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    @DisplayName("POST 추가 — Service CONFLICT(중복 mntrMthdCd) → 409")
    void add_중복_409() throws Exception {
        EmpCo2DetailVO req = new EmpCo2DetailVO();
        req.setMntrMthdCd("MTHD_A");

        given(empCo2DetailService.addChild(eq("EP0001"), any(EmpCo2DetailVO.class), any(IcasUser.class)))
                .willThrow(BusinessException.conflict("이미 등록된 모니터링 방법입니다: MTHD_A."));

        mockMvc.perform(post("/api/emp/plan/EP0001/co2-detail")
                        .with(authentication(authToken(airlineUserOP0001)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUT 수정 (path variable: mthdCd)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("PUT 수정 — 정상 200")
    void update_정상_200() throws Exception {
        EmpCo2DetailVO req = new EmpCo2DetailVO();
        req.setMsrTmingDesc("수정된 설명");

        willDoNothing().given(empCo2DetailService)
                .updateChild(eq("EP0001"), eq("MTHD_A"), any(EmpCo2DetailVO.class), any(IcasUser.class));

        mockMvc.perform(put("/api/emp/plan/EP0001/co2-detail/MTHD_A")
                        .with(authentication(authToken(airlineUserOP0001)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("PUT 수정 — Service NOT_FOUND → 404")
    void update_NOT_FOUND_404() throws Exception {
        EmpCo2DetailVO req = new EmpCo2DetailVO();
        req.setMsrTmingDesc("설명");

        willThrow(BusinessException.notFound("CO2 측정 상세"))
                .given(empCo2DetailService)
                .updateChild(eq("EP0001"), eq("BLOCK_ALLOC"), any(EmpCo2DetailVO.class), any(IcasUser.class));

        mockMvc.perform(put("/api/emp/plan/EP0001/co2-detail/BLOCK_ALLOC")
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
        willDoNothing().given(empCo2DetailService)
                .softDeleteChild(eq("EP0001"), eq("MTHD_A"), any(IcasUser.class));

        mockMvc.perform(delete("/api/emp/plan/EP0001/co2-detail/MTHD_A")
                        .with(authentication(authToken(airlineUserOP0001))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("DELETE — Service NOT_FOUND → 404")
    void delete_NOT_FOUND_404() throws Exception {
        willThrow(BusinessException.notFound("CO2 측정 상세"))
                .given(empCo2DetailService).softDeleteChild(eq("EP0001"), eq("REFUEL"), any(IcasUser.class));

        mockMvc.perform(delete("/api/emp/plan/EP0001/co2-detail/REFUEL")
                        .with(authentication(authToken(airlineUserOP0001))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }
}
