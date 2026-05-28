package kr.go.molit.icas.com.role;

import kr.go.molit.icas.com.role.domain.UserRoleMpngVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserRoleMpngMapper {

    /**
     * 현재 유효한 역할 부여 존재 여부.
     * grantRole 호출 전 충돌 체크에 사용.
     */
    boolean existsActive(@Param("userId") String userId, @Param("roleId") String roleId);

    /**
     * 사용자의 현재 유효 역할 목록 (role 정보 JOIN 포함).
     */
    List<UserRoleMpngVO> selectActiveRolesByUser(@Param("userId") String userId);

    /**
     * 사용자의 전체 역할 부여/회수 이력 (use_bgng_dt DESC).
     * 과거 만료 행 포함 — 유효구간 필터 없음.
     */
    List<UserRoleMpngVO> selectRoleHistory(@Param("userId") String userId);

    /**
     * 역할 부여 INSERT. use_bgng_dt = NOW(), use_end_dt = '9999-12-31 23:59:59'.
     */
    int grantRole(UserRoleMpngVO vo);

    /**
     * 역할 회수: 현재 유효한 행의 use_end_dt = NOW() - 1분.
     * 과거 만료 행은 건드리지 않음.
     */
    int revokeRole(@Param("userId") String userId,
                   @Param("roleId") String roleId,
                   @Param("lastChgUserId") String lastChgUserId);
}
