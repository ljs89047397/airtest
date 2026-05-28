package kr.go.molit.icas.er.rprt.afbr;

import kr.go.molit.icas.common.dto.ApiResponse;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.er.rprt.afbr.domain.ErAfbrVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 항공기 유형별 평균 연료연소율 REST 컨트롤러.
 *
 * <p>Base URL: {@code /api/er/rprt/{erId}/afbr}
 */
@RestController
@RequestMapping("/api/er/rprt/{erId}/afbr")
@RequiredArgsConstructor
public class ErAfbrController {

    private final ErAfbrService erAfbrService;

    /**
     * 평균 연료연소율 목록 조회.
     *
     * @param erId ER ID (path)
     * @param user 로그인 사용자
     * @return 평균 연료연소율 목록
     */
    @GetMapping
    public ApiResponse<List<ErAfbrVO>> list(
            @PathVariable String erId,
            @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(erAfbrService.list(erId, user));
    }

    /**
     * 평균 연료연소율 단건 조회.
     *
     * @param erId       ER ID (path)
     * @param acftTypeCd 항공기 유형 코드 (path)
     * @param user       로그인 사용자
     * @return 평균 연료연소율 VO
     */
    @GetMapping("/{acftTypeCd}")
    public ApiResponse<ErAfbrVO> getOne(
            @PathVariable String erId,
            @PathVariable String acftTypeCd,
            @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(erAfbrService.getOne(erId, acftTypeCd, user));
    }

    /**
     * 평균 연료연소율 upsert.
     *
     * <p>(er_id, acft_type_cd) 가 이미 존재하면 update, 없으면 insert.
     *
     * @param erId       ER ID (path)
     * @param acftTypeCd 항공기 유형 코드 (path)
     * @param vo         요청 바디 (afbrVal 필수)
     * @param user       로그인 사용자
     * @return 저장 후 최신 VO
     */
    @PutMapping("/{acftTypeCd}")
    public ApiResponse<ErAfbrVO> upsert(
            @PathVariable String erId,
            @PathVariable String acftTypeCd,
            @RequestBody ErAfbrVO vo,
            @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(erAfbrService.upsert(erId, acftTypeCd, vo, user));
    }

    /**
     * 평균 연료연소율 소프트삭제.
     *
     * @param erId       ER ID (path)
     * @param acftTypeCd 항공기 유형 코드 (path)
     * @param user       로그인 사용자
     * @return 처리 결과
     */
    @DeleteMapping("/{acftTypeCd}")
    public ApiResponse<Void> delete(
            @PathVariable String erId,
            @PathVariable String acftTypeCd,
            @AuthenticationPrincipal IcasUser user) {
        erAfbrService.softDelete(erId, acftTypeCd, user);
        return ApiResponse.ok(null);
    }
}
