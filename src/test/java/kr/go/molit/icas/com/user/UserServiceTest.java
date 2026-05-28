package kr.go.molit.icas.com.user;

import kr.go.molit.icas.com.ognz.OgnzService;
import kr.go.molit.icas.com.ognz.domain.OgnzVO;
import kr.go.molit.icas.com.user.domain.UserSearch;
import kr.go.molit.icas.com.user.domain.UserVO;
import kr.go.molit.icas.common.dto.PageResponse;
import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.common.util.Sha256;
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
@DisplayName("UserService 단위 테스트")
class UserServiceTest {

    @Mock
    UserMapper userMapper;

    @Mock
    OgnzService ognzService;

    @InjectMocks
    UserService userService;

    private IcasUser molitUser;
    private IcasUser molitMaster;
    private IcasUser airlineUser;
    private IcasUser selfUser;

    private OgnzVO makeOgnz(String seCd) {
        OgnzVO ognz = new OgnzVO();
        ognz.setOgnzSeCd(seCd);
        return ognz;
    }

    @BeforeEach
    void setUp() {
        // OgnzService stub — requireValidOgnz 기본 통과 (any ognzId)
        given(ognzService.requireValidOgnz(any())).willReturn(makeOgnz("AIRLINE"));

        molitUser = IcasUser.builder()
                .userId("molit01").userNm("국토부 담당자")
                .ognzSeCd("MOLIT").ognzId("ORG_MOLIT").master(false)
                .roleIds(List.of("ADMIN")).build();

        molitMaster = IcasUser.builder()
                .userId("master01").userNm("관리자")
                .ognzSeCd("MOLIT").ognzId("ORG_MOLIT").master(true)
                .roleIds(List.of("ADMIN")).build();

        airlineUser = IcasUser.builder()
                .userId("airline01").userNm("항공사 담당자")
                .ognzSeCd("AIRLINE").ognzId("ORG_AIR01").oprtrId("OP0001").master(false)
                .roleIds(List.of("AIRLINE_USER")).build();

        selfUser = IcasUser.builder()
                .userId("user01").userNm("일반 사용자")
                .ognzSeCd("AIRLINE").ognzId("ORG_AIR01").master(false)
                .roleIds(List.of("USER")).build();
    }

    private UserVO validUserVO() {
        UserVO vo = new UserVO();
        vo.setUserId("user01");
        vo.setUserNm("홍길동");
        vo.setOgnzId("ORG_AIR01");
        vo.setEmlAddr("hong@example.com");
        vo.setMblphnNo("01012345678");
        vo.setPswdHash(Sha256.hex("Password1!"));
        return vo;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // createUser
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("사용자 등록 — 비밀번호 정책 통과 시 정상 등록")
    void createUser_정상등록() {
        UserVO vo = validUserVO();
        vo.setPswdHash(null);
        given(userMapper.selectByUserId("user01")).willReturn(null, validUserVO());
        given(userMapper.insertUser(any())).willReturn(1);

        UserVO result = userService.createUser(vo, "ValidPswd1!", molitUser);

        assertThat(result).isNotNull();
        assertThat(result.getPswdHash()).isNull();  // clearSensitiveFields 확인
        then(userMapper).should().insertUser(any());
    }

    @Test
    @DisplayName("사용자 등록 — 비밀번호 8자 미만 시 BAD_REQUEST 예외")
    void createUser_비밀번호_8자미만_BAD_REQUEST() {
        UserVO vo = validUserVO();
        vo.setPswdHash(null);
        given(userMapper.selectByUserId("user01")).willReturn(null);

        assertThatThrownBy(() -> userService.createUser(vo, "Ab1!", molitUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));
    }

    @Test
    @DisplayName("사용자 등록 — 영문만 포함(숫자·특수문자 없음) 시 BAD_REQUEST 예외")
    void createUser_비밀번호_영문만_BAD_REQUEST() {
        UserVO vo = validUserVO();
        vo.setPswdHash(null);
        given(userMapper.selectByUserId("user01")).willReturn(null);

        assertThatThrownBy(() -> userService.createUser(vo, "OnlyLetters", molitUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));
    }

    @Test
    @DisplayName("사용자 등록 — 특수문자 없는 비밀번호 시 BAD_REQUEST 예외")
    void createUser_비밀번호_특수문자없음_BAD_REQUEST() {
        UserVO vo = validUserVO();
        vo.setPswdHash(null);
        given(userMapper.selectByUserId("user01")).willReturn(null);

        assertThatThrownBy(() -> userService.createUser(vo, "Password123", molitUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));
    }

    @Test
    @DisplayName("사용자 등록 — 중복 userId 시 CONFLICT 예외")
    void createUser_중복userId_CONFLICT() {
        UserVO vo = validUserVO();
        vo.setPswdHash(null);
        given(userMapper.selectByUserId("user01")).willReturn(validUserVO());

        assertThatThrownBy(() -> userService.createUser(vo, "ValidPswd1!", molitUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(409));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getMe
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("본인 정보 조회 — pswdHash 가 null 로 클리어되어 반환")
    void getMe_pswdHash_null() {
        UserVO stored = validUserVO();
        given(userMapper.selectByUserId("user01")).willReturn(stored);

        UserVO result = userService.getMe(selfUser);

        assertThat(result.getPswdHash()).isNull();
        assertThat(result.getUserNm()).isEqualTo("홍길동"); // 마스킹 없음
    }

    // ─────────────────────────────────────────────────────────────────────────
    // searchUsers — 마스킹
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("목록 조회 — 타인 조회 시 개인정보 마스킹 적용 확인")
    void searchUsers_타인조회_마스킹적용() {
        UserVO stored = validUserVO();
        stored.setUserId("other01"); // 조회자(molitUser) 와 다른 ID
        UserSearch search = new UserSearch();
        given(userMapper.selectUsers(any())).willReturn(List.of(stored));
        given(userMapper.countUsers(any())).willReturn(1L);

        PageResponse<UserVO> result = userService.searchUsers(search, molitUser);

        UserVO row = result.getRows().get(0);
        assertThat(row.getPswdHash()).isNull();
        assertThat(row.getUserNm()).endsWith("*");       // 마스킹 적용
        assertThat(row.getEmlAddr()).contains("***");    // 마스킹 적용
    }

    @Test
    @DisplayName("목록 조회 — master 사용자는 마스킹 우회")
    void searchUsers_master_마스킹우회() {
        UserVO stored = validUserVO();
        stored.setUserId("other01");
        UserSearch search = new UserSearch();
        given(userMapper.selectUsers(any())).willReturn(List.of(stored));
        given(userMapper.countUsers(any())).willReturn(1L);

        PageResponse<UserVO> result = userService.searchUsers(search, molitMaster);

        UserVO row = result.getRows().get(0);
        assertThat(row.getUserNm()).isEqualTo("홍길동");  // 마스킹 없음
    }

    // ─────────────────────────────────────────────────────────────────────────
    // changePassword
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("비밀번호 변경 — 본인 변경 시 oldPassword 필수")
    void changePassword_본인_oldPassword_필수() {
        UserVO stored = validUserVO();
        given(userMapper.selectByUserId("user01")).willReturn(stored);

        assertThatThrownBy(() -> userService.changePassword("user01", null, "NewPswd1!", selfUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));
    }

    @Test
    @DisplayName("비밀번호 변경 — 관리자는 oldPassword 없이 변경 가능")
    void changePassword_관리자_oldPassword_우회() {
        UserVO stored = validUserVO();
        given(userMapper.selectByUserId("user01")).willReturn(stored);
        given(userMapper.changePassword(eq("user01"), anyString(), anyString())).willReturn(1);

        assertThatCode(() -> userService.changePassword("user01", null, "NewPswd1!", molitUser))
                .doesNotThrowAnyException();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // unlockAccount
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("계정 잠금 해제 — unlockAccount Mapper 메서드 호출 verify")
    void unlockAccount_Mapper_호출확인() {
        UserVO stored = validUserVO();
        given(userMapper.selectByUserId("user01")).willReturn(stored);
        given(userMapper.unlockAccount(eq("user01"), anyString())).willReturn(1);

        userService.unlockAccount("user01", molitUser);

        then(userMapper).should().unlockAccount(eq("user01"), eq("molit01"));
    }

    @Test
    @DisplayName("계정 잠금 해제 — 존재하지 않는 사용자 시 NOT_FOUND 예외")
    void unlockAccount_없는사용자_NOT_FOUND() {
        given(userMapper.selectByUserId("no_user")).willReturn(null);

        assertThatThrownBy(() -> userService.unlockAccount("no_user", molitUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(404));
    }
}
