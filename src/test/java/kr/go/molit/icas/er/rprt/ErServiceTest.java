package kr.go.molit.icas.er.rprt;

import kr.go.molit.icas.com.oprtr.OprtrMapper;
import kr.go.molit.icas.com.oprtr.domain.OprtrVO;
import kr.go.molit.icas.common.dto.PageResponse;
import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.DataScopeValidator;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.common.util.IdGenerator;
import kr.go.molit.icas.emp.plan.EmpPlanMapper;
import kr.go.molit.icas.emp.plan.domain.EmpPlanVO;
import kr.go.molit.icas.er.rprt.domain.ErSearch;
import kr.go.molit.icas.er.rprt.domain.ErVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ErService 단위 테스트 — 상태머신 + 가시범위 + EMP Plan FK 검증")
class ErServiceTest {

    @Mock ErMapper            erMapper;
    @Mock EmpPlanMapper       empPlanMapper;
    @Mock OprtrMapper         oprtrMapper;
    @Mock DataScopeValidator  dataScopeValidator;
    @Mock IdGenerator         idGenerator;

    @InjectMocks
    ErService erService;

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

    // ── helper: ErVO 상태별 fixture ──
    private ErVO makeDraftEr() {
        ErVO vo = new ErVO();
        vo.setErId("ER0001");
        vo.setOprtrId("OP0001");
        vo.setRprtYr("2026");
        vo.setErVer("1.0");
        vo.setErStCd("DRAFT");
        return vo;
    }

    private ErVO makeSbmtdEr() {
        ErVO vo = makeDraftEr();
        vo.setErStCd("SBMTD");
        return vo;
    }

    private ErVO makeRvwngEr() {
        ErVO vo = makeDraftEr();
        vo.setErStCd("RVWNG");
        return vo;
    }

    private ErVO makeRcmddEr() {
        ErVO vo = makeDraftEr();
        vo.setErStCd("RCMDD");
        return vo;
    }

    private ErVO makeApprovedEr() {
        ErVO vo = makeDraftEr();
        vo.setErStCd("APRVD");
        return vo;
    }

    // ── helper: OprtrVO fixture ──
    private OprtrVO makeOprtr(String oprtrId) {
        OprtrVO vo = new OprtrVO();
        vo.setOprtrId(oprtrId);
        vo.setOprtrNm("대한항공");
        return vo;
    }

    // ── helper: APRVD 상태 EmpPlanVO fixture ──
    private EmpPlanVO makeAprvdPlan(String empPlanId, String oprtrId) {
        EmpPlanVO vo = new EmpPlanVO();
        vo.setEmpPlanId(empPlanId);
        vo.setOprtrId(oprtrId);
        vo.setEmpStCd("APRVD");
        vo.setEmpVer("1.0");
        vo.setRprtYr("2026");
        vo.setAprvDt(LocalDateTime.of(2026, 1, 1, 0, 0));
        vo.setUseBgngDt(LocalDateTime.of(2026, 1, 1, 0, 0));
        vo.setLastChgDt(LocalDateTime.of(2026, 3, 1, 0, 0));
        return vo;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // searchErs — 가시범위 검증
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("목록 조회 — MOLIT 사용자는 oprtrId 미주입, verifierScope 미변경하여 전체 조회")
    void searchErs_MOLIT_전체조회() {
        ErSearch search = new ErSearch();
        given(erMapper.countErs(any())).willReturn(3L);
        given(erMapper.selectErs(any())).willReturn(List.of(
                makeDraftEr(), makeSbmtdEr(), makeApprovedEr()));

        PageResponse<ErVO> result = erService.searchErs(search, molitUser);

        assertThat(result.getTotal()).isEqualTo(3L);
        assertThat(result.getRows()).hasSize(3);
        assertThat(search.getOprtrId()).isNull();
        assertThat(search.isVerifierScope()).isFalse();
    }

    @Test
    @DisplayName("목록 조회 — AIRLINE 사용자는 본인 oprtrId 가 search 에 강제 주입됨")
    void searchErs_AIRLINE_oprtrId_자동주입() {
        ErSearch search = new ErSearch();
        given(erMapper.countErs(any())).willReturn(1L);
        given(erMapper.selectErs(any())).willReturn(List.of(makeDraftEr()));

        erService.searchErs(search, airlineUserOP0001);

        assertThat(search.getOprtrId()).isEqualTo("OP0001");
        assertThat(search.isVerifierScope()).isFalse();
    }

    @Test
    @DisplayName("목록 조회 — VERIFIER 사용자는 verifierScope=true + vrfcnInstId 주입됨")
    void searchErs_VERIFIER_verifierScope_주입() {
        ErSearch search = new ErSearch();
        given(erMapper.countErs(any())).willReturn(1L);
        given(erMapper.selectErs(any())).willReturn(List.of(makeApprovedEr()));

        erService.searchErs(search, verifierUser);

        assertThat(search.isVerifierScope()).isTrue();
        assertThat(search.getVrfcnInstId()).isEqualTo("VI0001");
    }

    @Test
    @DisplayName("목록 조회 — 알 수 없는 ognzSeCd 사용자는 FORBIDDEN 예외")
    void searchErs_권한없는사용자_FORBIDDEN() {
        ErSearch search = new ErSearch();

        assertThatThrownBy(() -> erService.searchErs(search, unknownUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(403));

        then(erMapper).should(never()).selectErs(any());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getEr — 단건 조회
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("단건 조회 — 존재하는 ER, MOLIT 사용자 정상 반환")
    void getEr_정상조회() {
        ErVO er = makeDraftEr();
        given(erMapper.selectByErId("ER0001")).willReturn(er);
        willDoNothing().given(dataScopeValidator)
                .assertOprtrAccessible(eq(molitUser), eq("OP0001"), eq("2026"));

        ErVO result = erService.getEr("ER0001", molitUser);

        assertThat(result.getErId()).isEqualTo("ER0001");
    }

    @Test
    @DisplayName("단건 조회 — 존재하지 않는 ID → NOT_FOUND 예외")
    void getEr_미존재_NOT_FOUND() {
        given(erMapper.selectByErId("ER9999")).willReturn(null);

        assertThatThrownBy(() -> erService.getEr("ER9999", molitUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(404));
    }

    @Test
    @DisplayName("단건 조회 — AIRLINE 이 다른 운영사 ER 조회 시도 → FORBIDDEN 예외")
    void getEr_AIRLINE_타운영사조회_FORBIDDEN() {
        ErVO er = makeDraftEr();
        er.setOprtrId("OP0002");
        given(erMapper.selectByErId("ER0001")).willReturn(er);
        willThrow(BusinessException.forbidden("본인 항공사 데이터만 접근할 수 있습니다."))
                .given(dataScopeValidator).assertOprtrAccessible(eq(airlineUserOP0001), eq("OP0002"), eq("2026"));

        assertThatThrownBy(() -> erService.getEr("ER0001", airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(403));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // createEr — 신규 등록
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("신규 등록 — AIRLINE 본인 정상 등록: ER ID 채번, 상태 DRAFT")
    void createEr_AIRLINE_본인_정상등록() {
        ErVO vo = new ErVO();
        vo.setOprtrId("OP0001");
        vo.setRprtYr("2026");

        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));
        given(oprtrMapper.selectByOprtrId("OP0001")).willReturn(makeOprtr("OP0001"));
        given(erMapper.countByPrefix("ER")).willReturn(0);
        given(idGenerator.managementPk("ER", 1)).willReturn("ER0001");
        given(erMapper.selectMaxErVer("OP0001", "2026")).willReturn(null);
        given(erMapper.insertEr(any(ErVO.class))).willReturn(1);
        given(erMapper.selectByErId("ER0001")).willReturn(makeDraftEr());

        ErVO result = erService.createEr(vo, airlineUserOP0001);

        assertThat(result.getErId()).isEqualTo("ER0001");
        assertThat(result.getErStCd()).isEqualTo("DRAFT");
        then(erMapper).should(times(1)).insertEr(any(ErVO.class));
    }

    @Test
    @DisplayName("신규 등록 — MOLIT 사용자 등록 시도 → FORBIDDEN 예외 (assertOwnAirline)")
    void createEr_MOLIT_등록시도_FORBIDDEN() {
        ErVO vo = new ErVO();
        vo.setOprtrId("OP0001");
        vo.setRprtYr("2026");

        willThrow(BusinessException.forbidden("항공사 사용자만 수행할 수 있는 작업입니다."))
                .given(dataScopeValidator).assertOwnAirline(eq(molitUser), eq("OP0001"));

        assertThatThrownBy(() -> erService.createEr(vo, molitUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(403));

        then(erMapper).should(never()).insertEr(any());
    }

    @Test
    @DisplayName("신규 등록 — 다른 oprtrId 입력 시 → FORBIDDEN 예외")
    void createEr_타운영사oprtrId_FORBIDDEN() {
        ErVO vo = new ErVO();
        vo.setOprtrId("OP0002");
        vo.setRprtYr("2026");

        willThrow(BusinessException.forbidden("본인 항공사 데이터만 등록·수정할 수 있습니다."))
                .given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0002"));

        assertThatThrownBy(() -> erService.createEr(vo, airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(403));

        then(erMapper).should(never()).insertEr(any());
    }

    @Test
    @DisplayName("신규 등록 — rprtYr 4자리 아님 → BAD_REQUEST 예외")
    void createEr_rprtYr_4자리아님_BAD_REQUEST() {
        ErVO vo = new ErVO();
        vo.setOprtrId("OP0001");
        vo.setRprtYr("26"); // 4자리 아님

        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));
        given(oprtrMapper.selectByOprtrId("OP0001")).willReturn(makeOprtr("OP0001"));

        assertThatThrownBy(() -> erService.createEr(vo, airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));

        then(erMapper).should(never()).insertEr(any());
    }

    @Test
    @DisplayName("신규 등록 — certUseYn Y/N 아님 → BAD_REQUEST 예외")
    void createEr_certUseYn_유효하지않음_BAD_REQUEST() {
        ErVO vo = new ErVO();
        vo.setOprtrId("OP0001");
        vo.setRprtYr("2026");
        vo.setCertUseYn("X"); // 유효하지 않음

        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));
        given(oprtrMapper.selectByOprtrId("OP0001")).willReturn(makeOprtr("OP0001"));

        assertThatThrownBy(() -> erService.createEr(vo, airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));

        then(erMapper).should(never()).insertEr(any());
    }

    @Test
    @DisplayName("신규 등록 — allcUseYn Y/N 아님 → BAD_REQUEST 예외")
    void createEr_allcUseYn_유효하지않음_BAD_REQUEST() {
        ErVO vo = new ErVO();
        vo.setOprtrId("OP0001");
        vo.setRprtYr("2026");
        vo.setAllcUseYn("Z"); // 유효하지 않음

        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));
        given(oprtrMapper.selectByOprtrId("OP0001")).willReturn(makeOprtr("OP0001"));

        assertThatThrownBy(() -> erService.createEr(vo, airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));

        then(erMapper).should(never()).insertEr(any());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // createEr — emp_plan_id_apld FK 검증 (핵심)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("신규 등록 — empPlanIdApld 입력 시 plan 미존재 → BAD_REQUEST 예외")
    void createEr_empPlanIdApld_plan미존재_BAD_REQUEST() {
        ErVO vo = new ErVO();
        vo.setOprtrId("OP0001");
        vo.setRprtYr("2026");
        vo.setEmpPlanIdApld("EP9999");

        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));
        given(oprtrMapper.selectByOprtrId("OP0001")).willReturn(makeOprtr("OP0001"));
        given(empPlanMapper.selectByEmpPlanId("EP9999")).willReturn(null);

        assertThatThrownBy(() -> erService.createEr(vo, airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));

        then(erMapper).should(never()).insertEr(any());
    }

    @Test
    @DisplayName("신규 등록 — empPlanIdApld 입력 시 plan 존재 + oprtrId 불일치 → BAD_REQUEST 예외")
    void createEr_empPlanIdApld_oprtrId불일치_BAD_REQUEST() {
        ErVO vo = new ErVO();
        vo.setOprtrId("OP0001");
        vo.setRprtYr("2026");
        vo.setEmpPlanIdApld("EP0001");

        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));
        given(oprtrMapper.selectByOprtrId("OP0001")).willReturn(makeOprtr("OP0001"));
        // plan 은 OP0002 소유 → oprtrId 불일치
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(makeAprvdPlan("EP0001", "OP0002"));

        assertThatThrownBy(() -> erService.createEr(vo, airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));

        then(erMapper).should(never()).insertEr(any());
    }

    @Test
    @DisplayName("신규 등록 — empPlanIdApld 입력 시 plan 존재 + DRAFT 상태 → BAD_REQUEST 예외")
    void createEr_empPlanIdApld_plan미승인_BAD_REQUEST() {
        ErVO vo = new ErVO();
        vo.setOprtrId("OP0001");
        vo.setRprtYr("2026");
        vo.setEmpPlanIdApld("EP0001");

        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));
        given(oprtrMapper.selectByOprtrId("OP0001")).willReturn(makeOprtr("OP0001"));

        EmpPlanVO draftPlan = makeAprvdPlan("EP0001", "OP0001");
        draftPlan.setEmpStCd("DRAFT"); // APRVD 아님
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(draftPlan);

        assertThatThrownBy(() -> erService.createEr(vo, airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));

        then(erMapper).should(never()).insertEr(any());
    }

    @Test
    @DisplayName("신규 등록 — empPlanIdApld 입력 시 APRVD 상태 + oprtrId 일치 → 정상, prefill 필드 verify")
    void createEr_empPlanIdApld_APRVD_정상_prefill() {
        ErVO vo = new ErVO();
        vo.setOprtrId("OP0001");
        vo.setRprtYr("2026");
        vo.setEmpPlanIdApld("EP0001");

        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));
        given(oprtrMapper.selectByOprtrId("OP0001")).willReturn(makeOprtr("OP0001"));
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(makeAprvdPlan("EP0001", "OP0001"));
        given(erMapper.countByPrefix("ER")).willReturn(0);
        given(idGenerator.managementPk("ER", 1)).willReturn("ER0001");
        given(erMapper.selectMaxErVer("OP0001", "2026")).willReturn(null);
        given(erMapper.insertEr(any(ErVO.class))).willReturn(1);
        given(erMapper.selectByErId("ER0001")).willReturn(makeDraftEr());

        erService.createEr(vo, airlineUserOP0001);

        // insertEr 호출 시 prefill 필드가 설정되었는지 ArgumentCaptor 로 검증
        ArgumentCaptor<ErVO> captor = ArgumentCaptor.forClass(ErVO.class);
        then(erMapper).should(times(1)).insertEr(captor.capture());
        ErVO captured = captor.getValue();
        assertThat(captured.getEmpVerApld()).isEqualTo("1.0");
        assertThat(captured.getEmpAprvDt()).isNotNull();
        assertThat(captured.getEmpEffDt()).isNotNull();
        assertThat(captured.getEmpUpdtDt()).isNotNull();
    }

    @Test
    @DisplayName("신규 등록 — empPlanIdApld null → 검증 skip, 정상 진행")
    void createEr_empPlanIdApld_null_검증skip() {
        ErVO vo = new ErVO();
        vo.setOprtrId("OP0001");
        vo.setRprtYr("2026");
        // empPlanIdApld 미설정(null)

        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));
        given(oprtrMapper.selectByOprtrId("OP0001")).willReturn(makeOprtr("OP0001"));
        given(erMapper.countByPrefix("ER")).willReturn(0);
        given(idGenerator.managementPk("ER", 1)).willReturn("ER0001");
        given(erMapper.selectMaxErVer("OP0001", "2026")).willReturn(null);
        given(erMapper.insertEr(any(ErVO.class))).willReturn(1);
        given(erMapper.selectByErId("ER0001")).willReturn(makeDraftEr());

        ErVO result = erService.createEr(vo, airlineUserOP0001);

        assertThat(result.getErId()).isEqualTo("ER0001");
        // empPlanMapper 는 전혀 호출되지 않아야 함
        then(empPlanMapper).should(never()).selectByEmpPlanId(any());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // updateEr — DRAFT 수정
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("수정 — DRAFT 상태 ER 정상 수정")
    void updateEr_DRAFT상태_정상수정() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeDraftEr());
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));
        given(erMapper.updateEr(any(ErVO.class))).willReturn(1);

        ErVO updateVo = new ErVO();
        updateVo.setRprtYr("2026");

        assertThatCode(() -> erService.updateEr("ER0001", updateVo, airlineUserOP0001))
                .doesNotThrowAnyException();

        then(erMapper).should(times(1)).updateEr(any(ErVO.class));
    }

    @Test
    @DisplayName("수정 — DRAFT 아닌 상태(SBMTD)에서 수정 시도 → CONFLICT 예외")
    void updateEr_SBMTD상태_수정시도_CONFLICT() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeSbmtdEr());
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));

        ErVO updateVo = new ErVO();
        updateVo.setRprtYr("2026");

        assertThatThrownBy(() -> erService.updateEr("ER0001", updateVo, airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));

        then(erMapper).should(never()).updateEr(any());
    }

    @Test
    @DisplayName("수정 — AIRLINE 이 다른 운영사 ER 수정 시도 → FORBIDDEN 예외")
    void updateEr_타운영사ER_FORBIDDEN() {
        ErVO existing = makeDraftEr();
        existing.setOprtrId("OP0002");
        given(erMapper.selectByErId("ER0001")).willReturn(existing);
        willThrow(BusinessException.forbidden("본인 항공사 데이터만 등록·수정할 수 있습니다."))
                .given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0002"));

        ErVO updateVo = new ErVO();
        updateVo.setRprtYr("2026");

        assertThatThrownBy(() -> erService.updateEr("ER0001", updateVo, airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(403));

        then(erMapper).should(never()).updateEr(any());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 상태 전이 — submit
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("제출 — AIRLINE, DRAFT→SBMTD 정상 전이")
    void submit_DRAFT에서_SBMTD_정상전이() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeDraftEr());
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));
        given(erMapper.updateSubmit("ER0001", "airline01")).willReturn(1);

        assertThatCode(() -> erService.submit("ER0001", airlineUserOP0001))
                .doesNotThrowAnyException();

        then(erMapper).should().updateSubmit("ER0001", "airline01");
    }

    @Test
    @DisplayName("제출 — KOTSA 사용자 제출 시도 → FORBIDDEN 예외 (assertOwnAirline)")
    void submit_KOTSA사용자_FORBIDDEN() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeDraftEr());
        willThrow(BusinessException.forbidden("항공사 사용자만 수행할 수 있는 작업입니다."))
                .given(dataScopeValidator).assertOwnAirline(eq(kotsaUser), eq("OP0001"));

        assertThatThrownBy(() -> erService.submit("ER0001", kotsaUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(403));

        then(erMapper).should(never()).updateSubmit(any(), any());
    }

    @Test
    @DisplayName("제출 — DRAFT 아닌 상태(SBMTD)에서 submit 시도 → BAD_REQUEST 예외")
    void submit_SBMTD상태에서_submit시도_BAD_REQUEST() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeSbmtdEr());
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));

        assertThatThrownBy(() -> erService.submit("ER0001", airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));

        then(erMapper).should(never()).updateSubmit(any(), any());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 상태 전이 — review
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("검토 진입 — KOTSA, SBMTD→RVWNG 정상 전이")
    void review_SBMTD에서_RVWNG_정상전이() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeSbmtdEr());
        given(erMapper.updateErStCd("ER0001", "RVWNG", "kotsa01")).willReturn(1);

        assertThatCode(() -> erService.review("ER0001", kotsaUser))
                .doesNotThrowAnyException();

        then(erMapper).should().updateErStCd("ER0001", "RVWNG", "kotsa01");
    }

    @Test
    @DisplayName("검토 진입 — AIRLINE 사용자 시도 → FORBIDDEN 예외")
    void review_AIRLINE사용자_FORBIDDEN() {
        assertThatThrownBy(() -> erService.review("ER0001", airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(403));

        then(erMapper).should(never()).updateErStCd(any(), any(), any());
    }

    @Test
    @DisplayName("검토 진입 — DRAFT 상태에서 review 시도 → BAD_REQUEST 예외")
    void review_DRAFT상태에서_review시도_BAD_REQUEST() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeDraftEr());

        assertThatThrownBy(() -> erService.review("ER0001", kotsaUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));

        then(erMapper).should(never()).updateErStCd(any(), any(), any());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 상태 전이 — reject
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("반려 — KOTSA, RVWNG→DRAFT 정상 전이, 사유 포함")
    void reject_RVWNG에서_DRAFT_정상전이() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeRvwngEr());
        given(erMapper.updateReject("ER0001", "AOC 갱신 필요", "kotsa01")).willReturn(1);

        assertThatCode(() -> erService.reject("ER0001", "AOC 갱신 필요", kotsaUser))
                .doesNotThrowAnyException();

        then(erMapper).should().updateReject("ER0001", "AOC 갱신 필요", "kotsa01");
    }

    @Test
    @DisplayName("반려 — 사유 누락(빈 문자열) → BAD_REQUEST 예외")
    void reject_사유누락_BAD_REQUEST() {
        assertThatThrownBy(() -> erService.reject("ER0001", "", kotsaUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));

        then(erMapper).should(never()).updateReject(any(), any(), any());
    }

    @Test
    @DisplayName("반려 — AIRLINE 사용자 시도 → FORBIDDEN 예외")
    void reject_AIRLINE사용자_FORBIDDEN() {
        assertThatThrownBy(() -> erService.reject("ER0001", "사유", airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(403));

        then(erMapper).should(never()).updateReject(any(), any(), any());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 상태 전이 — recommend
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("권고 — KOTSA, RVWNG→RCMDD 정상 전이")
    void recommend_RVWNG에서_RCMDD_정상전이() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeRvwngEr());
        given(erMapper.updateErStCd("ER0001", "RCMDD", "kotsa01")).willReturn(1);

        assertThatCode(() -> erService.recommend("ER0001", kotsaUser))
                .doesNotThrowAnyException();

        then(erMapper).should().updateErStCd("ER0001", "RCMDD", "kotsa01");
    }

    @Test
    @DisplayName("권고 — DRAFT 상태에서 recommend 시도 → BAD_REQUEST 예외")
    void recommend_DRAFT상태에서_recommend시도_BAD_REQUEST() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeDraftEr());

        assertThatThrownBy(() -> erService.recommend("ER0001", kotsaUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));

        then(erMapper).should(never()).updateErStCd(any(), any(), any());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 상태 전이 — approve
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("승인 — MOLIT, RVWNG→APRVD 정상 전이")
    void approve_RVWNG에서_APRVD_정상전이() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeRvwngEr());
        given(erMapper.updateApprove("ER0001", "molit01")).willReturn(1);

        assertThatCode(() -> erService.approve("ER0001", molitUser))
                .doesNotThrowAnyException();

        then(erMapper).should().updateApprove("ER0001", "molit01");
    }

    @Test
    @DisplayName("승인 — MOLIT, RCMDD→APRVD 정상 전이")
    void approve_RCMDD에서_APRVD_정상전이() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeRcmddEr());
        given(erMapper.updateApprove("ER0001", "molit01")).willReturn(1);

        assertThatCode(() -> erService.approve("ER0001", molitUser))
                .doesNotThrowAnyException();

        then(erMapper).should().updateApprove("ER0001", "molit01");
    }

    @Test
    @DisplayName("승인 — KOTSA 사용자 시도 → FORBIDDEN 예외")
    void approve_KOTSA사용자_FORBIDDEN() {
        assertThatThrownBy(() -> erService.approve("ER0001", kotsaUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(403));

        then(erMapper).should(never()).updateApprove(any(), any());
    }

    @Test
    @DisplayName("승인 — DRAFT 상태에서 approve 시도 → BAD_REQUEST 예외")
    void approve_DRAFT상태에서_approve시도_BAD_REQUEST() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeDraftEr());

        assertThatThrownBy(() -> erService.approve("ER0001", molitUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));

        then(erMapper).should(never()).updateApprove(any(), any());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 상태 전이 — cancel
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("취소 — MOLIT, APRVD→CNCLD 정상 전이, 사유 포함")
    void cancel_APRVD에서_CNCLD_정상전이() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeApprovedEr());
        given(erMapper.updateCancel("ER0001", "운영 종료", "molit01")).willReturn(1);

        assertThatCode(() -> erService.cancel("ER0001", "운영 종료", molitUser))
                .doesNotThrowAnyException();

        then(erMapper).should().updateCancel("ER0001", "운영 종료", "molit01");
    }

    @Test
    @DisplayName("취소 — 사유 누락(null) → BAD_REQUEST 예외")
    void cancel_사유누락_BAD_REQUEST() {
        assertThatThrownBy(() -> erService.cancel("ER0001", null, molitUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));

        then(erMapper).should(never()).updateCancel(any(), any(), any());
    }

    @Test
    @DisplayName("취소 — DRAFT 상태에서 cancel 시도 → BAD_REQUEST 예외")
    void cancel_DRAFT상태에서_cancel시도_BAD_REQUEST() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeDraftEr());

        assertThatThrownBy(() -> erService.cancel("ER0001", "사유", molitUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));

        then(erMapper).should(never()).updateCancel(any(), any(), any());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // softDeleteEr — 소프트삭제
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("소프트삭제 — DRAFT 상태 정상 삭제")
    void softDeleteEr_DRAFT상태_정상삭제() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeDraftEr());
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));
        given(erMapper.softDeleteEr("ER0001", "airline01")).willReturn(1);

        assertThatCode(() -> erService.softDeleteEr("ER0001", airlineUserOP0001))
                .doesNotThrowAnyException();

        then(erMapper).should().softDeleteEr("ER0001", "airline01");
    }

    @Test
    @DisplayName("소프트삭제 — DRAFT 아닌 상태(SBMTD) → CONFLICT 예외")
    void softDeleteEr_SBMTD상태_CONFLICT() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeSbmtdEr());
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));

        assertThatThrownBy(() -> erService.softDeleteEr("ER0001", airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));

        then(erMapper).should(never()).softDeleteEr(any(), any());
    }
}
