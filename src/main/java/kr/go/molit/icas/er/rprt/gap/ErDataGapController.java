package kr.go.molit.icas.er.rprt.gap;

import kr.go.molit.icas.common.dto.ApiResponse;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.er.rprt.gap.domain.ErDataGapVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 데이터 갭 REST 컨트롤러.
 *
 * <p>Base URL: {@code /api/er/rprt/{erId}/data-gap}
 *
 * <pre>
 * GET    /api/er/rprt/{erId}/data-gap          — 목록 조회
 * GET    /api/er/rprt/{erId}/data-gap/{gapSn}  — 단건 조회
 * POST   /api/er/rprt/{erId}/data-gap          — 추가 (DRAFT + AIRLINE, thrshld5pctXcYn 자동 판정)
 * PUT    /api/er/rprt/{erId}/data-gap/{gapSn}  — 수정 (DRAFT + AIRLINE, thrshld5pctXcYn 자동 재판정)
 * DELETE /api/er/rprt/{erId}/data-gap/{gapSn}  — 소프트삭제 (DRAFT + AIRLINE)
 * </pre>
 */
@RestController
@RequestMapping("/api/er/rprt/{erId}/data-gap")
@RequiredArgsConstructor
public class ErDataGapController {

    private final ErDataGapService erDataGapService;

    /**
     * 데이터 갭 목록 조회.
     *
     * @param erId ER ID (path)
     * @param user 로그인 사용자
     * @return 데이터 갭 목록
     */
    @GetMapping
    public ApiResponse<List<ErDataGapVO>> list(
            @PathVariable String erId,
            @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(erDataGapService.list(erId, user));
    }

    /**
     * 데이터 갭 단건 조회.
     *
     * @param erId  ER ID (path)
     * @param gapSn 데이터 갭 일련번호 (path)
     * @param user  로그인 사용자
     * @return 데이터 갭 VO
     */
    @GetMapping("/{gapSn}")
    public ApiResponse<ErDataGapVO> getOne(
            @PathVariable String erId,
            @PathVariable int gapSn,
            @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(erDataGapService.getOne(erId, gapSn, user));
    }

    /**
     * 데이터 갭 추가 (DRAFT + AIRLINE).
     * thrshld5pctXcYn 요청 바디의 값은 무시되고 자동 계산됩니다.
     *
     * @param erId ER ID (path)
     * @param vo   요청 바디
     * @param user 로그인 사용자
     * @return 생성된 데이터 갭 VO (thrshld5pctXcYn 자동 설정)
     */
    @PostMapping
    public ApiResponse<ErDataGapVO> add(
            @PathVariable String erId,
            @RequestBody ErDataGapVO vo,
            @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(erDataGapService.add(erId, vo, user), "데이터 갭이 추가되었습니다.");
    }

    /**
     * 데이터 갭 수정 (DRAFT + AIRLINE).
     * thrshld5pctXcYn 요청 바디의 값은 무시되고 자동 재계산됩니다.
     *
     * @param erId  ER ID (path)
     * @param gapSn 데이터 갭 일련번호 (path)
     * @param vo    요청 바디
     * @param user  로그인 사용자
     * @return 처리 결과
     */
    @PutMapping("/{gapSn}")
    public ApiResponse<Void> update(
            @PathVariable String erId,
            @PathVariable int gapSn,
            @RequestBody ErDataGapVO vo,
            @AuthenticationPrincipal IcasUser user) {
        erDataGapService.update(erId, gapSn, vo, user);
        return ApiResponse.ok(null, "데이터 갭이 수정되었습니다.");
    }

    /**
     * 데이터 갭 소프트삭제 (DRAFT + AIRLINE).
     *
     * @param erId  ER ID (path)
     * @param gapSn 데이터 갭 일련번호 (path)
     * @param user  로그인 사용자
     * @return 처리 결과
     */
    @DeleteMapping("/{gapSn}")
    public ApiResponse<Void> delete(
            @PathVariable String erId,
            @PathVariable int gapSn,
            @AuthenticationPrincipal IcasUser user) {
        erDataGapService.softDelete(erId, gapSn, user);
        return ApiResponse.ok(null, "데이터 갭이 삭제되었습니다.");
    }
}
