package kr.go.molit.icas.er.eucr.validate;

import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.er.eucr.crdt.EucrCrdtDtlMapper;
import kr.go.molit.icas.er.eucr.validate.domain.CrdtConflictRow;
import kr.go.molit.icas.er.eucr.validate.domain.DoubleUsingResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * EUCR 일련번호 이중사용 검증기 (SFR-031).
 *
 * <h2>검증 대상</h2>
 * <p>{@code er.tn_eucr_crdt_dtl.crdt_no} — 전역 UK 컬럼. 다른 EUCR 에서 이미 사용 중이면 BLOCKED.
 *
 * <h2>등급 산출</h2>
 * <pre>
 *   OK      — 충돌 없음
 *   BLOCKED — 1개라도 다른 EUCR (자기 자신 제외) 에 점유됨
 * </pre>
 *
 * <p>일괄 등록 워크플로우 (CSV / 범위 expand) 에서 한번에 N개 검사 가능.
 * DB UK 가 최종 안전망이지만 비즈니스 단에서 친절한 사전 차단·메시지 제공.
 */
@Component
@RequiredArgsConstructor
public class EucrDoubleUsingValidator {

    private final EucrCrdtDtlMapper eucrCrdtDtlMapper;

    /**
     * 일련번호 리스트의 이중사용 검증.
     *
     * @param crdtNos       검사 대상 일련번호 (1개 이상 필요)
     * @param excludeEucrId 제외할 EUCR ID — 수정 시 자기 자신 제외 (null 허용 — 신규)
     * @return 검증 결과
     */
    public DoubleUsingResult validate(List<String> crdtNos, String excludeEucrId) {
        if (crdtNos == null || crdtNos.isEmpty()) {
            throw BusinessException.badRequest("검사할 일련번호 목록이 비어있습니다.");
        }

        List<CrdtConflictRow> conflicts = eucrCrdtDtlMapper.findConflicts(crdtNos, excludeEucrId);

        boolean blocked = !conflicts.isEmpty();
        String severity = blocked ? "BLOCKED" : "OK";
        String message  = blocked
                ? String.format("이중사용으로 등록할 수 없습니다. 충돌 %d 건 / 검사 %d 건. (다른 EUCR 에서 이미 사용)",
                                conflicts.size(), crdtNos.size())
                : String.format("이중사용 충돌 없음. 검사 %d 건 통과.", crdtNos.size());

        return DoubleUsingResult.builder()
                .requestedCount(crdtNos.size())
                .conflictCount(conflicts.size())
                .severity(severity)
                .message(message)
                .conflicts(conflicts)
                .build();
    }

    /**
     * 등록 시 강제 차단 — BLOCKED 면 {@link BusinessException#conflict(String)}.
     */
    public void assertNotBlocked(List<String> crdtNos, String excludeEucrId) {
        DoubleUsingResult r = validate(crdtNos, excludeEucrId);
        if ("BLOCKED".equals(r.getSeverity())) {
            throw BusinessException.conflict(r.getMessage());
        }
    }
}
