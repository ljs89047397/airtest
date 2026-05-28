package kr.go.molit.icas.er.rprt.fuelsmry;

import kr.go.molit.icas.common.dto.ApiResponse;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.er.rprt.fuelsmry.domain.ErFuelSmryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 연료 유형별 총사용량 요약 REST 컨트롤러.
 *
 * <p>Base URL: {@code /api/er/rprt/{erId}/fuel-smry}
 */
@RestController
@RequestMapping("/api/er/rprt/{erId}/fuel-smry")
@RequiredArgsConstructor
public class ErFuelSmryController {

    private final ErFuelSmryService erFuelSmryService;

    /**
     * 연료 유형별 총사용량 목록 조회.
     *
     * @param erId ER ID (path)
     * @param user 로그인 사용자
     * @return 연료 유형별 총사용량 목록
     */
    @GetMapping
    public ApiResponse<List<ErFuelSmryVO>> list(
            @PathVariable String erId,
            @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(erFuelSmryService.list(erId, user));
    }

    /**
     * 연료 유형별 총사용량 단건 조회.
     *
     * @param erId       ER ID (path)
     * @param fuelTypeCd 연료 유형 코드 (path)
     * @param user       로그인 사용자
     * @return 연료 유형별 총사용량 VO
     */
    @GetMapping("/{fuelTypeCd}")
    public ApiResponse<ErFuelSmryVO> getOne(
            @PathVariable String erId,
            @PathVariable String fuelTypeCd,
            @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(erFuelSmryService.getOne(erId, fuelTypeCd, user));
    }

    /**
     * 연료 유형별 총사용량 upsert.
     *
     * <p>(er_id, fuel_type_cd) 가 이미 존재하면 update, 없으면 insert.
     *
     * @param erId       ER ID (path)
     * @param fuelTypeCd 연료 유형 코드 (path)
     * @param vo         요청 바디
     * @param user       로그인 사용자
     * @return 저장 후 최신 VO
     */
    @PutMapping("/{fuelTypeCd}")
    public ApiResponse<ErFuelSmryVO> upsert(
            @PathVariable String erId,
            @PathVariable String fuelTypeCd,
            @RequestBody ErFuelSmryVO vo,
            @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(erFuelSmryService.upsert(erId, fuelTypeCd, vo, user));
    }

    /**
     * 연료 유형별 총사용량 소프트삭제.
     *
     * @param erId       ER ID (path)
     * @param fuelTypeCd 연료 유형 코드 (path)
     * @param user       로그인 사용자
     * @return 처리 결과
     */
    @DeleteMapping("/{fuelTypeCd}")
    public ApiResponse<Void> delete(
            @PathVariable String erId,
            @PathVariable String fuelTypeCd,
            @AuthenticationPrincipal IcasUser user) {
        erFuelSmryService.softDelete(erId, fuelTypeCd, user);
        return ApiResponse.ok(null);
    }
}
