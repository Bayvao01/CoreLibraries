package com.core.libraries.logging;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.Ordered;

@Configuration
@EnableAspectJAutoProxy
public class LoggingAspectConfiguration {
	private static final String LOGGER_FILTER_BEAN_NAME = "loggerFilter";
	private static final String[] LOGGER_FILTER_PATHS = { "/*" };

	@Bean
	public LoggingAspect loggingAspect() {
		return new LoggingAspect();
	}

	@SuppressWarnings("rawtypes")
	@Bean
	public FilterRegistrationBean loggerFilter() {
		FilterRegistrationBean bean = new FilterRegistrationBean();

		bean.setName(LOGGER_FILTER_BEAN_NAME);
		bean.setOrder(Ordered.HIGHEST_PRECEDENCE + 4);
		bean.addUrlPatterns(LOGGER_FILTER_PATHS);

		return bean;
	}

}
