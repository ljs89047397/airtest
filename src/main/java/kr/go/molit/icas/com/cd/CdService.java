package kr.go.molit.icas.com.cd;

import kr.go.molit.icas.com.cd.domain.CdDtlVO;
import kr.go.molit.icas.com.cd.domain.CdGroupVO;
import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.IcasUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CdService {

    private final CdMapper cdMapper;

    public List<CdGroupVO> listGroups() {
        return cdMapper.selectCdGroups();
    }

    public List<CdDtlVO> listDtlsByGroup(String grpId) {
        if (grpId == null || grpId.isBlank()) throw BusinessException.badRequest("그룹 ID 가 필요합니다.");
        return cdMapper.selectCdDtlsByGroup(grpId);
    }

    @Transactional
    public CdGroupVO createGroup(CdGroupVO vo, IcasUser user) {
        if (vo.getGrpId() == null || vo.getGrpNm() == null) {
            throw BusinessException.badRequest("그룹 ID 와 명칭은 필수입니다.");
        }
        vo.setFrstRegUserId(user.getUserId());
        vo.setLastChgUserId(user.getUserId());
        cdMapper.insertCdGroup(vo);
        return vo;
    }

    @Transactional
    public void updateGroup(CdGroupVO vo, IcasUser user) {
        vo.setLastChgUserId(user.getUserId());
        int affected = cdMapper.updateCdGroup(vo);
        if (affected == 0) throw BusinessException.notFound("공통코드 그룹");
    }

    @Transactional
    public void softDeleteGroup(String grpId, IcasUser user) {
        int affected = cdMapper.softDeleteCdGroup(grpId, user.getUserId());
        if (affected == 0) throw BusinessException.notFound("공통코드 그룹");
    }

    @Transactional
    public CdDtlVO createDtl(CdDtlVO vo, IcasUser user) {
        if (vo.getGrpId() == null || vo.getCd() == null) {
            throw BusinessException.badRequest("그룹 ID 와 코드는 필수입니다.");
        }
        vo.setFrstRegUserId(user.getUserId());
        vo.setLastChgUserId(user.getUserId());
        cdMapper.insertCdDtl(vo);
        return vo;
    }

    @Transactional
    public void updateDtl(CdDtlVO vo, IcasUser user) {
        vo.setLastChgUserId(user.getUserId());
        int affected = cdMapper.updateCdDtl(vo);
        if (affected == 0) throw BusinessException.notFound("공통코드 상세");
    }

    @Transactional
    public void softDeleteDtl(String grpId, String cd, IcasUser user) {
        int affected = cdMapper.softDeleteCdDtl(grpId, cd, user.getUserId());
        if (affected == 0) throw BusinessException.notFound("공통코드 상세");
    }
}
