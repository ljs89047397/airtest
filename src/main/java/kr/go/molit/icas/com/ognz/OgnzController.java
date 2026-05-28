package kr.go.molit.icas.com.ognz;

import kr.go.molit.icas.com.ognz.domain.OgnzVO;
import kr.go.molit.icas.common.dto.ApiResponse;
import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.IcasUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 기관 관리 API.
 * Base URL: /api/com/ognz
 *
 * 등록/수정/삭제는 MOLIT 또는 KOTSA 소속 사용자만 허용.
 * 조회는 인증된 사용자 모두 허용.
 */
@RestController
@RequestMapping("/api/com/ognz")
@RequiredArgsConstructor
public class OgnzController {

    private final OgnzService ognzService;

    /** GET /api/com/ognz — 유효한 기관 전체 목록 */
    @GetMapping
    public ApiResponse<List<OgnzVO>> listAll() {
        return ApiResponse.ok(ognzService.listAll());
    }

    /** GET /api/com/ognz/{ognzId} — 기관 단건 조회 */
    @GetMapping("/{ognzId}")
    public ApiResponse<OgnzVO> getOne(@PathVariable String ognzId) {
        return ApiResponse.ok(ognzService.getOgnz(ognzId));
    }

    /** POST /api/com/ognz — 기관 등록 (MOLIT/KOTSA 전용) */
    @PostMapping
    public ApiResponse<OgnzVO> create(@RequestBody OgnzVO vo,
                                      @AuthenticationPrincipal IcasUser user) {
        assertMolitOrKotsa(user);
        return ApiResponse.ok(ognzService.createOgnz(vo, user), "기관이 등록되었습니다.");
    }

    /** PUT /api/com/ognz/{ognzId} — 기관 수정 (MOLIT/KOTSA 전용) */
    @PutMapping("/{ognzId}")
    public ApiResponse<OgnzVO> update(@PathVariable String ognzId,
                                      @RequestBody OgnzVO vo,
                                      @AuthenticationPrincipal IcasUser user) {
        assertMolitOrKotsa(user);
        return ApiResponse.ok(ognzService.updateOgnz(ognzId, vo, user), "기관 정보가 수정되었습니다.");
    }

    /** DELETE /api/com/ognz/{ognzId} — 기관 소프트 삭제 (MOLIT/KOTSA 전용) */
    @DeleteMapping("/{ognzId}")
    public ApiResponse<Void> delete(@PathVariable String ognzId,
                                    @AuthenticationPrincipal IcasUser user) {
        assertMolitOrKotsa(user);
        ognzService.softDeleteOgnz(ognzId, user);
        return ApiResponse.ok(null, "기관이 삭제되었습니다.");
    }

    /* ── 권한 검증 헬퍼 ── */

    private void assertMolitOrKotsa(IcasUser user) {
        if (!user.isMolitOrKotsa()) {
            throw BusinessException.forbidden("국토부 또는 교통안전공단 소속 사용자만 접근할 수 있습니다.");
        }
    }
}
