package kr.go.molit.icas.er.eucr.validate.domain;

import lombok.Data;

/**
 * 일련번호 이중사용 교차 스캔 결과 한 행.
 *
 * <p>{@code crdt_no} 가 다른 EUCR (자기 EUCR 제외) 에 이미 존재할 때 1행 반환.
 */
@Data
public class CrdtConflictRow {

    /** 충돌 일련번호 */
    private String crdtNo;

    /** 점유 EUCR ID */
    private String eucrId;

    /** 배치 번호 */
    private String batchNo;

    /** 운영사 ID */
    private String oprtrId;

    /** 운영사명 (JOIN 조회) */
    private String oprtrNm;

    /** 보고연도 */
    private String rprtYr;
}
