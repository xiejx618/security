package org.exam.config;

import org.springframework.core.annotation.Order;
import org.springframework.session.web.context.AbstractHttpSessionApplicationInitializer;

/**
 * Created on 16/1/23.
 */
@Order(99)
public abstract class HttpSessionApplicationInitializer extends AbstractHttpSessionApplicationInitializer {
}