package kr.go.molit.icas.com.rglt;

import kr.go.molit.icas.com.rglt.domain.RgltSearch;
import kr.go.molit.icas.com.rglt.domain.RgltVO;
import kr.go.molit.icas.common.dto.PageResponse;
import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.common.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 규정 게시판 서비스 (SFR-057).
 *
 * <p>권한:
 * - 목록/상세 조회: 전 사용자
 * - 등록/수정: MOLIT (isMolitOrKotsa && ognzSeCd==MOLIT)
 * - 비공개(soft delete): MOLIT
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RgltService {

    private static final String RGLT_PREFIX = "RG";

    private final RgltMapper  rgltMapper;
    private final IdGenerator idGenerator;

    // ── 조회 ──────────────────────────────────────────────────────────────────

    /**
     * 규정 목록 조회 (페이징). 전 사용자 가능.
     */
    public PageResponse<RgltVO> listRglts(RgltSearch search, IcasUser user) {
        List<RgltVO> rows = rgltMapper.selectRglts(search);
        int total = rgltMapper.countRglts(search);
        return new PageResponse<>(rows, search.getPage(), search.getPageSize(), total);
    }

    /**
     * 규정 단건 조회. 전 사용자 가능.
     */
    public RgltVO getRglt(String rgltId, IcasUser user) {
        RgltVO vo = rgltMapper.selectByRgltId(rgltId);
        if (vo == null) throw BusinessException.notFound("규정 (" + rgltId + ")");
        return vo;
    }

    // ── 등록 / 수정 / 삭제 ───────────────────────────────────────────────────

    /**
     * 규정 등록. MOLIT 전용. RG 채번.
     */
    @Transactional
    public RgltVO createRglt(RgltVO vo, IcasUser user) {
        assertMolit(user);

        int seq = rgltMapper.countByPrefix() + 1;
        String rgltId = idGenerator.managementPk(RGLT_PREFIX, seq);
        vo.setRgltId(rgltId);
        vo.setFrstRegUserId(user.getUserId());
        vo.setLastChgUserId(user.getUserId());

        rgltMapper.insertRglt(vo);
        return rgltMapper.selectByRgltId(rgltId);
    }

    /**
     * 규정 수정. MOLIT 전용.
     */
    @Transactional
    public RgltVO updateRglt(RgltVO vo, IcasUser user) {
        assertMolit(user);
        RgltVO existing = rgltMapper.selectByRgltId(vo.getRgltId());
        if (existing == null) throw BusinessException.notFound("규정 (" + vo.getRgltId() + ")");
        vo.setLastChgUserId(user.getUserId());
        rgltMapper.updateRglt(vo);
        return rgltMapper.selectByRgltId(vo.getRgltId());
    }

    /**
     * 규정 비공개 처리 (soft delete). MOLIT 전용.
     */
    @Transactional
    public void archiveRglt(String rgltId, IcasUser user) {
        assertMolit(user);
        RgltVO existing = rgltMapper.selectByRgltId(rgltId);
        if (existing == null) throw BusinessException.notFound("규정 (" + rgltId + ")");
        rgltMapper.softDeleteRglt(rgltId, user.getUserId());
    }

    // ── private ───────────────────────────────────────────────────────────────

    /**
     * MOLIT 사용자 여부 검증.
     * master 는 우회 허용.
     */
    private void assertMolit(IcasUser user) {
        if (user.isMaster()) return;
        if (!"MOLIT".equals(user.getOgnzSeCd())) {
            throw BusinessException.forbidden("국토교통부(MOLIT) 사용자만 규정을 등록·수정할 수 있습니다.");
        }
    }
}
