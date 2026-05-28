package kr.go.molit.icas.emp.plan.co2detail;

import kr.go.molit.icas.common.dto.ApiResponse;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.emp.plan.co2detail.domain.EmpCo2DetailVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CO2 측정 상세 REST 컨트롤러.
 *
 * <p>Base URL: {@code /api/emp/plan/{empPlanId}/co2-detail}
 * <p>단건 조회/수정/삭제는 mntr_mthd_cd 를 path variable 로 사용.
 */
@RestController
@RequestMapping("/api/emp/plan/{empPlanId}/co2-detail")
@RequiredArgsConstructor
public class EmpCo2DetailController {

    private final EmpCo2DetailService empCo2DetailService;

    /**
     * CO2 측정 상세 목록 조회.
     *
     * @param empPlanId EMP Plan ID (path)
     * @param user      로그인 사용자
     * @return CO2 측정 상세 목록
     */
    @GetMapping
    public ApiResponse<List<EmpCo2DetailVO>> list(
            @PathVariable String empPlanId,
            @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(empCo2DetailService.listByPlan(empPlanId, user));
    }

    /**
     * CO2 측정 상세 단건 조회.
     *
     * @param empPlanId  EMP Plan ID (path)
     * @param mthdCd     모니터링 방법 코드 (path)
     * @param user       로그인 사용자
     * @return CO2 측정 상세 VO
     */
    @GetMapping("/{mthdCd}")
    public ApiResponse<EmpCo2DetailVO> getOne(
            @PathVariable String empPlanId,
            @PathVariable String mthdCd,
            @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(empCo2DetailService.getOne(empPlanId, mthdCd, user));
    }

    /**
     * CO2 측정 상세 추가.
     *
     * @param empPlanId EMP Plan ID (path)
     * @param vo        요청 바디 (mntrMthdCd 필수)
     * @param user      로그인 사용자
     * @return 생성된 CO2 측정 상세 VO
     */
    @PostMapping
    public ApiResponse<EmpCo2DetailVO> add(
            @PathVariable String empPlanId,
            @RequestBody EmpCo2DetailVO vo,
            @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(empCo2DetailService.addChild(empPlanId, vo, user));
    }

    /**
     * CO2 측정 상세 수정.
     *
     * @param empPlanId  EMP Plan ID (path)
     * @param mthdCd     모니터링 방법 코드 (path)
     * @param vo         요청 바디
     * @param user       로그인 사용자
     * @return 처리 결과
     */
    @PutMapping("/{mthdCd}")
    public ApiResponse<Void> update(
            @PathVariable String empPlanId,
            @PathVariable String mthdCd,
            @RequestBody EmpCo2DetailVO vo,
            @AuthenticationPrincipal IcasUser user) {
        empCo2DetailService.updateChild(empPlanId, mthdCd, vo, user);
        return ApiResponse.ok(null);
    }

    /**
     * CO2 측정 상세 소프트삭제.
     *
     * @param empPlanId  EMP Plan ID (path)
     * @param mthdCd     모니터링 방법 코드 (path)
     * @param user       로그인 사용자
     * @return 처리 결과
     */
    @DeleteMapping("/{mthdCd}")
    public ApiResponse<Void> delete(
            @PathVariable String empPlanId,
            @PathVariable String mthdCd,
            @AuthenticationPrincipal IcasUser user) {
        empCo2DetailService.softDeleteChild(empPlanId, mthdCd, user);
        return ApiResponse.ok(null);
    }
}
