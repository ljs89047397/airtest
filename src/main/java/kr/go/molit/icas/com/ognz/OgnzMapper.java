package kr.go.molit.icas.com.ognz;

import kr.go.molit.icas.com.ognz.domain.OgnzVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * com.tn_ognz 매퍼.
 * SQL 은 OgnzMapper.xml 에 정의 (어노테이션 SQL 금지).
 */
@Mapper
public interface OgnzMapper {

    /** 유효한 기관 전체 조회 */
    List<OgnzVO> selectAll();

    /** 단건 조회 (유효구간 필터 포함) */
    OgnzVO selectByOgnzId(@Param("ognzId") String ognzId);

    /** 기관 등록 */
    int insertOgnz(OgnzVO vo);

    /** 기관 수정 */
    int updateOgnz(OgnzVO vo);

    /** 소프트 삭제 */
    int softDeleteOgnz(@Param("ognzId") String ognzId, @Param("userId") String userId);
}
