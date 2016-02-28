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
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
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

        @Bean
        public SessionRegistry sessionRegistry(){
            return new SessionRegistryImpl();
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
                    .and().exceptionHandling().accessDeniedPage("/except/403")
                    .and().sessionManagement().maximumSessions(2).expiredUrl("/login?expired").sessionRegistry(sessionRegistry());
        }
    }

    @Configuration
    @EnableGlobalMethodSecurity(prePostEnabled = true)
    static class MethodSecurityConfig extends GlobalMethodSecurityConfiguration { }
}