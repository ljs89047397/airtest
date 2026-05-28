package kr.go.molit.icas.com.vrfcn;

import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.IcasUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VrfcnAssgnService 단위 테스트")
class VrfcnAssgnServiceTest {

    @Mock
    VrfcnAssgnMapper vrfcnAssgnMapper;

    @InjectMocks
    VrfcnAssgnService vrfcnAssgnService;

    // ── fixture ──
    private IcasUser molitUser;
    private IcasUser verifierUser;

    @BeforeEach
    void setUpFixtures() {
        molitUser = IcasUser.builder()
                .userId("molit01").userNm("국토부 담당자")
                .ognzSeCd("MOLIT").ognzId("ORG_MOLIT").master(false)
                .roleIds(List.of("ADMIN")).build();

        verifierUser = IcasUser.builder()
                .userId("verifier01").userNm("검증기관 담당자")
                .ognzSeCd("VERIFIER").ognzId("ORG_VRF01").vrfcnInstId("VI0001").master(false)
                .roleIds(List.of("VERIFIER_USER")).build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // selectAssgnList
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("전체 배정 목록 조회 — rprtYr 포함 시 Mapper 에 그대로 전달")
    void selectAssgnList_연도필터_정상() {
        Map<String, Object> row = Map.of("vrfcnInstId", "VI0001", "oprtrId", "OP0001", "rprtYr", "2025");
        given(vrfcnAssgnMapper.selectAssgnList("2025")).willReturn(List.of(row));

        List<Map<String, Object>> result = vrfcnAssgnService.selectAssgnList("2025");

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).containsEntry("vrfcnInstId", "VI0001");
        then(vrfcnAssgnMapper).should().selectAssgnList("2025");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // selectAssignedOprtrIds
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("특정 검증기관 배정 항공사 목록 조회 — 정상")
    void selectAssignedOprtrIds_정상() {
        given(vrfcnAssgnMapper.selectAssignedOprtrIds("VI0001", "2025"))
                .willReturn(List.of("OP0001", "OP0002"));

        List<String> result = vrfcnAssgnService.selectAssignedOprtrIds("VI0001", "2025");

        assertThat(result).containsExactly("OP0001", "OP0002");
    }

    @Test
    @DisplayName("특정 검증기관 배정 항공사 목록 조회 — vrfcnInstId 누락 시 BAD_REQUEST")
    void selectAssignedOprtrIds_vrfcnInstId_누락_BAD_REQUEST() {
        assertThatThrownBy(() -> vrfcnAssgnService.selectAssignedOprtrIds(null, "2025"))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));
    }

    @Test
    @DisplayName("특정 검증기관 배정 항공사 목록 조회 — rprtYr 누락 시 BAD_REQUEST")
    void selectAssignedOprtrIds_rprtYr_누락_BAD_REQUEST() {
        assertThatThrownBy(() -> vrfcnAssgnService.selectAssignedOprtrIds("VI0001", ""))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // createAssgn
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("배정 등록 — 정상 케이스: insert 호출")
    void createAssgn_정상() {
        given(vrfcnAssgnMapper.existsAssgn("VI0001", "OP0001", "2025")).willReturn(false);
        given(vrfcnAssgnMapper.insertAssgn("VI0001", "OP0001", "2025", "molit01")).willReturn(1);

        // 예외 없이 완료되어야 한다
        assertThatCode(() -> vrfcnAssgnService.createAssgn("VI0001", "OP0001", "2025", molitUser))
                .doesNotThrowAnyException();

        then(vrfcnAssgnMapper).should().insertAssgn("VI0001", "OP0001", "2025", "molit01");
    }

    @Test
    @DisplayName("배정 등록 — 이미 존재하는 배정 시 CONFLICT 예외")
    void createAssgn_중복배정_CONFLICT() {
        given(vrfcnAssgnMapper.existsAssgn("VI0001", "OP0001", "2025")).willReturn(true);

        assertThatThrownBy(() -> vrfcnAssgnService.createAssgn("VI0001", "OP0001", "2025", molitUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> {
                    assertThat(((BusinessException) e).getStatus()).isEqualTo(409);
                    assertThat(((BusinessException) e).getCode()).isEqualTo("CONFLICT");
                });

        then(vrfcnAssgnMapper).should(never()).insertAssgn(any(), any(), any(), any());
    }

    @Test
    @DisplayName("배정 등록 — 보고연도 4자리 미충족 시 BAD_REQUEST 예외")
    void createAssgn_rprtYr_형식오류_BAD_REQUEST() {
        assertThatThrownBy(() -> vrfcnAssgnService.createAssgn("VI0001", "OP0001", "25", molitUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));
    }

    @Test
    @DisplayName("배정 등록 — oprtrId 누락 시 BAD_REQUEST 예외")
    void createAssgn_oprtrId_누락_BAD_REQUEST() {
        assertThatThrownBy(() -> vrfcnAssgnService.createAssgn("VI0001", "  ", "2025", molitUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // softDeleteAssgn
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("배정 소프트삭제 — 정상 케이스")
    void softDeleteAssgn_정상() {
        given(vrfcnAssgnMapper.softDeleteAssgn("VI0001", "OP0001", "2025", "molit01")).willReturn(1);

        assertThatCode(() -> vrfcnAssgnService.softDeleteAssgn("VI0001", "OP0001", "2025", molitUser))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("배정 소프트삭제 — 존재하지 않는 배정 삭제 시 NOT_FOUND 예외")
    void softDeleteAssgn_없는배정_NOT_FOUND() {
        given(vrfcnAssgnMapper.softDeleteAssgn("VI0001", "OP9999", "2025", "molit01")).willReturn(0);

        assertThatThrownBy(() -> vrfcnAssgnService.softDeleteAssgn("VI0001", "OP9999", "2025", molitUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(404));
    }
}
