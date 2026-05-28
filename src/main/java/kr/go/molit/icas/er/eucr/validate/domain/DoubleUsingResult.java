package kr.go.molit.icas.er.eucr.validate.domain;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 일련번호 이중사용 검증 결과 (SFR-031).
 *
 * <pre>
 * severity:
 *   - OK       — 입력 일련번호 모두 충돌 없음
 *   - BLOCKED  — 1개라도 다른 EUCR 점유
 *
 * 일괄 등록 시 conflicts 가 1건 이상이면 BLOCKED.
 * </pre>
 */
@Getter
@Builder
public class DoubleUsingResult {

    /** 검사 입력 일련번호 개수 */
    private final int requestedCount;

    /** 충돌 발견 건수 */
    private final int conflictCount;

    /** 결과 등급: OK / BLOCKED */
    private final String severity;

    /** 사용자 표시 메시지 */
    private final String message;

    /** 충돌 상세 (BLOCKED 시 1건 이상) */
    private final List<CrdtConflictRow> conflicts;
}
