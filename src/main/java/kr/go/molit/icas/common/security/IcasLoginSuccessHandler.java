package kr.go.molit.icas.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import kr.go.molit.icas.common.dto.ApiResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.util.Map;

public class IcasLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws java.io.IOException {
        IcasUser user = (IcasUser) authentication.getPrincipal();
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");
        objectMapper.writeValue(response.getWriter(), ApiResponse.ok(Map.of(
                "userId",   user.getUserId(),
                "userNm",   user.getUserNm(),
                "ognzSeCd", user.getOgnzSeCd(),
                "oprtrId",  user.getOprtrId() == null ? "" : user.getOprtrId(),
                "roleIds",  user.getRoleIds()
        ), "로그인되었습니다"));
    }
}
