package kr.go.molit.icas.com.ognz;

import kr.go.molit.icas.com.ognz.domain.OgnzVO;
import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.IcasUser;
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
@DisplayName("OgnzService 단위 테스트")
class OgnzServiceTest {

    @Mock
    OgnzMapper ognzMapper;

    @InjectMocks
    OgnzService ognzService;

    private IcasUser molitUser;
    private IcasUser airlineUser;

    @BeforeEach
    void setUp() {
        molitUser = IcasUser.builder()
                .userId("molit01").userNm("국토부 담당자")
                .ognzSeCd("MOLIT").ognzId("ORG_MOLIT").master(false)
                .roleIds(List.of("ADMIN")).build();

        airlineUser = IcasUser.builder()
                .userId("airline01").userNm("항공사 담당자")
                .ognzSeCd("AIRLINE").ognzId("ORG_AIR01").oprtrId("OP0001").master(false)
                .roleIds(List.of("AIRLINE_USER")).build();
    }

    private OgnzVO validVO() {
        OgnzVO vo = new OgnzVO();
        vo.setOgnzId("ORG_TEST01");
        vo.setOgnzSeCd("AIRLINE");
        vo.setOgnzNm("테스트항공사");
        vo.setBizNo("1234567890");
        return vo;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // listAll
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("전체 목록 조회 — Mapper 결과를 그대로 반환한다")
    void listAll_정상반환() {
        given(ognzMapper.selectAll()).willReturn(List.of(validVO()));

        List<OgnzVO> result = ognzService.listAll();

        assertThat(result).hasSize(1);
        then(ognzMapper).should().selectAll();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getOgnz
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("단건 조회 — 존재하는 ID 정상 반환")
    void getOgnz_존재하는ID_반환() {
        OgnzVO vo = validVO();
        given(ognzMapper.selectByOgnzId("ORG_TEST01")).willReturn(vo);

        OgnzVO result = ognzService.getOgnz("ORG_TEST01");

        assertThat(result.getOgnzId()).isEqualTo("ORG_TEST01");
    }

    @Test
    @DisplayName("단건 조회 — 존재하지 않는 ID 시 NOT_FOUND 예외")
    void getOgnz_없는ID_NOT_FOUND() {
        given(ognzMapper.selectByOgnzId("NO_EXIST")).willReturn(null);

        assertThatThrownBy(() -> ognzService.getOgnz("NO_EXIST"))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(404));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // createOgnz
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("기관 등록 — 정상 케이스 MOLIT 사용자 등록 성공")
    void createOgnz_정상등록() {
        OgnzVO vo = validVO();
        given(ognzMapper.selectByOgnzId("ORG_TEST01")).willReturn(null, vo);
        given(ognzMapper.insertOgnz(any())).willReturn(1);

        OgnzVO result = ognzService.createOgnz(vo, molitUser);

        assertThat(result).isNotNull();
        then(ognzMapper).should().insertOgnz(any());
    }

    @Test
    @DisplayName("기관 등록 — ognzSeCd 잘못된 값(XXX) 시 BAD_REQUEST 예외")
    void createOgnz_잘못된ognzSeCd_BAD_REQUEST() {
        OgnzVO vo = validVO();
        vo.setOgnzSeCd("XXX");

        assertThatThrownBy(() -> ognzService.createOgnz(vo, molitUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));
    }

    @Test
    @DisplayName("기관 등록 — 중복 ognzId 시 CONFLICT 예외")
    void createOgnz_중복ID_CONFLICT() {
        OgnzVO vo = validVO();
        given(ognzMapper.selectByOgnzId("ORG_TEST01")).willReturn(validVO());

        assertThatThrownBy(() -> ognzService.createOgnz(vo, molitUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(409));
    }

    @Test
    @DisplayName("기관 등록 — 사업자번호 형식 오류(10자리 아님) 시 BAD_REQUEST 예외")
    void createOgnz_잘못된사업자번호_BAD_REQUEST() {
        OgnzVO vo = validVO();
        vo.setBizNo("12345");   // 5자리 → 오류

        assertThatThrownBy(() -> ognzService.createOgnz(vo, molitUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // updateOgnz
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("기관 수정 — 정상 수정")
    void updateOgnz_정상수정() {
        OgnzVO updateVO = validVO();
        OgnzVO returnVO = validVO();
        given(ognzMapper.updateOgnz(any())).willReturn(1);
        given(ognzMapper.selectByOgnzId("ORG_TEST01")).willReturn(returnVO);

        OgnzVO result = ognzService.updateOgnz("ORG_TEST01", updateVO, molitUser);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("기관 수정 — 존재하지 않는 ID 시 NOT_FOUND 예외")
    void updateOgnz_없는ID_NOT_FOUND() {
        OgnzVO updateVO = validVO();
        given(ognzMapper.updateOgnz(any())).willReturn(0);

        assertThatThrownBy(() -> ognzService.updateOgnz("NO_EXIST", updateVO, molitUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(404));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // softDeleteOgnz
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("소프트 삭제 — 정상 삭제")
    void softDeleteOgnz_정상삭제() {
        given(ognzMapper.softDeleteOgnz(eq("ORG_TEST01"), anyString())).willReturn(1);

        assertThatCode(() -> ognzService.softDeleteOgnz("ORG_TEST01", molitUser))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("소프트 삭제 — 존재하지 않는 ID 시 NOT_FOUND 예외")
    void softDeleteOgnz_없는ID_NOT_FOUND() {
        given(ognzMapper.softDeleteOgnz(eq("NO_EXIST"), anyString())).willReturn(0);

        assertThatThrownBy(() -> ognzService.softDeleteOgnz("NO_EXIST", molitUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(404));
    }
}
