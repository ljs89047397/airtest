package kr.go.molit.icas.er.eucr.validate;

import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.er.eucr.crdt.EucrCrdtDtlMapper;
import kr.go.molit.icas.er.eucr.validate.domain.CrdtConflictRow;
import kr.go.molit.icas.er.eucr.validate.domain.DoubleUsingResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EucrDoubleUsingValidator 단위 테스트 — SFR-031 일련번호 이중사용 검증")
class EucrDoubleUsingValidatorTest {

    @Mock
    EucrCrdtDtlMapper eucrCrdtDtlMapper;

    @InjectMocks
    EucrDoubleUsingValidator validator;

    private CrdtConflictRow row(String crdtNo, String eucrId, String oprtrId, String rprtYr) {
        CrdtConflictRow r = new CrdtConflictRow();
        r.setCrdtNo(crdtNo);
        r.setEucrId(eucrId);
        r.setBatchNo("B1");
        r.setOprtrId(oprtrId);
        r.setOprtrNm(oprtrId + "_NM");
        r.setRprtYr(rprtYr);
        return r;
    }

    @Test
    @DisplayName("충돌 없음 → OK")
    void validate_충돌없음_OK() {
        List<String> input = List.of("VCS-001", "VCS-002");
        given(eucrCrdtDtlMapper.findConflicts(input, null)).willReturn(List.of());

        DoubleUsingResult r = validator.validate(input, null);

        assertThat(r.getSeverity()).isEqualTo("OK");
        assertThat(r.getRequestedCount()).isEqualTo(2);
        assertThat(r.getConflictCount()).isEqualTo(0);
        assertThat(r.getMessage()).contains("통과");
    }

    @Test
    @DisplayName("1건 충돌 → BLOCKED")
    void validate_1건충돌_BLOCKED() {
        List<String> input = List.of("VCS-001", "VCS-002", "VCS-003");
        given(eucrCrdtDtlMapper.findConflicts(input, null))
                .willReturn(List.of(row("VCS-002", "EUCR0099", "OP0099", "2025")));

        DoubleUsingResult r = validator.validate(input, null);

        assertThat(r.getSeverity()).isEqualTo("BLOCKED");
        assertThat(r.getRequestedCount()).isEqualTo(3);
        assertThat(r.getConflictCount()).isEqualTo(1);
        assertThat(r.getConflicts()).hasSize(1)
                .satisfies(list -> assertThat(list.get(0).getCrdtNo()).isEqualTo("VCS-002"));
    }

    @Test
    @DisplayName("수정 시 자기 EUCR 제외 → excludeEucrId 전달 후 OK")
    void validate_excludeEucrId_OK() {
        List<String> input = List.of("VCS-001");
        given(eucrCrdtDtlMapper.findConflicts(input, "EUCR0001")).willReturn(List.of());

        DoubleUsingResult r = validator.validate(input, "EUCR0001");

        assertThat(r.getSeverity()).isEqualTo("OK");
        verify(eucrCrdtDtlMapper).findConflicts(input, "EUCR0001");
    }

    @Test
    @DisplayName("BLOCKED 시 assertNotBlocked → CONFLICT(409)")
    void assertNotBlocked_BLOCKED_시_예외() {
        List<String> input = List.of("VCS-001");
        given(eucrCrdtDtlMapper.findConflicts(input, null))
                .willReturn(List.of(row("VCS-001", "EUCR0099", "OP0099", "2025")));

        assertThatThrownBy(() -> validator.assertNotBlocked(input, null))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(409));
    }

    @Test
    @DisplayName("OK 시 assertNotBlocked 통과")
    void assertNotBlocked_OK_통과() {
        List<String> input = List.of("VCS-001");
        given(eucrCrdtDtlMapper.findConflicts(input, null)).willReturn(List.of());

        assertThatCode(() -> validator.assertNotBlocked(input, null))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("입력 빈 리스트 → BAD_REQUEST(400)")
    void validate_빈리스트_BAD_REQUEST() {
        assertThatThrownBy(() -> validator.validate(List.of(), null))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));
    }

    @Test
    @DisplayName("입력 null → BAD_REQUEST(400)")
    void validate_null_BAD_REQUEST() {
        assertThatThrownBy(() -> validator.validate(null, null))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));
    }
}
