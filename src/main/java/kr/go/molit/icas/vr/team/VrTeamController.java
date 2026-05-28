package kr.go.molit.icas.vr.team;

import kr.go.molit.icas.common.dto.ApiResponse;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.vr.team.domain.VrTeamVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 검증팀 구성원 REST API.
 *
 * <pre>
 * GET    /api/vr/{vrId}/team             — 팀 목록 조회
 * POST   /api/vr/{vrId}/team             — 구성원 추가 (VERIFIER, DRAFT)
 * PUT    /api/vr/{vrId}/team/{teamSn}    — 구성원 수정
 * DELETE /api/vr/{vrId}/team/{teamSn}    — 구성원 삭제
 * </pre>
 */
@RestController
@RequestMapping("/api/vr/{vrId}/team")
@RequiredArgsConstructor
public class VrTeamController {

    private final VrTeamService teamService;

    @GetMapping
    public ApiResponse<List<VrTeamVO>> list(@PathVariable String vrId,
                                            @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(teamService.list(vrId, user));
    }

    @PostMapping
    public ApiResponse<VrTeamVO> add(@PathVariable String vrId,
                                     @RequestBody VrTeamVO vo,
                                     @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(teamService.add(vrId, vo, user), "구성원이 추가되었습니다.");
    }

    @PutMapping("/{teamSn}")
    public ApiResponse<VrTeamVO> update(@PathVariable String vrId,
                                        @PathVariable int teamSn,
                                        @RequestBody VrTeamVO vo,
                                        @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(teamService.update(vrId, teamSn, vo, user));
    }

    @DeleteMapping("/{teamSn}")
    public ApiResponse<Void> delete(@PathVariable String vrId,
                                    @PathVariable int teamSn,
                                    @AuthenticationPrincipal IcasUser user) {
        teamService.delete(vrId, teamSn, user);
        return ApiResponse.ok(null);
    }
}
