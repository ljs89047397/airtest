package kr.go.molit.icas.com.vrfcn;

import kr.go.molit.icas.com.vrfcn.domain.VrfcnInstVO;
import kr.go.molit.icas.common.dto.ApiResponse;
import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.IcasUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 검증기관 REST Controller.
 *
 * <pre>
 * GET    /api/com/vrfcn/inst          — 전체 목록 (MOLIT/KOTSA/VERIFIER 가능)
 * GET    /api/com/vrfcn/inst/{id}     — 단건 조회 (VERIFIER 는 본인 기관만)
 * POST   /api/com/vrfcn/inst          — 등록 (MOLIT/KOTSA 만)
 * PUT    /api/com/vrfcn/inst/{id}     — 수정 (MOLIT/KOTSA 만)
 * DELETE /api/com/vrfcn/inst/{id}     — 소프트삭제 (MOLIT/KOTSA 만)
 * </pre>
 */
@RestController
@RequestMapping("/api/com/vrfcn/inst")
@RequiredArgsConstructor
public class VrfcnInstController {

    private final VrfcnInstService vrfcnInstService;

    /**
     * 유효한 검증기관 전체 목록 조회.
     * 권한 제한 없음 (인증된 사용자 모두 가능).
     */
    @GetMapping
    public ApiResponse<List<VrfcnInstVO>> listAll(
            @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(vrfcnInstService.selectAll());
    }

    /**
     * 검증기관 단건 조회.
     * VERIFIER 역할은 본인의 vrfcnInstId 에 해당하는 기관만 조회 가능.
     */
    @GetMapping("/{vrfcnInstId}")
    public ApiResponse<VrfcnInstVO> getOne(
            @PathVariable String vrfcnInstId,
            @AuthenticationPrincipal IcasUser user) {

        // VERIFIER 는 본인 기관만 조회 허용
        if (user.isVerifier()) {
            String myVrfcnInstId = user.getVrfcnInstId();
            if (myVrfcnInstId == null || !myVrfcnInstId.equals(vrfcnInstId)) {
                throw BusinessException.forbidden("본인 소속 검증기관 정보만 조회할 수 있습니다.");
            }
        }

        return ApiResponse.ok(vrfcnInstService.selectByVrfcnInstId(vrfcnInstId));
    }

    /**
     * 검증기관 등록.
     * MOLIT/KOTSA 만 허용.
     */
    @PostMapping
    public ApiResponse<VrfcnInstVO> create(
            @RequestBody VrfcnInstVO vo,
            @AuthenticationPrincipal IcasUser user) {

        assertMolitOrKotsa(user);
        VrfcnInstVO created = vrfcnInstService.createVrfcnInst(vo, user);
        return ApiResponse.ok(created, "검증기관이 등록되었습니다.");
    }

    /**
     * 검증기관 수정.
     * MOLIT/KOTSA 만 허용.
     */
    @PutMapping("/{vrfcnInstId}")
    public ApiResponse<Void> update(
            @PathVariable String vrfcnInstId,
            @RequestBody VrfcnInstVO vo,
            @AuthenticationPrincipal IcasUser user) {

        assertMolitOrKotsa(user);
        vrfcnInstService.updateVrfcnInst(vrfcnInstId, vo, user);
        return ApiResponse.ok(null, "검증기관 정보가 수정되었습니다.");
    }

    /**
     * 검증기관 소프트삭제.
     * MOLIT/KOTSA 만 허용.
     */
    @DeleteMapping("/{vrfcnInstId}")
    public ApiResponse<Void> delete(
            @PathVariable String vrfcnInstId,
            @AuthenticationPrincipal IcasUser user) {

        assertMolitOrKotsa(user);
        vrfcnInstService.softDeleteVrfcnInst(vrfcnInstId, user);
        return ApiResponse.ok(null, "검증기관이 삭제되었습니다.");
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
