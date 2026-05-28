package kr.go.molit.icas.er.rprt.vrfr;

import kr.go.molit.icas.common.dto.ApiResponse;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.er.rprt.vrfr.domain.ErVrfrInfoVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ER 참여 검증기관 정보 REST 컨트롤러.
 *
 * <p>Base URL: {@code /api/er/rprt/{erId}/vrfr-info}
 *
 * <pre>
 * GET    /api/er/rprt/{erId}/vrfr-info           — 목록 조회
 * GET    /api/er/rprt/{erId}/vrfr-info/{vrfrSn}  — 단건 조회
 * POST   /api/er/rprt/{erId}/vrfr-info           — 검증기관 추가 (DRAFT + AIRLINE)
 * PUT    /api/er/rprt/{erId}/vrfr-info/{vrfrSn}  — 검증기관 수정 (DRAFT + AIRLINE)
 * DELETE /api/er/rprt/{erId}/vrfr-info/{vrfrSn}  — 검증기관 소프트삭제 (DRAFT + AIRLINE)
 * </pre>
 */
@RestController
@RequestMapping("/api/er/rprt/{erId}/vrfr-info")
@RequiredArgsConstructor
public class ErVrfrInfoController {

    private final ErVrfrInfoService erVrfrInfoService;

    /**
     * 참여 검증기관 목록 조회.
     *
     * @param erId ER ID (path)
     * @param user 로그인 사용자
     * @return 참여 검증기관 목록
     */
    @GetMapping
    public ApiResponse<List<ErVrfrInfoVO>> list(
            @PathVariable String erId,
            @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(erVrfrInfoService.listByErId(erId, user));
    }

    /**
     * 참여 검증기관 단건 조회.
     *
     * @param erId   ER ID (path)
     * @param vrfrSn 검증기관 일련번호 (path)
     * @param user   로그인 사용자
     * @return 검증기관 정보 VO
     */
    @GetMapping("/{vrfrSn}")
    public ApiResponse<ErVrfrInfoVO> getOne(
            @PathVariable String erId,
            @PathVariable int vrfrSn,
            @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(erVrfrInfoService.getOne(erId, vrfrSn, user));
    }

    /**
     * 참여 검증기관 추가 (DRAFT + AIRLINE).
     *
     * @param erId ER ID (path)
     * @param vo   요청 바디 (vrfcnInstId 필수)
     * @param user 로그인 사용자
     * @return 생성된 검증기관 정보 VO
     */
    @PostMapping
    public ApiResponse<ErVrfrInfoVO> add(
            @PathVariable String erId,
            @RequestBody ErVrfrInfoVO vo,
            @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(erVrfrInfoService.addVrfr(erId, vo, user), "참여 검증기관이 추가되었습니다.");
    }

    /**
     * 참여 검증기관 수정 (DRAFT + AIRLINE).
     *
     * @param erId   ER ID (path)
     * @param vrfrSn 검증기관 일련번호 (path)
     * @param vo     요청 바디
     * @param user   로그인 사용자
     * @return 처리 결과
     */
    @PutMapping("/{vrfrSn}")
    public ApiResponse<Void> update(
            @PathVariable String erId,
            @PathVariable int vrfrSn,
            @RequestBody ErVrfrInfoVO vo,
            @AuthenticationPrincipal IcasUser user) {
        erVrfrInfoService.updateVrfr(erId, vrfrSn, vo, user);
        return ApiResponse.ok(null, "참여 검증기관 정보가 수정되었습니다.");
    }

    /**
     * 참여 검증기관 소프트삭제 (DRAFT + AIRLINE).
     *
     * @param erId   ER ID (path)
     * @param vrfrSn 검증기관 일련번호 (path)
     * @param user   로그인 사용자
     * @return 처리 결과
     */
    @DeleteMapping("/{vrfrSn}")
    public ApiResponse<Void> delete(
            @PathVariable String erId,
            @PathVariable int vrfrSn,
            @AuthenticationPrincipal IcasUser user) {
        erVrfrInfoService.softDeleteVrfr(erId, vrfrSn, user);
        return ApiResponse.ok(null, "참여 검증기관 정보가 삭제되었습니다.");
    }
}
