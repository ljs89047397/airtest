package kr.go.molit.icas.er.cef.lcyc;

import kr.go.molit.icas.er.cef.lcyc.domain.CefLcycVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * CEF 수명주기 배출량 MyBatis 매퍼 (er.tn_cef_lcyc).
 * SQL: {@code mapper/er/cef/lcyc/CefLcycMapper.xml}.
 *
 * <p>복합 PK (cef_id, claim_no) — 청구건당 0..1 행 (옵셔널).
 * upsert 시멘틱: claim 당 1행이므로 insert/update 를 별도로 호출.
 */
@Mapper
public interface CefLcycMapper {

    CefLcycVO selectOne(@Param("cefId") String cefId,
                        @Param("claimNo") String claimNo);

    int insertLcyc(CefLcycVO vo);
    int updateLcyc(CefLcycVO vo);
    int softDeleteOne(@Param("cefId") String cefId,
                      @Param("claimNo") String claimNo,
                      @Param("userId") String userId);
}
