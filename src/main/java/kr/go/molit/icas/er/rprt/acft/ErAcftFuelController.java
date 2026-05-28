package kr.go.molit.icas.er.rprt.acft;

import kr.go.molit.icas.common.dto.ApiResponse;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.er.rprt.acft.domain.ErAcftFuelVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 항공기·연료 REST 컨트롤러.
 *
 * <p>Base URL: {@code /api/er/rprt/{erId}/acft-fuel}
 */
@RestController
@RequestMapping("/api/er/rprt/{erId}/acft-fuel")
@RequiredArgsConstructor
public class ErAcftFuelController {

    private final ErAcftFuelService erAcftFuelService;

    /**
     * 항공기·연료 목록 조회.
     *
     * @param erId ER ID (path)
     * @param user 로그인 사용자
     * @return 항공기·연료 목록
     */
    @GetMapping
    public ApiResponse<List<ErAcftFuelVO>> list(
            @PathVariable String erId,
            @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(erAcftFuelService.list(erId, user));
    }

    /**
     * 항공기·연료 단건 조회.
     *
     * @param erId   ER ID (path)
     * @param acftSn 항공기 일련번호 (path)
     * @param user   로그인 사용자
     * @return 항공기·연료 VO
     */
    @GetMapping("/{acftSn}")
    public ApiResponse<ErAcftFuelVO> getOne(
            @PathVariable String erId,
            @PathVariable int acftSn,
            @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(erAcftFuelService.getOne(erId, acftSn, user));
    }

    /**
     * 항공기·연료 추가.
     *
     * @param erId ER ID (path)
     * @param vo   요청 바디 (regisMark, fuelTypeCd 필수)
     * @param user 로그인 사용자
     * @return 생성된 항공기·연료 VO
     */
    @PostMapping
    public ApiResponse<ErAcftFuelVO> add(
            @PathVariable String erId,
            @RequestBody ErAcftFuelVO vo,
            @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(erAcftFuelService.add(erId, vo, user));
    }

    /**
     * 항공기·연료 수정.
     *
     * @param erId   ER ID (path)
     * @param acftSn 항공기 일련번호 (path)
     * @param vo     요청 바디
     * @param user   로그인 사용자
     * @return 처리 결과
     */
    @PutMapping("/{acftSn}")
    public ApiResponse<Void> update(
            @PathVariable String erId,
            @PathVariable int acftSn,
            @RequestBody ErAcftFuelVO vo,
            @AuthenticationPrincipal IcasUser user) {
        erAcftFuelService.update(erId, acftSn, vo, user);
        return ApiResponse.ok(null);
    }

    /**
     * 항공기·연료 소프트삭제.
     *
     * @param erId   ER ID (path)
     * @param acftSn 항공기 일련번호 (path)
     * @param user   로그인 사용자
     * @return 처리 결과
     */
    @DeleteMapping("/{acftSn}")
    public ApiResponse<Void> delete(
            @PathVariable String erId,
            @PathVariable int acftSn,
            @AuthenticationPrincipal IcasUser user) {
        erAcftFuelService.softDelete(erId, acftSn, user);
        return ApiResponse.ok(null);
    }
}
