package kr.go.molit.icas.com.atrz;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import kr.go.molit.icas.com.atrz.domain.AtrzDmndSearch;
import kr.go.molit.icas.com.atrz.domain.AtrzDmndVO;
import kr.go.molit.icas.com.atrz.domain.AtrzPrcsVO;
import kr.go.molit.icas.common.dto.PageResponse;
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
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AtrzController 슬라이스 테스트.
 *
 * Spring Boot 없이 순수 MockMvcBuilders.standaloneSetup 사용.
 * SecurityMockMvcRequestPostProcessors.authentication() 으로 IcasUser 컨텍스트 주입.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AtrzController 슬라이스 테스트")
class AtrzControllerTest {

    MockMvc mockMvc;

    @Mock
    AtrzService atrzService;

    ObjectMapper objectMapper;

    // ── fixture ──
    IcasUser molitUser;
    IcasUser requesterUser;
    IcasUser approver1User;
    IcasUser otherUser;

    @BeforeEach
    void setUp() {
        AtrzController controller = new AtrzController(atrzService);
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

        requesterUser = IcasUser.builder()
                .userId("requester01").userNm("결재 요청자")
                .ognzSeCd("AIRLINE").ognzId("ORG_AIR01").oprtrId("OP0001").master(false)
                .roleIds(List.of("AIRLINE_USER")).build();

        approver1User = IcasUser.builder()
                .userId("approver01").userNm("1단계 결재자")
                .ognzSeCd("MOLIT").ognzId("ORG_MOLIT").master(false)
                .roleIds(List.of("TEAM_LEAD")).build();

        otherUser = IcasUser.builder()
                .userId("other01").userNm("무관한 사용자")
                .ognzSeCd("AIRLINE").ognzId("ORG_AIR02").oprtrId("OP0002").master(false)
                .roleIds(List.of("AIRLINE_USER")).build();
    }

    private UsernamePasswordAuthenticationToken authToken(IcasUser user) {
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }

    private AtrzDmndVO sampleDmnd() {
        AtrzDmndVO dmnd = new AtrzDmndVO();
        dmnd.setAtrzDmndId("AD0001");
        dmnd.setAtrzTaskId("ATZ_EMP_PLAN");
        dmnd.setDmndUserId("requester01");
        dmnd.setAtrzStCd("PEND");
        dmnd.setTitle("2026년 고용계획 결재 요청");
        return dmnd;
    }

    private AtrzPrcsVO samplePrcs() {
        AtrzPrcsVO prcs = new AtrzPrcsVO();
        prcs.setAtrzDmndId("AD0001");
        prcs.setAtrzSeq(1);
        prcs.setAtrzUserId("approver01");
        prcs.setAtrzRoleCd("TEAM_LEAD");
        return prcs;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/com/atrz — 결재 요청 검색
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("결재 요청 검색 — MOLIT 사용자 정상 검색 200 반환")
    void searchDmnds_MOLIT_정상검색_200() throws Exception {
        PageResponse<AtrzDmndVO> page = new PageResponse<>(List.of(sampleDmnd()), 1, 20, 1L);
        given(atrzService.searchDmnds(any(AtrzDmndSearch.class), any(IcasUser.class))).willReturn(page);

        mockMvc.perform(get("/api/com/atrz")
                        .with(authentication(authToken(molitUser)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.rows[0].atrzDmndId").value("AD0001"));
    }

    @Test
    @DisplayName("결재 요청 검색 — AIRLINE 사용자 본인 요청만 검색 200 반환")
    void searchDmnds_AIRLINE_본인검색_200() throws Exception {
        PageResponse<AtrzDmndVO> page = new PageResponse<>(List.of(sampleDmnd()), 1, 20, 1L);
        given(atrzService.searchDmnds(any(AtrzDmndSearch.class), any(IcasUser.class))).willReturn(page);

        mockMvc.perform(get("/api/com/atrz")
                        .param("atrzStCd", "PEND")
                        .with(authentication(authToken(requesterUser)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/com/atrz/my-pending
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("my-pending GET — 내 결재 대기 목록 200 반환")
    void myPending_정상조회_200() throws Exception {
        given(atrzService.selectMyPending(any(IcasUser.class))).willReturn(List.of(samplePrcs()));

        mockMvc.perform(get("/api/com/atrz/my-pending")
                        .with(authentication(authToken(approver1User)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].atrzUserId").value("approver01"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/com/atrz/{dmndId}
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("단건 조회 — 요청자 본인 조회 200 반환 (dmnd + prcs 목록 포함)")
    void getDmnd_요청자본인_200() throws Exception {
        AtrzService.DmndDetail detail = new AtrzService.DmndDetail(sampleDmnd(), List.of(samplePrcs()));
        given(atrzService.getDmnd(eq("AD0001"), any(IcasUser.class))).willReturn(detail);

        mockMvc.perform(get("/api/com/atrz/AD0001")
                        .with(authentication(authToken(requesterUser)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.dmnd.atrzDmndId").value("AD0001"))
                .andExpect(jsonPath("$.data.prcsList").isArray())
                .andExpect(jsonPath("$.data.prcsList[0].atrzSeq").value(1));
    }

    @Test
    @DisplayName("단건 조회 — 권한 없는 사용자 접근 시 Service FORBIDDEN → 403 반환")
    void getDmnd_권한없음_FORBIDDEN_403() throws Exception {
        given(atrzService.getDmnd(eq("AD0001"), any(IcasUser.class)))
                .willThrow(BusinessException.forbidden("해당 결재 요청에 접근할 권한이 없습니다."));

        mockMvc.perform(get("/api/com/atrz/AD0001")
                        .with(authentication(authToken(otherUser)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    @DisplayName("단건 조회 — 미존재 dmndId 시 Service NOT_FOUND → 404 반환")
    void getDmnd_미존재ID_NOT_FOUND_404() throws Exception {
        given(atrzService.getDmnd(eq("AD9999"), any(IcasUser.class)))
                .willThrow(BusinessException.notFound("결재 요청"));

        mockMvc.perform(get("/api/com/atrz/AD9999")
                        .with(authentication(authToken(molitUser)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/com/atrz — 결재 요청 제출
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("submit POST — 결재자 2명 정상 제출 200 반환")
    void submit_정상제출_200() throws Exception {
        given(atrzService.submit(any(AtrzService.SubmitRequest.class), any(IcasUser.class)))
                .willReturn(sampleDmnd());

        Map<String, Object> reqBody = Map.of(
                "atrzTaskId", "ATZ_EMP_PLAN",
                "rfrncTblNm", "emp.tn_emp_plan",
                "rfrncKeyCn", "{\"empPlanId\":\"EP0001\"}",
                "title", "2026년 고용계획 결재 요청",
                "contents", "상세 내용",
                "approvers", List.of(
                        Map.of("atrzUserId", "approver01", "atrzRoleCd", "TEAM_LEAD"),
                        Map.of("atrzUserId", "approver02", "atrzRoleCd", "DEPT_HEAD")
                )
        );

        mockMvc.perform(post("/api/com/atrz")
                        .with(authentication(authToken(requesterUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("결재 요청이 제출되었습니다."))
                .andExpect(jsonPath("$.data.atrzDmndId").value("AD0001"));
    }

    @Test
    @DisplayName("submit POST — 결재자 0명 시 Service BAD_REQUEST → 400 반환")
    void submit_결재자0명_BAD_REQUEST_400() throws Exception {
        given(atrzService.submit(any(AtrzService.SubmitRequest.class), any(IcasUser.class)))
                .willThrow(BusinessException.badRequest("결재자는 1명 이상이어야 합니다."));

        Map<String, Object> reqBody = Map.of(
                "atrzTaskId", "ATZ_EMP_PLAN",
                "title", "테스트",
                "approvers", List.of()
        );

        mockMvc.perform(post("/api/com/atrz")
                        .with(authentication(authToken(requesterUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/com/atrz/{dmndId}/approve
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("approve POST — 정상 승인 200 반환")
    void approve_정상승인_200() throws Exception {
        willDoNothing().given(atrzService).approve(eq("AD0001"), eq(1), any(), any(IcasUser.class));

        Map<String, Object> reqBody = Map.of("atrzSeq", 1, "atrzOpnn", "검토 완료");

        mockMvc.perform(post("/api/com/atrz/AD0001/approve")
                        .with(authentication(authToken(approver1User)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("승인 처리되었습니다."));
    }

    @Test
    @DisplayName("approve POST — 결재자 본인 아님 Service FORBIDDEN → 403 반환")
    void approve_결재자아님_FORBIDDEN_403() throws Exception {
        willThrow(BusinessException.forbidden("해당 결재 단계의 결재자가 아닙니다."))
                .given(atrzService).approve(eq("AD0001"), eq(1), any(), any(IcasUser.class));

        Map<String, Object> reqBody = Map.of("atrzSeq", 1, "atrzOpnn", "승인");

        mockMvc.perform(post("/api/com/atrz/AD0001/approve")
                        .with(authentication(authToken(otherUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqBody)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    @DisplayName("approve POST — 이미 처리된 단계 Service CONFLICT → 409 반환")
    void approve_이미처리됨_CONFLICT_409() throws Exception {
        willThrow(BusinessException.conflict("이미 처리된 결재 단계입니다."))
                .given(atrzService).approve(eq("AD0001"), eq(1), any(), any(IcasUser.class));

        Map<String, Object> reqBody = Map.of("atrzSeq", 1, "atrzOpnn", "재승인");

        mockMvc.perform(post("/api/com/atrz/AD0001/approve")
                        .with(authentication(authToken(approver1User)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqBody)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/com/atrz/{dmndId}/reject
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("reject POST — 정상 반려 200 반환")
    void reject_정상반려_200() throws Exception {
        willDoNothing().given(atrzService).reject(eq("AD0001"), eq(1), any(), any(IcasUser.class));

        Map<String, Object> reqBody = Map.of("atrzSeq", 1, "atrzOpnn", "반려 사유");

        mockMvc.perform(post("/api/com/atrz/AD0001/reject")
                        .with(authentication(authToken(approver1User)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("반려 처리되었습니다."));
    }

    @Test
    @DisplayName("reject POST — 결재자 본인 아님 Service FORBIDDEN → 403 반환")
    void reject_결재자아님_FORBIDDEN_403() throws Exception {
        willThrow(BusinessException.forbidden("해당 결재 단계의 결재자가 아닙니다."))
                .given(atrzService).reject(eq("AD0001"), eq(1), any(), any(IcasUser.class));

        Map<String, Object> reqBody = Map.of("atrzSeq", 1, "atrzOpnn", "반려");

        mockMvc.perform(post("/api/com/atrz/AD0001/reject")
                        .with(authentication(authToken(otherUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqBody)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/com/atrz/{dmndId}/cancel
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("cancel POST — 요청자 본인 정상 취소 200 반환")
    void cancel_정상취소_200() throws Exception {
        willDoNothing().given(atrzService).cancel(eq("AD0001"), any(IcasUser.class));

        mockMvc.perform(post("/api/com/atrz/AD0001/cancel")
                        .with(authentication(authToken(requesterUser))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("결재 요청이 취소되었습니다."));
    }

    @Test
    @DisplayName("cancel POST — 다른 사람 취소 시도 Service FORBIDDEN → 403 반환")
    void cancel_타인시도_FORBIDDEN_403() throws Exception {
        willThrow(BusinessException.forbidden("결재 요청자 본인만 취소할 수 있습니다."))
                .given(atrzService).cancel(eq("AD0001"), any(IcasUser.class));

        mockMvc.perform(post("/api/com/atrz/AD0001/cancel")
                        .with(authentication(authToken(otherUser))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    @DisplayName("cancel POST — 이미 종결된 요청 취소 시 Service CONFLICT → 409 반환")
    void cancel_종결된요청_CONFLICT_409() throws Exception {
        willThrow(BusinessException.conflict("이미 종결된 결재 요청은 취소할 수 없습니다. 현재 상태: APRVD"))
                .given(atrzService).cancel(eq("AD0001"), any(IcasUser.class));

        mockMvc.perform(post("/api/com/atrz/AD0001/cancel")
                        .with(authentication(authToken(requesterUser))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"));
    }
}
