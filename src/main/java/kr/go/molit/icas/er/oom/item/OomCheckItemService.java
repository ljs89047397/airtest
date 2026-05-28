package kr.go.molit.icas.er.oom.item;

import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.er.oom.OomCheckService;
import kr.go.molit.icas.er.oom.item.domain.OomCheckItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * OoM 점검 항목 서비스 (er.tn_oom_check_item, SFR-034).
 *
 * <h2>항목 구간</h2>
 * <ul>
 *   <li>1~18 — 자동 검증 결과 ({@link kr.go.molit.icas.er.oom.validate.CorsiaQuantValidator} 가 기록·갱신)</li>
 *   <li>100+ — 사용자 추가 항목 (KOTSA 수동 입력)</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OomCheckItemService {

    private static final int    USER_ITEM_START = 100;
    private static final Set<String> VALID_JUDG = Set.of("PASS", "WARN", "FAIL");

    private final OomCheckItemMapper oomCheckItemMapper;
    private final OomCheckService    oomCheckService;

    public List<OomCheckItemVO> list(String oomId, IcasUser user) {
        oomCheckService.loadForRead(oomId, user);
        return oomCheckItemMapper.selectByOomId(oomId);
    }

    public OomCheckItemVO getOne(String oomId, int itemNo, IcasUser user) {
        oomCheckService.loadForRead(oomId, user);
        OomCheckItemVO vo = oomCheckItemMapper.selectOne(oomId, itemNo);
        if (vo == null) throw BusinessException.notFound("OoM 점검 항목");
        return vo;
    }

    /** 사용자 추가 항목 등록 — item_no = 100+ max+1. */
    @Transactional
    public OomCheckItemVO addUserItem(String oomId, OomCheckItemVO vo, IcasUser user) {
        oomCheckService.assertOomInprgForChildEdit(oomId, user);
        validate(vo);

        int nextSn = oomCheckItemMapper.selectNextSn(oomId, USER_ITEM_START);

        vo.setOomId(oomId);
        vo.setItemNo(nextSn);
        vo.setFrstRegUserId(user.getUserId());
        vo.setLastChgUserId(user.getUserId());

        oomCheckItemMapper.insertItem(vo);
        return oomCheckItemMapper.selectOne(oomId, nextSn);
    }

    /**
     * 항목 수정 — 자동(1~18) / 추가(100+) 모두 허용.
     * 자동 항목 수동 보정 (judg_cd 변경) 시나리오 지원.
     */
    @Transactional
    public void update(String oomId, int itemNo, OomCheckItemVO vo, IcasUser user) {
        oomCheckService.assertOomInprgForChildEdit(oomId, user);
        if (oomCheckItemMapper.selectOne(oomId, itemNo) == null) {
            throw BusinessException.notFound("OoM 점검 항목");
        }
        validate(vo);

        vo.setOomId(oomId);
        vo.setItemNo(itemNo);
        vo.setLastChgUserId(user.getUserId());

        int affected = oomCheckItemMapper.updateItem(vo);
        if (affected == 0) throw BusinessException.conflict("수정 대상 항목이 만료되었거나 존재하지 않습니다.");
    }

    @Transactional
    public void softDelete(String oomId, int itemNo, IcasUser user) {
        oomCheckService.assertOomInprgForChildEdit(oomId, user);
        if (itemNo >= 1 && itemNo <= 18) {
            throw BusinessException.badRequest(
                    "자동 검증 항목(1~18)은 개별 삭제 불가. 검증 재실행으로 갱신하세요.");
        }
        int affected = oomCheckItemMapper.softDeleteOne(oomId, itemNo, user.getUserId());
        if (affected == 0) throw BusinessException.notFound("OoM 점검 항목");
    }

    private void validate(OomCheckItemVO vo) {
        if (vo.getItemNm() == null || vo.getItemNm().isBlank()) {
            throw BusinessException.badRequest("항목명(itemNm)은 필수입니다.");
        }
        if (vo.getJudgCd() != null && !VALID_JUDG.contains(vo.getJudgCd())) {
            throw BusinessException.badRequest(
                    "판정 코드(judgCd) 허용값: PASS, WARN, FAIL. 입력값: " + vo.getJudgCd());
        }
    }
}
