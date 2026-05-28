package kr.go.molit.icas.com.user;

import kr.go.molit.icas.com.ognz.OgnzService;
import kr.go.molit.icas.com.user.domain.UserSearch;
import kr.go.molit.icas.com.user.domain.UserVO;
import kr.go.molit.icas.common.dto.PageResponse;
import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.common.util.Sha256;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;

/**
 * 사용자(tn_user) 비즈니스 서비스.
 *
 * <p>비밀번호 정책: 9자 이상, 영문 + 숫자 + 특수문자 포함 (SER-009 §비밀번호 복잡성)
 * <p>비밀번호 해시: {@link Sha256#hex(String)} — 내부적으로 "icas" + plain + "cems" salt 처리
 * <p>개인정보 마스킹: List 응답에서 적용. 본인 또는 master 는 풀 노출.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    /** 비밀번호 복잡성 정규식: 9자 이상, 영문·숫자·특수문자 각 1자 이상 */
    private static final Pattern PASSWORD_POLICY =
            Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{9,}$");

    private final UserMapper userMapper;
    private final OgnzService ognzService;

    /* ════════════════════════════════════════════
     *  조회
     * ════════════════════════════════════════════ */

    /**
     * 사용자 목록 검색 + 페이징 (MOLIT/KOTSA 전용).
     * List 응답에서 개인정보 마스킹, pswd_hash null 처리.
     */
    public PageResponse<UserVO> searchUsers(UserSearch search, IcasUser user) {
        List<UserVO> rows = userMapper.selectUsers(search);
        long total = userMapper.countUsers(search);

        rows.forEach(vo -> {
            clearSensitiveFields(vo);
            applyMasking(vo, user);
        });

        return new PageResponse<>(rows, search.getPage(), search.getPageSize(), total);
    }

    /**
     * 단건 조회 (MOLIT/KOTSA 또는 본인).
     * 본인이면 마스킹 없음, 타인이면 마스킹 적용.
     */
    public UserVO getUser(String userId, IcasUser user) {
        UserVO vo = requireUser(userId);
        clearSensitiveFields(vo);
        if (!isSelfOrMaster(userId, user)) {
            applyMasking(vo, user);
        }
        return vo;
    }

    /**
     * 본인 정보 조회 — 마스킹 없음, pswd_hash 만 제거.
     */
    public UserVO getMe(IcasUser user) {
        UserVO vo = requireUser(user.getUserId());
        clearSensitiveFields(vo);
        return vo;
    }

    /* ════════════════════════════════════════════
     *  등록 / 수정 / 삭제
     * ════════════════════════════════════════════ */

    /**
     * 사용자 등록 (MOLIT/KOTSA 전용).
     * - user_id 중복 체크
     * - 초기 비번 정책 검증 + 해시
     * - pswd_chg_dt = null (초기 비번 강제 변경 표시)
     */
    @Transactional
    public UserVO createUser(UserVO vo, String plainPassword, IcasUser operator) {
        if (vo.getUserId() == null || vo.getUserId().isBlank()) {
            throw BusinessException.badRequest("사용자 ID 는 필수입니다.");
        }
        if (vo.getUserNm() == null || vo.getUserNm().isBlank()) {
            throw BusinessException.badRequest("사용자명은 필수입니다.");
        }
        if (vo.getOgnzId() == null || vo.getOgnzId().isBlank()) {
            throw BusinessException.badRequest("소속 기관 ID 는 필수입니다.");
        }

        // ognz_id FK 사전 검증
        ognzService.requireValidOgnz(vo.getOgnzId());

        // ID 중복 체크 (유효구간 내)
        UserVO existing = userMapper.selectByUserId(vo.getUserId());
        if (existing != null) {
            throw BusinessException.conflict("이미 사용 중인 사용자 ID 입니다: " + vo.getUserId());
        }

        // 비밀번호 정책 검증 + 해시
        validatePasswordPolicy(plainPassword);
        vo.setPswdHash(Sha256.hex(plainPassword));

        // masterYn 기본값
        if (vo.getMasterYn() == null || vo.getMasterYn().isBlank()) {
            vo.setMasterYn("N");
        }

        vo.setFrstRegUserId(operator.getUserId());
        vo.setLastChgUserId(operator.getUserId());
        userMapper.insertUser(vo);

        UserVO created = requireUser(vo.getUserId());
        clearSensitiveFields(created);
        return created;
    }

    /**
     * 기본정보 수정 (비밀번호 제외).
     * MOLIT/KOTSA 또는 본인 가능.
     */
    @Transactional
    public UserVO updateUser(String userId, UserVO vo, IcasUser operator) {
        requireUser(userId);   // 존재 확인

        // ognz_id 변경 시 FK 사전 검증 (부분 수정 허용 — null/blank 이면 skip)
        if (vo.getOgnzId() != null && !vo.getOgnzId().isBlank()) {
            ognzService.requireValidOgnz(vo.getOgnzId());
        }

        vo.setUserId(userId);
        vo.setLastChgUserId(operator.getUserId());
        int affected = userMapper.updateUser(vo);
        if (affected == 0) throw BusinessException.notFound("사용자");

        UserVO updated = requireUser(userId);
        clearSensitiveFields(updated);
        return updated;
    }

    /**
     * 소프트 삭제 (MOLIT/KOTSA 전용).
     */
    @Transactional
    public void softDeleteUser(String userId, IcasUser operator) {
        int affected = userMapper.softDeleteUser(userId, operator.getUserId());
        if (affected == 0) throw BusinessException.notFound("사용자");
    }

    /* ════════════════════════════════════════════
     *  비밀번호 변경 / 계정 잠금 해제
     * ════════════════════════════════════════════ */

    /**
     * 비밀번호 변경.
     *
     * <ul>
     *   <li>본인 변경 시: oldPassword 검증 필수.
     *   <li>MOLIT/KOTSA 관리자 강제 변경 시: oldPassword 없이 허용 (관리자 초기화).
     * </ul>
     * 변경 후 pswd_chg_dt = NOW(), pswd_fail_cnt = 0, acnt_lock_yn = 'N' 갱신.
     */
    @Transactional
    public void changePassword(String userId, String oldPlain, String newPlain, IcasUser operator) {
        boolean isSelf = userId.equals(operator.getUserId());
        boolean isAdmin = operator.isMolitOrKotsa();

        if (!isSelf && !isAdmin) {
            throw BusinessException.forbidden("본인 또는 MOLIT/KOTSA 관리자만 비밀번호를 변경할 수 있습니다.");
        }

        UserVO vo = requireUser(userId);

        // 본인 변경 시 기존 비밀번호 검증
        if (isSelf) {
            if (oldPlain == null || oldPlain.isBlank()) {
                throw BusinessException.badRequest("현재 비밀번호를 입력해야 합니다.");
            }
            if (!Sha256.matches(oldPlain, vo.getPswdHash())) {
                throw BusinessException.badRequest("현재 비밀번호가 일치하지 않습니다.");
            }
        }

        // 신 비밀번호 정책 검증
        validatePasswordPolicy(newPlain);

        String newHash = Sha256.hex(newPlain);
        int affected = userMapper.changePassword(userId, newHash, operator.getUserId());
        if (affected == 0) throw BusinessException.notFound("사용자");
    }

    /**
     * 계정 잠금 해제 (MOLIT/KOTSA 전용).
     * acnt_lock_yn = 'N', pswd_fail_cnt = 0 초기화.
     */
    @Transactional
    public void unlockAccount(String userId, IcasUser operator) {
        requireUser(userId);   // 존재 확인
        int affected = userMapper.unlockAccount(userId, operator.getUserId());
        if (affected == 0) throw BusinessException.notFound("사용자");
    }

    /* ════════════════════════════════════════════
     *  내부 유틸
     * ════════════════════════════════════════════ */

    /** 사용자 조회 후 없으면 404 */
    private UserVO requireUser(String userId) {
        UserVO vo = userMapper.selectByUserId(userId);
        if (vo == null) throw BusinessException.notFound("사용자");
        return vo;
    }

    /** 본인이거나 master 권한 확인 */
    private boolean isSelfOrMaster(String userId, IcasUser user) {
        return userId.equals(user.getUserId()) || user.isMaster();
    }

    /**
     * 응답에서 pswd_hash 제거.
     * 모든 응답에서 반드시 호출.
     */
    private void clearSensitiveFields(UserVO vo) {
        vo.setPswdHash(null);
    }

    /**
     * 개인정보 마스킹 적용.
     * 본인 또는 master 이면 마스킹 없음 (호출 전 판단).
     *
     * <ul>
     *   <li>userNm: 끝 한 글자 마스킹 ("홍길동" → "홍길*")
     *   <li>emlAddr: @ 앞 첫 글자만 남기고 마스킹 ("hong@ex.com" → "h***@ex.com")
     *   <li>mblphnNo: 뒷 4자리만 노출 ("01012345678" → "*******5678")
     * </ul>
     */
    private void applyMasking(UserVO vo, IcasUser viewer) {
        if (isSelfOrMaster(vo.getUserId(), viewer)) return;

        vo.setUserNm(maskUserNm(vo.getUserNm()));
        vo.setEmlAddr(maskEmlAddr(vo.getEmlAddr()));
        vo.setMblphnNo(maskMblphnNo(vo.getMblphnNo()));
    }

    /** "홍길동" → "홍길*" */
    private static String maskUserNm(String userNm) {
        if (userNm == null || userNm.length() <= 1) return userNm;
        return userNm.substring(0, userNm.length() - 1) + "*";
    }

    /** "hong@example.com" → "h***@example.com" */
    private static String maskEmlAddr(String emlAddr) {
        if (emlAddr == null) return null;
        int at = emlAddr.indexOf('@');
        if (at <= 0) return emlAddr;
        String local = emlAddr.substring(0, at);
        String domain = emlAddr.substring(at);          // "@example.com"
        String maskedLocal = local.charAt(0) + "*".repeat(Math.max(local.length() - 1, 3));
        return maskedLocal + domain;
    }

    /** "01012345678" → "*******5678" (뒷 4자리 노출) */
    private static String maskMblphnNo(String mblphnNo) {
        if (mblphnNo == null || mblphnNo.length() <= 4) return mblphnNo;
        int showLen = 4;
        int maskLen = mblphnNo.length() - showLen;
        return "*".repeat(maskLen) + mblphnNo.substring(maskLen);
    }

    /**
     * 비밀번호 정책 검증 (SER-009).
     * 9자 이상, 영문 + 숫자 + 특수문자 각 1자 이상 포함.
     */
    private static void validatePasswordPolicy(String plain) {
        if (plain == null || plain.isBlank()) {
            throw BusinessException.badRequest("비밀번호를 입력해야 합니다.");
        }
        if (!PASSWORD_POLICY.matcher(plain).matches()) {
            throw BusinessException.badRequest(
                "비밀번호는 9자 이상이며 영문, 숫자, 특수문자를 각각 1자 이상 포함해야 합니다.");
        }
    }
}
