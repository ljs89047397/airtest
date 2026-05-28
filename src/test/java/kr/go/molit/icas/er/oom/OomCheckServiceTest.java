package kr.go.molit.icas.er.oom;

import kr.go.molit.icas.com.oprtr.OprtrMapper;
import kr.go.molit.icas.com.oprtr.domain.OprtrVO;
import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.DataScopeValidator;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.common.util.IdGenerator;
import kr.go.molit.icas.er.oom.domain.OomCheckVO;
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
@DisplayName("OomCheckService 단위 테스트 — 점검 라이프사이클 + 권한")
class OomCheckServiceTest {

    @Mock OomCheckMapper     oomCheckMapper;
    @Mock OprtrMapper        oprtrMapper;
    @Mock DataScopeValidator dataScopeValidator;
    @Mock IdGenerator        idGenerator;

    @InjectMocks
    OomCheckService oomCheckService;

    private IcasUser airline;
    private IcasUser kotsa;

    @BeforeEach
    void setUp() {
        airline = IcasUser.builder()
                .userId("airline01").ognzSeCd("AIRLINE").oprtrId("OP0001").master(false)
                .roleIds(List.of("AIRLINE_USER")).build();
        kotsa = IcasUser.builder()
                .userId("kotsa01").ognzSeCd("KOTSA").master(false)
                .roleIds(List.of("KOTSA_REVIEWER")).build();
    }

    private OomCheckVO makeOom(String oomId, String stCd, String rsltCd) {
        OomCheckVO m = new OomCheckVO();
        m.setOomId(oomId);
        m.setOprtrId("OP0001");
        m.setRprtYr("2026");
        m.setOomStCd(stCd);
        m.setOomRsltCd(rsltCd);
        return m;
    }

    // ── createOom ─────────────────────────────────────────

    @Test
    @DisplayName("createOom: AIRLINE → FORBIDDEN(403)")
    void createOom_AIRLINE_FORBIDDEN() {
        OomCheckVO vo = new OomCheckVO();
        vo.setOprtrId("OP0001");
        vo.setRprtYr("2026");

        assertThatThrownBy(() -> oomCheckService.createOom(vo, airline))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(403));
    }

    @Test
    @DisplayName("createOom: KOTSA 정상 → INPRG 생성")
    void createOom_KOTSA_정상() {
        OomCheckVO vo = new OomCheckVO();
        vo.setOprtrId("OP0001");
        vo.setRprtYr("2026");

        given(oprtrMapper.selectByOprtrId("OP0001")).willReturn(new OprtrVO());
        given(oomCheckMapper.selectByOprtrYr("OP0001", "2026")).willReturn(null);
        given(oomCheckMapper.countByPrefix("OOM")).willReturn(0);
        given(idGenerator.managementPk("OOM", 1)).willReturn("OOM0001");
        given(oomCheckMapper.selectByOomId("OOM0001"))
                .willReturn(makeOom("OOM0001", "INPRG", null));

        OomCheckVO result = oomCheckService.createOom(vo, kotsa);

        assertThat(result.getOomId()).isEqualTo("OOM0001");
        assertThat(result.getOomStCd()).isEqualTo("INPRG");
        verify(oomCheckMapper).insertOom(any(OomCheckVO.class));
    }

    @Test
    @DisplayName("createOom: 동일 (oprtrId, rprtYr) 이미 존재 → CONFLICT(409)")
    void createOom_UK중복_CONFLICT() {
        OomCheckVO vo = new OomCheckVO();
        vo.setOprtrId("OP0001");
        vo.setRprtYr("2026");

        given(oprtrMapper.selectByOprtrId("OP0001")).willReturn(new OprtrVO());
        given(oomCheckMapper.selectByOprtrYr("OP0001", "2026"))
                .willReturn(makeOom("OOM0099", "INPRG", null));

        assertThatThrownBy(() -> oomCheckService.createOom(vo, kotsa))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(409));
    }

    @Test
    @DisplayName("createOom: rprtYr 4자리 아님 → BAD_REQUEST(400)")
    void createOom_rprtYr잘못_BAD_REQUEST() {
        OomCheckVO vo = new OomCheckVO();
        vo.setOprtrId("OP0001");
        vo.setRprtYr("26");

        assertThatThrownBy(() -> oomCheckService.createOom(vo, kotsa))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));
    }

    // ── finalizeOom ───────────────────────────────────────

    @Test
    @DisplayName("finalize: rsltCd 허용값 외 → BAD_REQUEST(400)")
    void finalize_잘못된코드_BAD_REQUEST() {
        assertThatThrownBy(() -> oomCheckService.finalizeOom("OOM0001", "INVALID", kotsa))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));
    }

    @Test
    @DisplayName("finalize: INPRG → DONE/PASS")
    void finalize_INPRG_정상() {
        given(oomCheckMapper.selectByOomId("OOM0001")).willReturn(makeOom("OOM0001", "INPRG", null));
        given(oomCheckMapper.updateFinalize("OOM0001", "PASS", "kotsa01", "kotsa01")).willReturn(1);

        oomCheckService.finalizeOom("OOM0001", "PASS", kotsa);

        verify(oomCheckMapper).updateFinalize("OOM0001", "PASS", "kotsa01", "kotsa01");
    }

    @Test
    @DisplayName("finalize: 이미 DONE → CONFLICT(409)")
    void finalize_이미DONE_CONFLICT() {
        given(oomCheckMapper.selectByOomId("OOM0001")).willReturn(makeOom("OOM0001", "DONE", "PASS"));

        assertThatThrownBy(() -> oomCheckService.finalizeOom("OOM0001", "FAIL", kotsa))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(409));
    }

    // ── hold ───────────────────────────────────────────────

    @Test
    @DisplayName("hold: INPRG → rslt=HOLD (상태는 INPRG 유지)")
    void hold_정상() {
        given(oomCheckMapper.selectByOomId("OOM0001")).willReturn(makeOom("OOM0001", "INPRG", null));
        given(oomCheckMapper.updateHold("OOM0001", "kotsa01")).willReturn(1);

        oomCheckService.hold("OOM0001", kotsa);

        verify(oomCheckMapper).updateHold("OOM0001", "kotsa01");
    }
}
