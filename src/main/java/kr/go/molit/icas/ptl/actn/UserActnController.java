package kr.go.molit.icas.ptl.actn;

import kr.go.molit.icas.common.dto.ApiResponse;
import kr.go.molit.icas.common.dto.PageResponse;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.ptl.actn.domain.UserActnSearch;
import kr.go.molit.icas.ptl.actn.domain.UserActnVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 사용자 행위 감사 로그 REST API (MOLIT/KOTSA 전용).
 *
 * <pre>
 * GET /api/ptl/actn                 — 목록 조회 (페이징)
 * GET /api/ptl/actn/target          — 특정 엔티티 행위 이력 조회
 * </pre>
 */
@RestController
@RequestMapping("/api/ptl/actn")
@RequiredArgsConstructor
public class UserActnController {

    private final UserActnService userActnService;

    /** 감사 로그 목록 조회 (페이징) */
    @GetMapping
    public ApiResponse<PageResponse<UserActnVO>> listActns(
            UserActnSearch search,
            @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(userActnService.listActns(search, user));
    }

    /** 특정 엔티티 행위 이력 조회 */
    @GetMapping("/target")
    public ApiResponse<List<UserActnVO>> listByTarget(
            @RequestParam String targetTbl,
            @RequestParam String targetPk,
            @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(userActnService.listByTarget(targetTbl, targetPk, user));
    }
}
