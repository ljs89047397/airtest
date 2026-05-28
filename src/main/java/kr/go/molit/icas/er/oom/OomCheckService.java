package kr.go.molit.icas.er.oom;

import kr.go.molit.icas.com.oprtr.OprtrMapper;
import kr.go.molit.icas.com.oprtr.domain.OprtrVO;
import kr.go.molit.icas.common.dto.PageResponse;
import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.DataScopeValidator;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.common.util.IdGenerator;
import kr.go.molit.icas.er.oom.domain.OomCheckVO;
import kr.go.molit.icas.er.oom.domain.OomSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * OoM-check 마스터 서비스 (SFR-033).
 *
 * <h2>라이프사이클</h2>
 * <pre>
 *   INPRG (점검 진행) → DONE (확정, oom_rslt_cd = PASS/FAIL/HOLD)
 * </pre>
 *
 * <h2>권한</h2>
 * <ul>
 *   <li>KOTSA — 생성·실행·확정 (점검 주체)</li>
 *   <li>AIRLINE — 본인 oprtrId 조회만</li>
 *   <li>VERIFIER — 배정 운영사 조회만</li>
 *   <li>MOLIT — 전체 조회 + DONE 후 사후 검토 (별도 승인 단계 없음)</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OomCheckService {

    private static final String OOM_ID_PREFIX = "OOM";
    private static final Set<String> VALID_RSLT_CD = Set.of("PASS", "FAIL", "HOLD");

    private final OomCheckMapper     oomCheckMapper;
    private final OprtrMapper        oprtrMapper;
    private final DataScopeValidator dataScopeValidator;
    private final IdGenerator        idGenerator;

    // ── 조회 ──

    public PageResponse<OomCheckVO> searchOoms(OomSearch search, IcasUser user) {
        if (user.isMaster() || user.isMolitOrKotsa()) {
            // 전체
        } else if (user.isAirline()) {
            search.setOprtrId(user.getOprtrId());
        } else if (user.isVerifier()) {
            search.setVerifierScope(true);
            search.setVrfcnInstId(user.getVrfcnInstId());
        } else {
            throw BusinessException.forbidden("OoM 조회 권한이 없습니다.");
        }
        long total = oomCheckMapper.countOoms(search);
        List<OomCheckVO> rows = oomCheckMapper.selectOoms(search);
        return new PageResponse<>(rows, search.getPage(), search.getPageSize(), total);
    }

    public OomCheckVO getOom(String oomId, IcasUser user) {
        OomCheckVO m = oomCheckMapper.selectByOomId(oomId);
        if (m == null) throw BusinessException.notFound("OoM");
        dataScopeValidator.assertOprtrAccessible(user, m.getOprtrId(), m.getRprtYr());
        return m;
    }

    // ── 신규 / 수정 / 삭제 ──

    @Transactional
    public OomCheckVO createOom(OomCheckVO vo, IcasUser user) {
        assertKotsa(user);
        validateRequired(vo);
        validateOprtrExists(vo.getOprtrId());

        // UK (oprtr_id, rprt_yr) — 동일 키 OoM 이미 있으면 차단
        OomCheckVO existing = oomCheckMapper.selectByOprtrYr(vo.getOprtrId(), vo.getRprtYr());
        if (existing != null) {
            throw BusinessException.conflict(
                    "해당 운영사·보고연도 OoM 이 이미 존재합니다: " + existing.getOomId());
        }

        int seq = oomCheckMapper.countByPrefix(OOM_ID_PREFIX) + 1;
        String oomId = idGenerator.managementPk(OOM_ID_PREFIX, seq);

        vo.setOomId(oomId);
        vo.setOomStCd("INPRG");
        vo.setInspctrUserId(user.getUserId());
        vo.setFrstRegUserId(user.getUserId());
        vo.setLastChgUserId(user.getUserId());

        oomCheckMapper.insertOom(vo);
        return oomCheckMapper.selectByOomId(oomId);
    }

    @Transactional
    public void updateLinks(String oomId, String erId, String vrId, IcasUser user) {
        assertKotsa(user);
        OomCheckVO m = loadAndAssertInprg(oomId);
        OomCheckVO vo = new OomCheckVO();
        vo.setOomId(oomId);
        vo.setErId(erId);
        vo.setVrId(vrId);
        vo.setLastChgUserId(user.getUserId());
        int affected = oomCheckMapper.updateLinks(vo);
        if (affected == 0) throw BusinessException.conflict("OoM 링크 수정에 실패했습니다.");
    }

    @Transactional
    public void softDelete(String oomId, IcasUser user) {
        assertKotsa(user);
        loadAndAssertInprg(oomId);
        int affected = oomCheckMapper.softDeleteOom(oomId, user.getUserId());
        if (affected == 0) throw BusinessException.conflict("INPRG 상태의 OoM 만 삭제 가능합니다.");
    }

    // ── 결과 확정 ──

    @Transactional
    public void finalizeOom(String oomId, String rsltCd, IcasUser user) {
        assertKotsa(user);
        if (rsltCd == null || !VALID_RSLT_CD.contains(rsltCd)) {
            throw BusinessException.badRequest(
                    "결과 코드(rsltCd) 허용값: PASS, FAIL, HOLD. 입력값: " + rsltCd);
        }
        loadAndAssertInprg(oomId);
        int affected = oomCheckMapper.updateFinalize(oomId, rsltCd, user.getUserId(), user.getUserId());
        if (affected == 0) throw BusinessException.conflict("OoM 확정에 실패했습니다.");
    }

    @Transactional
    public void hold(String oomId, IcasUser user) {
        assertKotsa(user);
        loadAndAssertInprg(oomId);
        int affected = oomCheckMapper.updateHold(oomId, user.getUserId());
        if (affected == 0) throw BusinessException.conflict("HOLD 처리에 실패했습니다.");
    }

    // ── 자식 Service 헬퍼 ──

    /** INPRG 상태 검증 — KOTSA 의 자식 편집 가드. */
    public OomCheckVO assertOomInprgForChildEdit(String oomId, IcasUser user) {
        assertKotsa(user);
        return loadAndAssertInprg(oomId);
    }

    /** 모든 사용자 — 읽기용 가시범위 검증. */
    public OomCheckVO loadForRead(String oomId, IcasUser user) {
        OomCheckVO m = oomCheckMapper.selectByOomId(oomId);
        if (m == null) throw BusinessException.notFound("OoM");
        dataScopeValidator.assertOprtrAccessible(user, m.getOprtrId(), m.getRprtYr());
        return m;
    }

    // ── Private ──

    private OomCheckVO loadAndAssertInprg(String oomId) {
        OomCheckVO m = oomCheckMapper.selectByOomId(oomId);
        if (m == null) throw BusinessException.notFound("OoM");
        if (!"INPRG".equals(m.getOomStCd())) {
            throw BusinessException.conflict(
                    "INPRG 상태의 OoM 에서만 수행 가능합니다. 현재: " + m.getOomStCd());
        }
        return m;
    }

    private void assertKotsa(IcasUser user) {
        if (user.isMaster()) return;
        if (!"KOTSA".equals(user.getOgnzSeCd())) {
            throw BusinessException.forbidden("한국교통안전공단(KOTSA) 사용자만 수행할 수 있는 작업입니다.");
        }
    }

    private void validateOprtrExists(String oprtrId) {
        OprtrVO o = oprtrMapper.selectByOprtrId(oprtrId);
        if (o == null) throw BusinessException.notFound("운영사(oprtrId=" + oprtrId + ")");
    }

    private void validateRequired(OomCheckVO vo) {
        if (vo.getOprtrId() == null || vo.getOprtrId().isBlank()) {
            throw BusinessException.badRequest("운영사 ID(oprtrId)는 필수입니다.");
        }
        if (vo.getRprtYr() == null || !vo.getRprtYr().matches("\\d{4}")) {
            throw BusinessException.badRequest("보고연도(rprtYr)는 4자리 숫자여야 합니다.");
        }
    }
}
