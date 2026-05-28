package kr.go.molit.icas.er.rprt.cntry;

import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.DataScopeValidator;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.er.rprt.ErMapper;
import kr.go.molit.icas.er.rprt.cntry.domain.ErCntryPairCo2VO;
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
@DisplayName("ErCntryPairCo2Service 단위 테스트 — 국가 쌍 배출량 CRUD + 검증")
class ErCntryPairCo2ServiceTest {

    @Mock
    ErCntryPairCo2Mapper erCntryPairCo2Mapper;

    @Mock
    ErMapper erMapper;

    @Mock
    DataScopeValidator dataScopeValidator;

    @InjectMocks
    ErCntryPairCo2Service erCntryPairCo2Service;

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

    private ErCntryPairCo2VO makeVO(String dprtr, String arvl, String fuelType,
                                    String cerEstmYn, String ofstReqYn,
                                    Integer fltCnt, BigDecimal fuelWght, BigDecimal convFctr) {
        ErCntryPairCo2VO vo = new ErCntryPairCo2VO();
        vo.setDprtrCntryCd(dprtr);
        vo.setArvlCntryCd(arvl);
        vo.setFuelTypeCd(fuelType);
        vo.setCerEstmYn(cerEstmYn);
        vo.setOfstReqYn(ofstReqYn);
        vo.setFltCnt(fltCnt);
        vo.setFuelWght(fuelWght);
        vo.setConvFctr(convFctr);
        return vo;
    }

    private ErCntryPairCo2VO makeSavedPair(String erId, int sn, String dprtr, String arvl) {
        ErCntryPairCo2VO vo = new ErCntryPairCo2VO();
        vo.setErId(erId);
        vo.setPairSn(sn);
        vo.setDprtrCntryCd(dprtr);
        vo.setArvlCntryCd(arvl);
        vo.setFuelTypeCd("JET-A");
        vo.setConvFctr(new BigDecimal("3.1600"));
        return vo;
    }

    private ErCntryPairCo2VO makeValidVO() {
        return makeVO("KR", "JP", "JET-A", "N", "N", 100, new BigDecimal("50000"), new BigDecimal("3.16"));
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
        given(erCntryPairCo2Mapper.selectByErId("ER0001"))
                .willReturn(List.of(makeSavedPair("ER0001", 1, "KR", "JP"),
                                    makeSavedPair("ER0001", 2, "KR", "US")));

        List<ErCntryPairCo2VO> result = erCntryPairCo2Service.list("ER0001", airlineUserOP0001);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getPairSn()).isEqualTo(1);
        assertThat(result.get(1).getDprtrCntryCd()).isEqualTo("KR");
    }

    @Test
    @DisplayName("list — 부모 ER 미존재 → NOT_FOUND(404)")
    void list_ER미존재_NOT_FOUND() {
        given(erMapper.selectByErId("ER9999")).willReturn(null);

        assertThatThrownBy(() -> erCntryPairCo2Service.list("ER9999", airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(404));

        then(erCntryPairCo2Mapper).should(never()).selectByErId(any());
    }

    @Test
    @DisplayName("list — 가시범위 위반 → FORBIDDEN(403)")
    void list_가시범위위반_FORBIDDEN() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeDraftEr("ER0001", "OP0001", "2026"));
        willThrow(BusinessException.forbidden("본인 항공사 데이터만 접근할 수 있습니다."))
                .given(dataScopeValidator)
                .assertOprtrAccessible(eq(airlineUserOP0002), eq("OP0001"), eq("2026"));

        assertThatThrownBy(() -> erCntryPairCo2Service.list("ER0001", airlineUserOP0002))
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
        given(erCntryPairCo2Mapper.existsByPair("ER0001", "KR", "JP", "JET-A", -1)).willReturn(false);
        given(erCntryPairCo2Mapper.selectNextSn("ER0001")).willReturn(1);
        given(erCntryPairCo2Mapper.insert(any(ErCntryPairCo2VO.class))).willReturn(1);
        given(erCntryPairCo2Mapper.selectOne("ER0001", 1)).willReturn(makeSavedPair("ER0001", 1, "KR", "JP"));

        ErCntryPairCo2VO vo = makeValidVO();
        ErCntryPairCo2VO result = erCntryPairCo2Service.add("ER0001", vo, airlineUserOP0001);

        assertThat(result.getPairSn()).isEqualTo(1);
        assertThat(result.getDprtrCntryCd()).isEqualTo("KR");

        ArgumentCaptor<ErCntryPairCo2VO> captor = ArgumentCaptor.forClass(ErCntryPairCo2VO.class);
        then(erCntryPairCo2Mapper).should(times(1)).insert(captor.capture());
        assertThat(captor.getValue().getPairSn()).isEqualTo(1);
        assertThat(captor.getValue().getFrstRegUserId()).isEqualTo("airline01");
    }

    @Test
    @DisplayName("add — 부모 ER DRAFT 아님 → BAD_REQUEST(400)")
    void add_ER_DRAFT아님_BAD_REQUEST() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeSubmittedEr("ER0001", "OP0001", "2026"));
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));

        assertThatThrownBy(() -> erCntryPairCo2Service.add("ER0001", makeValidVO(), airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));
    }

    @Test
    @DisplayName("add — AIRLINE 본인 외 → FORBIDDEN(403)")
    void add_타운영사_FORBIDDEN() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeDraftEr("ER0001", "OP0001", "2026"));
        willThrow(BusinessException.forbidden("본인 항공사 데이터만 등록·수정할 수 있습니다."))
                .given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0002), eq("OP0001"));

        assertThatThrownBy(() -> erCntryPairCo2Service.add("ER0001", makeValidVO(), airlineUserOP0002))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(403));
    }

    @Test
    @DisplayName("add — cerEstmYn Y/N 외 → BAD_REQUEST(400)")
    void add_cerEstmYn_잘못된값_BAD_REQUEST() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeDraftEr("ER0001", "OP0001", "2026"));
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));

        ErCntryPairCo2VO vo = makeVO("KR", "JP", "JET-A", "X", "N", 100,
                new BigDecimal("50000"), new BigDecimal("3.16"));

        assertThatThrownBy(() -> erCntryPairCo2Service.add("ER0001", vo, airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));
    }

    @Test
    @DisplayName("add — ofstReqYn Y/N 외 → BAD_REQUEST(400)")
    void add_ofstReqYn_잘못된값_BAD_REQUEST() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeDraftEr("ER0001", "OP0001", "2026"));
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));

        ErCntryPairCo2VO vo = makeVO("KR", "JP", "JET-A", "N", "Z", 100,
                new BigDecimal("50000"), new BigDecimal("3.16"));

        assertThatThrownBy(() -> erCntryPairCo2Service.add("ER0001", vo, airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));
    }

    @Test
    @DisplayName("add — dprtrCntryCd 2자 아님(K) → BAD_REQUEST(400)")
    void add_dprtrCntryCd_2자아님_BAD_REQUEST() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeDraftEr("ER0001", "OP0001", "2026"));
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));

        ErCntryPairCo2VO vo = makeVO("K", "JP", "JET-A", "N", "N", 100,
                new BigDecimal("50000"), new BigDecimal("3.16"));

        assertThatThrownBy(() -> erCntryPairCo2Service.add("ER0001", vo, airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));
    }

    @Test
    @DisplayName("add — arvlCntryCd 2자 아님(JPN) → BAD_REQUEST(400)")
    void add_arvlCntryCd_2자아님_BAD_REQUEST() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeDraftEr("ER0001", "OP0001", "2026"));
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));

        ErCntryPairCo2VO vo = makeVO("KR", "JPN", "JET-A", "N", "N", 100,
                new BigDecimal("50000"), new BigDecimal("3.16"));

        assertThatThrownBy(() -> erCntryPairCo2Service.add("ER0001", vo, airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));
    }

    @Test
    @DisplayName("add — fltCnt 음수 → BAD_REQUEST(400)")
    void add_fltCnt_음수_BAD_REQUEST() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeDraftEr("ER0001", "OP0001", "2026"));
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));

        ErCntryPairCo2VO vo = makeVO("KR", "JP", "JET-A", "N", "N", -1,
                new BigDecimal("50000"), new BigDecimal("3.16"));

        assertThatThrownBy(() -> erCntryPairCo2Service.add("ER0001", vo, airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));
    }

    @Test
    @DisplayName("add — fuelWght 음수 → BAD_REQUEST(400)")
    void add_fuelWght_음수_BAD_REQUEST() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeDraftEr("ER0001", "OP0001", "2026"));
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));

        ErCntryPairCo2VO vo = makeVO("KR", "JP", "JET-A", "N", "N", 100,
                new BigDecimal("-1"), new BigDecimal("3.16"));

        assertThatThrownBy(() -> erCntryPairCo2Service.add("ER0001", vo, airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));
    }

    @Test
    @DisplayName("add — convFctr <= 0 → BAD_REQUEST(400)")
    void add_convFctr_0이하_BAD_REQUEST() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeDraftEr("ER0001", "OP0001", "2026"));
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));

        ErCntryPairCo2VO vo = makeVO("KR", "JP", "JET-A", "N", "N", 100,
                new BigDecimal("50000"), BigDecimal.ZERO);

        assertThatThrownBy(() -> erCntryPairCo2Service.add("ER0001", vo, airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));
    }

    @Test
    @DisplayName("add — 같은 (dprtr, arvl, fuelType) 중복 → CONFLICT(409)")
    void add_국가쌍중복_CONFLICT() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeDraftEr("ER0001", "OP0001", "2026"));
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));
        given(erCntryPairCo2Mapper.existsByPair("ER0001", "KR", "JP", "JET-A", -1)).willReturn(true);

        assertThatThrownBy(() -> erCntryPairCo2Service.add("ER0001", makeValidVO(), airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(409));

        then(erCntryPairCo2Mapper).should(never()).insert(any());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // update — 자기 자신 제외 verify
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("update — 본인 수정 시 excludeSn=pairSn 으로 자기 자신 제외 verify")
    void update_자기자신_제외_verify() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeDraftEr("ER0001", "OP0001", "2026"));
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));
        given(erCntryPairCo2Mapper.selectOne("ER0001", 1)).willReturn(makeSavedPair("ER0001", 1, "KR", "JP"));
        given(erCntryPairCo2Mapper.existsByPair("ER0001", "KR", "US", "JET-A", 1)).willReturn(false);
        given(erCntryPairCo2Mapper.update(any(ErCntryPairCo2VO.class))).willReturn(1);

        ErCntryPairCo2VO vo = makeVO("KR", "US", "JET-A", "N", "N", 100,
                new BigDecimal("50000"), new BigDecimal("3.16"));
        erCntryPairCo2Service.update("ER0001", 1, vo, airlineUserOP0001);

        // existsByPair 가 pairSn=1 (자기 자신) 을 excludeSn 으로 전달했는지 검증
        then(erCntryPairCo2Mapper).should(times(1))
                .existsByPair("ER0001", "KR", "US", "JET-A", 1);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // softDelete
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("softDelete — 정상 소프트삭제")
    void softDelete_정상() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeDraftEr("ER0001", "OP0001", "2026"));
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));
        given(erCntryPairCo2Mapper.softDelete("ER0001", 1, "airline01")).willReturn(1);

        assertThatCode(() -> erCntryPairCo2Service.softDelete("ER0001", 1, airlineUserOP0001))
                .doesNotThrowAnyException();

        then(erCntryPairCo2Mapper).should(times(1)).softDelete("ER0001", 1, "airline01");
    }

    @Test
    @DisplayName("softDelete — 자식 미존재(affected=0) → NOT_FOUND(404)")
    void softDelete_자식미존재_NOT_FOUND() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeDraftEr("ER0001", "OP0001", "2026"));
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));
        given(erCntryPairCo2Mapper.softDelete("ER0001", 99, "airline01")).willReturn(0);

        assertThatThrownBy(() -> erCntryPairCo2Service.softDelete("ER0001", 99, airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(404));
    }
}
