package org.exam.security;

import org.exam.domain.entity.User;
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
 * 扩展提供验证码和记录登录时间等功能
 * Created on 16/2/28.
 */
public class DaoAuthenticationProviderExt extends DaoAuthenticationProvider {
    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCK_TIME = 3600000 * 2;
    private final UserService userService;

    public DaoAuthenticationProviderExt(UserDetailsService userDetailsService, UserService userService) {
        setUserDetailsService(userDetailsService);
        this.userService = userService;
    }

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        User user = userService.findByUsername(userDetails.getUsername());
        Date now = new Date();
        if (user.getAttempts() >= MAX_ATTEMPTS && (now.getTime() - user.getLockTime() < LOCK_TIME)) {//检查密码尝试次数
            throw new LockedException("已超过最大尝试密码次数");
        }
        try {
            super.additionalAuthenticationChecks(userDetails, authentication);
            //密码正确时,尝试次数，锁定时间设0
            user.setAttempts(0);
            user.setLockTime(0);
            user.setLoginTime(now);
            userService.save(user);
        } catch (AuthenticationException e) {//密码不正确时,更新密码尝试次数
            int attempts = user.getAttempts() + 1;
            String msg;
            if (attempts == MAX_ATTEMPTS) {//记录最后一次失败登录时间作为锁定时间
                user.setLockTime(now.getTime());
                msg = "账户被锁定," + LOCK_TIME / 3600000 + "小时后再尝试";
            } else {
                msg = "密码不正确,还可以尝试" + (MAX_ATTEMPTS - user.getAttempts()) + "次";
            }
            user.setAttempts(attempts);
            userService.save(user);
            throw new BadCredentialsException(msg);
        }
    }
}
