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
package org.cloudfoundry.practical.demo.core.webdav;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import net.sf.webdav.IWebdavStore;
import net.sf.webdav.WebDavServletBean;

import org.cloudfoundry.tools.io.Folder;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * Servlet {@link Filter} used to support WebDav requests. Used to delegate to {@link WebDavServletBean} whilst
 * retaining a single {@link DispatcherServlet} mapping.
 * 
 * @author Phillip Webb
 */
@Component
public class WebDavFilter implements Filter, ServletContextAware, InitializingBean {

	private static final String MAPPING_PATH = "/dav";

	private ServletContext servletContext;

	private IWebdavStore store;

	private WebDavServletBean servlet;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
			ServletException {
		if ((request instanceof HttpServletRequest)
				&& ((HttpServletRequest) request).getPathInfo().startsWith(MAPPING_PATH)) {
			handleWebDav((HttpServletRequest) request, (HttpServletResponse) response);
		} else {
			chain.doFilter(request, response);
		}
	}

	private void handleWebDav(HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException {
		HttpServletRequestWrapper requestWrapper = new HttpServletRequestWrapper(request) {

			@Override
			public String getServletPath() {
				return super.getServletPath() + MAPPING_PATH;
			}

			@Override
			public String getPathInfo() {
				return super.getPathInfo().substring(MAPPING_PATH.length());
			}
		};
		this.servlet.service(requestWrapper, response);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(this.servletContext, "ServletContext must not be null");
		Assert.notNull(this.store, "Store must not be null");
		this.servlet = new WebDavServletBean() {
			@Override
			public ServletContext getServletContext() {
				return WebDavFilter.this.servletContext;
			}
		};
		this.servlet.init(this.store, null, null, -1, false);
	}

	@Override
	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	@Autowired
	public void setFolder(Folder rootFolder) {
		this.store = new FolderWebdavStore(rootFolder);
	}
}
