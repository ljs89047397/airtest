package kr.go.molit.icas.saf.cert;

import kr.go.molit.icas.common.dto.PageResponse;
import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.DataScopeValidator;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.common.util.IdGenerator;
import kr.go.molit.icas.saf.cert.domain.SafCertAuditVO;
import kr.go.molit.icas.saf.cert.domain.SafCertSearch;
import kr.go.molit.icas.saf.cert.domain.SafCertVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * SAF 인증서 서비스 (SFR-035/036).
 *
 * <h2>회수(Surrender) 처리</h2>
 * <ol>
 *   <li>srnd_yn N → Y, srnd_dt = today</li>
 *   <li>tn_saf_cert_audit 에 actn_cd=SRND 행 INSERT</li>
 *   <li>회수된 인증서는 수정·삭제 불가 (영구 보존)</li>
 * </ol>
 */
@Service
@RequiredArgsConstructor
@Transactional
public class SafCertService {

    private static final String CERT_ID_PREFIX = "SC";

    private final SafCertMapper      safCertMapper;
    private final DataScopeValidator dataScopeValidator;
    private final IdGenerator        idGenerator;

    // ── 조회 ──

    public PageResponse<SafCertVO> search(SafCertSearch search, IcasUser user) {
        if (user.isMaster() || user.isMolitOrKotsa()) {
            // 전사
        } else if (user.isAirline()) {
            search.setOprtrId(user.getOprtrId());
        } else {
            throw BusinessException.forbidden("SAF 인증서 조회 권한이 없습니다.");
        }
        long total = safCertMapper.countCerts(search);
        List<SafCertVO> rows = safCertMapper.selectCerts(search);
        return new PageResponse<>(rows, search.getPage(), search.getPageSize(), total);
    }

    public SafCertVO get(String certId, IcasUser user) {
        SafCertVO m = loadOrThrow(certId);
        if (!user.isMaster() && !user.isMolitOrKotsa() && user.isAirline()) {
            dataScopeValidator.assertOwnAirline(user, m.getOprtrId());
        }
        addAudit(certId, "VIEW", user.getUserId());
        return m;
    }

    public List<SafCertAuditVO> getAudits(String certId, IcasUser user) {
        SafCertVO m = loadOrThrow(certId);
        if (!user.isMaster() && !user.isMolitOrKotsa() && user.isAirline()) {
            dataScopeValidator.assertOwnAirline(user, m.getOprtrId());
        }
        return safCertMapper.selectAudits(certId);
    }

    // ── 신규 ──

    @Transactional
    public SafCertVO register(SafCertVO vo, IcasUser user) {
        assertAirline(user);
        dataScopeValidator.assertOwnAirline(user, vo.getOprtrId());
        validateCert(vo);

        int seq = safCertMapper.countByPrefix(CERT_ID_PREFIX) + 1;
        String certId = idGenerator.managementPk(CERT_ID_PREFIX, seq);
        vo.setCertId(certId);
        vo.setFrstRegUserId(user.getUserId());
        vo.setLastChgUserId(user.getUserId());

        safCertMapper.insertCert(vo);
        addAudit(certId, "UPLD", user.getUserId());
        return loadOrThrow(certId);
    }

    // ── 수정 ──

    @Transactional
    public SafCertVO update(String certId, SafCertVO vo, IcasUser user) {
        assertAirline(user);
        SafCertVO m = loadOrThrow(certId);
        dataScopeValidator.assertOwnAirline(user, m.getOprtrId());
        assertNotSurrendered(m);

        vo.setCertId(certId);
        vo.setLastChgUserId(user.getUserId());
        safCertMapper.updateCert(vo);
        return loadOrThrow(certId);
    }

    // ── 회수 ──

    @Transactional
    public void surrender(String certId, IcasUser user) {
        assertAirline(user);
        SafCertVO m = loadOrThrow(certId);
        dataScopeValidator.assertOwnAirline(user, m.getOprtrId());
        assertNotSurrendered(m);

        int affected = safCertMapper.updateSurrender(certId, user.getUserId());
        if (affected == 0) throw BusinessException.conflict("인증서 회수 처리에 실패했습니다.");
        addAudit(certId, "SRND", user.getUserId());
    }

    // ── 삭제 ──

    @Transactional
    public void softDelete(String certId, IcasUser user) {
        assertAirline(user);
        SafCertVO m = loadOrThrow(certId);
        dataScopeValidator.assertOwnAirline(user, m.getOprtrId());
        assertNotSurrendered(m);
        safCertMapper.softDeleteCert(certId, user.getUserId());
    }

    // ── Private ──

    private SafCertVO loadOrThrow(String certId) {
        SafCertVO m = safCertMapper.selectByCertId(certId);
        if (m == null) throw BusinessException.notFound("SAF 인증서(certId=" + certId + ")");
        return m;
    }

    private void assertNotSurrendered(SafCertVO m) {
        if ("Y".equals(m.getSrndYn())) {
            throw BusinessException.conflict("이미 회수된 인증서는 수정·삭제할 수 없습니다. certId=" + m.getCertId());
        }
    }

    private void assertAirline(IcasUser user) {
        if (user.isMaster()) return;
        if (!user.isAirline()) throw BusinessException.forbidden("항공사 사용자만 SAF 인증서를 등록·수정할 수 있습니다.");
    }

    private void validateCert(SafCertVO vo) {
        if (vo.getOprtrId() == null || vo.getOprtrId().isBlank())
            throw BusinessException.badRequest("운영사 ID(oprtrId)는 필수입니다.");
        if (vo.getCertTypeCd() == null || !List.of("PoS","PoC").contains(vo.getCertTypeCd()))
            throw BusinessException.badRequest("인증서 유형(certTypeCd)은 PoS 또는 PoC 여야 합니다.");
        if (vo.getCertSchmCd() == null || vo.getCertSchmCd().isBlank())
            throw BusinessException.badRequest("인증 체계(certSchmCd)는 필수입니다.");
    }

    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void addAudit(String certId, String actnCd, String userId) {
        int sn = safCertMapper.maxAuditSn(certId) + 1;
        SafCertAuditVO audit = new SafCertAuditVO();
        audit.setCertId(certId);
        audit.setAuditSn(sn);
        audit.setActnCd(actnCd);
        audit.setUserId(userId);
        safCertMapper.insertAudit(audit);
    }
}
