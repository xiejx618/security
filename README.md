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
四.配置启用全局方法.
1.使用@EnableGlobalMethodSecurity注解,并继承GlobalMethodSecurityConfiguration.因为需要AuthenticationManager,所以在前面将这个Bean暴露出来就得了,并配置一下没有权限抛403时的跳转页面.整理后如下:
```java
package org.exam.config;

import org.exam.security.CustomAuthenticationProvider;
import org.exam.security.CustomUserDetailsService;
import org.exam.security.KaptchaAuthenticationFilter;
import org.exam.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
/**
 * Created by xin on 15/1/7.
 */
@Configuration
public class SecurityConfig {
    @Configuration
    @EnableWebSecurity
    static class WebSecurityConfig extends WebSecurityConfigurerAdapter {
        @Autowired
        private UserService userService;

        @Bean
        public ReloadableResourceBundleMessageSource messageSource() {
            //本地化(不完全)
            ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
            messageSource.setBasename("classpath:org/springframework/security/messages");
            return messageSource;
        }

        @Bean
        public UserDetailsService userDetailsService() {
            return new CustomUserDetailsService(userService);
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
            PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4);//这里的strength为4-31位,设置成16都觉得编码有点慢了
            return passwordEncoder;
        }

        //暴露AuthenticationManager注册成Bean供@EnableGlobalMethodSecurity使用
        @Bean
        @Override
        public AuthenticationManager authenticationManagerBean() throws Exception {
            return super.authenticationManagerBean();
        }

        @Override
        protected void configure(AuthenticationManagerBuilder auth) throws Exception {
            DaoAuthenticationProvider authenticationProvider = new CustomAuthenticationProvider(userDetailsService(), userService);
            authenticationProvider.setPasswordEncoder(passwordEncoder());
            auth.authenticationProvider(authenticationProvider);
        }

        @Override
        public void configure(WebSecurity web) {
            web.ignoring().antMatchers("/static/**", "/except/**");
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.addFilterBefore(new KaptchaAuthenticationFilter("/login", "/login?error"), UsernamePasswordAuthenticationFilter.class)
                    .authorizeRequests().anyRequest().authenticated()
                    .and().formLogin().loginPage("/login").failureUrl("/login?error").usernameParameter("username").passwordParameter("password").permitAll()
                    .and().logout().logoutUrl("/logout").permitAll()
                    .and().rememberMe().key("9D119EE5A2B7DAF6B4DC1EF871D0AC3C")
                    .and().exceptionHandling().accessDeniedPage("/except/403");
        }
    }

    @Configuration
    @EnableGlobalMethodSecurity(prePostEnabled = true)
    static class MethodSecurityConfig extends GlobalMethodSecurityConfiguration { }
}
```
2./except/403请求一样应排除在拦截之外,还有转到那个页面渲染,在org.exam.config.MvcConfig.addViewControllers配置
```java
@Override
public void addViewControllers(ViewControllerRegistry registry) {
    registry.addViewController("/").setViewName("index");
    registry.addViewController("/login").setViewName("login");
    registry.addViewController("/except/403").setViewName("except/403");
    registry.setOrder(Ordered.HIGHEST_PRECEDENCE);
}
```
3.在方法(类或接口上)添加注解来限制方法的访问.注解类注册的Bean所处的ApplicationContext应与@EnableGlobalMethodSecurity对应的ApplicationContext是同一个.
因为把@EnableGlobalMethodSecurity放在org.exam.config.SecurityConfig.MethodSecurityConfig,而SecurityConfig放在RootApplicationContext,所以@PreAuthorize也应在RootApplicationContext.
我们使用prePostEnabled(从参考文档介绍,要比securedEnabled和jsr250Enabled强大).例如:
public interface UserService {
    @PreAuthorize("hasAuthority('user_query')")
    Page<User> findAll(Pageable pageable);
    User save(User user);
    User findOne(Long id);
    void delete(Long id);
}

3.添加测试用户
```java
    @Test
    @Rollback(false)
    public void testSave() {
        Authority authority1=new Authority();
        authority1.setName("查看用户");
        authority1.setAuthority("USER_QUERY");
        authorityRepository.save(authority1);
        Authority authority2=new Authority();
        authority2.setName("保存用户");
        authority2.setAuthority("USER_SAVE");
        authorityRepository.save(authority2);
        Authority authority3=new Authority();
        authority3.setName("删除用户");
        authority3.setAuthority("USER_DELETE");
        authorityRepository.save(authority3);
        Role role1=new Role();
        role1.setName("管理员");
        role1.setAuthorities(new HashSet<>(Arrays.asList(authority2, authority3)));
        roleRepository.save(role1);
        User user1=new User();
        user1.setUsername("admin");
        user1.setPassword("$2a$04$fCqcakHV2O.4AJgp3CIAGO9l5ZBq61Gt6YNzjcyC8M.js0ucpyun.");//admin
        user1.setRoles(new HashSet<>(Arrays.asList(role1)));
        userRepository.save(user1);
    }
```
还有,参考文档有安全表达试的介绍,比如下面代表的意思:传入的联系人为当前的认证用户,才可以执行doSomething方法
@PreAuthorize("#c.name == authentication.name")
public void doSomething(@P("c")Contact contact);

四.页面使用权限进行控制显示(与启用全局方法安全无关)
${pageContext.request.remoteUser}这样可以获取当前登录的用户名.jsp页面要根据权限来显示页面元素,可以先引入spring-security-taglibs包,再使用spring security标签就可以.下面是使用例子
```html
<sec:authorize access="isRememberMe()">欢迎你通过记住我登录到首页!</sec:authorize>
<sec:authorize access="isFullyAuthenticated()">${pageContext.request.remoteUser},欢迎你通过用户名/密码到首页!</sec:authorize></span>
<sec:authorize access="hasAuthority('USER_QUERY')">你有USER_QUERY权限</sec:authorize>
```
五.session并发控制与集成Spring Session
session并发控制
1.注册SessionRegistry Bean
```java
@Bean
public SessionRegistry sessionRegistry(){
    return new SessionRegistryImpl();
}
```
2.配置并发管理
```java
@Override
protected void configure(HttpSecurity http) throws Exception {
    http.addFilterBefore(new KaptchaAuthenticationFilter("/login", "/login?error"), UsernamePasswordAuthenticationFilter.class)
            .authorizeRequests().anyRequest().authenticated()
            .and().formLogin().loginPage("/login").failureUrl("/login?error").usernameParameter("username").passwordParameter("password").permitAll()
            .and().logout().logoutUrl("/logout").permitAll()
            .and().rememberMe().key("9D119EE5A2B7DAF6B4DC1EF871D0AC3C")
            .and().exceptionHandling().accessDeniedPage("/except/403")
            .and().sessionManagement().maximumSessions(2).expiredUrl("/login?expired").sessionRegistry(sessionRegistry());
}
```
3.SessionRegistry获取所需信息
```java
@Autowired
private SessionRegistry sessionRegistry;
@RequestMapping("/")
public String index(Model model){
    int numOfUsers=sessionRegistry.getAllPrincipals().size();
    model.addAttribute("numOfUsers",numOfUsers);
    return "index";
}
```
下面继续看看集成SpringSession
1.加入主要依赖
```xml
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-oxm</artifactId>
    <version>${spring.version}</version>
</dependency>
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-context-support</artifactId>
    <version>${spring.version}</version>
</dependency>
<dependency>
    <groupId>org.springframework.data</groupId>
    <artifactId>spring-data-redis</artifactId>
    <version>${spring.data.redis.version}</version>
</dependency>
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-pool2</artifactId>
    <version>${commons.pool2.version}</version>
</dependency>
<dependency>
    <groupId>redis.clients</groupId>
    <artifactId>jedis</artifactId>
    <version>2.5.2</version>
</dependency>
<dependency>
    <groupId>org.springframework.session</groupId>
    <artifactId>spring-session</artifactId>
    <version>${spring.session.version}</version>
</dependency>
```
2.启用EnableRedisHttpSession,并配置一个连接工厂,并将此配置加到RootApplicationContext
```java
package org.exam.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
@Configuration
@EnableRedisHttpSession
public class HttpSessionConfig {
    @Bean
    public JedisConnectionFactory jedisConnectionFactory() {
        return new JedisConnectionFactory();
    }
}
```
```java
@Override
protected Class<?>[] getRootConfigClasses() {
    return new Class<?>[]{AppConfig.class,HttpSessionConfig.class,SecurityConfig.class,MvcConfig.class};
}
```
3.配置springSessionRepositoryFilter.并确保springSessionRepositoryFilter要比springSecurityFilterChain靠前
```java
package org.exam.config;

import org.springframework.core.annotation.Order;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.session.web.context.AbstractHttpSessionApplicationInitializer;

import javax.servlet.ServletContext;
@Order(99)
public class HttpSessionApplicationInitializer extends AbstractHttpSessionApplicationInitializer {
    @Override
    protected void afterSessionRepositoryFilter(ServletContext servletContext) {
        servletContext.addListener(new HttpSessionEventPublisher());
    }
}
```
5.启动redis测试
redis:
a.查询所有key:keys命令,keys *
b.根据某个key删除,使用del命令
