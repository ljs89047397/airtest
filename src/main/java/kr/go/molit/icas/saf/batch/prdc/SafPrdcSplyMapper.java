package kr.go.molit.icas.saf.batch.prdc;

import kr.go.molit.icas.saf.batch.prdc.domain.SafPrdcSplyVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SafPrdcSplyMapper {
    SafPrdcSplyVO selectByBatchId(@Param("batchId") String batchId);
    int insertPrdc(SafPrdcSplyVO vo);
    int updatePrdc(SafPrdcSplyVO vo);
    int deleteByBatchId(@Param("batchId") String batchId);
}
