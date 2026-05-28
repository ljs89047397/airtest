package kr.go.molit.icas.com.prgrm;

import kr.go.molit.icas.com.prgrm.domain.AuthrtPrgrmMpngVO;
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
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * AuthrtPrgrmMpngService 단위 테스트.
 * Mapper 는 @Mock, Service 는 @InjectMocks.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthrtPrgrmMpngService 단위 테스트")
class AuthrtPrgrmMpngServiceTest {

    @Mock
    AuthrtPrgrmMpngMapper authrtPrgrmMpngMapper;

    @InjectMocks
    AuthrtPrgrmMpngService authrtPrgrmMpngService;

    // ── fixture ──
    IcasUser molitUser;
    IcasUser airlineUser;

    @BeforeEach
    void setUp() {
        molitUser = IcasUser.builder()
                .userId("molit01").userNm("국토부 담당자")
                .ognzSeCd("MOLIT").ognzId("ORG_MOLIT").master(false)
                .prgrmPathsInq(Set.of("/api/com/prgrm"))
                .prgrmPathsInpt(Set.of("/api/com/prgrm"))
                .roleIds(List.of("ADMIN")).build();

        airlineUser = IcasUser.builder()
                .userId("airline01").userNm("항공사 담당자")
                .ognzSeCd("AIRLINE").ognzId("ORG_AIR01").oprtrId("OP0001").master(false)
                .prgrmPathsInq(Set.of())
                .prgrmPathsInpt(Set.of())
                .roleIds(List.of("AIRLINE_USER")).build();
    }

    private AuthrtPrgrmMpngVO sampleMpngVO(String authrtId, String prgrmId) {
        AuthrtPrgrmMpngVO vo = new AuthrtPrgrmMpngVO();
        vo.setAuthrtId(authrtId);
        vo.setPrgrmId(prgrmId);
        vo.setInqAuthrtYn("Y");
        vo.setInptAuthrtYn("N");
        return vo;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // selectByAuthrt
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("권한별 프로그램 목록 조회 — 정상 반환")
    void selectByAuthrt_정상반환() {
        given(authrtPrgrmMpngMapper.selectByAuthrt("ROLE_ADMIN"))
                .willReturn(List.of(sampleMpngVO("ROLE_ADMIN", "PG001")));

        List<AuthrtPrgrmMpngVO> result = authrtPrgrmMpngService.selectByAuthrt("ROLE_ADMIN");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAuthrtId()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    @DisplayName("권한별 프로그램 목록 조회 — authrtId 공백 → BAD_REQUEST 예외")
    void selectByAuthrt_빈ID_예외() {
        assertThatThrownBy(() -> authrtPrgrmMpngService.selectByAuthrt(""))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException be = (BusinessException) ex;
                    assertThat(be.getCode()).isEqualTo("BAD_REQUEST");
                });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // selectByPrgrm
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("프로그램별 권한 목록 조회 — 정상 반환")
    void selectByPrgrm_정상반환() {
        given(authrtPrgrmMpngMapper.selectByPrgrm("PG001"))
                .willReturn(List.of(sampleMpngVO("ROLE_ADMIN", "PG001")));

        List<AuthrtPrgrmMpngVO> result = authrtPrgrmMpngService.selectByPrgrm("PG001");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPrgrmId()).isEqualTo("PG001");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // setAuthority (upsert)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("setAuthority upsert — 기존 매핑 없음 → insertMapping 호출")
    void setAuthority_기존없음_insert() {
        given(authrtPrgrmMpngMapper.selectOne("ROLE_ADMIN", "PG001")).willReturn(null);

        authrtPrgrmMpngService.setAuthority("ROLE_ADMIN", "PG001", "Y", "N", molitUser);

        then(authrtPrgrmMpngMapper).should().insertMapping(any(AuthrtPrgrmMpngVO.class));
        then(authrtPrgrmMpngMapper).should(never()).updateMapping(any());
    }

    @Test
    @DisplayName("setAuthority upsert — 기존 매핑 있음 → updateMapping 호출")
    void setAuthority_기존있음_update() {
        given(authrtPrgrmMpngMapper.selectOne("ROLE_ADMIN", "PG001"))
                .willReturn(sampleMpngVO("ROLE_ADMIN", "PG001"));

        authrtPrgrmMpngService.setAuthority("ROLE_ADMIN", "PG001", "Y", "Y", molitUser);

        then(authrtPrgrmMpngMapper).should().updateMapping(any(AuthrtPrgrmMpngVO.class));
        then(authrtPrgrmMpngMapper).should(never()).insertMapping(any());
    }

    @Test
    @DisplayName("setAuthority — inqYn=Y, inptYn=N 정상 매핑 생성")
    void setAuthority_inqY_inptN_정상() {
        given(authrtPrgrmMpngMapper.selectOne("ROLE_ADMIN", "PG001")).willReturn(null);

        assertThatCode(() ->
                authrtPrgrmMpngService.setAuthority("ROLE_ADMIN", "PG001", "Y", "N", molitUser))
                .doesNotThrowAnyException();

        then(authrtPrgrmMpngMapper).should().insertMapping(argThat(vo ->
                "Y".equals(vo.getInqAuthrtYn()) && "N".equals(vo.getInptAuthrtYn())));
    }

    @Test
    @DisplayName("setAuthority — inqYn/inptYn 둘 다 유효하지 않은 값 → BAD_REQUEST 예외")
    void setAuthority_잘못된_yn값_예외() {
        // inqAuthrtYn = "X" 는 Y/N 외 값
        assertThatThrownBy(() ->
                authrtPrgrmMpngService.setAuthority("ROLE_ADMIN", "PG001", "X", "N", molitUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException be = (BusinessException) ex;
                    assertThat(be.getCode()).isEqualTo("BAD_REQUEST");
                });

        then(authrtPrgrmMpngMapper).should(never()).insertMapping(any());
        then(authrtPrgrmMpngMapper).should(never()).updateMapping(any());
    }

    @Test
    @DisplayName("setAuthority — AIRLINE 사용자(권한 없음) → FORBIDDEN 예외")
    void setAuthority_권한없는사용자_예외() {
        assertThatThrownBy(() ->
                authrtPrgrmMpngService.setAuthority("ROLE_ADMIN", "PG001", "Y", "N", airlineUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException be = (BusinessException) ex;
                    assertThat(be.getCode()).isEqualTo("FORBIDDEN");
                });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // removeMapping
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("매핑 삭제 — 정상 삭제")
    void removeMapping_정상삭제() {
        given(authrtPrgrmMpngMapper.softDeleteMapping("ROLE_ADMIN", "PG001", "molit01")).willReturn(1);

        assertThatCode(() ->
                authrtPrgrmMpngService.removeMapping("ROLE_ADMIN", "PG001", molitUser))
                .doesNotThrowAnyException();

        then(authrtPrgrmMpngMapper).should().softDeleteMapping("ROLE_ADMIN", "PG001", "molit01");
    }

    @Test
    @DisplayName("매핑 삭제 — 존재하지 않는 매핑 → NOT_FOUND 예외")
    void removeMapping_없는매핑_예외() {
        given(authrtPrgrmMpngMapper.softDeleteMapping("ROLE_ADMIN", "PG999", "molit01")).willReturn(0);

        assertThatThrownBy(() ->
                authrtPrgrmMpngService.removeMapping("ROLE_ADMIN", "PG999", molitUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException be = (BusinessException) ex;
                    assertThat(be.getCode()).isEqualTo("NOT_FOUND");
                });
    }
}
