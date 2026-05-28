package kr.go.molit.icas.er.cef.spchn;

import kr.go.molit.icas.er.cef.spchn.domain.CefSpchnVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * CEF 공급망 MyBatis 매퍼 (er.tn_cef_spchn).
 * SQL: {@code mapper/er/cef/spchn/CefSpchnMapper.xml}.
 *
 * <p>복합 PK (cef_id, claim_no, chn_sn) — 청구건당 1:N.
 * chn_sn 은 같은 (cef_id, claim_no) 내 max+1 자동 채번.
 */
@Mapper
public interface CefSpchnMapper {

    List<CefSpchnVO> selectByClaim(@Param("cefId") String cefId,
                                   @Param("claimNo") String claimNo);

    CefSpchnVO selectOne(@Param("cefId") String cefId,
                         @Param("claimNo") String claimNo,
                         @Param("chnSn") int chnSn);

    int selectNextSn(@Param("cefId") String cefId,
                     @Param("claimNo") String claimNo);

    int insertSpchn(CefSpchnVO vo);
    int updateSpchn(CefSpchnVO vo);
    int softDeleteOne(@Param("cefId") String cefId,
                      @Param("claimNo") String claimNo,
                      @Param("chnSn") int chnSn,
                      @Param("userId") String userId);
}
