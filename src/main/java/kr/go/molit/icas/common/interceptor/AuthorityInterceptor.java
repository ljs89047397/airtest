package kr.go.molit.icas.common.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.IcasUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

/**
 * URL + HTTP 메서드를 기준으로 사용자의 프로그램 권한 (inq/inpt) 검사.
 *
 * <ul>
 *   <li>GET / HEAD → 조회 권한 (PRGRM_PATHS_INQ 에 매칭 URL prefix 포함)</li>
 *   <li>POST / PUT / PATCH / DELETE → 입력 권한</li>
 * </ul>
 *
 * 마스터 계정은 모든 검사 우회.
 */
@Slf4j
public class AuthorityInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof IcasUser user)) {
            throw BusinessException.forbidden("로그인이 필요합니다.");
        }
        if (user.isMaster()) return true;

        String uri    = request.getRequestURI();
        String method = request.getMethod();

        Set<String> permitted = "GET".equals(method) || "HEAD".equals(method)
                ? user.getPrgrmPathsInq() : user.getPrgrmPathsInpt();

        if (permitted == null) {
            log.warn("Authority denied (no permitted set): uri={} method={} userId={}", uri, method, user.getUserId());
            throw BusinessException.forbidden("해당 기능에 대한 권한이 없습니다.");
        }
        boolean ok = permitted.stream().anyMatch(uri::startsWith);
        if (!ok) {
            log.warn("Authority denied: uri={} method={} userId={} permitted={}", uri, method, user.getUserId(), permitted);
            throw BusinessException.forbidden("해당 기능에 대한 권한이 없습니다.");
        }
        return true;
    }
}
