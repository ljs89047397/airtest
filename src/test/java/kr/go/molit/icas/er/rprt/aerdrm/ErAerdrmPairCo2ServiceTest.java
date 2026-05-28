package kr.go.molit.icas.er.rprt.aerdrm;

import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.DataScopeValidator;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.er.rprt.ErMapper;
import kr.go.molit.icas.er.rprt.aerdrm.domain.ErAerdrmPairCo2VO;
import kr.go.molit.icas.er.rprt.domain.ErVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ErAerdrmPairCo2Service 단위 테스트 — 비행장 쌍 배출량 CRUD + 검증")
class ErAerdrmPairCo2ServiceTest {

    @Mock
    ErAerdrmPairCo2Mapper erAerdrmPairCo2Mapper;

    @Mock
    ErMapper erMapper;

    @Mock
    DataScopeValidator dataScopeValidator;

    @InjectMocks
    ErAerdrmPairCo2Service erAerdrmPairCo2Service;

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

    private ErVO makeDraftEr(String erId, String oprtrId, String rprtYr) {
        ErVO er = new ErVO();
        er.setErId(erId);
        er.setOprtrId(oprtrId);
        er.setRprtYr(rprtYr);
        er.setErStCd("DRAFT");
        return er;
    }

    private ErVO makeSubmittedEr(String erId, String oprtrId, String rprtYr) {
        ErVO er = makeDraftEr(erId, oprtrId, rprtYr);
        er.setErStCd("SBMTD");
        return er;
    }

    private ErAerdrmPairCo2VO makeVO(String dprtrAerdrm, String arvlAerdrm,
                                     String dprtrCntry, String arvlCntry,
                                     String fuelType, Integer fltCnt,
                                     BigDecimal fuelWght, BigDecimal co2Emsn) {
        ErAerdrmPairCo2VO vo = new ErAerdrmPairCo2VO();
        vo.setDprtrAerdrmCd(dprtrAerdrm);
        vo.setArvlAerdrmCd(arvlAerdrm);
        vo.setDprtrCntryCd(dprtrCntry);
        vo.setArvlCntryCd(arvlCntry);
        vo.setFuelTypeCd(fuelType);
        vo.setFltCnt(fltCnt);
        vo.setFuelWght(fuelWght);
        vo.setCo2Emsn(co2Emsn);
        return vo;
    }

    private ErAerdrmPairCo2VO makeSavedPair(String erId, int sn, String dprtrAerdrm, String arvlAerdrm) {
        ErAerdrmPairCo2VO vo = new ErAerdrmPairCo2VO();
        vo.setErId(erId);
        vo.setPairSn(sn);
        vo.setDprtrAerdrmCd(dprtrAerdrm);
        vo.setArvlAerdrmCd(arvlAerdrm);
        vo.setDprtrCntryCd("KR");
        vo.setArvlCntryCd("JP");
        vo.setFuelTypeCd("JET-A");
        return vo;
    }

    private ErAerdrmPairCo2VO makeValidVO() {
        return makeVO("RKSI", "RJTT", "KR", "JP", "JET-A", 100,
                new BigDecimal("50000"), new BigDecimal("158000"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // list
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("list — 정상: ER 존재 + 가시범위 통과 → 목록 반환 (pair_sn ASC)")
    void list_정상() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeDraftEr("ER0001", "OP0001", "2026"));
        willDoNothing().given(dataScopeValidator)
                .assertOprtrAccessible(eq(airlineUserOP0001), eq("OP0001"), eq("2026"));
        given(erAerdrmPairCo2Mapper.selectByErId("ER0001"))
                .willReturn(List.of(makeSavedPair("ER0001", 1, "RKSI", "RJTT"),
                                    makeSavedPair("ER0001", 2, "RKSI", "KLAX")));

        List<ErAerdrmPairCo2VO> result = erAerdrmPairCo2Service.list("ER0001", airlineUserOP0001);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getPairSn()).isEqualTo(1);
        assertThat(result.get(1).getDprtrAerdrmCd()).isEqualTo("RKSI");
    }

    @Test
    @DisplayName("list — 부모 ER 미존재 → NOT_FOUND(404)")
    void list_ER미존재_NOT_FOUND() {
        given(erMapper.selectByErId("ER9999")).willReturn(null);

        assertThatThrownBy(() -> erAerdrmPairCo2Service.list("ER9999", airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(404));

        then(erAerdrmPairCo2Mapper).should(never()).selectByErId(any());
    }

    @Test
    @DisplayName("list — 가시범위 위반 → FORBIDDEN(403)")
    void list_가시범위위반_FORBIDDEN() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeDraftEr("ER0001", "OP0001", "2026"));
        willThrow(BusinessException.forbidden("본인 항공사 데이터만 접근할 수 있습니다."))
                .given(dataScopeValidator)
                .assertOprtrAccessible(eq(airlineUserOP0002), eq("OP0001"), eq("2026"));

        assertThatThrownBy(() -> erAerdrmPairCo2Service.list("ER0001", airlineUserOP0002))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(403));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // add
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("add — 정상: sn 채번 후 insert, frstRegUserId 설정 검증")
    void add_정상_sn채번() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeDraftEr("ER0001", "OP0001", "2026"));
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));
        given(erAerdrmPairCo2Mapper.existsByAerdrmPair("ER0001", "RKSI", "RJTT", "JET-A", -1)).willReturn(false);
        given(erAerdrmPairCo2Mapper.selectNextSn("ER0001")).willReturn(1);
        given(erAerdrmPairCo2Mapper.insert(any(ErAerdrmPairCo2VO.class))).willReturn(1);
        given(erAerdrmPairCo2Mapper.selectOne("ER0001", 1))
                .willReturn(makeSavedPair("ER0001", 1, "RKSI", "RJTT"));

        ErAerdrmPairCo2VO vo = makeValidVO();
        ErAerdrmPairCo2VO result = erAerdrmPairCo2Service.add("ER0001", vo, airlineUserOP0001);

        assertThat(result.getPairSn()).isEqualTo(1);
        assertThat(result.getDprtrAerdrmCd()).isEqualTo("RKSI");

        ArgumentCaptor<ErAerdrmPairCo2VO> captor = ArgumentCaptor.forClass(ErAerdrmPairCo2VO.class);
        then(erAerdrmPairCo2Mapper).should(times(1)).insert(captor.capture());
        assertThat(captor.getValue().getPairSn()).isEqualTo(1);
        assertThat(captor.getValue().getFrstRegUserId()).isEqualTo("airline01");
    }

    @Test
    @DisplayName("add — 부모 ER DRAFT 아님 → BAD_REQUEST(400)")
    void add_ER_DRAFT아님_BAD_REQUEST() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeSubmittedEr("ER0001", "OP0001", "2026"));
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));

        assertThatThrownBy(() -> erAerdrmPairCo2Service.add("ER0001", makeValidVO(), airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));
    }

    @Test
    @DisplayName("add — AIRLINE 본인 외 → FORBIDDEN(403)")
    void add_타운영사_FORBIDDEN() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeDraftEr("ER0001", "OP0001", "2026"));
        willThrow(BusinessException.forbidden("본인 항공사 데이터만 등록·수정할 수 있습니다."))
                .given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0002), eq("OP0001"));

        assertThatThrownBy(() -> erAerdrmPairCo2Service.add("ER0001", makeValidVO(), airlineUserOP0002))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(403));
    }

    @Test
    @DisplayName("add — dprtrAerdrmCd 4자 아님(RKS) → BAD_REQUEST(400)")
    void add_dprtrAerdrmCd_4자아님_BAD_REQUEST() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeDraftEr("ER0001", "OP0001", "2026"));
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));

        ErAerdrmPairCo2VO vo = makeVO("RKS", "RJTT", "KR", "JP", "JET-A", 100,
                new BigDecimal("50000"), new BigDecimal("158000"));

        assertThatThrownBy(() -> erAerdrmPairCo2Service.add("ER0001", vo, airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));
    }

    @Test
    @DisplayName("add — arvlAerdrmCd 4자 아님(RJTTT) → BAD_REQUEST(400)")
    void add_arvlAerdrmCd_4자아님_BAD_REQUEST() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeDraftEr("ER0001", "OP0001", "2026"));
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));

        ErAerdrmPairCo2VO vo = makeVO("RKSI", "RJTTT", "KR", "JP", "JET-A", 100,
                new BigDecimal("50000"), new BigDecimal("158000"));

        assertThatThrownBy(() -> erAerdrmPairCo2Service.add("ER0001", vo, airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));
    }

    @Test
    @DisplayName("add — dprtrCntryCd 2자 아님(KOR) → BAD_REQUEST(400)")
    void add_dprtrCntryCd_2자아님_BAD_REQUEST() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeDraftEr("ER0001", "OP0001", "2026"));
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));

        ErAerdrmPairCo2VO vo = makeVO("RKSI", "RJTT", "KOR", "JP", "JET-A", 100,
                new BigDecimal("50000"), new BigDecimal("158000"));

        assertThatThrownBy(() -> erAerdrmPairCo2Service.add("ER0001", vo, airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));
    }

    @Test
    @DisplayName("add — arvlCntryCd 2자 아님(J) → BAD_REQUEST(400)")
    void add_arvlCntryCd_2자아님_BAD_REQUEST() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeDraftEr("ER0001", "OP0001", "2026"));
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));

        ErAerdrmPairCo2VO vo = makeVO("RKSI", "RJTT", "KR", "J", "JET-A", 100,
                new BigDecimal("50000"), new BigDecimal("158000"));

        assertThatThrownBy(() -> erAerdrmPairCo2Service.add("ER0001", vo, airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));
    }

    @Test
    @DisplayName("add — fltCnt 음수 → BAD_REQUEST(400)")
    void add_fltCnt_음수_BAD_REQUEST() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeDraftEr("ER0001", "OP0001", "2026"));
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));

        ErAerdrmPairCo2VO vo = makeVO("RKSI", "RJTT", "KR", "JP", "JET-A", -1,
                new BigDecimal("50000"), new BigDecimal("158000"));

        assertThatThrownBy(() -> erAerdrmPairCo2Service.add("ER0001", vo, airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));
    }

    @Test
    @DisplayName("add — fuelWght 음수 → BAD_REQUEST(400)")
    void add_fuelWght_음수_BAD_REQUEST() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeDraftEr("ER0001", "OP0001", "2026"));
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));

        ErAerdrmPairCo2VO vo = makeVO("RKSI", "RJTT", "KR", "JP", "JET-A", 100,
                new BigDecimal("-1"), new BigDecimal("158000"));

        assertThatThrownBy(() -> erAerdrmPairCo2Service.add("ER0001", vo, airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));
    }

    @Test
    @DisplayName("add — co2Emsn 음수 → BAD_REQUEST(400)")
    void add_co2Emsn_음수_BAD_REQUEST() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeDraftEr("ER0001", "OP0001", "2026"));
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));

        ErAerdrmPairCo2VO vo = makeVO("RKSI", "RJTT", "KR", "JP", "JET-A", 100,
                new BigDecimal("50000"), new BigDecimal("-100"));

        assertThatThrownBy(() -> erAerdrmPairCo2Service.add("ER0001", vo, airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));
    }

    @Test
    @DisplayName("add — 같은 (dprtrAerdrm, arvlAerdrm, fuelType) 중복 → CONFLICT(409)")
    void add_비행장쌍중복_CONFLICT() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeDraftEr("ER0001", "OP0001", "2026"));
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));
        given(erAerdrmPairCo2Mapper.existsByAerdrmPair("ER0001", "RKSI", "RJTT", "JET-A", -1)).willReturn(true);

        assertThatThrownBy(() -> erAerdrmPairCo2Service.add("ER0001", makeValidVO(), airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(409));

        then(erAerdrmPairCo2Mapper).should(never()).insert(any());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // update
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("update — 부모 ER DRAFT 아님 → BAD_REQUEST(400)")
    void update_ER_DRAFT아님_BAD_REQUEST() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeSubmittedEr("ER0001", "OP0001", "2026"));
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));

        assertThatThrownBy(() -> erAerdrmPairCo2Service.update("ER0001", 1, makeValidVO(), airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // softDelete
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("softDelete — 정상 소프트삭제")
    void softDelete_정상() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeDraftEr("ER0001", "OP0001", "2026"));
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));
        given(erAerdrmPairCo2Mapper.softDelete("ER0001", 1, "airline01")).willReturn(1);

        assertThatCode(() -> erAerdrmPairCo2Service.softDelete("ER0001", 1, airlineUserOP0001))
                .doesNotThrowAnyException();

        then(erAerdrmPairCo2Mapper).should(times(1)).softDelete("ER0001", 1, "airline01");
    }

    @Test
    @DisplayName("softDelete — 자식 미존재(affected=0) → NOT_FOUND(404)")
    void softDelete_자식미존재_NOT_FOUND() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeDraftEr("ER0001", "OP0001", "2026"));
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));
        given(erAerdrmPairCo2Mapper.softDelete("ER0001", 99, "airline01")).willReturn(0);

        assertThatThrownBy(() -> erAerdrmPairCo2Service.softDelete("ER0001", 99, airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(404));
    }
}
