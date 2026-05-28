package kr.go.molit.icas.com.auth;

import kr.go.molit.icas.com.auth.domain.UserAuthInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AuthMapper {

    UserAuthInfo selectUserAuthInfo(@Param("userId") String userId);

    List<String> selectRoleIds(@Param("userId") String userId);

    List<String> selectPrgrmPathsInq(@Param("userId") String userId);

    List<String> selectPrgrmPathsInpt(@Param("userId") String userId);

    int incrementPasswordFailCount(@Param("userId") String userId);

    int resetPasswordFailCount(@Param("userId") String userId);

    int updateLastLogn(@Param("userId") String userId);

    int lockAccount(@Param("userId") String userId);

    int insertLoginHistory(@Param("userId") String userId,
                           @Param("logn_rslt_cd") String rsltCd,
                           @Param("fail_rsn") String failRsn,
                           @Param("ip_addr") String ipAddr,
                           @Param("user_agent") String userAgent);
}
