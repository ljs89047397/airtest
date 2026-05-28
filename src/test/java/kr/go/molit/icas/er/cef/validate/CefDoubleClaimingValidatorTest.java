package kr.go.molit.icas.er.cef.validate;

import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.er.cef.claim.CefClaimMapper;
import kr.go.molit.icas.er.cef.validate.domain.BatchConflictRow;
import kr.go.molit.icas.er.cef.validate.domain.DoubleClaimingResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CefDoubleClaimingValidator 단위 테스트 — SFR-021 이중청구 검증")
class CefDoubleClaimingValidatorTest {

    @Mock
    CefClaimMapper cefClaimMapper;

    @InjectMocks
    CefDoubleClaimingValidator validator;

    private BatchConflictRow cefRow(String cefId, String claimNo, String oprtrId, String rprtYr) {
        BatchConflictRow r = new BatchConflictRow();
        r.setSourceType("CEF");
        r.setSourceId(cefId);
        r.setClaimNo(claimNo);
        r.setOprtrId(oprtrId);
        r.setOprtrNm(oprtrId + "_NM");
        r.setRprtYr(rprtYr);
        r.setMass(new BigDecimal("100"));
        return r;
    }

    private BatchConflictRow safRow(String batchId, String oprtrId) {
        BatchConflictRow r = new BatchConflictRow();
        r.setSourceType("SAF");
        r.setSourceId(batchId);
        r.setOprtrId(oprtrId);
        r.setOprtrNm(oprtrId + "_NM");
        r.setMass(new BigDecimal("200"));
        return r;
    }

    @Test
    @DisplayName("충돌 없음 → OK")
    void validate_충돌없음_OK() {
        given(cefClaimMapper.findCefBatchConflicts("BATCH001", null, null)).willReturn(List.of());
        given(cefClaimMapper.findSafBatchConflicts("BATCH001")).willReturn(List.of());

        DoubleClaimingResult r = validator.validate("BATCH001", "OP0001", null, null);

        assertThat(r.getSeverity()).isEqualTo("OK");
        assertThat(r.getCefConflicts()).isEmpty();
        assertThat(r.getSafConflicts()).isEmpty();
    }

    @Test
    @DisplayName("다른 항공사 CEF 점유 → BLOCKED")
    void validate_다른항공사CEF_BLOCKED() {
        given(cefClaimMapper.findCefBatchConflicts("BATCH001", null, null))
                .willReturn(List.of(cefRow("CEF0002", "CLM-2", "OP0002", "2026")));
        given(cefClaimMapper.findSafBatchConflicts("BATCH001")).willReturn(List.of());

        DoubleClaimingResult r = validator.validate("BATCH001", "OP0001", null, null);

        assertThat(r.getSeverity()).isEqualTo("BLOCKED");
        assertThat(r.getMessage()).contains("다른 항공사");
    }

    @Test
    @DisplayName("다른 항공사 SAF 배치 점유 → BLOCKED")
    void validate_다른항공사SAF_BLOCKED() {
        given(cefClaimMapper.findCefBatchConflicts("BATCH001", null, null)).willReturn(List.of());
        given(cefClaimMapper.findSafBatchConflicts("BATCH001"))
                .willReturn(List.of(safRow("BATCH001", "OP0002")));

        DoubleClaimingResult r = validator.validate("BATCH001", "OP0001", null, null);

        assertThat(r.getSeverity()).isEqualTo("BLOCKED");
        assertThat(r.getMessage()).contains("SAF");
    }

    @Test
    @DisplayName("동일 CEF 내 다른 청구건에 같은 batch_id → BLOCKED (excludeCefId 지정)")
    void validate_동일CEF내중복_BLOCKED() {
        given(cefClaimMapper.findCefBatchConflicts("BATCH001", "CEF0001", "CLM-1"))
                .willReturn(List.of(cefRow("CEF0001", "CLM-9", "OP0001", "2026")));
        given(cefClaimMapper.findSafBatchConflicts("BATCH001")).willReturn(List.of());

        DoubleClaimingResult r = validator.validate("BATCH001", "OP0001", "CEF0001", "CLM-1");

        assertThat(r.getSeverity()).isEqualTo("BLOCKED");
        assertThat(r.getMessage()).contains("동일 CEF");
    }

    @Test
    @DisplayName("같은 항공사의 다른 CEF (rprt_yr 다름)와 batch 공유 → WARNING")
    void validate_같은항공사다른CEF_WARNING() {
        given(cefClaimMapper.findCefBatchConflicts("BATCH001", "CEF0001", null))
                .willReturn(List.of(cefRow("CEF0008", "CLM-X", "OP0001", "2025")));
        given(cefClaimMapper.findSafBatchConflicts("BATCH001")).willReturn(List.of());

        DoubleClaimingResult r = validator.validate("BATCH001", "OP0001", "CEF0001", null);

        assertThat(r.getSeverity()).isEqualTo("WARNING");
        assertThat(r.getMessage()).contains("다른 CEF");
    }

    @Test
    @DisplayName("같은 항공사의 SAF 배치만 발견 → WARNING")
    void validate_같은항공사SAF_WARNING() {
        given(cefClaimMapper.findCefBatchConflicts("BATCH001", null, null)).willReturn(List.of());
        given(cefClaimMapper.findSafBatchConflicts("BATCH001"))
                .willReturn(List.of(safRow("BATCH001", "OP0001")));

        DoubleClaimingResult r = validator.validate("BATCH001", "OP0001", null, null);

        assertThat(r.getSeverity()).isEqualTo("WARNING");
        assertThat(r.getMessage()).contains("SAF");
    }

    @Test
    @DisplayName("BLOCKED 인 경우 assertNotBlocked 가 CONFLICT(409) 예외")
    void assertNotBlocked_BLOCKED_시_예외() {
        given(cefClaimMapper.findCefBatchConflicts("BATCH001", null, null))
                .willReturn(List.of(cefRow("CEF0002", "CLM-2", "OP0002", "2026")));
        given(cefClaimMapper.findSafBatchConflicts("BATCH001")).willReturn(List.of());

        assertThatThrownBy(() -> validator.assertNotBlocked("BATCH001", "OP0001", null, null))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(409));
    }

    @Test
    @DisplayName("WARNING 인 경우 assertNotBlocked 통과 (예외 없음)")
    void assertNotBlocked_WARNING_통과() {
        given(cefClaimMapper.findCefBatchConflicts("BATCH001", null, null))
                .willReturn(List.of(cefRow("CEF0008", "CLM-X", "OP0001", "2025")));
        given(cefClaimMapper.findSafBatchConflicts("BATCH001")).willReturn(List.of());

        assertThatCode(() -> validator.assertNotBlocked("BATCH001", "OP0001", null, null))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("batchIdNo 누락 → BAD_REQUEST(400)")
    void validate_batchIdNo누락_BAD_REQUEST() {
        assertThatThrownBy(() -> validator.validate("", "OP0001", null, null))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));
    }

    @Test
    @DisplayName("currentOprtrId 누락 → BAD_REQUEST(400)")
    void validate_oprtrId누락_BAD_REQUEST() {
        assertThatThrownBy(() -> validator.validate("BATCH001", null, null, null))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));
    }
}
