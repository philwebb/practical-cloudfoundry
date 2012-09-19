/*
 * Copyright 2010-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cloudfoundry.practical.demo;

import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import org.cloudfoundry.practical.demo.web.WebConfiguration;
import org.cloudfoundry.reconfiguration.spring.CloudApplicationContextInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * Programmatic initialization for a Servlet 3.0 environment.
 * 
 * @author Phillip Webb
 */
public class WebApplicationInitializer implements org.springframework.web.WebApplicationInitializer {

	/**
	 *
	 */
	private static final String DISPATCHER_SERVLET_NAME = "dispatcherServlet";
	private static final String CONTEXT_INITIALIZER_CLASSES = CloudApplicationContextInitializer.class.getName();

	@Override
	public void onStartup(ServletContext servletContext) throws ServletException {
		setupCloudFoundryAutoReconfiguration(servletContext);
		setupRootContext(servletContext);
		setupWebContext(servletContext);
		setupSecurity(servletContext);
		setupTimeoutProtection(servletContext);
	}

	/**
	 * Enable cloud foundry magic. This is required because when running tomcat as a stand alone application.
	 * @param servletContext the servlet context
	 */
	private void setupCloudFoundryAutoReconfiguration(ServletContext servletContext) {
		servletContext.setInitParameter("contextInitializerClasses", CONTEXT_INITIALIZER_CLASSES);
	}

	/**
	 * Add support for the {@link RootConfiguration}.
	 * @param servletContext the servlet context
	 */
	private void setupRootContext(ServletContext servletContext) {
		AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
		context.register(RootConfiguration.class);
		servletContext.addListener(new ContextLoaderListener(context));
	}

	/**
	 * Add a {@link DispatcherServlet} and support for the {@link WebConfiguration}
	 * @param servletContext the servlet context
	 */
	private void setupWebContext(ServletContext servletContext) {
		AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
		context.register(WebConfiguration.class);
		DispatcherServlet servlet = new ExtendedDispatcherServlet(context);
		ServletRegistration.Dynamic registration = servletContext.addServlet(DISPATCHER_SERVLET_NAME, servlet);
		registration.setLoadOnStartup(1);
		registration.addMapping("/*");
	}

	/**
	 * Setup Spring Security.
	 * @param servletContext the servlet context
	 */
	private void setupSecurity(ServletContext servletContext) {
		FilterRegistration.Dynamic registration = servletContext.addFilter("springSecurityFilterChain",
				DelegatingFilterProxy.class);
		registration.addMappingForUrlPatterns(null, false, "/*");
	}

	/**
	 * Add timeout protection.
	 * @param servletContext
	 */
	private void setupTimeoutProtection(ServletContext servletContext) {
		FilterRegistration.Dynamic registration = servletContext.addFilter("timeoutProtectionFilter",
				DelegatingFilterProxy.class);
		registration.addMappingForServletNames(null, false, DISPATCHER_SERVLET_NAME);
	}

}
