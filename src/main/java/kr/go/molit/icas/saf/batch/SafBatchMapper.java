package kr.go.molit.icas.saf.batch;

import kr.go.molit.icas.saf.batch.domain.SafBatchSearch;
import kr.go.molit.icas.saf.batch.domain.SafBatchVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SafBatchMapper {
    SafBatchVO selectByBatchId(@Param("batchId") String batchId);
    long countBatches(SafBatchSearch search);
    List<SafBatchVO> selectBatches(SafBatchSearch search);
    int insertBatch(SafBatchVO vo);
    int updateBatch(SafBatchVO vo);
    int softDeleteBatch(@Param("batchId") String batchId, @Param("userId") String userId);
    /** 배치 목록 (인증서 등록용 드롭다운) */
    List<SafBatchVO> selectByOprtr(@Param("oprtrId") String oprtrId);
}
