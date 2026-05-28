package kr.go.molit.icas.er.eucr.batch;

import kr.go.molit.icas.er.eucr.batch.domain.EucrBatchVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * EUCR 배출권 배치 매퍼 (er.tn_eucr_batch).
 * SQL: {@code mapper/er/eucr/batch/EucrBatchMapper.xml}.
 *
 * <p>복합 PK: (eucr_id, batch_no). batch_no 는 사용자 입력.
 */
@Mapper
public interface EucrBatchMapper {

    List<EucrBatchVO> selectByEucrId(@Param("eucrId") String eucrId);
    EucrBatchVO selectOne(@Param("eucrId") String eucrId,
                          @Param("batchNo") String batchNo);

    boolean existsBatchNo(@Param("eucrId") String eucrId,
                          @Param("batchNo") String batchNo);

    int insertBatch(EucrBatchVO vo);
    int updateBatch(EucrBatchVO vo);
    int softDeleteOne(@Param("eucrId") String eucrId,
                      @Param("batchNo") String batchNo,
                      @Param("userId") String userId);
}
