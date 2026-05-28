package kr.go.molit.icas.vr.inpt;

import kr.go.molit.icas.vr.inpt.domain.VrInputInfoVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface VrInputInfoMapper {
    List<VrInputInfoVO> selectByVrId(@Param("vrId") String vrId);
    VrInputInfoVO selectByPk(@Param("vrId") String vrId, @Param("inputSn") int inputSn);
    int maxInputSn(@Param("vrId") String vrId);
    int insertInputInfo(VrInputInfoVO vo);
    int updateInputInfo(VrInputInfoVO vo);
    int deleteByPk(@Param("vrId") String vrId, @Param("inputSn") int inputSn);
    int deleteByVrId(@Param("vrId") String vrId);
}
