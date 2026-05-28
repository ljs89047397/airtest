package kr.go.molit.icas.emp.plan.risk;

import kr.go.molit.icas.common.dto.ApiResponse;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.emp.plan.risk.domain.EmpRiskVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 위험·통제 항목 REST 컨트롤러.
 *
 * <p>Base URL: {@code /api/emp/plan/{empPlanId}/risk}
 */
@RestController
@RequestMapping("/api/emp/plan/{empPlanId}/risk")
@RequiredArgsConstructor
public class EmpRiskController {

    private final EmpRiskService empRiskService;

    /**
     * 위험·통제 항목 목록 조회.
     *
     * @param empPlanId EMP Plan ID (path)
     * @param user      로그인 사용자
     * @return 위험·통제 목록
     */
    @GetMapping
    public ApiResponse<List<EmpRiskVO>> list(
            @PathVariable String empPlanId,
            @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(empRiskService.listByPlan(empPlanId, user));
    }

    /**
     * 위험·통제 항목 단건 조회.
     *
     * @param empPlanId EMP Plan ID (path)
     * @param sn        위험 항목 일련번호 (path)
     * @param user      로그인 사용자
     * @return 위험·통제 VO
     */
    @GetMapping("/{sn}")
    public ApiResponse<EmpRiskVO> getOne(
            @PathVariable String empPlanId,
            @PathVariable int sn,
            @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(empRiskService.getOne(empPlanId, sn, user));
    }

    /**
     * 위험·통제 항목 추가.
     *
     * @param empPlanId EMP Plan ID (path)
     * @param vo        요청 바디 (riskDesc 필수)
     * @param user      로그인 사용자
     * @return 생성된 위험·통제 항목 VO
     */
    @PostMapping
    public ApiResponse<EmpRiskVO> add(
            @PathVariable String empPlanId,
            @RequestBody EmpRiskVO vo,
            @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(empRiskService.addChild(empPlanId, vo, user));
    }

    /**
     * 위험·통제 항목 수정.
     *
     * @param empPlanId EMP Plan ID (path)
     * @param sn        위험 항목 일련번호 (path)
     * @param vo        요청 바디
     * @param user      로그인 사용자
     * @return 처리 결과
     */
    @PutMapping("/{sn}")
    public ApiResponse<Void> update(
            @PathVariable String empPlanId,
            @PathVariable int sn,
            @RequestBody EmpRiskVO vo,
            @AuthenticationPrincipal IcasUser user) {
        empRiskService.updateChild(empPlanId, sn, vo, user);
        return ApiResponse.ok(null);
    }

    /**
     * 위험·통제 항목 소프트삭제.
     *
     * @param empPlanId EMP Plan ID (path)
     * @param sn        위험 항목 일련번호 (path)
     * @param user      로그인 사용자
     * @return 처리 결과
     */
    @DeleteMapping("/{sn}")
    public ApiResponse<Void> delete(
            @PathVariable String empPlanId,
            @PathVariable int sn,
            @AuthenticationPrincipal IcasUser user) {
        empRiskService.softDeleteChild(empPlanId, sn, user);
        return ApiResponse.ok(null);
    }
}
