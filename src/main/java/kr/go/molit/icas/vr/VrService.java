package kr.go.molit.icas.vr;

import kr.go.molit.icas.com.vrfcn.VrfcnAssgnMapper;
import kr.go.molit.icas.com.vrfcn.VrfcnInstMapper;
import kr.go.molit.icas.com.vrfcn.domain.VrfcnInstVO;
import kr.go.molit.icas.common.dto.PageResponse;
import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.DataScopeValidator;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.common.util.IdGenerator;
import kr.go.molit.icas.vr.domain.VrSearch;
import kr.go.molit.icas.vr.domain.VrVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * VR(검증보고서) 마스터 서비스 (SFR-024 ~ SFR-028).
 *
 * <h2>라이프사이클</h2>
 * <pre>
 *   DRAFT → SBMTD (VERIFIER 제출)
 *   SBMTD → DRAFT (KOTSA 반려, rjctRsn 필수)
 *   SBMTD → RCMDD (KOTSA 권고)
 *   RCMDD → APRVD (MOLIT 승인)
 * </pre>
 *
 * <h2>권한</h2>
 * <ul>
 *   <li>VERIFIER — 본인 배정 (oprtr_id, rprt_yr) 의 VR 만 작성·제출</li>
 *   <li>KOTSA — 반려·권고</li>
 *   <li>MOLIT — 승인</li>
 *   <li>AIRLINE — 본인 VR 조회</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VrService {

    private static final String VR_ID_PREFIX     = "VR";
    private static final Set<String> VALID_TYPE  = Set.of("ER", "EUCR");

    private final VrMapper           vrMapper;
    private final VrfcnAssgnMapper   vrfcnAssgnMapper;
    private final VrfcnInstMapper    vrfcnInstMapper;
    private final DataScopeValidator dataScopeValidator;
    private final IdGenerator        idGenerator;

    // ── 조회 ──────────────────────────────────────────────────

    public PageResponse<VrVO> searchVrs(VrSearch search, IcasUser user) {
        if (user.isMaster() || user.isMolitOrKotsa()) {
            // 전사 가시
        } else if (user.isAirline()) {
            search.setOprtrId(user.getOprtrId());
        } else if (user.isVerifier()) {
            search.setVerifierScope(true);
            search.setVrfcnInstId(user.getVrfcnInstId());
        } else {
            throw BusinessException.forbidden("VR 조회 권한이 없습니다.");
        }
        long total = vrMapper.countVrs(search);
        List<VrVO> rows = vrMapper.selectVrs(search);
        return new PageResponse<>(rows, search.getPage(), search.getPageSize(), total);
    }

    public VrVO getVr(String vrId, IcasUser user) {
        VrVO m = loadOrThrow(vrId);
        dataScopeValidator.assertOprtrAccessible(user, m.getOprtrId(), m.getRprtYr());
        return m;
    }

    // ── 신규 ──────────────────────────────────────────────────

    @Transactional
    public VrVO createVr(VrVO vo, IcasUser user) {
        assertVerifierOwn(user, vo.getVrfcnInstId());
        validateRequired(vo);

        // 배정 확인
        assertAssigned(user.getVrfcnInstId(), vo.getOprtrId(), vo.getRprtYr());

        int seq    = vrMapper.countByPrefix(VR_ID_PREFIX) + 1;
        String vrId = idGenerator.managementPk(VR_ID_PREFIX, seq);

        vo.setVrId(vrId);
        vo.setVrVer(1);
        vo.setVrStCd("DRAFT");
        vo.setVrfcnInstId(user.getVrfcnInstId());
        vo.setFrstRegUserId(user.getUserId());
        vo.setLastChgUserId(user.getUserId());

        vrMapper.insertVr(vo);
        return loadOrThrow(vrId);
    }

    @Transactional
    public VrVO updateLinks(String vrId, String erId, String eucrId, IcasUser user) {
        VrVO m = loadAndAssertDraft(vrId);
        assertVerifierOwn(user, m.getVrfcnInstId());

        m.setErId(erId);
        m.setEucrId(eucrId);
        m.setLastChgUserId(user.getUserId());
        vrMapper.updateVr(m);
        return loadOrThrow(vrId);
    }

    // ── 라이프사이클 ──────────────────────────────────────────

    @Transactional
    public void submit(String vrId, IcasUser user) {
        VrVO m = loadAndAssertDraft(vrId);
        assertVerifierOwn(user, m.getVrfcnInstId());
        assertCcrNotExpired(user.getVrfcnInstId());

        int affected = vrMapper.updateSubmit(vrId, user.getUserId());
        if (affected == 0) throw BusinessException.conflict("VR 제출에 실패했습니다.");
    }

    @Transactional
    public void recommend(String vrId, IcasUser user) {
        assertKotsa(user);
        VrVO m = loadOrThrow(vrId);
        if (!"SBMTD".equals(m.getVrStCd())) {
            throw BusinessException.conflict("SBMTD 상태의 VR 만 권고 가능합니다. 현재: " + m.getVrStCd());
        }
        int affected = vrMapper.updateRecommend(vrId, user.getUserId());
        if (affected == 0) throw BusinessException.conflict("VR 권고 처리에 실패했습니다.");
    }

    @Transactional
    public void approve(String vrId, IcasUser user) {
        assertMolit(user);
        VrVO m = loadOrThrow(vrId);
        if (!"RCMDD".equals(m.getVrStCd())) {
            throw BusinessException.conflict("RCMDD 상태의 VR 만 승인 가능합니다. 현재: " + m.getVrStCd());
        }
        int affected = vrMapper.updateApprove(vrId, user.getUserId());
        if (affected == 0) throw BusinessException.conflict("VR 승인에 실패했습니다.");
    }

    @Transactional
    public void reject(String vrId, String rjctRsn, IcasUser user) {
        assertKotsa(user);
        if (rjctRsn == null || rjctRsn.isBlank()) {
            throw BusinessException.badRequest("반려 사유(rjctRsn)는 필수입니다.");
        }
        VrVO m = loadOrThrow(vrId);
        if (!"SBMTD".equals(m.getVrStCd())) {
            throw BusinessException.conflict("SBMTD 상태의 VR 만 반려 가능합니다. 현재: " + m.getVrStCd());
        }
        int affected = vrMapper.updateReject(vrId, rjctRsn, user.getUserId());
        if (affected == 0) throw BusinessException.conflict("VR 반려 처리에 실패했습니다.");
    }

    @Transactional
    public void softDelete(String vrId, IcasUser user) {
        VrVO m = loadAndAssertDraft(vrId);
        assertVerifierOwn(user, m.getVrfcnInstId());
        int affected = vrMapper.softDeleteVr(vrId, user.getUserId());
        if (affected == 0) throw BusinessException.conflict("DRAFT 상태의 VR 만 삭제 가능합니다.");
    }

    // ── 자식 Service 헬퍼 ─────────────────────────────────────

    /**
     * DRAFT 상태 + VERIFIER 본인 소유 검증 — 자식 편집 공통 가드.
     */
    public VrVO assertVrDraftForChildEdit(String vrId, IcasUser user) {
        VrVO m = loadAndAssertDraft(vrId);
        assertVerifierOwn(user, m.getVrfcnInstId());
        return m;
    }

    /** 모든 역할 — 읽기용 가시범위 검증. */
    public VrVO loadForRead(String vrId, IcasUser user) {
        VrVO m = loadOrThrow(vrId);
        dataScopeValidator.assertOprtrAccessible(user, m.getOprtrId(), m.getRprtYr());
        return m;
    }

    // ── Private ──────────────────────────────────────────────

    private VrVO loadOrThrow(String vrId) {
        VrVO m = vrMapper.selectByVrId(vrId);
        if (m == null) throw BusinessException.notFound("검증보고서(VR)");
        return m;
    }

    private VrVO loadAndAssertDraft(String vrId) {
        VrVO m = loadOrThrow(vrId);
        if (!"DRAFT".equals(m.getVrStCd())) {
            throw BusinessException.conflict(
                    "DRAFT 상태의 VR 에서만 수행 가능합니다. 현재: " + m.getVrStCd());
        }
        return m;
    }

    private void assertVerifierOwn(IcasUser user, String vrVrfcnInstId) {
        if (user.isMaster()) return;
        if (!user.isVerifier()) {
            throw BusinessException.forbidden("검증기관(VERIFIER) 사용자만 수행할 수 있는 작업입니다.");
        }
        if (!user.getVrfcnInstId().equals(vrVrfcnInstId)) {
            throw BusinessException.forbidden("본인 소속 검증기관의 VR 만 편집할 수 있습니다.");
        }
    }

    private void assertKotsa(IcasUser user) {
        if (user.isMaster()) return;
        if (!"KOTSA".equals(user.getOgnzSeCd())) {
            throw BusinessException.forbidden("한국교통안전공단(KOTSA) 사용자만 수행할 수 있는 작업입니다.");
        }
    }

    private void assertMolit(IcasUser user) {
        if (user.isMaster()) return;
        if (!"MOLIT".equals(user.getOgnzSeCd())) {
            throw BusinessException.forbidden("국토교통부(MOLIT) 사용자만 수행할 수 있는 작업입니다.");
        }
    }

    private void assertAssigned(String vrfcnInstId, String oprtrId, String rprtYr) {
        boolean ok = vrfcnAssgnMapper.existsAssgn(vrfcnInstId, oprtrId, rprtYr);
        if (!ok) throw BusinessException.forbidden(
                "해당 항공사·보고연도에 배정되지 않은 검증기관입니다. (oprtrId=" + oprtrId + ", rprtYr=" + rprtYr + ")");
    }

    private void assertCcrNotExpired(String vrfcnInstId) {
        VrfcnInstVO inst = vrfcnInstMapper.selectByVrfcnInstId(vrfcnInstId);
        if (inst == null) throw BusinessException.notFound("검증기관(vrfcnInstId=" + vrfcnInstId + ")");
        LocalDate xprDt = inst.getIcaoCcrAccrdXprDt();
        if (xprDt == null || xprDt.isBefore(LocalDate.now())) {
            throw BusinessException.badRequest(
                    "ICAO CCR 공인이 만료된 검증기관은 VR 을 제출할 수 없습니다. 만료일: " + xprDt);
        }
    }

    private void validateRequired(VrVO vo) {
        if (vo.getOprtrId() == null || vo.getOprtrId().isBlank())
            throw BusinessException.badRequest("운영사 ID(oprtrId)는 필수입니다.");
        if (vo.getRprtYr() == null || !vo.getRprtYr().matches("\\d{4}"))
            throw BusinessException.badRequest("보고연도(rprtYr)는 4자리 숫자여야 합니다.");
        if (vo.getVrTypeCd() == null || !VALID_TYPE.contains(vo.getVrTypeCd()))
            throw BusinessException.badRequest("VR 유형(vrTypeCd)은 ER 또는 EUCR 이어야 합니다.");
    }
}
