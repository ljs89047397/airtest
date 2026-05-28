package kr.go.molit.icas.er.eucr.crdt;

import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.DataScopeValidator;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.er.eucr.EucrMapper;
import kr.go.molit.icas.er.eucr.EucrService;
import kr.go.molit.icas.er.eucr.batch.EucrBatchMapper;
import kr.go.molit.icas.er.eucr.batch.domain.EucrBatchVO;
import kr.go.molit.icas.er.eucr.crdt.domain.EucrCrdtDtlVO;
import kr.go.molit.icas.er.eucr.domain.EucrVO;
import kr.go.molit.icas.er.eucr.validate.EucrDoubleUsingValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * EUCR 일련번호 상세 서비스 (er.tn_eucr_crdt_dtl).
 *
 * <h2>핵심 규칙</h2>
 * <ul>
 *   <li>부모 EUCR DRAFT 한정</li>
 *   <li>batch 존재 검증 (eucr_id + batch_no)</li>
 *   <li>일괄 등록 — 사전 이중사용 검증 (BLOCKED 시 일괄 차단)</li>
 *   <li>DB UK(crdt_no) 가 최종 안전망</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EucrCrdtDtlService {

    private final EucrCrdtDtlMapper          eucrCrdtDtlMapper;
    private final EucrBatchMapper            eucrBatchMapper;
    private final EucrMapper                 eucrMapper;
    private final EucrService                eucrService;
    private final EucrDoubleUsingValidator   doubleUsingValidator;
    private final DataScopeValidator         dataScopeValidator;

    public List<EucrCrdtDtlVO> listByEucr(String eucrId, IcasUser user) {
        loadEucrForRead(eucrId, user);
        return eucrCrdtDtlMapper.selectByEucrId(eucrId);
    }

    public List<EucrCrdtDtlVO> listByBatch(String eucrId, String batchNo, IcasUser user) {
        loadEucrForRead(eucrId, user);
        return eucrCrdtDtlMapper.selectByBatch(eucrId, batchNo);
    }

    /**
     * 일련번호 단건 등록.
     */
    @Transactional
    public EucrCrdtDtlVO add(String eucrId, EucrCrdtDtlVO vo, IcasUser user) {
        return addBulk(eucrId, List.of(vo), user).get(0);
    }

    /**
     * 일련번호 일괄 등록 (CSV / 범위 expand 결과).
     *
     * <p>입력 N개 중 1개라도 이중사용이면 일괄 차단 (트랜잭션 롤백).
     */
    @Transactional
    public List<EucrCrdtDtlVO> addBulk(String eucrId, List<EucrCrdtDtlVO> rows, IcasUser user) {
        eucrService.assertEucrDraftForChildEdit(eucrId, user);
        if (rows == null || rows.isEmpty()) {
            throw BusinessException.badRequest("등록할 일련번호가 없습니다.");
        }

        // 1. 입력 검증 + batch 존재 검증
        for (EucrCrdtDtlVO vo : rows) {
            if (isBlank(vo.getCrdtNo())) {
                throw BusinessException.badRequest("일련번호(crdtNo)는 필수입니다.");
            }
            if (isBlank(vo.getBatchNo())) {
                throw BusinessException.badRequest("배치 번호(batchNo)는 필수입니다.");
            }
            EucrBatchVO batch = eucrBatchMapper.selectOne(eucrId, vo.getBatchNo());
            if (batch == null) {
                throw BusinessException.badRequest(
                        "지정한 batch_no 가 해당 EUCR 에 존재하지 않습니다: " + vo.getBatchNo());
            }
        }

        // 2. 입력 내 중복 검사 (같은 요청 묶음 내 crdt_no 중복)
        List<String> crdtNos = rows.stream().map(EucrCrdtDtlVO::getCrdtNo).toList();
        if (crdtNos.size() != crdtNos.stream().distinct().count()) {
            throw BusinessException.badRequest("요청 내 일련번호가 중복됩니다.");
        }

        // 3. 이중사용 교차 스캔 (다른 EUCR 점유 검사)
        doubleUsingValidator.assertNotBlocked(crdtNos, eucrId);

        // 4. INSERT
        List<EucrCrdtDtlVO> result = new ArrayList<>(rows.size());
        for (EucrCrdtDtlVO vo : rows) {
            vo.setEucrId(eucrId);
            vo.setFrstRegUserId(user.getUserId());
            vo.setLastChgUserId(user.getUserId());
            eucrCrdtDtlMapper.insertCrdt(vo);
            result.add(eucrCrdtDtlMapper.selectOne(eucrId, vo.getCrdtNo()));
        }
        return result;
    }

    @Transactional
    public void softDelete(String eucrId, String crdtNo, IcasUser user) {
        eucrService.assertEucrDraftForChildEdit(eucrId, user);
        int affected = eucrCrdtDtlMapper.softDeleteOne(eucrId, crdtNo, user.getUserId());
        if (affected == 0) throw BusinessException.notFound("EUCR 일련번호");
    }

    // ── Private ──

    private EucrVO loadEucrForRead(String eucrId, IcasUser user) {
        EucrVO e = eucrMapper.selectByEucrId(eucrId);
        if (e == null) throw BusinessException.notFound("EUCR");
        dataScopeValidator.assertOprtrAccessible(user, e.getOprtrId(), e.getRprtYr());
        return e;
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
