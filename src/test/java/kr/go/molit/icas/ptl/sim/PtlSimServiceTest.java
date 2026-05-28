package kr.go.molit.icas.ptl.sim;

import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.common.util.IdGenerator;
import kr.go.molit.icas.ptl.sim.domain.PtlSimSearch;
import kr.go.molit.icas.ptl.sim.domain.PtlSimVO;
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
@DisplayName("PtlSimService 단위 테스트")
class PtlSimServiceTest {

    @Mock PtlSimMapper simMapper;
    @Mock IdGenerator  idGenerator;

    @InjectMocks
    PtlSimService simService;

    private IcasUser molitUser;
    private IcasUser airlineUser;
    private IcasUser otherUser;

    @BeforeEach
    void setUp() {
        molitUser = IcasUser.builder()
                .userId("molit01").ognzSeCd("MOLIT").master(false)
                .roleIds(List.of("ADMIN")).build();

        airlineUser = IcasUser.builder()
                .userId("airline01").ognzSeCd("AIRLINE").oprtrId("OP0001").master(false)
                .roleIds(List.of("AIRLINE_USER")).build();

        otherUser = IcasUser.builder()
                .userId("other01").ognzSeCd("AIRLINE").oprtrId("OP0002").master(false)
                .roleIds(List.of("AIRLINE_USER")).build();
    }

    private String validInputJson() {
        return """
                {
                  "growthRate": 3.0,
                  "baseEmission": 100000.0,
                  "fromYr": 2024,
                  "toYr": 2026,
                  "carbonPrice": {"2024": 15.0, "2025": 17.0, "2026": 20.0},
                  "safRatio":    {"2024": 2.0,  "2025": 3.0,  "2026": 5.0}
                }
                """;
    }

    // ─── Case 1: createSim — MOLIT → 정상, SM 채번, rslt_json 생성 ──────────

    @Test
    @DisplayName("Case 1: createSim — MOLIT → SM 채번 + rslt_json 생성 정상 동작")
    void createSim_MOLIT_정상생성() {
        PtlSimVO req = new PtlSimVO();
        req.setSimNm("테스트 시뮬레이션");
        req.setInputJson(validInputJson());

        PtlSimVO saved = new PtlSimVO();
        saved.setSimId("SM0001");
        saved.setOwnerUserId("molit01");
        saved.setRsltJson("[{\"year\":2024}]");

        given(idGenerator.managementPk("SM", 1)).willReturn("SM0001");
        given(simMapper.countByPrefix()).willReturn(0);
        given(simMapper.selectBySimId("SM0001")).willReturn(saved);

        PtlSimVO result = simService.createSim(req, molitUser);

        then(simMapper).should().insertSim(any());
        assertThat(result.getSimId()).isEqualTo("SM0001");
        assertThat(result.getRsltJson()).isNotNull();
    }

    // ─── Case 2: createSim — inputJson null → rslt_json null (계산 생략) ─────

    @Test
    @DisplayName("Case 2: createSim — inputJson null → rslt_json null (계산 생략)")
    void createSim_inputJson_null_rsltJson_null() {
        PtlSimVO req = new PtlSimVO();
        req.setSimNm("입력 없는 시뮬레이션");
        req.setInputJson(null);

        PtlSimVO saved = new PtlSimVO();
        saved.setSimId("SM0001");
        saved.setRsltJson(null);

        given(idGenerator.managementPk("SM", 1)).willReturn("SM0001");
        given(simMapper.countByPrefix()).willReturn(0);
        given(simMapper.selectBySimId("SM0001")).willReturn(saved);

        PtlSimVO result = simService.createSim(req, airlineUser);

        then(simMapper).should().insertSim(any());
        assertThat(result.getRsltJson()).isNull();
    }

    // ─── Case 3: getSim — PRIVATE 타인 접근 → forbidden ─────────────────────

    @Test
    @DisplayName("Case 3: getSim — PRIVATE 시뮬레이션 타인 접근 → FORBIDDEN(403)")
    void getSim_PRIVATE_타인접근_forbidden() {
        PtlSimVO vo = new PtlSimVO();
        vo.setSimId("SM0001");
        vo.setOwnerUserId("airline01");
        vo.setShareSeCd("PRIVATE");
        given(simMapper.selectBySimId("SM0001")).willReturn(vo);

        assertThatThrownBy(() -> simService.getSim("SM0001", otherUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(403));
    }

    // ─── Case 4: getSim — PUBLIC → 누구나 가능 ──────────────────────────────

    @Test
    @DisplayName("Case 4: getSim — PUBLIC 시뮬레이션 → 타인도 정상 조회")
    void getSim_PUBLIC_누구나가능() {
        PtlSimVO vo = new PtlSimVO();
        vo.setSimId("SM0001");
        vo.setOwnerUserId("airline01");
        vo.setShareSeCd("PUBLIC");
        given(simMapper.selectBySimId("SM0001")).willReturn(vo);

        assertThatCode(() -> simService.getSim("SM0001", otherUser))
                .doesNotThrowAnyException();
    }

    // ─── Case 5: deleteSim — 소유자 → 정상 삭제 ─────────────────────────────

    @Test
    @DisplayName("Case 5: deleteSim — 소유자 본인 → 정상 삭제")
    void deleteSim_소유자_정상삭제() {
        PtlSimVO vo = new PtlSimVO();
        vo.setSimId("SM0001");
        vo.setOwnerUserId("airline01");
        given(simMapper.selectBySimId("SM0001")).willReturn(vo);

        assertThatCode(() -> simService.deleteSim("SM0001", airlineUser))
                .doesNotThrowAnyException();

        then(simMapper).should().softDeleteSim("SM0001", "airline01");
    }

    // ─── Case 6: deleteSim — 타인 → forbidden ──────────────────────────────

    @Test
    @DisplayName("Case 6: deleteSim — 타인 접근 → FORBIDDEN(403)")
    void deleteSim_타인_forbidden() {
        PtlSimVO vo = new PtlSimVO();
        vo.setSimId("SM0001");
        vo.setOwnerUserId("airline01");
        given(simMapper.selectBySimId("SM0001")).willReturn(vo);

        assertThatThrownBy(() -> simService.deleteSim("SM0001", otherUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(403));
    }

    // ─── Case 7: runSim — input_json 파싱 오류 → badRequest ─────────────────

    @Test
    @DisplayName("Case 7: runSim — input_json 파싱 오류 → BAD_REQUEST(400)")
    void runSim_inputJson_파싱오류_badRequest() {
        PtlSimVO vo = new PtlSimVO();
        vo.setSimId("SM0001");
        vo.setOwnerUserId("molit01");
        vo.setShareSeCd("PUBLIC");
        vo.setInputJson("{invalid json !!!");
        given(simMapper.selectBySimId("SM0001")).willReturn(vo);

        assertThatThrownBy(() -> simService.runSim("SM0001", molitUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));
    }
}
