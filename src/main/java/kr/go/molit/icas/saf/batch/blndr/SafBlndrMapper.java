package kr.go.molit.icas.saf.batch.blndr;

import kr.go.molit.icas.saf.batch.blndr.domain.SafBlndrVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SafBlndrMapper {
    SafBlndrVO selectByBatchId(@Param("batchId") String batchId);
    int insertBlndr(SafBlndrVO vo);
    int updateBlndr(SafBlndrVO vo);
    int deleteByBatchId(@Param("batchId") String batchId);
}
