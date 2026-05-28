package kr.go.molit.icas.com.atrz;

import kr.go.molit.icas.com.atrz.domain.AtrzDmndSearch;
import kr.go.molit.icas.com.atrz.domain.AtrzDmndVO;
import kr.go.molit.icas.com.atrz.domain.AtrzPrcsVO;
import kr.go.molit.icas.com.atrz.domain.AtrzTaskVO;
import kr.go.molit.icas.common.dto.PageResponse;
import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.common.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 결재 흐름 통합 서비스 (요청 · 처리).
 *
 * <p>비즈니스 흐름:
 * <ol>
 *   <li>submit  — 결재 요청 등록 + 결재자 목록(atrz_prcs) 다중 insert</li>
 *   <li>approve — 특정 단계 승인. 선행 단계 강제. 마지막이면 dmnd APRVD</li>
 *   <li>reject  — 특정 단계 반려. dmnd 즉시 RJCTD 종결</li>
 *   <li>cancel  — 요청자 본인이 PEND/INPRG 일 때만 취소</li>
 * </ol>
 *
 * <p>권한 규칙:
 * <ul>
 *   <li>MOLIT/KOTSA — 전체 조회 가능</li>
 *   <li>그 외 — 자기 dmnd_user_id 인 건만 조회</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AtrzService {

    /** atrz_dmnd_id 채번 접두어 */
    private static final String DMND_ID_PREFIX = "AD";

    private final AtrzDmndMapper  atrzDmndMapper;
    private final AtrzPrcsMapper  atrzPrcsMapper;
    private final AtrzTaskMapper  atrzTaskMapper;
    private final IdGenerator     idGenerator;

    // ──────────────────────────────────────────────
    // 결재 요청 제출
    // ──────────────────────────────────────────────

    /**
     * 결재 요청 신규 생성.
     *
     * @param req  요청 정보 (atrzTaskId, rfrncTblNm, rfrncKeyCn, title, contents, approvers)
     * @param user 로그인 사용자 (dmnd_user_id 로 설정)
     * @return 생성된 결재 요청 VO
     * @throws BusinessException BAD_REQUEST — atrz_task 미존재 / 결재자 0명 / 결재자 중복
     */
    @Transactional
    public AtrzDmndVO submit(SubmitRequest req, IcasUser user) {
        // 1. 결재 업무 존재·유효 확인
        AtrzTaskVO task = atrzTaskMapper.selectByTaskId(req.getAtrzTaskId());
        if (task == null) {
            throw BusinessException.badRequest("존재하지 않거나 유효기간이 만료된 결재 업무입니다: " + req.getAtrzTaskId());
        }

        // 2. 결재자 검증 — 0명 금지
        List<ApproverItem> approvers = req.getApprovers();
        if (approvers == null || approvers.isEmpty()) {
            throw BusinessException.badRequest("결재자는 1명 이상이어야 합니다.");
        }

        // 3. 결재자 중복 체크 (같은 userId 두 번 금지)
        Set<String> approverIdSet = new HashSet<>();
        for (ApproverItem item : approvers) {
            if (item.getAtrzUserId() == null || item.getAtrzUserId().isBlank()) {
                throw BusinessException.badRequest("결재자 사용자 ID 는 필수입니다.");
            }
            if (!approverIdSet.add(item.getAtrzUserId())) {
                throw BusinessException.badRequest("결재자 목록에 동일한 사용자가 중복되었습니다: " + item.getAtrzUserId());
            }
        }

        // 4. atrz_dmnd_id 채번 (AD + 4자리)
        int nextSeq = atrzDmndMapper.countByPrefix(DMND_ID_PREFIX) + 1;
        String dmndId = idGenerator.managementPk(DMND_ID_PREFIX, nextSeq);

        // 5. 결재 요청 insert
        AtrzDmndVO dmnd = new AtrzDmndVO();
        dmnd.setAtrzDmndId(dmndId);
        dmnd.setAtrzTaskId(req.getAtrzTaskId());
        dmnd.setRfrncTblNm(req.getRfrncTblNm());
        dmnd.setRfrncKeyCn(req.getRfrncKeyCn());
        dmnd.setDmndUserId(user.getUsername());
        dmnd.setTitle(req.getTitle());
        dmnd.setContents(req.getContents());
        dmnd.setFrstRegUserId(user.getUsername());
        dmnd.setLastChgUserId(user.getUsername());

        atrzDmndMapper.insertAtrzDmnd(dmnd);

        // 6. 결재 처리 행 다중 insert (atrz_seq = 1, 2, 3 ..., atrz_rslt_cd = NULL)
        for (int i = 0; i < approvers.size(); i++) {
            ApproverItem item = approvers.get(i);

            AtrzPrcsVO prcs = new AtrzPrcsVO();
            prcs.setAtrzDmndId(dmndId);
            prcs.setAtrzSeq(i + 1);
            prcs.setAtrzUserId(item.getAtrzUserId());
            prcs.setAtrzRoleCd(item.getAtrzRoleCd());
            prcs.setFrstRegUserId(user.getUsername());
            prcs.setLastChgUserId(user.getUsername());

            atrzPrcsMapper.insertAtrzPrcs(prcs);
        }

        return dmnd;
    }

    // ──────────────────────────────────────────────
    // 승인
    // ──────────────────────────────────────────────

    /**
     * 특정 단계 승인.
     *
     * @param dmndId  결재 요청 ID
     * @param seq     결재 순번
     * @param atrzOpnn 결재 의견
     * @param user    로그인 사용자 (결재자 본인 검증)
     * @throws BusinessException NOT_FOUND  — prcs 행 미존재
     * @throws BusinessException FORBIDDEN  — 결재자 본인이 아님
     * @throws BusinessException CONFLICT   — 이미 처리된 단계
     * @throws BusinessException BAD_REQUEST — 선행 단계 미완료
     */
    @Transactional
    public void approve(String dmndId, int seq, String atrzOpnn, IcasUser user) {
        // 0. dmnd 종결 상태 확인 (RJCTD/APRVD/CNCLD 면 처리 차단)
        assertDmndOpen(dmndId);

        // 1. 결재 처리 행 존재 확인
        AtrzPrcsVO prcs = atrzPrcsMapper.selectPrcs(dmndId, seq);
        if (prcs == null) throw BusinessException.notFound("결재 단계");

        // 2. 결재자 본인 검증
        if (!user.getUsername().equals(prcs.getAtrzUserId())) {
            throw BusinessException.forbidden("해당 결재 단계의 결재자가 아닙니다.");
        }

        // 3. 이미 처리됨 검증 (atrz_rslt_cd != null 이면 이미 처리)
        if (prcs.getAtrzRsltCd() != null) {
            throw BusinessException.conflict("이미 처리된 결재 단계입니다.");
        }

        // 4. 선행 단계 모두 APRVD 확인 (RJCTD 는 별도 dmnd 상태 차단으로 처리)
        int pendingBefore = atrzPrcsMapper.countPendingBefore(dmndId, seq);
        if (pendingBefore > 0) {
            throw BusinessException.badRequest("선행 결재 단계가 아직 완료되지 않았습니다. 순서대로 결재해야 합니다.");
        }

        // 5. prcs 승인 처리
        atrzPrcsMapper.updateAtrzPrcs(dmndId, seq, "APRVD", atrzOpnn, user.getUsername());

        // 6. 마지막 단계 여부 확인 후 dmnd 상태 업데이트
        int totalPrcs = atrzPrcsMapper.countTotalPrcs(dmndId);
        String newDmndSt = (seq >= totalPrcs) ? "APRVD" : "INPRG";
        atrzDmndMapper.updateAtrzStCd(dmndId, newDmndSt, user.getUsername());
    }

    // ──────────────────────────────────────────────
    // 반려
    // ──────────────────────────────────────────────

    /**
     * 특정 단계 반려. dmnd 즉시 RJCTD 종결.
     *
     * @param dmndId  결재 요청 ID
     * @param seq     결재 순번
     * @param atrzOpnn 반려 의견
     * @param user    로그인 사용자 (결재자 본인 검증)
     * @throws BusinessException NOT_FOUND  — prcs 행 미존재
     * @throws BusinessException FORBIDDEN  — 결재자 본인이 아님
     * @throws BusinessException CONFLICT   — 이미 처리된 단계
     * @throws BusinessException BAD_REQUEST — 선행 단계 미완료
     */
    @Transactional
    public void reject(String dmndId, int seq, String atrzOpnn, IcasUser user) {
        // 0. dmnd 종결 상태 확인 (RJCTD/APRVD/CNCLD 면 처리 차단)
        assertDmndOpen(dmndId);

        // 1. 결재 처리 행 존재 확인
        AtrzPrcsVO prcs = atrzPrcsMapper.selectPrcs(dmndId, seq);
        if (prcs == null) throw BusinessException.notFound("결재 단계");

        // 2. 결재자 본인 검증
        if (!user.getUsername().equals(prcs.getAtrzUserId())) {
            throw BusinessException.forbidden("해당 결재 단계의 결재자가 아닙니다.");
        }

        // 3. 이미 처리됨 검증
        if (prcs.getAtrzRsltCd() != null) {
            throw BusinessException.conflict("이미 처리된 결재 단계입니다.");
        }

        // 4. 선행 단계 모두 처리 완료 확인
        int pendingBefore = atrzPrcsMapper.countPendingBefore(dmndId, seq);
        if (pendingBefore > 0) {
            throw BusinessException.badRequest("선행 결재 단계가 아직 완료되지 않았습니다. 순서대로 결재해야 합니다.");
        }

        // 5. prcs 반려 처리
        atrzPrcsMapper.updateAtrzPrcs(dmndId, seq, "RJCTD", atrzOpnn, user.getUsername());

        // 6. dmnd 즉시 RJCTD 종결
        atrzDmndMapper.updateAtrzStCd(dmndId, "RJCTD", user.getUsername());
    }

    // ──────────────────────────────────────────────
    // 취소
    // ──────────────────────────────────────────────

    /**
     * 결재 요청 취소. 요청자 본인만 가능.
     * PEND 또는 INPRG 상태인 경우만 취소 가능.
     *
     * @param dmndId 결재 요청 ID
     * @param user   로그인 사용자 (요청자 본인 검증)
     * @throws BusinessException NOT_FOUND — 요청 미존재
     * @throws BusinessException FORBIDDEN — 요청자 본인이 아님
     * @throws BusinessException CONFLICT  — 이미 종결된 요청
     */
    @Transactional
    public void cancel(String dmndId, IcasUser user) {
        // 1. 결재 요청 조회
        AtrzDmndVO dmnd = atrzDmndMapper.selectByDmndId(dmndId);
        if (dmnd == null) throw BusinessException.notFound("결재 요청");

        // 2. 요청자 본인 검증
        if (!user.getUsername().equals(dmnd.getDmndUserId())) {
            throw BusinessException.forbidden("결재 요청자 본인만 취소할 수 있습니다.");
        }

        // 3. 취소 가능 상태 확인 (PEND / INPRG 만)
        String currentSt = dmnd.getAtrzStCd();
        if (!"PEND".equals(currentSt) && !"INPRG".equals(currentSt)) {
            throw BusinessException.conflict("이미 종결된 결재 요청은 취소할 수 없습니다. 현재 상태: " + currentSt);
        }

        // 4. dmnd 취소 처리
        atrzDmndMapper.updateAtrzStCd(dmndId, "CNCLD", user.getUsername());
    }

    // ──────────────────────────────────────────────
    // 조회
    // ──────────────────────────────────────────────

    /**
     * 결재 요청 페이징 검색.
     * MOLIT/KOTSA — 전체 조회 가능.
     * 그 외 — 자기 dmnd_user_id 건만 조회.
     *
     * @param search 검색 조건 DTO
     * @param user   로그인 사용자
     * @return 페이징 결과
     */
    public PageResponse<AtrzDmndVO> searchDmnds(AtrzDmndSearch search, IcasUser user) {
        // 행 단위 가시범위 적용: MOLIT/KOTSA 외 → 본인 요청만
        if (!user.isMolitOrKotsa()) {
            search.setDmndUserId(user.getUsername());
        }

        long total = atrzDmndMapper.countAtrzDmnds(search);
        List<AtrzDmndVO> rows = atrzDmndMapper.selectAtrzDmnds(search);
        return new PageResponse<>(rows, search.getPage(), search.getPageSize(), total);
    }

    /**
     * 결재 요청 단건 조회 (dmnd + prcs 목록 포함).
     * 권한: 요청자 본인 / 결재자 본인(prcs에 포함) / MOLIT·KOTSA
     *
     * @param dmndId 결재 요청 ID
     * @param user   로그인 사용자
     * @return 결재 요청 상세 응답 (dmnd + prcs 목록)
     * @throws BusinessException NOT_FOUND — 미존재
     * @throws BusinessException FORBIDDEN — 접근 권한 없음
     */
    public DmndDetail getDmnd(String dmndId, IcasUser user) {
        AtrzDmndVO dmnd = atrzDmndMapper.selectByDmndId(dmndId);
        if (dmnd == null) throw BusinessException.notFound("결재 요청");

        List<AtrzPrcsVO> prcsList = atrzPrcsMapper.selectPrcsByDmndId(dmndId);

        // 권한 확인 — MOLIT/KOTSA 전체 허용
        if (!user.isMolitOrKotsa()) {
            boolean isDmndUser = user.getUsername().equals(dmnd.getDmndUserId());
            boolean isApprover = prcsList.stream()
                    .anyMatch(p -> user.getUsername().equals(p.getAtrzUserId()));
            if (!isDmndUser && !isApprover) {
                throw BusinessException.forbidden("해당 결재 요청에 접근할 권한이 없습니다.");
            }
        }

        return new DmndDetail(dmnd, prcsList);
    }

    /**
     * 내가 처리해야 할 PENDING 단계 목록 (현재 차례인 행).
     *
     * @param user 로그인 사용자
     * @return 내 처리 대기 목록
     */
    public List<AtrzPrcsVO> selectMyPending(IcasUser user) {
        return atrzPrcsMapper.selectMyPending(user.getUsername());
    }

    /**
     * 결재 요청이 처리 가능한 상태(PEND / INPRG)인지 확인.
     * APRVD / RJCTD / CNCLD 면 conflict 던짐.
     * approve/reject 진입 시 호출하여 종결된 dmnd 에 대한 후속 처리 차단.
     */
    private void assertDmndOpen(String dmndId) {
        AtrzDmndVO dmnd = atrzDmndMapper.selectByDmndId(dmndId);
        if (dmnd == null) throw BusinessException.notFound("결재 요청");
        String currentSt = dmnd.getAtrzStCd();
        if (!"PEND".equals(currentSt) && !"INPRG".equals(currentSt)) {
            throw BusinessException.conflict(
                    "이미 종결된 결재 요청에는 추가 처리를 할 수 없습니다. 현재 상태: " + currentSt);
        }
    }

    // ──────────────────────────────────────────────
    // 내부 요청/응답 DTO
    // ──────────────────────────────────────────────

    /**
     * submit 요청 파라미터.
     */
    public static class SubmitRequest {
        private String            atrzTaskId;
        private String            rfrncTblNm;
        private String            rfrncKeyCn;
        private String            title;
        private String            contents;
        private List<ApproverItem> approvers;

        public String             getAtrzTaskId()  { return atrzTaskId; }
        public String             getRfrncTblNm()  { return rfrncTblNm; }
        public String             getRfrncKeyCn()  { return rfrncKeyCn; }
        public String             getTitle()       { return title; }
        public String             getContents()    { return contents; }
        public List<ApproverItem> getApprovers()   { return approvers; }

        public void setAtrzTaskId(String atrzTaskId)           { this.atrzTaskId = atrzTaskId; }
        public void setRfrncTblNm(String rfrncTblNm)           { this.rfrncTblNm = rfrncTblNm; }
        public void setRfrncKeyCn(String rfrncKeyCn)           { this.rfrncKeyCn = rfrncKeyCn; }
        public void setTitle(String title)                     { this.title = title; }
        public void setContents(String contents)               { this.contents = contents; }
        public void setApprovers(List<ApproverItem> approvers) { this.approvers = approvers; }
    }

    /**
     * 결재자 목록 항목.
     */
    public static class ApproverItem {
        private String atrzUserId;
        private String atrzRoleCd;

        public String getAtrzUserId() { return atrzUserId; }
        public String getAtrzRoleCd() { return atrzRoleCd; }
        public void   setAtrzUserId(String atrzUserId) { this.atrzUserId = atrzUserId; }
        public void   setAtrzRoleCd(String atrzRoleCd) { this.atrzRoleCd = atrzRoleCd; }
    }

    /**
     * getDmnd 응답 구조 — dmnd + prcs 목록 동시 반환.
     */
    public static class DmndDetail {
        private final AtrzDmndVO       dmnd;
        private final List<AtrzPrcsVO> prcsList;

        public DmndDetail(AtrzDmndVO dmnd, List<AtrzPrcsVO> prcsList) {
            this.dmnd    = dmnd;
            this.prcsList = prcsList;
        }

        public AtrzDmndVO       getDmnd()     { return dmnd; }
        public List<AtrzPrcsVO> getPrcsList() { return prcsList; }
    }
}
