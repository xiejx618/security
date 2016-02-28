package org.exam.config;

import org.springframework.core.annotation.Order;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.session.web.context.AbstractHttpSessionApplicationInitializer;

import javax.servlet.ServletContext;

/**
 * Created by xin on 16/1/23.
 */
@Order(99)
public class HttpSessionApplicationInitializer extends AbstractHttpSessionApplicationInitializer {
    @Override
    protected void afterSessionRepositoryFilter(ServletContext servletContext) {
        servletContext.addListener(new HttpSessionEventPublisher());
    }
}
