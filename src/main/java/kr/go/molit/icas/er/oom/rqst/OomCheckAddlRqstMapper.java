package kr.go.molit.icas.er.oom.rqst;

import kr.go.molit.icas.er.oom.rqst.domain.OomCheckAddlRqstVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * OoM 추가 설명 요청 매퍼 (er.tn_oom_check_addl_rqst).
 * SQL: {@code mapper/er/oom/rqst/OomCheckAddlRqstMapper.xml}.
 */
@Mapper
public interface OomCheckAddlRqstMapper {

    List<OomCheckAddlRqstVO> selectByOomId(@Param("oomId") String oomId);

    OomCheckAddlRqstVO selectOne(@Param("oomId") String oomId,
                                  @Param("rqstSn") int rqstSn);

    int selectNextSn(@Param("oomId") String oomId);

    int insertRqst(OomCheckAddlRqstVO vo);

    /** 응답 입력 (AIRLINE): resp_dt = NOW, resp_user_id, resp_cn */
    int updateResponse(@Param("oomId") String oomId,
                       @Param("rqstSn") int rqstSn,
                       @Param("respCn") String respCn,
                       @Param("respUserId") String respUserId);

    int softDeleteOne(@Param("oomId") String oomId,
                      @Param("rqstSn") int rqstSn,
                      @Param("userId") String userId);
}
