package kr.go.molit.icas.er.rprt.cntry;

import kr.go.molit.icas.common.dto.ApiResponse;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.er.rprt.cntry.domain.ErCntryPairCo2VO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 국가 쌍 배출량 REST 컨트롤러.
 *
 * <p>Base URL: {@code /api/er/rprt/{erId}/cntry-pair}
 *
 * <pre>
 * GET    /api/er/rprt/{erId}/cntry-pair            — 목록 조회
 * GET    /api/er/rprt/{erId}/cntry-pair/{pairSn}   — 단건 조회
 * POST   /api/er/rprt/{erId}/cntry-pair            — 추가 (DRAFT + AIRLINE)
 * PUT    /api/er/rprt/{erId}/cntry-pair/{pairSn}   — 수정 (DRAFT + AIRLINE)
 * DELETE /api/er/rprt/{erId}/cntry-pair/{pairSn}   — 소프트삭제 (DRAFT + AIRLINE)
 * </pre>
 */
@RestController
@RequestMapping("/api/er/rprt/{erId}/cntry-pair")
@RequiredArgsConstructor
public class ErCntryPairCo2Controller {

    private final ErCntryPairCo2Service erCntryPairCo2Service;

    /**
     * 국가 쌍 배출량 목록 조회.
     *
     * @param erId ER ID (path)
     * @param user 로그인 사용자
     * @return 국가 쌍 배출량 목록
     */
    @GetMapping
    public ApiResponse<List<ErCntryPairCo2VO>> list(
            @PathVariable String erId,
            @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(erCntryPairCo2Service.list(erId, user));
    }

    /**
     * 국가 쌍 배출량 단건 조회.
     *
     * @param erId   ER ID (path)
     * @param pairSn 국가 쌍 일련번호 (path)
     * @param user   로그인 사용자
     * @return 국가 쌍 배출량 VO
     */
    @GetMapping("/{pairSn}")
    public ApiResponse<ErCntryPairCo2VO> getOne(
            @PathVariable String erId,
            @PathVariable int pairSn,
            @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(erCntryPairCo2Service.getOne(erId, pairSn, user));
    }

    /**
     * 국가 쌍 배출량 추가 (DRAFT + AIRLINE).
     *
     * @param erId ER ID (path)
     * @param vo   요청 바디 (fuelTypeCd, convFctr 필수)
     * @param user 로그인 사용자
     * @return 생성된 국가 쌍 배출량 VO
     */
    @PostMapping
    public ApiResponse<ErCntryPairCo2VO> add(
            @PathVariable String erId,
            @RequestBody ErCntryPairCo2VO vo,
            @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(erCntryPairCo2Service.add(erId, vo, user), "국가 쌍 배출량이 추가되었습니다.");
    }

    /**
     * 국가 쌍 배출량 수정 (DRAFT + AIRLINE).
     *
     * @param erId   ER ID (path)
     * @param pairSn 국가 쌍 일련번호 (path)
     * @param vo     요청 바디
     * @param user   로그인 사용자
     * @return 처리 결과
     */
    @PutMapping("/{pairSn}")
    public ApiResponse<Void> update(
            @PathVariable String erId,
            @PathVariable int pairSn,
            @RequestBody ErCntryPairCo2VO vo,
            @AuthenticationPrincipal IcasUser user) {
        erCntryPairCo2Service.update(erId, pairSn, vo, user);
        return ApiResponse.ok(null, "국가 쌍 배출량이 수정되었습니다.");
    }

    /**
     * 국가 쌍 배출량 소프트삭제 (DRAFT + AIRLINE).
     *
     * @param erId   ER ID (path)
     * @param pairSn 국가 쌍 일련번호 (path)
     * @param user   로그인 사용자
     * @return 처리 결과
     */
    @DeleteMapping("/{pairSn}")
    public ApiResponse<Void> delete(
            @PathVariable String erId,
            @PathVariable int pairSn,
            @AuthenticationPrincipal IcasUser user) {
        erCntryPairCo2Service.softDelete(erId, pairSn, user);
        return ApiResponse.ok(null, "국가 쌍 배출량이 삭제되었습니다.");
    }
}
