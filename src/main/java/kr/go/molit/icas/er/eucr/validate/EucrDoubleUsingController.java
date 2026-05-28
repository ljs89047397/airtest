package kr.go.molit.icas.er.eucr.validate;

import kr.go.molit.icas.common.dto.ApiResponse;
import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.er.eucr.validate.domain.DoubleUsingResult;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * EUCR 일련번호 이중사용 검증 REST 엔드포인트 (SFR-031).
 *
 * <p>등록 전 사용자가 일련번호 충돌을 미리 확인할 수 있는 정보 제공 API.
 *
 * <pre>
 * POST /api/er/eucr/validate-double-using
 *   body: { "crdtNos": ["VCS-001", "VCS-002"], "excludeEucrId": "EUCR0001" }
 * </pre>
 */
@RestController
@RequestMapping("/api/er/eucr/validate-double-using")
@RequiredArgsConstructor
public class EucrDoubleUsingController {

    private final EucrDoubleUsingValidator validator;

    @PostMapping
    public ApiResponse<DoubleUsingResult> validate(@RequestBody ValidateRequest body,
                                                    @AuthenticationPrincipal IcasUser user) {
        if (user == null) throw BusinessException.forbidden("로그인이 필요합니다.");
        return ApiResponse.ok(validator.validate(body.getCrdtNos(), body.getExcludeEucrId()));
    }

    @Getter
    @Setter
    public static class ValidateRequest {
        private List<String> crdtNos;
        private String       excludeEucrId;
    }
}
