package kr.go.molit.icas.emp.plan.co2detail;

import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.DataScopeValidator;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.emp.plan.EmpPlanMapper;
import kr.go.molit.icas.emp.plan.co2detail.domain.EmpCo2DetailVO;
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
@DisplayName("EmpCo2DetailService 단위 테스트 — CO2 측정 상세 CRUD + 검증")
class EmpCo2DetailServiceTest {

    @Mock
    EmpCo2DetailMapper empCo2DetailMapper;

    @Mock
    EmpPlanMapper empPlanMapper;

    @Mock
    DataScopeValidator dataScopeValidator;

    @InjectMocks
    EmpCo2DetailService empCo2DetailService;

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

    private EmpCo2DetailVO makeDetailVO(String mntrMthdCd) {
        EmpCo2DetailVO vo = new EmpCo2DetailVO();
        vo.setMntrMthdCd(mntrMthdCd);
        vo.setMsrTmingDesc("측정 시점 설명");
        return vo;
    }

    private EmpCo2DetailVO makeSavedDetail(String mntrMthdCd) {
        EmpCo2DetailVO vo = new EmpCo2DetailVO();
        vo.setEmpPlanId("EP0001");
        vo.setMntrMthdCd(mntrMthdCd);
        vo.setMsrTmingDesc("측정 시점 설명");
        return vo;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // listByPlan
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("listByPlan — 정상: plan 존재 + 가시범위 통과 → 목록 반환")
    void listByPlan_정상() {
        EmpPlanVO plan = makeDraftPlan();
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willDoNothing().given(dataScopeValidator)
                .assertOprtrAccessible(eq(airlineUserOP0001), eq("OP0001"), eq("2026"));
        given(empCo2DetailMapper.selectByPlanId("EP0001"))
                .willReturn(List.of(makeSavedDetail("MTHD_A"), makeSavedDetail("MTHD_B")));

        List<EmpCo2DetailVO> result = empCo2DetailService.listByPlan("EP0001", airlineUserOP0001);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getMntrMthdCd()).isEqualTo("MTHD_A");
    }

    @Test
    @DisplayName("listByPlan — 부모 plan 미존재 → NOT_FOUND(404)")
    void listByPlan_plan미존재_NOT_FOUND() {
        given(empPlanMapper.selectByEmpPlanId("EP9999")).willReturn(null);

        assertThatThrownBy(() -> empCo2DetailService.listByPlan("EP9999", airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(404));

        then(empCo2DetailMapper).should(never()).selectByPlanId(any());
    }

    @Test
    @DisplayName("listByPlan — 가시범위 위반 → FORBIDDEN(403)")
    void listByPlan_가시범위위반_FORBIDDEN() {
        EmpPlanVO plan = makeDraftPlan();
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willThrow(BusinessException.forbidden("본인 항공사 데이터만 접근할 수 있습니다."))
                .given(dataScopeValidator).assertOprtrAccessible(eq(airlineUserOP0002), eq("OP0001"), eq("2026"));

        assertThatThrownBy(() -> empCo2DetailService.listByPlan("EP0001", airlineUserOP0002))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(403));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // addChild
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("addChild — 정상: MTHD_A 신규 등록, insert 1회 호출 + 반환 VO 검증")
    void addChild_정상() {
        EmpPlanVO plan = makeDraftPlan();
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));
        given(empCo2DetailMapper.existsByMethod("EP0001", "MTHD_A")).willReturn(0);
        given(empCo2DetailMapper.insertEmpCo2Detail(any(EmpCo2DetailVO.class))).willReturn(1);
        given(empCo2DetailMapper.selectOne("EP0001", "MTHD_A")).willReturn(makeSavedDetail("MTHD_A"));

        EmpCo2DetailVO vo = makeDetailVO("MTHD_A");
        EmpCo2DetailVO result = empCo2DetailService.addChild("EP0001", vo, airlineUserOP0001);

        assertThat(result.getMntrMthdCd()).isEqualTo("MTHD_A");

        ArgumentCaptor<EmpCo2DetailVO> captor = ArgumentCaptor.forClass(EmpCo2DetailVO.class);
        then(empCo2DetailMapper).should(times(1)).insertEmpCo2Detail(captor.capture());
        assertThat(captor.getValue().getEmpPlanId()).isEqualTo("EP0001");
        assertThat(captor.getValue().getFrstRegUserId()).isEqualTo("airline01");
    }

    @Test
    @DisplayName("addChild — 부모 plan DRAFT 아님 → BAD_REQUEST(400)")
    void addChild_부모plan_DRAFT아님_BAD_REQUEST() {
        EmpPlanVO plan = makeSubmittedPlan();
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));

        EmpCo2DetailVO vo = makeDetailVO("MTHD_A");

        assertThatThrownBy(() -> empCo2DetailService.addChild("EP0001", vo, airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));

        then(empCo2DetailMapper).should(never()).insertEmpCo2Detail(any());
    }

    @Test
    @DisplayName("addChild — AIRLINE 본인 외 → FORBIDDEN(403)")
    void addChild_타운영사_FORBIDDEN() {
        EmpPlanVO plan = makeDraftPlan();
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willThrow(BusinessException.forbidden("본인 항공사 데이터만 등록·수정할 수 있습니다."))
                .given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0002), eq("OP0001"));

        EmpCo2DetailVO vo = makeDetailVO("MTHD_A");

        assertThatThrownBy(() -> empCo2DetailService.addChild("EP0001", vo, airlineUserOP0002))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(403));
    }

    @Test
    @DisplayName("addChild — mntr_mthd_cd 화이트리스트 외(INVALID_CD) → BAD_REQUEST(400)")
    void addChild_mntrMthdCd_화이트리스트외_BAD_REQUEST() {
        EmpPlanVO plan = makeDraftPlan();
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));

        EmpCo2DetailVO vo = makeDetailVO("INVALID_CD");

        assertThatThrownBy(() -> empCo2DetailService.addChild("EP0001", vo, airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));
    }

    @Test
    @DisplayName("addChild — 같은 plan에 MTHD_A 중복 등록 → CONFLICT(409)")
    void addChild_mntrMthdCd_중복_CONFLICT() {
        EmpPlanVO plan = makeDraftPlan();
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));
        given(empCo2DetailMapper.existsByMethod("EP0001", "MTHD_A")).willReturn(1);

        EmpCo2DetailVO vo = makeDetailVO("MTHD_A");

        assertThatThrownBy(() -> empCo2DetailService.addChild("EP0001", vo, airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(409));

        then(empCo2DetailMapper).should(never()).insertEmpCo2Detail(any());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // updateChild (PK = mntrMthdCd)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("updateChild — 정상 수정: MTHD_A 서술 컬럼 업데이트")
    void updateChild_정상() {
        EmpPlanVO plan = makeDraftPlan();
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));
        given(empCo2DetailMapper.selectOne("EP0001", "MTHD_A")).willReturn(makeSavedDetail("MTHD_A"));
        given(empCo2DetailMapper.updateEmpCo2Detail(any(EmpCo2DetailVO.class))).willReturn(1);

        EmpCo2DetailVO vo = new EmpCo2DetailVO();
        vo.setMsrTmingDesc("수정된 시점 설명");
        vo.setMsrDeviceDesc("수정된 장치 설명");

        assertThatCode(() -> empCo2DetailService.updateChild("EP0001", "MTHD_A", vo, airlineUserOP0001))
                .doesNotThrowAnyException();

        then(empCo2DetailMapper).should(times(1)).updateEmpCo2Detail(any(EmpCo2DetailVO.class));
    }

    @Test
    @DisplayName("updateChild — 자식 미존재(mntrMthdCd=BLOCK_ON_OFF) → NOT_FOUND(404)")
    void updateChild_자식미존재_NOT_FOUND() {
        EmpPlanVO plan = makeDraftPlan();
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));
        given(empCo2DetailMapper.selectOne("EP0001", "BLOCK_ON_OFF")).willReturn(null);

        EmpCo2DetailVO vo = new EmpCo2DetailVO();
        vo.setMsrTmingDesc("설명");

        assertThatThrownBy(() -> empCo2DetailService.updateChild("EP0001", "BLOCK_ON_OFF", vo, airlineUserOP0001))
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
        given(empCo2DetailMapper.softDeleteOne("EP0001", "MTHD_A", "airline01")).willReturn(1);

        assertThatCode(() -> empCo2DetailService.softDeleteChild("EP0001", "MTHD_A", airlineUserOP0001))
                .doesNotThrowAnyException();

        then(empCo2DetailMapper).should(times(1)).softDeleteOne("EP0001", "MTHD_A", "airline01");
    }

    @Test
    @DisplayName("softDeleteChild — 자식 미존재(affected=0) → NOT_FOUND(404)")
    void softDeleteChild_자식미존재_NOT_FOUND() {
        EmpPlanVO plan = makeDraftPlan();
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));
        given(empCo2DetailMapper.softDeleteOne("EP0001", "REFUEL", "airline01")).willReturn(0);

        assertThatThrownBy(() -> empCo2DetailService.softDeleteChild("EP0001", "REFUEL", airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(404));
    }
}
