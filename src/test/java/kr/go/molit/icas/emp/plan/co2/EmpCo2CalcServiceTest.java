package kr.go.molit.icas.emp.plan.co2;

import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.DataScopeValidator;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.emp.plan.EmpPlanMapper;
import kr.go.molit.icas.emp.plan.co2.domain.EmpCo2CalcVO;
import kr.go.molit.icas.emp.plan.domain.EmpPlanVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmpCo2CalcService 단위 테스트 — 1:1 자식 조회/Upsert/소프트삭제")
class EmpCo2CalcServiceTest {

    @Mock
    EmpCo2CalcMapper empCo2CalcMapper;

    @Mock
    EmpPlanMapper empPlanMapper;

    @Mock
    DataScopeValidator dataScopeValidator;

    @InjectMocks
    EmpCo2CalcService empCo2CalcService;

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

    // ── helper: 유효한 EmpCo2CalcVO 생성 ──
    private EmpCo2CalcVO validCo2Calc() {
        EmpCo2CalcVO vo = new EmpCo2CalcVO();
        vo.setMntrMthdCd("MTHD_A");
        vo.setFuelDnstySecd("ACTUAL");
        return vo;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // selectByPlanId — 조회
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("조회 — 부모 plan 존재 + 가시범위 통과 → Co2Calc 반환")
    void selectByPlanId_정상조회() {
        EmpPlanVO plan = makeDraftPlan("EP0001", "OP0001");
        EmpCo2CalcVO co2 = new EmpCo2CalcVO();
        co2.setEmpPlanId("EP0001");
        co2.setMntrMthdCd("MTHD_A");

        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willDoNothing().given(dataScopeValidator)
                .assertOprtrAccessible(eq(molitUser), eq("OP0001"), eq("2026"));
        given(empCo2CalcMapper.selectByPlanId("EP0001")).willReturn(co2);

        EmpCo2CalcVO result = empCo2CalcService.selectByPlanId("EP0001", molitUser);

        assertThat(result).isNotNull();
        assertThat(result.getEmpPlanId()).isEqualTo("EP0001");
        assertThat(result.getMntrMthdCd()).isEqualTo("MTHD_A");
    }

    @Test
    @DisplayName("조회 — 부모 plan 미존재 → NOT_FOUND(404) 예외")
    void selectByPlanId_부모plan_미존재_NOT_FOUND() {
        given(empPlanMapper.selectByEmpPlanId("EP9999")).willReturn(null);

        assertThatThrownBy(() -> empCo2CalcService.selectByPlanId("EP9999", molitUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(404));

        then(empCo2CalcMapper).should(never()).selectByPlanId(any());
    }

    @Test
    @DisplayName("조회 — 가시범위 위반 → FORBIDDEN(403) 예외")
    void selectByPlanId_가시범위_위반_FORBIDDEN() {
        EmpPlanVO plan = makeDraftPlan("EP0001", "OP0001");
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willThrow(BusinessException.forbidden("본인 항공사 데이터만 접근할 수 있습니다."))
                .given(dataScopeValidator)
                .assertOprtrAccessible(eq(airlineUserOP0002), eq("OP0001"), eq("2026"));

        assertThatThrownBy(() -> empCo2CalcService.selectByPlanId("EP0001", airlineUserOP0002))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(403));

        then(empCo2CalcMapper).should(never()).selectByPlanId(any());
    }

    @Test
    @DisplayName("조회 — 자식 미작성 → null 반환 (정상)")
    void selectByPlanId_자식_미작성_null_반환() {
        EmpPlanVO plan = makeDraftPlan("EP0001", "OP0001");
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willDoNothing().given(dataScopeValidator)
                .assertOprtrAccessible(eq(molitUser), eq("OP0001"), eq("2026"));
        given(empCo2CalcMapper.selectByPlanId("EP0001")).willReturn(null);

        EmpCo2CalcVO result = empCo2CalcService.selectByPlanId("EP0001", molitUser);

        assertThat(result).isNull();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // upsertCo2Calc — Upsert
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Upsert — existsByPlanId=false → insertEmpCo2Calc 호출 (MTHD_A 정상 케이스)")
    void upsertCo2Calc_insert_경로_MTHD_A() {
        EmpPlanVO plan = makeDraftPlan("EP0001", "OP0001");
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));
        given(empCo2CalcMapper.existsByPlanId("EP0001")).willReturn(false);

        EmpCo2CalcVO vo = validCo2Calc();
        vo.setMntrMthdCd("MTHD_A");
        empCo2CalcService.upsertCo2Calc("EP0001", vo, airlineUserOP0001);

        then(empCo2CalcMapper).should(times(1)).insertEmpCo2Calc(any(EmpCo2CalcVO.class));
        then(empCo2CalcMapper).should(never()).updateEmpCo2Calc(any());
    }

    @Test
    @DisplayName("Upsert — existsByPlanId=true → updateEmpCo2Calc 호출 (BLOCK_ON_OFF 정상 케이스)")
    void upsertCo2Calc_update_경로_BLOCK_ON_OFF() {
        EmpPlanVO plan = makeDraftPlan("EP0001", "OP0001");
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));
        given(empCo2CalcMapper.existsByPlanId("EP0001")).willReturn(true);

        EmpCo2CalcVO vo = validCo2Calc();
        vo.setMntrMthdCd("BLOCK_ON_OFF");
        empCo2CalcService.upsertCo2Calc("EP0001", vo, airlineUserOP0001);

        then(empCo2CalcMapper).should(times(1)).updateEmpCo2Calc(any(EmpCo2CalcVO.class));
        then(empCo2CalcMapper).should(never()).insertEmpCo2Calc(any());
    }

    @Test
    @DisplayName("Upsert — 부모 plan DRAFT 아님(SBMTD) → CONFLICT(409) 예외")
    void upsertCo2Calc_부모plan_DRAFT_아님_CONFLICT() {
        EmpPlanVO plan = makePlan("EP0001", "OP0001", "SBMTD");
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));

        EmpCo2CalcVO vo = validCo2Calc();
        assertThatThrownBy(() -> empCo2CalcService.upsertCo2Calc("EP0001", vo, airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(409));

        then(empCo2CalcMapper).should(never()).insertEmpCo2Calc(any());
        then(empCo2CalcMapper).should(never()).updateEmpCo2Calc(any());
    }

    @Test
    @DisplayName("Upsert — AIRLINE 본인 외 운영사 접근 → FORBIDDEN(403) 예외")
    void upsertCo2Calc_AIRLINE_본인외_FORBIDDEN() {
        EmpPlanVO plan = makeDraftPlan("EP0001", "OP0001");
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willThrow(BusinessException.forbidden("본인 항공사 데이터만 등록·수정할 수 있습니다."))
                .given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0002), eq("OP0001"));

        EmpCo2CalcVO vo = validCo2Calc();
        assertThatThrownBy(() -> empCo2CalcService.upsertCo2Calc("EP0001", vo, airlineUserOP0002))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(403));

        then(empCo2CalcMapper).should(never()).insertEmpCo2Calc(any());
    }

    @Test
    @DisplayName("Upsert — mntrMthdCd 화이트리스트 외 값(INVALID) → BAD_REQUEST(400) 예외")
    void upsertCo2Calc_mntrMthdCd_허용외_BAD_REQUEST() {
        EmpPlanVO plan = makeDraftPlan("EP0001", "OP0001");
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));

        EmpCo2CalcVO vo = validCo2Calc();
        vo.setMntrMthdCd("INVALID");

        assertThatThrownBy(() -> empCo2CalcService.upsertCo2Calc("EP0001", vo, airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));
    }

    @Test
    @DisplayName("Upsert — fuelDnstySecd null → BAD_REQUEST(400) 예외")
    void upsertCo2Calc_fuelDnstySecd_null_BAD_REQUEST() {
        EmpPlanVO plan = makeDraftPlan("EP0001", "OP0001");
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));

        EmpCo2CalcVO vo = new EmpCo2CalcVO();
        vo.setMntrMthdCd("MTHD_B");
        vo.setFuelDnstySecd(null);

        assertThatThrownBy(() -> empCo2CalcService.upsertCo2Calc("EP0001", vo, airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));
    }

    @Test
    @DisplayName("Upsert — certUseYn Y/N 외 값(X) → BAD_REQUEST(400) 예외")
    void upsertCo2Calc_certUseYn_허용외_BAD_REQUEST() {
        EmpPlanVO plan = makeDraftPlan("EP0001", "OP0001");
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));

        EmpCo2CalcVO vo = validCo2Calc();
        vo.setCertUseYn("X"); // Y/N 외 불허

        assertThatThrownBy(() -> empCo2CalcService.upsertCo2Calc("EP0001", vo, airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));
    }

    @Test
    @DisplayName("Upsert — estCo2Emsn 음수 → BAD_REQUEST(400) 예외")
    void upsertCo2Calc_estCo2Emsn_음수_BAD_REQUEST() {
        EmpPlanVO plan = makeDraftPlan("EP0001", "OP0001");
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));

        EmpCo2CalcVO vo = validCo2Calc();
        vo.setEstCo2Emsn(new BigDecimal("-1.0000"));

        assertThatThrownBy(() -> empCo2CalcService.upsertCo2Calc("EP0001", vo, airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));
    }

    @Test
    @DisplayName("Upsert — estCo2Emsn null 허용 → 정상 처리")
    void upsertCo2Calc_estCo2Emsn_null_정상() {
        EmpPlanVO plan = makeDraftPlan("EP0001", "OP0001");
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));
        given(empCo2CalcMapper.existsByPlanId("EP0001")).willReturn(false);

        EmpCo2CalcVO vo = validCo2Calc();
        vo.setEstCo2Emsn(null); // null 허용

        assertThatCode(() -> empCo2CalcService.upsertCo2Calc("EP0001", vo, airlineUserOP0001))
                .doesNotThrowAnyException();

        then(empCo2CalcMapper).should(times(1)).insertEmpCo2Calc(any());
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

        assertThatCode(() -> empCo2CalcService.softDeleteByPlanId("EP0001", airlineUserOP0001))
                .doesNotThrowAnyException();

        then(empCo2CalcMapper).should(times(1)).softDeleteByPlanId(eq("EP0001"), eq("airline01"));
    }

    @Test
    @DisplayName("소프트삭제 — 부모 plan DRAFT 아님(APRVD) → CONFLICT(409) 예외")
    void softDeleteByPlanId_부모plan_DRAFT_아님_CONFLICT() {
        EmpPlanVO plan = makePlan("EP0001", "OP0001", "APRVD");
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));

        assertThatThrownBy(() -> empCo2CalcService.softDeleteByPlanId("EP0001", airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(409));

        then(empCo2CalcMapper).should(never()).softDeleteByPlanId(any(), any());
    }
}
