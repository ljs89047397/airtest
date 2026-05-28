package kr.go.molit.icas.com.vrfcn;

import kr.go.molit.icas.com.ognz.OgnzService;
import kr.go.molit.icas.com.ognz.domain.OgnzVO;
import kr.go.molit.icas.com.vrfcn.domain.VrfcnInstVO;
import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.common.util.IdGenerator;
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
@DisplayName("VrfcnInstService 단위 테스트")
class VrfcnInstServiceTest {

    @Mock
    VrfcnInstMapper vrfcnInstMapper;

    @Mock
    IdGenerator idGenerator;

    @Mock
    OgnzService ognzService;

    @InjectMocks
    VrfcnInstService vrfcnInstService;

    // ── fixture ──
    private IcasUser molitUser;
    private IcasUser verifierUser;

    private OgnzVO makeOgnz(String seCd) {
        OgnzVO ognz = new OgnzVO();
        ognz.setOgnzSeCd(seCd);
        return ognz;
    }

    @BeforeEach
    void setUpFixtures() {
        // OgnzService stub — requireOgnzOfType(VERIFIER) 기본 통과
        given(ognzService.requireOgnzOfType(anyString(), eq("VERIFIER")))
                .willReturn(makeOgnz("VERIFIER"));

        molitUser = IcasUser.builder()
                .userId("molit01").userNm("국토부 담당자")
                .ognzSeCd("MOLIT").ognzId("ORG_MOLIT").master(false)
                .roleIds(List.of("ADMIN")).build();

        verifierUser = IcasUser.builder()
                .userId("verifier01").userNm("검증기관 담당자")
                .ognzSeCd("VERIFIER").ognzId("ORG_VRF01").vrfcnInstId("VI0001").master(false)
                .roleIds(List.of("VERIFIER_USER")).build();
    }

    private VrfcnInstVO validVO() {
        VrfcnInstVO vo = new VrfcnInstVO();
        vo.setOgnzId("ORG_VRF01");
        vo.setVrfcnInstNm("한국탄소검증원");
        vo.setVrfcnInstNmEn("Korea Carbon Verification Institute");
        vo.setIcaoCcrAccrdYn("Y");
        return vo;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // selectAll
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("전체 목록 조회 — Mapper 결과를 그대로 반환한다")
    void selectAll_정상() {
        VrfcnInstVO vo = validVO();
        vo.setVrfcnInstId("VI0001");
        given(vrfcnInstMapper.selectAll()).willReturn(List.of(vo));

        List<VrfcnInstVO> result = vrfcnInstService.selectAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getVrfcnInstId()).isEqualTo("VI0001");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // selectByVrfcnInstId
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("단건 조회 — 존재하는 ID 는 정상 반환")
    void selectByVrfcnInstId_정상() {
        VrfcnInstVO vo = validVO();
        vo.setVrfcnInstId("VI0001");
        given(vrfcnInstMapper.selectByVrfcnInstId("VI0001")).willReturn(vo);

        VrfcnInstVO result = vrfcnInstService.selectByVrfcnInstId("VI0001");

        assertThat(result.getVrfcnInstId()).isEqualTo("VI0001");
    }

    @Test
    @DisplayName("단건 조회 — 존재하지 않는 ID 는 NOT_FOUND 예외")
    void selectByVrfcnInstId_없는ID_NOT_FOUND() {
        given(vrfcnInstMapper.selectByVrfcnInstId("VI9999")).willReturn(null);

        assertThatThrownBy(() -> vrfcnInstService.selectByVrfcnInstId("VI9999"))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(404));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // createVrfcnInst
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("검증기관 등록 — 정상 케이스: ID 채번 후 insert 호출")
    void createVrfcnInst_정상_ID채번및등록() {
        VrfcnInstVO vo = validVO();
        given(vrfcnInstMapper.countByPrefix("VI")).willReturn(0);
        given(idGenerator.managementPk("VI", 1)).willReturn("VI0001");
        given(vrfcnInstMapper.insertVrfcnInst(any(VrfcnInstVO.class))).willReturn(1);

        VrfcnInstVO result = vrfcnInstService.createVrfcnInst(vo, molitUser);

        assertThat(result.getVrfcnInstId()).isEqualTo("VI0001");
        assertThat(result.getFrstRegUserId()).isEqualTo("molit01");
        then(vrfcnInstMapper).should().insertVrfcnInst(vo);
    }

    @Test
    @DisplayName("검증기관 등록 — icaoCcrAccrdYn 잘못된 값('X') 시 BAD_REQUEST 예외")
    void createVrfcnInst_icaoCcrAccrdYn_오류값_BAD_REQUEST() {
        VrfcnInstVO vo = validVO();
        vo.setIcaoCcrAccrdYn("X");

        assertThatThrownBy(() -> vrfcnInstService.createVrfcnInst(vo, molitUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> {
                    assertThat(((BusinessException) e).getStatus()).isEqualTo(400);
                    assertThat(((BusinessException) e).getCode()).isEqualTo("BAD_REQUEST");
                });
    }

    @Test
    @DisplayName("검증기관 등록 — icaoCcrAccrdYn null 시 BAD_REQUEST 예외")
    void createVrfcnInst_icaoCcrAccrdYn_null_BAD_REQUEST() {
        VrfcnInstVO vo = validVO();
        vo.setIcaoCcrAccrdYn(null);

        assertThatThrownBy(() -> vrfcnInstService.createVrfcnInst(vo, molitUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));
    }

    @Test
    @DisplayName("검증기관 등록 — 필수 필드(기관명) 누락 시 BAD_REQUEST 예외")
    void createVrfcnInst_필수필드_누락_BAD_REQUEST() {
        VrfcnInstVO vo = validVO();
        vo.setVrfcnInstNm(null);

        assertThatThrownBy(() -> vrfcnInstService.createVrfcnInst(vo, molitUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // updateVrfcnInst
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("검증기관 수정 — 존재하지 않는 ID 수정 시도 시 NOT_FOUND 예외")
    void updateVrfcnInst_없는ID_NOT_FOUND() {
        given(vrfcnInstMapper.selectByVrfcnInstId("VI9999")).willReturn(null);

        VrfcnInstVO vo = validVO();
        assertThatThrownBy(() -> vrfcnInstService.updateVrfcnInst("VI9999", vo, molitUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(404));
    }

    @Test
    @DisplayName("검증기관 수정 — icaoCcrAccrdYn 잘못된 값 시 BAD_REQUEST 예외")
    void updateVrfcnInst_icaoCcrAccrdYn_오류값_BAD_REQUEST() {
        VrfcnInstVO existing = validVO();
        existing.setVrfcnInstId("VI0001");
        given(vrfcnInstMapper.selectByVrfcnInstId("VI0001")).willReturn(existing);

        VrfcnInstVO updateVO = validVO();
        updateVO.setIcaoCcrAccrdYn("X");

        assertThatThrownBy(() -> vrfcnInstService.updateVrfcnInst("VI0001", updateVO, molitUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // softDeleteVrfcnInst
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("소프트삭제 — Mapper 가 0 반환 시 NOT_FOUND 예외")
    void softDeleteVrfcnInst_Mapper결과없음_NOT_FOUND() {
        given(vrfcnInstMapper.softDeleteVrfcnInst("VI9999", "molit01")).willReturn(0);

        assertThatThrownBy(() -> vrfcnInstService.softDeleteVrfcnInst("VI9999", molitUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(404));
    }
}
