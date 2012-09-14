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

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import org.cloudfoundry.practical.demo.web.WebConfiguration;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * @author Phillip Webb
 */
public class WebApplicationInitializer implements org.springframework.web.WebApplicationInitializer {

	@Override
	public void onStartup(ServletContext servletContext) throws ServletException {
		setupRootContext(servletContext);
		setupWebContext(servletContext);
	}

	private void setupRootContext(ServletContext servletContext) {
		AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
		context.register(RootConfiguration.class);
		servletContext.addListener(new ContextLoaderListener(context));
	}

	private void setupWebContext(ServletContext servletContext) {
		AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
		context.register(WebConfiguration.class);
		DispatcherServlet dispatcherServlet = new DispatcherServlet(context);
		ServletRegistration.Dynamic servletRegistration = servletContext.addServlet("dispatcherServlet",
				dispatcherServlet);
		servletRegistration.setLoadOnStartup(1);
		servletRegistration.addMapping("/");
	}

}
