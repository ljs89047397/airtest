package kr.go.molit.icas.vr.cncls;

import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.vr.VrService;
import kr.go.molit.icas.vr.cncls.domain.VrCnclsVO;
import kr.go.molit.icas.vr.domain.VrVO;
import kr.go.molit.icas.vr.ncnfrm.VrNcnfrmService;
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
@DisplayName("VrCnclsService 단위 테스트 — 최종의견 + 미해결부적합 차단 (SFR-027)")
class VrCnclsServiceTest {

    @Mock VrCnclsMapper  vrCnclsMapper;
    @Mock VrService      vrService;
    @Mock VrNcnfrmService vrNcnfrmService;

    @InjectMocks
    VrCnclsService vrCnclsService;

    private IcasUser verifier;

    @BeforeEach
    void setUp() {
        verifier = IcasUser.builder()
                .userId("vrf01").ognzSeCd("VERIFIER").vrfcnInstId("VI0001").master(false)
                .roleIds(List.of("VERIFIER_USER")).build();
    }

    private VrVO makeVr(String vrId) {
        VrVO v = new VrVO();
        v.setVrId(vrId);
        v.setOprtrId("OP0001");
        v.setRprtYr("2026");
        v.setVrStCd("DRAFT");
        v.setVrfcnInstId("VI0001");
        return v;
    }

    private VrCnclsVO makeCncls(String vrId, String opnnCd) {
        VrCnclsVO c = new VrCnclsVO();
        c.setVrId(vrId);
        c.setFinalOpnnCd(opnnCd);
        c.setErCncls("배출량 검증 완료");
        c.setDataQltyEval("양호");
        c.setMtrltyEval("중요성 기준 충족");
        c.setJudgCn("전체적으로 적정");
        return c;
    }

    // ── REASONABLE 차단 ──────────────────────────────────────

    @Test
    @DisplayName("REASONABLE: 미해결 부적합 2건 → BAD_REQUEST(400)")
    void reasonable_미해결존재_BAD_REQUEST() {
        given(vrService.assertVrDraftForChildEdit("VR0001", verifier)).willReturn(makeVr("VR0001"));
        given(vrNcnfrmService.countUnresolved("VR0001")).willReturn(2);

        VrCnclsVO vo = makeCncls("VR0001", "REASONABLE");

        assertThatThrownBy(() -> vrCnclsService.saveOrUpdate("VR0001", vo, verifier))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));

        verify(vrCnclsMapper, never()).insertCncls(any());
        verify(vrCnclsMapper, never()).updateCncls(any());
    }

    @Test
    @DisplayName("REASONABLE: 미해결 0건 → 정상 저장")
    void reasonable_미해결없음_정상() {
        given(vrService.assertVrDraftForChildEdit("VR0001", verifier)).willReturn(makeVr("VR0001"));
        given(vrNcnfrmService.countUnresolved("VR0001")).willReturn(0);
        given(vrCnclsMapper.selectByVrId("VR0001")).willReturn(null);

        VrCnclsVO vo = makeCncls("VR0001", "REASONABLE");
        VrCnclsVO saved = makeCncls("VR0001", "REASONABLE");
        given(vrCnclsMapper.selectByVrId("VR0001")).willReturn(null).willReturn(saved);

        VrCnclsVO result = vrCnclsService.saveOrUpdate("VR0001", vo, verifier);

        assertThat(result.getFinalOpnnCd()).isEqualTo("REASONABLE");
        verify(vrCnclsMapper).insertCncls(any(VrCnclsVO.class));
    }

    // ── 다른 의견 코드 ───────────────────────────────────────

    @Test
    @DisplayName("LIMITED: 미해결 부적합 있어도 허용")
    void limited_미해결존재_허용() {
        given(vrService.assertVrDraftForChildEdit("VR0001", verifier)).willReturn(makeVr("VR0001"));
        given(vrCnclsMapper.selectByVrId("VR0001")).willReturn(null);
        VrCnclsVO saved = makeCncls("VR0001", "LIMITED");
        given(vrCnclsMapper.selectByVrId("VR0001")).willReturn(null).willReturn(saved);

        VrCnclsVO vo = makeCncls("VR0001", "LIMITED");
        VrCnclsVO result = vrCnclsService.saveOrUpdate("VR0001", vo, verifier);

        // LIMITED 는 countUnresolved 를 호출하지 않아야 함
        verify(vrNcnfrmService, never()).countUnresolved(any());
        assertThat(result.getFinalOpnnCd()).isEqualTo("LIMITED");
    }

    @Test
    @DisplayName("ADVERSE: 기존 데이터 있으면 update 호출")
    void adverse_기존데이터_update() {
        given(vrService.assertVrDraftForChildEdit("VR0001", verifier)).willReturn(makeVr("VR0001"));
        VrCnclsVO existing = makeCncls("VR0001", "LIMITED");
        VrCnclsVO updated  = makeCncls("VR0001", "ADVERSE");
        given(vrCnclsMapper.selectByVrId("VR0001")).willReturn(existing).willReturn(updated);

        VrCnclsVO vo = makeCncls("VR0001", "ADVERSE");
        VrCnclsVO result = vrCnclsService.saveOrUpdate("VR0001", vo, verifier);

        verify(vrCnclsMapper).updateCncls(any(VrCnclsVO.class));
        verify(vrCnclsMapper, never()).insertCncls(any());
        assertThat(result.getFinalOpnnCd()).isEqualTo("ADVERSE");
    }

    // ── 유효하지 않은 의견 코드 ─────────────────────────────

    @Test
    @DisplayName("finalOpnnCd = INVALID → BAD_REQUEST(400)")
    void invalid_opnn_BAD_REQUEST() {
        given(vrService.assertVrDraftForChildEdit("VR0001", verifier)).willReturn(makeVr("VR0001"));

        VrCnclsVO vo = makeCncls("VR0001", "INVALID");

        assertThatThrownBy(() -> vrCnclsService.saveOrUpdate("VR0001", vo, verifier))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));
    }

    @Test
    @DisplayName("finalOpnnCd = null → BAD_REQUEST(400)")
    void null_opnn_BAD_REQUEST() {
        given(vrService.assertVrDraftForChildEdit("VR0001", verifier)).willReturn(makeVr("VR0001"));

        VrCnclsVO vo = makeCncls("VR0001", null);

        assertThatThrownBy(() -> vrCnclsService.saveOrUpdate("VR0001", vo, verifier))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));
    }

    // ── QUALIFIED 정상 저장 ──────────────────────────────────

    @Test
    @DisplayName("QUALIFIED: 정상 저장")
    void qualified_정상() {
        given(vrService.assertVrDraftForChildEdit("VR0001", verifier)).willReturn(makeVr("VR0001"));
        VrCnclsVO saved = makeCncls("VR0001", "QUALIFIED");
        given(vrCnclsMapper.selectByVrId("VR0001")).willReturn(null).willReturn(saved);

        VrCnclsVO vo = makeCncls("VR0001", "QUALIFIED");
        VrCnclsVO result = vrCnclsService.saveOrUpdate("VR0001", vo, verifier);

        verify(vrNcnfrmService, never()).countUnresolved(any());
        assertThat(result.getFinalOpnnCd()).isEqualTo("QUALIFIED");
    }
}
