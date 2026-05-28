package kr.go.molit.icas.emp.plan;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import kr.go.molit.icas.common.dto.PageResponse;
import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.exception.GlobalExceptionHandler;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.emp.plan.domain.EmpChgHstryVO;
import kr.go.molit.icas.emp.plan.domain.EmpPlanVO;
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
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * EmpPlanController 슬라이스 테스트.
 *
 * Spring Boot 없이 순수 MockMvcBuilders.standaloneSetup 사용.
 * SecurityMockMvcRequestPostProcessors.authentication() 으로 IcasUser 컨텍스트를 주입한다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EmpPlanController 슬라이스 테스트")
class EmpPlanControllerTest {

    MockMvc mockMvc;

    @Mock
    EmpPlanService empPlanService;

    ObjectMapper objectMapper;

    // ── fixture ──
    IcasUser molitUser;
    IcasUser kotsaUser;
    IcasUser airlineUserOP0001;
    IcasUser airlineUserOP0002;

    @BeforeEach
    void setUp() {
        EmpPlanController controller = new EmpPlanController(empPlanService);
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

        airlineUserOP0002 = IcasUser.builder()
                .userId("airline02").userNm("항공사B 담당자")
                .ognzSeCd("AIRLINE").ognzId("ORG_AIR02").oprtrId("OP0002").master(false)
                .roleIds(List.of("AIRLINE_USER")).build();
    }

    private UsernamePasswordAuthenticationToken authToken(IcasUser user) {
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }

    private EmpPlanVO samplePlan(String empPlanId, String oprtrId, String empStCd) {
        EmpPlanVO vo = new EmpPlanVO();
        vo.setEmpPlanId(empPlanId);
        vo.setOprtrId(oprtrId);
        vo.setEmpStCd(empStCd);
        vo.setEmpVer("1.0");
        vo.setRprtYr("2026");
        vo.setSigChgYn("N");
        return vo;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/emp/plan — 목록 조회
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("목록 조회 — MOLIT 사용자 정상 200 응답, data.rows 배열 포함")
    void list_MOLIT_정상조회_200() throws Exception {
        PageResponse<EmpPlanVO> page = new PageResponse<>(
                List.of(samplePlan("EP0001", "OP0001", "DRAFT")), 1, 20, 1L);
        given(empPlanService.searchEmpPlans(any(), any(IcasUser.class))).willReturn(page);

        mockMvc.perform(get("/api/emp/plan")
                        .with(authentication(authToken(molitUser)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.rows").isArray())
                .andExpect(jsonPath("$.data.rows[0].empPlanId").value("EP0001"))
                .andExpect(jsonPath("$.data.total").value(1));

        then(empPlanService).should().searchEmpPlans(any(), any(IcasUser.class));
    }

    @Test
    @DisplayName("목록 조회 — Service 가 FORBIDDEN 던지면 403 응답")
    void list_Service_FORBIDDEN_403() throws Exception {
        given(empPlanService.searchEmpPlans(any(), any(IcasUser.class)))
                .willThrow(BusinessException.forbidden("EMP Plan 조회 권한이 없습니다."));

        mockMvc.perform(get("/api/emp/plan")
                        .with(authentication(authToken(airlineUserOP0001)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/emp/plan/{id} — 단건 조회
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("단건 조회 — 정상 200 응답, $.data.empPlanId / $.data.empStCd 검증")
    void get_단건조회_정상_200() throws Exception {
        EmpPlanVO plan = samplePlan("EP0001", "OP0001", "DRAFT");
        given(empPlanService.getEmpPlan(eq("EP0001"), any(IcasUser.class))).willReturn(plan);

        mockMvc.perform(get("/api/emp/plan/EP0001")
                        .with(authentication(authToken(molitUser)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.empPlanId").value("EP0001"))
                .andExpect(jsonPath("$.data.empStCd").value("DRAFT"));
    }

    @Test
    @DisplayName("단건 조회 — Service NOT_FOUND 시 404 응답")
    void get_Service_NOT_FOUND_404() throws Exception {
        given(empPlanService.getEmpPlan(eq("EP9999"), any(IcasUser.class)))
                .willThrow(BusinessException.notFound("EMP Plan"));

        mockMvc.perform(get("/api/emp/plan/EP9999")
                        .with(authentication(authToken(molitUser)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    @DisplayName("단건 조회 — AIRLINE 이 타 운영사 plan 조회 시 Service FORBIDDEN → 403 응답")
    void get_AIRLINE_타운영사조회_403() throws Exception {
        given(empPlanService.getEmpPlan(eq("EP0001"), any(IcasUser.class)))
                .willThrow(BusinessException.forbidden("본인 항공사 데이터만 접근할 수 있습니다."));

        mockMvc.perform(get("/api/emp/plan/EP0001")
                        .with(authentication(authToken(airlineUserOP0002)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/emp/plan/{id}/history — 이력 조회
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("이력 조회 — 정상 200 응답, data 배열 반환")
    void history_정상조회_200() throws Exception {
        EmpChgHstryVO hstry = new EmpChgHstryVO();
        hstry.setChgHstryId(1L);
        hstry.setEmpPlanId("EP0001");
        hstry.setChgChptr("MASTER");
        hstry.setChgCn("{\"action\":\"신규등록\"}");

        given(empPlanService.getEmpPlanHistory(eq("EP0001"), any(IcasUser.class)))
                .willReturn(List.of(hstry));

        mockMvc.perform(get("/api/emp/plan/EP0001/history")
                        .with(authentication(authToken(molitUser)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].empPlanId").value("EP0001"))
                .andExpect(jsonPath("$.data[0].chgChptr").value("MASTER"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/emp/plan — 신규 등록
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("신규 등록 — AIRLINE 정상 등록 200 응답, $.data.empPlanId / $.message 검증")
    void create_AIRLINE_정상등록_200() throws Exception {
        EmpPlanVO reqVo = new EmpPlanVO();
        reqVo.setOprtrId("OP0001");
        reqVo.setRprtYr("2026");

        EmpPlanVO created = samplePlan("EP0001", "OP0001", "DRAFT");
        given(empPlanService.createEmpPlan(any(EmpPlanVO.class), any(IcasUser.class))).willReturn(created);

        mockMvc.perform(post("/api/emp/plan")
                        .with(authentication(authToken(airlineUserOP0001)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqVo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("EMP Plan 이 등록되었습니다."))
                .andExpect(jsonPath("$.data.empPlanId").value("EP0001"))
                .andExpect(jsonPath("$.data.empStCd").value("DRAFT"));
    }

    @Test
    @DisplayName("신규 등록 — Service FORBIDDEN 시 403 응답")
    void create_Service_FORBIDDEN_403() throws Exception {
        EmpPlanVO reqVo = new EmpPlanVO();
        reqVo.setOprtrId("OP0001");
        reqVo.setRprtYr("2026");

        given(empPlanService.createEmpPlan(any(EmpPlanVO.class), any(IcasUser.class)))
                .willThrow(BusinessException.forbidden("항공사 사용자만 수행할 수 있는 작업입니다."));

        mockMvc.perform(post("/api/emp/plan")
                        .with(authentication(authToken(molitUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqVo)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/emp/plan/{id}/submit — 제출
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("제출 — AIRLINE 정상 제출 200 응답")
    void submit_AIRLINE_정상제출_200() throws Exception {
        willDoNothing().given(empPlanService).submit(eq("EP0001"), any(IcasUser.class));

        mockMvc.perform(post("/api/emp/plan/EP0001/submit")
                        .with(authentication(authToken(airlineUserOP0001))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("EMP Plan 이 제출되었습니다."));
    }

    @Test
    @DisplayName("제출 — Service BAD_REQUEST 시 400 응답 (DRAFT 아닌 상태)")
    void submit_Service_BAD_REQUEST_400() throws Exception {
        willThrow(BusinessException.badRequest("DRAFT 상태에서만 가능한 작업입니다. 현재 상태: SBMTD"))
                .given(empPlanService).submit(eq("EP0001"), any(IcasUser.class));

        mockMvc.perform(post("/api/emp/plan/EP0001/submit")
                        .with(authentication(authToken(airlineUserOP0001))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/emp/plan/{id}/reject — 반려 (사유 필수)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("반려 — KOTSA 사유 포함 정상 반려 200 응답")
    void reject_KOTSA_정상반려_200() throws Exception {
        willDoNothing().given(empPlanService).reject(eq("EP0001"), eq("AOC 갱신 필요"), any(IcasUser.class));

        Map<String, String> body = Map.of("reason", "AOC 갱신 필요");

        mockMvc.perform(post("/api/emp/plan/EP0001/reject")
                        .with(authentication(authToken(kotsaUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("EMP Plan 이 반려되었습니다."));
    }

    @Test
    @DisplayName("반려 — reason 미포함(빈 문자열) 시 Service BAD_REQUEST → 400 응답")
    void reject_reason_누락_400() throws Exception {
        willThrow(BusinessException.badRequest("반려 사유는 필수입니다."))
                .given(empPlanService).reject(eq("EP0001"), eq(""), any(IcasUser.class));

        Map<String, String> body = Map.of("reason", "");

        mockMvc.perform(post("/api/emp/plan/EP0001/reject")
                        .with(authentication(authToken(kotsaUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/emp/plan/{id}/approve — 승인
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("승인 — MOLIT 정상 승인 200 응답")
    void approve_MOLIT_정상승인_200() throws Exception {
        willDoNothing().given(empPlanService).approve(eq("EP0001"), any(IcasUser.class));

        mockMvc.perform(post("/api/emp/plan/EP0001/approve")
                        .with(authentication(authToken(molitUser))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("EMP Plan 이 승인되었습니다."));
    }

    @Test
    @DisplayName("승인 — Service FORBIDDEN 시 403 응답 (MOLIT 아닌 사용자)")
    void approve_Service_FORBIDDEN_403() throws Exception {
        willThrow(BusinessException.forbidden("국토부(MOLIT) 사용자만 수행할 수 있는 작업입니다."))
                .given(empPlanService).approve(eq("EP0001"), any(IcasUser.class));

        mockMvc.perform(post("/api/emp/plan/EP0001/approve")
                        .with(authentication(authToken(kotsaUser))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/emp/plan/{id}/cancel — 취소 (사유 필수)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("취소 — MOLIT 사유 포함 정상 취소 200 응답")
    void cancel_MOLIT_정상취소_200() throws Exception {
        willDoNothing().given(empPlanService).cancel(eq("EP0001"), eq("운영 종료"), any(IcasUser.class));

        Map<String, String> body = Map.of("reason", "운영 종료");

        mockMvc.perform(post("/api/emp/plan/EP0001/cancel")
                        .with(authentication(authToken(molitUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("EMP Plan 이 취소되었습니다."));
    }

    @Test
    @DisplayName("취소 — Service CONFLICT 시 409 응답 (APRVD 아닌 상태)")
    void cancel_Service_CONFLICT_409() throws Exception {
        willThrow(BusinessException.conflict("취소 처리에 실패했습니다. 현재 상태를 확인하세요."))
                .given(empPlanService).cancel(eq("EP0001"), eq("운영 종료"), any(IcasUser.class));

        Map<String, String> body = Map.of("reason", "운영 종료");

        mockMvc.perform(post("/api/emp/plan/EP0001/cancel")
                        .with(authentication(authToken(molitUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/emp/plan/{id}/new-version — 신버전 생성
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("신버전 생성 — AIRLINE 정상 200 응답, $.data.empPlanId 검증")
    void newVersion_AIRLINE_정상생성_200() throws Exception {
        EmpPlanVO newPlan = samplePlan("EP0002", "OP0001", "DRAFT");
        newPlan.setEmpVer("2.0");
        newPlan.setPrevEmpPlanId("EP0001");

        given(empPlanService.createNewVersion(eq("EP0001"), any(IcasUser.class))).willReturn(newPlan);

        mockMvc.perform(post("/api/emp/plan/EP0001/new-version")
                        .with(authentication(authToken(airlineUserOP0001))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("EMP Plan 신버전이 생성되었습니다."))
                .andExpect(jsonPath("$.data.empPlanId").value("EP0002"))
                .andExpect(jsonPath("$.data.empStCd").value("DRAFT"));
    }

    @Test
    @DisplayName("신버전 생성 — Service CONFLICT 시 409 응답 (진행 중 plan 존재)")
    void newVersion_Service_CONFLICT_409() throws Exception {
        willThrow(BusinessException.conflict("이미 진행 중인 EMP Plan 이 존재합니다."))
                .given(empPlanService).createNewVersion(eq("EP0001"), any(IcasUser.class));

        mockMvc.perform(post("/api/emp/plan/EP0001/new-version")
                        .with(authentication(authToken(airlineUserOP0001))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"));
    }
}
