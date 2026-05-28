package kr.go.molit.icas.er.rprt.validate;

import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.DataScopeValidator;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.er.rprt.ErMapper;
import kr.go.molit.icas.er.rprt.aerdrm.ErAerdrmPairCo2Mapper;
import kr.go.molit.icas.er.rprt.cntry.ErCntryPairCo2Mapper;
import kr.go.molit.icas.er.rprt.domain.ErVO;
import kr.go.molit.icas.er.rprt.validate.ErPairSumValidationService.PairSumValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ErPairSumValidationService 단위 테스트 — 국가 쌍 ↔ 비행장 쌍 합계 일치 검증")
class ErPairSumValidationServiceTest {

    @Mock
    ErCntryPairCo2Mapper erCntryPairCo2Mapper;

    @Mock
    ErAerdrmPairCo2Mapper erAerdrmPairCo2Mapper;

    @Mock
    ErMapper erMapper;

    @Mock
    DataScopeValidator dataScopeValidator;

    @InjectMocks
    ErPairSumValidationService erPairSumValidationService;

    private IcasUser airlineUserOP0001;
    private IcasUser airlineUserOP0002;

    @BeforeEach
    void setUpFixtures() {
        airlineUserOP0001 = IcasUser.builder()
                .userId("airline01").userNm("항공사A 담당자")
                .ognzSeCd("AIRLINE").ognzId("ORG_AIR01").oprtrId("OP0001").master(false)
                .roleIds(List.of("AIRLINE_USER")).build();

        airlineUserOP0002 = IcasUser.builder()
                .userId("airline02").userNm("항공사B 담당자")
                .ognzSeCd("AIRLINE").ognzId("ORG_AIR02").oprtrId("OP0002").master(false)
                .roleIds(List.of("AIRLINE_USER")).build();
    }

    // ── helpers ──

    private ErVO makeDraftEr(String erId, String oprtrId, String rprtYr) {
        ErVO er = new ErVO();
        er.setErId(erId);
        er.setOprtrId(oprtrId);
        er.setRprtYr(rprtYr);
        er.setErStCd("DRAFT");
        return er;
    }

    private void setUpSums(String erId, BigDecimal cntrySum, BigDecimal aerdrmSum) {
        given(erMapper.selectByErId(erId)).willReturn(makeDraftEr(erId, "OP0001", "2026"));
        willDoNothing().given(dataScopeValidator)
                .assertOprtrAccessible(eq(airlineUserOP0001), eq("OP0001"), eq("2026"));
        given(erCntryPairCo2Mapper.sumCo2EmsnByEr(erId)).willReturn(cntrySum);
        given(erAerdrmPairCo2Mapper.sumCo2EmsnByEr(erId)).willReturn(aerdrmSum);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 정상 통과 시나리오
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("정상 통과: cntrySum=1,000,000 / aerdrmSum=999,500 → deviation=500 / pct≈0.05% → passed=true")
    void validatePairSum_통과_편차0_05퍼센트() {
        BigDecimal cntrySum  = new BigDecimal("1000000");
        BigDecimal aerdrmSum = new BigDecimal("999500");
        setUpSums("ER0001", cntrySum, aerdrmSum);

        PairSumValidationResult result = erPairSumValidationService.validatePairSum("ER0001", airlineUserOP0001);

        assertThat(result.isPassed()).isTrue();
        assertThat(result.getCntrySum().compareTo(cntrySum)).isEqualTo(0);
        assertThat(result.getAerdrmSum().compareTo(aerdrmSum)).isEqualTo(0);
        assertThat(result.getDeviation().compareTo(new BigDecimal("500"))).isEqualTo(0);
        // deviationPct = 500/1000000*100 = 0.05%
        assertThat(result.getDeviationPct()).isNotNull();
        assertThat(result.getDeviationPct().compareTo(new BigDecimal("0.0500"))).isEqualTo(0);
        assertThat(result.getMessage()).contains("통과");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 미통과 시나리오
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("미통과: cntrySum=1,000,000 / aerdrmSum=990,000 → deviation=10,000 / pct=1% → passed=false")
    void validatePairSum_미통과_편차1퍼센트() {
        BigDecimal cntrySum  = new BigDecimal("1000000");
        BigDecimal aerdrmSum = new BigDecimal("990000");
        setUpSums("ER0001", cntrySum, aerdrmSum);

        PairSumValidationResult result = erPairSumValidationService.validatePairSum("ER0001", airlineUserOP0001);

        assertThat(result.isPassed()).isFalse();
        assertThat(result.getDeviation().compareTo(new BigDecimal("10000"))).isEqualTo(0);
        // deviationPct = 10000/1000000*100 = 1.0%
        assertThat(result.getDeviationPct()).isNotNull();
        assertThat(result.getDeviationPct().compareTo(new BigDecimal("1.0000"))).isEqualTo(0);
        assertThat(result.getMessage()).contains("실패");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 경계 시나리오
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("둘 다 0 → passed=true, deviationPct=0 (데이터 없음 통과)")
    void validatePairSum_둘다0_통과() {
        setUpSums("ER0001", BigDecimal.ZERO, BigDecimal.ZERO);

        PairSumValidationResult result = erPairSumValidationService.validatePairSum("ER0001", airlineUserOP0001);

        assertThat(result.isPassed()).isTrue();
        assertThat(result.getDeviation().compareTo(BigDecimal.ZERO)).isEqualTo(0);
        assertThat(result.getDeviationPct().compareTo(BigDecimal.ZERO)).isEqualTo(0);
    }

    @Test
    @DisplayName("cntrySum=0 + aerdrmSum>0 → passed=false, deviationPct=null (계산 불가)")
    void validatePairSum_cntry0_aerdrm양수_미통과() {
        setUpSums("ER0001", BigDecimal.ZERO, new BigDecimal("500000"));

        PairSumValidationResult result = erPairSumValidationService.validatePairSum("ER0001", airlineUserOP0001);

        assertThat(result.isPassed()).isFalse();
        assertThat(result.getDeviationPct()).isNull();
        assertThat(result.getCntrySum().compareTo(BigDecimal.ZERO)).isEqualTo(0);
        assertThat(result.getAerdrmSum().compareTo(new BigDecimal("500000"))).isEqualTo(0);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 응답 DTO 구조 검증
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("응답 DTO 구조: cntrySum/aerdrmSum/deviation/deviationPct/passed/message 모두 포함")
    void validatePairSum_DTO구조_검증() {
        setUpSums("ER0001", new BigDecimal("1000000"), new BigDecimal("999500"));

        PairSumValidationResult result = erPairSumValidationService.validatePairSum("ER0001", airlineUserOP0001);

        assertThat(result.getCntrySum()).isNotNull();
        assertThat(result.getAerdrmSum()).isNotNull();
        assertThat(result.getDeviation()).isNotNull();
        assertThat(result.getDeviationPct()).isNotNull();
        assertThat(result.getMessage()).isNotBlank();
        // passed 필드는 boolean → isTrue/isFalse 로 검증 가능 (여기서는 통과 케이스)
        assertThat(result.isPassed()).isTrue();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 예외 시나리오
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("부모 ER 미존재 → NOT_FOUND(404)")
    void validatePairSum_ER미존재_NOT_FOUND() {
        given(erMapper.selectByErId("ER9999")).willReturn(null);

        assertThatThrownBy(() -> erPairSumValidationService.validatePairSum("ER9999", airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(404));
    }

    @Test
    @DisplayName("가시범위 위반 → FORBIDDEN(403)")
    void validatePairSum_가시범위위반_FORBIDDEN() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeDraftEr("ER0001", "OP0001", "2026"));
        willThrow(BusinessException.forbidden("본인 항공사 데이터만 접근할 수 있습니다."))
                .given(dataScopeValidator)
                .assertOprtrAccessible(eq(airlineUserOP0002), eq("OP0001"), eq("2026"));

        assertThatThrownBy(() -> erPairSumValidationService.validatePairSum("ER0001", airlineUserOP0002))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(403));
    }
}
