package kr.go.molit.icas.com.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import kr.go.molit.icas.com.user.domain.UserVO;
import kr.go.molit.icas.common.dto.PageResponse;
import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.exception.GlobalExceptionHandler;
import kr.go.molit.icas.common.security.IcasUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * UserController 슬라이스 테스트.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserController 슬라이스 테스트")
class UserControllerTest {

    MockMvc mockMvc;

    @Mock
    UserService userService;

    ObjectMapper objectMapper;

    IcasUser molitUser;
    IcasUser airlineUser;
    IcasUser selfUser;

    @BeforeEach
    void setUp() {
        UserController controller = new UserController(userService);
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver())
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        molitUser = IcasUser.builder()
                .userId("molit01").userNm("국토부 담당자")
                .ognzSeCd("MOLIT").ognzId("ORG_MOLIT").master(false)
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

    private UsernamePasswordAuthenticationToken authToken(IcasUser user) {
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }

    private UserVO sampleUserVO() {
        UserVO vo = new UserVO();
        vo.setUserId("user01");
        vo.setUserNm("홍길동");
        vo.setOgnzId("ORG_AIR01");
        vo.setEmlAddr("hong@example.com");
        vo.setMblphnNo("01012345678");
        // pswdHash 는 Service 에서 클리어 → null
        return vo;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/com/user/me
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("/me 본인 정보 조회 — pswdHash null, 마스킹 없이 200 반환")
    void getMe_본인정보_200() throws Exception {
        UserVO me = sampleUserVO();
        given(userService.getMe(any(IcasUser.class))).willReturn(me);

        mockMvc.perform(get("/api/com/user/me")
                        .with(authentication(authToken(selfUser)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value("user01"))
                .andExpect(jsonPath("$.data.pswdHash").doesNotExist()); // null → JsonInclude.NON_NULL
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/com/user
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("목록 조회 — MOLIT 사용자 200 반환")
    void list_MOLIT_200() throws Exception {
        PageResponse<UserVO> page = new PageResponse<>(List.of(sampleUserVO()), 1, 20, 1L);
        given(userService.searchUsers(any(), any(IcasUser.class))).willReturn(page);

        mockMvc.perform(get("/api/com/user")
                        .with(authentication(authToken(molitUser)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.rows").isArray());
    }

    @Test
    @DisplayName("목록 조회 — AIRLINE 사용자는 Controller 에서 403 반환")
    void list_AIRLINE_FORBIDDEN_403() throws Exception {
        mockMvc.perform(get("/api/com/user")
                        .with(authentication(authToken(airlineUser)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));

        then(userService).should(never()).searchUsers(any(), any());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/com/user
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("사용자 등록 — MOLIT 사용자 정상 등록 200 반환")
    void create_MOLIT_정상등록_200() throws Exception {
        UserVO created = sampleUserVO();
        given(userService.createUser(any(UserVO.class), anyString(), any(IcasUser.class))).willReturn(created);

        Map<String, String> req = Map.of(
                "userId", "user01",
                "userNm", "홍길동",
                "ognzId", "ORG_AIR01",
                "password", "ValidPswd1!"
        );

        mockMvc.perform(post("/api/com/user")
                        .with(authentication(authToken(molitUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("사용자가 등록되었습니다."));
    }

    @Test
    @DisplayName("사용자 등록 — 비밀번호 정책 위반 시 Service BAD_REQUEST → 400")
    void create_비밀번호정책위반_BAD_REQUEST_400() throws Exception {
        given(userService.createUser(any(), anyString(), any()))
                .willThrow(BusinessException.badRequest("비밀번호는 9자 이상이며 영문, 숫자, 특수문자를 각각 1자 이상 포함해야 합니다."));

        Map<String, String> req = Map.of(
                "userId", "user01",
                "userNm", "홍길동",
                "ognzId", "ORG_AIR01",
                "password", "abc"
        );

        mockMvc.perform(post("/api/com/user")
                        .with(authentication(authToken(molitUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    @Test
    @DisplayName("사용자 등록 — 중복 userId 시 Service CONFLICT → 409")
    void create_중복userId_CONFLICT_409() throws Exception {
        given(userService.createUser(any(), anyString(), any()))
                .willThrow(BusinessException.conflict("이미 사용 중인 사용자 ID 입니다: user01"));

        Map<String, String> req = Map.of(
                "userId", "user01",
                "userNm", "홍길동",
                "ognzId", "ORG_AIR01",
                "password", "ValidPswd1!"
        );

        mockMvc.perform(post("/api/com/user")
                        .with(authentication(authToken(molitUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"));
    }

    @Test
    @DisplayName("사용자 등록 — AIRLINE 사용자 시도 시 Controller 에서 403 반환")
    void create_AIRLINE_FORBIDDEN_403() throws Exception {
        Map<String, String> req = Map.of(
                "userId", "user02",
                "userNm", "테스트",
                "ognzId", "ORG_AIR01",
                "password", "ValidPswd1!"
        );

        mockMvc.perform(post("/api/com/user")
                        .with(authentication(authToken(airlineUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));

        then(userService).should(never()).createUser(any(), any(), any());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/com/user/{userId}/password
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("비밀번호 변경 — 본인 oldPassword 미입력 시 Service BAD_REQUEST → 400")
    void changePassword_본인_oldPassword_미입력_400() throws Exception {
        willThrow(BusinessException.badRequest("현재 비밀번호를 입력해야 합니다."))
                .given(userService).changePassword(eq("user01"), isNull(), anyString(), any(IcasUser.class));

        Map<String, String> req = Map.of("newPassword", "NewPswd1!");

        mockMvc.perform(post("/api/com/user/user01/password")
                        .with(authentication(authToken(selfUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    @Test
    @DisplayName("비밀번호 변경 — 관리자(MOLIT) oldPassword 없이 변경 성공")
    void changePassword_관리자_oldPassword_없이_200() throws Exception {
        willDoNothing().given(userService)
                .changePassword(eq("user01"), isNull(), anyString(), any(IcasUser.class));

        Map<String, String> req = Map.of("newPassword", "NewPswd1!");

        mockMvc.perform(post("/api/com/user/user01/password")
                        .with(authentication(authToken(molitUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("비밀번호가 변경되었습니다."));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/com/user/{userId}/unlock
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("계정 잠금 해제 — MOLIT 사용자 정상 호출 200 반환")
    void unlock_MOLIT_200() throws Exception {
        willDoNothing().given(userService).unlockAccount(eq("user01"), any(IcasUser.class));

        mockMvc.perform(post("/api/com/user/user01/unlock")
                        .with(authentication(authToken(molitUser))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("계정 잠금이 해제되었습니다."));
    }

    @Test
    @DisplayName("계정 잠금 해제 — AIRLINE 사용자 시도 시 403 반환")
    void unlock_AIRLINE_FORBIDDEN_403() throws Exception {
        mockMvc.perform(post("/api/com/user/user01/unlock")
                        .with(authentication(authToken(airlineUser))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));

        then(userService).should(never()).unlockAccount(any(), any());
    }
}
