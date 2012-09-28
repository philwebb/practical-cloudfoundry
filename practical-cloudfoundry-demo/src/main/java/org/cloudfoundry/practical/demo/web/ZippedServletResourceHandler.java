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
package org.cloudfoundry.practical.demo.web;

import java.io.IOException;
import java.util.Collections;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.context.support.ServletContextResource;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

/**
 * {@link HttpRequestHandler} that can serve resources from inside a zip file.
 * 
 * @author Phillip Webb
 */
public class ZippedServletResourceHandler implements HttpRequestHandler, ServletContextAware, InitializingBean,
		BeanFactoryAware, BeanNameAware {

	private ServletContext servletContext;

	private ResourceHttpRequestHandler handler;

	private BeanFactory beanFactory;

	private String beanName;

	private String zipFile;

	private String rootPath;

	public ZippedServletResourceHandler(String zipFile, String rootPath) {
		this.zipFile = zipFile;
		this.rootPath = rootPath;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		ServletContextResource zipResource = new ServletContextResource(this.servletContext, this.zipFile);
		UrlResource urlResource = new UrlResource("jar:" + zipResource.getFile().toURI().toString() + "!/"
				+ this.rootPath + "/");
		this.handler = new ResourceHttpRequestHandler();
		this.handler.setLocations(Collections.<Resource> singletonList(urlResource));
		((AutowireCapableBeanFactory) this.beanFactory).initializeBean(this.handler, this.beanName + "ResourceHandler");
	}

	@Override
	public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException {
		this.handler.handleRequest(request, response);
	}

	@Override
	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	@Override
	public void setBeanName(String name) {
		this.beanName = name;
	}
}
