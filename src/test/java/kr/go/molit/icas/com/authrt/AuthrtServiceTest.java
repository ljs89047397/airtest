package kr.go.molit.icas.com.authrt;

import kr.go.molit.icas.com.authrt.domain.AuthrtVO;
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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthrtService 단위 테스트")
class AuthrtServiceTest {

    @Mock
    AuthrtMapper authrtMapper;

    @InjectMocks
    AuthrtService authrtService;

    private IcasUser molitUser;
    private IcasUser airlineUser;

    @BeforeEach
    void setUp() {
        molitUser = IcasUser.builder()
                .userId("molit01").userNm("국토부 담당자")
                .ognzSeCd("MOLIT").ognzId("ORG_MOLIT").master(false)
                .roleIds(List.of("ADMIN")).build();

        airlineUser = IcasUser.builder()
                .userId("airline01").userNm("항공사 담당자")
                .ognzSeCd("AIRLINE").ognzId("ORG_AIR01").oprtrId("OP0001").master(false)
                .roleIds(List.of("AIRLINE_USER")).build();
    }

    private AuthrtVO validVO() {
        AuthrtVO vo = new AuthrtVO();
        vo.setAuthrtId("AUTHRT_TEST");
        vo.setAuthrtNm("테스트 권한");
        vo.setAuthrtDesc("테스트 용도");
        return vo;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // listAuthrts
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("권한 전체 목록 조회 — Mapper 결과 반환")
    void listAuthrts_정상반환() {
        given(authrtMapper.selectAuthrts()).willReturn(List.of(validVO()));

        List<AuthrtVO> result = authrtService.listAuthrts();

        assertThat(result).hasSize(1);
        then(authrtMapper).should().selectAuthrts();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getAuthrt
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("단건 조회 — 존재하는 authrtId 정상 반환")
    void getAuthrt_존재하는ID_반환() {
        given(authrtMapper.selectAuthrt("AUTHRT_TEST")).willReturn(validVO());

        AuthrtVO result = authrtService.getAuthrt("AUTHRT_TEST");

        assertThat(result.getAuthrtId()).isEqualTo("AUTHRT_TEST");
    }

    @Test
    @DisplayName("단건 조회 — 존재하지 않는 authrtId 시 NOT_FOUND 예외")
    void getAuthrt_없는ID_NOT_FOUND() {
        given(authrtMapper.selectAuthrt("NO_AUTHRT")).willReturn(null);

        assertThatThrownBy(() -> authrtService.getAuthrt("NO_AUTHRT"))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(404));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // createAuthrt
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("권한 등록 — MOLIT 사용자 정상 등록")
    void createAuthrt_정상등록() {
        AuthrtVO vo = validVO();
        given(authrtMapper.existsAuthrtId("AUTHRT_TEST")).willReturn(false);
        given(authrtMapper.insertAuthrt(any())).willReturn(1);

        AuthrtVO result = authrtService.createAuthrt(vo, molitUser);

        assertThat(result.getAuthrtId()).isEqualTo("AUTHRT_TEST");
        then(authrtMapper).should().insertAuthrt(any());
    }

    @Test
    @DisplayName("권한 등록 — AIRLINE 사용자 시도 시 FORBIDDEN 예외")
    void createAuthrt_AIRLINE_FORBIDDEN() {
        AuthrtVO vo = validVO();

        assertThatThrownBy(() -> authrtService.createAuthrt(vo, airlineUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(403));

        then(authrtMapper).should(never()).insertAuthrt(any());
    }

    @Test
    @DisplayName("권한 등록 — 중복 authrtId 시 CONFLICT 예외")
    void createAuthrt_중복ID_CONFLICT() {
        AuthrtVO vo = validVO();
        given(authrtMapper.existsAuthrtId("AUTHRT_TEST")).willReturn(true);

        assertThatThrownBy(() -> authrtService.createAuthrt(vo, molitUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(409));
    }

    @Test
    @DisplayName("권한 등록 — authrtId blank 시 BAD_REQUEST 예외")
    void createAuthrt_빈authrtId_BAD_REQUEST() {
        AuthrtVO vo = validVO();
        vo.setAuthrtId("");

        assertThatThrownBy(() -> authrtService.createAuthrt(vo, molitUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // updateAuthrt
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("권한 수정 — MOLIT 사용자 정상 수정")
    void updateAuthrt_정상수정() {
        AuthrtVO vo = validVO();
        given(authrtMapper.updateAuthrt(any())).willReturn(1);

        assertThatCode(() -> authrtService.updateAuthrt("AUTHRT_TEST", vo, molitUser))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("권한 수정 — 존재하지 않는 ID 시 NOT_FOUND 예외")
    void updateAuthrt_없는ID_NOT_FOUND() {
        AuthrtVO vo = validVO();
        given(authrtMapper.updateAuthrt(any())).willReturn(0);

        assertThatThrownBy(() -> authrtService.updateAuthrt("NO_AUTHRT", vo, molitUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(404));
    }

    @Test
    @DisplayName("권한 수정 — 권한 없는 사용자 시 FORBIDDEN 예외")
    void updateAuthrt_AIRLINE_FORBIDDEN() {
        AuthrtVO vo = validVO();

        assertThatThrownBy(() -> authrtService.updateAuthrt("AUTHRT_TEST", vo, airlineUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(403));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // softDeleteAuthrt
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("권한 소프트 삭제 — MOLIT 사용자 정상 삭제")
    void softDeleteAuthrt_정상삭제() {
        given(authrtMapper.softDeleteAuthrt(eq("AUTHRT_TEST"), anyString())).willReturn(1);

        assertThatCode(() -> authrtService.softDeleteAuthrt("AUTHRT_TEST", molitUser))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("권한 소프트 삭제 — 존재하지 않는 ID 시 NOT_FOUND 예외")
    void softDeleteAuthrt_없는ID_NOT_FOUND() {
        given(authrtMapper.softDeleteAuthrt(eq("NO_AUTHRT"), anyString())).willReturn(0);

        assertThatThrownBy(() -> authrtService.softDeleteAuthrt("NO_AUTHRT", molitUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(404));
    }
}
