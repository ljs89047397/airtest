package kr.go.molit.icas.er.cef.validate.domain;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 이중청구 검증 결과 (SFR-021).
 *
 * <pre>
 * severity 값:
 *   - OK       — 충돌 없음 (등록 가능)
 *   - WARNING  — 동일 운영사의 다른 CEF/SAF 와 batch_id 공유 (등록 가능하나 검토 권장)
 *   - BLOCKED  — 등록 차단 (다른 운영사 점유 또는 동일 CEF 내 중복)
 * </pre>
 */
@Getter
@Builder
public class DoubleClaimingResult {

    /** 검색 입력 batch_id_no */
    private final String batchIdNo;

    /** 현재 청구하려는 운영사 ID */
    private final String currentOprtrId;

    /** 결과 등급: OK / WARNING / BLOCKED */
    private final String severity;

    /** 사용자 표시 메시지 (한국어) */
    private final String message;

    /** CEF 도메인 충돌 목록 */
    private final List<BatchConflictRow> cefConflicts;

    /** SAF 도메인 충돌 목록 */
    private final List<BatchConflictRow> safConflicts;
}
