package kr.go.molit.icas.ptl.ccr;

import kr.go.molit.icas.common.dto.PageResponse;
import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.common.util.IdGenerator;
import kr.go.molit.icas.ptl.actn.UserActnService;
import kr.go.molit.icas.ptl.ccr.domain.PtlCcrExtrSearch;
import kr.go.molit.icas.ptl.ccr.domain.PtlCcrExtrVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * CCR(ICAO 중앙등록소) 추출 서비스 (SFR-055).
 *
 * <h2>추출 흐름</h2>
 * <ol>
 *   <li>MOLIT_ADMIN 검증</li>
 *   <li>INPRG 상태로 tn_ptl_ccr_extr 행 등록 (extr_id 채번)</li>
 *   <li>동기 추출 작업: APRVD ER + EUCR 데이터 수집 → 1차 더미 처리</li>
 *   <li>완료 시 extr_st_cd = DONE + file_id 업데이트</li>
 *   <li>실패 시 extr_st_cd = FAIL + rmrk에 오류메시지</li>
 * </ol>
 *
 * <p>1차: 비동기(@Async)는 설정 복잡도로 인해 동기 처리로 대체.
 * 추출 데이터: 운영사, ER 기본정보, 국가쌍 배출량(헤더만) — 실제 CCR XML 변환은 2차.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PtlCcrExtrService {

    private static final String EXTR_PREFIX = "CE";

    private final PtlCcrExtrMapper ccrExtrMapper;
    private final UserActnService  userActnService;
    private final IdGenerator      idGenerator;

    // ── 추출 요청 ─────────────────────────────────────────────────────────────

    /**
     * CCR 추출 요청 및 동기 처리.
     * MOLIT 또는 MASTER 전용.
     */
    @Transactional
    public PtlCcrExtrVO requestExtraction(String rprtYr, String extrScopeCd, IcasUser user) {
        assertMolitOrMaster(user);

        // 1. extr_id 채번
        int seq = ccrExtrMapper.countByPrefix() + 1;
        String extrId = idGenerator.managementPk(EXTR_PREFIX, seq);

        // 2. INPRG 상태로 INSERT
        PtlCcrExtrVO vo = new PtlCcrExtrVO();
        vo.setExtrId(extrId);
        vo.setRprtYr(rprtYr);
        vo.setExtrScopeCd(extrScopeCd);
        vo.setExtrStCd("INPRG");
        vo.setExtrUserId(user.getUserId());
        vo.setFrstRegUserId(user.getUserId());
        vo.setLastChgUserId(user.getUserId());
        ccrExtrMapper.insertExtr(vo);

        // 3. 동기 추출 처리 (1차: 더미 — file_id null로 DONE 처리)
        try {
            log.info("[PtlCcrExtrService] CCR 추출 시작 — extrId={}, rprtYr={}, scope={}",
                    extrId, rprtYr, extrScopeCd);
            // TODO 2차: APRVD ER + EUCR 데이터 수집 → CSV/XML 생성 → 파일 저장
            ccrExtrMapper.updateStatus(extrId, "DONE", null, user.getUserId());
            log.info("[PtlCcrExtrService] CCR 추출 완료 — extrId={}", extrId);
        } catch (Exception e) {
            log.error("[PtlCcrExtrService] CCR 추출 실패 — extrId={}: {}", extrId, e.getMessage(), e);
            ccrExtrMapper.updateStatus(extrId, "FAIL", null, user.getUserId());
        }

        // 4. 감사 로그
        userActnService.log("CCR_EXTR", "ptl.tn_ptl_ccr_extr", extrId, "SUCCESS", user.getUserId());

        return ccrExtrMapper.selectByExtrId(extrId);
    }

    // ── 조회 ──────────────────────────────────────────────────────────────────

    /**
     * 단건 조회. MOLIT 또는 MASTER 전용.
     */
    public PtlCcrExtrVO getExtraction(String extrId, IcasUser user) {
        assertMolitOrMaster(user);
        PtlCcrExtrVO vo = ccrExtrMapper.selectByExtrId(extrId);
        if (vo == null) {
            throw BusinessException.notFound("CCR 추출 이력 (" + extrId + ")");
        }
        return vo;
    }

    /**
     * 목록 조회 (페이징). MOLIT 또는 MASTER 전용.
     */
    public PageResponse<PtlCcrExtrVO> listExtractions(PtlCcrExtrSearch search, IcasUser user) {
        assertMolitOrMaster(user);
        List<PtlCcrExtrVO> rows = ccrExtrMapper.selectExtrs(search);
        int total = ccrExtrMapper.countExtrs(search);
        return new PageResponse<>(rows, search.getPage(), search.getPageSize(), total);
    }

    // ── private ───────────────────────────────────────────────────────────────

    private void assertMolitOrMaster(IcasUser user) {
        if (user.isMaster()) return;
        if (!user.isMolitOrKotsa() || !"MOLIT".equals(user.getOgnzSeCd())) {
            throw BusinessException.forbidden("CCR 추출은 MOLIT 사용자만 가능합니다.");
        }
    }
}
