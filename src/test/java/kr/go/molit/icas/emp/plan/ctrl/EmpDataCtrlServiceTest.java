package kr.go.molit.icas.emp.plan.ctrl;

import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.DataScopeValidator;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.emp.plan.EmpPlanMapper;
import kr.go.molit.icas.emp.plan.ctrl.domain.EmpDataCtrlVO;
import kr.go.molit.icas.emp.plan.domain.EmpPlanVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmpDataCtrlService 단위 테스트 — 1:1 자식 조회/Upsert/소프트삭제")
class EmpDataCtrlServiceTest {

    @Mock
    EmpDataCtrlMapper empDataCtrlMapper;

    @Mock
    EmpPlanMapper empPlanMapper;

    @Mock
    DataScopeValidator dataScopeValidator;

    @InjectMocks
    EmpDataCtrlService empDataCtrlService;

    // ── 공통 fixture ──
    private IcasUser molitUser;
    private IcasUser airlineUserOP0001;
    private IcasUser airlineUserOP0002;

    @BeforeEach
    void setUpFixtures() {
        molitUser = IcasUser.builder()
                .userId("molit01").userNm("국토부 담당자")
                .ognzSeCd("MOLIT").ognzId("ORG_MOLIT").master(false)
                .roleIds(List.of("ADMIN")).build();

        airlineUserOP0001 = IcasUser.builder()
                .userId("airline01").userNm("항공사A 담당자")
                .ognzSeCd("AIRLINE").ognzId("ORG_AIR01").oprtrId("OP0001").master(false)
                .roleIds(List.of("AIRLINE_USER")).build();

        airlineUserOP0002 = IcasUser.builder()
                .userId("airline02").userNm("항공사B 담당자")
                .ognzSeCd("AIRLINE").ognzId("ORG_AIR02").oprtrId("OP0002").master(false)
                .roleIds(List.of("AIRLINE_USER")).build();
    }

    // ── helper: EmpPlanVO 생성 ──
    private EmpPlanVO makeDraftPlan(String empPlanId, String oprtrId) {
        EmpPlanVO vo = new EmpPlanVO();
        vo.setEmpPlanId(empPlanId);
        vo.setOprtrId(oprtrId);
        vo.setEmpStCd("DRAFT");
        vo.setRprtYr("2026");
        return vo;
    }

    private EmpPlanVO makePlan(String empPlanId, String oprtrId, String empStCd) {
        EmpPlanVO vo = new EmpPlanVO();
        vo.setEmpPlanId(empPlanId);
        vo.setOprtrId(oprtrId);
        vo.setEmpStCd(empStCd);
        vo.setRprtYr("2026");
        return vo;
    }

    // ── helper: 유효한 EmpDataCtrlVO 생성 ──
    private EmpDataCtrlVO validDataCtrl() {
        EmpDataCtrlVO vo = new EmpDataCtrlVO();
        vo.setFlowDesc("연료 소비 데이터 흐름 기술");
        vo.setGapThrshld5pct("Y");
        return vo;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // selectByPlanId — 조회
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("조회 — 부모 plan 존재 + 가시범위 통과 → DataCtrl 반환")
    void selectByPlanId_정상조회() {
        EmpPlanVO plan = makeDraftPlan("EP0001", "OP0001");
        EmpDataCtrlVO ctrl = new EmpDataCtrlVO();
        ctrl.setEmpPlanId("EP0001");
        ctrl.setGapThrshld5pct("Y");

        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willDoNothing().given(dataScopeValidator)
                .assertOprtrAccessible(eq(molitUser), eq("OP0001"), eq("2026"));
        given(empDataCtrlMapper.selectByPlanId("EP0001")).willReturn(ctrl);

        EmpDataCtrlVO result = empDataCtrlService.selectByPlanId("EP0001", molitUser);

        assertThat(result).isNotNull();
        assertThat(result.getEmpPlanId()).isEqualTo("EP0001");
        assertThat(result.getGapThrshld5pct()).isEqualTo("Y");
    }

    @Test
    @DisplayName("조회 — 부모 plan 미존재 → NOT_FOUND(404) 예외")
    void selectByPlanId_부모plan_미존재_NOT_FOUND() {
        given(empPlanMapper.selectByEmpPlanId("EP9999")).willReturn(null);

        assertThatThrownBy(() -> empDataCtrlService.selectByPlanId("EP9999", molitUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(404));

        then(empDataCtrlMapper).should(never()).selectByPlanId(any());
    }

    @Test
    @DisplayName("조회 — 가시범위 위반 → FORBIDDEN(403) 예외")
    void selectByPlanId_가시범위_위반_FORBIDDEN() {
        EmpPlanVO plan = makeDraftPlan("EP0001", "OP0001");
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willThrow(BusinessException.forbidden("본인 항공사 데이터만 접근할 수 있습니다."))
                .given(dataScopeValidator)
                .assertOprtrAccessible(eq(airlineUserOP0002), eq("OP0001"), eq("2026"));

        assertThatThrownBy(() -> empDataCtrlService.selectByPlanId("EP0001", airlineUserOP0002))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(403));

        then(empDataCtrlMapper).should(never()).selectByPlanId(any());
    }

    @Test
    @DisplayName("조회 — 자식 미작성 → null 반환 (정상)")
    void selectByPlanId_자식_미작성_null_반환() {
        EmpPlanVO plan = makeDraftPlan("EP0001", "OP0001");
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willDoNothing().given(dataScopeValidator)
                .assertOprtrAccessible(eq(molitUser), eq("OP0001"), eq("2026"));
        given(empDataCtrlMapper.selectByPlanId("EP0001")).willReturn(null);

        EmpDataCtrlVO result = empDataCtrlService.selectByPlanId("EP0001", molitUser);

        assertThat(result).isNull();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // upsertDataCtrl — Upsert
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Upsert — existsByPlanId=false → insertEmpDataCtrl 호출")
    void upsertDataCtrl_insert_경로() {
        EmpPlanVO plan = makeDraftPlan("EP0001", "OP0001");
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));
        given(empDataCtrlMapper.existsByPlanId("EP0001")).willReturn(false);

        EmpDataCtrlVO vo = validDataCtrl();
        empDataCtrlService.upsertDataCtrl("EP0001", vo, airlineUserOP0001);

        then(empDataCtrlMapper).should(times(1)).insertEmpDataCtrl(any(EmpDataCtrlVO.class));
        then(empDataCtrlMapper).should(never()).updateEmpDataCtrl(any());
    }

    @Test
    @DisplayName("Upsert — existsByPlanId=true → updateEmpDataCtrl 호출")
    void upsertDataCtrl_update_경로() {
        EmpPlanVO plan = makeDraftPlan("EP0001", "OP0001");
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));
        given(empDataCtrlMapper.existsByPlanId("EP0001")).willReturn(true);

        EmpDataCtrlVO vo = validDataCtrl();
        empDataCtrlService.upsertDataCtrl("EP0001", vo, airlineUserOP0001);

        then(empDataCtrlMapper).should(times(1)).updateEmpDataCtrl(any(EmpDataCtrlVO.class));
        then(empDataCtrlMapper).should(never()).insertEmpDataCtrl(any());
    }

    @Test
    @DisplayName("Upsert — 부모 plan DRAFT 아님(SBMTD) → CONFLICT(409) 예외")
    void upsertDataCtrl_부모plan_DRAFT_아님_CONFLICT() {
        EmpPlanVO plan = makePlan("EP0001", "OP0001", "SBMTD");
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));

        EmpDataCtrlVO vo = validDataCtrl();
        assertThatThrownBy(() -> empDataCtrlService.upsertDataCtrl("EP0001", vo, airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(409));

        then(empDataCtrlMapper).should(never()).insertEmpDataCtrl(any());
        then(empDataCtrlMapper).should(never()).updateEmpDataCtrl(any());
    }

    @Test
    @DisplayName("Upsert — AIRLINE 본인 외 운영사 접근 → FORBIDDEN(403) 예외")
    void upsertDataCtrl_AIRLINE_본인외_FORBIDDEN() {
        EmpPlanVO plan = makeDraftPlan("EP0001", "OP0001");
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willThrow(BusinessException.forbidden("본인 항공사 데이터만 등록·수정할 수 있습니다."))
                .given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0002), eq("OP0001"));

        EmpDataCtrlVO vo = validDataCtrl();
        assertThatThrownBy(() -> empDataCtrlService.upsertDataCtrl("EP0001", vo, airlineUserOP0002))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(403));

        then(empDataCtrlMapper).should(never()).insertEmpDataCtrl(any());
    }

    @Test
    @DisplayName("Upsert — gapThrshld5pct Y/N 외 값(X) → BAD_REQUEST(400) 예외")
    void upsertDataCtrl_gapThrshld5pct_허용외_BAD_REQUEST() {
        EmpPlanVO plan = makeDraftPlan("EP0001", "OP0001");
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));

        EmpDataCtrlVO vo = new EmpDataCtrlVO();
        vo.setGapThrshld5pct("X"); // Y/N 외 불허

        assertThatThrownBy(() -> empDataCtrlService.upsertDataCtrl("EP0001", vo, airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));
    }

    @Test
    @DisplayName("Upsert — gapThrshld5pct null 허용 → 정상 처리 (DB COALESCE 기본값 Y)")
    void upsertDataCtrl_gapThrshld5pct_null_정상() {
        EmpPlanVO plan = makeDraftPlan("EP0001", "OP0001");
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));
        given(empDataCtrlMapper.existsByPlanId("EP0001")).willReturn(false);

        EmpDataCtrlVO vo = new EmpDataCtrlVO();
        vo.setGapThrshld5pct(null); // null 허용 — DB가 COALESCE 로 Y 처리

        assertThatCode(() -> empDataCtrlService.upsertDataCtrl("EP0001", vo, airlineUserOP0001))
                .doesNotThrowAnyException();

        then(empDataCtrlMapper).should(times(1)).insertEmpDataCtrl(any());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // softDeleteByPlanId — 소프트삭제
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("소프트삭제 — DRAFT plan 정상 삭제")
    void softDeleteByPlanId_정상삭제() {
        EmpPlanVO plan = makeDraftPlan("EP0001", "OP0001");
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));

        assertThatCode(() -> empDataCtrlService.softDeleteByPlanId("EP0001", airlineUserOP0001))
                .doesNotThrowAnyException();

        then(empDataCtrlMapper).should(times(1)).softDeleteByPlanId(eq("EP0001"), eq("airline01"));
    }

    @Test
    @DisplayName("소프트삭제 — 부모 plan DRAFT 아님(APRVD) → CONFLICT(409) 예외")
    void softDeleteByPlanId_부모plan_DRAFT_아님_CONFLICT() {
        EmpPlanVO plan = makePlan("EP0001", "OP0001", "APRVD");
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));

        assertThatThrownBy(() -> empDataCtrlService.softDeleteByPlanId("EP0001", airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(409));

        then(empDataCtrlMapper).should(never()).softDeleteByPlanId(any(), any());
    }
}
