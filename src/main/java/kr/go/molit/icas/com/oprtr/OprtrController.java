package kr.go.molit.icas.com.oprtr;

import kr.go.molit.icas.com.oprtr.domain.OprtrVO;
import kr.go.molit.icas.common.dto.ApiResponse;
import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.IcasUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Year;
import java.util.List;

@RestController
@RequestMapping("/api/com/oprtr")
@RequiredArgsConstructor
public class OprtrController {

    private final OprtrService oprtrService;

    /**
     * 목록 조회.
     * MOLIT/KOTSA: 전체, AIRLINE: 본인만, VERIFIER: 배정된 운영사만.
     */
    @GetMapping
    public ApiResponse<List<OprtrVO>> list(
            @RequestParam(required = false) String rprtYr,
            @AuthenticationPrincipal IcasUser user) {

        if (user.isMolitOrKotsa() || user.isMaster()) {
            return ApiResponse.ok(oprtrService.selectAll());
        }

        if (user.isAirline()) {
            // 본인 oprtrId 만
            return ApiResponse.ok(oprtrService.selectAccessibleForUser(
                    "AIRLINE", user.getOprtrId(), null, null));
        }

        if (user.isVerifier()) {
            String yr = (rprtYr != null && !rprtYr.isBlank())
                    ? rprtYr
                    : String.valueOf(Year.now().getValue());
            return ApiResponse.ok(oprtrService.selectAccessibleForUser(
                    "VERIFIER", null, user.getVrfcnInstId(), yr));
        }

        throw BusinessException.forbidden("접근 권한이 없습니다.");
    }

    /** 단건 조회 */
    @GetMapping("/{oprtrId}")
    public ApiResponse<OprtrVO> get(
            @PathVariable String oprtrId,
            @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(oprtrService.selectByOprtrId(oprtrId, user));
    }

    /** 등록 (MOLIT/KOTSA 전용) */
    @PostMapping
    public ApiResponse<OprtrVO> create(
            @RequestBody OprtrVO vo,
            @AuthenticationPrincipal IcasUser user) {
        if (!user.isMaster() && !user.isMolitOrKotsa()) {
            throw BusinessException.forbidden("운영사 등록은 국토부·한국교통안전공단만 가능합니다.");
        }
        return ApiResponse.ok(oprtrService.insert(vo, user), "등록되었습니다");
    }

    /** 수정 (MOLIT/KOTSA 또는 본인 항공사) */
    @PutMapping("/{oprtrId}")
    public ApiResponse<Void> update(
            @PathVariable String oprtrId,
            @RequestBody OprtrVO vo,
            @AuthenticationPrincipal IcasUser user) {
        oprtrService.update(oprtrId, vo, user);
        return ApiResponse.ok(null, "수정되었습니다");
    }

    /** 소프트삭제 (MOLIT/KOTSA 전용) */
    @DeleteMapping("/{oprtrId}")
    public ApiResponse<Void> delete(
            @PathVariable String oprtrId,
            @AuthenticationPrincipal IcasUser user) {
        if (!user.isMaster() && !user.isMolitOrKotsa()) {
            throw BusinessException.forbidden("운영사 삭제는 국토부·한국교통안전공단만 가능합니다.");
        }
        oprtrService.softDelete(oprtrId, user);
        return ApiResponse.ok(null, "삭제되었습니다");
    }
}
