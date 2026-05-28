package kr.go.molit.icas.vr.time;

import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.vr.VrService;
import kr.go.molit.icas.vr.time.domain.VrTimeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 검증 시간 서비스 — tn_vr_time (1:1).
 * total_hrs = onsite_hrs + offsite_hrs 자동 계산.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VrTimeService {

    private final VrTimeMapper vrTimeMapper;
    private final VrService    vrService;

    public VrTimeVO get(String vrId, IcasUser user) {
        vrService.loadForRead(vrId, user);
        VrTimeVO t = vrTimeMapper.selectByVrId(vrId);
        if (t == null) throw BusinessException.notFound("VR 검증시간 정보");
        return t;
    }

    @Transactional
    public VrTimeVO saveOrUpdate(String vrId, VrTimeVO vo, IcasUser user) {
        vrService.assertVrDraftForChildEdit(vrId, user);
        validateTime(vo);

        vo.setVrId(vrId);
        // total_hrs 자동 계산
        BigDecimal onsite  = vo.getOnsiteHrs()  != null ? vo.getOnsiteHrs()  : BigDecimal.ZERO;
        BigDecimal offsite = vo.getOffsiteHrs() != null ? vo.getOffsiteHrs() : BigDecimal.ZERO;
        vo.setTotalHrs(onsite.add(offsite));
        vo.setFrstRegUserId(user.getUserId());
        vo.setLastChgUserId(user.getUserId());

        VrTimeVO existing = vrTimeMapper.selectByVrId(vrId);
        if (existing == null) {
            vrTimeMapper.insertTime(vo);
        } else {
            vrTimeMapper.updateTime(vo);
        }
        return vrTimeMapper.selectByVrId(vrId);
    }

    private void validateTime(VrTimeVO vo) {
        if (vo.getOnsiteHrs() == null && vo.getOffsiteHrs() == null)
            throw BusinessException.badRequest("현장/비현장 시간 중 하나 이상 입력해야 합니다.");
        if (vo.getOnsiteHrs() != null && vo.getOnsiteHrs().compareTo(BigDecimal.ZERO) < 0)
            throw BusinessException.badRequest("현장 검증 시간은 0 이상이어야 합니다.");
        if (vo.getOffsiteHrs() != null && vo.getOffsiteHrs().compareTo(BigDecimal.ZERO) < 0)
            throw BusinessException.badRequest("비현장 검증 시간은 0 이상이어야 합니다.");
    }
}
