package kr.go.molit.icas.vr;

import kr.go.molit.icas.com.vrfcn.VrfcnAssgnMapper;
import kr.go.molit.icas.com.vrfcn.VrfcnInstMapper;
import kr.go.molit.icas.com.vrfcn.domain.VrfcnInstVO;
import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.DataScopeValidator;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.common.util.IdGenerator;
import kr.go.molit.icas.vr.domain.VrVO;
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
@DisplayName("VrService 단위 테스트 — 라이프사이클 + CCR 만료 + 권한")
class VrServiceTest {

    @Mock VrMapper           vrMapper;
    @Mock VrfcnAssgnMapper   vrfcnAssgnMapper;
    @Mock VrfcnInstMapper    vrfcnInstMapper;
    @Mock DataScopeValidator dataScopeValidator;
    @Mock IdGenerator        idGenerator;

    @InjectMocks
    VrService vrService;

    private IcasUser verifier;
    private IcasUser kotsa;
    private IcasUser molit;
    private IcasUser airline;

    @BeforeEach
    void setUp() {
        verifier = IcasUser.builder()
                .userId("vrf01").ognzSeCd("VERIFIER").vrfcnInstId("VI0001").master(false)
                .roleIds(List.of("VERIFIER_USER")).build();
        kotsa = IcasUser.builder()
                .userId("kotsa01").ognzSeCd("KOTSA").master(false)
                .roleIds(List.of("KOTSA_REVIEWER")).build();
        molit = IcasUser.builder()
                .userId("molit01").ognzSeCd("MOLIT").master(false)
                .roleIds(List.of("MOLIT_ADMIN")).build();
        airline = IcasUser.builder()
                .userId("airline01").ognzSeCd("AIRLINE").oprtrId("OP0001").master(false)
                .roleIds(List.of("AIRLINE_USER")).build();
    }

    private VrVO makeVr(String vrId, String stCd, String vrfcnInstId) {
        VrVO v = new VrVO();
        v.setVrId(vrId);
        v.setOprtrId("OP0001");
        v.setRprtYr("2026");
        v.setVrVer(1);
        v.setVrTypeCd("ER");
        v.setVrStCd(stCd);
        v.setVrfcnInstId(vrfcnInstId);
        return v;
    }

    private VrfcnInstVO makeInst(LocalDate xprDt) {
        VrfcnInstVO inst = new VrfcnInstVO();
        inst.setVrfcnInstId("VI0001");
        inst.setIcaoCcrAccrdXprDt(xprDt);
        return inst;
    }

    // ── createVr ─────────────────────────────────────────────

    @Test
    @DisplayName("createVr: AIRLINE → FORBIDDEN(403)")
    void createVr_AIRLINE_FORBIDDEN() {
        VrVO vo = new VrVO();
        vo.setOprtrId("OP0001");
        vo.setRprtYr("2026");
        vo.setVrTypeCd("ER");
        vo.setVrfcnInstId("VI0001");

        assertThatThrownBy(() -> vrService.createVr(vo, airline))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(403));
    }

    @Test
    @DisplayName("createVr: 배정 없는 검증기관 → FORBIDDEN(403)")
    void createVr_미배정_FORBIDDEN() {
        VrVO vo = new VrVO();
        vo.setOprtrId("OP0001");
        vo.setRprtYr("2026");
        vo.setVrTypeCd("ER");
        vo.setVrfcnInstId("VI0001");

        given(vrfcnAssgnMapper.existsAssgn("VI0001", "OP0001", "2026")).willReturn(false);

        assertThatThrownBy(() -> vrService.createVr(vo, verifier))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(403));
    }

    @Test
    @DisplayName("createVr: VERIFIER 정상 → DRAFT 생성")
    void createVr_VERIFIER_정상() {
        VrVO vo = new VrVO();
        vo.setOprtrId("OP0001");
        vo.setRprtYr("2026");
        vo.setVrTypeCd("ER");
        vo.setVrfcnInstId("VI0001");

        given(vrfcnAssgnMapper.existsAssgn("VI0001", "OP0001", "2026")).willReturn(true);
        given(vrMapper.countByPrefix("VR")).willReturn(0);
        given(idGenerator.managementPk("VR", 1)).willReturn("VR0001");
        given(vrMapper.selectByVrId("VR0001")).willReturn(makeVr("VR0001", "DRAFT", "VI0001"));

        VrVO result = vrService.createVr(vo, verifier);

        assertThat(result.getVrId()).isEqualTo("VR0001");
        assertThat(result.getVrStCd()).isEqualTo("DRAFT");
        verify(vrMapper).insertVr(any(VrVO.class));
    }

    @Test
    @DisplayName("createVr: vrTypeCd 잘못된 값 → BAD_REQUEST(400)")
    void createVr_잘못된타입_BAD_REQUEST() {
        VrVO vo = new VrVO();
        vo.setOprtrId("OP0001");
        vo.setRprtYr("2026");
        vo.setVrTypeCd("INVALID");
        vo.setVrfcnInstId("VI0001");

        assertThatThrownBy(() -> vrService.createVr(vo, verifier))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));
    }

    // ── submit ───────────────────────────────────────────────

    @Test
    @DisplayName("submit: CCR 만료 → BAD_REQUEST(400)")
    void submit_CCR만료_BAD_REQUEST() {
        given(vrMapper.selectByVrId("VR0001")).willReturn(makeVr("VR0001", "DRAFT", "VI0001"));
        given(vrfcnInstMapper.selectByVrfcnInstId("VI0001"))
                .willReturn(makeInst(LocalDate.now().minusDays(1)));

        assertThatThrownBy(() -> vrService.submit("VR0001", verifier))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));
    }

    @Test
    @DisplayName("submit: CCR 유효 → SBMTD 갱신")
    void submit_CCR유효_정상() {
        given(vrMapper.selectByVrId("VR0001")).willReturn(makeVr("VR0001", "DRAFT", "VI0001"));
        given(vrfcnInstMapper.selectByVrfcnInstId("VI0001"))
                .willReturn(makeInst(LocalDate.now().plusDays(30)));
        given(vrMapper.updateSubmit("VR0001", "vrf01")).willReturn(1);

        vrService.submit("VR0001", verifier);

        verify(vrMapper).updateSubmit("VR0001", "vrf01");
    }

    @Test
    @DisplayName("submit: 타 검증기관 VR → FORBIDDEN(403)")
    void submit_타검증기관_FORBIDDEN() {
        given(vrMapper.selectByVrId("VR0001")).willReturn(makeVr("VR0001", "DRAFT", "VI9999"));

        assertThatThrownBy(() -> vrService.submit("VR0001", verifier))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(403));
    }

    @Test
    @DisplayName("submit: DRAFT 아닌 상태 → CONFLICT(409)")
    void submit_비DRAFT_CONFLICT() {
        given(vrMapper.selectByVrId("VR0001")).willReturn(makeVr("VR0001", "SBMTD", "VI0001"));

        assertThatThrownBy(() -> vrService.submit("VR0001", verifier))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(409));
    }

    // ── recommend ────────────────────────────────────────────

    @Test
    @DisplayName("recommend: KOTSA + SBMTD → 권고 성공")
    void recommend_KOTSA_정상() {
        given(vrMapper.selectByVrId("VR0001")).willReturn(makeVr("VR0001", "SBMTD", "VI0001"));
        given(vrMapper.updateRecommend("VR0001", "kotsa01")).willReturn(1);

        vrService.recommend("VR0001", kotsa);

        verify(vrMapper).updateRecommend("VR0001", "kotsa01");
    }

    @Test
    @DisplayName("recommend: VERIFIER → FORBIDDEN(403)")
    void recommend_VERIFIER_FORBIDDEN() {
        assertThatThrownBy(() -> vrService.recommend("VR0001", verifier))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(403));
    }

    @Test
    @DisplayName("recommend: SBMTD 아닌 상태 → CONFLICT(409)")
    void recommend_비SBMTD_CONFLICT() {
        given(vrMapper.selectByVrId("VR0001")).willReturn(makeVr("VR0001", "DRAFT", "VI0001"));

        assertThatThrownBy(() -> vrService.recommend("VR0001", kotsa))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(409));
    }

    // ── approve ──────────────────────────────────────────────

    @Test
    @DisplayName("approve: MOLIT + RCMDD → 승인 성공")
    void approve_MOLIT_정상() {
        given(vrMapper.selectByVrId("VR0001")).willReturn(makeVr("VR0001", "RCMDD", "VI0001"));
        given(vrMapper.updateApprove("VR0001", "molit01")).willReturn(1);

        vrService.approve("VR0001", molit);

        verify(vrMapper).updateApprove("VR0001", "molit01");
    }

    @Test
    @DisplayName("approve: KOTSA → FORBIDDEN(403)")
    void approve_KOTSA_FORBIDDEN() {
        assertThatThrownBy(() -> vrService.approve("VR0001", kotsa))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(403));
    }

    // ── reject ───────────────────────────────────────────────

    @Test
    @DisplayName("reject: rjctRsn 없음 → BAD_REQUEST(400)")
    void reject_사유없음_BAD_REQUEST() {
        assertThatThrownBy(() -> vrService.reject("VR0001", null, kotsa))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));
    }

    @Test
    @DisplayName("reject: KOTSA + SBMTD + 사유 → 반려 성공")
    void reject_KOTSA_정상() {
        given(vrMapper.selectByVrId("VR0001")).willReturn(makeVr("VR0001", "SBMTD", "VI0001"));
        given(vrMapper.updateReject("VR0001", "데이터 오류", "kotsa01")).willReturn(1);

        vrService.reject("VR0001", "데이터 오류", kotsa);

        verify(vrMapper).updateReject("VR0001", "데이터 오류", "kotsa01");
    }
}
