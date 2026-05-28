package kr.go.molit.icas.er.rprt.validate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.exception.GlobalExceptionHandler;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.er.rprt.validate.ErPairSumValidationService.PairSumValidationResult;
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
@DisplayName("ErPairSumValidationController 슬라이스 테스트")
class ErPairSumValidationControllerTest {

    MockMvc mockMvc;

    @Mock
    ErPairSumValidationService erPairSumValidationService;

    ObjectMapper objectMapper;

    IcasUser airlineUserOP0001;
    IcasUser airlineUserOP0002;

    @BeforeEach
    void setUp() {
        ErPairSumValidationController controller =
                new ErPairSumValidationController(erPairSumValidationService);
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

    private PairSumValidationResult passedResult() {
        return PairSumValidationResult.builder()
                .cntrySum(new BigDecimal("1000000.0000"))
                .aerdrmSum(new BigDecimal("999500.0000"))
                .deviation(new BigDecimal("500.0000"))
                .deviationPct(new BigDecimal("0.0500"))
                .passed(true)
                .message("검증 통과: 국가 쌍(1000000.0000) ↔ 비행장 쌍(999500.0000) 편차율 0.0500% — ±0.1% 이내")
                .build();
    }

    private PairSumValidationResult failedResult() {
        return PairSumValidationResult.builder()
                .cntrySum(new BigDecimal("1000000.0000"))
                .aerdrmSum(new BigDecimal("990000.0000"))
                .deviation(new BigDecimal("10000.0000"))
                .deviationPct(new BigDecimal("1.0000"))
                .passed(false)
                .message("검증 실패: 국가 쌍(1000000.0000) ↔ 비행장 쌍(990000.0000) 편차율 1.0000% — ±0.1% 초과")
                .build();
    }

    private PairSumValidationResult cntryZeroResult() {
        return PairSumValidationResult.builder()
                .cntrySum(BigDecimal.ZERO)
                .aerdrmSum(new BigDecimal("500000.0000"))
                .deviation(new BigDecimal("500000.0000"))
                .deviationPct(null)
                .passed(false)
                .message("국가 쌍 CO₂ 합계가 0 이지만 비행장 쌍 CO₂ 합계는 500000.0000 입니다.")
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET — 합계 일치 검증 (항상 HTTP 200)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("합계 검증 — 통과 시 200 응답, passed=true / 응답 JSON 구조 검증")
    void validate_통과_200() throws Exception {
        given(erPairSumValidationService.validatePairSum(eq("ER0001"), any(IcasUser.class)))
                .willReturn(passedResult());

        mockMvc.perform(get("/api/er/rprt/ER0001/validate-pair-sum")
                        .with(authentication(authToken(airlineUserOP0001)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.passed").value(true))
                .andExpect(jsonPath("$.data.cntrySum").value(1000000.0000))
                .andExpect(jsonPath("$.data.aerdrmSum").value(999500.0000))
                .andExpect(jsonPath("$.data.deviation").value(500.0000))
                .andExpect(jsonPath("$.data.deviationPct").value(0.0500))
                .andExpect(jsonPath("$.data.message").isString());
    }

    @Test
    @DisplayName("합계 검증 — 미통과 시도 200 응답(항상), passed=false / message 검증")
    void validate_미통과_200() throws Exception {
        given(erPairSumValidationService.validatePairSum(eq("ER0001"), any(IcasUser.class)))
                .willReturn(failedResult());

        mockMvc.perform(get("/api/er/rprt/ER0001/validate-pair-sum")
                        .with(authentication(authToken(airlineUserOP0001)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))  // HTTP 는 항상 200
                .andExpect(jsonPath("$.data.passed").value(false))
                .andExpect(jsonPath("$.data.deviationPct").value(1.0000))
                .andExpect(jsonPath("$.data.message").value(org.hamcrest.Matchers.containsString("실패")));
    }

    @Test
    @DisplayName("합계 검증 — cntrySum=0 + aerdrmSum>0 → 200, passed=false, deviationPct=null")
    void validate_cntrySum0_200() throws Exception {
        given(erPairSumValidationService.validatePairSum(eq("ER0001"), any(IcasUser.class)))
                .willReturn(cntryZeroResult());

        mockMvc.perform(get("/api/er/rprt/ER0001/validate-pair-sum")
                        .with(authentication(authToken(airlineUserOP0001)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.passed").value(false))
                .andExpect(jsonPath("$.data.deviationPct").doesNotExist()); // null → JSON 에 포함 안 됨
    }

    @Test
    @DisplayName("합계 검증 — Service NOT_FOUND 시 404 응답")
    void validate_ER미존재_404() throws Exception {
        given(erPairSumValidationService.validatePairSum(eq("ER9999"), any(IcasUser.class)))
                .willThrow(BusinessException.notFound("ER"));

        mockMvc.perform(get("/api/er/rprt/ER9999/validate-pair-sum")
                        .with(authentication(authToken(airlineUserOP0001)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    @DisplayName("합계 검증 — Service FORBIDDEN 시 403 응답")
    void validate_가시범위위반_403() throws Exception {
        given(erPairSumValidationService.validatePairSum(eq("ER0001"), any(IcasUser.class)))
                .willThrow(BusinessException.forbidden("본인 항공사 데이터만 접근할 수 있습니다."));

        mockMvc.perform(get("/api/er/rprt/ER0001/validate-pair-sum")
                        .with(authentication(authToken(airlineUserOP0002)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    @DisplayName("합계 검증 응답 DTO 구조 — cntrySum/aerdrmSum/deviation/deviationPct/passed/message 6개 필드 모두 존재")
    void validate_DTO구조_6개필드() throws Exception {
        given(erPairSumValidationService.validatePairSum(eq("ER0001"), any(IcasUser.class)))
                .willReturn(passedResult());

        mockMvc.perform(get("/api/er/rprt/ER0001/validate-pair-sum")
                        .with(authentication(authToken(airlineUserOP0001)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.cntrySum").exists())
                .andExpect(jsonPath("$.data.aerdrmSum").exists())
                .andExpect(jsonPath("$.data.deviation").exists())
                .andExpect(jsonPath("$.data.deviationPct").exists())
                .andExpect(jsonPath("$.data.passed").exists())
                .andExpect(jsonPath("$.data.message").exists());
    }
}
