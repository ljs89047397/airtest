package kr.go.molit.icas.emp.plan.cnct;

import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.DataScopeValidator;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.emp.plan.EmpPlanMapper;
import kr.go.molit.icas.emp.plan.cnct.domain.EmpOprtrCnctVO;
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
@DisplayName("EmpOprtrCnctService 단위 테스트 — 담당자 연락처 CRUD + 검증")
class EmpOprtrCnctServiceTest {

    @Mock
    EmpOprtrCnctMapper empOprtrCnctMapper;

    @Mock
    EmpPlanMapper empPlanMapper;

    @Mock
    DataScopeValidator dataScopeValidator;

    @InjectMocks
    EmpOprtrCnctService empOprtrCnctService;

    private IcasUser airlineUserOP0001;
    private IcasUser airlineUserOP0002;
    private IcasUser molitUser;

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

        molitUser = IcasUser.builder()
                .userId("molit01").userNm("국토부 담당자")
                .ognzSeCd("MOLIT").ognzId("ORG_MOLIT").master(false)
                .roleIds(List.of("ADMIN")).build();
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

    private EmpOprtrCnctVO makeCnctVO(String cnctSeCd, String userNm, String mblphnNo, String emlAddr) {
        EmpOprtrCnctVO vo = new EmpOprtrCnctVO();
        vo.setCnctSeCd(cnctSeCd);
        vo.setUserNm(userNm);
        vo.setMblphnNo(mblphnNo);
        vo.setEmlAddr(emlAddr);
        return vo;
    }

    private EmpOprtrCnctVO makeSavedCnct(int sn, String cnctSeCd) {
        EmpOprtrCnctVO vo = new EmpOprtrCnctVO();
        vo.setEmpPlanId("EP0001");
        vo.setCnctSn(sn);
        vo.setCnctSeCd(cnctSeCd);
        vo.setUserNm("홍길동");
        vo.setMblphnNo("010-1234-5678");
        return vo;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // listByPlan
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("listByPlan — 정상: plan 존재 + 가시범위 통과 → 목록 반환 (cnct_sn ASC 가정)")
    void listByPlan_정상() {
        EmpPlanVO plan = makeDraftPlan();
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willDoNothing().given(dataScopeValidator)
                .assertOprtrAccessible(eq(airlineUserOP0001), eq("OP0001"), eq("2026"));
        given(empOprtrCnctMapper.selectByPlanId("EP0001"))
                .willReturn(List.of(makeSavedCnct(1, "PRIMARY"), makeSavedCnct(2, "SUB")));

        List<EmpOprtrCnctVO> result = empOprtrCnctService.listByPlan("EP0001", airlineUserOP0001);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getCnctSn()).isEqualTo(1);
        assertThat(result.get(1).getCnctSn()).isEqualTo(2);
    }

    @Test
    @DisplayName("listByPlan — 부모 plan 미존재 → NOT_FOUND(404)")
    void listByPlan_plan미존재_NOT_FOUND() {
        given(empPlanMapper.selectByEmpPlanId("EP9999")).willReturn(null);

        assertThatThrownBy(() -> empOprtrCnctService.listByPlan("EP9999", airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(404));

        then(empOprtrCnctMapper).should(never()).selectByPlanId(any());
    }

    @Test
    @DisplayName("listByPlan — 가시범위 위반 → FORBIDDEN(403)")
    void listByPlan_가시범위위반_FORBIDDEN() {
        EmpPlanVO plan = makeDraftPlan();
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willThrow(BusinessException.forbidden("본인 항공사 데이터만 접근할 수 있습니다."))
                .given(dataScopeValidator).assertOprtrAccessible(eq(airlineUserOP0002), eq("OP0001"), eq("2026"));

        assertThatThrownBy(() -> empOprtrCnctService.listByPlan("EP0001", airlineUserOP0002))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(403));

        then(empOprtrCnctMapper).should(never()).selectByPlanId(any());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // addChild
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("addChild — 정상: sn 채번 후 insert, 반환된 VO 의 cnctSn 검증")
    void addChild_정상() {
        EmpPlanVO plan = makeDraftPlan();
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));
        given(empOprtrCnctMapper.countByPlanAndSeCd("EP0001", "PRIMARY")).willReturn(0);
        given(empOprtrCnctMapper.selectNextSn("EP0001")).willReturn(1);
        given(empOprtrCnctMapper.insertEmpOprtrCnct(any(EmpOprtrCnctVO.class))).willReturn(1);

        EmpOprtrCnctVO saved = makeSavedCnct(1, "PRIMARY");
        given(empOprtrCnctMapper.selectOne("EP0001", 1)).willReturn(saved);

        EmpOprtrCnctVO vo = makeCnctVO("PRIMARY", "홍길동", "010-1234-5678", null);

        EmpOprtrCnctVO result = empOprtrCnctService.addChild("EP0001", vo, airlineUserOP0001);

        assertThat(result.getCnctSn()).isEqualTo(1);
        assertThat(result.getCnctSeCd()).isEqualTo("PRIMARY");

        ArgumentCaptor<EmpOprtrCnctVO> captor = ArgumentCaptor.forClass(EmpOprtrCnctVO.class);
        then(empOprtrCnctMapper).should(times(1)).insertEmpOprtrCnct(captor.capture());
        assertThat(captor.getValue().getCnctSn()).isEqualTo(1);
        assertThat(captor.getValue().getFrstRegUserId()).isEqualTo("airline01");
    }

    @Test
    @DisplayName("addChild — 부모 plan DRAFT 아님(SBMTD) → BAD_REQUEST(400)")
    void addChild_부모plan_DRAFT아님_BAD_REQUEST() {
        EmpPlanVO plan = makeSubmittedPlan();
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));

        EmpOprtrCnctVO vo = makeCnctVO("PRIMARY", "홍길동", "010-1234-5678", null);

        assertThatThrownBy(() -> empOprtrCnctService.addChild("EP0001", vo, airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));

        then(empOprtrCnctMapper).should(never()).insertEmpOprtrCnct(any());
    }

    @Test
    @DisplayName("addChild — AIRLINE 본인 외 운영사 → FORBIDDEN(403)")
    void addChild_타운영사_FORBIDDEN() {
        EmpPlanVO plan = makeDraftPlan();
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willThrow(BusinessException.forbidden("본인 항공사 데이터만 등록·수정할 수 있습니다."))
                .given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0002), eq("OP0001"));

        EmpOprtrCnctVO vo = makeCnctVO("PRIMARY", "홍길동", "010-1234-5678", null);

        assertThatThrownBy(() -> empOprtrCnctService.addChild("EP0001", vo, airlineUserOP0002))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(403));

        then(empOprtrCnctMapper).should(never()).insertEmpOprtrCnct(any());
    }

    @Test
    @DisplayName("addChild — cnct_se_cd 화이트리스트 외(XXX) → BAD_REQUEST(400)")
    void addChild_cnctSeCd_화이트리스트외_BAD_REQUEST() {
        EmpPlanVO plan = makeDraftPlan();
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));

        EmpOprtrCnctVO vo = makeCnctVO("XXX", "홍길동", "010-1234-5678", null);

        assertThatThrownBy(() -> empOprtrCnctService.addChild("EP0001", vo, airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));
    }

    @Test
    @DisplayName("addChild — user_nm 누락 → BAD_REQUEST(400)")
    void addChild_userNm누락_BAD_REQUEST() {
        EmpPlanVO plan = makeDraftPlan();
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));

        EmpOprtrCnctVO vo = makeCnctVO("PRIMARY", null, "010-1234-5678", null);

        assertThatThrownBy(() -> empOprtrCnctService.addChild("EP0001", vo, airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));
    }

    @Test
    @DisplayName("addChild — mblphn_no, eml_addr 모두 null → BAD_REQUEST(400)")
    void addChild_연락처모두null_BAD_REQUEST() {
        EmpPlanVO plan = makeDraftPlan();
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));

        EmpOprtrCnctVO vo = makeCnctVO("SUB", "홍길동", null, null);

        assertThatThrownBy(() -> empOprtrCnctService.addChild("EP0001", vo, airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));
    }

    @Test
    @DisplayName("addChild — 같은 plan에 PRIMARY 이미 존재, 또 PRIMARY 추가 → CONFLICT(409)")
    void addChild_PRIMARY중복_CONFLICT() {
        EmpPlanVO plan = makeDraftPlan();
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));
        given(empOprtrCnctMapper.countByPlanAndSeCd("EP0001", "PRIMARY")).willReturn(1);

        EmpOprtrCnctVO vo = makeCnctVO("PRIMARY", "홍길동", "010-1234-5678", null);

        assertThatThrownBy(() -> empOprtrCnctService.addChild("EP0001", vo, airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(409));

        then(empOprtrCnctMapper).should(never()).insertEmpOprtrCnct(any());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // updateChild
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("updateChild — 정상 수정: SUB → SUB (PRIMARY 카운트 체크 생략)")
    void updateChild_정상() {
        EmpPlanVO plan = makeDraftPlan();
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));
        given(empOprtrCnctMapper.selectOne("EP0001", 1)).willReturn(makeSavedCnct(1, "SUB"));
        given(empOprtrCnctMapper.updateEmpOprtrCnct(any(EmpOprtrCnctVO.class))).willReturn(1);

        EmpOprtrCnctVO vo = makeCnctVO("SUB", "김철수", null, "kim@test.com");

        assertThatCode(() -> empOprtrCnctService.updateChild("EP0001", 1, vo, airlineUserOP0001))
                .doesNotThrowAnyException();

        then(empOprtrCnctMapper).should(times(1)).updateEmpOprtrCnct(any(EmpOprtrCnctVO.class));
    }

    @Test
    @DisplayName("updateChild — 본인이 PRIMARY인 행 수정(PRIMARY→PRIMARY) → 통과 (자신 cnctSeCd == 대상 cnctSeCd)")
    void updateChild_PRIMARY행을PRIMARY로_수정_통과() {
        EmpPlanVO plan = makeDraftPlan();
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));
        // existing 도 PRIMARY
        given(empOprtrCnctMapper.selectOne("EP0001", 1)).willReturn(makeSavedCnct(1, "PRIMARY"));
        given(empOprtrCnctMapper.updateEmpOprtrCnct(any(EmpOprtrCnctVO.class))).willReturn(1);

        // 수정 vo 도 PRIMARY → 서비스 로직에서 existing.getCnctSeCd().equals("PRIMARY") 이므로 카운트 체크 스킵
        EmpOprtrCnctVO vo = makeCnctVO("PRIMARY", "홍길동 수정", "010-9999-0000", null);

        assertThatCode(() -> empOprtrCnctService.updateChild("EP0001", 1, vo, airlineUserOP0001))
                .doesNotThrowAnyException();

        // countByPlanAndSeCd 호출 안 됨
        then(empOprtrCnctMapper).should(never()).countByPlanAndSeCd(any(), any());
    }

    @Test
    @DisplayName("updateChild — 자식 미존재 → NOT_FOUND(404)")
    void updateChild_자식미존재_NOT_FOUND() {
        EmpPlanVO plan = makeDraftPlan();
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));
        given(empOprtrCnctMapper.selectOne("EP0001", 99)).willReturn(null);

        EmpOprtrCnctVO vo = makeCnctVO("SUB", "홍길동", "010-1234-5678", null);

        assertThatThrownBy(() -> empOprtrCnctService.updateChild("EP0001", 99, vo, airlineUserOP0001))
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
        given(empOprtrCnctMapper.softDeleteOne("EP0001", 1, "airline01")).willReturn(1);

        assertThatCode(() -> empOprtrCnctService.softDeleteChild("EP0001", 1, airlineUserOP0001))
                .doesNotThrowAnyException();

        then(empOprtrCnctMapper).should(times(1)).softDeleteOne("EP0001", 1, "airline01");
    }

    @Test
    @DisplayName("softDeleteChild — 자식 미존재(affected=0) → NOT_FOUND(404)")
    void softDeleteChild_자식미존재_NOT_FOUND() {
        EmpPlanVO plan = makeDraftPlan();
        given(empPlanMapper.selectByEmpPlanId("EP0001")).willReturn(plan);
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));
        given(empOprtrCnctMapper.softDeleteOne("EP0001", 99, "airline01")).willReturn(0);

        assertThatThrownBy(() -> empOprtrCnctService.softDeleteChild("EP0001", 99, airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(404));
    }
}
