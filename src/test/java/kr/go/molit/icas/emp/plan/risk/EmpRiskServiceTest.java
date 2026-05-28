package kr.go.molit.icas.emp.plan.risk;

import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.DataScopeValidator;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.emp.plan.EmpPlanMapper;
import kr.go.molit.icas.emp.plan.domain.EmpPlanVO;
import kr.go.molit.icas.emp.plan.risk.domain.EmpRiskVO;
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
@DisplayName("EmpRiskService 단위 테스트 — 위험·통제 항목 CRUD + 검증")
class EmpRiskServiceTest {

    @Mock
    EmpRiskMapper empRiskMapper;

    @Mock
    EmpPlanMapper empPlanMapper;

    @Mock
    DataScopeValidator dataScopeValidator;

    @InjectMocks
    EmpRiskService empRiskService;

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

    private EmpRiskVO makeRiskVO(String riskDesc, String ctrlActv) {
        EmpRiskVO vo = new EmpRiskVO();
        vo.setRiskDesc(riskDesc);
        vo.setCtrlActv(ctrlActv);
        return vo;
    }

    private EmpRiskVO makeSavedRisk(int sn) {
        EmpRiskVO vo = new EmpRiskVO();
        vo.setEmpPlanId("EP0001");
        vo.setRiskSn(sn);
        vo.setRiskDesc("데이터 누락 위험");
        vo.setCtrlActv("이중 점검 실시");
        return vo;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // listByPlan
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("listByPlan — 정상: plan 존재 + 가시범위 통과 → 목록 반환 (risk_sn ASC)")
    void listByPlan_정상() {
        EmpPlanVO plan = makeDraftPlan();
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willDoNothing().given(dataScopeValidator)
                .assertOprtrAccessible(eq(airlineUserOP0001), eq("OP0001"), eq("2026"));
        given(empRiskMapper.selectByPlanId("EP0001"))
                .willReturn(List.of(makeSavedRisk(1), makeSavedRisk(2)));

        List<EmpRiskVO> result = empRiskService.listByPlan("EP0001", airlineUserOP0001);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getRiskSn()).isEqualTo(1);
        assertThat(result.get(1).getRiskSn()).isEqualTo(2);
    }

    @Test
    @DisplayName("listByPlan — 부모 plan 미존재 → NOT_FOUND(404)")
    void listByPlan_plan미존재_NOT_FOUND() {
        given(empPlanMapper.selectByEmpPlanId("EP9999")).willReturn(null);

        assertThatThrownBy(() -> empRiskService.listByPlan("EP9999", airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(404));

        then(empRiskMapper).should(never()).selectByPlanId(any());
    }

    @Test
    @DisplayName("listByPlan — 가시범위 위반 → FORBIDDEN(403)")
    void listByPlan_가시범위위반_FORBIDDEN() {
        EmpPlanVO plan = makeDraftPlan();
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willThrow(BusinessException.forbidden("본인 항공사 데이터만 접근할 수 있습니다."))
                .given(dataScopeValidator).assertOprtrAccessible(eq(airlineUserOP0002), eq("OP0001"), eq("2026"));

        assertThatThrownBy(() -> empRiskService.listByPlan("EP0001", airlineUserOP0002))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(403));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // addChild
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("addChild — 정상: sn 채번 후 insert, 반환 VO riskSn 검증")
    void addChild_정상() {
        EmpPlanVO plan = makeDraftPlan();
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));
        given(empRiskMapper.selectNextSn("EP0001")).willReturn(1);
        given(empRiskMapper.insertEmpRisk(any(EmpRiskVO.class))).willReturn(1);
        given(empRiskMapper.selectOne("EP0001", 1)).willReturn(makeSavedRisk(1));

        EmpRiskVO vo = makeRiskVO("데이터 누락 위험", "이중 점검 실시");
        EmpRiskVO result = empRiskService.addChild("EP0001", vo, airlineUserOP0001);

        assertThat(result.getRiskSn()).isEqualTo(1);
        assertThat(result.getRiskDesc()).isEqualTo("데이터 누락 위험");

        ArgumentCaptor<EmpRiskVO> captor = ArgumentCaptor.forClass(EmpRiskVO.class);
        then(empRiskMapper).should(times(1)).insertEmpRisk(captor.capture());
        assertThat(captor.getValue().getRiskSn()).isEqualTo(1);
        assertThat(captor.getValue().getFrstRegUserId()).isEqualTo("airline01");
    }

    @Test
    @DisplayName("addChild — 부모 plan DRAFT 아님(SBMTD) → BAD_REQUEST(400)")
    void addChild_부모plan_DRAFT아님_BAD_REQUEST() {
        EmpPlanVO plan = makeSubmittedPlan();
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));

        EmpRiskVO vo = makeRiskVO("위험 설명", null);

        assertThatThrownBy(() -> empRiskService.addChild("EP0001", vo, airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));

        then(empRiskMapper).should(never()).insertEmpRisk(any());
    }

    @Test
    @DisplayName("addChild — AIRLINE 본인 외 → FORBIDDEN(403)")
    void addChild_타운영사_FORBIDDEN() {
        EmpPlanVO plan = makeDraftPlan();
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willThrow(BusinessException.forbidden("본인 항공사 데이터만 등록·수정할 수 있습니다."))
                .given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0002), eq("OP0001"));

        EmpRiskVO vo = makeRiskVO("위험 설명", null);

        assertThatThrownBy(() -> empRiskService.addChild("EP0001", vo, airlineUserOP0002))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(403));
    }

    @Test
    @DisplayName("addChild — risk_desc null → BAD_REQUEST(400)")
    void addChild_riskDesc_null_BAD_REQUEST() {
        EmpPlanVO plan = makeDraftPlan();
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));

        EmpRiskVO vo = makeRiskVO(null, null);

        assertThatThrownBy(() -> empRiskService.addChild("EP0001", vo, airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));
    }

    @Test
    @DisplayName("addChild — risk_desc 공백 문자열 → BAD_REQUEST(400)")
    void addChild_riskDesc_blank_BAD_REQUEST() {
        EmpPlanVO plan = makeDraftPlan();
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));

        EmpRiskVO vo = makeRiskVO("   ", null);

        assertThatThrownBy(() -> empRiskService.addChild("EP0001", vo, airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));
    }

    @Test
    @DisplayName("addChild — risk_desc 2001자 초과 → BAD_REQUEST(400)")
    void addChild_riskDesc_2001자초과_BAD_REQUEST() {
        EmpPlanVO plan = makeDraftPlan();
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));

        String tooLong = "A".repeat(2001);
        EmpRiskVO vo = makeRiskVO(tooLong, null);

        assertThatThrownBy(() -> empRiskService.addChild("EP0001", vo, airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));
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
        given(empRiskMapper.selectOne("EP0001", 1)).willReturn(makeSavedRisk(1));
        given(empRiskMapper.updateEmpRisk(any(EmpRiskVO.class))).willReturn(1);

        EmpRiskVO vo = makeRiskVO("수정된 위험 설명", "수정된 통제 활동");

        assertThatCode(() -> empRiskService.updateChild("EP0001", 1, vo, airlineUserOP0001))
                .doesNotThrowAnyException();

        then(empRiskMapper).should(times(1)).updateEmpRisk(any(EmpRiskVO.class));
    }

    @Test
    @DisplayName("updateChild — 자식 미존재 → NOT_FOUND(404)")
    void updateChild_자식미존재_NOT_FOUND() {
        EmpPlanVO plan = makeDraftPlan();
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));
        given(empRiskMapper.selectOne("EP0001", 99)).willReturn(null);

        EmpRiskVO vo = makeRiskVO("위험 설명", null);

        assertThatThrownBy(() -> empRiskService.updateChild("EP0001", 99, vo, airlineUserOP0001))
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
        given(empRiskMapper.softDeleteOne("EP0001", 1, "airline01")).willReturn(1);

        assertThatCode(() -> empRiskService.softDeleteChild("EP0001", 1, airlineUserOP0001))
                .doesNotThrowAnyException();

        then(empRiskMapper).should(times(1)).softDeleteOne("EP0001", 1, "airline01");
    }

    @Test
    @DisplayName("softDeleteChild — 자식 미존재(affected=0) → NOT_FOUND(404)")
    void softDeleteChild_자식미존재_NOT_FOUND() {
        EmpPlanVO plan = makeDraftPlan();
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));
        given(empRiskMapper.softDeleteOne("EP0001", 99, "airline01")).willReturn(0);

        assertThatThrownBy(() -> empRiskService.softDeleteChild("EP0001", 99, airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(404));
    }
}
