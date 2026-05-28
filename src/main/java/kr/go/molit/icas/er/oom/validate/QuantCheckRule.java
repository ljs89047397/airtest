package kr.go.molit.icas.er.oom.validate;

import kr.go.molit.icas.er.oom.validate.domain.QuantCheckResult;

/**
 * 정량 검증 Rule 인터페이스 (SFR-034).
 *
 * <p>구현체는 {@link QuantCheckRuleFactory} 가 등록·순회.
 * 신규 항목 추가 시 본 인터페이스 구현 + Factory 등록만 하면 됨.
 *
 * <p>구현 가이드:
 * <ul>
 *   <li>외부 데이터 가용 → 본 검증 수행 후 PASS/FAIL</li>
 *   <li>외부 데이터 부족 → WARN + rmrk="데이터 부족" 으로 자동판정 보류</li>
 *   <li>본 메서드는 예외를 던지지 않음 — 예외 상황도 WARN/FAIL 로 처리</li>
 * </ul>
 */
public interface QuantCheckRule {

    /** SFR-034 항목 번호 (1~18) */
    int itemNo();

    /** 항목명 (사용자 표시) */
    String itemNm();

    /** 검증 수행 — 결과 DTO 반환 */
    QuantCheckResult check(QuantCheckContext ctx);

    /**
     * 데이터 부족 시 표준 WARN 결과 생성 헬퍼.
     */
    default QuantCheckResult warnNoData(String reason) {
        return QuantCheckResult.builder()
                .itemNo(itemNo())
                .itemNm(itemNm())
                .judgCd("WARN")
                .rmrk("데이터 부족: " + reason)
                .build();
    }
}
