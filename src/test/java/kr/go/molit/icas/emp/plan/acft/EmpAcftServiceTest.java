package kr.go.molit.icas.emp.plan.acft;

import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.DataScopeValidator;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.emp.plan.EmpPlanMapper;
import kr.go.molit.icas.emp.plan.acft.domain.EmpAcftVO;
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
@DisplayName("EmpAcftService 단위 테스트 — 항공기 CRUD + 검증")
class EmpAcftServiceTest {

    @Mock
    EmpAcftMapper empAcftMapper;

    @Mock
    EmpPlanMapper empPlanMapper;

    @Mock
    DataScopeValidator dataScopeValidator;

    @InjectMocks
    EmpAcftService empAcftService;

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

    private EmpAcftVO makeAcftVO(String acftTypeCd, String fuelTypeCd, int acftCnt) {
        EmpAcftVO vo = new EmpAcftVO();
        vo.setAcftTypeCd(acftTypeCd);
        vo.setFuelTypeCd(fuelTypeCd);
        vo.setAcftCnt(acftCnt);
        return vo;
    }

    private EmpAcftVO makeSavedAcft(int sn) {
        EmpAcftVO vo = new EmpAcftVO();
        vo.setEmpPlanId("EP0001");
        vo.setAcftSn(sn);
        vo.setAcftTypeCd("B737");
        vo.setFuelTypeCd("JET_A");
        vo.setAcftCnt(5);
        return vo;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // listByPlan
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("listByPlan — 정상: plan 존재 + 가시범위 통과 → 목록 반환 (acft_sn ASC)")
    void listByPlan_정상() {
        EmpPlanVO plan = makeDraftPlan();
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willDoNothing().given(dataScopeValidator)
                .assertOprtrAccessible(eq(airlineUserOP0001), eq("OP0001"), eq("2026"));
        given(empAcftMapper.selectByPlanId("EP0001"))
                .willReturn(List.of(makeSavedAcft(1), makeSavedAcft(2)));

        List<EmpAcftVO> result = empAcftService.listByPlan("EP0001", airlineUserOP0001);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getAcftSn()).isEqualTo(1);
        assertThat(result.get(1).getAcftSn()).isEqualTo(2);
    }

    @Test
    @DisplayName("listByPlan — 부모 plan 미존재 → NOT_FOUND(404)")
    void listByPlan_plan미존재_NOT_FOUND() {
        given(empPlanMapper.selectByEmpPlanId("EP9999")).willReturn(null);

        assertThatThrownBy(() -> empAcftService.listByPlan("EP9999", airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(404));

        then(empAcftMapper).should(never()).selectByPlanId(any());
    }

    @Test
    @DisplayName("listByPlan — 가시범위 위반 → FORBIDDEN(403)")
    void listByPlan_가시범위위반_FORBIDDEN() {
        EmpPlanVO plan = makeDraftPlan();
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willThrow(BusinessException.forbidden("본인 항공사 데이터만 접근할 수 있습니다."))
                .given(dataScopeValidator).assertOprtrAccessible(eq(airlineUserOP0002), eq("OP0001"), eq("2026"));

        assertThatThrownBy(() -> empAcftService.listByPlan("EP0001", airlineUserOP0002))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(403));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // addChild
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("addChild — 정상: sn 채번 후 insert, 반환 VO acftSn 검증")
    void addChild_정상() {
        EmpPlanVO plan = makeDraftPlan();
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));
        given(empAcftMapper.selectNextSn("EP0001")).willReturn(1);
        given(empAcftMapper.insertEmpAcft(any(EmpAcftVO.class))).willReturn(1);
        given(empAcftMapper.selectOne("EP0001", 1)).willReturn(makeSavedAcft(1));

        EmpAcftVO vo = makeAcftVO("B737", "JET_A", 5);

        EmpAcftVO result = empAcftService.addChild("EP0001", vo, airlineUserOP0001);

        assertThat(result.getAcftSn()).isEqualTo(1);

        ArgumentCaptor<EmpAcftVO> captor = ArgumentCaptor.forClass(EmpAcftVO.class);
        then(empAcftMapper).should(times(1)).insertEmpAcft(captor.capture());
        assertThat(captor.getValue().getAcftSn()).isEqualTo(1);
        assertThat(captor.getValue().getFrstRegUserId()).isEqualTo("airline01");
    }

    @Test
    @DisplayName("addChild — 부모 plan DRAFT 아님(SBMTD) → BAD_REQUEST(400)")
    void addChild_부모plan_DRAFT아님_BAD_REQUEST() {
        EmpPlanVO plan = makeSubmittedPlan();
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));

        EmpAcftVO vo = makeAcftVO("B737", "JET_A", 5);

        assertThatThrownBy(() -> empAcftService.addChild("EP0001", vo, airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));

        then(empAcftMapper).should(never()).insertEmpAcft(any());
    }

    @Test
    @DisplayName("addChild — AIRLINE 본인 외 운영사 → FORBIDDEN(403)")
    void addChild_타운영사_FORBIDDEN() {
        EmpPlanVO plan = makeDraftPlan();
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willThrow(BusinessException.forbidden("본인 항공사 데이터만 등록·수정할 수 있습니다."))
                .given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0002), eq("OP0001"));

        EmpAcftVO vo = makeAcftVO("B737", "JET_A", 5);

        assertThatThrownBy(() -> empAcftService.addChild("EP0001", vo, airlineUserOP0002))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(403));
    }

    @Test
    @DisplayName("addChild — acft_cnt 0 → BAD_REQUEST(400)")
    void addChild_acftCnt_0_BAD_REQUEST() {
        EmpPlanVO plan = makeDraftPlan();
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));

        EmpAcftVO vo = makeAcftVO("B737", "JET_A", 0);

        assertThatThrownBy(() -> empAcftService.addChild("EP0001", vo, airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));
    }

    @Test
    @DisplayName("addChild — acft_cnt 음수(-1) → BAD_REQUEST(400)")
    void addChild_acftCnt_음수_BAD_REQUEST() {
        EmpPlanVO plan = makeDraftPlan();
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));

        EmpAcftVO vo = makeAcftVO("B737", "JET_A", -1);

        assertThatThrownBy(() -> empAcftService.addChild("EP0001", vo, airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));
    }

    @Test
    @DisplayName("addChild — acft_type_cd null → BAD_REQUEST(400)")
    void addChild_acftTypeCd_null_BAD_REQUEST() {
        EmpPlanVO plan = makeDraftPlan();
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));

        EmpAcftVO vo = makeAcftVO(null, "JET_A", 3);

        assertThatThrownBy(() -> empAcftService.addChild("EP0001", vo, airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));
    }

    @Test
    @DisplayName("addChild — fuel_type_cd null → BAD_REQUEST(400)")
    void addChild_fuelTypeCd_null_BAD_REQUEST() {
        EmpPlanVO plan = makeDraftPlan();
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));

        EmpAcftVO vo = makeAcftVO("B737", null, 3);

        assertThatThrownBy(() -> empAcftService.addChild("EP0001", vo, airlineUserOP0001))
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
        given(empAcftMapper.selectOne("EP0001", 1)).willReturn(makeSavedAcft(1));
        given(empAcftMapper.updateEmpAcft(any(EmpAcftVO.class))).willReturn(1);

        EmpAcftVO vo = makeAcftVO("A320", "JET_A", 3);

        assertThatCode(() -> empAcftService.updateChild("EP0001", 1, vo, airlineUserOP0001))
                .doesNotThrowAnyException();

        then(empAcftMapper).should(times(1)).updateEmpAcft(any(EmpAcftVO.class));
    }

    @Test
    @DisplayName("updateChild — 자식 미존재 → NOT_FOUND(404)")
    void updateChild_자식미존재_NOT_FOUND() {
        EmpPlanVO plan = makeDraftPlan();
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));
        given(empAcftMapper.selectOne("EP0001", 99)).willReturn(null);

        EmpAcftVO vo = makeAcftVO("A320", "JET_A", 3);

        assertThatThrownBy(() -> empAcftService.updateChild("EP0001", 99, vo, airlineUserOP0001))
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
        given(empAcftMapper.softDeleteOne("EP0001", 1, "airline01")).willReturn(1);

        assertThatCode(() -> empAcftService.softDeleteChild("EP0001", 1, airlineUserOP0001))
                .doesNotThrowAnyException();

        then(empAcftMapper).should(times(1)).softDeleteOne("EP0001", 1, "airline01");
    }

    @Test
    @DisplayName("softDeleteChild — 자식 미존재(affected=0) → NOT_FOUND(404)")
    void softDeleteChild_자식미존재_NOT_FOUND() {
        EmpPlanVO plan = makeDraftPlan();
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));
        given(empAcftMapper.softDeleteOne("EP0001", 99, "airline01")).willReturn(0);

        assertThatThrownBy(() -> empAcftService.softDeleteChild("EP0001", 99, airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(404));
    }
}
