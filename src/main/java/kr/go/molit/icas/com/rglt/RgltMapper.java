package kr.go.molit.icas.com.rglt;

import kr.go.molit.icas.com.rglt.domain.RgltSearch;
import kr.go.molit.icas.com.rglt.domain.RgltVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RgltMapper {
    /** 단건 조회 */
    RgltVO selectByRgltId(String rgltId);

    /** 목록 조회 (검색 + 페이징) */
    List<RgltVO> selectRglts(RgltSearch search);

    /** 총 건수 (페이징용) */
    int countRglts(RgltSearch search);

    /** 채번: RG prefix 다음 순번 */
    int countByPrefix();

    /** 규정 등록 */
    void insertRglt(RgltVO vo);

    /** 규정 수정 */
    void updateRglt(RgltVO vo);

    /** 소프트 삭제 */
    void softDeleteRglt(@Param("rgltId") String rgltId, @Param("userId") String userId);
}
