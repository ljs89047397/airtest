package kr.go.molit.icas.er.eucr;

import kr.go.molit.icas.com.oprtr.OprtrMapper;
import kr.go.molit.icas.com.oprtr.domain.OprtrVO;
import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.DataScopeValidator;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.common.util.IdGenerator;
import kr.go.molit.icas.er.eucr.domain.EucrVO;
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
@DisplayName("EucrService 단위 테스트 — 의무량 충족 판정 + 상태기")
class EucrServiceTest {

    @Mock EucrMapper         eucrMapper;
    @Mock OprtrMapper        oprtrMapper;
    @Mock DataScopeValidator dataScopeValidator;
    @Mock IdGenerator        idGenerator;

    @InjectMocks
    EucrService eucrService;

    private IcasUser airline;
    private IcasUser kotsa;
    private IcasUser molit;

    @BeforeEach
    void setUp() {
        airline = IcasUser.builder()
                .userId("airline01").ognzSeCd("AIRLINE").oprtrId("OP0001").master(false)
                .roleIds(List.of("AIRLINE_USER")).build();
        kotsa = IcasUser.builder()
                .userId("kotsa01").ognzSeCd("KOTSA").master(false)
                .roleIds(List.of("KOTSA_REVIEWER")).build();
        molit = IcasUser.builder()
                .userId("molit01").ognzSeCd("MOLIT").master(false)
                .roleIds(List.of("MOLIT_ADMIN")).build();
    }

    private EucrVO makeEucr(String eucrId, String stCd, BigDecimal ofstReqQty) {
        EucrVO e = new EucrVO();
        e.setEucrId(eucrId);
        e.setOprtrId("OP0001");
        e.setRprtYr("2026");
        e.setEucrVer("1.0");
        e.setEucrStCd(stCd);
        e.setOfstReqQty(ofstReqQty);
        return e;
    }

    // ── createEucr ─────────────────────────────────────────

    @Test
    @DisplayName("createEucr 정상")
    void createEucr_정상() {
        EucrVO input = new EucrVO();
        input.setOprtrId("OP0001");
        input.setRprtYr("2026");
        input.setOfstReqQty(new BigDecimal("1000"));

        willDoNothing().given(dataScopeValidator).assertOwnAirline(airline, "OP0001");
        given(oprtrMapper.selectByOprtrId("OP0001")).willReturn(new OprtrVO());
        given(eucrMapper.countByPrefix("EUCR")).willReturn(0);
        given(idGenerator.managementPk("EUCR", 1)).willReturn("EUCR0001");
        given(eucrMapper.selectMaxEucrVer("OP0001", "2026")).willReturn(null);
        given(eucrMapper.selectByEucrId("EUCR0001"))
                .willReturn(makeEucr("EUCR0001", "DRAFT", new BigDecimal("1000")));

        EucrVO result = eucrService.createEucr(input, airline);

        assertThat(result.getEucrId()).isEqualTo("EUCR0001");
        assertThat(result.getEucrVer()).isEqualTo("1.0");
        verify(eucrMapper).insertEucr(any(EucrVO.class));
    }

    @Test
    @DisplayName("createEucr: 보고연도 4자리 아님 → BAD_REQUEST(400)")
    void createEucr_rprtYr잘못_BAD_REQUEST() {
        EucrVO input = new EucrVO();
        input.setOprtrId("OP0001");
        input.setRprtYr("26");
        willDoNothing().given(dataScopeValidator).assertOwnAirline(airline, "OP0001");

        assertThatThrownBy(() -> eucrService.createEucr(input, airline))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));
    }

    // ── recalcTtlAndFulfilled — 의무량 판정 ───────────────────

    @Test
    @DisplayName("recalc: ttl_qty(1500) >= ofst_req_qty(1000) → fulfilled_yn='Y'")
    void recalc_충족_Y() {
        given(eucrMapper.selectByEucrId("EUCR0001"))
                .willReturn(makeEucr("EUCR0001", "DRAFT", new BigDecimal("1000")));
        willDoNothing().given(dataScopeValidator).assertOwnAirline(airline, "OP0001");
        given(eucrMapper.sumBatchQty("EUCR0001")).willReturn(new BigDecimal("1500"));

        eucrService.recalcTtlAndFulfilled("EUCR0001", airline);

        verify(eucrMapper).updateTtlQtyAndFulfilled(
                eq("EUCR0001"), eq(new BigDecimal("1500")), eq("Y"), eq("airline01"));
    }

    @Test
    @DisplayName("recalc: ttl_qty(800) < ofst_req_qty(1000) → fulfilled_yn='N'")
    void recalc_미충족_N() {
        given(eucrMapper.selectByEucrId("EUCR0001"))
                .willReturn(makeEucr("EUCR0001", "DRAFT", new BigDecimal("1000")));
        willDoNothing().given(dataScopeValidator).assertOwnAirline(airline, "OP0001");
        given(eucrMapper.sumBatchQty("EUCR0001")).willReturn(new BigDecimal("800"));

        eucrService.recalcTtlAndFulfilled("EUCR0001", airline);

        verify(eucrMapper).updateTtlQtyAndFulfilled(
                eq("EUCR0001"), eq(new BigDecimal("800")), eq("N"), eq("airline01"));
    }

    @Test
    @DisplayName("recalc: 정확히 일치(=) → fulfilled_yn='Y'")
    void recalc_정확일치_Y() {
        given(eucrMapper.selectByEucrId("EUCR0001"))
                .willReturn(makeEucr("EUCR0001", "DRAFT", new BigDecimal("1000")));
        willDoNothing().given(dataScopeValidator).assertOwnAirline(airline, "OP0001");
        given(eucrMapper.sumBatchQty("EUCR0001")).willReturn(new BigDecimal("1000"));

        eucrService.recalcTtlAndFulfilled("EUCR0001", airline);

        verify(eucrMapper).updateTtlQtyAndFulfilled(
                eq("EUCR0001"), eq(new BigDecimal("1000")), eq("Y"), eq("airline01"));
    }

    @Test
    @DisplayName("recalc: ofst_req_qty=0 + ttl_qty=0 → fulfilled='Y' (0 >= 0)")
    void recalc_0대0_Y() {
        given(eucrMapper.selectByEucrId("EUCR0001"))
                .willReturn(makeEucr("EUCR0001", "DRAFT", BigDecimal.ZERO));
        willDoNothing().given(dataScopeValidator).assertOwnAirline(airline, "OP0001");
        given(eucrMapper.sumBatchQty("EUCR0001")).willReturn(BigDecimal.ZERO);

        eucrService.recalcTtlAndFulfilled("EUCR0001", airline);

        verify(eucrMapper).updateTtlQtyAndFulfilled(
                eq("EUCR0001"), eq(BigDecimal.ZERO), eq("Y"), eq("airline01"));
    }

    // ── 상태 전이 ───────────────────────────────────────────

    @Test
    @DisplayName("approve: AIRLINE → FORBIDDEN(403)")
    void approve_AIRLINE_FORBIDDEN() {
        assertThatThrownBy(() -> eucrService.approve("EUCR0001", airline))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(403));
    }

    @Test
    @DisplayName("approve: KOTSA → FORBIDDEN(403) (MOLIT 만 가능)")
    void approve_KOTSA_FORBIDDEN() {
        assertThatThrownBy(() -> eucrService.approve("EUCR0001", kotsa))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(403));
    }

    @Test
    @DisplayName("approve: MOLIT, RCMDD → APRVD")
    void approve_MOLIT_RCMDD_정상() {
        given(eucrMapper.selectByEucrId("EUCR0001"))
                .willReturn(makeEucr("EUCR0001", "RCMDD", new BigDecimal("1000")));
        given(eucrMapper.updateApprove("EUCR0001", "molit01")).willReturn(1);

        eucrService.approve("EUCR0001", molit);

        verify(eucrMapper).updateApprove("EUCR0001", "molit01");
    }

    @Test
    @DisplayName("approve: MOLIT, DRAFT → BAD_REQUEST(400)")
    void approve_MOLIT_DRAFT_BAD_REQUEST() {
        given(eucrMapper.selectByEucrId("EUCR0001"))
                .willReturn(makeEucr("EUCR0001", "DRAFT", new BigDecimal("1000")));

        assertThatThrownBy(() -> eucrService.approve("EUCR0001", molit))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));
    }

    @Test
    @DisplayName("review: KOTSA, SBMTD → RVWNG")
    void review_KOTSA_정상() {
        given(eucrMapper.selectByEucrId("EUCR0001"))
                .willReturn(makeEucr("EUCR0001", "SBMTD", new BigDecimal("1000")));
        given(eucrMapper.updateEucrStCd("EUCR0001", "RVWNG", "kotsa01")).willReturn(1);

        eucrService.review("EUCR0001", kotsa);

        verify(eucrMapper).updateEucrStCd("EUCR0001", "RVWNG", "kotsa01");
    }

    @Test
    @DisplayName("reject: KOTSA, RVWNG + 사유 → DRAFT")
    void reject_KOTSA_정상() {
        given(eucrMapper.selectByEucrId("EUCR0001"))
                .willReturn(makeEucr("EUCR0001", "RVWNG", new BigDecimal("1000")));
        given(eucrMapper.updateEucrStCd("EUCR0001", "DRAFT", "kotsa01")).willReturn(1);

        eucrService.reject("EUCR0001", "보완 필요", kotsa);

        verify(eucrMapper).updateEucrStCd("EUCR0001", "DRAFT", "kotsa01");
    }

    @Test
    @DisplayName("reject: 사유 누락 → BAD_REQUEST(400)")
    void reject_사유누락_BAD_REQUEST() {
        assertThatThrownBy(() -> eucrService.reject("EUCR0001", "", kotsa))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));
    }
}
