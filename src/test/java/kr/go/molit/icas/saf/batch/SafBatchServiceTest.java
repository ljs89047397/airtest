package kr.go.molit.icas.saf.batch;

import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.DataScopeValidator;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.saf.batch.blndr.SafBlndrMapper;
import kr.go.molit.icas.saf.batch.domain.SafBatchVO;
import kr.go.molit.icas.saf.batch.feed.SafFeedMapper;
import kr.go.molit.icas.saf.batch.ghg.SafGhgMapper;
import kr.go.molit.icas.saf.batch.prdc.SafPrdcSplyMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SafBatchService 단위 테스트 — 배치 등록 + 권한 + 중복차단")
class SafBatchServiceTest {

    @Mock SafBatchMapper    safBatchMapper;
    @Mock SafPrdcSplyMapper prdcSplyMapper;
    @Mock SafBlndrMapper    blndrMapper;
    @Mock SafFeedMapper     feedMapper;
    @Mock SafGhgMapper      ghgMapper;
    @Mock DataScopeValidator dataScopeValidator;

    @InjectMocks SafBatchService safBatchService;

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

    private SafBatchVO makeBatch(String batchId) {
        SafBatchVO b = new SafBatchVO();
        b.setBatchId(batchId);
        b.setOprtrId("OP0001");
        b.setBatchQty(new BigDecimal("1000.0"));
        b.setDnstySecd("DEFAULT");
        return b;
    }

    // ── create ────────────────────────────────────────────

    @Test
    @DisplayName("create: AIRLINE 정상 → 배치 등록")
    void create_AIRLINE_정상() {
        SafBatchVO vo = makeBatch("ISCC-2026-KAL-001");
        given(safBatchMapper.selectByBatchId("ISCC-2026-KAL-001")).willReturn(null).willReturn(vo);

        SafBatchVO result = safBatchService.create(vo, airline);

        verify(safBatchMapper).insertBatch(any(SafBatchVO.class));
        assertThat(result.getBatchId()).isEqualTo("ISCC-2026-KAL-001");
    }

    @Test
    @DisplayName("create: 중복 batchId → CONFLICT(409)")
    void create_중복_CONFLICT() {
        SafBatchVO vo = makeBatch("ISCC-2026-KAL-001");
        given(safBatchMapper.selectByBatchId("ISCC-2026-KAL-001")).willReturn(makeBatch("ISCC-2026-KAL-001"));

        assertThatThrownBy(() -> safBatchService.create(vo, airline))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(409));
    }

    @Test
    @DisplayName("create: KOTSA → FORBIDDEN(403)")
    void create_KOTSA_FORBIDDEN() {
        SafBatchVO vo = makeBatch("ISCC-2026-KAL-001");

        assertThatThrownBy(() -> safBatchService.create(vo, kotsa))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(403));
    }

    @Test
    @DisplayName("create: dnstySecd=ACTUAL + neatSafDnsty null → BAD_REQUEST(400)")
    void create_ACTUAL_밀도없음_BAD_REQUEST() {
        SafBatchVO vo = makeBatch("ISCC-2026-KAL-002");
        vo.setDnstySecd("ACTUAL");
        vo.setNeatSafDnsty(null);  // 누락

        assertThatThrownBy(() -> safBatchService.create(vo, airline))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));
    }

    @Test
    @DisplayName("create: batchQty <= 0 → BAD_REQUEST(400)")
    void create_물량0_BAD_REQUEST() {
        SafBatchVO vo = makeBatch("ISCC-2026-KAL-003");
        vo.setBatchQty(BigDecimal.ZERO);

        assertThatThrownBy(() -> safBatchService.create(vo, airline))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));
    }

    // ── softDelete ────────────────────────────────────────

    @Test
    @DisplayName("softDelete: 정상 삭제 → 자식 4종 + 배치 삭제")
    void softDelete_정상() {
        given(safBatchMapper.selectByBatchId("ISCC-2026-KAL-001")).willReturn(makeBatch("ISCC-2026-KAL-001"));

        safBatchService.softDelete("ISCC-2026-KAL-001", airline);

        verify(prdcSplyMapper).deleteByBatchId("ISCC-2026-KAL-001");
        verify(blndrMapper).deleteByBatchId("ISCC-2026-KAL-001");
        verify(feedMapper).deleteByBatchId("ISCC-2026-KAL-001");
        verify(ghgMapper).deleteByBatchId("ISCC-2026-KAL-001");
        verify(safBatchMapper).softDeleteBatch("ISCC-2026-KAL-001", "airline01");
    }
}
