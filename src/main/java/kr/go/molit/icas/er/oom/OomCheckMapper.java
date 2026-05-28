package kr.go.molit.icas.er.oom;

import kr.go.molit.icas.er.oom.domain.OomCheckVO;
import kr.go.molit.icas.er.oom.domain.OomSearch;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * OoM-check 마스터 매퍼 (er.tn_oom_check).
 * SQL: {@code mapper/er/oom/OomCheckMapper.xml}.
 */
@Mapper
public interface OomCheckMapper {

    List<OomCheckVO> selectOoms(OomSearch search);
    long countOoms(OomSearch search);
    OomCheckVO selectByOomId(@Param("oomId") String oomId);
    OomCheckVO selectByOprtrYr(@Param("oprtrId") String oprtrId,
                                @Param("rprtYr") String rprtYr);

    int countByPrefix(@Param("prefix") String prefix);

    int insertOom(OomCheckVO vo);
    int updateLinks(OomCheckVO vo);
    int softDeleteOom(@Param("oomId") String oomId, @Param("userId") String userId);

    /** 결과 확정: oom_st_cd = DONE, oom_rslt_cd = #{rsltCd}, inspn_dt = NOW */
    int updateFinalize(@Param("oomId") String oomId,
                       @Param("rsltCd") String rsltCd,
                       @Param("inspctrUserId") String inspctrUserId,
                       @Param("userId") String userId);

    /** HOLD 처리 (재개 가능) — oom_st_cd 유지, rslt_cd 만 HOLD */
    int updateHold(@Param("oomId") String oomId, @Param("userId") String userId);
}
