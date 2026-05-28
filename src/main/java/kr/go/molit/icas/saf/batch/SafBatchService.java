package kr.go.molit.icas.saf.batch;

import kr.go.molit.icas.common.dto.PageResponse;
import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.DataScopeValidator;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.saf.batch.blndr.SafBlndrMapper;
import kr.go.molit.icas.saf.batch.domain.SafBatchSearch;
import kr.go.molit.icas.saf.batch.domain.SafBatchVO;
import kr.go.molit.icas.saf.batch.feed.SafFeedMapper;
import kr.go.molit.icas.saf.batch.ghg.SafGhgMapper;
import kr.go.molit.icas.saf.batch.prdc.SafPrdcSplyMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * SAF 배치 마스터 서비스 (SFR-037/038).
 *
 * <h2>권한</h2>
 * <ul>
 *   <li>AIRLINE — 자사 배치 등록·수정·삭제</li>
 *   <li>KOTSA/MOLIT — 전사 조회</li>
 * </ul>
 *
 * <p>batch_id 는 자연키(생산자 PoS Batch ID). 채번 없음.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SafBatchService {

    private final SafBatchMapper     safBatchMapper;
    private final SafPrdcSplyMapper  prdcSplyMapper;
    private final SafBlndrMapper     blndrMapper;
    private final SafFeedMapper      feedMapper;
    private final SafGhgMapper       ghgMapper;
    private final DataScopeValidator dataScopeValidator;

    // ── 조회 ──

    public PageResponse<SafBatchVO> search(SafBatchSearch search, IcasUser user) {
        if (user.isMaster() || user.isMolitOrKotsa()) {
            // 전체
        } else if (user.isAirline()) {
            search.setOprtrId(user.getOprtrId());
        } else {
            throw BusinessException.forbidden("SAF 배치 조회 권한이 없습니다.");
        }
        long total = safBatchMapper.countBatches(search);
        List<SafBatchVO> rows = safBatchMapper.selectBatches(search);
        return new PageResponse<>(rows, search.getPage(), search.getPageSize(), total);
    }

    public SafBatchVO get(String batchId, IcasUser user) {
        SafBatchVO m = loadOrThrow(batchId);
        if (!user.isMaster() && !user.isMolitOrKotsa() && user.isAirline()) {
            dataScopeValidator.assertOwnAirline(user, m.getOprtrId());
        }
        return m;
    }

    // ── 신규 ──

    @Transactional
    public SafBatchVO create(SafBatchVO vo, IcasUser user) {
        assertAirline(user);
        dataScopeValidator.assertOwnAirline(user, vo.getOprtrId());
        validateBatch(vo);

        if (safBatchMapper.selectByBatchId(vo.getBatchId()) != null) {
            throw BusinessException.conflict("이미 등록된 배치 ID 입니다: " + vo.getBatchId());
        }

        vo.setFrstRegUserId(user.getUserId());
        vo.setLastChgUserId(user.getUserId());
        safBatchMapper.insertBatch(vo);
        return loadOrThrow(vo.getBatchId());
    }

    // ── 수정 ──

    @Transactional
    public SafBatchVO update(String batchId, SafBatchVO vo, IcasUser user) {
        assertAirline(user);
        SafBatchVO m = loadOrThrow(batchId);
        dataScopeValidator.assertOwnAirline(user, m.getOprtrId());
        validateBatch(vo);

        vo.setBatchId(batchId);
        vo.setLastChgUserId(user.getUserId());
        safBatchMapper.updateBatch(vo);
        return loadOrThrow(batchId);
    }

    // ── 삭제 ──

    @Transactional
    public void softDelete(String batchId, IcasUser user) {
        assertAirline(user);
        SafBatchVO m = loadOrThrow(batchId);
        dataScopeValidator.assertOwnAirline(user, m.getOprtrId());
        // 하위 1:1 데이터 물리 삭제
        prdcSplyMapper.deleteByBatchId(batchId);
        blndrMapper.deleteByBatchId(batchId);
        feedMapper.deleteByBatchId(batchId);
        ghgMapper.deleteByBatchId(batchId);
        safBatchMapper.softDeleteBatch(batchId, user.getUserId());
    }

    // ── 자식 Service 헬퍼 ──

    public SafBatchVO assertBatchOwnedByAirline(String batchId, IcasUser user) {
        SafBatchVO m = loadOrThrow(batchId);
        assertAirline(user);
        dataScopeValidator.assertOwnAirline(user, m.getOprtrId());
        return m;
    }

    // ── Private ──

    SafBatchVO loadOrThrow(String batchId) {
        SafBatchVO m = safBatchMapper.selectByBatchId(batchId);
        if (m == null) throw BusinessException.notFound("SAF 배치(batchId=" + batchId + ")");
        return m;
    }

    private void assertAirline(IcasUser user) {
        if (user.isMaster()) return;
        if (!user.isAirline()) throw BusinessException.forbidden("항공사 사용자만 SAF 배치를 등록·수정할 수 있습니다.");
    }

    private void validateBatch(SafBatchVO vo) {
        if (vo.getBatchId() == null || vo.getBatchId().isBlank())
            throw BusinessException.badRequest("배치 ID(batchId)는 필수입니다.");
        if (vo.getBatchQty() == null || vo.getBatchQty().signum() <= 0)
            throw BusinessException.badRequest("배치 물량(batchQty)은 0 초과여야 합니다.");
        if ("ACTUAL".equals(vo.getDnstySecd()) && vo.getNeatSafDnsty() == null)
            throw BusinessException.badRequest("밀도 구분 ACTUAL 선택 시 실제 밀도(neatSafDnsty)는 필수입니다.");
    }
}
