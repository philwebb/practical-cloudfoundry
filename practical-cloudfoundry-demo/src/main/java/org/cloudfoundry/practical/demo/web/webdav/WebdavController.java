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
package org.cloudfoundry.practical.demo.web.webdav;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import net.sf.webdav.IWebdavStore;
import net.sf.webdav.WebDavServletBean;

import org.cloudfoundry.practical.demo.ExtendedDispatcherServlet;
import org.cloudfoundry.tools.io.Folder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.ServletContextAware;

/**
 * MVC controller that exposes Webdav functionality by delegating to {@link WebDavServletBean}. NOTE: in order to use
 * this controller the {@link ExtendedDispatcherServlet} must be used.
 * 
 * @author Phillip Webb
 */
@Controller
public class WebdavController implements ServletContextAware {

	private static final String MAPPING_PATH = "/dav";

	private IWebdavStore store;

	private ServletContext servletContext;

	private WebDavServletBean servlet;

	@PostConstruct
	public void init() throws ServletException {
		Assert.notNull(this.servletContext, "ServletContext must not be null");
		Assert.notNull(this.store, "Store must not be null");
		this.servlet = new WebDavServletBean() {
			@Override
			public ServletContext getServletContext() {
				return WebdavController.this.servletContext;
			}
		};
		this.servlet.init(this.store, null, null, -1, false);
	}

	@RequestMapping("/dav/**")
	public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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

	@Autowired
	public void setFolder(Folder rootFolder) {
		this.store = new FolderWebdavStore(rootFolder);
	}

	@Override
	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}
}
