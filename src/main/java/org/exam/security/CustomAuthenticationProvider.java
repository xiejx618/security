package org.exam.security;

import org.exam.domain.User;
import org.exam.service.UserService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Date;

/**
 * Created by xin on 16/2/28.
 */
public class CustomAuthenticationProvider extends DaoAuthenticationProvider {
    private final int MAX_ATTEMPTS=5;
    private final UserService userService;
    public CustomAuthenticationProvider(UserDetailsService userDetailsService, UserService userService) {
        setUserDetailsService(userDetailsService);
        this.userService=userService;
    }
    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        User user = userService.findByUsername(userDetails.getUsername());
        if (user.getAttempts()>MAX_ATTEMPTS){//检查密码尝试次数
            throw new LockedException("已超过最大尝试密码次数");
        }
        try {
            super.additionalAuthenticationChecks(userDetails, authentication);
            //密码正确时,尝试次数清零
            user.setAttempts(0);
            user.setLastTime(new Date());
            userService.save(user);
        } catch (AuthenticationException e) {//密码不正确时,更新密码尝试次数
            user.setAttempts(user.getAttempts()+1);
            userService.save(user);
            throw new BadCredentialsException("密码不正确,还可以尝试"+(MAX_ATTEMPTS-user.getAttempts())+"次");
        }
    }
}
