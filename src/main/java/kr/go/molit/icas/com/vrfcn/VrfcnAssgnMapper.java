package kr.go.molit.icas.com.vrfcn;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface VrfcnAssgnMapper {

    boolean existsAssgn(@Param("vrfcnInstId") String vrfcnInstId,
                        @Param("oprtrId")     String oprtrId,
                        @Param("rprtYr")      String rprtYr);

    List<String> selectAssignedOprtrIds(@Param("vrfcnInstId") String vrfcnInstId,
                                        @Param("rprtYr")     String rprtYr);

    List<Map<String, Object>> selectAssgnList(@Param("rprtYr") String rprtYr);

    int insertAssgn(@Param("vrfcnInstId") String vrfcnInstId,
                    @Param("oprtrId")     String oprtrId,
                    @Param("rprtYr")      String rprtYr,
                    @Param("userId")      String userId);

    int softDeleteAssgn(@Param("vrfcnInstId") String vrfcnInstId,
                        @Param("oprtrId")     String oprtrId,
                        @Param("rprtYr")      String rprtYr,
                        @Param("userId")      String userId);
}
