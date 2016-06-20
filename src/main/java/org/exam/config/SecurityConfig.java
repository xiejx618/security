package org.exam.config;

import org.exam.repository.mongo.MongoSessionInfoRepo;
import org.exam.security.SessionRegistryImpl;
import org.exam.security.UserDetailsServiceImpl;
import org.exam.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Created on 15/1/7.
 */
@Configuration
public class SecurityConfig {
    @Configuration
    @EnableWebSecurity
    protected static class WebSecurityConfig extends WebSecurityConfigurerAdapter {
        @Autowired
        private UserService userService;
        @Bean
        public UserDetailsService userDetailsService() {
            return new UserDetailsServiceImpl(userService);
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder(4);
        }

        @Autowired
        private MongoSessionInfoRepo mongoSessionInfoRepo;
        @Autowired
        private MongoTemplate mongoTemplate;

        @Bean
        public SessionRegistry sessionRegistry() {
            return new SessionRegistryImpl(mongoSessionInfoRepo, mongoTemplate);
        }

        @Override
        protected void configure(AuthenticationManagerBuilder auth) throws Exception {
            auth.userDetailsService(userDetailsService()).passwordEncoder(passwordEncoder());
        }

        @Override
        public void configure(WebSecurity web) {
            web.ignoring().antMatchers("/static/**", "/exclude/**");
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.authorizeRequests().anyRequest().authenticated()
                    .and().formLogin().loginPage("/login").permitAll()
                    .and().logout().permitAll()
                    .and().rememberMe()
                    .and().exceptionHandling().accessDeniedPage("/exclude/403")
                    .and().sessionManagement().maximumSessions(2).expiredUrl("/login?expired").sessionRegistry(sessionRegistry());
        }

        @Bean
        public ReloadableResourceBundleMessageSource messageSource() {
            ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();//本地化(不完全)
            messageSource.setBasename("classpath:org/springframework/security/messages");
            return messageSource;
        }

        //暴露AuthenticationManager注册成Bean供@EnableGlobalMethodSecurity使用
        @Bean
        @Override
        public AuthenticationManager authenticationManagerBean() throws Exception {
            return super.authenticationManagerBean();
        }
    }

    @Configuration
    @EnableGlobalMethodSecurity(prePostEnabled = true)
    protected static class MethodSecurityConfig extends GlobalMethodSecurityConfiguration {
    }
}