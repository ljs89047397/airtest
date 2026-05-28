package kr.go.molit.icas.com.atrz;

import kr.go.molit.icas.com.atrz.domain.AtrzDmndSearch;
import kr.go.molit.icas.com.atrz.domain.AtrzDmndVO;
import kr.go.molit.icas.com.atrz.domain.AtrzPrcsVO;
import kr.go.molit.icas.com.atrz.domain.AtrzTaskVO;
import kr.go.molit.icas.common.dto.PageResponse;
import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.common.util.IdGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AtrzService 단위 테스트 — 결재 상태머신")
class AtrzServiceTest {

    @Mock
    AtrzDmndMapper atrzDmndMapper;

    @Mock
    AtrzPrcsMapper atrzPrcsMapper;

    @Mock
    AtrzTaskMapper atrzTaskMapper;

    @Mock
    IdGenerator idGenerator;

    @InjectMocks
    AtrzService atrzService;

    // ── 공통 fixture ──
    private IcasUser molitUser;
    private IcasUser requesterUser;   // 결재 요청자
    private IcasUser approver1User;   // 1단계 결재자
    private IcasUser approver2User;   // 2단계 결재자
    private IcasUser otherUser;       // 무관한 사용자

    @BeforeEach
    void setUpFixtures() {
        molitUser = IcasUser.builder()
                .userId("molit01")
                .userNm("국토부 담당자")
                .ognzSeCd("MOLIT")
                .ognzId("ORG_MOLIT")
                .master(false)
                .roleIds(List.of("ADMIN"))
                .build();

        requesterUser = IcasUser.builder()
                .userId("requester01")
                .userNm("결재 요청자")
                .ognzSeCd("AIRLINE")
                .ognzId("ORG_AIR01")
                .oprtrId("OP0001")
                .master(false)
                .roleIds(List.of("AIRLINE_USER"))
                .build();

        approver1User = IcasUser.builder()
                .userId("approver01")
                .userNm("1단계 결재자")
                .ognzSeCd("MOLIT")
                .ognzId("ORG_MOLIT")
                .master(false)
                .roleIds(List.of("TEAM_LEAD"))
                .build();

        approver2User = IcasUser.builder()
                .userId("approver02")
                .userNm("2단계 결재자")
                .ognzSeCd("MOLIT")
                .ognzId("ORG_MOLIT")
                .master(false)
                .roleIds(List.of("DEPT_HEAD"))
                .build();

        otherUser = IcasUser.builder()
                .userId("other01")
                .userNm("무관한 사용자")
                .ognzSeCd("AIRLINE")
                .ognzId("ORG_AIR02")
                .oprtrId("OP0002")
                .master(false)
                .roleIds(List.of("AIRLINE_USER"))
                .build();
    }

    // ── helper: AtrzTaskVO 생성 ──
    private AtrzTaskVO makeTask() {
        AtrzTaskVO task = new AtrzTaskVO();
        task.setAtrzTaskId("ATZ_EMP_PLAN");
        task.setAtrzTaskNm("고용계획 결재");
        task.setSysSeCd("EMP");
        return task;
    }

    // ── helper: AtrzPrcsVO 생성 ──
    private AtrzPrcsVO makePrcs(String dmndId, int seq, String userId, String rsltCd) {
        AtrzPrcsVO prcs = new AtrzPrcsVO();
        prcs.setAtrzDmndId(dmndId);
        prcs.setAtrzSeq(seq);
        prcs.setAtrzUserId(userId);
        prcs.setAtrzRsltCd(rsltCd);
        prcs.setAtrzRoleCd("TEAM_LEAD");
        return prcs;
    }

    // ── helper: AtrzDmndVO 생성 ──
    private AtrzDmndVO makeDmnd(String dmndId, String userId, String stCd) {
        AtrzDmndVO dmnd = new AtrzDmndVO();
        dmnd.setAtrzDmndId(dmndId);
        dmnd.setAtrzTaskId("ATZ_EMP_PLAN");
        dmnd.setDmndUserId(userId);
        dmnd.setAtrzStCd(stCd);
        dmnd.setTitle("고용계획 결재 요청");
        return dmnd;
    }

    // ── helper: SubmitRequest 생성 ──
    private AtrzService.SubmitRequest makeSubmitReq(List<AtrzService.ApproverItem> approvers) {
        AtrzService.SubmitRequest req = new AtrzService.SubmitRequest();
        req.setAtrzTaskId("ATZ_EMP_PLAN");
        req.setRfrncTblNm("emp.tn_emp_plan");
        req.setRfrncKeyCn("{\"empPlanId\":\"EP0001\"}");
        req.setTitle("2026년 고용계획 결재 요청");
        req.setContents("상세 내용");
        req.setApprovers(approvers);
        return req;
    }

    private AtrzService.ApproverItem makeApprover(String userId, String roleCd) {
        AtrzService.ApproverItem item = new AtrzService.ApproverItem();
        item.setAtrzUserId(userId);
        item.setAtrzRoleCd(roleCd);
        return item;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // submit — 결재 요청 제출
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("submit 정상 — 결재자 2명 지정 시 dmnd insert 1건 + prcs insert 2건, atrz_seq 1,2 부여")
    void submit_결재자2명_정상() {
        // given
        given(atrzTaskMapper.selectByTaskId("ATZ_EMP_PLAN")).willReturn(makeTask());
        given(atrzDmndMapper.countByPrefix("AD")).willReturn(0);
        given(idGenerator.managementPk("AD", 1)).willReturn("AD0001");
        given(atrzDmndMapper.insertAtrzDmnd(any(AtrzDmndVO.class))).willReturn(1);
        given(atrzPrcsMapper.insertAtrzPrcs(any(AtrzPrcsVO.class))).willReturn(1);

        List<AtrzService.ApproverItem> approvers = List.of(
                makeApprover("approver01", "TEAM_LEAD"),
                makeApprover("approver02", "DEPT_HEAD")
        );
        AtrzService.SubmitRequest req = makeSubmitReq(approvers);

        // when
        AtrzDmndVO result = atrzService.submit(req, requesterUser);

        // then
        assertThat(result.getAtrzDmndId()).isEqualTo("AD0001");
        assertThat(result.getDmndUserId()).isEqualTo("requester01");
        assertThat(result.getFrstRegUserId()).isEqualTo("requester01");

        // dmnd insert 1번 확인
        then(atrzDmndMapper).should(times(1)).insertAtrzDmnd(any(AtrzDmndVO.class));

        // prcs insert 2번 확인, atrz_seq 검증
        ArgumentCaptor<AtrzPrcsVO> prcsCaptor = ArgumentCaptor.forClass(AtrzPrcsVO.class);
        then(atrzPrcsMapper).should(times(2)).insertAtrzPrcs(prcsCaptor.capture());
        List<AtrzPrcsVO> capturedPrcs = prcsCaptor.getAllValues();
        assertThat(capturedPrcs.get(0).getAtrzSeq()).isEqualTo(1);
        assertThat(capturedPrcs.get(0).getAtrzUserId()).isEqualTo("approver01");
        assertThat(capturedPrcs.get(1).getAtrzSeq()).isEqualTo(2);
        assertThat(capturedPrcs.get(1).getAtrzUserId()).isEqualTo("approver02");
    }

    @Test
    @DisplayName("submit — 결재자 0명 시 BAD_REQUEST 예외")
    void submit_결재자0명_BAD_REQUEST() {
        given(atrzTaskMapper.selectByTaskId("ATZ_EMP_PLAN")).willReturn(makeTask());

        AtrzService.SubmitRequest req = makeSubmitReq(List.of());

        assertThatThrownBy(() -> atrzService.submit(req, requesterUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));

        then(atrzDmndMapper).should(never()).insertAtrzDmnd(any());
    }

    @Test
    @DisplayName("submit — 결재자 중복(같은 userId 두 번) 시 BAD_REQUEST 예외")
    void submit_결재자중복_BAD_REQUEST() {
        given(atrzTaskMapper.selectByTaskId("ATZ_EMP_PLAN")).willReturn(makeTask());

        List<AtrzService.ApproverItem> approvers = List.of(
                makeApprover("approver01", "TEAM_LEAD"),
                makeApprover("approver01", "DEPT_HEAD")  // 동일 userId 중복
        );
        AtrzService.SubmitRequest req = makeSubmitReq(approvers);

        assertThatThrownBy(() -> atrzService.submit(req, requesterUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));

        then(atrzDmndMapper).should(never()).insertAtrzDmnd(any());
    }

    @Test
    @DisplayName("submit — atrz_task 미존재 시 BAD_REQUEST 예외")
    void submit_atrzTask미존재_BAD_REQUEST() {
        given(atrzTaskMapper.selectByTaskId("ATZ_NOT_EXIST")).willReturn(null);

        AtrzService.SubmitRequest req = new AtrzService.SubmitRequest();
        req.setAtrzTaskId("ATZ_NOT_EXIST");
        req.setApprovers(List.of(makeApprover("approver01", "TEAM_LEAD")));

        assertThatThrownBy(() -> atrzService.submit(req, requesterUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // approve — 승인
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("approve seq=1 정상 — prcs APRVD 갱신, dmnd INPRG (마지막 단계 아님)")
    void approve_seq1_정상_INPRG() {
        // 1단계 결재자, 미처리 상태
        AtrzPrcsVO prcs = makePrcs("AD0001", 1, "approver01", null);
        given(atrzPrcsMapper.selectPrcs("AD0001", 1)).willReturn(prcs);
        given(atrzPrcsMapper.countPendingBefore("AD0001", 1)).willReturn(0);
        given(atrzPrcsMapper.updateAtrzPrcs(eq("AD0001"), eq(1), eq("APRVD"), any(), eq("approver01"))).willReturn(1);
        given(atrzPrcsMapper.countTotalPrcs("AD0001")).willReturn(2); // 총 2단계 → 1단계 완료 = 아직 진행중
        given(atrzDmndMapper.updateAtrzStCd(eq("AD0001"), eq("INPRG"), eq("approver01"))).willReturn(1);

        assertThatCode(() -> atrzService.approve("AD0001", 1, "검토 완료", approver1User))
                .doesNotThrowAnyException();

        then(atrzPrcsMapper).should().updateAtrzPrcs("AD0001", 1, "APRVD", "검토 완료", "approver01");
        then(atrzDmndMapper).should().updateAtrzStCd("AD0001", "INPRG", "approver01");
    }

    @Test
    @DisplayName("approve 마지막 seq — dmnd 상태 APRVD 로 최종 처리")
    void approve_마지막seq_APRVD() {
        // 2단계 결재자, 미처리 상태, 총 단계 2
        AtrzPrcsVO prcs = makePrcs("AD0001", 2, "approver02", null);
        given(atrzPrcsMapper.selectPrcs("AD0001", 2)).willReturn(prcs);
        given(atrzPrcsMapper.countPendingBefore("AD0001", 2)).willReturn(0);
        given(atrzPrcsMapper.updateAtrzPrcs(eq("AD0001"), eq(2), eq("APRVD"), any(), eq("approver02"))).willReturn(1);
        given(atrzPrcsMapper.countTotalPrcs("AD0001")).willReturn(2); // seq=2 == totalPrcs=2 → 마지막
        given(atrzDmndMapper.updateAtrzStCd(eq("AD0001"), eq("APRVD"), eq("approver02"))).willReturn(1);

        assertThatCode(() -> atrzService.approve("AD0001", 2, "최종 승인", approver2User))
                .doesNotThrowAnyException();

        then(atrzDmndMapper).should().updateAtrzStCd("AD0001", "APRVD", "approver02");
    }

    @Test
    @DisplayName("approve — 결재자 본인이 아닌 사용자 시도 시 FORBIDDEN 예외")
    void approve_결재자아님_FORBIDDEN() {
        AtrzPrcsVO prcs = makePrcs("AD0001", 1, "approver01", null);
        given(atrzPrcsMapper.selectPrcs("AD0001", 1)).willReturn(prcs);

        // otherUser 는 approver01 이 아님
        assertThatThrownBy(() -> atrzService.approve("AD0001", 1, "승인", otherUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(403));

        then(atrzPrcsMapper).should(never()).updateAtrzPrcs(any(), anyInt(), any(), any(), any());
    }

    @Test
    @DisplayName("approve — 이미 처리된 단계 (atrzRsltCd != null) 시 CONFLICT 예외")
    void approve_이미처리됨_CONFLICT() {
        // 이미 APRVD 처리된 상태
        AtrzPrcsVO prcs = makePrcs("AD0001", 1, "approver01", "APRVD");
        given(atrzPrcsMapper.selectPrcs("AD0001", 1)).willReturn(prcs);

        assertThatThrownBy(() -> atrzService.approve("AD0001", 1, "재승인", approver1User))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(409));
    }

    @Test
    @DisplayName("approve — 선행 단계 미처리 (countPendingBefore > 0) 시 BAD_REQUEST 예외")
    void approve_선행단계미처리_BAD_REQUEST() {
        AtrzPrcsVO prcs = makePrcs("AD0001", 2, "approver02", null);
        given(atrzPrcsMapper.selectPrcs("AD0001", 2)).willReturn(prcs);
        given(atrzPrcsMapper.countPendingBefore("AD0001", 2)).willReturn(1); // 선행 단계 미완료

        assertThatThrownBy(() -> atrzService.approve("AD0001", 2, "승인", approver2User))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // reject — 반려
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("reject 정상 — prcs RJCTD + dmnd RJCTD 즉시 종결")
    void reject_정상_RJCTD() {
        AtrzPrcsVO prcs = makePrcs("AD0001", 1, "approver01", null);
        given(atrzPrcsMapper.selectPrcs("AD0001", 1)).willReturn(prcs);
        given(atrzPrcsMapper.countPendingBefore("AD0001", 1)).willReturn(0);
        given(atrzPrcsMapper.updateAtrzPrcs(eq("AD0001"), eq(1), eq("RJCTD"), any(), eq("approver01"))).willReturn(1);
        given(atrzDmndMapper.updateAtrzStCd(eq("AD0001"), eq("RJCTD"), eq("approver01"))).willReturn(1);

        assertThatCode(() -> atrzService.reject("AD0001", 1, "반려 사유", approver1User))
                .doesNotThrowAnyException();

        then(atrzPrcsMapper).should().updateAtrzPrcs("AD0001", 1, "RJCTD", "반려 사유", "approver01");
        then(atrzDmndMapper).should().updateAtrzStCd("AD0001", "RJCTD", "approver01");
    }

    @Test
    @DisplayName("reject — 결재자 본인 아닌 경우 FORBIDDEN 예외")
    void reject_결재자아님_FORBIDDEN() {
        AtrzPrcsVO prcs = makePrcs("AD0001", 1, "approver01", null);
        given(atrzPrcsMapper.selectPrcs("AD0001", 1)).willReturn(prcs);

        assertThatThrownBy(() -> atrzService.reject("AD0001", 1, "반려", otherUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(403));
    }

    @Test
    @DisplayName("reject — 이미 처리된 단계 CONFLICT 예외")
    void reject_이미처리됨_CONFLICT() {
        AtrzPrcsVO prcs = makePrcs("AD0001", 1, "approver01", "RJCTD");
        given(atrzPrcsMapper.selectPrcs("AD0001", 1)).willReturn(prcs);

        assertThatThrownBy(() -> atrzService.reject("AD0001", 1, "재반려", approver1User))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(409));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // cancel — 취소
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("cancel 정상 — 요청자 본인, PEND 상태 → dmnd CNCLD")
    void cancel_요청자본인_PEND_CNCLD() {
        AtrzDmndVO dmnd = makeDmnd("AD0001", "requester01", "PEND");
        given(atrzDmndMapper.selectByDmndId("AD0001")).willReturn(dmnd);
        given(atrzDmndMapper.updateAtrzStCd(eq("AD0001"), eq("CNCLD"), eq("requester01"))).willReturn(1);

        assertThatCode(() -> atrzService.cancel("AD0001", requesterUser))
                .doesNotThrowAnyException();

        then(atrzDmndMapper).should().updateAtrzStCd("AD0001", "CNCLD", "requester01");
    }

    @Test
    @DisplayName("cancel 정상 — 요청자 본인, INPRG 상태 → dmnd CNCLD")
    void cancel_요청자본인_INPRG_CNCLD() {
        AtrzDmndVO dmnd = makeDmnd("AD0001", "requester01", "INPRG");
        given(atrzDmndMapper.selectByDmndId("AD0001")).willReturn(dmnd);
        given(atrzDmndMapper.updateAtrzStCd(eq("AD0001"), eq("CNCLD"), eq("requester01"))).willReturn(1);

        assertThatCode(() -> atrzService.cancel("AD0001", requesterUser))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("cancel — 다른 사람이 취소 시도 시 FORBIDDEN 예외")
    void cancel_타인시도_FORBIDDEN() {
        AtrzDmndVO dmnd = makeDmnd("AD0001", "requester01", "PEND");
        given(atrzDmndMapper.selectByDmndId("AD0001")).willReturn(dmnd);

        assertThatThrownBy(() -> atrzService.cancel("AD0001", otherUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(403));

        then(atrzDmndMapper).should(never()).updateAtrzStCd(any(), any(), any());
    }

    @Test
    @DisplayName("cancel — 이미 APRVD 상태 취소 시도 CONFLICT 예외")
    void cancel_APRVD상태_CONFLICT() {
        AtrzDmndVO dmnd = makeDmnd("AD0001", "requester01", "APRVD");
        given(atrzDmndMapper.selectByDmndId("AD0001")).willReturn(dmnd);

        assertThatThrownBy(() -> atrzService.cancel("AD0001", requesterUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(409));
    }

    @Test
    @DisplayName("cancel — 이미 RJCTD 상태 취소 시도 CONFLICT 예외")
    void cancel_RJCTD상태_CONFLICT() {
        AtrzDmndVO dmnd = makeDmnd("AD0001", "requester01", "RJCTD");
        given(atrzDmndMapper.selectByDmndId("AD0001")).willReturn(dmnd);

        assertThatThrownBy(() -> atrzService.cancel("AD0001", requesterUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(409));
    }

    @Test
    @DisplayName("cancel — 이미 CNCLD 상태 취소 시도 CONFLICT 예외")
    void cancel_CNCLD상태_CONFLICT() {
        AtrzDmndVO dmnd = makeDmnd("AD0001", "requester01", "CNCLD");
        given(atrzDmndMapper.selectByDmndId("AD0001")).willReturn(dmnd);

        assertThatThrownBy(() -> atrzService.cancel("AD0001", requesterUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(409));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getDmnd — 단건 조회 권한
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getDmnd — 요청자 본인은 조회 가능")
    void getDmnd_요청자본인_성공() {
        AtrzDmndVO dmnd = makeDmnd("AD0001", "requester01", "PEND");
        given(atrzDmndMapper.selectByDmndId("AD0001")).willReturn(dmnd);
        given(atrzPrcsMapper.selectPrcsByDmndId("AD0001")).willReturn(List.of());

        AtrzService.DmndDetail result = atrzService.getDmnd("AD0001", requesterUser);

        assertThat(result.getDmnd().getAtrzDmndId()).isEqualTo("AD0001");
        assertThat(result.getPrcsList()).isEmpty();
    }

    @Test
    @DisplayName("getDmnd — 결재자 본인(prcs에 포함)은 조회 가능")
    void getDmnd_결재자본인_성공() {
        AtrzDmndVO dmnd = makeDmnd("AD0001", "requester01", "INPRG");
        AtrzPrcsVO prcs = makePrcs("AD0001", 1, "approver01", null);
        given(atrzDmndMapper.selectByDmndId("AD0001")).willReturn(dmnd);
        given(atrzPrcsMapper.selectPrcsByDmndId("AD0001")).willReturn(List.of(prcs));

        AtrzService.DmndDetail result = atrzService.getDmnd("AD0001", approver1User);

        assertThat(result.getDmnd().getAtrzDmndId()).isEqualTo("AD0001");
        assertThat(result.getPrcsList()).hasSize(1);
    }

    @Test
    @DisplayName("getDmnd — MOLIT 사용자는 모든 결재 요청 조회 가능")
    void getDmnd_MOLIT_전체조회_가능() {
        AtrzDmndVO dmnd = makeDmnd("AD0001", "requester01", "PEND");
        given(atrzDmndMapper.selectByDmndId("AD0001")).willReturn(dmnd);
        given(atrzPrcsMapper.selectPrcsByDmndId("AD0001")).willReturn(List.of());

        AtrzService.DmndDetail result = atrzService.getDmnd("AD0001", molitUser);

        assertThat(result.getDmnd().getDmndUserId()).isEqualTo("requester01");
    }

    @Test
    @DisplayName("getDmnd — 권한 없는 사용자 접근 시 FORBIDDEN 예외")
    void getDmnd_권한없는사용자_FORBIDDEN() {
        AtrzDmndVO dmnd = makeDmnd("AD0001", "requester01", "PEND");
        given(atrzDmndMapper.selectByDmndId("AD0001")).willReturn(dmnd);
        given(atrzPrcsMapper.selectPrcsByDmndId("AD0001")).willReturn(List.of(
                makePrcs("AD0001", 1, "approver01", null) // otherUser 는 결재자 아님
        ));

        assertThatThrownBy(() -> atrzService.getDmnd("AD0001", otherUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(403));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // searchDmnds — 페이징 검색
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("searchDmnds — MOLIT 사용자는 전체 조회 (dmndUserId 미설정)")
    void searchDmnds_MOLIT_전체조회() {
        AtrzDmndSearch search = new AtrzDmndSearch();
        given(atrzDmndMapper.countAtrzDmnds(any())).willReturn(2L);
        given(atrzDmndMapper.selectAtrzDmnds(any())).willReturn(
                List.of(makeDmnd("AD0001", "requester01", "PEND"),
                        makeDmnd("AD0002", "requester02", "INPRG")));

        PageResponse<AtrzDmndVO> result = atrzService.searchDmnds(search, molitUser);

        assertThat(result.getTotal()).isEqualTo(2L);
        assertThat(result.getRows()).hasSize(2);
        // MOLIT 이므로 dmndUserId 필터 미설정
        assertThat(search.getDmndUserId()).isNull();
    }

    @Test
    @DisplayName("searchDmnds — AIRLINE 사용자는 본인 userId 로 dmndUserId 필터 자동 설정")
    void searchDmnds_AIRLINE_본인필터_자동설정() {
        AtrzDmndSearch search = new AtrzDmndSearch();
        given(atrzDmndMapper.countAtrzDmnds(any())).willReturn(1L);
        given(atrzDmndMapper.selectAtrzDmnds(any())).willReturn(
                List.of(makeDmnd("AD0001", "requester01", "PEND")));

        atrzService.searchDmnds(search, requesterUser);

        // AIRLINE 사용자 → dmndUserId 가 본인 ID 로 설정되어야 함
        assertThat(search.getDmndUserId()).isEqualTo("requester01");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // selectMyPending
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("selectMyPending — 내가 결재자인 PENDING 행만 반환")
    void selectMyPending_내결재대기목록_반환() {
        AtrzPrcsVO prcs = makePrcs("AD0001", 1, "approver01", null);
        given(atrzPrcsMapper.selectMyPending("approver01")).willReturn(List.of(prcs));

        List<AtrzPrcsVO> result = atrzService.selectMyPending(approver1User);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAtrzUserId()).isEqualTo("approver01");
        assertThat(result.get(0).getAtrzRsltCd()).isNull(); // 미처리 상태
        then(atrzPrcsMapper).should().selectMyPending("approver01");
    }
}
