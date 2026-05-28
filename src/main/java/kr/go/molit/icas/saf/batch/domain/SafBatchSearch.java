package kr.go.molit.icas.saf.batch.domain;

import kr.go.molit.icas.common.dto.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SafBatchSearch extends PageRequest {
    private String oprtrId;
    private String batchId;
    private String pocIdNo;
    private String safRecvCntryCd;
    private String custChnModlCd;
}
