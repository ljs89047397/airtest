package kr.go.molit.icas.com.role;

import kr.go.molit.icas.com.role.domain.RoleVO;
import kr.go.molit.icas.com.role.domain.UserRoleMpngVO;
import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.IcasUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserRoleMpngService 단위 테스트")
class UserRoleMpngServiceTest {

    @Mock
    UserRoleMpngMapper userRoleMpngMapper;

    @Mock
    RoleMapper roleMapper;

    @InjectMocks
    UserRoleMpngService userRoleMpngService;

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

    private RoleVO airlineRole() {
        RoleVO vo = new RoleVO();
        vo.setRoleId("ROLE_AIRLINE");
        vo.setRoleNm("항공사 역할");
        vo.setOgnzSeCdAllowed("AIRLINE");
        return vo;
    }

    private RoleVO molitRole() {
        RoleVO vo = new RoleVO();
        vo.setRoleId("ROLE_MOLIT");
        vo.setRoleNm("국토부 역할");
        vo.setOgnzSeCdAllowed("MOLIT");
        return vo;
    }

    private UserRoleMpngVO sampleMpng() {
        UserRoleMpngVO vo = new UserRoleMpngVO();
        vo.setUserId("user01");
        vo.setRoleId("ROLE_AIRLINE");
        vo.setUseBgngDt(LocalDateTime.now());
        vo.setUseEndDt(LocalDateTime.of(9999, 12, 31, 23, 59, 59));
        return vo;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // selectActiveRolesByUser
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("활성 역할 조회 — userId 기준 목록 정상 반환")
    void selectActiveRolesByUser_정상반환() {
        given(userRoleMpngMapper.selectActiveRolesByUser("user01")).willReturn(List.of(sampleMpng()));

        List<UserRoleMpngVO> result = userRoleMpngService.selectActiveRolesByUser("user01");

        assertThat(result).hasSize(1);
        then(userRoleMpngMapper).should().selectActiveRolesByUser("user01");
    }

    @Test
    @DisplayName("활성 역할 조회 — userId null 시 BAD_REQUEST 예외")
    void selectActiveRolesByUser_userId_null_BAD_REQUEST() {
        assertThatThrownBy(() -> userRoleMpngService.selectActiveRolesByUser(null))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // selectRoleHistory
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("역할 이력 조회 — userId 기준 전체 이력 정상 반환")
    void selectRoleHistory_정상반환() {
        given(userRoleMpngMapper.selectRoleHistory("user01")).willReturn(List.of(sampleMpng()));

        List<UserRoleMpngVO> result = userRoleMpngService.selectRoleHistory("user01");

        assertThat(result).hasSize(1);
        then(userRoleMpngMapper).should().selectRoleHistory("user01");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // grantRole
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("역할 부여 — AIRLINE 사용자에게 AIRLINE 전용 역할 정상 부여")
    void grantRole_정상부여_AIRLINE_사용자_AIRLINE_역할() {
        given(roleMapper.selectRole("ROLE_AIRLINE")).willReturn(airlineRole());
        given(userRoleMpngMapper.existsActive("user01", "ROLE_AIRLINE")).willReturn(false);
        given(userRoleMpngMapper.grantRole(any())).willReturn(1);

        assertThatCode(() -> userRoleMpngService.grantRole("user01", "ROLE_AIRLINE", "AIRLINE", molitUser))
                .doesNotThrowAnyException();

        then(userRoleMpngMapper).should().grantRole(any());
    }

    @Test
    @DisplayName("역할 부여 — AIRLINE 사용자에게 MOLIT 전용 역할 부여 시 FORBIDDEN 예외")
    void grantRole_AIRLINE사용자_MOLIT역할_FORBIDDEN() {
        given(roleMapper.selectRole("ROLE_MOLIT")).willReturn(molitRole());

        assertThatThrownBy(() -> userRoleMpngService.grantRole("user01", "ROLE_MOLIT", "AIRLINE", molitUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(403));

        then(userRoleMpngMapper).should(never()).grantRole(any());
    }

    @Test
    @DisplayName("역할 부여 — 이미 유효한 매핑 존재 시 CONFLICT 예외")
    void grantRole_이미부여된역할_CONFLICT() {
        given(roleMapper.selectRole("ROLE_AIRLINE")).willReturn(airlineRole());
        given(userRoleMpngMapper.existsActive("user01", "ROLE_AIRLINE")).willReturn(true);

        assertThatThrownBy(() -> userRoleMpngService.grantRole("user01", "ROLE_AIRLINE", "AIRLINE", molitUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(409));
    }

    @Test
    @DisplayName("역할 부여 — 권한 없는 사용자(AIRLINE) 가 시도 시 FORBIDDEN 예외")
    void grantRole_권한없는사용자_FORBIDDEN() {
        assertThatThrownBy(() -> userRoleMpngService.grantRole("user01", "ROLE_AIRLINE", "AIRLINE", airlineUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(403));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // revokeRole
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("역할 회수 — revokeRole 호출로 use_end_dt 갱신 verify")
    void revokeRole_정상회수_Mapper_호출확인() {
        given(userRoleMpngMapper.revokeRole(eq("user01"), eq("ROLE_AIRLINE"), anyString())).willReturn(1);

        userRoleMpngService.revokeRole("user01", "ROLE_AIRLINE", molitUser);

        then(userRoleMpngMapper).should().revokeRole(eq("user01"), eq("ROLE_AIRLINE"), eq("molit01"));
    }

    @Test
    @DisplayName("역할 회수 — 활성 매핑 미존재 시 NOT_FOUND 예외")
    void revokeRole_활성매핑_없음_NOT_FOUND() {
        given(userRoleMpngMapper.revokeRole(any(), any(), any())).willReturn(0);

        assertThatThrownBy(() -> userRoleMpngService.revokeRole("user01", "ROLE_AIRLINE", molitUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(404));
    }
}
