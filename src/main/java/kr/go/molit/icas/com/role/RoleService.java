package kr.go.molit.icas.com.role;

import kr.go.molit.icas.com.role.domain.RoleVO;
import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.IcasUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * 역할(tn_role) 비즈니스 서비스.
 * 모든 변경 작업은 MOLIT/KOTSA 전용.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoleService {

    /** ognz_se_cd_allowed 허용 토큰 집합 */
    private static final Set<String> ALLOWED_OGNZ_TOKENS =
            Set.of("MOLIT", "KOTSA", "AIRLINE", "VERIFIER");

    private final RoleMapper roleMapper;

    /** 유효한 역할 전체 목록 */
    public List<RoleVO> listRoles() {
        return roleMapper.selectRoles();
    }

    /** 단건 조회 */
    public RoleVO getRole(String roleId) {
        RoleVO vo = roleMapper.selectRole(roleId);
        if (vo == null) throw BusinessException.notFound("역할");
        return vo;
    }

    /**
     * 역할 등록.
     * MOLIT/KOTSA 전용, role_id 중복 불가, ognz_se_cd_allowed 토큰 검증.
     */
    @Transactional
    public RoleVO createRole(RoleVO vo, IcasUser user) {
        assertMolitOrKotsa(user);
        validateRoleId(vo.getRoleId());
        validateOgnzSeCdAllowed(vo.getOgnzSeCdAllowed());

        if (roleMapper.existsRoleId(vo.getRoleId())) {
            throw BusinessException.conflict("이미 존재하는 역할 ID입니다: " + vo.getRoleId());
        }

        vo.setFrstRegUserId(user.getUserId());
        vo.setLastChgUserId(user.getUserId());
        roleMapper.insertRole(vo);
        return vo;
    }

    /**
     * 역할 수정.
     * MOLIT/KOTSA 전용, ognz_se_cd_allowed 토큰 재검증.
     */
    @Transactional
    public void updateRole(String roleId, RoleVO vo, IcasUser user) {
        assertMolitOrKotsa(user);
        validateOgnzSeCdAllowed(vo.getOgnzSeCdAllowed());

        vo.setRoleId(roleId);
        vo.setLastChgUserId(user.getUserId());
        int affected = roleMapper.updateRole(vo);
        if (affected == 0) throw BusinessException.notFound("역할");
    }

    /**
     * 역할 소프트 삭제.
     * MOLIT/KOTSA 전용.
     */
    @Transactional
    public void softDeleteRole(String roleId, IcasUser user) {
        assertMolitOrKotsa(user);
        int affected = roleMapper.softDeleteRole(roleId, user.getUserId());
        if (affected == 0) throw BusinessException.notFound("역할");
    }

    /* ── 내부 검증 헬퍼 ─────────────────────────────── */

    private void assertMolitOrKotsa(IcasUser user) {
        if (!user.isMolitOrKotsa()) {
            throw BusinessException.forbidden("역할 관리는 MOLIT/KOTSA 사용자만 가능합니다.");
        }
    }

    private void validateRoleId(String roleId) {
        if (roleId == null || roleId.isBlank()) {
            throw BusinessException.badRequest("역할 ID는 필수입니다.");
        }
    }

    /**
     * ognz_se_cd_allowed: null 불가, 쉼표 구분 각 토큰이
     * MOLIT/KOTSA/AIRLINE/VERIFIER 중 하나여야 함.
     */
    private void validateOgnzSeCdAllowed(String ognzSeCdAllowed) {
        if (ognzSeCdAllowed == null || ognzSeCdAllowed.isBlank()) {
            throw BusinessException.badRequest("허용 조직구분코드(ognzSeCdAllowed)는 필수입니다.");
        }
        String[] tokens = ognzSeCdAllowed.split(",");
        for (String token : tokens) {
            String trimmed = token.trim();
            if (!ALLOWED_OGNZ_TOKENS.contains(trimmed)) {
                throw BusinessException.badRequest(
                        "허용되지 않는 조직구분코드입니다: " + trimmed
                        + " (허용값: MOLIT, KOTSA, AIRLINE, VERIFIER)");
            }
        }
    }
}
