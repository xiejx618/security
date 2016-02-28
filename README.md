二.增加密码登录次数限制,记录上一次登录成功时间功能.
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
二.增加验证码功能.增加一个Filter来处理验证码校验.
```java
@Override
protected void configure(HttpSecurity http) throws Exception {
    http.addFilterBefore(new KaptchaAuthenticationFilter("/login", "/login?error"), UsernamePasswordAuthenticationFilter.class)
            .csrf().disable()
            .authorizeRequests().anyRequest().authenticated()
            .and().formLogin().loginPage("/login").failureUrl("/login?error").usernameParameter("username").passwordParameter("password").permitAll()
            .and().logout().logoutUrl("/logout").permitAll();
}
```
2.HttpSecurity有addFilterBefore,addFilterAfter,就没有replaceFilter(基于xml方式有<custom-filter position="FORM_LOGIN_FILTER" ref="multipleInputAuthenticationFilter" />),所以思路只能这么来.先看看KaptchaAuthenticationFilter
```java
package org.exam.security;
import com.google.code.kaptcha.Constants;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
public class KaptchaAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
    private String servletPath;
    public KaptchaAuthenticationFilter(String servletPath,String failureUrl) {
        super(servletPath);
        this.servletPath=servletPath;
        setAuthenticationFailureHandler(new SimpleUrlAuthenticationFailureHandler(failureUrl));
    }
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res=(HttpServletResponse)response;
        if ("POST".equalsIgnoreCase(req.getMethod())&&servletPath.equals(req.getServletPath())){
            String expect = (String) req.getSession().getAttribute(Constants.KAPTCHA_SESSION_KEY);
            if(expect!=null&&!expect.equalsIgnoreCase(req.getParameter("kaptcha"))){
                unsuccessfulAuthentication(req, res, new InsufficientAuthenticationException("输入的验证码不正确"));
                return;
            }
        }
        chain.doFilter(request,response);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
        return null;
    }
}
```
这里使用kaptcha生成验证码,加入kaptcha依赖包
```xml
<dependency>
    <groupId>com.github.penggle</groupId>
    <artifactId>kaptcha</artifactId>
    <version>2.3.2</version>
</dependency>
```
3.配置servlet来生成验证码,org.exam.config.DispatcherServletInitializer.onStartup
```java
@Override
public void onStartup(ServletContext servletContext) throws ServletException {
    super.onStartup(servletContext);
    FilterRegistration.Dynamic encodingFilter = servletContext.addFilter("encoding-filter", CharacterEncodingFilter.class);
    encodingFilter.setInitParameter("encoding", "UTF-8");
    encodingFilter.setInitParameter("forceEncoding", "true");
    encodingFilter.setAsyncSupported(true);
    encodingFilter.addMappingForUrlPatterns(null, false, "/*");
    ServletRegistration.Dynamic kaptchaServlet = servletContext.addServlet("kaptcha-servlet", KaptchaServlet.class);
    kaptchaServlet.addMapping("/except/kaptcha");
}
```
同时将生成验证码的请求排除,不让security来拦截
```java
@Override
public void configure(WebSecurity web) {
    web.ignoring().antMatchers("/static/**","/except/**");
}
```
4.页面加入验证码,然后测试
```html
<input type="text" id="kaptcha" name="kaptcha"/><img src="/testweb/except/kaptcha" width="80" height="25"/>
```

