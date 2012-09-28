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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;
import org.thymeleaf.spring3.SpringTemplateEngine;
import org.thymeleaf.spring3.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

/**
 * Configuration for Spring MVC.
 * 
 * @author Phillip Webb
 */
@Configuration
@EnableWebMvc
@ComponentScan
public class WebConfiguration {

	private static final String DOJO_ROOT = "dojo-release-1.8.0";
	private static final String DOJO_ZIP = "/resources/dojo-release-1.8.0.zip";

	@Bean
	public ServletContextTemplateResolver thymleafTemplateResolver() {
		ServletContextTemplateResolver resolver = new ServletContextTemplateResolver();
		resolver.setPrefix("/WEB-INF/templates/");
		resolver.setSuffix(".html");
		resolver.setTemplateMode("HTML5");
		return resolver;
	}

	@Bean
	public SpringTemplateEngine thymleafTemplateEngine() {
		SpringTemplateEngine templateEngine = new SpringTemplateEngine();
		templateEngine.setTemplateResolver(thymleafTemplateResolver());
		return templateEngine;
	}

	@Bean
	public ThymeleafViewResolver thymeleafViewResolver() {
		ThymeleafViewResolver resolver = new ThymeleafViewResolver();
		resolver.setTemplateEngine(thymleafTemplateEngine());
		return resolver;
	}

	@Bean
	public HandlerMapping resourcesHandlerMapping() {
		SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
		Map<String, Object> urlMap = new HashMap<String, Object>();
		urlMap.put("/cloudfoundry/**", cloudfoundryResourceHandler());
		urlMap.put("/dojo/**", dojoResourceHandler());
		urlMap.put("/dojox/**", dojoxResourceHandler());
		urlMap.put("/dijit/**", dijitResourceHandler());
		mapping.setUrlMap(urlMap);
		return mapping;
	}

	@Bean
	public HttpRequestHandler cloudfoundryResourceHandler() {
		ResourceHttpRequestHandler handler = new ResourceHttpRequestHandler();
		Resource location = new ClassPathResource("/cloudfoundry/");
		handler.setLocations(Collections.singletonList(location));
		return handler;
	}

	@Bean
	public HttpRequestHandler dojoResourceHandler() {
		return new ZippedServletResourceHandler(DOJO_ZIP, DOJO_ROOT + "/dojo");
	}

	@Bean
	public HttpRequestHandler dojoxResourceHandler() {
		return new ZippedServletResourceHandler(DOJO_ZIP, DOJO_ROOT + "/dojox");
	}

	@Bean
	public HttpRequestHandler dijitResourceHandler() {
		return new ZippedServletResourceHandler(DOJO_ZIP, DOJO_ROOT + "/dijit");
	}
}
