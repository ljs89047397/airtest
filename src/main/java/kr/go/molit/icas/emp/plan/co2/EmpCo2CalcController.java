package kr.go.molit.icas.emp.plan.co2;

import kr.go.molit.icas.common.dto.ApiResponse;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.emp.plan.co2.domain.EmpCo2CalcVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 배출량 계산방법 REST 컨트롤러 (SFR-004).
 *
 * <pre>
 * GET    /api/emp/plan/{empPlanId}/co2-calc   — 단건 조회
 * PUT    /api/emp/plan/{empPlanId}/co2-calc   — Upsert (AIRLINE, DRAFT 한정)
 * DELETE /api/emp/plan/{empPlanId}/co2-calc   — 소프트삭제
 * </pre>
 *
 * <p>권한 검증은 Service 단 {@code DataScopeValidator} 에서 처리한다.
 * URL-method 매핑 권한 체크는 {@code AuthorityInterceptor} 가 담당.
 */
@RestController
@RequestMapping("/api/emp/plan/{empPlanId}/co2-calc")
@RequiredArgsConstructor
public class EmpCo2CalcController {

    private final EmpCo2CalcService empCo2CalcService;

    /**
     * 배출량 계산방법 단건 조회.
     *
     * @param empPlanId EMP Plan ID (path variable)
     * @param user      로그인 사용자
     * @return 배출량 계산방법 VO (미작성이면 data=null, success=true)
     */
    @GetMapping
    public ApiResponse<EmpCo2CalcVO> get(
            @PathVariable String empPlanId,
            @AuthenticationPrincipal IcasUser user) {
        EmpCo2CalcVO vo = empCo2CalcService.selectByPlanId(empPlanId, user);
        return ApiResponse.ok(vo);
    }

    /**
     * 배출량 계산방법 Upsert.
     *
     * <p>행이 없으면 INSERT, 있으면 UPDATE. 부모 plan 이 DRAFT 상태일 때만 허용.
     *
     * @param empPlanId EMP Plan ID (path variable)
     * @param vo        요청 바디
     * @param user      로그인 사용자
     * @return 처리 결과
     */
    @PutMapping
    public ApiResponse<Void> upsert(
            @PathVariable String empPlanId,
            @RequestBody EmpCo2CalcVO vo,
            @AuthenticationPrincipal IcasUser user) {
        empCo2CalcService.upsertCo2Calc(empPlanId, vo, user);
        return ApiResponse.ok(null);
    }

    /**
     * 배출량 계산방법 소프트삭제.
     *
     * <p>use_end_dt = NOW() - INTERVAL '1 minute' 로 논리 삭제.
     * 부모 plan 이 DRAFT 상태일 때만 허용.
     *
     * @param empPlanId EMP Plan ID (path variable)
     * @param user      로그인 사용자
     * @return 처리 결과
     */
    @DeleteMapping
    public ApiResponse<Void> delete(
            @PathVariable String empPlanId,
            @AuthenticationPrincipal IcasUser user) {
        empCo2CalcService.softDeleteByPlanId(empPlanId, user);
        return ApiResponse.ok(null);
    }
}
