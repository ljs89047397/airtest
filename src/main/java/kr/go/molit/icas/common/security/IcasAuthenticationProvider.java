package kr.go.molit.icas.common.security;

import kr.go.molit.icas.com.auth.AuthMapper;
import kr.go.molit.icas.com.auth.LoginHistoryService;
import kr.go.molit.icas.com.auth.domain.UserAuthInfo;
import kr.go.molit.icas.common.util.Sha256;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
public class IcasAuthenticationProvider implements AuthenticationProvider {

    private final AuthMapper authMapper;
    private final LoginHistoryService loginHistoryService;

    @Autowired
    public IcasAuthenticationProvider(AuthMapper authMapper, LoginHistoryService loginHistoryService) {
        this.authMapper = authMapper;
        this.loginHistoryService = loginHistoryService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String userId   = String.valueOf(authentication.getPrincipal());
        String password = String.valueOf(authentication.getCredentials());

        UserAuthInfo info = authMapper.selectUserAuthInfo(userId);
        if (info == null) {
            loginHistoryService.recordFail(userId, "FAIL_OTHER", "user not found");
            throw new BadCredentialsException("아이디 또는 비밀번호가 일치하지 않습니다.");
        }
        if ("Y".equals(info.getAcntLockYn())) {
            loginHistoryService.recordFail(userId, "FAIL_LOCKED", "account locked");
            throw new LockedException("계정이 잠겼습니다. 관리자에게 문의하세요.");
        }
        if (!Sha256.matches(password, info.getPswdHash())) {
            authMapper.incrementPasswordFailCount(userId);
            loginHistoryService.recordFail(userId, "FAIL_PWD", "wrong password");
            throw new BadCredentialsException("아이디 또는 비밀번호가 일치하지 않습니다.");
        }

        authMapper.resetPasswordFailCount(userId);
        authMapper.updateLastLogn(userId);

        Set<String> inq  = new HashSet<>(authMapper.selectPrgrmPathsInq(userId));
        Set<String> inpt = new HashSet<>(authMapper.selectPrgrmPathsInpt(userId));
        List<String> roleIds = authMapper.selectRoleIds(userId);

        IcasUser user = IcasUser.builder()
                .userId(info.getUserId())
                .userNm(info.getUserNm())
                .pswdHash(info.getPswdHash())
                .ognzId(info.getOgnzId())
                .ognzSeCd(info.getOgnzSeCd())
                .oprtrId(info.getOprtrId())
                .vrfcnInstId(info.getVrfcnInstId())
                .master("Y".equals(info.getMasterYn()))
                .roleIds(roleIds)
                .prgrmPathsInq(inq)
                .prgrmPathsInpt(inpt)
                .build();

        loginHistoryService.recordSuccess(userId);
        log.info("Login success userId={} ognz={}/{} master={}", userId, info.getOgnzSeCd(), info.getOgnzId(), info.getMasterYn());

        return new UsernamePasswordAuthenticationToken(user, password, user.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
