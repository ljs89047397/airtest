package kr.go.molit.icas.saf.batch.blndr;

import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.saf.batch.SafBatchService;
import kr.go.molit.icas.saf.batch.blndr.domain.SafBlndrVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SafBlndrService {

    private final SafBlndrMapper  blndrMapper;
    private final SafBatchService batchService;

    public SafBlndrVO get(String batchId, IcasUser user) {
        batchService.get(batchId, user);
        SafBlndrVO b = blndrMapper.selectByBatchId(batchId);
        if (b == null) throw BusinessException.notFound("SAF 혼합사 정보");
        return b;
    }

    @Transactional
    public SafBlndrVO saveOrUpdate(String batchId, SafBlndrVO vo, IcasUser user) {
        batchService.assertBatchOwnedByAirline(batchId, user);
        vo.setBatchId(batchId);
        vo.setFrstRegUserId(user.getUserId());
        vo.setLastChgUserId(user.getUserId());

        SafBlndrVO existing = blndrMapper.selectByBatchId(batchId);
        if (existing == null) blndrMapper.insertBlndr(vo);
        else                  blndrMapper.updateBlndr(vo);
        return blndrMapper.selectByBatchId(batchId);
    }
}
