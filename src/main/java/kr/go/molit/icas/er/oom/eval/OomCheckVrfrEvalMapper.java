package kr.go.molit.icas.er.oom.eval;

import kr.go.molit.icas.er.oom.eval.domain.OomCheckVrfrEvalVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * OoM 검증기관 품질 평가 매퍼 (er.tn_oom_check_vrfr_eval).
 * SQL: {@code mapper/er/oom/eval/OomCheckVrfrEvalMapper.xml}.
 */
@Mapper
public interface OomCheckVrfrEvalMapper {

    List<OomCheckVrfrEvalVO> selectByOomId(@Param("oomId") String oomId);

    OomCheckVrfrEvalVO selectOne(@Param("oomId") String oomId,
                                   @Param("vrfcnInstId") String vrfcnInstId);

    int insertEval(OomCheckVrfrEvalVO vo);
    int updateEval(OomCheckVrfrEvalVO vo);
    int softDeleteOne(@Param("oomId") String oomId,
                      @Param("vrfcnInstId") String vrfcnInstId,
                      @Param("userId") String userId);
}
