package kr.go.molit.icas.er.cef.validate;

import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.er.cef.claim.CefClaimMapper;
import kr.go.molit.icas.er.cef.validate.domain.BatchConflictRow;
import kr.go.molit.icas.er.cef.validate.domain.DoubleClaimingResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * CEF 이중청구 검증기 (SFR-021).
 *
 * <h2>검증 대상</h2>
 * <ul>
 *   <li>er.tn_cef_claim — 동일 batch_id_no 를 가진 다른 CEF 청구건</li>
 *   <li>saf.tn_saf_batch — 동일 batch_id 를 가진 SAF 배치 (이미 SAF 도메인에서 청구된 배치)</li>
 * </ul>
 *
 * <h2>등급 산출 규칙</h2>
 * <pre>
 *   BLOCKED — 다음 중 하나라도 발견되면
 *     · 동일 cef_id 내 다른 claim_no 에 같은 batch_id_no (1 CEF 내 중복)
 *     · 다른 운영사 점유 (CEF 또는 SAF, oprtr_id ≠ currentOprtrId)
 *
 *   WARNING — 위 BLOCKED 조건은 아니나
 *     · 동일 운영사의 다른 CEF (다른 cef_id) 와 batch_id 공유
 *     · 동일 운영사의 SAF 배치와 batch_id 공유 (이미 SAF 도메인에서 다룬 배치)
 *
 *   OK — 충돌 없음
 * </pre>
 *
 * <p>SAF 도메인 진행 시 본 클래스를 {@code common.validate} 패키지로 승격 검토.
 */
@Component
@RequiredArgsConstructor
public class CefDoubleClaimingValidator {

    private final CefClaimMapper cefClaimMapper;

    /**
     * 배치 ID 이중청구 검증.
     *
     * @param batchIdNo      검사 대상 배치 ID (필수)
     * @param currentOprtrId 현재 청구 운영사 ID (필수, 자기 점유 판정용)
     * @param excludeCefId   제외할 cef_id — 수정 시 자기 자신 제외용 (신규 시 null)
     * @param excludeClaimNo 제외할 claim_no — (신규 시 null)
     * @return 검증 결과
     * @throws BusinessException BAD_REQUEST — batchIdNo / currentOprtrId 누락
     */
    public DoubleClaimingResult validate(String batchIdNo,
                                          String currentOprtrId,
                                          String excludeCefId,
                                          String excludeClaimNo) {
        if (isBlank(batchIdNo)) {
            throw BusinessException.badRequest("배치 ID(batchIdNo)는 필수입니다.");
        }
        if (isBlank(currentOprtrId)) {
            throw BusinessException.badRequest("현재 운영사 ID(currentOprtrId)는 필수입니다.");
        }

        List<BatchConflictRow> cefRows = cefClaimMapper.findCefBatchConflicts(
                batchIdNo, excludeCefId, excludeClaimNo);
        List<BatchConflictRow> safRows = cefClaimMapper.findSafBatchConflicts(batchIdNo);

        String severity;
        String message;

        boolean otherOprtrInCef    = cefRows.stream()
                .anyMatch(r -> !currentOprtrId.equals(r.getOprtrId()));
        boolean otherOprtrInSaf    = safRows.stream()
                .anyMatch(r -> !currentOprtrId.equals(r.getOprtrId()));
        boolean sameCefDifferentClaim = excludeCefId != null
                && cefRows.stream().anyMatch(r -> excludeCefId.equals(r.getSourceId()));
        boolean sameOprtrOtherCef  = cefRows.stream()
                .anyMatch(r -> currentOprtrId.equals(r.getOprtrId())
                        && (excludeCefId == null || !excludeCefId.equals(r.getSourceId())));
        boolean sameOprtrSaf       = safRows.stream()
                .anyMatch(r -> currentOprtrId.equals(r.getOprtrId()));

        if (otherOprtrInCef || otherOprtrInSaf || sameCefDifferentClaim) {
            severity = "BLOCKED";
            message  = buildBlockedMessage(batchIdNo,
                                            otherOprtrInCef, otherOprtrInSaf, sameCefDifferentClaim);
        } else if (sameOprtrOtherCef || sameOprtrSaf) {
            severity = "WARNING";
            message  = buildWarningMessage(batchIdNo, sameOprtrOtherCef, sameOprtrSaf);
        } else {
            severity = "OK";
            message  = "이중청구 충돌이 발견되지 않았습니다.";
        }

        return DoubleClaimingResult.builder()
                .batchIdNo(batchIdNo)
                .currentOprtrId(currentOprtrId)
                .severity(severity)
                .message(message)
                .cefConflicts(cefRows)
                .safConflicts(safRows)
                .build();
    }

    /**
     * 등록·수정 시 강제 차단용 헬퍼.
     * BLOCKED 인 경우 {@link BusinessException#conflict(String)} 던짐.
     */
    public void assertNotBlocked(String batchIdNo,
                                  String currentOprtrId,
                                  String excludeCefId,
                                  String excludeClaimNo) {
        DoubleClaimingResult result = validate(batchIdNo, currentOprtrId, excludeCefId, excludeClaimNo);
        if ("BLOCKED".equals(result.getSeverity())) {
            throw BusinessException.conflict(result.getMessage());
        }
    }

    private String buildBlockedMessage(String batchIdNo,
                                        boolean otherCef, boolean otherSaf, boolean sameCefDup) {
        StringBuilder sb = new StringBuilder("이중청구로 등록할 수 없습니다 (batchIdNo=").append(batchIdNo).append("). ");
        if (sameCefDup) sb.append("동일 CEF 내 다른 청구건에서 이미 사용 중. ");
        if (otherCef)   sb.append("다른 항공사의 CEF 청구건에서 이미 사용 중. ");
        if (otherSaf)   sb.append("다른 항공사의 SAF 배치에서 이미 사용 중. ");
        return sb.toString().trim();
    }

    private String buildWarningMessage(String batchIdNo,
                                        boolean sameOprtrOtherCef, boolean sameOprtrSaf) {
        StringBuilder sb = new StringBuilder("동일 항공사의 다른 청구·배치와 batch_id 공유가 감지되었습니다 (batchIdNo=")
                .append(batchIdNo).append("). 합계 누적 여부 검토 필요. ");
        if (sameOprtrOtherCef) sb.append("동일 운영사의 다른 CEF 청구건 존재. ");
        if (sameOprtrSaf)      sb.append("동일 운영사의 SAF 배치 존재. ");
        return sb.toString().trim();
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
