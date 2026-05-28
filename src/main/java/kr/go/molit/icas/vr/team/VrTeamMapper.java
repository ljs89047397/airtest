package kr.go.molit.icas.vr.team;

import kr.go.molit.icas.vr.team.domain.VrTeamVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface VrTeamMapper {
    List<VrTeamVO> selectByVrId(@Param("vrId") String vrId);
    VrTeamVO selectByPk(@Param("vrId") String vrId, @Param("teamSn") int teamSn);
    int maxTeamSn(@Param("vrId") String vrId);
    int insertTeam(VrTeamVO vo);
    int updateTeam(VrTeamVO vo);
    int deleteByPk(@Param("vrId") String vrId, @Param("teamSn") int teamSn);
    int deleteByVrId(@Param("vrId") String vrId);
    /** LEAD 의 연속 검증 횟수 조회 (OoM Rule17 연동) */
    Integer selectLeadConscutvCnt(@Param("vrId") String vrId);
}
