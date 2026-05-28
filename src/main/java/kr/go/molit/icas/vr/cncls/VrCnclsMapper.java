package kr.go.molit.icas.vr.cncls;

import kr.go.molit.icas.vr.cncls.domain.VrCnclsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface VrCnclsMapper {
    VrCnclsVO selectByVrId(@Param("vrId") String vrId);
    int insertCncls(VrCnclsVO vo);
    int updateCncls(VrCnclsVO vo);
    int deleteByVrId(@Param("vrId") String vrId);
}
