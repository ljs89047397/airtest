package kr.go.molit.icas.er.cef.validate;

import kr.go.molit.icas.common.dto.ApiResponse;
import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.er.cef.validate.domain.DoubleClaimingResult;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * CEF 이중청구 검증 REST 엔드포인트 (SFR-021).
 *
 * <p>등록 전 사용자가 배치 ID 충돌을 미리 확인할 수 있는 정보 제공 API.
 * Service 단의 등록 흐름은 {@link CefDoubleClaimingValidator#assertNotBlocked}
 * 로 강제 차단됨.
 *
 * <pre>
 * GET /api/er/cef/validate-double-claim
 *   ?batchIdNo=...
 *   &amp;currentOprtrId=...      (AIRLINE 은 본인 oprtrId 자동 강제)
 *   &amp;excludeCefId=...        (수정 시 자기 자신 제외용, 선택)
 *   &amp;excludeClaimNo=...      (선택)
 * </pre>
 */
@RestController
@RequestMapping("/api/er/cef/validate-double-claim")
@RequiredArgsConstructor
public class CefDoubleClaimingController {

    private final CefDoubleClaimingValidator validator;

    @GetMapping
    public ApiResponse<DoubleClaimingResult> validate(
            @RequestParam String batchIdNo,
            @RequestParam(required = false) String currentOprtrId,
            @RequestParam(required = false) String excludeCefId,
            @RequestParam(required = false) String excludeClaimNo,
            @AuthenticationPrincipal IcasUser user) {

        String oprtrId = resolveOprtrId(user, currentOprtrId);
        return ApiResponse.ok(validator.validate(batchIdNo, oprtrId, excludeCefId, excludeClaimNo));
    }

    /**
     * 운영사 ID 결정 + 권한 검증.
     * <ul>
     *   <li>AIRLINE — currentOprtrId 가 본인 소속과 일치해야 함 (입력 누락 시 본인 자동 주입)</li>
     *   <li>MOLIT / KOTSA / VERIFIER / MASTER — currentOprtrId 필수 입력</li>
     * </ul>
     */
    private String resolveOprtrId(IcasUser user, String currentOprtrId) {
        if (user == null) throw BusinessException.forbidden("로그인이 필요합니다.");

        if (user.isAirline()) {
            String own = user.getOprtrId();
            if (currentOprtrId != null && !currentOprtrId.equals(own)) {
                throw BusinessException.forbidden("본인 항공사 데이터만 조회할 수 있습니다.");
            }
            return own;
        }
        if (currentOprtrId == null || currentOprtrId.isBlank()) {
            throw BusinessException.badRequest("currentOprtrId 는 필수입니다.");
        }
        return currentOprtrId;
    }
}
