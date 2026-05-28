package kr.go.molit.icas.com.authrt;

import kr.go.molit.icas.com.authrt.domain.AuthrtRoleMpngVO;
import kr.go.molit.icas.com.role.RoleMapper;
import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.IcasUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 권한-역할 매핑(tn_sys_authrt_role_mpng) 비즈니스 서비스.
 * PK = (authrt_id, role_id). 모든 변경 작업은 MOLIT/KOTSA 전용.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthrtRoleMpngService {

    private final AuthrtRoleMpngMapper authrtRoleMpngMapper;
    private final AuthrtMapper         authrtMapper;
    private final RoleMapper           roleMapper;

    /**
     * 역할에 부여된 권한 목록.
     * 권한 정보(authrt_nm, authrt_desc) JOIN 포함.
     */
    public List<AuthrtRoleMpngVO> selectByRole(String roleId) {
        if (roleId == null || roleId.isBlank()) {
            throw BusinessException.badRequest("역할 ID는 필수입니다.");
        }
        return authrtRoleMpngMapper.selectByRole(roleId);
    }

    /**
     * 권한을 보유한 역할 목록.
     * 역할 정보(role_nm) JOIN 포함.
     */
    public List<AuthrtRoleMpngVO> selectByAuthrt(String authrtId) {
        if (authrtId == null || authrtId.isBlank()) {
            throw BusinessException.badRequest("권한 ID는 필수입니다.");
        }
        return authrtRoleMpngMapper.selectByAuthrt(authrtId);
    }

    /**
     * 권한-역할 매핑 추가.
     * MOLIT/KOTSA 전용. 권한·역할 유효 여부 및 중복 사전 체크.
     *
     * @param authrtId 매핑할 권한 ID
     * @param roleId   매핑할 역할 ID
     * @param by       처리자 (로그인 사용자)
     */
    @Transactional
    public void addMapping(String authrtId, String roleId, IcasUser by) {
        assertMolitOrKotsa(by);

        // 1. 권한 유효성 확인
        if (authrtMapper.selectAuthrt(authrtId) == null) {
            throw BusinessException.notFound("권한");
        }

        // 2. 역할 유효성 확인
        if (roleMapper.selectRole(roleId) == null) {
            throw BusinessException.notFound("역할");
        }

        // 3. 유효 매핑 중복 체크 — 이미 있으면 Conflict
        if (authrtRoleMpngMapper.existsActive(authrtId, roleId)) {
            throw BusinessException.conflict(
                    "권한 '" + authrtId + "'과 역할 '" + roleId + "'의 매핑이 이미 존재합니다.");
        }

        // 4. 매핑 INSERT
        AuthrtRoleMpngVO vo = new AuthrtRoleMpngVO();
        vo.setAuthrtId(authrtId);
        vo.setRoleId(roleId);
        vo.setFrstRegUserId(by.getUserId());
        vo.setLastChgUserId(by.getUserId());
        authrtRoleMpngMapper.addMapping(vo);
    }

    /**
     * 권한-역할 매핑 소프트 삭제.
     * MOLIT/KOTSA 전용. 현재 유효한 매핑의 use_end_dt = NOW() - 1분.
     *
     * @param authrtId 매핑 해제할 권한 ID
     * @param roleId   매핑 해제할 역할 ID
     * @param by       처리자 (로그인 사용자)
     */
    @Transactional
    public void removeMapping(String authrtId, String roleId, IcasUser by) {
        assertMolitOrKotsa(by);

        int affected = authrtRoleMpngMapper.removeMapping(authrtId, roleId, by.getUserId());
        if (affected == 0) {
            throw BusinessException.notFound(
                    "권한 '" + authrtId + "'과 역할 '" + roleId + "'의 활성 매핑");
        }
    }

    /* ── 내부 검증 헬퍼 ─────────────────────────────── */

    private void assertMolitOrKotsa(IcasUser user) {
        if (!user.isMolitOrKotsa()) {
            throw BusinessException.forbidden("권한-역할 매핑 관리는 MOLIT/KOTSA 사용자만 가능합니다.");
        }
    }
}
