package org.exam.config;

import org.springframework.core.annotation.Order;
import org.springframework.security.web.context.AbstractSecurityWebApplicationInitializer;

/**
 * Created by xin on 15/1/7.
 */
@Order(100)
public class SecurityWebApplicationInitializer extends AbstractSecurityWebApplicationInitializer {
}
