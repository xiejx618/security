1.配置认证提供者
```java
@Override
protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    UserDetailsService userDetailsService=new CustomUserDetailsService(userService);
    AuthenticationProvider authenticationProvider=new CustomAuthenticationProvider(userDetailsService,userService);
    auth.authenticationProvider(authenticationProvider);
}
```
2.继承org.springframework.security.authentication.dao.DaoAuthenticationProvider自定义CustomAuthenticationProvider
```java
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
```
实现UserDetailsService来将spring security与系统用户连接起来.
```java
package org.exam.security;

import org.exam.domain.Authority;
import org.exam.domain.User;
import org.exam.service.UserService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.HashSet;
import java.util.Set;
public class CustomUserDetailsService implements UserDetailsService {
    private final UserService userService;
    public CustomUserDetailsService(UserService userService) {
        this.userService = userService;
    }
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userService.loadUserByUsername(username);
        if (user==null){
            throw new UsernameNotFoundException("找不到用户");
        }
        Set<Authority> authorities = user.getAuthorities();
        Set<GrantedAuthority> grantedAuthorities = new HashSet<GrantedAuthority>(authorities.size());
        for (Authority authority : authorities) {
            grantedAuthorities.add(new SimpleGrantedAuthority(authority.getAuthority()));
        }
        //转为spring security用户和权限,多余信息移除,如果使用session保存认证用户,就可以减小内存占用.
        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), user.isEnabled(),
                true,true,true, grantedAuthorities);
    }
}
```
3.其它细节不再赘述
