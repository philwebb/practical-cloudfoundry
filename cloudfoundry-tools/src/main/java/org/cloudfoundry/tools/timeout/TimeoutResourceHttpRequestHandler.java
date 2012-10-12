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
package org.cloudfoundry.tools.timeout;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

/**
 * Factory Bean that creates a {@link ResourceHttpRequestHandler} to serve timeout resources.
 * 
 * @author Phillip Webb
 */
public class TimeoutResourceHttpRequestHandler extends ResourceHttpRequestHandler implements
		ApplicationListener<ContextRefreshedEvent> {

	private static final long ONE_SECOND = TimeUnit.SECONDS.toMillis(1);

	private Map<Resource, WeakReference<String>> cache = new WeakHashMap<Resource, WeakReference<String>>();

	private TimeoutValues timeoutValues;

	public TimeoutResourceHttpRequestHandler() {
		Resource location = new ClassPathResource("/cloudfoundry/");
		setLocations(Collections.singletonList(location));
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		this.timeoutValues = BeanFactoryUtils.beanOfTypeIncludingAncestors(event.getApplicationContext(),
				TimeoutValues.class);
	}

	@Override
	protected void writeContent(HttpServletResponse response, Resource resource) throws IOException {
		String content = getContent(resource);
		FileCopyUtils.copy(content, response.getWriter());
	}

	private String getContent(Resource resource) throws IOException {
		WeakReference<String> contentReference = this.cache.get(resource);
		String content = contentReference == null ? null : contentReference.get();
		if (content == null) {
			Assert.notNull(this.timeoutValues, "TimeoutValues must not be null");
			content = FileCopyUtils.copyToString(new InputStreamReader(resource.getInputStream()));
			content = content.replace("{polltimeout}", String.valueOf(this.timeoutValues.getThreshold() + ONE_SECOND));
			content = content.replace("{failtimeout}", String.valueOf(this.timeoutValues.getFailTimeout()));
			this.cache.put(resource, new WeakReference<String>(content));
		}
		return content;
	}
}
