package kr.go.molit.icas.er.oom.validate.domain;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 18종 정량 검증 일괄 실행 요약.
 *
 * <p>Validator 가 한 OoM 에 대해 18 Rule 실행 후 본 DTO 반환.
 * 동시에 oom_check_item 에 일괄 저장.
 */
@Getter
@Builder
public class QuantCheckRunResult {

    /** 대상 OoM ID */
    private final String oomId;

    /** 실행 Rule 개수 (정상은 18) */
    private final int totalCount;

    /** PASS 건수 */
    private final int passCount;

    /** WARN 건수 */
    private final int warnCount;

    /** FAIL 건수 */
    private final int failCount;

    /**
     * 종합 결과 — FAIL 1개 이상이면 FAIL,
     * WARN 만 있으면 HOLD, 모두 PASS 면 PASS.
     */
    private final String overallRslt;

    /** 항목별 상세 결과 (item_no 순) */
    private final List<QuantCheckResult> results;
}
