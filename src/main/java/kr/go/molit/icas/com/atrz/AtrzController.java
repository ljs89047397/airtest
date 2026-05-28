package kr.go.molit.icas.com.atrz;

import kr.go.molit.icas.com.atrz.domain.AtrzDmndSearch;
import kr.go.molit.icas.com.atrz.domain.AtrzDmndVO;
import kr.go.molit.icas.com.atrz.domain.AtrzPrcsVO;
import kr.go.molit.icas.common.dto.ApiResponse;
import kr.go.molit.icas.common.dto.PageResponse;
import kr.go.molit.icas.common.security.IcasUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 결재 요청·처리 REST Controller.
 *
 * <pre>
 * GET  /api/com/atrz                        — 결재 요청 검색 (페이징)
 * GET  /api/com/atrz/my-pending             — 내 처리 대기 목록
 * GET  /api/com/atrz/{dmndId}               — 단건 조회 (dmnd + prcs 목록)
 * POST /api/com/atrz                        — 결재 요청 제출
 * POST /api/com/atrz/{dmndId}/approve       — 승인
 * POST /api/com/atrz/{dmndId}/reject        — 반려
 * POST /api/com/atrz/{dmndId}/cancel        — 취소
 * </pre>
 */
@RestController
@RequestMapping("/api/com/atrz")
@RequiredArgsConstructor
public class AtrzController {

    private final AtrzService atrzService;

    // ──────────────────────────────────────────────
    // 조회
    // ──────────────────────────────────────────────

    /**
     * 결재 요청 검색 (페이징).
     *
     * @param search atrzStCd / atrzTaskId / dmndUserId / dateFrom / dateTo / page / pageSize
     * @param user   로그인 사용자
     */
    @GetMapping
    public ApiResponse<PageResponse<AtrzDmndVO>> searchDmnds(
            @ModelAttribute AtrzDmndSearch search,
            @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(atrzService.searchDmnds(search, user));
    }

    /**
     * 내가 처리해야 할 PENDING 단계 목록 (현재 차례인 행).
     *
     * @param user 로그인 사용자
     */
    @GetMapping("/my-pending")
    public ApiResponse<List<AtrzPrcsVO>> myPending(
            @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(atrzService.selectMyPending(user));
    }

    /**
     * 결재 요청 단건 조회 (dmnd + prcs 목록 동시 반환).
     *
     * @param dmndId 결재 요청 ID
     * @param user   로그인 사용자
     */
    @GetMapping("/{dmndId}")
    public ApiResponse<AtrzService.DmndDetail> getDmnd(
            @PathVariable String dmndId,
            @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(atrzService.getDmnd(dmndId, user));
    }

    // ──────────────────────────────────────────────
    // 결재 요청 제출
    // ──────────────────────────────────────────────

    /**
     * 결재 요청 신규 제출.
     *
     * <p>요청 body:
     * <pre>{@code
     * {
     *   "atrzTaskId": "ATZ_EMP_PLAN",
     *   "rfrncTblNm": "emp.tn_emp_plan",
     *   "rfrncKeyCn": "{\"empPlanId\":\"EP0001\"}",
     *   "title":      "2026년 고용계획 결재 요청",
     *   "contents":   "상세 내용...",
     *   "approvers": [
     *     {"atrzUserId": "USER01", "atrzRoleCd": "TEAM_LEAD"},
     *     {"atrzUserId": "USER02", "atrzRoleCd": "DEPT_HEAD"}
     *   ]
     * }
     * }</pre>
     *
     * @param req  요청 body
     * @param user 로그인 사용자
     */
    @PostMapping
    public ApiResponse<AtrzDmndVO> submit(
            @RequestBody AtrzService.SubmitRequest req,
            @AuthenticationPrincipal IcasUser user) {
        AtrzDmndVO dmnd = atrzService.submit(req, user);
        return ApiResponse.ok(dmnd, "결재 요청이 제출되었습니다.");
    }

    // ──────────────────────────────────────────────
    // 결재 처리
    // ──────────────────────────────────────────────

    /**
     * 특정 단계 승인.
     *
     * <p>요청 body: {@code {"atrzSeq": 1, "atrzOpnn": "검토 완료"}}
     *
     * @param dmndId 결재 요청 ID
     * @param req    승인 요청 body (atrzSeq, atrzOpnn)
     * @param user   로그인 사용자 (결재자 본인)
     */
    @PostMapping("/{dmndId}/approve")
    public ApiResponse<Void> approve(
            @PathVariable String dmndId,
            @RequestBody ApproveRejectRequest req,
            @AuthenticationPrincipal IcasUser user) {
        atrzService.approve(dmndId, req.getAtrzSeq(), req.getAtrzOpnn(), user);
        return ApiResponse.ok(null, "승인 처리되었습니다.");
    }

    /**
     * 특정 단계 반려.
     *
     * <p>요청 body: {@code {"atrzSeq": 1, "atrzOpnn": "반려 사유"}}
     *
     * @param dmndId 결재 요청 ID
     * @param req    반려 요청 body (atrzSeq, atrzOpnn)
     * @param user   로그인 사용자 (결재자 본인)
     */
    @PostMapping("/{dmndId}/reject")
    public ApiResponse<Void> reject(
            @PathVariable String dmndId,
            @RequestBody ApproveRejectRequest req,
            @AuthenticationPrincipal IcasUser user) {
        atrzService.reject(dmndId, req.getAtrzSeq(), req.getAtrzOpnn(), user);
        return ApiResponse.ok(null, "반려 처리되었습니다.");
    }

    /**
     * 결재 요청 취소. 요청자 본인 + PEND/INPRG 상태만 가능.
     *
     * @param dmndId 결재 요청 ID
     * @param user   로그인 사용자 (요청자 본인)
     */
    @PostMapping("/{dmndId}/cancel")
    public ApiResponse<Void> cancel(
            @PathVariable String dmndId,
            @AuthenticationPrincipal IcasUser user) {
        atrzService.cancel(dmndId, user);
        return ApiResponse.ok(null, "결재 요청이 취소되었습니다.");
    }

    // ──────────────────────────────────────────────
    // 내부 요청 DTO
    // ──────────────────────────────────────────────

    /**
     * 승인 / 반려 공통 요청 body.
     */
    static class ApproveRejectRequest {
        /** 결재 순번 */
        private int    atrzSeq;
        /** 결재 의견 */
        private String atrzOpnn;

        public int    getAtrzSeq()            { return atrzSeq; }
        public String getAtrzOpnn()           { return atrzOpnn; }
        public void   setAtrzSeq(int atrzSeq) { this.atrzSeq = atrzSeq; }
        public void   setAtrzOpnn(String atrzOpnn) { this.atrzOpnn = atrzOpnn; }
    }
}
