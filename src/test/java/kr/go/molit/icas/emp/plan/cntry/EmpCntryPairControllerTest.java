package kr.go.molit.icas.emp.plan.cntry;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.exception.GlobalExceptionHandler;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.emp.plan.cntry.domain.EmpCntryPairVO;
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
@DisplayName("EmpCntryPairController 슬라이스 테스트")
class EmpCntryPairControllerTest {

    MockMvc mockMvc;

    @Mock
    EmpCntryPairService empCntryPairService;

    ObjectMapper objectMapper;

    IcasUser airlineUserOP0001;
    IcasUser molitUser;

    @BeforeEach
    void setUp() {
        EmpCntryPairController controller = new EmpCntryPairController(empCntryPairService);
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

    private EmpCntryPairVO samplePair(int sn, String dprtr, String arvl) {
        EmpCntryPairVO vo = new EmpCntryPairVO();
        vo.setEmpPlanId("EP0001");
        vo.setPairSn(sn);
        vo.setDprtrCntryCd(dprtr);
        vo.setArvlCntryCd(arvl);
        vo.setIntlYn("Y");
        return vo;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET 목록
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET 목록 — 정상 200, data 배열 + pairSn/dprtrCntryCd 검증")
    void list_정상_200() throws Exception {
        given(empCntryPairService.listByPlan(eq("EP0001"), any(IcasUser.class)))
                .willReturn(List.of(samplePair(1, "KR", "JP"), samplePair(2, "KR", "US")));

        mockMvc.perform(get("/api/emp/plan/EP0001/cntry-pair")
                        .with(authentication(authToken(molitUser)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].pairSn").value(1))
                .andExpect(jsonPath("$.data[0].dprtrCntryCd").value("KR"))
                .andExpect(jsonPath("$.data[0].arvlCntryCd").value("JP"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET 단건
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET 단건 — 정상 200, data.intlYn 검증")
    void getOne_정상_200() throws Exception {
        given(empCntryPairService.getOne(eq("EP0001"), eq(1), any(IcasUser.class)))
                .willReturn(samplePair(1, "KR", "JP"));

        mockMvc.perform(get("/api/emp/plan/EP0001/cntry-pair/1")
                        .with(authentication(authToken(molitUser)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.pairSn").value(1))
                .andExpect(jsonPath("$.data.intlYn").value("Y"));
    }

    @Test
    @DisplayName("GET 단건 — Service NOT_FOUND → 404")
    void getOne_NOT_FOUND_404() throws Exception {
        given(empCntryPairService.getOne(eq("EP0001"), eq(99), any(IcasUser.class)))
                .willThrow(BusinessException.notFound("운항 국가 쌍"));

        mockMvc.perform(get("/api/emp/plan/EP0001/cntry-pair/99")
                        .with(authentication(authToken(molitUser)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST 추가
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST 추가 — 정상 200, data.pairSn 검증")
    void add_정상_200() throws Exception {
        EmpCntryPairVO req = new EmpCntryPairVO();
        req.setDprtrCntryCd("KR");
        req.setArvlCntryCd("JP");
        req.setIntlYn("Y");

        given(empCntryPairService.addChild(eq("EP0001"), any(EmpCntryPairVO.class), any(IcasUser.class)))
                .willReturn(samplePair(1, "KR", "JP"));

        mockMvc.perform(post("/api/emp/plan/EP0001/cntry-pair")
                        .with(authentication(authToken(airlineUserOP0001)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.pairSn").value(1));
    }

    @Test
    @DisplayName("POST 추가 — Service BAD_REQUEST(자기참조 KR→KR) → 400")
    void add_validation실패_400() throws Exception {
        EmpCntryPairVO req = new EmpCntryPairVO();
        req.setDprtrCntryCd("KR");
        req.setArvlCntryCd("KR");
        req.setIntlYn("Y");

        given(empCntryPairService.addChild(eq("EP0001"), any(EmpCntryPairVO.class), any(IcasUser.class)))
                .willThrow(BusinessException.badRequest("국제 운항은 출발·도착 국가가 달라야 합니다."));

        mockMvc.perform(post("/api/emp/plan/EP0001/cntry-pair")
                        .with(authentication(authToken(airlineUserOP0001)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    @Test
    @DisplayName("POST 추가 — Service FORBIDDEN(타 운영사) → 403")
    void add_권한위반_403() throws Exception {
        EmpCntryPairVO req = new EmpCntryPairVO();
        req.setDprtrCntryCd("KR");
        req.setArvlCntryCd("JP");
        req.setIntlYn("Y");

        given(empCntryPairService.addChild(eq("EP0001"), any(EmpCntryPairVO.class), any(IcasUser.class)))
                .willThrow(BusinessException.forbidden("본인 항공사 데이터만 등록·수정할 수 있습니다."));

        mockMvc.perform(post("/api/emp/plan/EP0001/cntry-pair")
                        .with(authentication(authToken(airlineUserOP0001)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    @DisplayName("POST 추가 — Service CONFLICT(중복 쌍) → 409")
    void add_중복쌍_409() throws Exception {
        EmpCntryPairVO req = new EmpCntryPairVO();
        req.setDprtrCntryCd("KR");
        req.setArvlCntryCd("JP");
        req.setIntlYn("Y");

        given(empCntryPairService.addChild(eq("EP0001"), any(EmpCntryPairVO.class), any(IcasUser.class)))
                .willThrow(BusinessException.conflict("동일한 출발·도착 국가 조합이 이미 등록되어 있습니다."));

        mockMvc.perform(post("/api/emp/plan/EP0001/cntry-pair")
                        .with(authentication(authToken(airlineUserOP0001)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUT 수정
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("PUT 수정 — 정상 200")
    void update_정상_200() throws Exception {
        EmpCntryPairVO req = new EmpCntryPairVO();
        req.setDprtrCntryCd("KR");
        req.setArvlCntryCd("US");
        req.setIntlYn("Y");

        willDoNothing().given(empCntryPairService)
                .updateChild(eq("EP0001"), eq(1), any(EmpCntryPairVO.class), any(IcasUser.class));

        mockMvc.perform(put("/api/emp/plan/EP0001/cntry-pair/1")
                        .with(authentication(authToken(airlineUserOP0001)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("DELETE — 정상 200")
    void delete_정상_200() throws Exception {
        willDoNothing().given(empCntryPairService)
                .softDeleteChild(eq("EP0001"), eq(1), any(IcasUser.class));

        mockMvc.perform(delete("/api/emp/plan/EP0001/cntry-pair/1")
                        .with(authentication(authToken(airlineUserOP0001))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("DELETE — Service NOT_FOUND → 404")
    void delete_NOT_FOUND_404() throws Exception {
        willThrow(BusinessException.notFound("운항 국가 쌍"))
                .given(empCntryPairService).softDeleteChild(eq("EP0001"), eq(99), any(IcasUser.class));

        mockMvc.perform(delete("/api/emp/plan/EP0001/cntry-pair/99")
                        .with(authentication(authToken(airlineUserOP0001))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }
}
