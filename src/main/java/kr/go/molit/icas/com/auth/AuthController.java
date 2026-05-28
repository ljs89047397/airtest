package kr.go.molit.icas.com.auth;

import kr.go.molit.icas.common.dto.ApiResponse;
import kr.go.molit.icas.common.security.IcasUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 인증 정보 API. (로그인/로그아웃은 Spring Security 가 처리)
 */
@RestController
@RequestMapping("/api/com/auth")
public class AuthController {

    @GetMapping("/current")
    public ApiResponse<Map<String, Object>> current(@AuthenticationPrincipal IcasUser user) {
        Map<String, Object> body = new HashMap<>();
        if (user == null) {
            body.put("authenticated", false);
            return ApiResponse.ok(body);
        }
        body.put("authenticated", true);
        body.put("userId",        user.getUserId());
        body.put("userNm",        user.getUserNm());
        body.put("ognzId",        user.getOgnzId());
        body.put("ognzSeCd",      user.getOgnzSeCd());
        body.put("oprtrId",       user.getOprtrId());
        body.put("vrfcnInstId",   user.getVrfcnInstId());
        body.put("master",        user.isMaster());
        body.put("roleIds",       user.getRoleIds());
        body.put("prgrmPathsInq", user.getPrgrmPathsInq());
        body.put("prgrmPathsInpt",user.getPrgrmPathsInpt());
        return ApiResponse.ok(body);
    }
}
