package kr.go.molit.icas.com.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@RequiredArgsConstructor
public class LoginHistoryService {

    private final AuthMapper authMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordSuccess(String userId) {
        String[] ctx = ipAndAgent();
        authMapper.insertLoginHistory(userId, "SUCCESS", null, ctx[0], ctx[1]);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFail(String userId, String rsltCd, String reason) {
        String[] ctx = ipAndAgent();
        authMapper.insertLoginHistory(userId, rsltCd, reason, ctx[0], ctx[1]);
    }

    private String[] ipAndAgent() {
        try {
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attr == null) return new String[]{null, null};
            String ip = attr.getRequest().getHeader("X-Forwarded-For");
            if (ip == null || ip.isBlank()) ip = attr.getRequest().getRemoteAddr();
            String ua = attr.getRequest().getHeader("User-Agent");
            return new String[]{ip, ua};
        } catch (Exception ignore) {
            return new String[]{null, null};
        }
    }
}
