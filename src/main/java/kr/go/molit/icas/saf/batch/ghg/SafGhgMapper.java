package kr.go.molit.icas.saf.batch.ghg;

import kr.go.molit.icas.saf.batch.ghg.domain.SafGhgVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SafGhgMapper {
    SafGhgVO selectByBatchId(@Param("batchId") String batchId);
    int insertGhg(SafGhgVO vo);
    int updateGhg(SafGhgVO vo);
    int deleteByBatchId(@Param("batchId") String batchId);
}
