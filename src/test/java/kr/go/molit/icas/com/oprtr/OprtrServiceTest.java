package kr.go.molit.icas.com.oprtr;

import kr.go.molit.icas.com.ognz.OgnzService;
import kr.go.molit.icas.com.ognz.domain.OgnzVO;
import kr.go.molit.icas.com.oprtr.domain.OprtrVO;
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
@DisplayName("OprtrService 단위 테스트")
class OprtrServiceTest {

    @Mock
    OprtrMapper oprtrMapper;

    @Mock
    IdGenerator idGenerator;

    @Mock
    OgnzService ognzService;

    @InjectMocks
    OprtrService oprtrService;

    // ── 공통 fixture ──
    private IcasUser molitUser;
    private IcasUser airlineUserOP0001;
    private IcasUser airlineUserOP0002;
    private IcasUser verifierUser;

    private OgnzVO makeOgnz(String seCd) {
        OgnzVO ognz = new OgnzVO();
        ognz.setOgnzSeCd(seCd);
        return ognz;
    }

    @BeforeEach
    void setUpFixtures() {
        // OgnzService stub — requireOgnzOfType(AIRLINE) 기본 통과
        given(ognzService.requireOgnzOfType(anyString(), eq("AIRLINE")))
                .willReturn(makeOgnz("AIRLINE"));

        molitUser = IcasUser.builder()
                .userId("molit01")
                .userNm("국토부 담당자")
                .ognzSeCd("MOLIT")
                .ognzId("ORG_MOLIT")
                .master(false)
                .roleIds(List.of("ADMIN"))
                .build();

        airlineUserOP0001 = IcasUser.builder()
                .userId("airline01")
                .userNm("항공사A 담당자")
                .ognzSeCd("AIRLINE")
                .ognzId("ORG_AIR01")
                .oprtrId("OP0001")
                .master(false)
                .roleIds(List.of("AIRLINE_USER"))
                .build();

        airlineUserOP0002 = IcasUser.builder()
                .userId("airline02")
                .userNm("항공사B 담당자")
                .ognzSeCd("AIRLINE")
                .ognzId("ORG_AIR02")
                .oprtrId("OP0002")
                .master(false)
                .roleIds(List.of("AIRLINE_USER"))
                .build();

        verifierUser = IcasUser.builder()
                .userId("verifier01")
                .userNm("검증기관 담당자")
                .ognzSeCd("VERIFIER")
                .ognzId("ORG_VRF01")
                .vrfcnInstId("VI0001")
                .master(false)
                .roleIds(List.of("VERIFIER_USER"))
                .build();
    }

    private OprtrVO validOprtrVO() {
        OprtrVO vo = new OprtrVO();
        vo.setOprtrNm("대한항공");
        vo.setOprtrNmEn("Korean Air");
        vo.setIcaoDesig("KAL");
        vo.setOgnzId("ORG_AIR01");
        return vo;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // selectAll
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("전체 목록 조회 — Mapper 결과를 그대로 반환한다")
    void selectAll_반환목록_정상() {
        OprtrVO vo = validOprtrVO();
        vo.setOprtrId("OP0001");
        given(oprtrMapper.selectAll()).willReturn(List.of(vo));

        List<OprtrVO> result = oprtrService.selectAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOprtrId()).isEqualTo("OP0001");
        then(oprtrMapper).should().selectAll();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // selectByOprtrId — 권한 시나리오
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("단건 조회 — MOLIT 사용자는 타 항공사 ID 도 조회 가능")
    void selectByOprtrId_MOLIT_성공() {
        OprtrVO vo = validOprtrVO();
        vo.setOprtrId("OP0001");
        given(oprtrMapper.selectByOprtrId("OP0001")).willReturn(vo);

        OprtrVO result = oprtrService.selectByOprtrId("OP0001", molitUser);

        assertThat(result.getOprtrId()).isEqualTo("OP0001");
    }

    @Test
    @DisplayName("단건 조회 — AIRLINE 사용자는 본인 oprtrId 조회 성공")
    void selectByOprtrId_AIRLINE_본인조회_성공() {
        OprtrVO vo = validOprtrVO();
        vo.setOprtrId("OP0001");
        given(oprtrMapper.selectByOprtrId("OP0001")).willReturn(vo);

        OprtrVO result = oprtrService.selectByOprtrId("OP0001", airlineUserOP0001);

        assertThat(result.getOprtrId()).isEqualTo("OP0001");
    }

    @Test
    @DisplayName("단건 조회 — AIRLINE 사용자가 타 항공사 ID 조회 시 FORBIDDEN 예외")
    void selectByOprtrId_AIRLINE_타사조회_FORBIDDEN() {
        OprtrVO vo = validOprtrVO();
        vo.setOprtrId("OP0001");
        given(oprtrMapper.selectByOprtrId("OP0001")).willReturn(vo);

        // airlineUserOP0002 는 OP0002 소속이므로 OP0001 조회 불가
        assertThatThrownBy(() -> oprtrService.selectByOprtrId("OP0001", airlineUserOP0002))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(403));
    }

    @Test
    @DisplayName("단건 조회 — 존재하지 않는 oprtrId 는 NOT_FOUND 예외")
    void selectByOprtrId_존재하지않음_NOT_FOUND() {
        given(oprtrMapper.selectByOprtrId("OP9999")).willReturn(null);

        assertThatThrownBy(() -> oprtrService.selectByOprtrId("OP9999", molitUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(404));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // insert
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("운영사 등록 — 정상 케이스: ID 채번 후 insert 호출")
    void insert_정상_ID채번및등록() {
        OprtrVO vo = validOprtrVO();
        given(oprtrMapper.countByIcaoDesig("KAL")).willReturn(0);
        given(oprtrMapper.countByPrefix("OP")).willReturn(0);
        given(idGenerator.managementPk("OP", 1)).willReturn("OP0001");
        given(oprtrMapper.insert(any(OprtrVO.class))).willReturn(1);

        OprtrVO result = oprtrService.insert(vo, molitUser);

        assertThat(result.getOprtrId()).isEqualTo("OP0001");
        assertThat(result.getFrstRegUserId()).isEqualTo("molit01");
        then(oprtrMapper).should().insert(vo);
    }

    @Test
    @DisplayName("운영사 등록 — ICAO 지정어 중복 시 CONFLICT 예외")
    void insert_ICAO중복_CONFLICT() {
        OprtrVO vo = validOprtrVO();
        given(oprtrMapper.countByIcaoDesig("KAL")).willReturn(1);

        assertThatThrownBy(() -> oprtrService.insert(vo, molitUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(409));
    }

    @Test
    @DisplayName("운영사 등록 — ICAO 지정어 3자 미충족 시 BAD_REQUEST 예외")
    void insert_ICAO지정어_길이오류_BAD_REQUEST() {
        OprtrVO vo = validOprtrVO();
        vo.setIcaoDesig("KA");  // 2자 → 오류

        assertThatThrownBy(() -> oprtrService.insert(vo, molitUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // update
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("운영사 수정 — AIRLINE 이 타사 데이터 수정 시도 시 FORBIDDEN 예외")
    void update_AIRLINE_타사수정_FORBIDDEN() {
        OprtrVO existing = validOprtrVO();
        existing.setOprtrId("OP0001");
        given(oprtrMapper.selectByOprtrId("OP0001")).willReturn(existing);

        OprtrVO updateVO = validOprtrVO();
        // airlineUserOP0002 는 OP0002 소속 → OP0001 수정 불가
        assertThatThrownBy(() -> oprtrService.update("OP0001", updateVO, airlineUserOP0002))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(403));
    }

    @Test
    @DisplayName("운영사 수정 — 존재하지 않는 ID 수정 시도 시 NOT_FOUND 예외")
    void update_존재하지않음_NOT_FOUND() {
        given(oprtrMapper.selectByOprtrId("OP9999")).willReturn(null);

        OprtrVO updateVO = validOprtrVO();
        assertThatThrownBy(() -> oprtrService.update("OP9999", updateVO, molitUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(404));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // softDelete
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("소프트삭제 — 존재하지 않는 ID 삭제 시도 시 NOT_FOUND 예외")
    void softDelete_존재하지않음_NOT_FOUND() {
        given(oprtrMapper.selectByOprtrId("OP9999")).willReturn(null);

        assertThatThrownBy(() -> oprtrService.softDelete("OP9999", molitUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(404));
    }
}
