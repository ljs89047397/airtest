package kr.go.molit.icas.saf.batch.feed;

import kr.go.molit.icas.saf.batch.feed.domain.SafFeedVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SafFeedMapper {
    SafFeedVO selectByBatchId(@Param("batchId") String batchId);
    int insertFeed(SafFeedVO vo);
    int updateFeed(SafFeedVO vo);
    int deleteByBatchId(@Param("batchId") String batchId);
}
