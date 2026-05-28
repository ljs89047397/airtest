package kr.go.molit.icas.er.oom.validate.domain;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * 18종 정량 검증 한 항목의 결과 (SFR-034).
 *
 * <p>{@link kr.go.molit.icas.er.oom.validate.CorsiaQuantValidator} 가
 * 각 Rule 실행 후 본 DTO 를 모아 oom_check_item 으로 일괄 저장.
 *
 * <h2>판정 코드</h2>
 * <ul>
 *   <li>PASS — 검증 통과</li>
 *   <li>WARN — 통과지만 주의 필요 (또는 데이터 부족으로 자동판정 불가)</li>
 *   <li>FAIL — 검증 실패 (수동 조치 필요)</li>
 * </ul>
 */
@Getter
@Builder
public class QuantCheckResult {

    /** 항목 번호 (1~18 — SFR-034 표준) */
    private final int itemNo;

    /** 항목명 */
    private final String itemNm;

    /** 예상값 (문자열 — 숫자/문자/문장 모두) */
    private final String expctdVal;

    /** 보고값 */
    private final String rprtdVal;

    /** 편차율 (계산 가능 시) */
    private final BigDecimal dvtnRate;

    /** 판정 코드 — PASS / WARN / FAIL */
    private final String judgCd;

    /** 비고 (한국어) */
    private final String rmrk;
}
