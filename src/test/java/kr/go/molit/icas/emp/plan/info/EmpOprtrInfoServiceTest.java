package kr.go.molit.icas.emp.plan.info;

import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.DataScopeValidator;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.emp.plan.EmpPlanMapper;
import kr.go.molit.icas.emp.plan.domain.EmpPlanVO;
import kr.go.molit.icas.emp.plan.info.domain.EmpOprtrInfoVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmpOprtrInfoService 단위 테스트 — 1:1 자식 조회/Upsert/소프트삭제")
class EmpOprtrInfoServiceTest {

    @Mock
    EmpOprtrInfoMapper empOprtrInfoMapper;

    @Mock
    EmpPlanMapper empPlanMapper;

    @Mock
    DataScopeValidator dataScopeValidator;

    @InjectMocks
    EmpOprtrInfoService empOprtrInfoService;

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

    // ── helper: 유효한 EmpOprtrInfoVO 생성 ──
    private EmpOprtrInfoVO validOprtrInfo() {
        EmpOprtrInfoVO vo = new EmpOprtrInfoVO();
        vo.setOprtrNm("대한항공");
        vo.setOprtrNmEn("Korean Air");
        return vo;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // selectByPlanId — 조회
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("조회 — 부모 plan 존재 + 가시범위 통과 → OprtrInfo 반환")
    void selectByPlanId_정상조회() {
        EmpPlanVO plan = makeDraftPlan("EP0001", "OP0001");
        EmpOprtrInfoVO info = new EmpOprtrInfoVO();
        info.setEmpPlanId("EP0001");
        info.setOprtrNm("대한항공");

        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willDoNothing().given(dataScopeValidator)
                .assertOprtrAccessible(eq(molitUser), eq("OP0001"), eq("2026"));
        given(empOprtrInfoMapper.selectByPlanId("EP0001")).willReturn(info);

        EmpOprtrInfoVO result = empOprtrInfoService.selectByPlanId("EP0001", molitUser);

        assertThat(result).isNotNull();
        assertThat(result.getEmpPlanId()).isEqualTo("EP0001");
        assertThat(result.getOprtrNm()).isEqualTo("대한항공");
    }

    @Test
    @DisplayName("조회 — 부모 plan 미존재 → NOT_FOUND(404) 예외")
    void selectByPlanId_부모plan_미존재_NOT_FOUND() {
        given(empPlanMapper.selectByEmpPlanId("EP9999")).willReturn(null);

        assertThatThrownBy(() -> empOprtrInfoService.selectByPlanId("EP9999", molitUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(404));

        then(empOprtrInfoMapper).should(never()).selectByPlanId(any());
    }

    @Test
    @DisplayName("조회 — 가시범위 위반 → FORBIDDEN(403) 예외")
    void selectByPlanId_가시범위_위반_FORBIDDEN() {
        EmpPlanVO plan = makeDraftPlan("EP0001", "OP0001");
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willThrow(BusinessException.forbidden("본인 항공사 데이터만 접근할 수 있습니다."))
                .given(dataScopeValidator)
                .assertOprtrAccessible(eq(airlineUserOP0002), eq("OP0001"), eq("2026"));

        assertThatThrownBy(() -> empOprtrInfoService.selectByPlanId("EP0001", airlineUserOP0002))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(403));

        then(empOprtrInfoMapper).should(never()).selectByPlanId(any());
    }

    @Test
    @DisplayName("조회 — 자식 미작성 → null 반환 (정상)")
    void selectByPlanId_자식_미작성_null_반환() {
        EmpPlanVO plan = makeDraftPlan("EP0001", "OP0001");
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willDoNothing().given(dataScopeValidator)
                .assertOprtrAccessible(eq(molitUser), eq("OP0001"), eq("2026"));
        given(empOprtrInfoMapper.selectByPlanId("EP0001")).willReturn(null);

        EmpOprtrInfoVO result = empOprtrInfoService.selectByPlanId("EP0001", molitUser);

        assertThat(result).isNull();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // upsertOprtrInfo — Upsert
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Upsert — existsByPlanId=false → insertEmpOprtrInfo 호출")
    void upsertOprtrInfo_insert_경로() {
        EmpPlanVO plan = makeDraftPlan("EP0001", "OP0001");
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));
        given(empOprtrInfoMapper.existsByPlanId("EP0001")).willReturn(false);

        EmpOprtrInfoVO vo = validOprtrInfo();
        empOprtrInfoService.upsertOprtrInfo("EP0001", vo, airlineUserOP0001);

        then(empOprtrInfoMapper).should(times(1)).insertEmpOprtrInfo(any(EmpOprtrInfoVO.class));
        then(empOprtrInfoMapper).should(never()).updateEmpOprtrInfo(any());
    }

    @Test
    @DisplayName("Upsert — existsByPlanId=true → updateEmpOprtrInfo 호출")
    void upsertOprtrInfo_update_경로() {
        EmpPlanVO plan = makeDraftPlan("EP0001", "OP0001");
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));
        given(empOprtrInfoMapper.existsByPlanId("EP0001")).willReturn(true);

        EmpOprtrInfoVO vo = validOprtrInfo();
        empOprtrInfoService.upsertOprtrInfo("EP0001", vo, airlineUserOP0001);

        then(empOprtrInfoMapper).should(times(1)).updateEmpOprtrInfo(any(EmpOprtrInfoVO.class));
        then(empOprtrInfoMapper).should(never()).insertEmpOprtrInfo(any());
    }

    @Test
    @DisplayName("Upsert — 부모 plan DRAFT 아님(SBMTD) → CONFLICT(409) 예외")
    void upsertOprtrInfo_부모plan_DRAFT_아님_CONFLICT() {
        EmpPlanVO plan = makePlan("EP0001", "OP0001", "SBMTD");
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));

        EmpOprtrInfoVO vo = validOprtrInfo();
        assertThatThrownBy(() -> empOprtrInfoService.upsertOprtrInfo("EP0001", vo, airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(409));

        then(empOprtrInfoMapper).should(never()).insertEmpOprtrInfo(any());
        then(empOprtrInfoMapper).should(never()).updateEmpOprtrInfo(any());
    }

    @Test
    @DisplayName("Upsert — AIRLINE 본인 외 운영사 접근 → FORBIDDEN(403) 예외")
    void upsertOprtrInfo_AIRLINE_본인외_FORBIDDEN() {
        EmpPlanVO plan = makeDraftPlan("EP0001", "OP0001");
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willThrow(BusinessException.forbidden("본인 항공사 데이터만 등록·수정할 수 있습니다."))
                .given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0002), eq("OP0001"));

        EmpOprtrInfoVO vo = validOprtrInfo();
        assertThatThrownBy(() -> empOprtrInfoService.upsertOprtrInfo("EP0001", vo, airlineUserOP0002))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(403));

        then(empOprtrInfoMapper).should(never()).insertEmpOprtrInfo(any());
    }

    @Test
    @DisplayName("Upsert — oprtrNm null → BAD_REQUEST(400) 예외")
    void upsertOprtrInfo_oprtrNm_null_BAD_REQUEST() {
        EmpPlanVO plan = makeDraftPlan("EP0001", "OP0001");
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));

        EmpOprtrInfoVO vo = new EmpOprtrInfoVO();
        vo.setOprtrNm(null);
        vo.setOprtrNmEn("Korean Air");

        assertThatThrownBy(() -> empOprtrInfoService.upsertOprtrInfo("EP0001", vo, airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));
    }

    @Test
    @DisplayName("Upsert — oprtrNmEn blank → BAD_REQUEST(400) 예외")
    void upsertOprtrInfo_oprtrNmEn_blank_BAD_REQUEST() {
        EmpPlanVO plan = makeDraftPlan("EP0001", "OP0001");
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));

        EmpOprtrInfoVO vo = new EmpOprtrInfoVO();
        vo.setOprtrNm("대한항공");
        vo.setOprtrNmEn("   ");

        assertThatThrownBy(() -> empOprtrInfoService.upsertOprtrInfo("EP0001", vo, airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));
    }

    @Test
    @DisplayName("Upsert — icaoDesig 2자 입력 → BAD_REQUEST(400) 예외")
    void upsertOprtrInfo_icaoDesig_2자_BAD_REQUEST() {
        EmpPlanVO plan = makeDraftPlan("EP0001", "OP0001");
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));

        EmpOprtrInfoVO vo = validOprtrInfo();
        vo.setIcaoDesig("KE"); // 2자 — 불허

        assertThatThrownBy(() -> empOprtrInfoService.upsertOprtrInfo("EP0001", vo, airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));
    }

    @Test
    @DisplayName("Upsert — icaoDesig 4자 입력 → BAD_REQUEST(400) 예외")
    void upsertOprtrInfo_icaoDesig_4자_BAD_REQUEST() {
        EmpPlanVO plan = makeDraftPlan("EP0001", "OP0001");
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));

        EmpOprtrInfoVO vo = validOprtrInfo();
        vo.setIcaoDesig("KEAL"); // 4자 — 불허

        assertThatThrownBy(() -> empOprtrInfoService.upsertOprtrInfo("EP0001", vo, airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));
    }

    @Test
    @DisplayName("Upsert — aocIsueDt > aocXprDt → BAD_REQUEST(400) 예외")
    void upsertOprtrInfo_aocIsueDt_after_aocXprDt_BAD_REQUEST() {
        EmpPlanVO plan = makeDraftPlan("EP0001", "OP0001");
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));

        EmpOprtrInfoVO vo = validOprtrInfo();
        vo.setAocIsueDt(LocalDate.of(2026, 12, 31));
        vo.setAocXprDt(LocalDate.of(2026, 1, 1)); // 발급일이 만료일보다 이후

        assertThatThrownBy(() -> empOprtrInfoService.upsertOprtrInfo("EP0001", vo, airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));
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

        assertThatCode(() -> empOprtrInfoService.softDeleteByPlanId("EP0001", airlineUserOP0001))
                .doesNotThrowAnyException();

        then(empOprtrInfoMapper).should(times(1)).softDeleteByPlanId(eq("EP0001"), eq("airline01"));
    }

    @Test
    @DisplayName("소프트삭제 — 부모 plan DRAFT 아님(APRVD) → CONFLICT(409) 예외")
    void softDeleteByPlanId_부모plan_DRAFT_아님_CONFLICT() {
        EmpPlanVO plan = makePlan("EP0001", "OP0001", "APRVD");
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));

        assertThatThrownBy(() -> empOprtrInfoService.softDeleteByPlanId("EP0001", airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(409));

        then(empOprtrInfoMapper).should(never()).softDeleteByPlanId(any(), any());
    }
}
