package kr.go.molit.icas.com.authrt;

import kr.go.molit.icas.com.authrt.domain.AuthrtRoleMpngVO;
import kr.go.molit.icas.com.authrt.domain.AuthrtVO;
import kr.go.molit.icas.com.role.RoleMapper;
import kr.go.molit.icas.com.role.domain.RoleVO;
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
@DisplayName("AuthrtRoleMpngService 단위 테스트")
class AuthrtRoleMpngServiceTest {

    @Mock
    AuthrtRoleMpngMapper authrtRoleMpngMapper;

    @Mock
    AuthrtMapper authrtMapper;

    @Mock
    RoleMapper roleMapper;

    @InjectMocks
    AuthrtRoleMpngService authrtRoleMpngService;

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

    private AuthrtVO sampleAuthrt() {
        AuthrtVO vo = new AuthrtVO();
        vo.setAuthrtId("AUTHRT_TEST");
        vo.setAuthrtNm("테스트 권한");
        return vo;
    }

    private RoleVO sampleRole() {
        RoleVO vo = new RoleVO();
        vo.setRoleId("ROLE_TEST");
        vo.setRoleNm("테스트 역할");
        vo.setOgnzSeCdAllowed("MOLIT");
        return vo;
    }

    private AuthrtRoleMpngVO sampleMpng() {
        AuthrtRoleMpngVO vo = new AuthrtRoleMpngVO();
        vo.setAuthrtId("AUTHRT_TEST");
        vo.setRoleId("ROLE_TEST");
        vo.setAuthrtNm("테스트 권한");
        vo.setRoleNm("테스트 역할");
        return vo;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // selectByRole
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("역할 기준 권한 목록 조회 — 정상 반환")
    void selectByRole_정상반환() {
        given(authrtRoleMpngMapper.selectByRole("ROLE_TEST")).willReturn(List.of(sampleMpng()));

        List<AuthrtRoleMpngVO> result = authrtRoleMpngService.selectByRole("ROLE_TEST");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRoleId()).isEqualTo("ROLE_TEST");
        then(authrtRoleMpngMapper).should().selectByRole("ROLE_TEST");
    }

    @Test
    @DisplayName("역할 기준 권한 목록 조회 — roleId null 시 BAD_REQUEST 예외")
    void selectByRole_roleId_null_BAD_REQUEST() {
        assertThatThrownBy(() -> authrtRoleMpngService.selectByRole(null))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // selectByAuthrt
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("권한 기준 역할 목록 조회 — 정상 반환")
    void selectByAuthrt_정상반환() {
        given(authrtRoleMpngMapper.selectByAuthrt("AUTHRT_TEST")).willReturn(List.of(sampleMpng()));

        List<AuthrtRoleMpngVO> result = authrtRoleMpngService.selectByAuthrt("AUTHRT_TEST");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAuthrtId()).isEqualTo("AUTHRT_TEST");
        then(authrtRoleMpngMapper).should().selectByAuthrt("AUTHRT_TEST");
    }

    @Test
    @DisplayName("권한 기준 역할 목록 조회 — authrtId blank 시 BAD_REQUEST 예외")
    void selectByAuthrt_authrtId_blank_BAD_REQUEST() {
        assertThatThrownBy(() -> authrtRoleMpngService.selectByAuthrt(""))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // addMapping
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("권한-역할 매핑 추가 — MOLIT 사용자 정상 추가")
    void addMapping_정상추가() {
        given(authrtMapper.selectAuthrt("AUTHRT_TEST")).willReturn(sampleAuthrt());
        given(roleMapper.selectRole("ROLE_TEST")).willReturn(sampleRole());
        given(authrtRoleMpngMapper.existsActive("AUTHRT_TEST", "ROLE_TEST")).willReturn(false);
        given(authrtRoleMpngMapper.addMapping(any())).willReturn(1);

        assertThatCode(() -> authrtRoleMpngService.addMapping("AUTHRT_TEST", "ROLE_TEST", molitUser))
                .doesNotThrowAnyException();

        then(authrtRoleMpngMapper).should().addMapping(any());
    }

    @Test
    @DisplayName("권한-역할 매핑 추가 — 권한 없는 사용자(AIRLINE) 시 FORBIDDEN 예외")
    void addMapping_AIRLINE_FORBIDDEN() {
        assertThatThrownBy(() -> authrtRoleMpngService.addMapping("AUTHRT_TEST", "ROLE_TEST", airlineUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(403));

        then(authrtRoleMpngMapper).should(never()).addMapping(any());
    }

    @Test
    @DisplayName("권한-역할 매핑 추가 — 존재하지 않는 authrtId 시 NOT_FOUND 예외")
    void addMapping_없는authrt_NOT_FOUND() {
        given(authrtMapper.selectAuthrt("NO_AUTHRT")).willReturn(null);

        assertThatThrownBy(() -> authrtRoleMpngService.addMapping("NO_AUTHRT", "ROLE_TEST", molitUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(404));
    }

    @Test
    @DisplayName("권한-역할 매핑 추가 — 이미 유효한 매핑 존재 시 CONFLICT 예외")
    void addMapping_중복매핑_CONFLICT() {
        given(authrtMapper.selectAuthrt("AUTHRT_TEST")).willReturn(sampleAuthrt());
        given(roleMapper.selectRole("ROLE_TEST")).willReturn(sampleRole());
        given(authrtRoleMpngMapper.existsActive("AUTHRT_TEST", "ROLE_TEST")).willReturn(true);

        assertThatThrownBy(() -> authrtRoleMpngService.addMapping("AUTHRT_TEST", "ROLE_TEST", molitUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(409));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // removeMapping
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("권한-역할 매핑 소프트 삭제 — MOLIT 사용자 정상 삭제")
    void removeMapping_정상삭제() {
        given(authrtRoleMpngMapper.removeMapping(eq("AUTHRT_TEST"), eq("ROLE_TEST"), anyString())).willReturn(1);

        assertThatCode(() -> authrtRoleMpngService.removeMapping("AUTHRT_TEST", "ROLE_TEST", molitUser))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("권한-역할 매핑 소프트 삭제 — 미존재 매핑 삭제 시 NOT_FOUND 예외")
    void removeMapping_미존재_NOT_FOUND() {
        given(authrtRoleMpngMapper.removeMapping(any(), any(), any())).willReturn(0);

        assertThatThrownBy(() -> authrtRoleMpngService.removeMapping("AUTHRT_TEST", "NO_ROLE", molitUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(404));
    }
}
