package kr.go.molit.icas.er.cef;

import kr.go.molit.icas.com.oprtr.OprtrMapper;
import kr.go.molit.icas.com.oprtr.domain.OprtrVO;
import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.DataScopeValidator;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.common.util.IdGenerator;
import kr.go.molit.icas.er.cef.domain.CefVO;
import kr.go.molit.icas.er.rprt.ErMapper;
import kr.go.molit.icas.er.rprt.domain.ErVO;
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
@DisplayName("CefService 단위 테스트 — 마스터 흐름")
class CefServiceTest {

    @Mock CefMapper          cefMapper;
    @Mock ErMapper           erMapper;
    @Mock OprtrMapper        oprtrMapper;
    @Mock DataScopeValidator dataScopeValidator;
    @Mock IdGenerator        idGenerator;

    @InjectMocks
    CefService cefService;

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

    private ErVO makeEr(String erId, String oprtrId, String rprtYr) {
        ErVO er = new ErVO();
        er.setErId(erId);
        er.setOprtrId(oprtrId);
        er.setRprtYr(rprtYr);
        er.setErStCd("DRAFT");
        return er;
    }

    private CefVO makeCef(String cefId, String erId, String oprtrId, String rprtYr, String stCd) {
        CefVO c = new CefVO();
        c.setCefId(cefId);
        c.setErId(erId);
        c.setOprtrId(oprtrId);
        c.setRprtYr(rprtYr);
        c.setCefStCd(stCd);
        return c;
    }

    // ── createCef ─────────────────────────────────────────

    @Test
    @DisplayName("createCef 정상: ER 기반 oprtrId/rprtYr 도출 + cef_id 자동 채번")
    void createCef_정상() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeEr("ER0001", "OP0001", "2026"));
        willDoNothing().given(dataScopeValidator).assertOwnAirline(airline, "OP0001");
        given(oprtrMapper.selectByOprtrId("OP0001")).willReturn(new OprtrVO());
        given(cefMapper.selectByErId("ER0001")).willReturn(null);
        given(cefMapper.countByPrefix("CEF")).willReturn(0);
        given(idGenerator.managementPk("CEF", 1)).willReturn("CEF0001");
        given(cefMapper.selectByCefId("CEF0001")).willReturn(makeCef("CEF0001", "ER0001", "OP0001", "2026", "DRAFT"));

        CefVO result = cefService.createCef("ER0001", airline);

        assertThat(result.getCefId()).isEqualTo("CEF0001");
        assertThat(result.getOprtrId()).isEqualTo("OP0001");
        assertThat(result.getRprtYr()).isEqualTo("2026");
        verify(cefMapper).insertCef(any(CefVO.class));
    }

    @Test
    @DisplayName("createCef: 동일 ER 에 CEF 중복 → CONFLICT(409)")
    void createCef_중복_CONFLICT() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeEr("ER0001", "OP0001", "2026"));
        willDoNothing().given(dataScopeValidator).assertOwnAirline(airline, "OP0001");
        given(oprtrMapper.selectByOprtrId("OP0001")).willReturn(new OprtrVO());
        given(cefMapper.selectByErId("ER0001"))
                .willReturn(makeCef("CEF0099", "ER0001", "OP0001", "2026", "DRAFT"));

        assertThatThrownBy(() -> cefService.createCef("ER0001", airline))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(409));
    }

    @Test
    @DisplayName("createCef: ER 미존재 → NOT_FOUND(404)")
    void createCef_ER미존재_NOT_FOUND() {
        given(erMapper.selectByErId("ER9999")).willReturn(null);

        assertThatThrownBy(() -> cefService.createCef("ER9999", airline))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(404));
    }

    // ── recalcTtlRedu ─────────────────────────────────────

    @Test
    @DisplayName("recalcTtlRedu: 자식 합계로 마스터 갱신")
    void recalcTtlRedu_정상() {
        given(cefMapper.selectByCefId("CEF0001"))
                .willReturn(makeCef("CEF0001", "ER0001", "OP0001", "2026", "DRAFT"));
        willDoNothing().given(dataScopeValidator).assertOwnAirline(airline, "OP0001");
        given(cefMapper.sumClaimMass("CEF0001")).willReturn(new BigDecimal("12345.6789"));

        BigDecimal sum = cefService.recalcTtlRedu("CEF0001", airline);

        assertThat(sum.compareTo(new BigDecimal("12345.6789"))).isEqualTo(0);
        verify(cefMapper).updateTtlReduAmt(eq("CEF0001"), eq(new BigDecimal("12345.6789")), eq("airline01"));
    }

    @Test
    @DisplayName("recalcTtlRedu: 자식 0건 → 0 으로 갱신")
    void recalcTtlRedu_자식0건() {
        given(cefMapper.selectByCefId("CEF0001"))
                .willReturn(makeCef("CEF0001", "ER0001", "OP0001", "2026", "DRAFT"));
        willDoNothing().given(dataScopeValidator).assertOwnAirline(airline, "OP0001");
        given(cefMapper.sumClaimMass("CEF0001")).willReturn(null);

        BigDecimal sum = cefService.recalcTtlRedu("CEF0001", airline);

        assertThat(sum).isEqualTo(BigDecimal.ZERO);
        verify(cefMapper).updateTtlReduAmt(eq("CEF0001"), eq(BigDecimal.ZERO), eq("airline01"));
    }

    // ── 상태 전이 ─────────────────────────────────────────

    @Test
    @DisplayName("submit: DRAFT → SBMTD + 합계 재계산 호출")
    void submit_정상() {
        given(cefMapper.selectByCefId("CEF0001"))
                .willReturn(makeCef("CEF0001", "ER0001", "OP0001", "2026", "DRAFT"));
        willDoNothing().given(dataScopeValidator).assertOwnAirline(airline, "OP0001");
        given(cefMapper.sumClaimMass("CEF0001")).willReturn(new BigDecimal("100"));
        given(cefMapper.updateSubmit("CEF0001", "airline01")).willReturn(1);

        cefService.submit("CEF0001", airline);

        verify(cefMapper).updateTtlReduAmt("CEF0001", new BigDecimal("100"), "airline01");
        verify(cefMapper).updateSubmit("CEF0001", "airline01");
    }

    @Test
    @DisplayName("submit: 이미 SBMTD → BAD_REQUEST(400)")
    void submit_이미SBMTD_BAD_REQUEST() {
        given(cefMapper.selectByCefId("CEF0001"))
                .willReturn(makeCef("CEF0001", "ER0001", "OP0001", "2026", "SBMTD"));
        willDoNothing().given(dataScopeValidator).assertOwnAirline(airline, "OP0001");

        assertThatThrownBy(() -> cefService.submit("CEF0001", airline))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));
    }

    @Test
    @DisplayName("approve: AIRLINE 사용자 → FORBIDDEN(403)")
    void approve_AIRLINE_FORBIDDEN() {
        assertThatThrownBy(() -> cefService.approve("CEF0001", airline))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(403));
    }

    @Test
    @DisplayName("approve: KOTSA, SBMTD → APRVD")
    void approve_KOTSA_정상() {
        given(cefMapper.selectByCefId("CEF0001"))
                .willReturn(makeCef("CEF0001", "ER0001", "OP0001", "2026", "SBMTD"));
        given(cefMapper.updateApprove("CEF0001", "kotsa01")).willReturn(1);

        cefService.approve("CEF0001", kotsa);

        verify(cefMapper).updateApprove("CEF0001", "kotsa01");
    }

    @Test
    @DisplayName("cancel: MOLIT, 사유 없으면 BAD_REQUEST(400)")
    void cancel_사유누락_BAD_REQUEST() {
        assertThatThrownBy(() -> cefService.cancel("CEF0001", "", molit))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));
    }

    @Test
    @DisplayName("cancel: MOLIT, APRVD → CNCLD")
    void cancel_MOLIT_정상() {
        given(cefMapper.selectByCefId("CEF0001"))
                .willReturn(makeCef("CEF0001", "ER0001", "OP0001", "2026", "APRVD"));
        given(cefMapper.updateCancel("CEF0001", "사유1", "molit01")).willReturn(1);

        cefService.cancel("CEF0001", "사유1", molit);

        verify(cefMapper).updateCancel("CEF0001", "사유1", "molit01");
    }
}
