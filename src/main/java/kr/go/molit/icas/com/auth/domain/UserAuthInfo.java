package kr.go.molit.icas.com.auth.domain;

import lombok.Data;

@Data
public class UserAuthInfo {
    private String userId;
    private String userNm;
    private String pswdHash;
    private String ognzId;
    private String ognzSeCd;
    private String oprtrId;
    private String vrfcnInstId;
    private String acntLockYn;
    private String masterYn;
}
