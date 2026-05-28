package kr.go.molit.icas.com.user;

import kr.go.molit.icas.com.user.domain.UserSearch;
import kr.go.molit.icas.com.user.domain.UserVO;
import kr.go.molit.icas.common.dto.ApiResponse;
import kr.go.molit.icas.common.dto.PageResponse;
import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.IcasUser;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 사용자 관리 API.
 * Base URL: /api/com/user
 *
 * 목록/단건(타인)/등록/삭제/잠금해제는 MOLIT/KOTSA 전용.
 * 본인 정보 조회·수정, 비밀번호 변경은 본인 가능.
 */
@RestController
@RequestMapping("/api/com/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /* ── 조회 ── */

    /** GET /api/com/user — 사용자 검색 + 페이징 (MOLIT/KOTSA 전용) */
    @GetMapping
    public ApiResponse<PageResponse<UserVO>> list(@ModelAttribute UserSearch search,
                                                   @AuthenticationPrincipal IcasUser user) {
        assertMolitOrKotsa(user);
        return ApiResponse.ok(userService.searchUsers(search, user));
    }

    /**
     * GET /api/com/user/me — 본인 정보 조회 (마스킹 없음).
     * /{userId} 패턴과 겹치지 않도록 /me 를 먼저 선언.
     */
    @GetMapping("/me")
    public ApiResponse<UserVO> getMe(@AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(userService.getMe(user));
    }

    /** GET /api/com/user/{userId} — 사용자 단건 조회 (MOLIT/KOTSA 또는 본인) */
    @GetMapping("/{userId}")
    public ApiResponse<UserVO> getOne(@PathVariable String userId,
                                      @AuthenticationPrincipal IcasUser user) {
        assertMolitOrKotsaOrSelf(userId, user);
        return ApiResponse.ok(userService.getUser(userId, user));
    }

    /* ── 등록 / 수정 / 삭제 ── */

    /**
     * POST /api/com/user — 사용자 등록 (MOLIT/KOTSA 전용).
     * Body: UserVO 필드 + password (평문)
     */
    @PostMapping
    public ApiResponse<UserVO> create(@RequestBody UserCreateRequest req,
                                      @AuthenticationPrincipal IcasUser user) {
        assertMolitOrKotsa(user);
        UserVO vo = req.toUserVO();
        return ApiResponse.ok(userService.createUser(vo, req.getPassword(), user), "사용자가 등록되었습니다.");
    }

    /**
     * PUT /api/com/user/{userId} — 기본정보 수정 (비밀번호 제외).
     * MOLIT/KOTSA 또는 본인 가능.
     */
    @PutMapping("/{userId}")
    public ApiResponse<UserVO> update(@PathVariable String userId,
                                      @RequestBody UserVO vo,
                                      @AuthenticationPrincipal IcasUser user) {
        assertMolitOrKotsaOrSelf(userId, user);
        return ApiResponse.ok(userService.updateUser(userId, vo, user), "사용자 정보가 수정되었습니다.");
    }

    /** DELETE /api/com/user/{userId} — 소프트 삭제 (MOLIT/KOTSA 전용) */
    @DeleteMapping("/{userId}")
    public ApiResponse<Void> delete(@PathVariable String userId,
                                    @AuthenticationPrincipal IcasUser user) {
        assertMolitOrKotsa(user);
        userService.softDeleteUser(userId, user);
        return ApiResponse.ok(null, "사용자가 삭제되었습니다.");
    }

    /* ── 비밀번호 / 잠금 ── */

    /**
     * POST /api/com/user/{userId}/password — 비밀번호 변경.
     * Body: { "oldPassword": "(선택)", "newPassword": "(필수)" }
     * 본인 변경 시 oldPassword 필수. MOLIT/KOTSA 강제 변경 시 oldPassword 생략 가능.
     */
    @PostMapping("/{userId}/password")
    public ApiResponse<Void> changePassword(@PathVariable String userId,
                                            @RequestBody PasswordChangeRequest req,
                                            @AuthenticationPrincipal IcasUser user) {
        userService.changePassword(userId, req.getOldPassword(), req.getNewPassword(), user);
        return ApiResponse.ok(null, "비밀번호가 변경되었습니다.");
    }

    /** POST /api/com/user/{userId}/unlock — 계정 잠금 해제 (MOLIT/KOTSA 전용) */
    @PostMapping("/{userId}/unlock")
    public ApiResponse<Void> unlock(@PathVariable String userId,
                                    @AuthenticationPrincipal IcasUser user) {
        assertMolitOrKotsa(user);
        userService.unlockAccount(userId, user);
        return ApiResponse.ok(null, "계정 잠금이 해제되었습니다.");
    }

    /* ── 권한 검증 헬퍼 ── */

    private void assertMolitOrKotsa(IcasUser user) {
        if (!user.isMolitOrKotsa()) {
            throw BusinessException.forbidden("국토부 또는 교통안전공단 소속 사용자만 접근할 수 있습니다.");
        }
    }

    private void assertMolitOrKotsaOrSelf(String targetUserId, IcasUser user) {
        if (!user.isMolitOrKotsa() && !targetUserId.equals(user.getUserId())) {
            throw BusinessException.forbidden("본인 또는 MOLIT/KOTSA 관리자만 접근할 수 있습니다.");
        }
    }

    /* ── 요청 DTO ── */

    /**
     * 사용자 등록 요청 DTO.
     * UserVO 필드 + 평문 비밀번호를 함께 수신.
     */
    @Getter
    @Setter
    public static class UserCreateRequest {
        private String userId;
        private String userNm;
        private String ognzId;
        private String emlAddr;
        private String mblphnNo;
        private String tlphnNo;
        private String masterYn;
        /** 초기 비밀번호 평문 */
        private String password;

        public UserVO toUserVO() {
            UserVO vo = new UserVO();
            vo.setUserId(userId);
            vo.setUserNm(userNm);
            vo.setOgnzId(ognzId);
            vo.setEmlAddr(emlAddr);
            vo.setMblphnNo(mblphnNo);
            vo.setTlphnNo(tlphnNo);
            vo.setMasterYn(masterYn);
            return vo;
        }
    }

    /**
     * 비밀번호 변경 요청 DTO.
     */
    @Getter
    @Setter
    public static class PasswordChangeRequest {
        /** 현재 비밀번호 (본인 변경 시 필수, 관리자 초기화 시 생략) */
        private String oldPassword;
        /** 새 비밀번호 (필수) */
        private String newPassword;
    }
}
