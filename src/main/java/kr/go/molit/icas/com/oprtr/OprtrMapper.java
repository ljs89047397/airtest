package kr.go.molit.icas.com.oprtr;

import kr.go.molit.icas.com.oprtr.domain.OprtrVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OprtrMapper {

    List<OprtrVO> selectAll();

    OprtrVO selectByOprtrId(@Param("oprtrId") String oprtrId);

    List<OprtrVO> selectAccessibleForUser(@Param("ognzSeCd") String ognzSeCd,
                                          @Param("oprtrId")  String oprtrId,
                                          @Param("vrfcnInstId") String vrfcnInstId,
                                          @Param("rprtYr")   String rprtYr);

    int insert(OprtrVO vo);

    int update(OprtrVO vo);

    int softDelete(@Param("oprtrId") String oprtrId, @Param("userId") String userId);

    int countByPrefix(@Param("prefix") String prefix);

    int countByIcaoDesig(@Param("icaoDesig") String icaoDesig);

    int countByIcaoDesigExclude(@Param("icaoDesig") String icaoDesig,
                                @Param("oprtrId")   String oprtrId);
}
