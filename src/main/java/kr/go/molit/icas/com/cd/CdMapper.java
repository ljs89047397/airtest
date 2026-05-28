package kr.go.molit.icas.com.cd;

import kr.go.molit.icas.com.cd.domain.CdDtlVO;
import kr.go.molit.icas.com.cd.domain.CdGroupVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CdMapper {

    List<CdGroupVO> selectCdGroups();

    List<CdDtlVO> selectCdDtlsByGroup(@Param("grpId") String grpId);

    boolean existsValidCd(@Param("grpId") String grpId, @Param("cd") String cd);

    CdDtlVO selectCdDtl(@Param("grpId") String grpId, @Param("cd") String cd);

    int insertCdGroup(CdGroupVO vo);

    int updateCdGroup(CdGroupVO vo);

    int softDeleteCdGroup(@Param("grpId") String grpId, @Param("userId") String userId);

    int insertCdDtl(CdDtlVO vo);

    int updateCdDtl(CdDtlVO vo);

    int softDeleteCdDtl(@Param("grpId") String grpId, @Param("cd") String cd, @Param("userId") String userId);
}
