package kr.go.molit.icas.com.prgrm;

import kr.go.molit.icas.com.prgrm.domain.PrgrmVO;
import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.IcasUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * 프로그램(화면·API 단위) 관리 서비스.
 * 변경 작업(등록/수정/삭제)은 MOLIT/KOTSA 전용.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PrgrmService {

    private static final Set<String> VALID_SYS_SE_CD =
            Set.of("COM", "EMP", "ER", "VR", "SAF", "PTL");

    private final PrgrmMapper prgrmMapper;

    // ------------------------------------------------------------------ //
    //  조회
    // ------------------------------------------------------------------ //

    /**
     * 전체 목록 조회. sysSeCd 가 있으면 해당 시스템만 필터.
     */
    public List<PrgrmVO> selectPrgrms(String sysSeCd) {
        return prgrmMapper.selectPrgrms(sysSeCd);
    }

    /**
     * 단건 조회. 존재하지 않으면 404.
     */
    public PrgrmVO selectPrgrm(String prgrmId) {
        PrgrmVO vo = prgrmMapper.selectPrgrm(prgrmId);
        if (vo == null) throw BusinessException.notFound("프로그램");
        return vo;
    }

    // ------------------------------------------------------------------ //
    //  변경 (MOLIT/KOTSA 전용)
    // ------------------------------------------------------------------ //

    /**
     * 프로그램 등록.
     */
    @Transactional
    public PrgrmVO insertPrgrm(PrgrmVO vo, IcasUser user) {
        assertMolitOrKotsa(user);
        validateSysSeCd(vo.getSysSeCd());

        if (vo.getPrgrmId() == null || vo.getPrgrmId().isBlank())
            throw BusinessException.badRequest("프로그램 ID 는 필수입니다.");
        if (vo.getPrgrmNm() == null || vo.getPrgrmNm().isBlank())
            throw BusinessException.badRequest("프로그램 명칭은 필수입니다.");

        if (prgrmMapper.existsPrgrm(vo.getPrgrmId()))
            throw BusinessException.conflict("이미 존재하는 프로그램 ID 입니다: " + vo.getPrgrmId());

        vo.setFrstRegUserId(user.getUserId());
        vo.setLastChgUserId(user.getUserId());
        prgrmMapper.insertPrgrm(vo);
        return prgrmMapper.selectPrgrm(vo.getPrgrmId());
    }

    /**
     * 프로그램 수정.
     */
    @Transactional
    public void updatePrgrm(String prgrmId, PrgrmVO vo, IcasUser user) {
        assertMolitOrKotsa(user);
        validateSysSeCd(vo.getSysSeCd());

        vo.setPrgrmId(prgrmId);
        vo.setLastChgUserId(user.getUserId());
        int affected = prgrmMapper.updatePrgrm(vo);
        if (affected == 0) throw BusinessException.notFound("프로그램");
    }

    /**
     * 프로그램 소프트 삭제.
     */
    @Transactional
    public void softDeletePrgrm(String prgrmId, IcasUser user) {
        assertMolitOrKotsa(user);
        int affected = prgrmMapper.softDeletePrgrm(prgrmId, user.getUserId());
        if (affected == 0) throw BusinessException.notFound("프로그램");
    }

    // ------------------------------------------------------------------ //
    //  내부 검증
    // ------------------------------------------------------------------ //

    private void assertMolitOrKotsa(IcasUser user) {
        if (!user.isMolitOrKotsa()) {
            throw BusinessException.forbidden("MOLIT/KOTSA 사용자만 프로그램을 변경할 수 있습니다.");
        }
    }

    private void validateSysSeCd(String sysSeCd) {
        if (sysSeCd == null || !VALID_SYS_SE_CD.contains(sysSeCd)) {
            throw BusinessException.badRequest(
                    "유효하지 않은 시스템 구분 코드입니다. 허용값: " + VALID_SYS_SE_CD);
        }
    }
}
