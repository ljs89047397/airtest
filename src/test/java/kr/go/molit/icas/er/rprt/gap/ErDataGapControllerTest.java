package kr.go.molit.icas.er.rprt.gap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.exception.GlobalExceptionHandler;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.er.rprt.gap.domain.ErDataGapVO;
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
@DisplayName("ErDataGapController 슬라이스 테스트")
class ErDataGapControllerTest {

    MockMvc mockMvc;

    @Mock
    ErDataGapService erDataGapService;

    ObjectMapper objectMapper;

    IcasUser airlineUserOP0001;
    IcasUser airlineUserOP0002;

    @BeforeEach
    void setUp() {
        ErDataGapController controller = new ErDataGapController(erDataGapService);
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

    private ErDataGapVO sampleGap(String erId, int gapSn, String thrshld) {
        ErDataGapVO vo = new ErDataGapVO();
        vo.setErId(erId);
        vo.setGapSn(gapSn);
        vo.setAfctCo2Emsn(new BigDecimal("60000.0000"));
        vo.setThrshld5pctXcYn(thrshld);
        vo.setGapCauseCd("DATA_MISSING");
        return vo;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET — 목록 조회
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("목록 조회 — 정상 200 응답, data 배열 포함")
    void list_정상_200() throws Exception {
        given(erDataGapService.list(eq("ER0001"), any(IcasUser.class)))
                .willReturn(List.of(sampleGap("ER0001", 1, "Y"), sampleGap("ER0001", 2, "N")));

        mockMvc.perform(get("/api/er/rprt/ER0001/data-gap")
                        .with(authentication(authToken(airlineUserOP0001)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].gapSn").value(1))
                .andExpect(jsonPath("$.data[0].thrshld5pctXcYn").value("Y"));
    }

    @Test
    @DisplayName("목록 조회 — Service NOT_FOUND 시 404 응답")
    void list_ER미존재_404() throws Exception {
        given(erDataGapService.list(eq("ER9999"), any(IcasUser.class)))
                .willThrow(BusinessException.notFound("ER"));

        mockMvc.perform(get("/api/er/rprt/ER9999/data-gap")
                        .with(authentication(authToken(airlineUserOP0001)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    @DisplayName("목록 조회 — Service FORBIDDEN 시 403 응답")
    void list_가시범위위반_403() throws Exception {
        given(erDataGapService.list(eq("ER0001"), any(IcasUser.class)))
                .willThrow(BusinessException.forbidden("본인 항공사 데이터만 접근할 수 있습니다."));

        mockMvc.perform(get("/api/er/rprt/ER0001/data-gap")
                        .with(authentication(authToken(airlineUserOP0002)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST — 추가 (자동판정 결과 응답 포함 검증)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("추가 — 정상 200 응답, 자동판정 결과 thrshld5pctXcYn='Y' 응답에 포함")
    void add_자동판정결과_응답포함_200() throws Exception {
        ErDataGapVO req = sampleGap("ER0001", 0, "N"); // 사용자 입력 N
        ErDataGapVO saved = sampleGap("ER0001", 1, "Y"); // 자동판정 결과 Y

        given(erDataGapService.add(eq("ER0001"), any(ErDataGapVO.class), any(IcasUser.class)))
                .willReturn(saved);

        mockMvc.perform(post("/api/er/rprt/ER0001/data-gap")
                        .with(authentication(authToken(airlineUserOP0001)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("데이터 갭이 추가되었습니다."))
                .andExpect(jsonPath("$.data.gapSn").value(1))
                .andExpect(jsonPath("$.data.thrshld5pctXcYn").value("Y"));
    }

    @Test
    @DisplayName("추가 — Service BAD_REQUEST 시 400 응답 (afctCo2Emsn 음수)")
    void add_검증실패_400() throws Exception {
        ErDataGapVO req = sampleGap("ER0001", 0, "N");
        given(erDataGapService.add(eq("ER0001"), any(ErDataGapVO.class), any(IcasUser.class)))
                .willThrow(BusinessException.badRequest("갭 영향 CO₂ 배출량(afctCo2Emsn)은 0 이상이어야 합니다."));

        mockMvc.perform(post("/api/er/rprt/ER0001/data-gap")
                        .with(authentication(authToken(airlineUserOP0001)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUT — 수정
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("수정 — 정상 200 응답, message 검증")
    void update_정상_200() throws Exception {
        ErDataGapVO req = sampleGap("ER0001", 1, "N");
        willDoNothing().given(erDataGapService)
                .update(eq("ER0001"), eq(1), any(ErDataGapVO.class), any(IcasUser.class));

        mockMvc.perform(put("/api/er/rprt/ER0001/data-gap/1")
                        .with(authentication(authToken(airlineUserOP0001)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("데이터 갭이 수정되었습니다."));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE — 소프트삭제
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("소프트삭제 — 정상 200 응답, message 검증")
    void delete_정상_200() throws Exception {
        willDoNothing().given(erDataGapService)
                .softDelete(eq("ER0001"), eq(1), any(IcasUser.class));

        mockMvc.perform(delete("/api/er/rprt/ER0001/data-gap/1")
                        .with(authentication(authToken(airlineUserOP0001))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("데이터 갭이 삭제되었습니다."));
    }

    @Test
    @DisplayName("소프트삭제 — Service NOT_FOUND 시 404 응답")
    void delete_자식미존재_404() throws Exception {
        willThrow(BusinessException.notFound("데이터 갭"))
                .given(erDataGapService).softDelete(eq("ER0001"), eq(99), any(IcasUser.class));

        mockMvc.perform(delete("/api/er/rprt/ER0001/data-gap/99")
                        .with(authentication(authToken(airlineUserOP0001))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }
}
