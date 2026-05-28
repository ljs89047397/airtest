package kr.go.molit.icas.ptl.stat;

import kr.go.molit.icas.com.oprtr.OprtrMapper;
import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.DataScopeValidator;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.common.util.IdGenerator;
import kr.go.molit.icas.er.cef.CefMapper;
import kr.go.molit.icas.er.cef.domain.CefVO;
import kr.go.molit.icas.er.rprt.ErMapper;
import kr.go.molit.icas.er.rprt.domain.ErVO;
import kr.go.molit.icas.er.rprt.fuelsmry.ErFuelSmryMapper;
import kr.go.molit.icas.er.rprt.fuelsmry.domain.ErFuelSmryVO;
import kr.go.molit.icas.saf.airprt.purch.SafAirprtPurchMapper;
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
@DisplayName("PtlStatService 단위 테스트")
class PtlStatServiceTest {

    @Mock PtlStatYearlyMapper  statMapper;
    @Mock ErMapper             erMapper;
    @Mock CefMapper            cefMapper;
    @Mock SafAirprtPurchMapper safPurchMapper;
    @Mock ErFuelSmryMapper     fuelSmryMapper;
    @Mock OprtrMapper          oprtrMapper;
    @Mock DataScopeValidator   dataScopeValidator;

    @InjectMocks
    PtlStatService statService;

    private IcasUser molitUser;
    private IcasUser kotsaUser;
    private IcasUser airlineUser;
    private IcasUser verifierUser;

    @BeforeEach
    void setUp() {
        molitUser = IcasUser.builder()
                .userId("molit01").ognzSeCd("MOLIT").master(false)
                .roleIds(List.of("ADMIN")).build();

        kotsaUser = IcasUser.builder()
                .userId("kotsa01").ognzSeCd("KOTSA").master(false)
                .roleIds(List.of("ADMIN")).build();

        airlineUser = IcasUser.builder()
                .userId("airline01").ognzSeCd("AIRLINE").oprtrId("OP0001").master(false)
                .roleIds(List.of("AIRLINE_USER")).build();

        verifierUser = IcasUser.builder()
                .userId("verifier01").ognzSeCd("VERIFIER").master(false)
                .roleIds(List.of("VERIFIER_USER")).build();
    }

    // ─── Case 1: aggregateOne — APRVD ER 합산 → upsertStat 호출 ─────────────

    @Test
    @DisplayName("Case 1: aggregateOne — APRVD ER+CEF 합산 후 upsertStat 호출 확인")
    void aggregateOne_APRVD_ER_합산_upsertStat_호출() {
        ErVO er = new ErVO(); er.setErId("ER0001");
        ErFuelSmryVO fs = new ErFuelSmryVO();
        fs.setTtlCo2Emsn(new BigDecimal("1000.0000"));
        fs.setTtlFuelWght(new BigDecimal("500.0000"));

        CefVO cef = new CefVO();
        cef.setTtlReduAmt(new BigDecimal("50.0000"));

        PtlStatYearlyVO saved = new PtlStatYearlyVO();
        saved.setRprtYr("2026"); saved.setOprtrId("OP0001");
        saved.setTtlCo2Emsn(new BigDecimal("1000.0000"));

        given(erMapper.selectErs(any())).willReturn(List.of(er));
        given(fuelSmryMapper.selectByErId("ER0001")).willReturn(List.of(fs));
        given(cefMapper.selectCefs(any())).willReturn(List.of(cef));
        given(safPurchMapper.sumPurchQty("OP0001", "2026")).willReturn(new BigDecimal("100.0"));
        given(statMapper.selectByPk("2026", "OP0001")).willReturn(saved);

        PtlStatYearlyVO result = statService.aggregateOne("2026", "OP0001", molitUser);

        then(statMapper).should().upsertStat(any());
        assertThat(result.getTtlCo2Emsn()).isEqualByComparingTo("1000.0000");
    }

    // ─── Case 2: aggregateOne — ER 없음 → 0으로 집계 (예외 없음) ─────────────

    @Test
    @DisplayName("Case 2: aggregateOne — APRVD ER 없음 → 0으로 집계 (예외 없음)")
    void aggregateOne_ER없음_0으로집계() {
        PtlStatYearlyVO saved = new PtlStatYearlyVO();
        saved.setRprtYr("2026"); saved.setOprtrId("OP0001");
        saved.setTtlCo2Emsn(BigDecimal.ZERO);

        given(erMapper.selectErs(any())).willReturn(List.of());
        given(cefMapper.selectCefs(any())).willReturn(List.of());
        given(safPurchMapper.sumPurchQty("OP0001", "2026")).willReturn(null);
        given(statMapper.selectByPk("2026", "OP0001")).willReturn(saved);

        assertThatCode(() -> statService.aggregateOne("2026", "OP0001", molitUser))
                .doesNotThrowAnyException();

        then(statMapper).should().upsertStat(any());
    }

    // ─── Case 3: listByRprtYr — MOLIT → 전체 반환 ──────────────────────────

    @Test
    @DisplayName("Case 3: listByRprtYr — MOLIT → 전체 운영사 목록 반환")
    void listByRprtYr_MOLIT_전체반환() {
        PtlStatYearlyVO s1 = new PtlStatYearlyVO(); s1.setOprtrId("OP0001");
        PtlStatYearlyVO s2 = new PtlStatYearlyVO(); s2.setOprtrId("OP0002");
        given(statMapper.selectByRprtYr("2026")).willReturn(List.of(s1, s2));

        List<PtlStatYearlyVO> result = statService.listByRprtYr("2026", molitUser);

        assertThat(result).hasSize(2);
        then(statMapper).should().selectByRprtYr("2026");
    }

    // ─── Case 4: listByRprtYr — AIRLINE → assertOprtrAccessible 호출 ────────

    @Test
    @DisplayName("Case 4: listByRprtYr — AIRLINE → DataScopeValidator 호출 후 자기 것만 반환")
    void listByRprtYr_AIRLINE_자기것만() {
        PtlStatYearlyVO myVO = new PtlStatYearlyVO();
        myVO.setOprtrId("OP0001");
        given(statMapper.selectByPk("2026", "OP0001")).willReturn(myVO);

        List<PtlStatYearlyVO> result = statService.listByRprtYr("2026", airlineUser);

        then(dataScopeValidator).should().assertOprtrAccessible(airlineUser, "OP0001", "2026");
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOprtrId()).isEqualTo("OP0001");
    }

    // ─── Case 5: getStat — 미존재 → BusinessException.notFound ──────────────

    @Test
    @DisplayName("Case 5: getStat — 미존재 → NOT_FOUND(404)")
    void getStat_미존재_NOT_FOUND() {
        given(statMapper.selectByPk("2026", "OP9999")).willReturn(null);

        assertThatThrownBy(() -> statService.getStat("2026", "OP9999", molitUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(404));
    }

    // ─── Case 6: aggregateAll — VERIFIER → forbidden ─────────────────────────

    @Test
    @DisplayName("Case 6: aggregateAll — VERIFIER → FORBIDDEN(403)")
    void aggregateAll_VERIFIER_forbidden() {
        assertThatThrownBy(() -> statService.aggregateAll("2026", verifierUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(403));
    }

    // ─── Case 7: aggregateAll — KOTSA → 정상 실행 ───────────────────────────

    @Test
    @DisplayName("Case 7: aggregateAll — KOTSA → 전체 운영사 집계 정상 실행")
    void aggregateAll_KOTSA_정상실행() {
        kr.go.molit.icas.com.oprtr.domain.OprtrVO op1 = new kr.go.molit.icas.com.oprtr.domain.OprtrVO();
        op1.setOprtrId("OP0001");
        given(oprtrMapper.selectAll()).willReturn(List.of(op1));
        given(erMapper.selectErs(any())).willReturn(List.of());
        given(cefMapper.selectCefs(any())).willReturn(List.of());
        given(safPurchMapper.sumPurchQty(any(), any())).willReturn(BigDecimal.ZERO);
        given(statMapper.selectByPk(any(), any())).willReturn(new PtlStatYearlyVO());

        assertThatCode(() -> statService.aggregateAll("2026", kotsaUser))
                .doesNotThrowAnyException();

        then(statMapper).should(atLeastOnce()).upsertStat(any());
    }

    // ─── Case 8: getStat — AIRLINE 자기 것 → 정상 조회 ─────────────────────

    @Test
    @DisplayName("Case 8: getStat — AIRLINE 자기 운영사 조회 → 정상 반환")
    void getStat_AIRLINE_자기것_정상조회() {
        PtlStatYearlyVO myVO = new PtlStatYearlyVO();
        myVO.setRprtYr("2026");
        myVO.setOprtrId("OP0001");
        given(statMapper.selectByPk("2026", "OP0001")).willReturn(myVO);

        PtlStatYearlyVO result = statService.getStat("2026", "OP0001", airlineUser);

        then(dataScopeValidator).should().assertOprtrAccessible(airlineUser, "OP0001", "2026");
        assertThat(result.getOprtrId()).isEqualTo("OP0001");
    }
}
