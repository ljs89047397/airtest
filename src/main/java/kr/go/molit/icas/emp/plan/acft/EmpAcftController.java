package kr.go.molit.icas.emp.plan.acft;

import kr.go.molit.icas.common.dto.ApiResponse;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.emp.plan.acft.domain.EmpAcftVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 항공기 REST 컨트롤러.
 *
 * <p>Base URL: {@code /api/emp/plan/{empPlanId}/acft}
 */
@RestController
@RequestMapping("/api/emp/plan/{empPlanId}/acft")
@RequiredArgsConstructor
public class EmpAcftController {

    private final EmpAcftService empAcftService;

    /**
     * 항공기 목록 조회.
     *
     * @param empPlanId EMP Plan ID (path)
     * @param user      로그인 사용자
     * @return 항공기 목록
     */
    @GetMapping
    public ApiResponse<List<EmpAcftVO>> list(
            @PathVariable String empPlanId,
            @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(empAcftService.listByPlan(empPlanId, user));
    }

    /**
     * 항공기 단건 조회.
     *
     * @param empPlanId EMP Plan ID (path)
     * @param sn        항공기 일련번호 (path)
     * @param user      로그인 사용자
     * @return 항공기 VO
     */
    @GetMapping("/{sn}")
    public ApiResponse<EmpAcftVO> getOne(
            @PathVariable String empPlanId,
            @PathVariable int sn,
            @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(empAcftService.getOne(empPlanId, sn, user));
    }

    /**
     * 항공기 추가.
     *
     * @param empPlanId EMP Plan ID (path)
     * @param vo        요청 바디 (acftTypeCd, fuelTypeCd, acftCnt 필수)
     * @param user      로그인 사용자
     * @return 생성된 항공기 VO
     */
    @PostMapping
    public ApiResponse<EmpAcftVO> add(
            @PathVariable String empPlanId,
            @RequestBody EmpAcftVO vo,
            @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(empAcftService.addChild(empPlanId, vo, user));
    }

    /**
     * 항공기 수정.
     *
     * @param empPlanId EMP Plan ID (path)
     * @param sn        항공기 일련번호 (path)
     * @param vo        요청 바디
     * @param user      로그인 사용자
     * @return 처리 결과
     */
    @PutMapping("/{sn}")
    public ApiResponse<Void> update(
            @PathVariable String empPlanId,
            @PathVariable int sn,
            @RequestBody EmpAcftVO vo,
            @AuthenticationPrincipal IcasUser user) {
        empAcftService.updateChild(empPlanId, sn, vo, user);
        return ApiResponse.ok(null);
    }

    /**
     * 항공기 소프트삭제.
     *
     * @param empPlanId EMP Plan ID (path)
     * @param sn        항공기 일련번호 (path)
     * @param user      로그인 사용자
     * @return 처리 결과
     */
    @DeleteMapping("/{sn}")
    public ApiResponse<Void> delete(
            @PathVariable String empPlanId,
            @PathVariable int sn,
            @AuthenticationPrincipal IcasUser user) {
        empAcftService.softDeleteChild(empPlanId, sn, user);
        return ApiResponse.ok(null);
    }
}
