package org.exam.security;

import org.exam.domain.entity.Authority;
import org.exam.domain.entity.User;
import org.exam.service.UserService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.HashSet;
import java.util.Set;

/**
 * Created on 16/2/28.
 */
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserService userService;

    public UserDetailsServiceImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userService.loadUserByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("找不到用户");
        }
        Set<Authority> authorities = user.getAuthorities();
        Set<GrantedAuthority> grantedAuthorities = new HashSet<GrantedAuthority>(authorities.size());
        for (Authority authority : authorities) {
            grantedAuthorities.add(new SimpleGrantedAuthority(authority.getAuthority()));
        }
        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), user.isEnabled(),
                true, true, true, grantedAuthorities);
    }
}
