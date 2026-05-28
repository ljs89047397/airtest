package kr.go.molit.icas.vr.scope;

import kr.go.molit.icas.vr.scope.domain.VrScopeVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface VrScopeMapper {
    VrScopeVO selectByVrId(@Param("vrId") String vrId);
    int insertScope(VrScopeVO vo);
    int updateScope(VrScopeVO vo);
    int deleteByVrId(@Param("vrId") String vrId);
}
