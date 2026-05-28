package kr.go.molit.icas.com.prgrm;

import kr.go.molit.icas.com.prgrm.domain.AuthrtPrgrmMpngVO;
import kr.go.molit.icas.common.dto.ApiResponse;
import kr.go.molit.icas.common.security.IcasUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 권한-프로그램 매핑 관리 API.
 * Base URL: /api/com/authrt-prgrm
 */
@RestController
@RequestMapping("/api/com/authrt-prgrm")
@RequiredArgsConstructor
public class AuthrtPrgrmMpngController {

    private final AuthrtPrgrmMpngService authrtPrgrmMpngService;

    /**
     * GET /api/com/authrt-prgrm/authrt/{authrtId}
     * 특정 권한의 프로그램 매핑 목록 조회.
     */
    @GetMapping("/authrt/{authrtId}")
    public ApiResponse<List<AuthrtPrgrmMpngVO>> listByAuthrt(
            @PathVariable String authrtId) {
        return ApiResponse.ok(authrtPrgrmMpngService.selectByAuthrt(authrtId));
    }

    /**
     * GET /api/com/authrt-prgrm/prgrm/{prgrmId}
     * 특정 프로그램을 가진 권한 목록 조회.
     */
    @GetMapping("/prgrm/{prgrmId}")
    public ApiResponse<List<AuthrtPrgrmMpngVO>> listByPrgrm(
            @PathVariable String prgrmId) {
        return ApiResponse.ok(authrtPrgrmMpngService.selectByPrgrm(prgrmId));
    }

    /**
     * POST /api/com/authrt-prgrm
     * 권한-프로그램 매핑 upsert (MOLIT/KOTSA 전용).
     * body: { authrtId, prgrmId, inqAuthrtYn, inptAuthrtYn }
     */
    @PostMapping
    public ApiResponse<Void> setAuthority(
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal IcasUser user) {
        authrtPrgrmMpngService.setAuthority(
                body.get("authrtId"),
                body.get("prgrmId"),
                body.get("inqAuthrtYn"),
                body.get("inptAuthrtYn"),
                user);
        return ApiResponse.ok(null, "권한 매핑이 저장되었습니다.");
    }

    /**
     * DELETE /api/com/authrt-prgrm?authrtId=..&prgrmId=..
     * 권한-프로그램 매핑 소프트 삭제 (MOLIT/KOTSA 전용).
     */
    @DeleteMapping
    public ApiResponse<Void> removeMapping(
            @RequestParam String authrtId,
            @RequestParam String prgrmId,
            @AuthenticationPrincipal IcasUser user) {
        authrtPrgrmMpngService.removeMapping(authrtId, prgrmId, user);
        return ApiResponse.ok(null, "삭제되었습니다.");
    }
}
