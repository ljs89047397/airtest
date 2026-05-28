package kr.go.molit.icas.saf.batch.feed;

import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.saf.batch.SafBatchService;
import kr.go.molit.icas.saf.batch.feed.domain.SafFeedVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SafFeedService {

    private final SafFeedMapper   feedMapper;
    private final SafBatchService batchService;

    public SafFeedVO get(String batchId, IcasUser user) {
        batchService.get(batchId, user);
        SafFeedVO f = feedMapper.selectByBatchId(batchId);
        if (f == null) throw BusinessException.notFound("SAF 원료·제품 정보");
        return f;
    }

    @Transactional
    public SafFeedVO saveOrUpdate(String batchId, SafFeedVO vo, IcasUser user) {
        batchService.assertBatchOwnedByAirline(batchId, user);
        vo.setBatchId(batchId);
        vo.setFrstRegUserId(user.getUserId());
        vo.setLastChgUserId(user.getUserId());

        SafFeedVO existing = feedMapper.selectByBatchId(batchId);
        if (existing == null) feedMapper.insertFeed(vo);
        else                  feedMapper.updateFeed(vo);
        return feedMapper.selectByBatchId(batchId);
    }
}
