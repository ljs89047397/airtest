package kr.go.molit.icas.emp.plan.cnct;

import kr.go.molit.icas.common.dto.ApiResponse;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.emp.plan.cnct.domain.EmpOprtrCnctVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 담당자 연락처 REST 컨트롤러.
 *
 * <p>Base URL: {@code /api/emp/plan/{empPlanId}/cnct}
 */
@RestController
@RequestMapping("/api/emp/plan/{empPlanId}/cnct")
@RequiredArgsConstructor
public class EmpOprtrCnctController {

    private final EmpOprtrCnctService empOprtrCnctService;

    /**
     * 담당자 연락처 목록 조회.
     *
     * @param empPlanId EMP Plan ID (path)
     * @param user      로그인 사용자
     * @return 연락처 목록
     */
    @GetMapping
    public ApiResponse<List<EmpOprtrCnctVO>> list(
            @PathVariable String empPlanId,
            @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(empOprtrCnctService.listByPlan(empPlanId, user));
    }

    /**
     * 담당자 연락처 단건 조회.
     *
     * @param empPlanId EMP Plan ID (path)
     * @param sn        연락처 일련번호 (path)
     * @param user      로그인 사용자
     * @return 연락처 VO
     */
    @GetMapping("/{sn}")
    public ApiResponse<EmpOprtrCnctVO> getOne(
            @PathVariable String empPlanId,
            @PathVariable int sn,
            @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(empOprtrCnctService.getOne(empPlanId, sn, user));
    }

    /**
     * 담당자 연락처 추가.
     *
     * @param empPlanId EMP Plan ID (path)
     * @param vo        요청 바디 (cnctSeCd, userNm, mblphnNo/emlAddr)
     * @param user      로그인 사용자
     * @return 생성된 연락처 VO
     */
    @PostMapping
    public ApiResponse<EmpOprtrCnctVO> add(
            @PathVariable String empPlanId,
            @RequestBody EmpOprtrCnctVO vo,
            @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(empOprtrCnctService.addChild(empPlanId, vo, user));
    }

    /**
     * 담당자 연락처 수정.
     *
     * @param empPlanId EMP Plan ID (path)
     * @param sn        연락처 일련번호 (path)
     * @param vo        요청 바디
     * @param user      로그인 사용자
     * @return 처리 결과
     */
    @PutMapping("/{sn}")
    public ApiResponse<Void> update(
            @PathVariable String empPlanId,
            @PathVariable int sn,
            @RequestBody EmpOprtrCnctVO vo,
            @AuthenticationPrincipal IcasUser user) {
        empOprtrCnctService.updateChild(empPlanId, sn, vo, user);
        return ApiResponse.ok(null);
    }

    /**
     * 담당자 연락처 소프트삭제.
     *
     * @param empPlanId EMP Plan ID (path)
     * @param sn        연락처 일련번호 (path)
     * @param user      로그인 사용자
     * @return 처리 결과
     */
    @DeleteMapping("/{sn}")
    public ApiResponse<Void> delete(
            @PathVariable String empPlanId,
            @PathVariable int sn,
            @AuthenticationPrincipal IcasUser user) {
        empOprtrCnctService.softDeleteChild(empPlanId, sn, user);
        return ApiResponse.ok(null);
    }
}
