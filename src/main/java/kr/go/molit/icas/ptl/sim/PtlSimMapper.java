package kr.go.molit.icas.ptl.sim;

import kr.go.molit.icas.ptl.sim.domain.PtlSimSearch;
import kr.go.molit.icas.ptl.sim.domain.PtlSimVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PtlSimMapper {
    /** 단건 조회 */
    PtlSimVO selectBySimId(String simId);

    /** 목록 조회 (검색 + 페이징) */
    List<PtlSimVO> selectSims(PtlSimSearch search);

    /** 총 건수 (페이징용) */
    int countSims(PtlSimSearch search);

    /** 채번: SM prefix 다음 순번 */
    int countByPrefix();

    /** 신규 시뮬레이션 등록 */
    void insertSim(PtlSimVO vo);

    /** 시뮬레이션 수정 (sim_nm, scope_*, base_yr, prdctn_yr_*, input_json, rslt_json, share_se_cd) */
    void updateSim(PtlSimVO vo);

    /** 소프트 삭제 */
    void softDeleteSim(@Param("simId") String simId, @Param("userId") String userId);
}
