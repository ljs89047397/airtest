package kr.go.molit.icas.saf.cert;

import kr.go.molit.icas.saf.cert.domain.SafCertAuditVO;
import kr.go.molit.icas.saf.cert.domain.SafCertSearch;
import kr.go.molit.icas.saf.cert.domain.SafCertVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SafCertMapper {

    SafCertVO selectByCertId(@Param("certId") String certId);
    long countCerts(SafCertSearch search);
    List<SafCertVO> selectCerts(SafCertSearch search);
    int countByPrefix(@Param("prefix") String prefix);
    int insertCert(SafCertVO vo);
    int updateCert(SafCertVO vo);

    /** 회수 처리 — srnd_yn=Y, srnd_dt=today */
    int updateSurrender(@Param("certId") String certId, @Param("userId") String userId);

    /** 특정 배치의 미회수 인증서 구매량 합산 (혼합비율 계산용) */
    int sumActiveCertQty(@Param("oprtrId") String oprtrId, @Param("rprtYr") String rprtYr);

    int softDeleteCert(@Param("certId") String certId, @Param("userId") String userId);

    // ── 감사 추적 ──
    List<SafCertAuditVO> selectAudits(@Param("certId") String certId);
    int maxAuditSn(@Param("certId") String certId);
    int insertAudit(SafCertAuditVO vo);
}
