package kr.go.molit.icas.emp.plan.cntry;

import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.DataScopeValidator;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.emp.plan.EmpPlanMapper;
import kr.go.molit.icas.emp.plan.cntry.domain.EmpCntryPairVO;
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
@DisplayName("EmpCntryPairService 단위 테스트 — 운항 국가 쌍 CRUD + 검증")
class EmpCntryPairServiceTest {

    @Mock
    EmpCntryPairMapper empCntryPairMapper;

    @Mock
    EmpPlanMapper empPlanMapper;

    @Mock
    DataScopeValidator dataScopeValidator;

    @InjectMocks
    EmpCntryPairService empCntryPairService;

    private IcasUser airlineUserOP0001;
    private IcasUser airlineUserOP0002;

    @BeforeEach
    void setUpFixtures() {
        airlineUserOP0001 = IcasUser.builder()
                .userId("airline01").userNm("항공사A 담당자")
                .ognzSeCd("AIRLINE").ognzId("ORG_AIR01").oprtrId("OP0001").master(false)
                .roleIds(List.of("AIRLINE_USER")).build();

        airlineUserOP0002 = IcasUser.builder()
                .userId("airline02").userNm("항공사B 담당자")
                .ognzSeCd("AIRLINE").ognzId("ORG_AIR02").oprtrId("OP0002").master(false)
                .roleIds(List.of("AIRLINE_USER")).build();
    }

    // ── helpers ──

    private EmpPlanVO makeDraftPlan() {
        EmpPlanVO plan = new EmpPlanVO();
        plan.setEmpPlanId("EP0001");
        plan.setOprtrId("OP0001");
        plan.setEmpStCd("DRAFT");
        plan.setRprtYr("2026");
        return plan;
    }

    private EmpPlanVO makeSubmittedPlan() {
        EmpPlanVO plan = makeDraftPlan();
        plan.setEmpStCd("SBMTD");
        return plan;
    }

    private EmpCntryPairVO makePairVO(String dprtr, String arvl, String intlYn, String exemptCd) {
        EmpCntryPairVO vo = new EmpCntryPairVO();
        vo.setDprtrCntryCd(dprtr);
        vo.setArvlCntryCd(arvl);
        vo.setIntlYn(intlYn);
        vo.setExemptCd(exemptCd);
        return vo;
    }

    private EmpCntryPairVO makeSavedPair(int sn, String dprtr, String arvl) {
        EmpCntryPairVO vo = new EmpCntryPairVO();
        vo.setEmpPlanId("EP0001");
        vo.setPairSn(sn);
        vo.setDprtrCntryCd(dprtr);
        vo.setArvlCntryCd(arvl);
        vo.setIntlYn("Y");
        return vo;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // listByPlan
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("listByPlan — 정상: plan 존재 + 가시범위 통과 → 목록 반환 (pair_sn ASC)")
    void listByPlan_정상() {
        EmpPlanVO plan = makeDraftPlan();
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willDoNothing().given(dataScopeValidator)
                .assertOprtrAccessible(eq(airlineUserOP0001), eq("OP0001"), eq("2026"));
        given(empCntryPairMapper.selectByPlanId("EP0001"))
                .willReturn(List.of(makeSavedPair(1, "KR", "JP"), makeSavedPair(2, "KR", "US")));

        List<EmpCntryPairVO> result = empCntryPairService.listByPlan("EP0001", airlineUserOP0001);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getPairSn()).isEqualTo(1);
        assertThat(result.get(1).getDprtrCntryCd()).isEqualTo("KR");
    }

    @Test
    @DisplayName("listByPlan — 부모 plan 미존재 → NOT_FOUND(404)")
    void listByPlan_plan미존재_NOT_FOUND() {
        given(empPlanMapper.selectByEmpPlanId("EP9999")).willReturn(null);

        assertThatThrownBy(() -> empCntryPairService.listByPlan("EP9999", airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(404));

        then(empCntryPairMapper).should(never()).selectByPlanId(any());
    }

    @Test
    @DisplayName("listByPlan — 가시범위 위반 → FORBIDDEN(403)")
    void listByPlan_가시범위위반_FORBIDDEN() {
        EmpPlanVO plan = makeDraftPlan();
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willThrow(BusinessException.forbidden("본인 항공사 데이터만 접근할 수 있습니다."))
                .given(dataScopeValidator).assertOprtrAccessible(eq(airlineUserOP0002), eq("OP0001"), eq("2026"));

        assertThatThrownBy(() -> empCntryPairService.listByPlan("EP0001", airlineUserOP0002))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(403));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // addChild
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("addChild — 정상: KR→JP, intlYn=Y, sn 채번 후 insert")
    void addChild_정상() {
        EmpPlanVO plan = makeDraftPlan();
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));
        given(empCntryPairMapper.existsByCntryPair("EP0001", "KR", "JP", -1)).willReturn(0);
        given(empCntryPairMapper.selectNextSn("EP0001")).willReturn(1);
        given(empCntryPairMapper.insertEmpCntryPair(any(EmpCntryPairVO.class))).willReturn(1);
        given(empCntryPairMapper.selectOne("EP0001", 1)).willReturn(makeSavedPair(1, "KR", "JP"));

        EmpCntryPairVO vo = makePairVO("KR", "JP", "Y", null);
        EmpCntryPairVO result = empCntryPairService.addChild("EP0001", vo, airlineUserOP0001);

        assertThat(result.getPairSn()).isEqualTo(1);
        assertThat(result.getDprtrCntryCd()).isEqualTo("KR");
        assertThat(result.getArvlCntryCd()).isEqualTo("JP");

        ArgumentCaptor<EmpCntryPairVO> captor = ArgumentCaptor.forClass(EmpCntryPairVO.class);
        then(empCntryPairMapper).should(times(1)).insertEmpCntryPair(captor.capture());
        assertThat(captor.getValue().getPairSn()).isEqualTo(1);
        assertThat(captor.getValue().getFrstRegUserId()).isEqualTo("airline01");
    }

    @Test
    @DisplayName("addChild — 부모 plan DRAFT 아님 → BAD_REQUEST(400)")
    void addChild_부모plan_DRAFT아님_BAD_REQUEST() {
        EmpPlanVO plan = makeSubmittedPlan();
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));

        EmpCntryPairVO vo = makePairVO("KR", "JP", "Y", null);

        assertThatThrownBy(() -> empCntryPairService.addChild("EP0001", vo, airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));
    }

    @Test
    @DisplayName("addChild — AIRLINE 본인 외 → FORBIDDEN(403)")
    void addChild_타운영사_FORBIDDEN() {
        EmpPlanVO plan = makeDraftPlan();
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willThrow(BusinessException.forbidden("본인 항공사 데이터만 등록·수정할 수 있습니다."))
                .given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0002), eq("OP0001"));

        EmpCntryPairVO vo = makePairVO("KR", "JP", "Y", null);

        assertThatThrownBy(() -> empCntryPairService.addChild("EP0001", vo, airlineUserOP0002))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(403));
    }

    @Test
    @DisplayName("addChild — dprtr_cntry_cd 2자 아님(K) → BAD_REQUEST(400)")
    void addChild_dprtrCntryCd_2자아님_BAD_REQUEST() {
        EmpPlanVO plan = makeDraftPlan();
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));

        EmpCntryPairVO vo = makePairVO("K", "JP", "Y", null);

        assertThatThrownBy(() -> empCntryPairService.addChild("EP0001", vo, airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));
    }

    @Test
    @DisplayName("addChild — arvl_cntry_cd 2자 아님(JPN) → BAD_REQUEST(400)")
    void addChild_arvlCntryCd_2자아님_BAD_REQUEST() {
        EmpPlanVO plan = makeDraftPlan();
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));

        EmpCntryPairVO vo = makePairVO("KR", "JPN", "Y", null);

        assertThatThrownBy(() -> empCntryPairService.addChild("EP0001", vo, airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));
    }

    @Test
    @DisplayName("addChild — intl_yn=Y + 출발==도착(KR→KR) → BAD_REQUEST(400)")
    void addChild_국제선_자기참조_BAD_REQUEST() {
        EmpPlanVO plan = makeDraftPlan();
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));

        EmpCntryPairVO vo = makePairVO("KR", "KR", "Y", null);

        assertThatThrownBy(() -> empCntryPairService.addChild("EP0001", vo, airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));
    }

    @Test
    @DisplayName("addChild — intl_yn=Y + 대소문자 다른 자기참조(kr→KR) → BAD_REQUEST(400) — equalsIgnoreCase 검증")
    void addChild_국제선_대소문자다른_자기참조_BAD_REQUEST() {
        EmpPlanVO plan = makeDraftPlan();
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));

        EmpCntryPairVO vo = makePairVO("kr", "KR", "Y", null);

        assertThatThrownBy(() -> empCntryPairService.addChild("EP0001", vo, airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));
    }

    @Test
    @DisplayName("addChild — intl_yn=Y + 출발!=도착(KR→JP) → 정상 통과")
    void addChild_국제선_다른국가_정상() {
        EmpPlanVO plan = makeDraftPlan();
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));
        given(empCntryPairMapper.existsByCntryPair("EP0001", "KR", "JP", -1)).willReturn(0);
        given(empCntryPairMapper.selectNextSn("EP0001")).willReturn(1);
        given(empCntryPairMapper.insertEmpCntryPair(any())).willReturn(1);
        given(empCntryPairMapper.selectOne("EP0001", 1)).willReturn(makeSavedPair(1, "KR", "JP"));

        EmpCntryPairVO vo = makePairVO("KR", "JP", "Y", null);

        assertThatCode(() -> empCntryPairService.addChild("EP0001", vo, airlineUserOP0001))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("addChild — exempt_cd 화이트리스트 외(XXX) → BAD_REQUEST(400)")
    void addChild_exemptCd_화이트리스트외_BAD_REQUEST() {
        EmpPlanVO plan = makeDraftPlan();
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));

        EmpCntryPairVO vo = makePairVO("KR", "JP", "Y", "XXX");

        assertThatThrownBy(() -> empCntryPairService.addChild("EP0001", vo, airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));
    }

    @Test
    @DisplayName("addChild — exempt_cd null 허용 → 정상 통과")
    void addChild_exemptCd_null_허용() {
        EmpPlanVO plan = makeDraftPlan();
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));
        given(empCntryPairMapper.existsByCntryPair("EP0001", "KR", "JP", -1)).willReturn(0);
        given(empCntryPairMapper.selectNextSn("EP0001")).willReturn(1);
        given(empCntryPairMapper.insertEmpCntryPair(any())).willReturn(1);
        given(empCntryPairMapper.selectOne("EP0001", 1)).willReturn(makeSavedPair(1, "KR", "JP"));

        EmpCntryPairVO vo = makePairVO("KR", "JP", "Y", null);

        assertThatCode(() -> empCntryPairService.addChild("EP0001", vo, airlineUserOP0001))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("addChild — 같은 (dprtr, arvl) 중복 추가 → CONFLICT(409)")
    void addChild_국가쌍중복_CONFLICT() {
        EmpPlanVO plan = makeDraftPlan();
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));
        given(empCntryPairMapper.existsByCntryPair("EP0001", "KR", "JP", -1)).willReturn(1);

        EmpCntryPairVO vo = makePairVO("KR", "JP", "Y", null);

        assertThatThrownBy(() -> empCntryPairService.addChild("EP0001", vo, airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(409));

        then(empCntryPairMapper).should(never()).insertEmpCntryPair(any());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // updateChild
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("updateChild — 정상 수정")
    void updateChild_정상() {
        EmpPlanVO plan = makeDraftPlan();
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));
        given(empCntryPairMapper.selectOne("EP0001", 1)).willReturn(makeSavedPair(1, "KR", "JP"));
        given(empCntryPairMapper.existsByCntryPair("EP0001", "KR", "US", 1)).willReturn(0);
        given(empCntryPairMapper.updateEmpCntryPair(any(EmpCntryPairVO.class))).willReturn(1);

        EmpCntryPairVO vo = makePairVO("KR", "US", "Y", null);

        assertThatCode(() -> empCntryPairService.updateChild("EP0001", 1, vo, airlineUserOP0001))
                .doesNotThrowAnyException();

        then(empCntryPairMapper).should(times(1)).updateEmpCntryPair(any(EmpCntryPairVO.class));
    }

    @Test
    @DisplayName("updateChild — 자식 미존재 → NOT_FOUND(404)")
    void updateChild_자식미존재_NOT_FOUND() {
        EmpPlanVO plan = makeDraftPlan();
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));
        given(empCntryPairMapper.selectOne("EP0001", 99)).willReturn(null);

        EmpCntryPairVO vo = makePairVO("KR", "US", "Y", null);

        assertThatThrownBy(() -> empCntryPairService.updateChild("EP0001", 99, vo, airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(404));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // softDeleteChild
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("softDeleteChild — 정상 소프트삭제")
    void softDeleteChild_정상() {
        EmpPlanVO plan = makeDraftPlan();
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));
        given(empCntryPairMapper.softDeleteOne("EP0001", 1, "airline01")).willReturn(1);

        assertThatCode(() -> empCntryPairService.softDeleteChild("EP0001", 1, airlineUserOP0001))
                .doesNotThrowAnyException();

        then(empCntryPairMapper).should(times(1)).softDeleteOne("EP0001", 1, "airline01");
    }

    @Test
    @DisplayName("softDeleteChild — 자식 미존재(affected=0) → NOT_FOUND(404)")
    void softDeleteChild_자식미존재_NOT_FOUND() {
        EmpPlanVO plan = makeDraftPlan();
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));
        given(empCntryPairMapper.softDeleteOne("EP0001", 99, "airline01")).willReturn(0);

        assertThatThrownBy(() -> empCntryPairService.softDeleteChild("EP0001", 99, airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(404));
    }
}
