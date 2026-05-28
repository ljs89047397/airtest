package kr.go.molit.icas.saf.mntr;

import kr.go.molit.icas.saf.mntr.domain.SafBlndMntrVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SafBlndMntrMapper {
    SafBlndMntrVO selectByPk(@Param("oprtrId") String oprtrId, @Param("rprtYr") String rprtYr);
    List<SafBlndMntrVO> selectByRprtYr(@Param("rprtYr") String rprtYr);
    int insertMntr(SafBlndMntrVO vo);
    int updateMntr(SafBlndMntrVO vo);
}
