package kr.go.molit.icas.com.rglt;

import kr.go.molit.icas.com.rglt.domain.RgltSearch;
import kr.go.molit.icas.com.rglt.domain.RgltVO;
import kr.go.molit.icas.common.dto.PageResponse;
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
@DisplayName("RgltService 단위 테스트")
class RgltServiceTest {

    @Mock RgltMapper  rgltMapper;
    @Mock IdGenerator idGenerator;

    @InjectMocks
    RgltService rgltService;

    private IcasUser molitUser;
    private IcasUser kotsaUser;
    private IcasUser airlineUser;

    @BeforeEach
    void setUp() {
        molitUser = IcasUser.builder()
                .userId("molit01").ognzSeCd("MOLIT").master(false)
                .roleIds(List.of("ADMIN")).build();

        kotsaUser = IcasUser.builder()
                .userId("kotsa01").ognzSeCd("KOTSA").master(false)
                .roleIds(List.of("ADMIN")).build();

        airlineUser = IcasUser.builder()
                .userId("airline01").ognzSeCd("AIRLINE").oprtrId("OP0001").master(false)
                .roleIds(List.of("AIRLINE_USER")).build();
    }

    private RgltVO makeRgltVO(String rgltId) {
        RgltVO vo = new RgltVO();
        vo.setRgltId(rgltId);
        vo.setRgltNm("CORSIA 이행 지침");
        vo.setRgltSeCd("NTC");
        return vo;
    }

    // ─── Case 1: createRglt — MOLIT → RG0001 채번, 정상 저장 ────────────────

    @Test
    @DisplayName("Case 1: createRglt — MOLIT → RG0001 채번 후 정상 저장")
    void createRglt_MOLIT_정상생성() {
        RgltVO req = makeRgltVO(null);
        RgltVO saved = makeRgltVO("RG0001");

        given(rgltMapper.countByPrefix()).willReturn(0);
        given(idGenerator.managementPk("RG", 1)).willReturn("RG0001");
        given(rgltMapper.selectByRgltId("RG0001")).willReturn(saved);

        RgltVO result = rgltService.createRglt(req, molitUser);

        then(rgltMapper).should().insertRglt(any());
        assertThat(result.getRgltId()).isEqualTo("RG0001");
    }

    // ─── Case 2: createRglt — AIRLINE → forbidden ────────────────────────────

    @Test
    @DisplayName("Case 2: createRglt — AIRLINE → FORBIDDEN(403)")
    void createRglt_AIRLINE_forbidden() {
        RgltVO req = makeRgltVO(null);

        assertThatThrownBy(() -> rgltService.createRglt(req, airlineUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(403));

        then(rgltMapper).should(never()).insertRglt(any());
    }

    // ─── Case 3: listRglts — 전체 조회 → AIRLINE도 가능 ─────────────────────

    @Test
    @DisplayName("Case 3: listRglts — AIRLINE도 전체 조회 가능")
    void listRglts_AIRLINE_조회가능() {
        RgltVO r1 = makeRgltVO("RG0001");
        RgltVO r2 = makeRgltVO("RG0002");
        RgltSearch search = new RgltSearch();

        given(rgltMapper.selectRglts(any())).willReturn(List.of(r1, r2));
        given(rgltMapper.countRglts(any())).willReturn(2);

        PageResponse<RgltVO> result = rgltService.listRglts(search, airlineUser);

        assertThat(result.getRows()).hasSize(2);
        assertThat(result.getTotal()).isEqualTo(2);
    }

    // ─── Case 4: archiveRglt — MOLIT → soft delete 정상 실행 ────────────────

    @Test
    @DisplayName("Case 4: archiveRglt — MOLIT → 소프트 삭제 정상 실행")
    void archiveRglt_MOLIT_소프트삭제() {
        RgltVO existing = makeRgltVO("RG0001");
        given(rgltMapper.selectByRgltId("RG0001")).willReturn(existing);

        assertThatCode(() -> rgltService.archiveRglt("RG0001", molitUser))
                .doesNotThrowAnyException();

        then(rgltMapper).should().softDeleteRglt("RG0001", "molit01");
    }

    // ─── Case 5: archiveRglt — KOTSA → forbidden ─────────────────────────────

    @Test
    @DisplayName("Case 5: archiveRglt — KOTSA → FORBIDDEN(403) (KOTSA는 수정 불가)")
    void archiveRglt_KOTSA_forbidden() {
        assertThatThrownBy(() -> rgltService.archiveRglt("RG0001", kotsaUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(403));

        then(rgltMapper).should(never()).softDeleteRglt(any(), any());
    }
}
