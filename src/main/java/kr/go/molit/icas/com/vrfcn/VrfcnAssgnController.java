package kr.go.molit.icas.com.vrfcn;

import kr.go.molit.icas.common.dto.ApiResponse;
import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.IcasUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 검증기관-항공사 배정 REST Controller.
 *
 * <pre>
 * GET    /api/com/vrfcn/assgn?rprtYr={year}                      — 전체 배정 목록
 * GET    /api/com/vrfcn/assgn/inst/{vrfcnInstId}?rprtYr={year}   — 특정 검증기관 배정 항공사 목록
 * POST   /api/com/vrfcn/assgn                                     — 배정 등록 (MOLIT/KOTSA 만)
 * DELETE /api/com/vrfcn/assgn?vrfcnInstId=..&oprtrId=..&rprtYr=. — 배정 삭제 (MOLIT/KOTSA 만)
 * </pre>
 */
@RestController
@RequestMapping("/api/com/vrfcn/assgn")
@RequiredArgsConstructor
public class VrfcnAssgnController {

    private final VrfcnAssgnService vrfcnAssgnService;

    /**
     * 전체 배정 목록 조회.
     * <ul>
     *   <li>MOLIT/KOTSA: 전체 목록</li>
     *   <li>VERIFIER: 본인 기관의 배정만 (Controller 에서 vrfcnInstId 주입)</li>
     *   <li>AIRLINE: 조회 불가 (FORBIDDEN)</li>
     * </ul>
     *
     * @param rprtYr 보고연도 (생략 가능)
     */
    @GetMapping
    public ApiResponse<List<Map<String, Object>>> listAll(
            @RequestParam(required = false) String rprtYr,
            @AuthenticationPrincipal IcasUser user) {

        // 항공사는 배정 목록 조회 불가
        if (user.isAirline()) {
            throw BusinessException.forbidden("항공사 사용자는 배정 목록을 조회할 수 없습니다.");
        }

        // VERIFIER 는 본인 기관의 배정만 노출
        if (user.isVerifier()) {
            String myVrfcnInstId = user.getVrfcnInstId();
            if (myVrfcnInstId == null) {
                throw BusinessException.forbidden("검증기관 정보가 없는 사용자입니다.");
            }
            return ApiResponse.ok(
                    vrfcnAssgnService.selectAssgnList(rprtYr).stream()
                            .filter(m -> myVrfcnInstId.equals(m.get("vrfcnInstId"))
                                    || myVrfcnInstId.equals(m.get("vrfcn_inst_id")))
                            .toList()
            );
        }

        // MOLIT/KOTSA: 전체
        return ApiResponse.ok(vrfcnAssgnService.selectAssgnList(rprtYr));
    }

    /**
     * 특정 검증기관에 배정된 항공사 ID 목록 조회.
     * 인증된 사용자 모두 가능 (단, VERIFIER 는 본인 기관만).
     */
    @GetMapping("/inst/{vrfcnInstId}")
    public ApiResponse<List<String>> listByInst(
            @PathVariable String vrfcnInstId,
            @RequestParam(required = false) String rprtYr,
            @AuthenticationPrincipal IcasUser user) {

        // VERIFIER 는 본인 기관만 조회 허용
        if (user.isVerifier()) {
            String myVrfcnInstId = user.getVrfcnInstId();
            if (myVrfcnInstId == null || !myVrfcnInstId.equals(vrfcnInstId)) {
                throw BusinessException.forbidden("본인 소속 검증기관의 배정 정보만 조회할 수 있습니다.");
            }
        }

        // 항공사는 조회 불가
        if (user.isAirline()) {
            throw BusinessException.forbidden("항공사 사용자는 배정 정보를 조회할 수 없습니다.");
        }

        return ApiResponse.ok(vrfcnAssgnService.selectAssignedOprtrIds(vrfcnInstId, rprtYr));
    }

    /**
     * 검증기관-항공사 배정 등록.
     * MOLIT/KOTSA 만 허용.
     *
     * @param body {vrfcnInstId, oprtrId, rprtYr}
     */
    @PostMapping
    public ApiResponse<Void> create(
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal IcasUser user) {

        assertMolitOrKotsa(user);

        String vrfcnInstId = body.get("vrfcnInstId");
        String oprtrId     = body.get("oprtrId");
        String rprtYr      = body.get("rprtYr");

        vrfcnAssgnService.createAssgn(vrfcnInstId, oprtrId, rprtYr, user);
        return ApiResponse.ok(null, "배정이 등록되었습니다.");
    }

    /**
     * 검증기관-항공사 배정 소프트삭제.
     * MOLIT/KOTSA 만 허용.
     */
    @DeleteMapping
    public ApiResponse<Void> delete(
            @RequestParam String vrfcnInstId,
            @RequestParam String oprtrId,
            @RequestParam String rprtYr,
            @AuthenticationPrincipal IcasUser user) {

        assertMolitOrKotsa(user);
        vrfcnAssgnService.softDeleteAssgn(vrfcnInstId, oprtrId, rprtYr, user);
        return ApiResponse.ok(null, "배정이 삭제되었습니다.");
    }

    // ──────────────────────────────────────────────
    // private helpers
    // ──────────────────────────────────────────────

    private void assertMolitOrKotsa(IcasUser user) {
        if (!user.isMolitOrKotsa()) {
            throw BusinessException.forbidden("국토부 또는 한국교통안전공단 사용자만 접근할 수 있습니다.");
        }
    }
}
