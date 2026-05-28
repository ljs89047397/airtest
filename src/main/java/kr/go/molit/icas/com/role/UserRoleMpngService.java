package kr.go.molit.icas.com.role;

import kr.go.molit.icas.com.role.domain.RoleVO;
import kr.go.molit.icas.com.role.domain.UserRoleMpngVO;
import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.IcasUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * 사용자-역할 시계열 매핑(tn_user_role_mpng) 비즈니스 서비스.
 *
 * <p>시계열 설계: (user_id, role_id, use_bgng_dt) 복합 PK.
 * 같은 (user_id, role_id)라도 use_bgng_dt가 다르면 별개 행으로 이력 누적.
 * 부여/회수는 현재 유효 행만 대상으로 하며 과거 이력은 보존.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserRoleMpngService {

    private final UserRoleMpngMapper userRoleMpngMapper;
    private final RoleMapper         roleMapper;

    /** 사용자의 현재 유효 역할 목록 */
    public List<UserRoleMpngVO> selectActiveRolesByUser(String userId) {
        if (userId == null || userId.isBlank()) {
            throw BusinessException.badRequest("사용자 ID는 필수입니다.");
        }
        return userRoleMpngMapper.selectActiveRolesByUser(userId);
    }

    /** 사용자의 전체 역할 부여/회수 이력 (과거 포함) */
    public List<UserRoleMpngVO> selectRoleHistory(String userId) {
        if (userId == null || userId.isBlank()) {
            throw BusinessException.badRequest("사용자 ID는 필수입니다.");
        }
        return userRoleMpngMapper.selectRoleHistory(userId);
    }

    /**
     * 역할 부여.
     *
     * <p>MOLIT/KOTSA 전용. 부여 전 다음을 순서대로 검증:
     * <ol>
     *   <li>역할 존재 및 유효 여부</li>
     *   <li>해당 역할의 ognz_se_cd_allowed에 사용자 ognzSeCd 포함 여부 (호환성 검증)</li>
     *   <li>현재 유효한 매핑 중복 (existsActive) — 이미 있으면 409 Conflict</li>
     * </ol>
     *
     * @param userId         부여 대상 사용자 ID
     * @param roleId         부여할 역할 ID
     * @param userOgnzSeCd   대상 사용자의 조직구분코드 (Controller에서 주입)
     * @param grantedBy      부여 처리자 (로그인 사용자)
     */
    @Transactional
    public void grantRole(String userId, String roleId, String userOgnzSeCd, IcasUser grantedBy) {
        assertMolitOrKotsa(grantedBy);

        // 1. 역할 유효성 확인
        RoleVO role = roleMapper.selectRole(roleId);
        if (role == null) throw BusinessException.notFound("역할");

        // 2. ognz_se_cd_allowed 호환성 검증 — 대상 사용자 조직구분코드가 허용 목록에 있어야 함
        String allowed = role.getOgnzSeCdAllowed();
        if (allowed == null || allowed.isBlank()) {
            throw BusinessException.badRequest("역할에 허용 조직구분코드가 설정되어 있지 않습니다.");
        }
        boolean compatible = Arrays.stream(allowed.split(","))
                .map(String::trim)
                .anyMatch(token -> token.equals(userOgnzSeCd));
        if (!compatible) {
            throw BusinessException.forbidden(
                    "해당 사용자의 조직구분(" + userOgnzSeCd + ")은 역할 '" + roleId
                    + "'의 허용 범위(" + allowed + ")에 포함되지 않습니다.");
        }

        // 3. 현재 유효한 매핑 중복 체크 — 이미 부여된 경우 Conflict
        if (userRoleMpngMapper.existsActive(userId, roleId)) {
            throw BusinessException.conflict(
                    "사용자 " + userId + "에게 이미 활성화된 역할 '" + roleId + "'이(가) 존재합니다.");
        }

        // 4. 역할 부여 INSERT (시계열: 새 행 추가)
        UserRoleMpngVO vo = new UserRoleMpngVO();
        vo.setUserId(userId);
        vo.setRoleId(roleId);
        vo.setFrstRegUserId(grantedBy.getUserId());
        vo.setLastChgUserId(grantedBy.getUserId());
        userRoleMpngMapper.grantRole(vo);
    }

    /**
     * 역할 회수.
     * MOLIT/KOTSA 전용. 현재 유효한 매핑의 use_end_dt = NOW() - 1분으로 soft delete.
     * 과거 만료 이력 행은 건드리지 않음.
     */
    @Transactional
    public void revokeRole(String userId, String roleId, IcasUser revokedBy) {
        assertMolitOrKotsa(revokedBy);

        int affected = userRoleMpngMapper.revokeRole(userId, roleId, revokedBy.getUserId());
        if (affected == 0) {
            throw BusinessException.notFound(
                    "사용자 " + userId + "의 활성 역할 '" + roleId + "' 매핑");
        }
    }

    /* ── 내부 검증 헬퍼 ─────────────────────────────── */

    private void assertMolitOrKotsa(IcasUser user) {
        if (!user.isMolitOrKotsa()) {
            throw BusinessException.forbidden("사용자-역할 매핑 관리는 MOLIT/KOTSA 사용자만 가능합니다.");
        }
    }
}
