package kr.go.molit.icas.saf.mntr;

import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.DataScopeValidator;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.saf.airprt.fuel.SafAirprtFuelMapper;
import kr.go.molit.icas.saf.airprt.purch.SafAirprtPurchMapper;
import kr.go.molit.icas.saf.mntr.domain.SafBlndMntrVO;
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
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SafBlndMntrService 단위 테스트 — 혼합비율 자동 산출 (SFR-046)")
class SafBlndMntrServiceTest {

    @Mock SafBlndMntrMapper    mntrMapper;
    @Mock SafAirprtFuelMapper  fuelMapper;
    @Mock SafAirprtPurchMapper purchMapper;
    @Mock DataScopeValidator   dataScopeValidator;

    @InjectMocks SafBlndMntrService mntrService;

    private IcasUser airline;
    private IcasUser kotsa;
    private IcasUser verifier;

    @BeforeEach
    void setUp() {
        airline = IcasUser.builder()
                .userId("airline01").ognzSeCd("AIRLINE").oprtrId("OP0001").master(false)
                .roleIds(List.of("AIRLINE_USER")).build();
        kotsa = IcasUser.builder()
                .userId("kotsa01").ognzSeCd("KOTSA").master(false)
                .roleIds(List.of("KOTSA_REVIEWER")).build();
        verifier = IcasUser.builder()
                .userId("vrf01").ognzSeCd("VERIFIER").vrfcnInstId("VI0001").master(false)
                .roleIds(List.of("VERIFIER_USER")).build();
    }

    // ── runCalc ──────────────────────────────────────────

    @Test
    @DisplayName("runCalc: 총 급유량 10000, SAF 구매 150 → 비율 1.5% ≥ 1.0% → Y")
    void runCalc_이행완료_Y() {
        given(fuelMapper.sumActlFuelQty("OP0001", "2028")).willReturn(new BigDecimal("10000"));
        given(purchMapper.sumPurchQty("OP0001", "2028")).willReturn(new BigDecimal("150"));
        given(mntrMapper.selectByPk("OP0001", "2028")).willReturn(null).willReturn(expectedVo("Y", "1.5000"));

        SafBlndMntrVO result = mntrService.runCalc("OP0001", "2028", kotsa);

        assertThat(result.getFulfilledYn()).isEqualTo("Y");
        verify(mntrMapper).insertMntr(any(SafBlndMntrVO.class));
    }

    @Test
    @DisplayName("runCalc: 총 급유량 10000, SAF 구매 80 → 비율 0.8% < 1.0% → N")
    void runCalc_미이행_N() {
        given(fuelMapper.sumActlFuelQty("OP0001", "2028")).willReturn(new BigDecimal("10000"));
        given(purchMapper.sumPurchQty("OP0001", "2028")).willReturn(new BigDecimal("80"));
        given(mntrMapper.selectByPk("OP0001", "2028")).willReturn(null).willReturn(expectedVo("N", "0.8000"));

        SafBlndMntrVO result = mntrService.runCalc("OP0001", "2028", airline);

        assertThat(result.getFulfilledYn()).isEqualTo("N");
    }

    @Test
    @DisplayName("runCalc: 총 급유량 0 → 비율 0.0% → N")
    void runCalc_급유량0_N() {
        given(fuelMapper.sumActlFuelQty("OP0001", "2028")).willReturn(BigDecimal.ZERO);
        given(purchMapper.sumPurchQty("OP0001", "2028")).willReturn(new BigDecimal("100"));
        given(mntrMapper.selectByPk("OP0001", "2028")).willReturn(null).willReturn(expectedVo("N", "0.0000"));

        SafBlndMntrVO result = mntrService.runCalc("OP0001", "2028", kotsa);

        assertThat(result.getFulfilledYn()).isEqualTo("N");
    }

    @Test
    @DisplayName("runCalc: 기존 데이터 있으면 updateMntr 호출")
    void runCalc_기존데이터_update() {
        given(fuelMapper.sumActlFuelQty("OP0001", "2028")).willReturn(new BigDecimal("10000"));
        given(purchMapper.sumPurchQty("OP0001", "2028")).willReturn(new BigDecimal("200"));
        SafBlndMntrVO existing = expectedVo("Y", "1.0000");
        SafBlndMntrVO updated  = expectedVo("Y", "2.0000");
        given(mntrMapper.selectByPk("OP0001", "2028")).willReturn(existing).willReturn(updated);

        mntrService.runCalc("OP0001", "2028", kotsa);

        verify(mntrMapper).updateMntr(any(SafBlndMntrVO.class));
        verify(mntrMapper, never()).insertMntr(any());
    }

    @Test
    @DisplayName("listAll: VERIFIER → FORBIDDEN(403)")
    void listAll_VERIFIER_FORBIDDEN() {
        assertThatThrownBy(() -> mntrService.listByRprtYr("2028", verifier))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(403));
    }

    private SafBlndMntrVO expectedVo(String fulfilledYn, String blndRatio) {
        SafBlndMntrVO vo = new SafBlndMntrVO();
        vo.setOprtrId("OP0001");
        vo.setRprtYr("2028");
        vo.setFulfilledYn(fulfilledYn);
        vo.setBlndRatio(new BigDecimal(blndRatio));
        vo.setOblgRatio(new BigDecimal("1.0"));
        return vo;
    }
}
