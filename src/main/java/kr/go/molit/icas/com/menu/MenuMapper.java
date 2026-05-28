package kr.go.molit.icas.com.menu;

import kr.go.molit.icas.com.menu.domain.MenuVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MenuMapper {

    /** 시스템 구분으로 유효 메뉴 전체 평면 목록 (트리 조립용) */
    List<MenuVO> selectAllBySysSeCd(@Param("sysSeCd") String sysSeCd);

    /** 단건 조회 */
    MenuVO selectMenu(@Param("menuId") String menuId);

    /** 상위 메뉴 ID 기준 하위 메뉴 수 (삭제 전 자식 유무 확인) */
    int countChildren(@Param("menuId") String menuId);

    /** ID 중복 체크 */
    boolean existsMenu(@Param("menuId") String menuId);

    /** 등록 */
    int insertMenu(MenuVO vo);

    /** 수정 */
    int updateMenu(MenuVO vo);

    /** 소프트 삭제 */
    int softDeleteMenu(@Param("menuId") String menuId, @Param("userId") String userId);
}
