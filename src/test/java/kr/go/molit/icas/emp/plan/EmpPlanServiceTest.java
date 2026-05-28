package kr.go.molit.icas.emp.plan;

import kr.go.molit.icas.com.oprtr.OprtrMapper;
import kr.go.molit.icas.com.oprtr.domain.OprtrVO;
import kr.go.molit.icas.common.dto.PageResponse;
import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.DataScopeValidator;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.common.util.IdGenerator;
import kr.go.molit.icas.emp.plan.domain.EmpChgHstryVO;
import kr.go.molit.icas.emp.plan.domain.EmpPlanSearch;
import kr.go.molit.icas.emp.plan.domain.EmpPlanVO;
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
@DisplayName("EmpPlanService 단위 테스트 — 상태머신 + 가시범위")
class EmpPlanServiceTest {

    @Mock
    EmpPlanMapper empPlanMapper;

    @Mock
    EmpChgHstryMapper empChgHstryMapper;

    @Mock
    OprtrMapper oprtrMapper;

    @Mock
    DataScopeValidator dataScopeValidator;

    @Mock
    IdGenerator idGenerator;

    @InjectMocks
    EmpPlanService empPlanService;

    // ── 공통 fixture ──
    private IcasUser molitUser;
    private IcasUser kotsaUser;
    private IcasUser airlineUserOP0001;
    private IcasUser airlineUserOP0002;
    private IcasUser verifierUser;
    private IcasUser unknownUser;

    @BeforeEach
    void setUpFixtures() {
        molitUser = IcasUser.builder()
                .userId("molit01").userNm("국토부 담당자")
                .ognzSeCd("MOLIT").ognzId("ORG_MOLIT").master(false)
                .roleIds(List.of("ADMIN")).build();

        kotsaUser = IcasUser.builder()
                .userId("kotsa01").userNm("교통안전공단 담당자")
                .ognzSeCd("KOTSA").ognzId("ORG_KOTSA").master(false)
                .roleIds(List.of("KOTSA_USER")).build();

        airlineUserOP0001 = IcasUser.builder()
                .userId("airline01").userNm("항공사A 담당자")
                .ognzSeCd("AIRLINE").ognzId("ORG_AIR01").oprtrId("OP0001").master(false)
                .roleIds(List.of("AIRLINE_USER")).build();

        airlineUserOP0002 = IcasUser.builder()
                .userId("airline02").userNm("항공사B 담당자")
                .ognzSeCd("AIRLINE").ognzId("ORG_AIR02").oprtrId("OP0002").master(false)
                .roleIds(List.of("AIRLINE_USER")).build();

        verifierUser = IcasUser.builder()
                .userId("verifier01").userNm("검증기관 담당자")
                .ognzSeCd("VERIFIER").ognzId("ORG_VRF01").vrfcnInstId("VI0001").master(false)
                .roleIds(List.of("VERIFIER_USER")).build();

        unknownUser = IcasUser.builder()
                .userId("unknown01").userNm("알 수 없는 사용자")
                .ognzSeCd("UNKNOWN").ognzId("ORG_UNK").master(false)
                .roleIds(List.of()).build();
    }

    // ── helper: EmpPlanVO 생성 ──
    private EmpPlanVO makePlan(String empPlanId, String oprtrId, String empStCd) {
        EmpPlanVO vo = new EmpPlanVO();
        vo.setEmpPlanId(empPlanId);
        vo.setOprtrId(oprtrId);
        vo.setEmpStCd(empStCd);
        vo.setEmpVer("1.0");
        vo.setRprtYr("2026");
        vo.setSigChgYn("N");
        return vo;
    }

    // ── helper: OprtrVO 생성 ──
    private OprtrVO makeOprtr(String oprtrId) {
        OprtrVO vo = new OprtrVO();
        vo.setOprtrId(oprtrId);
        vo.setOprtrNm("대한항공");
        return vo;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // searchEmpPlans — 가시범위 검증
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("목록 조회 — MOLIT 사용자는 verifierScope 미설정, oprtrId 미주입하여 전체 조회")
    void searchEmpPlans_MOLIT_전체조회() {
        EmpPlanSearch search = new EmpPlanSearch();
        given(empPlanMapper.countEmpPlans(any())).willReturn(3L);
        given(empPlanMapper.selectEmpPlans(any())).willReturn(
                List.of(makePlan("EP0001", "OP0001", "DRAFT"),
                        makePlan("EP0002", "OP0002", "SBMTD"),
                        makePlan("EP0003", "OP0003", "APRVD")));

        PageResponse<EmpPlanVO> result = empPlanService.searchEmpPlans(search, molitUser);

        assertThat(result.getTotal()).isEqualTo(3L);
        assertThat(result.getRows()).hasSize(3);
        // MOLIT — oprtrId 미주입, verifierScope 미변경
        assertThat(search.getOprtrId()).isNull();
        assertThat(search.isVerifierScope()).isFalse();
    }

    @Test
    @DisplayName("목록 조회 — AIRLINE 사용자는 본인 oprtrId 가 search 에 강제 주입됨")
    void searchEmpPlans_AIRLINE_oprtrId_자동주입() {
        EmpPlanSearch search = new EmpPlanSearch();
        given(empPlanMapper.countEmpPlans(any())).willReturn(1L);
        given(empPlanMapper.selectEmpPlans(any())).willReturn(
                List.of(makePlan("EP0001", "OP0001", "DRAFT")));

        empPlanService.searchEmpPlans(search, airlineUserOP0001);

        assertThat(search.getOprtrId()).isEqualTo("OP0001");
        assertThat(search.isVerifierScope()).isFalse();
    }

    @Test
    @DisplayName("목록 조회 — VERIFIER 사용자는 verifierScope=true + vrfcnInstId 주입됨")
    void searchEmpPlans_VERIFIER_verifierScope_주입() {
        EmpPlanSearch search = new EmpPlanSearch();
        search.setRprtYr("2026");
        given(empPlanMapper.countEmpPlans(any())).willReturn(1L);
        given(empPlanMapper.selectEmpPlans(any())).willReturn(
                List.of(makePlan("EP0001", "OP0001", "APRVD")));

        empPlanService.searchEmpPlans(search, verifierUser);

        assertThat(search.isVerifierScope()).isTrue();
        assertThat(search.getVrfcnInstId()).isEqualTo("VI0001");
    }

    @Test
    @DisplayName("목록 조회 — 알 수 없는 ognzSeCd 사용자는 FORBIDDEN 예외")
    void searchEmpPlans_권한없는사용자_FORBIDDEN() {
        EmpPlanSearch search = new EmpPlanSearch();

        assertThatThrownBy(() -> empPlanService.searchEmpPlans(search, unknownUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(403));

        then(empPlanMapper).should(never()).selectEmpPlans(any());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getEmpPlan — 단건 조회
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("단건 조회 — 존재하는 plan, MOLIT 사용자 정상 반환")
    void getEmpPlan_정상조회() {
        EmpPlanVO plan = makePlan("EP0001", "OP0001", "DRAFT");
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willDoNothing().given(dataScopeValidator)
                .assertOprtrAccessible(eq(molitUser), eq("OP0001"), eq("2026"));

        EmpPlanVO result = empPlanService.getEmpPlan("EP0001", molitUser);

        assertThat(result.getEmpPlanId()).isEqualTo("EP0001");
    }

    @Test
    @DisplayName("단건 조회 — 존재하지 않는 ID → NOT_FOUND 예외")
    void getEmpPlan_미존재_NOT_FOUND() {
        given(empPlanMapper.selectByEmpPlanId("EP9999")).willReturn(null);

        assertThatThrownBy(() -> empPlanService.getEmpPlan("EP9999", molitUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(404));
    }

    @Test
    @DisplayName("단건 조회 — AIRLINE 이 다른 운영사 plan 조회 시도 → FORBIDDEN 예외")
    void getEmpPlan_AIRLINE_타운영사조회_FORBIDDEN() {
        EmpPlanVO plan = makePlan("EP0001", "OP0002", "DRAFT"); // OP0002 소유
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willThrow(BusinessException.forbidden("본인 항공사 데이터만 접근할 수 있습니다."))
                .given(dataScopeValidator).assertOprtrAccessible(eq(airlineUserOP0001), eq("OP0002"), eq("2026"));

        assertThatThrownBy(() -> empPlanService.getEmpPlan("EP0001", airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(403));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // createEmpPlan — 신규 등록
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("신규 등록 — AIRLINE 본인 정상 등록: ID EP0001 채번, 상태 DRAFT, 이력 1건 insert")
    void createEmpPlan_AIRLINE_본인_정상등록() {
        EmpPlanVO vo = new EmpPlanVO();
        vo.setOprtrId("OP0001");
        vo.setRprtYr("2026");
        vo.setSigChgYn("N");

        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));
        given(oprtrMapper.selectByOprtrId("OP0001")).willReturn(makeOprtr("OP0001"));
        given(empPlanMapper.countByPrefix("EP")).willReturn(0);
        given(idGenerator.managementPk("EP", 1)).willReturn("EP0001");
        given(empPlanMapper.selectMaxEmpVer("OP0001")).willReturn(null);
        given(empPlanMapper.insertEmpPlan(any(EmpPlanVO.class))).willReturn(1);
        given(empChgHstryMapper.insertHstry(any(EmpChgHstryVO.class))).willReturn(1);

        EmpPlanVO created = makePlan("EP0001", "OP0001", "DRAFT");
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(created);

        EmpPlanVO result = empPlanService.createEmpPlan(vo, airlineUserOP0001);

        assertThat(result.getEmpPlanId()).isEqualTo("EP0001");
        assertThat(result.getEmpStCd()).isEqualTo("DRAFT");

        // insert 1번 확인
        then(empPlanMapper).should(times(1)).insertEmpPlan(any(EmpPlanVO.class));

        // 이력 1건 확인
        ArgumentCaptor<EmpChgHstryVO> hstryCaptor = ArgumentCaptor.forClass(EmpChgHstryVO.class);
        then(empChgHstryMapper).should(times(1)).insertHstry(hstryCaptor.capture());
        EmpChgHstryVO capturedHstry = hstryCaptor.getValue();
        assertThat(capturedHstry.getEmpPlanId()).isEqualTo("EP0001");
        assertThat(capturedHstry.getChgChptr()).isEqualTo("MASTER");
        assertThat(capturedHstry.getChgCn()).contains("신규등록");
    }

    @Test
    @DisplayName("신규 등록 — MOLIT 사용자 등록 시도 → FORBIDDEN 예외 (DataScopeValidator 통과 못함)")
    void createEmpPlan_MOLIT_등록시도_FORBIDDEN() {
        EmpPlanVO vo = new EmpPlanVO();
        vo.setOprtrId("OP0001");
        vo.setRprtYr("2026");

        willThrow(BusinessException.forbidden("항공사 사용자만 수행할 수 있는 작업입니다."))
                .given(dataScopeValidator).assertOwnAirline(eq(molitUser), eq("OP0001"));

        assertThatThrownBy(() -> empPlanService.createEmpPlan(vo, molitUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(403));

        then(empPlanMapper).should(never()).insertEmpPlan(any());
    }

    @Test
    @DisplayName("신규 등록 — 다른 운영사 oprtrId 입력 시 → FORBIDDEN 예외")
    void createEmpPlan_타운영사oprtrId_FORBIDDEN() {
        EmpPlanVO vo = new EmpPlanVO();
        vo.setOprtrId("OP0002"); // 본인(OP0001)이 아닌 타 운영사 ID
        vo.setRprtYr("2026");

        willThrow(BusinessException.forbidden("본인 항공사 데이터만 등록·수정할 수 있습니다."))
                .given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0002"));

        assertThatThrownBy(() -> empPlanService.createEmpPlan(vo, airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(403));

        then(empPlanMapper).should(never()).insertEmpPlan(any());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // updateEmpPlan — DRAFT 수정
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("수정 — DRAFT 상태 plan 정상 수정")
    void updateEmpPlan_DRAFT상태_정상수정() {
        EmpPlanVO existing = makePlan("EP0001", "OP0001", "DRAFT");
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(existing);
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));
        given(empPlanMapper.updateEmpPlan(any(EmpPlanVO.class))).willReturn(1);
        given(empChgHstryMapper.insertHstry(any(EmpChgHstryVO.class))).willReturn(1);

        EmpPlanVO updateVo = new EmpPlanVO();
        updateVo.setRprtYr("2026");
        updateVo.setRmrk("수정 비고");

        assertThatCode(() -> empPlanService.updateEmpPlan("EP0001", updateVo, airlineUserOP0001))
                .doesNotThrowAnyException();

        then(empPlanMapper).should(times(1)).updateEmpPlan(any(EmpPlanVO.class));
        then(empChgHstryMapper).should(times(1)).insertHstry(any(EmpChgHstryVO.class));
    }

    @Test
    @DisplayName("수정 — DRAFT 아닌 상태(SBMTD)에서 수정 시도 → BAD_REQUEST 예외")
    void updateEmpPlan_SBMTD상태_수정시도_CONFLICT() {
        EmpPlanVO existing = makePlan("EP0001", "OP0001", "SBMTD"); // DRAFT 아닌 상태
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(existing);
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));

        EmpPlanVO updateVo = new EmpPlanVO();
        updateVo.setRprtYr("2026");

        assertThatThrownBy(() -> empPlanService.updateEmpPlan("EP0001", updateVo, airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));

        then(empPlanMapper).should(never()).updateEmpPlan(any());
    }

    @Test
    @DisplayName("수정 — AIRLINE 이 다른 운영사 plan 수정 시도 → FORBIDDEN 예외")
    void updateEmpPlan_타운영사plan_FORBIDDEN() {
        EmpPlanVO existing = makePlan("EP0001", "OP0002", "DRAFT"); // OP0002 소유
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(existing);
        willThrow(BusinessException.forbidden("본인 항공사 데이터만 등록·수정할 수 있습니다."))
                .given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0002"));

        EmpPlanVO updateVo = new EmpPlanVO();
        updateVo.setRprtYr("2026");

        assertThatThrownBy(() -> empPlanService.updateEmpPlan("EP0001", updateVo, airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(403));

        then(empPlanMapper).should(never()).updateEmpPlan(any());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 상태 전이 — submit
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("제출 — DRAFT→SBMTD 정상 전이, 이력 STATUS 1건 기록")
    void submit_DRAFT에서_SBMTD_정상전이() {
        EmpPlanVO plan = makePlan("EP0001", "OP0001", "DRAFT");
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));
        given(empPlanMapper.updateSubmit("EP0001", "airline01")).willReturn(1);
        given(empChgHstryMapper.insertHstry(any(EmpChgHstryVO.class))).willReturn(1);

        assertThatCode(() -> empPlanService.submit("EP0001", airlineUserOP0001))
                .doesNotThrowAnyException();

        then(empPlanMapper).should().updateSubmit("EP0001", "airline01");

        ArgumentCaptor<EmpChgHstryVO> captor = ArgumentCaptor.forClass(EmpChgHstryVO.class);
        then(empChgHstryMapper).should(times(1)).insertHstry(captor.capture());
        assertThat(captor.getValue().getChgChptr()).isEqualTo("STATUS");
        assertThat(captor.getValue().getChgCn()).contains("DRAFT").contains("SBMTD");
    }

    @Test
    @DisplayName("제출 — DRAFT 아닌 상태(SBMTD)에서 submit 시도 → BAD_REQUEST 예외")
    void submit_SBMTD상태에서_submit_시도_BAD_REQUEST() {
        EmpPlanVO plan = makePlan("EP0001", "OP0001", "SBMTD");
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));

        assertThatThrownBy(() -> empPlanService.submit("EP0001", airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));

        then(empPlanMapper).should(never()).updateSubmit(any(), any());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 상태 전이 — review
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("검토 진입 — SBMTD→RVWNG 정상 전이 (KOTSA)")
    void review_SBMTD에서_RVWNG_정상전이() {
        EmpPlanVO plan = makePlan("EP0001", "OP0001", "SBMTD");
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        given(empPlanMapper.updateEmpStCd("EP0001", "RVWNG", "kotsa01")).willReturn(1);
        given(empChgHstryMapper.insertHstry(any(EmpChgHstryVO.class))).willReturn(1);

        assertThatCode(() -> empPlanService.review("EP0001", kotsaUser))
                .doesNotThrowAnyException();

        then(empPlanMapper).should().updateEmpStCd("EP0001", "RVWNG", "kotsa01");
    }

    @Test
    @DisplayName("검토 진입 — KOTSA 아닌 사용자 (AIRLINE) 시도 → FORBIDDEN 예외")
    void review_AIRLINE사용자_시도_FORBIDDEN() {
        assertThatThrownBy(() -> empPlanService.review("EP0001", airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(403));

        then(empPlanMapper).should(never()).updateEmpStCd(any(), any(), any());
    }

    @Test
    @DisplayName("검토 진입 — DRAFT 상태에서 review 시도 → BAD_REQUEST 예외 (상태 불일치)")
    void review_DRAFT상태에서_review_시도_BAD_REQUEST() {
        EmpPlanVO plan = makePlan("EP0001", "OP0001", "DRAFT"); // SBMTD 여야 함
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);

        assertThatThrownBy(() -> empPlanService.review("EP0001", kotsaUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));

        then(empPlanMapper).should(never()).updateEmpStCd(any(), any(), any());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 상태 전이 — reject
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("반려 — RVWNG→DRAFT 정상 전이 (KOTSA, 사유 포함)")
    void reject_RVWNG에서_DRAFT_정상전이() {
        EmpPlanVO plan = makePlan("EP0001", "OP0001", "RVWNG");
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        given(empPlanMapper.updateReject("EP0001", "AOC 갱신 필요", "kotsa01")).willReturn(1);
        given(empChgHstryMapper.insertHstry(any(EmpChgHstryVO.class))).willReturn(1);

        assertThatCode(() -> empPlanService.reject("EP0001", "AOC 갱신 필요", kotsaUser))
                .doesNotThrowAnyException();

        then(empPlanMapper).should().updateReject("EP0001", "AOC 갱신 필요", "kotsa01");
    }

    @Test
    @DisplayName("반려 — 사유 누락(빈 문자열) 시 BAD_REQUEST 예외")
    void reject_사유누락_BAD_REQUEST() {
        assertThatThrownBy(() -> empPlanService.reject("EP0001", "", kotsaUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));

        then(empPlanMapper).should(never()).updateReject(any(), any(), any());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 상태 전이 — approve
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("승인 — RVWNG→APRVD 정상 전이 (MOLIT), 직전 APRVD 자동 만료 expirePlan 호출 확인")
    void approve_RVWNG에서_APRVD_정상전이_직전만료확인() {
        EmpPlanVO plan = makePlan("EP0002", "OP0001", "RVWNG");
        given(empPlanMapper.selectByEmpPlanId("EP0002")).willReturn(plan);

        EmpPlanVO prevAprvd = makePlan("EP0001", "OP0001", "APRVD");
        given(empPlanMapper.selectAprvdByOprtrId("OP0001", "EP0002")).willReturn(List.of(prevAprvd));
        given(empPlanMapper.expirePlan("EP0001", "molit01")).willReturn(1);
        given(empPlanMapper.updateApprove("EP0002", "molit01")).willReturn(1);
        given(empChgHstryMapper.insertHstry(any(EmpChgHstryVO.class))).willReturn(1);

        assertThatCode(() -> empPlanService.approve("EP0002", molitUser))
                .doesNotThrowAnyException();

        // 직전 APRVD 만료 호출 확인
        then(empPlanMapper).should().expirePlan("EP0001", "molit01");
        then(empPlanMapper).should().updateApprove("EP0002", "molit01");

        // 이력은 직전만료(1건) + 승인(1건) = 최소 2건
        then(empChgHstryMapper).should(times(2)).insertHstry(any(EmpChgHstryVO.class));
    }

    @Test
    @DisplayName("승인 — MOLIT 아닌 사용자 (KOTSA) 시도 → FORBIDDEN 예외")
    void approve_KOTSA사용자_시도_FORBIDDEN() {
        assertThatThrownBy(() -> empPlanService.approve("EP0001", kotsaUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(403));

        then(empPlanMapper).should(never()).updateApprove(any(), any());
    }

    @Test
    @DisplayName("승인 — DRAFT 상태에서 approve 시도 → BAD_REQUEST 예외")
    void approve_DRAFT상태에서_approve_시도_BAD_REQUEST() {
        EmpPlanVO plan = makePlan("EP0001", "OP0001", "DRAFT"); // RVWNG/RCMDD 여야 함
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);

        assertThatThrownBy(() -> empPlanService.approve("EP0001", molitUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));

        then(empPlanMapper).should(never()).updateApprove(any(), any());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 상태 전이 — cancel
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("취소 — APRVD→CNCLD 정상 전이 (MOLIT, 사유 포함)")
    void cancel_APRVD에서_CNCLD_정상전이() {
        EmpPlanVO plan = makePlan("EP0001", "OP0001", "APRVD");
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        given(empPlanMapper.updateCancel("EP0001", "운영 종료", "molit01")).willReturn(1);
        given(empChgHstryMapper.insertHstry(any(EmpChgHstryVO.class))).willReturn(1);

        assertThatCode(() -> empPlanService.cancel("EP0001", "운영 종료", molitUser))
                .doesNotThrowAnyException();

        then(empPlanMapper).should().updateCancel("EP0001", "운영 종료", "molit01");
    }

    @Test
    @DisplayName("취소 — 사유 누락(null) 시 BAD_REQUEST 예외")
    void cancel_사유누락_BAD_REQUEST() {
        assertThatThrownBy(() -> empPlanService.cancel("EP0001", null, molitUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));

        then(empPlanMapper).should(never()).updateCancel(any(), any(), any());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // createNewVersion — 신버전 생성
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("신버전 생성 — 기존 APRVD plan 기반 정상 생성: emp_ver +1, DRAFT, prev_emp_plan_id 설정")
    void createNewVersion_APRVD기반_정상생성() {
        EmpPlanVO basePlan = makePlan("EP0001", "OP0001", "APRVD");
        basePlan.setEmpVer("1.0");
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(basePlan);
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));
        given(empPlanMapper.countInProgressByOprtrId("OP0001", "EP0001")).willReturn(0);
        given(empPlanMapper.countByPrefix("EP")).willReturn(1);
        given(idGenerator.managementPk("EP", 2)).willReturn("EP0002");
        given(empPlanMapper.selectMaxEmpVer("OP0001")).willReturn("1.0");
        given(empPlanMapper.insertEmpPlan(any(EmpPlanVO.class))).willReturn(1);
        given(empChgHstryMapper.insertHstry(any(EmpChgHstryVO.class))).willReturn(1);

        EmpPlanVO newPlan = makePlan("EP0002", "OP0001", "DRAFT");
        newPlan.setEmpVer("2.0");
        newPlan.setPrevEmpPlanId("EP0001");
        given(empPlanMapper.selectByEmpPlanId("EP0002")).willReturn(newPlan);

        EmpPlanVO result = empPlanService.createNewVersion("EP0001", airlineUserOP0001);

        assertThat(result.getEmpPlanId()).isEqualTo("EP0002");
        assertThat(result.getEmpStCd()).isEqualTo("DRAFT");
        assertThat(result.getEmpVer()).isEqualTo("2.0");
        assertThat(result.getPrevEmpPlanId()).isEqualTo("EP0001");

        // insertEmpPlan 시 prevEmpPlanId 가 EP0001 로 설정되었는지 확인
        ArgumentCaptor<EmpPlanVO> planCaptor = ArgumentCaptor.forClass(EmpPlanVO.class);
        then(empPlanMapper).should().insertEmpPlan(planCaptor.capture());
        assertThat(planCaptor.getValue().getPrevEmpPlanId()).isEqualTo("EP0001");
        assertThat(planCaptor.getValue().getEmpStCd()).isEqualTo("DRAFT");
    }

    @Test
    @DisplayName("신버전 생성 — 진행 중인 plan 이 이미 존재하면 CONFLICT 예외")
    void createNewVersion_진행중plan존재_CONFLICT() {
        EmpPlanVO basePlan = makePlan("EP0001", "OP0001", "APRVD");
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(basePlan);
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));
        given(empPlanMapper.countInProgressByOprtrId("OP0001", "EP0001")).willReturn(1); // 이미 진행 중

        assertThatThrownBy(() -> empPlanService.createNewVersion("EP0001", airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(409));

        then(empPlanMapper).should(never()).insertEmpPlan(any());
    }

    @Test
    @DisplayName("신버전 생성 — 기존 plan 이 DRAFT 상태이면 BAD_REQUEST 예외 (APRVD/CNCLD만 허용)")
    void createNewVersion_기존plan_DRAFT상태_BAD_REQUEST() {
        EmpPlanVO basePlan = makePlan("EP0001", "OP0001", "DRAFT"); // APRVD/CNCLD 여야 함
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(basePlan);
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));

        assertThatThrownBy(() -> empPlanService.createNewVersion("EP0001", airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));

        then(empPlanMapper).should(never()).insertEmpPlan(any());
    }

    @Test
    @DisplayName("신버전 생성 — AIRLINE 본인 외 사용자(OP0002) 시도 → FORBIDDEN 예외")
    void createNewVersion_타운영사_FORBIDDEN() {
        EmpPlanVO basePlan = makePlan("EP0001", "OP0001", "APRVD");
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(basePlan);
        willThrow(BusinessException.forbidden("본인 항공사 데이터만 등록·수정할 수 있습니다."))
                .given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0002), eq("OP0001"));

        assertThatThrownBy(() -> empPlanService.createNewVersion("EP0001", airlineUserOP0002))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(403));

        then(empPlanMapper).should(never()).insertEmpPlan(any());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getEmpPlanHistory — 변경 이력 조회
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("이력 조회 — 정상 조회 (최신순 확인: chgDt DESC 정렬은 DB가 보장, 반환 순서 그대로)")
    void getEmpPlanHistory_정상조회_이력목록반환() {
        EmpPlanVO plan = makePlan("EP0001", "OP0001", "APRVD");
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willDoNothing().given(dataScopeValidator)
                .assertOprtrAccessible(eq(molitUser), eq("OP0001"), eq("2026"));

        EmpChgHstryVO hstry1 = new EmpChgHstryVO();
        hstry1.setChgHstryId(2L);
        hstry1.setEmpPlanId("EP0001");
        hstry1.setChgChptr("STATUS");
        hstry1.setChgCn("{\"from\":\"RVWNG\",\"to\":\"APRVD\"}");

        EmpChgHstryVO hstry2 = new EmpChgHstryVO();
        hstry2.setChgHstryId(1L);
        hstry2.setEmpPlanId("EP0001");
        hstry2.setChgChptr("MASTER");
        hstry2.setChgCn("{\"action\":\"신규등록\"}");

        given(empChgHstryMapper.selectHstryByEmpPlanId("EP0001"))
                .willReturn(List.of(hstry1, hstry2)); // DB에서 최신순으로 이미 정렬

        List<EmpChgHstryVO> result = empPlanService.getEmpPlanHistory("EP0001", molitUser);

        assertThat(result).hasSize(2);
        // 최신 이력(ID=2)이 첫 번째
        assertThat(result.get(0).getChgHstryId()).isEqualTo(2L);
        assertThat(result.get(0).getChgChptr()).isEqualTo("STATUS");
        // 이전 이력(ID=1)이 두 번째
        assertThat(result.get(1).getChgHstryId()).isEqualTo(1L);
        assertThat(result.get(1).getChgChptr()).isEqualTo("MASTER");
    }
}
