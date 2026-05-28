package kr.go.molit.icas.er.rprt.validate;

import kr.go.molit.icas.common.dto.ApiResponse;
import kr.go.molit.icas.common.security.IcasUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 국가 쌍 ↔ 비행장 쌍 CO₂ 합계 일치 검증 REST 컨트롤러 (SFR-013).
 *
 * <p>Base URL: {@code /api/er/rprt/{erId}/validate-pair-sum}
 *
 * <pre>
 * GET /api/er/rprt/{erId}/validate-pair-sum  — 합계 일치 검증 (정보 제공용, 항상 HTTP 200)
 * </pre>
 *
 * <p>검증 통과 여부는 응답 DTO 의 {@code passed} 필드로 확인.
 * 통과/불통과 모두 HTTP 200 반환.
 */
@RestController
@RequestMapping("/api/er/rprt/{erId}/validate-pair-sum")
@RequiredArgsConstructor
public class ErPairSumValidationController {

    private final ErPairSumValidationService erPairSumValidationService;

    /**
     * 국가 쌍 ↔ 비행장 쌍 CO₂ 합계 일치 검증.
     *
     * <p>응답 예시 (통과):
     * <pre>
     * {
     *   "success": true,
     *   "code": "OK",
     *   "data": {
     *     "cntrySum":    1000000.0000,
     *     "aerdrmSum":   1000500.0000,
     *     "deviation":       500.0000,
     *     "deviationPct":      0.0500,
     *     "passed": false,
     *     "message": "검증 실패: 국가 쌍(1000000.0000) ↔ 비행장 쌍(1000500.0000) 편차율 0.0500% — ±0.1% 초과"
     *   }
     * }
     * </pre>
     *
     * @param erId ER ID (path)
     * @param user 로그인 사용자
     * @return 검증 결과 DTO (항상 HTTP 200)
     */
    @GetMapping
    public ApiResponse<ErPairSumValidationService.PairSumValidationResult> validate(
            @PathVariable String erId,
            @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(erPairSumValidationService.validatePairSum(erId, user));
    }
}
