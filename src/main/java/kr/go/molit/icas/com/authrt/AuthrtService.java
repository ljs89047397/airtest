package kr.go.molit.icas.com.authrt;

import kr.go.molit.icas.com.authrt.domain.AuthrtVO;
import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.IcasUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 시스템 권한(tn_sys_authrt) 비즈니스 서비스.
 * 모든 변경 작업은 MOLIT/KOTSA 전용.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthrtService {

    private final AuthrtMapper authrtMapper;

    /** 유효한 권한 전체 목록 */
    public List<AuthrtVO> listAuthrts() {
        return authrtMapper.selectAuthrts();
    }

    /** 단건 조회 */
    public AuthrtVO getAuthrt(String authrtId) {
        AuthrtVO vo = authrtMapper.selectAuthrt(authrtId);
        if (vo == null) throw BusinessException.notFound("권한");
        return vo;
    }

    /**
     * 권한 등록.
     * MOLIT/KOTSA 전용, authrt_id 중복 불가.
     */
    @Transactional
    public AuthrtVO createAuthrt(AuthrtVO vo, IcasUser user) {
        assertMolitOrKotsa(user);
        validateAuthrtId(vo.getAuthrtId());

        if (authrtMapper.existsAuthrtId(vo.getAuthrtId())) {
            throw BusinessException.conflict("이미 존재하는 권한 ID입니다: " + vo.getAuthrtId());
        }

        vo.setFrstRegUserId(user.getUserId());
        vo.setLastChgUserId(user.getUserId());
        authrtMapper.insertAuthrt(vo);
        return vo;
    }

    /**
     * 권한 수정.
     * MOLIT/KOTSA 전용.
     */
    @Transactional
    public void updateAuthrt(String authrtId, AuthrtVO vo, IcasUser user) {
        assertMolitOrKotsa(user);
        vo.setAuthrtId(authrtId);
        vo.setLastChgUserId(user.getUserId());
        int affected = authrtMapper.updateAuthrt(vo);
        if (affected == 0) throw BusinessException.notFound("권한");
    }

    /**
     * 권한 소프트 삭제.
     * MOLIT/KOTSA 전용.
     */
    @Transactional
    public void softDeleteAuthrt(String authrtId, IcasUser user) {
        assertMolitOrKotsa(user);
        int affected = authrtMapper.softDeleteAuthrt(authrtId, user.getUserId());
        if (affected == 0) throw BusinessException.notFound("권한");
    }

    /* ── 내부 검증 헬퍼 ─────────────────────────────── */

    private void assertMolitOrKotsa(IcasUser user) {
        if (!user.isMolitOrKotsa()) {
            throw BusinessException.forbidden("권한 관리는 MOLIT/KOTSA 사용자만 가능합니다.");
        }
    }

    private void validateAuthrtId(String authrtId) {
        if (authrtId == null || authrtId.isBlank()) {
            throw BusinessException.badRequest("권한 ID는 필수입니다.");
        }
    }
}
