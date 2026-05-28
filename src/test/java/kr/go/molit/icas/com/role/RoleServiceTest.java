package kr.go.molit.icas.com.role;

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
@DisplayName("RoleService 단위 테스트")
class RoleServiceTest {

    @Mock
    RoleMapper roleMapper;

    @InjectMocks
    RoleService roleService;

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

    private RoleVO validRoleVO() {
        RoleVO vo = new RoleVO();
        vo.setRoleId("ROLE_TEST");
        vo.setRoleNm("테스트 역할");
        vo.setOgnzSeCdAllowed("MOLIT,KOTSA");
        vo.setRoleDesc("테스트 역할 설명");
        return vo;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // listRoles
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("역할 전체 목록 조회 — Mapper 결과 반환")
    void listRoles_정상반환() {
        given(roleMapper.selectRoles()).willReturn(List.of(validRoleVO()));

        List<RoleVO> result = roleService.listRoles();

        assertThat(result).hasSize(1);
        then(roleMapper).should().selectRoles();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getRole
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("단건 조회 — 존재하는 roleId 정상 반환")
    void getRole_존재하는ID_반환() {
        given(roleMapper.selectRole("ROLE_TEST")).willReturn(validRoleVO());

        RoleVO result = roleService.getRole("ROLE_TEST");

        assertThat(result.getRoleId()).isEqualTo("ROLE_TEST");
    }

    @Test
    @DisplayName("단건 조회 — 존재하지 않는 roleId 시 NOT_FOUND 예외")
    void getRole_없는ID_NOT_FOUND() {
        given(roleMapper.selectRole("NO_ROLE")).willReturn(null);

        assertThatThrownBy(() -> roleService.getRole("NO_ROLE"))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(404));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // createRole
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("역할 등록 — MOLIT 사용자 정상 등록 (MOLIT,KOTSA 허용 코드)")
    void createRole_정상등록_MOLIT() {
        RoleVO vo = validRoleVO();
        given(roleMapper.existsRoleId("ROLE_TEST")).willReturn(false);
        given(roleMapper.insertRole(any())).willReturn(1);

        RoleVO result = roleService.createRole(vo, molitUser);

        assertThat(result.getRoleId()).isEqualTo("ROLE_TEST");
        then(roleMapper).should().insertRole(any());
    }

    @Test
    @DisplayName("역할 등록 — AIRLINE 사용자 시도 시 FORBIDDEN 예외")
    void createRole_AIRLINE_FORBIDDEN() {
        RoleVO vo = validRoleVO();

        assertThatThrownBy(() -> roleService.createRole(vo, airlineUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(403));

        then(roleMapper).should(never()).insertRole(any());
    }

    @Test
    @DisplayName("역할 등록 — ognzSeCdAllowed 잘못된 토큰(MOLIT,XYZ) 시 BAD_REQUEST 예외")
    void createRole_잘못된ognzSeCdAllowed_BAD_REQUEST() {
        RoleVO vo = validRoleVO();
        vo.setOgnzSeCdAllowed("MOLIT,XYZ");

        assertThatThrownBy(() -> roleService.createRole(vo, molitUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));
    }

    @Test
    @DisplayName("역할 등록 — 중복 roleId 시 CONFLICT 예외")
    void createRole_중복ID_CONFLICT() {
        RoleVO vo = validRoleVO();
        given(roleMapper.existsRoleId("ROLE_TEST")).willReturn(true);

        assertThatThrownBy(() -> roleService.createRole(vo, molitUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(409));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // updateRole
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("역할 수정 — MOLIT 사용자 정상 수정")
    void updateRole_정상수정() {
        RoleVO vo = validRoleVO();
        given(roleMapper.updateRole(any())).willReturn(1);

        assertThatCode(() -> roleService.updateRole("ROLE_TEST", vo, molitUser))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("역할 수정 — 존재하지 않는 ID 시 NOT_FOUND 예외")
    void updateRole_없는ID_NOT_FOUND() {
        RoleVO vo = validRoleVO();
        given(roleMapper.updateRole(any())).willReturn(0);

        assertThatThrownBy(() -> roleService.updateRole("NO_ROLE", vo, molitUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(404));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // softDeleteRole
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("역할 소프트 삭제 — MOLIT 사용자 정상 삭제")
    void softDeleteRole_정상삭제() {
        given(roleMapper.softDeleteRole(eq("ROLE_TEST"), anyString())).willReturn(1);

        assertThatCode(() -> roleService.softDeleteRole("ROLE_TEST", molitUser))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("역할 소프트 삭제 — 존재하지 않는 ID 시 NOT_FOUND 예외")
    void softDeleteRole_없는ID_NOT_FOUND() {
        given(roleMapper.softDeleteRole(eq("NO_ROLE"), anyString())).willReturn(0);

        assertThatThrownBy(() -> roleService.softDeleteRole("NO_ROLE", molitUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(404));
    }
}
