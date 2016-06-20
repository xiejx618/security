package org.exam.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

import javax.annotation.Resource;

/**
 * Created on 16/1/23.
 */
@Configuration
@EnableRedisHttpSession
@PropertySource("classpath:config.properties")
public class HttpSessionConfig {
    @Resource
    private Environment env;
    @Bean
    public JedisConnectionFactory jedisConnectionFactory() {
        JedisConnectionFactory connectionFactory = new JedisConnectionFactory();
        connectionFactory.setHostName(env.getProperty("redis.host"));
        connectionFactory.setPort(env.getProperty("redis.port",Integer.class));
        return connectionFactory;
    }
    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }
}
