package kr.go.molit.icas.common.security;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 로그인 세션에 적재되는 사용자 정보.
 * 모든 Controller / Service 에서 {@code @AuthenticationPrincipal IcasUser user} 로 받음.
 */
@Getter
@Builder
@ToString(exclude = "pswdHash")
public class IcasUser implements UserDetails {

    private final String      userId;
    private final String      userNm;
    private final String      pswdHash;
    private final String      ognzId;
    private final String      ognzSeCd;   // MOLIT / KOTSA / AIRLINE / VERIFIER
    private final String      oprtrId;    // AIRLINE 인 경우만
    private final String      vrfcnInstId;// VERIFIER 인 경우만
    private final boolean     master;
    private final List<String> roleIds;
    private final Set<String> prgrmPathsInq;
    private final Set<String> prgrmPathsInpt;

    public boolean isMaster()      { return master; }
    public boolean isAirline()     { return "AIRLINE".equals(ognzSeCd); }
    public boolean isVerifier()    { return "VERIFIER".equals(ognzSeCd); }
    public boolean isMolitOrKotsa(){ return "MOLIT".equals(ognzSeCd) || "KOTSA".equals(ognzSeCd); }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roleIds == null ? List.of()
                : roleIds.stream().map(r -> new SimpleGrantedAuthority("ROLE_" + r)).collect(Collectors.toList());
    }

    @Override public String getPassword()              { return pswdHash; }
    @Override public String getUsername()              { return userId; }
    @Override public boolean isAccountNonExpired()     { return true; }
    @Override public boolean isAccountNonLocked()      { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled()               { return true; }
}
