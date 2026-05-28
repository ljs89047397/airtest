package kr.go.molit.icas.emp.plan.ctrl;

import kr.go.molit.icas.common.dto.ApiResponse;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.emp.plan.ctrl.domain.EmpDataCtrlVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 데이터 품질 통제 REST 컨트롤러 (SFR-005).
 *
 * <pre>
 * GET    /api/emp/plan/{empPlanId}/data-ctrl   — 단건 조회
 * PUT    /api/emp/plan/{empPlanId}/data-ctrl   — Upsert (AIRLINE, DRAFT 한정)
 * DELETE /api/emp/plan/{empPlanId}/data-ctrl   — 소프트삭제
 * </pre>
 *
 * <p>권한 검증은 Service 단 {@code DataScopeValidator} 에서 처리한다.
 * URL-method 매핑 권한 체크는 {@code AuthorityInterceptor} 가 담당.
 */
@RestController
@RequestMapping("/api/emp/plan/{empPlanId}/data-ctrl")
@RequiredArgsConstructor
public class EmpDataCtrlController {

    private final EmpDataCtrlService empDataCtrlService;

    /**
     * 데이터 품질 통제 단건 조회.
     *
     * @param empPlanId EMP Plan ID (path variable)
     * @param user      로그인 사용자
     * @return 데이터 품질 통제 VO (미작성이면 data=null, success=true)
     */
    @GetMapping
    public ApiResponse<EmpDataCtrlVO> get(
            @PathVariable String empPlanId,
            @AuthenticationPrincipal IcasUser user) {
        EmpDataCtrlVO vo = empDataCtrlService.selectByPlanId(empPlanId, user);
        return ApiResponse.ok(vo);
    }

    /**
     * 데이터 품질 통제 Upsert.
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
            @RequestBody EmpDataCtrlVO vo,
            @AuthenticationPrincipal IcasUser user) {
        empDataCtrlService.upsertDataCtrl(empPlanId, vo, user);
        return ApiResponse.ok(null);
    }

    /**
     * 데이터 품질 통제 소프트삭제.
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
        empDataCtrlService.softDeleteByPlanId(empPlanId, user);
        return ApiResponse.ok(null);
    }
}
