package kr.go.molit.icas.com.prgrm;

import kr.go.molit.icas.com.prgrm.domain.PrgrmVO;
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
 * PrgrmService 단위 테스트.
 * Mapper 는 @Mock, Service 는 @InjectMocks.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PrgrmService 단위 테스트")
class PrgrmServiceTest {

    @Mock
    PrgrmMapper prgrmMapper;

    @InjectMocks
    PrgrmService prgrmService;

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

    private PrgrmVO sampleVO(String prgrmId) {
        PrgrmVO vo = new PrgrmVO();
        vo.setPrgrmId(prgrmId);
        vo.setSysSeCd("COM");
        vo.setPrgrmNm("공통 프로그램");
        vo.setPrgrmUrl("/com/prgrm");
        vo.setApiPathPrefix("/api/com/prgrm");
        return vo;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // selectPrgrm
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("단건 조회 — 존재하는 ID 정상 반환")
    void selectPrgrm_정상반환() {
        given(prgrmMapper.selectPrgrm("PG001")).willReturn(sampleVO("PG001"));

        PrgrmVO result = prgrmService.selectPrgrm("PG001");

        assertThat(result).isNotNull();
        assertThat(result.getPrgrmId()).isEqualTo("PG001");
        then(prgrmMapper).should().selectPrgrm("PG001");
    }

    @Test
    @DisplayName("단건 조회 — 존재하지 않는 ID → NOT_FOUND 예외")
    void selectPrgrm_없는ID_예외() {
        given(prgrmMapper.selectPrgrm("PG999")).willReturn(null);

        assertThatThrownBy(() -> prgrmService.selectPrgrm("PG999"))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException be = (BusinessException) ex;
                    assertThat(be.getCode()).isEqualTo("NOT_FOUND");
                    assertThat(be.getStatus()).isEqualTo(404);
                });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // insertPrgrm
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("프로그램 등록 — MOLIT 사용자 정상 등록")
    void insertPrgrm_정상등록() {
        PrgrmVO vo = sampleVO("PG001");
        given(prgrmMapper.existsPrgrm("PG001")).willReturn(false);
        given(prgrmMapper.insertPrgrm(any(PrgrmVO.class))).willReturn(1);
        given(prgrmMapper.selectPrgrm("PG001")).willReturn(vo);

        PrgrmVO result = prgrmService.insertPrgrm(vo, molitUser);

        assertThat(result.getPrgrmId()).isEqualTo("PG001");
        then(prgrmMapper).should().insertPrgrm(any(PrgrmVO.class));
    }

    @Test
    @DisplayName("프로그램 등록 — 유효하지 않은 sysSeCd → BAD_REQUEST 예외")
    void insertPrgrm_잘못된_sysSeCd_예외() {
        PrgrmVO vo = sampleVO("PG001");
        vo.setSysSeCd("INVALID");

        assertThatThrownBy(() -> prgrmService.insertPrgrm(vo, molitUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException be = (BusinessException) ex;
                    assertThat(be.getCode()).isEqualTo("BAD_REQUEST");
                    assertThat(be.getStatus()).isEqualTo(400);
                });

        then(prgrmMapper).should(never()).insertPrgrm(any());
    }

    @Test
    @DisplayName("프로그램 등록 — 중복 prgrmId → CONFLICT 예외")
    void insertPrgrm_중복ID_예외() {
        PrgrmVO vo = sampleVO("PG001");
        given(prgrmMapper.existsPrgrm("PG001")).willReturn(true);

        assertThatThrownBy(() -> prgrmService.insertPrgrm(vo, molitUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException be = (BusinessException) ex;
                    assertThat(be.getCode()).isEqualTo("CONFLICT");
                    assertThat(be.getStatus()).isEqualTo(409);
                });
    }

    @Test
    @DisplayName("프로그램 등록 — AIRLINE 사용자 → FORBIDDEN 예외")
    void insertPrgrm_권한없는사용자_예외() {
        PrgrmVO vo = sampleVO("PG001");

        assertThatThrownBy(() -> prgrmService.insertPrgrm(vo, airlineUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException be = (BusinessException) ex;
                    assertThat(be.getCode()).isEqualTo("FORBIDDEN");
                    assertThat(be.getStatus()).isEqualTo(403);
                });

        then(prgrmMapper).should(never()).insertPrgrm(any());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // updatePrgrm
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("프로그램 수정 — MOLIT 사용자 정상 수정")
    void updatePrgrm_정상수정() {
        PrgrmVO vo = sampleVO("PG001");
        given(prgrmMapper.updatePrgrm(any(PrgrmVO.class))).willReturn(1);

        assertThatCode(() -> prgrmService.updatePrgrm("PG001", vo, molitUser))
                .doesNotThrowAnyException();

        then(prgrmMapper).should().updatePrgrm(any(PrgrmVO.class));
    }

    @Test
    @DisplayName("프로그램 수정 — 존재하지 않는 ID → NOT_FOUND 예외")
    void updatePrgrm_없는ID_예외() {
        PrgrmVO vo = sampleVO("PG999");
        given(prgrmMapper.updatePrgrm(any(PrgrmVO.class))).willReturn(0);

        assertThatThrownBy(() -> prgrmService.updatePrgrm("PG999", vo, molitUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException be = (BusinessException) ex;
                    assertThat(be.getCode()).isEqualTo("NOT_FOUND");
                });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // softDeletePrgrm
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("프로그램 삭제 — MOLIT 사용자 정상 삭제")
    void softDeletePrgrm_정상삭제() {
        given(prgrmMapper.softDeletePrgrm("PG001", "molit01")).willReturn(1);

        assertThatCode(() -> prgrmService.softDeletePrgrm("PG001", molitUser))
                .doesNotThrowAnyException();

        then(prgrmMapper).should().softDeletePrgrm("PG001", "molit01");
    }

    @Test
    @DisplayName("프로그램 삭제 — 존재하지 않는 ID → NOT_FOUND 예외")
    void softDeletePrgrm_없는ID_예외() {
        given(prgrmMapper.softDeletePrgrm(eq("PG999"), anyString())).willReturn(0);

        assertThatThrownBy(() -> prgrmService.softDeletePrgrm("PG999", molitUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException be = (BusinessException) ex;
                    assertThat(be.getCode()).isEqualTo("NOT_FOUND");
                });
    }
}
