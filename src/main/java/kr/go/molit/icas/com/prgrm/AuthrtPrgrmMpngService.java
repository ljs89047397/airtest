package kr.go.molit.icas.com.prgrm;

import kr.go.molit.icas.com.prgrm.domain.AuthrtPrgrmMpngVO;
import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.IcasUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 권한-프로그램 매핑 관리 서비스.
 * 변경 작업(등록/수정/삭제)은 MOLIT/KOTSA 전용.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthrtPrgrmMpngService {

    private final AuthrtPrgrmMpngMapper authrtPrgrmMpngMapper;

    // ------------------------------------------------------------------ //
    //  조회
    // ------------------------------------------------------------------ //

    /**
     * 해당 권한이 가진 모든 프로그램 + 조회/입력 비트 조회.
     */
    public List<AuthrtPrgrmMpngVO> selectByAuthrt(String authrtId) {
        if (authrtId == null || authrtId.isBlank())
            throw BusinessException.badRequest("권한 ID 가 필요합니다.");
        return authrtPrgrmMpngMapper.selectByAuthrt(authrtId);
    }

    /**
     * 해당 프로그램을 가진 권한들 조회.
     */
    public List<AuthrtPrgrmMpngVO> selectByPrgrm(String prgrmId) {
        if (prgrmId == null || prgrmId.isBlank())
            throw BusinessException.badRequest("프로그램 ID 가 필요합니다.");
        return authrtPrgrmMpngMapper.selectByPrgrm(prgrmId);
    }

    // ------------------------------------------------------------------ //
    //  변경 (MOLIT/KOTSA 전용)
    // ------------------------------------------------------------------ //

    /**
     * 권한-프로그램 upsert.
     * 기존 유효 매핑이 있으면 inq/inpt 비트만 갱신, 없으면 신규 등록.
     *
     * @param authrtId    권한 ID
     * @param prgrmId     프로그램 ID
     * @param inqAuthrtYn 조회 권한 여부 (Y/N)
     * @param inptAuthrtYn 입력 권한 여부 (Y/N)
     * @param user        처리 사용자
     */
    @Transactional
    public void setAuthority(String authrtId, String prgrmId,
                             String inqAuthrtYn, String inptAuthrtYn,
                             IcasUser user) {
        assertMolitOrKotsa(user);
        validateParams(authrtId, prgrmId, inqAuthrtYn, inptAuthrtYn);

        AuthrtPrgrmMpngVO existing = authrtPrgrmMpngMapper.selectOne(authrtId, prgrmId);

        AuthrtPrgrmMpngVO vo = new AuthrtPrgrmMpngVO();
        vo.setAuthrtId(authrtId);
        vo.setPrgrmId(prgrmId);
        vo.setInqAuthrtYn(inqAuthrtYn);
        vo.setInptAuthrtYn(inptAuthrtYn);
        vo.setLastChgUserId(user.getUserId());

        if (existing != null) {
            authrtPrgrmMpngMapper.updateMapping(vo);
        } else {
            vo.setFrstRegUserId(user.getUserId());
            authrtPrgrmMpngMapper.insertMapping(vo);
        }
    }

    /**
     * 권한-프로그램 매핑 소프트 삭제.
     */
    @Transactional
    public void removeMapping(String authrtId, String prgrmId, IcasUser user) {
        assertMolitOrKotsa(user);
        if (authrtId == null || authrtId.isBlank() || prgrmId == null || prgrmId.isBlank())
            throw BusinessException.badRequest("권한 ID 와 프로그램 ID 는 필수입니다.");

        int affected = authrtPrgrmMpngMapper.softDeleteMapping(authrtId, prgrmId, user.getUserId());
        if (affected == 0) throw BusinessException.notFound("권한-프로그램 매핑");
    }

    // ------------------------------------------------------------------ //
    //  내부 검증
    // ------------------------------------------------------------------ //

    private void assertMolitOrKotsa(IcasUser user) {
        if (!user.isMolitOrKotsa()) {
            throw BusinessException.forbidden("MOLIT/KOTSA 사용자만 권한 매핑을 변경할 수 있습니다.");
        }
    }

    private void validateParams(String authrtId, String prgrmId,
                                String inqAuthrtYn, String inptAuthrtYn) {
        if (authrtId == null || authrtId.isBlank())
            throw BusinessException.badRequest("권한 ID 는 필수입니다.");
        if (prgrmId == null || prgrmId.isBlank())
            throw BusinessException.badRequest("프로그램 ID 는 필수입니다.");
        if (!"Y".equals(inqAuthrtYn) && !"N".equals(inqAuthrtYn))
            throw BusinessException.badRequest("조회 권한 여부는 Y 또는 N 이어야 합니다.");
        if (!"Y".equals(inptAuthrtYn) && !"N".equals(inptAuthrtYn))
            throw BusinessException.badRequest("입력 권한 여부는 Y 또는 N 이어야 합니다.");
    }
}
