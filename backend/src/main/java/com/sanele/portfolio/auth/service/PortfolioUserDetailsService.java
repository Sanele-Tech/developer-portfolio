package com.sanele.portfolio.auth.service;

import com.sanele.portfolio.auth.security.PortfolioUserDetails;
import com.sanele.portfolio.users.entity.User;
import com.sanele.portfolio.users.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class PortfolioUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    public PortfolioUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findWithRolesByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        List<GrantedAuthority> authorities = new ArrayList<>();
        user.getRoles().forEach(role -> {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
            role.getPermissions().forEach(permission ->
                    authorities.add(new SimpleGrantedAuthority(permission.getName())));
        });
        return new PortfolioUserDetails(user.getId(), user.getUsername(), user.getEmail(), user.getDisplayName(), user.getPasswordHash(),
                user.isEnabled(), authorities);
    }
}