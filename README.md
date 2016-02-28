一.基本搭建

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
三.启用CSRF,加入记住我和增强密码功能
1.启用CSRF.一旦启用,那些Action为PATCH, POST, PUT, and DELETE的请求(包含登录和登出)都要附加CSRF Token提交到服务端.还有,登出也要使用POST(参考官方文档,当然可改为GET,但不推荐).
  1.1.下面使用比较笨的方法:加入csrf Input标签.
  a.在登录页面和其它表单都添加加入<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
  b.登出:<form action="${logoutUrl}" method="post"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/><input type="submit" value="退出"/></form>
  1.2.如果使用Spring MVC <form:form>标签或Thymeleaf 2.1+,使用@EnableWebMvcSecurity替换@EnableWebSecurity,那么提交的表单会自动嵌入CsrfToken提交到服务端(使用4.0.0.RC1发现@EnableWebMvcSecurity已过时,@EnableWebSecurity已有这样的功能,即用回@EnableWebSecurity即可)
2.记住我功能
2.1修改配置如下:
```java
@Override
protected void configure(HttpSecurity http) throws Exception {
    http.addFilterBefore(new KaptchaAuthenticationFilter("/login", "/login?error"), UsernamePasswordAuthenticationFilter.class)
            .authorizeRequests().anyRequest().authenticated()
            .and().formLogin().loginPage("/login").failureUrl("/login?error").usernameParameter("username").passwordParameter("password").permitAll()
            .and().logout().logoutUrl("/logout").permitAll()
            .and().rememberMe().key("9D119EE5A2B7DAF6B4DC1EF871D0AC3C");
}
```
2.2记住我需要userDetailsService
```java
@Bean
public UserDetailsService userDetailsService(){
    return new CustomUserDetailsService(userService);
}
```
同时将AuthenticationManagerBuilder配置改一下
```java
@Override
protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    DaoAuthenticationProvider authenticationProvider=new CustomAuthenticationProvider(userDetailsService(),userService);
    auth.authenticationProvider(authenticationProvider);
}
```
2.3登录页面加入记住我checkbox
```html
<input type="checkbox" name="remember-me" value="true"/>Remember me
```

关于CSRF攻击:
假设银行网站提供一个表单，它允许从当前登录的用户转帐到另一个银行帐户.例如,HTTP请求可能与如下相似：
POST /transfer HTTP/1.1
Host: bank.example.com
Cookie: JSESSIONID=randomid; Domain=bank.example.com; Secure; HttpOnly
Content-Type: application/x-www-form-urlencoded
amount=100.00&routingNumber=1234&account=9876

现在再假设你认证了你的银行站点,并且没有登出,去访问了一个恶意网站.此恶意网站包含如下一个表单的HTML页面:
```html
<form action="https://bank.example.com/transfer" method="post">
<input type="hidden" name="amount" value="100.00"/>
<input type="hidden" name="routingNumber" value="evilsRoutingNumber"/>
<input type="hidden" name="account" value="evilsAccountNumber"/>
<input type="submit" value="Win Money!"/>
</form>
```
你想要赢得这部分钱,那么你会点击提交按钮.在这个过程中,你无意转了100美元到一个恶意用户.这是因为,虽然恶意网站无法看到你的cookies,但是此cookies会和你关联的银行伴随着请求一起发送.
更糟的是,整个过程可以自动使用JavaScript.这意味着你甚至不需要点击按钮,那么我们如何保护自己免受这种攻击?(哈哈,当然spring security的防止csrf功能就是一种解决方案).

3.增强密码.
3.1使用DaoAuthenticationProvider
```java
@Override
protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    DaoAuthenticationProvider authenticationProvider=new CustomAuthenticationProvider(userDetailsService(),userService);
    authenticationProvider.setPasswordEncoder(passwordEncoder());
    auth.authenticationProvider(authenticationProvider);
}
```
3.2注册PasswordEncoder Bean,这里的strength为4-31位,设置成16都觉得编码有点慢了
```java
@Bean
public PasswordEncoder passwordEncoder(){
    PasswordEncoder passwordEncoder=new BCryptPasswordEncoder(4);
    return passwordEncoder;
}
```
3.3注册的这个bean就可注入其它地方来生成密码,测试最简单的使用main
```java
public static void main(String[] args) {
    PasswordEncoder passwordEncoder=new BCryptPasswordEncoder(4);
    String result=passwordEncoder.encode("admin");
    System.out.println(result);
}
```
最后当然测试啦.

