package com.sanele.portfolio.auth.security;

import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class PortfolioUserDetails implements UserDetails {
    private final Long id;
    private final String username;
    private final String email;
    private final String displayName;
    private final String password;
    private final boolean enabled;
    private final List<GrantedAuthority> authorities;

    public PortfolioUserDetails(Long id, String username, String email, String displayName, String password, boolean enabled,
            Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.displayName = displayName;
        this.password = password;
        this.enabled = enabled;
        this.authorities = List.copyOf(authorities);
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getDisplayName() { return displayName; }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override public String getPassword() { return password; }
    @Override public String getUsername() { return username; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return enabled; }
}