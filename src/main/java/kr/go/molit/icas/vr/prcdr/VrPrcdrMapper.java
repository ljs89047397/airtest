package kr.go.molit.icas.vr.prcdr;

import kr.go.molit.icas.vr.prcdr.domain.VrPrcdrVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface VrPrcdrMapper {
    VrPrcdrVO selectByVrId(@Param("vrId") String vrId);
    int insertPrcdr(VrPrcdrVO vo);
    int updatePrcdr(VrPrcdrVO vo);
    int deleteByVrId(@Param("vrId") String vrId);
}
