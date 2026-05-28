package kr.go.molit.icas.er.rprt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import kr.go.molit.icas.common.dto.PageResponse;
import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.exception.GlobalExceptionHandler;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.er.rprt.domain.ErVO;
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
 * ErController 슬라이스 테스트.
 * Spring Boot 없이 순수 MockMvcBuilders.standaloneSetup 사용.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ErController 슬라이스 테스트")
class ErControllerTest {

    MockMvc mockMvc;

    @Mock
    ErService erService;

    ObjectMapper objectMapper;

    // ── fixture ──
    IcasUser molitUser;
    IcasUser kotsaUser;
    IcasUser airlineUserOP0001;
    IcasUser airlineUserOP0002;

    @BeforeEach
    void setUp() {
        ErController controller = new ErController(erService);
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

    private ErVO sampleEr(String erId, String oprtrId, String erStCd) {
        ErVO vo = new ErVO();
        vo.setErId(erId);
        vo.setOprtrId(oprtrId);
        vo.setRprtYr("2026");
        vo.setErVer("1.0");
        vo.setErStCd(erStCd);
        return vo;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/er/rprt — 목록 조회
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("목록 조회 — MOLIT 사용자 정상 200 응답, data.rows 배열 포함")
    void list_MOLIT_정상조회_200() throws Exception {
        PageResponse<ErVO> page = new PageResponse<>(
                List.of(sampleEr("ER0001", "OP0001", "DRAFT")), 1, 20, 1L);
        given(erService.searchErs(any(), any(IcasUser.class))).willReturn(page);

        mockMvc.perform(get("/api/er/rprt")
                        .with(authentication(authToken(molitUser)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.rows").isArray())
                .andExpect(jsonPath("$.data.rows[0].erId").value("ER0001"))
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    @DisplayName("목록 조회 — Service FORBIDDEN 시 403 응답")
    void list_Service_FORBIDDEN_403() throws Exception {
        given(erService.searchErs(any(), any(IcasUser.class)))
                .willThrow(BusinessException.forbidden("ER 조회 권한이 없습니다."));

        mockMvc.perform(get("/api/er/rprt")
                        .with(authentication(authToken(airlineUserOP0001)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/er/rprt/{erId} — 단건 조회
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("단건 조회 — 정상 200 응답, $.data.erId / $.data.erStCd 검증")
    void get_단건조회_정상_200() throws Exception {
        given(erService.getEr(eq("ER0001"), any(IcasUser.class)))
                .willReturn(sampleEr("ER0001", "OP0001", "DRAFT"));

        mockMvc.perform(get("/api/er/rprt/ER0001")
                        .with(authentication(authToken(molitUser)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.erId").value("ER0001"))
                .andExpect(jsonPath("$.data.erStCd").value("DRAFT"));
    }

    @Test
    @DisplayName("단건 조회 — Service NOT_FOUND 시 404 응답")
    void get_Service_NOT_FOUND_404() throws Exception {
        given(erService.getEr(eq("ER9999"), any(IcasUser.class)))
                .willThrow(BusinessException.notFound("ER"));

        mockMvc.perform(get("/api/er/rprt/ER9999")
                        .with(authentication(authToken(molitUser)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    @DisplayName("단건 조회 — AIRLINE 이 타 운영사 ER 조회 시 FORBIDDEN → 403 응답")
    void get_AIRLINE_타운영사조회_403() throws Exception {
        given(erService.getEr(eq("ER0001"), any(IcasUser.class)))
                .willThrow(BusinessException.forbidden("본인 항공사 데이터만 접근할 수 있습니다."));

        mockMvc.perform(get("/api/er/rprt/ER0001")
                        .with(authentication(authToken(airlineUserOP0002)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/er/rprt — 신규 등록
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("신규 등록 — AIRLINE 정상 등록 200 응답, $.data.erId / $.message 검증")
    void create_AIRLINE_정상등록_200() throws Exception {
        ErVO reqVo = new ErVO();
        reqVo.setOprtrId("OP0001");
        reqVo.setRprtYr("2026");

        given(erService.createEr(any(ErVO.class), any(IcasUser.class)))
                .willReturn(sampleEr("ER0001", "OP0001", "DRAFT"));

        mockMvc.perform(post("/api/er/rprt")
                        .with(authentication(authToken(airlineUserOP0001)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqVo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("ER 이 등록되었습니다."))
                .andExpect(jsonPath("$.data.erId").value("ER0001"))
                .andExpect(jsonPath("$.data.erStCd").value("DRAFT"));
    }

    @Test
    @DisplayName("신규 등록 — Service FORBIDDEN 시 403 응답")
    void create_Service_FORBIDDEN_403() throws Exception {
        ErVO reqVo = new ErVO();
        reqVo.setOprtrId("OP0001");
        reqVo.setRprtYr("2026");

        given(erService.createEr(any(ErVO.class), any(IcasUser.class)))
                .willThrow(BusinessException.forbidden("항공사 사용자만 수행할 수 있는 작업입니다."));

        mockMvc.perform(post("/api/er/rprt")
                        .with(authentication(authToken(molitUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqVo)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    @DisplayName("신규 등록 — Service BAD_REQUEST 시 400 응답 (rprtYr 형식 오류)")
    void create_Service_BAD_REQUEST_400() throws Exception {
        ErVO reqVo = new ErVO();
        reqVo.setOprtrId("OP0001");
        reqVo.setRprtYr("26");

        given(erService.createEr(any(ErVO.class), any(IcasUser.class)))
                .willThrow(BusinessException.badRequest("보고연도는 4자리 숫자 형식이어야 합니다."));

        mockMvc.perform(post("/api/er/rprt")
                        .with(authentication(authToken(airlineUserOP0001)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqVo)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUT /api/er/rprt/{erId} — DRAFT 수정
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("수정 — AIRLINE 정상 수정 200 응답, $.message 검증")
    void update_AIRLINE_정상수정_200() throws Exception {
        ErVO reqVo = new ErVO();
        reqVo.setRprtYr("2026");

        willDoNothing().given(erService).updateEr(eq("ER0001"), any(ErVO.class), any(IcasUser.class));

        mockMvc.perform(put("/api/er/rprt/ER0001")
                        .with(authentication(authToken(airlineUserOP0001)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqVo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("ER 이 수정되었습니다."));
    }

    @Test
    @DisplayName("수정 — Service CONFLICT 시 409 응답 (DRAFT 아닌 상태)")
    void update_Service_CONFLICT_409() throws Exception {
        ErVO reqVo = new ErVO();
        reqVo.setRprtYr("2026");

        willThrow(BusinessException.conflict("수정 대상 ER 이 DRAFT 상태가 아니거나 존재하지 않습니다."))
                .given(erService).updateEr(eq("ER0001"), any(ErVO.class), any(IcasUser.class));

        mockMvc.perform(put("/api/er/rprt/ER0001")
                        .with(authentication(authToken(airlineUserOP0001)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqVo)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE /api/er/rprt/{erId} — 소프트삭제
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("소프트삭제 — AIRLINE DRAFT 정상 삭제 200 응답")
    void delete_AIRLINE_정상삭제_200() throws Exception {
        willDoNothing().given(erService).softDeleteEr(eq("ER0001"), any(IcasUser.class));

        mockMvc.perform(delete("/api/er/rprt/ER0001")
                        .with(authentication(authToken(airlineUserOP0001))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("ER 이 삭제되었습니다."));
    }

    @Test
    @DisplayName("소프트삭제 — Service CONFLICT 시 409 응답 (DRAFT 아닌 상태)")
    void delete_Service_CONFLICT_409() throws Exception {
        willThrow(BusinessException.conflict("삭제 대상 ER 이 DRAFT 상태가 아니거나 존재하지 않습니다."))
                .given(erService).softDeleteEr(eq("ER0001"), any(IcasUser.class));

        mockMvc.perform(delete("/api/er/rprt/ER0001")
                        .with(authentication(authToken(airlineUserOP0001))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/er/rprt/{erId}/submit — 제출
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("제출 — AIRLINE 정상 제출 200 응답")
    void submit_AIRLINE_정상제출_200() throws Exception {
        willDoNothing().given(erService).submit(eq("ER0001"), any(IcasUser.class));

        mockMvc.perform(post("/api/er/rprt/ER0001/submit")
                        .with(authentication(authToken(airlineUserOP0001))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("ER 이 제출되었습니다."));
    }

    @Test
    @DisplayName("제출 — Service BAD_REQUEST 시 400 응답 (DRAFT 아닌 상태)")
    void submit_Service_BAD_REQUEST_400() throws Exception {
        willThrow(BusinessException.badRequest("DRAFT 상태에서만 가능한 작업입니다."))
                .given(erService).submit(eq("ER0001"), any(IcasUser.class));

        mockMvc.perform(post("/api/er/rprt/ER0001/submit")
                        .with(authentication(authToken(airlineUserOP0001))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/er/rprt/{erId}/review — 검토 진입
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("검토 진입 — KOTSA 정상 200 응답")
    void review_KOTSA_정상_200() throws Exception {
        willDoNothing().given(erService).review(eq("ER0001"), any(IcasUser.class));

        mockMvc.perform(post("/api/er/rprt/ER0001/review")
                        .with(authentication(authToken(kotsaUser))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("ER 검토가 시작되었습니다."));
    }

    @Test
    @DisplayName("검토 진입 — Service FORBIDDEN 시 403 응답 (AIRLINE 시도)")
    void review_Service_FORBIDDEN_403() throws Exception {
        willThrow(BusinessException.forbidden("한국교통안전공단(KOTSA) 사용자만 수행할 수 있는 작업입니다."))
                .given(erService).review(eq("ER0001"), any(IcasUser.class));

        mockMvc.perform(post("/api/er/rprt/ER0001/review")
                        .with(authentication(authToken(airlineUserOP0001))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/er/rprt/{erId}/reject — 반려 (사유 필수)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("반려 — KOTSA 사유 포함 정상 반려 200 응답")
    void reject_KOTSA_정상반려_200() throws Exception {
        willDoNothing().given(erService).reject(eq("ER0001"), eq("AOC 갱신 필요"), any(IcasUser.class));

        Map<String, String> body = Map.of("reason", "AOC 갱신 필요");

        mockMvc.perform(post("/api/er/rprt/ER0001/reject")
                        .with(authentication(authToken(kotsaUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("ER 이 반려되었습니다."));
    }

    @Test
    @DisplayName("반려 — reason 빈 문자열 시 Service BAD_REQUEST → 400 응답")
    void reject_reason_누락_400() throws Exception {
        willThrow(BusinessException.badRequest("반려 사유는 필수입니다."))
                .given(erService).reject(eq("ER0001"), eq(""), any(IcasUser.class));

        Map<String, String> body = Map.of("reason", "");

        mockMvc.perform(post("/api/er/rprt/ER0001/reject")
                        .with(authentication(authToken(kotsaUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/er/rprt/{erId}/recommend — 권고
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("권고 — KOTSA 정상 200 응답")
    void recommend_KOTSA_정상_200() throws Exception {
        willDoNothing().given(erService).recommend(eq("ER0001"), any(IcasUser.class));

        mockMvc.perform(post("/api/er/rprt/ER0001/recommend")
                        .with(authentication(authToken(kotsaUser))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("ER 이 권고 처리되었습니다."));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/er/rprt/{erId}/approve — 승인
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("승인 — MOLIT 정상 승인 200 응답")
    void approve_MOLIT_정상승인_200() throws Exception {
        willDoNothing().given(erService).approve(eq("ER0001"), any(IcasUser.class));

        mockMvc.perform(post("/api/er/rprt/ER0001/approve")
                        .with(authentication(authToken(molitUser))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("ER 이 승인되었습니다."));
    }

    @Test
    @DisplayName("승인 — Service FORBIDDEN 시 403 응답 (KOTSA 시도)")
    void approve_Service_FORBIDDEN_403() throws Exception {
        willThrow(BusinessException.forbidden("국토부(MOLIT) 사용자만 수행할 수 있는 작업입니다."))
                .given(erService).approve(eq("ER0001"), any(IcasUser.class));

        mockMvc.perform(post("/api/er/rprt/ER0001/approve")
                        .with(authentication(authToken(kotsaUser))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/er/rprt/{erId}/cancel — 취소 (사유 필수)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("취소 — MOLIT 사유 포함 정상 취소 200 응답")
    void cancel_MOLIT_정상취소_200() throws Exception {
        willDoNothing().given(erService).cancel(eq("ER0001"), eq("운영 종료"), any(IcasUser.class));

        Map<String, String> body = Map.of("reason", "운영 종료");

        mockMvc.perform(post("/api/er/rprt/ER0001/cancel")
                        .with(authentication(authToken(molitUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("ER 이 취소되었습니다."));
    }

    @Test
    @DisplayName("취소 — Service BAD_REQUEST 시 400 응답 (APRVD 아닌 상태)")
    void cancel_Service_BAD_REQUEST_400() throws Exception {
        willThrow(BusinessException.badRequest("APRVD 상태에서만 가능한 작업입니다."))
                .given(erService).cancel(eq("ER0001"), eq("운영 종료"), any(IcasUser.class));

        Map<String, String> body = Map.of("reason", "운영 종료");

        mockMvc.perform(post("/api/er/rprt/ER0001/cancel")
                        .with(authentication(authToken(molitUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }
}
