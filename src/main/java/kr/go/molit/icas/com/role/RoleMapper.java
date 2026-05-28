package kr.go.molit.icas.com.role;

import kr.go.molit.icas.com.role.domain.RoleVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RoleMapper {

    /** 유효한 역할 전체 목록 조회 */
    List<RoleVO> selectRoles();

    /** 단건 조회 (유효구간 필터) */
    RoleVO selectRole(@Param("roleId") String roleId);

    /** role_id 중복 체크 (유효·만료 포함 전체) */
    boolean existsRoleId(@Param("roleId") String roleId);

    /** 역할 등록 */
    int insertRole(RoleVO vo);

    /** 역할 수정 */
    int updateRole(RoleVO vo);

    /** 역할 소프트 삭제 (use_end_dt = NOW() - 1분) */
    int softDeleteRole(@Param("roleId") String roleId, @Param("userId") String userId);
}
