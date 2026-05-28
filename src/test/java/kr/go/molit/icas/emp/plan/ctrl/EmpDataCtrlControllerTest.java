package kr.go.molit.icas.emp.plan.ctrl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.exception.GlobalExceptionHandler;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.emp.plan.ctrl.domain.EmpDataCtrlVO;
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
 * EmpDataCtrlController 슬라이스 테스트.
 *
 * Spring Boot 없이 순수 MockMvcBuilders.standaloneSetup 사용.
 * SecurityMockMvcRequestPostProcessors.authentication() 으로 IcasUser 컨텍스트 주입.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EmpDataCtrlController 슬라이스 테스트")
class EmpDataCtrlControllerTest {

    MockMvc mockMvc;

    @Mock
    EmpDataCtrlService empDataCtrlService;

    ObjectMapper objectMapper;

    // ── fixture ──
    IcasUser molitUser;
    IcasUser airlineUserOP0001;
    IcasUser airlineUserOP0002;

    @BeforeEach
    void setUp() {
        EmpDataCtrlController controller = new EmpDataCtrlController(empDataCtrlService);
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
    }

    private UsernamePasswordAuthenticationToken authToken(IcasUser user) {
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }

    private EmpDataCtrlVO sampleDataCtrl(String empPlanId) {
        EmpDataCtrlVO vo = new EmpDataCtrlVO();
        vo.setEmpPlanId(empPlanId);
        vo.setFlowDesc("연료 소비 데이터 흐름 기술");
        vo.setGapThrshld5pct("Y");
        return vo;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/emp/plan/{empPlanId}/data-ctrl — 단건 조회
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET — 정상 조회 200 응답, $.data.empPlanId / $.data.gapThrshld5pct 검증")
    void get_정상조회_200() throws Exception {
        EmpDataCtrlVO ctrl = sampleDataCtrl("EP0001");
        given(empDataCtrlService.selectByPlanId(eq("EP0001"), any(IcasUser.class))).willReturn(ctrl);

        mockMvc.perform(get("/api/emp/plan/EP0001/data-ctrl")
                        .with(authentication(authToken(molitUser)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.empPlanId").value("EP0001"))
                .andExpect(jsonPath("$.data.gapThrshld5pct").value("Y"));
    }

    @Test
    @DisplayName("GET — 부모 plan 미존재 → 404 응답")
    void get_부모plan_미존재_404() throws Exception {
        given(empDataCtrlService.selectByPlanId(eq("EP9999"), any(IcasUser.class)))
                .willThrow(BusinessException.notFound("EMP Plan"));

        mockMvc.perform(get("/api/emp/plan/EP9999/data-ctrl")
                        .with(authentication(authToken(molitUser)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    @DisplayName("GET — 가시범위 위반 → 403 응답")
    void get_가시범위_위반_403() throws Exception {
        given(empDataCtrlService.selectByPlanId(eq("EP0001"), any(IcasUser.class)))
                .willThrow(BusinessException.forbidden("본인 항공사 데이터만 접근할 수 있습니다."));

        mockMvc.perform(get("/api/emp/plan/EP0001/data-ctrl")
                        .with(authentication(authToken(airlineUserOP0002)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    @DisplayName("GET — 자식 미작성 → data=null, success=true (200)")
    void get_자식_미작성_null_200() throws Exception {
        given(empDataCtrlService.selectByPlanId(eq("EP0001"), any(IcasUser.class))).willReturn(null);

        mockMvc.perform(get("/api/emp/plan/EP0001/data-ctrl")
                        .with(authentication(authToken(molitUser)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUT /api/emp/plan/{empPlanId}/data-ctrl — Upsert
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("PUT — 정상 Upsert 200 응답")
    void put_정상_upsert_200() throws Exception {
        EmpDataCtrlVO reqVo = sampleDataCtrl("EP0001");
        willDoNothing().given(empDataCtrlService)
                .upsertDataCtrl(eq("EP0001"), any(EmpDataCtrlVO.class), any(IcasUser.class));

        mockMvc.perform(put("/api/emp/plan/EP0001/data-ctrl")
                        .with(authentication(authToken(airlineUserOP0001)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqVo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("PUT — gapThrshld5pct 허용 외 값 → BAD_REQUEST(400) 응답")
    void put_gapThrshld5pct_허용외_400() throws Exception {
        EmpDataCtrlVO reqVo = sampleDataCtrl("EP0001");
        reqVo.setGapThrshld5pct("X");
        willThrow(BusinessException.badRequest("5% 격차 임계값 적용 여부(gapThrshld5pct)는 Y 또는 N 만 허용됩니다."))
                .given(empDataCtrlService)
                .upsertDataCtrl(eq("EP0001"), any(EmpDataCtrlVO.class), any(IcasUser.class));

        mockMvc.perform(put("/api/emp/plan/EP0001/data-ctrl")
                        .with(authentication(authToken(airlineUserOP0001)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqVo)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    @Test
    @DisplayName("PUT — 부모 plan DRAFT 아님 → CONFLICT(409) 응답")
    void put_부모plan_DRAFT_아님_409() throws Exception {
        EmpDataCtrlVO reqVo = sampleDataCtrl("EP0001");
        willThrow(BusinessException.conflict("DRAFT 상태에서만 자식 정보를 수정할 수 있습니다."))
                .given(empDataCtrlService)
                .upsertDataCtrl(eq("EP0001"), any(EmpDataCtrlVO.class), any(IcasUser.class));

        mockMvc.perform(put("/api/emp/plan/EP0001/data-ctrl")
                        .with(authentication(authToken(airlineUserOP0001)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqVo)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"));
    }

    @Test
    @DisplayName("PUT — 권한 위반(FORBIDDEN) → 403 응답")
    void put_권한_위반_403() throws Exception {
        EmpDataCtrlVO reqVo = sampleDataCtrl("EP0001");
        willThrow(BusinessException.forbidden("본인 항공사 데이터만 등록·수정할 수 있습니다."))
                .given(empDataCtrlService)
                .upsertDataCtrl(eq("EP0001"), any(EmpDataCtrlVO.class), any(IcasUser.class));

        mockMvc.perform(put("/api/emp/plan/EP0001/data-ctrl")
                        .with(authentication(authToken(airlineUserOP0002)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqVo)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE /api/emp/plan/{empPlanId}/data-ctrl — 소프트삭제
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("DELETE — 정상 소프트삭제 200 응답")
    void delete_정상삭제_200() throws Exception {
        willDoNothing().given(empDataCtrlService)
                .softDeleteByPlanId(eq("EP0001"), any(IcasUser.class));

        mockMvc.perform(delete("/api/emp/plan/EP0001/data-ctrl")
                        .with(authentication(authToken(airlineUserOP0001))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("DELETE — 부모 plan DRAFT 아님 → CONFLICT(409) 응답")
    void delete_부모plan_DRAFT_아님_409() throws Exception {
        willThrow(BusinessException.conflict("DRAFT 상태에서만 자식 정보를 수정할 수 있습니다."))
                .given(empDataCtrlService).softDeleteByPlanId(eq("EP0001"), any(IcasUser.class));

        mockMvc.perform(delete("/api/emp/plan/EP0001/data-ctrl")
                        .with(authentication(authToken(airlineUserOP0001))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"));
    }
}
