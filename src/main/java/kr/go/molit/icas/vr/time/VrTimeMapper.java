package kr.go.molit.icas.vr.time;

import kr.go.molit.icas.vr.time.domain.VrTimeVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface VrTimeMapper {
    VrTimeVO selectByVrId(@Param("vrId") String vrId);
    int insertTime(VrTimeVO vo);
    int updateTime(VrTimeVO vo);
    int deleteByVrId(@Param("vrId") String vrId);
}
