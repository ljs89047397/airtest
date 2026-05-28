package kr.go.molit.icas.com.authrt;

import kr.go.molit.icas.com.authrt.domain.AuthrtRoleMpngVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AuthrtRoleMpngMapper {

    /** (authrt_id, role_id) 유효 매핑 존재 여부 — 중복 체크용 */
    boolean existsActive(@Param("authrtId") String authrtId, @Param("roleId") String roleId);

    /**
     * 역할에 부여된 권한 목록.
     * 권한 정보(authrt_nm, authrt_desc) JOIN 포함.
     */
    List<AuthrtRoleMpngVO> selectByRole(@Param("roleId") String roleId);

    /**
     * 권한을 보유한 역할 목록.
     * 역할 정보(role_nm) JOIN 포함.
     */
    List<AuthrtRoleMpngVO> selectByAuthrt(@Param("authrtId") String authrtId);

    /** 권한-역할 매핑 추가 */
    int addMapping(AuthrtRoleMpngVO vo);

    /**
     * 권한-역할 매핑 소프트 삭제.
     * use_end_dt = NOW() - 1분.
     */
    int removeMapping(@Param("authrtId") String authrtId,
                      @Param("roleId") String roleId,
                      @Param("lastChgUserId") String lastChgUserId);
}
