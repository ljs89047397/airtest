package kr.go.molit.icas.saf.cert.domain;

import kr.go.molit.icas.common.dto.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SafCertSearch extends PageRequest {
    private String oprtrId;
    private String batchId;
    private String certTypeCd;
    private String certSchmCd;
    private String srndYn;
}
