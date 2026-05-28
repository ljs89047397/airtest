package kr.go.molit.icas.er.rprt.validate;

import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.DataScopeValidator;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.er.rprt.ErMapper;
import kr.go.molit.icas.er.rprt.aerdrm.ErAerdrmPairCo2Mapper;
import kr.go.molit.icas.er.rprt.cntry.ErCntryPairCo2Mapper;
import kr.go.molit.icas.er.rprt.domain.ErVO;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 국가 쌍 ↔ 비행장 쌍 CO₂ 합계 일치 검증 서비스 (SFR-013).
 *
 * <h2>검증 알고리즘</h2>
 * <ol>
 *   <li>부모 ER 조회 + 가시범위 검증 (assertOprtrAccessible)</li>
 *   <li>국가 쌍 CO₂ 합계 (sumCo2EmsnByEr) 조회</li>
 *   <li>비행장 쌍 CO₂ 합계 (sumCo2EmsnByEr) 조회</li>
 *   <li>cntrySum = 0 && aerdrmSum = 0 → 데이터 없음, 통과</li>
 *   <li>cntrySum = 0 && aerdrmSum ≠ 0 → 편차율 계산 불가 → 불통과</li>
 *   <li>|cntrySum - aerdrmSum| / cntrySum ≤ 0.001 (0.1%) → 통과</li>
 * </ol>
 *
 * <p>검증 통과 여부와 무관하게 HTTP 200 응답 — 정보 제공용.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ErPairSumValidationService {

    /** 0.1% 허용 편차 */
    private static final BigDecimal TOLERANCE = new BigDecimal("0.001");

    private final ErCntryPairCo2Mapper   erCntryPairCo2Mapper;
    private final ErAerdrmPairCo2Mapper  erAerdrmPairCo2Mapper;
    private final ErMapper               erMapper;
    private final DataScopeValidator     dataScopeValidator;

    // ══════════════════════════════════════════════════════
    // 응답 DTO
    // ══════════════════════════════════════════════════════

    /**
     * 국가 쌍 ↔ 비행장 쌍 합계 일치 검증 결과 DTO.
     *
     * <pre>
     * {
     *   "cntrySum":     1000000.0000,  // 국가 쌍 CO₂ 합계
     *   "aerdrmSum":    1000500.0000,  // 비행장 쌍 CO₂ 합계
     *   "deviation":       500.0000,  // 절대 편차 |cntry - aerdrm|
     *   "deviationPct":      0.0500,  // 편차율 % (소수점 4자리, 없으면 null)
     *   "passed":           false,    // 0.1% 이내 여부
     *   "message":          "..."     // 결과 메시지
     * }
     * </pre>
     */
    @Getter
    @Builder
    public static class PairSumValidationResult {

        /** 국가 쌍 CO₂ 합계 (0 이상) */
        private final BigDecimal cntrySum;

        /** 비행장 쌍 CO₂ 합계 (0 이상) */
        private final BigDecimal aerdrmSum;

        /** 절대 편차: |cntrySum - aerdrmSum| */
        private final BigDecimal deviation;

        /**
         * 편차율 (%): deviation / cntrySum * 100.
         * cntrySum = 0 일 때 null (계산 불가).
         */
        private final BigDecimal deviationPct;

        /** true: ±0.1% 이내 → 통과 */
        private final boolean passed;

        /** 결과 메시지 (한국어) */
        private final String message;
    }

    // ══════════════════════════════════════════════════════
    // 검증 메서드
    // ══════════════════════════════════════════════════════

    /**
     * 국가 쌍 ↔ 비행장 쌍 CO₂ 합계 일치 검증.
     *
     * @param erId ER ID
     * @param user 로그인 사용자
     * @return 검증 결과 DTO (항상 HTTP 200 — 통과 여부는 DTO 내 passed 필드로 확인)
     * @throws BusinessException NOT_FOUND — ER 미존재
     * @throws BusinessException FORBIDDEN — 가시범위 밖
     */
    public PairSumValidationResult validatePairSum(String erId, IcasUser user) {
        ErVO er = erMapper.selectByErId(erId);
        if (er == null) throw BusinessException.notFound("ER");
        dataScopeValidator.assertOprtrAccessible(user, er.getOprtrId(), er.getRprtYr());

        BigDecimal cntrySum  = erCntryPairCo2Mapper.sumCo2EmsnByEr(erId);
        BigDecimal aerdrmSum = erAerdrmPairCo2Mapper.sumCo2EmsnByEr(erId);

        // COALESCE 처리 결과이므로 null 은 없지만 방어적으로 처리
        if (cntrySum  == null) cntrySum  = BigDecimal.ZERO;
        if (aerdrmSum == null) aerdrmSum = BigDecimal.ZERO;

        BigDecimal deviation = cntrySum.subtract(aerdrmSum).abs();

        // 케이스 1: 둘 다 0 → 데이터 없음, 통과
        if (cntrySum.compareTo(BigDecimal.ZERO) == 0
                && aerdrmSum.compareTo(BigDecimal.ZERO) == 0) {
            return PairSumValidationResult.builder()
                    .cntrySum(cntrySum)
                    .aerdrmSum(aerdrmSum)
                    .deviation(BigDecimal.ZERO)
                    .deviationPct(BigDecimal.ZERO)
                    .passed(true)
                    .message("국가 쌍 및 비행장 쌍 데이터가 아직 없습니다. 검증을 통과로 처리합니다.")
                    .build();
        }

        // 케이스 2: cntrySum = 0, aerdrmSum ≠ 0 → 편차율 계산 불가, 불통과
        if (cntrySum.compareTo(BigDecimal.ZERO) == 0) {
            return PairSumValidationResult.builder()
                    .cntrySum(cntrySum)
                    .aerdrmSum(aerdrmSum)
                    .deviation(deviation)
                    .deviationPct(null)
                    .passed(false)
                    .message("국가 쌍 CO₂ 합계가 0 이지만 비행장 쌍 CO₂ 합계는 " + aerdrmSum +
                             " 입니다. 국가 쌍 데이터를 먼저 입력하세요.")
                    .build();
        }

        // 케이스 3: deviationPct 계산 및 0.1% 이내 여부 판정
        // deviationPct = deviation / cntrySum (소수, 즉 0.001 = 0.1%)
        BigDecimal deviationRate = deviation.divide(cntrySum, 10, RoundingMode.HALF_UP);
        // 화면 표시용 퍼센트 (예: 0.001 → 0.1000)
        BigDecimal deviationPct  = deviationRate.multiply(new BigDecimal("100"))
                                                .setScale(4, RoundingMode.HALF_UP);

        boolean passed = deviationRate.compareTo(TOLERANCE) <= 0;

        String message = passed
                ? String.format("검증 통과: 국가 쌍(%.4f) ↔ 비행장 쌍(%.4f) 편차율 %.4f%% — ±0.1%% 이내",
                                cntrySum, aerdrmSum, deviationPct)
                : String.format("검증 실패: 국가 쌍(%.4f) ↔ 비행장 쌍(%.4f) 편차율 %.4f%% — ±0.1%% 초과",
                                cntrySum, aerdrmSum, deviationPct);

        return PairSumValidationResult.builder()
                .cntrySum(cntrySum)
                .aerdrmSum(aerdrmSum)
                .deviation(deviation)
                .deviationPct(deviationPct)
                .passed(passed)
                .message(message)
                .build();
    }
}
