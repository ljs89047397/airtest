package kr.go.molit.icas.emp.plan.cntry;

import kr.go.molit.icas.common.dto.ApiResponse;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.emp.plan.cntry.domain.EmpCntryPairVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 운항 국가 쌍 REST 컨트롤러.
 *
 * <p>Base URL: {@code /api/emp/plan/{empPlanId}/cntry-pair}
 */
@RestController
@RequestMapping("/api/emp/plan/{empPlanId}/cntry-pair")
@RequiredArgsConstructor
public class EmpCntryPairController {

    private final EmpCntryPairService empCntryPairService;

    /**
     * 운항 국가 쌍 목록 조회.
     *
     * @param empPlanId EMP Plan ID (path)
     * @param user      로그인 사용자
     * @return 국가쌍 목록
     */
    @GetMapping
    public ApiResponse<List<EmpCntryPairVO>> list(
            @PathVariable String empPlanId,
            @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(empCntryPairService.listByPlan(empPlanId, user));
    }

    /**
     * 운항 국가 쌍 단건 조회.
     *
     * @param empPlanId EMP Plan ID (path)
     * @param sn        국가쌍 일련번호 (path)
     * @param user      로그인 사용자
     * @return 국가쌍 VO
     */
    @GetMapping("/{sn}")
    public ApiResponse<EmpCntryPairVO> getOne(
            @PathVariable String empPlanId,
            @PathVariable int sn,
            @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(empCntryPairService.getOne(empPlanId, sn, user));
    }

    /**
     * 운항 국가 쌍 추가.
     *
     * @param empPlanId EMP Plan ID (path)
     * @param vo        요청 바디 (dprtrCntryCd, arvlCntryCd, intlYn 필수)
     * @param user      로그인 사용자
     * @return 생성된 국가쌍 VO
     */
    @PostMapping
    public ApiResponse<EmpCntryPairVO> add(
            @PathVariable String empPlanId,
            @RequestBody EmpCntryPairVO vo,
            @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(empCntryPairService.addChild(empPlanId, vo, user));
    }

    /**
     * 운항 국가 쌍 수정.
     *
     * @param empPlanId EMP Plan ID (path)
     * @param sn        국가쌍 일련번호 (path)
     * @param vo        요청 바디
     * @param user      로그인 사용자
     * @return 처리 결과
     */
    @PutMapping("/{sn}")
    public ApiResponse<Void> update(
            @PathVariable String empPlanId,
            @PathVariable int sn,
            @RequestBody EmpCntryPairVO vo,
            @AuthenticationPrincipal IcasUser user) {
        empCntryPairService.updateChild(empPlanId, sn, vo, user);
        return ApiResponse.ok(null);
    }

    /**
     * 운항 국가 쌍 소프트삭제.
     *
     * @param empPlanId EMP Plan ID (path)
     * @param sn        국가쌍 일련번호 (path)
     * @param user      로그인 사용자
     * @return 처리 결과
     */
    @DeleteMapping("/{sn}")
    public ApiResponse<Void> delete(
            @PathVariable String empPlanId,
            @PathVariable int sn,
            @AuthenticationPrincipal IcasUser user) {
        empCntryPairService.softDeleteChild(empPlanId, sn, user);
        return ApiResponse.ok(null);
    }
}
