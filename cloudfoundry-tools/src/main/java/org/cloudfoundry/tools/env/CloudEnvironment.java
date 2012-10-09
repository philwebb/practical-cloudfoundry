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
package org.cloudfoundry.tools.env;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.cloudfoundry.runtime.env.ApplicationInstanceInfo;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.util.StringUtils;

/**
 * Provided access to cloud foundry environment information. This version extends
 * {@link org.cloudfoundry.runtime.env.CloudEnvironment}, providing additional methods as well as handy
 * {@link #current() static} access.
 * 
 * @author Phillip Webb
 */
public class CloudEnvironment extends org.cloudfoundry.runtime.env.CloudEnvironment {

	private static final String CLOUD_CONTROLLER_VARIABLE_NAME = "cloudcontroller";

	private static final String DEFAULT_CONTROLLER_URL = "https://api.cloudfoundry.com";

	private static final Pattern CONTROLLER_PATTERN = Pattern.compile("^(https?://).*?\\.(.*$)");

	private static CloudEnvironment environment;

	private ObjectMapper objectMapper = new ObjectMapper();

	/**
	 * Returns the users currently logged in.
	 * @return the users
	 */
	@SuppressWarnings("unchecked")
	public List<String> getUsers() {
		Map<String, Object> vapApplication = getValueAsMap("VCAP_APPLICATION");
		List<String> users = (List<String>) vapApplication.get("users");
		return (users == null ? Collections.<String> emptyList() : users);
	}

	/**
	 * Returns the user currently logged in.
	 * @return the user
	 */
	public String getUser() {
		List<String> users = getUsers();
		if (users.isEmpty()) {
			return null;
		}
		return users.get(0);
	}

	/**
	 * Returns the controller URL. This method will construct the URL based on the application URL. If the url cannot be
	 * determined set a {@code cloudcontroller} environment variable.
	 * @return the controller URL.
	 */
	public String getControllerUrl() {
		String controllerUrl = getValue(CLOUD_CONTROLLER_VARIABLE_NAME);
		if (StringUtils.hasLength(controllerUrl)) {
			return controllerUrl;
		}

		ApplicationInstanceInfo instanceInfo = getInstanceInfo();
		List<String> uris = instanceInfo == null ? null : instanceInfo.getUris();
		if (uris == null || uris.isEmpty()) {
			return DEFAULT_CONTROLLER_URL;
		}
		String uri = uris.get(0).toLowerCase();
		if (uri.endsWith("cloudfoundry.com")) {
			return DEFAULT_CONTROLLER_URL;
		}
		if (!uri.startsWith("http")) {
			uri = "http://" + uri;
		}
		Matcher matcher = CONTROLLER_PATTERN.matcher(uri);
		if (matcher.matches()) {
			return matcher.group(1) + "api." + matcher.group(2);
		}

		return DEFAULT_CONTROLLER_URL;
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> getValueAsMap(String name) {
		String value = getValue(name);
		if (StringUtils.hasLength(value)) {
			try {
				return this.objectMapper.readValue(value, Map.class);
			} catch (Exception e) {
				throw new IllegalStateException("Unable to read value '" + name + "' as a map", e);
			}
		}
		return Collections.emptyMap();
	}

	public static CloudEnvironment current() {
		if (environment == null) {
			environment = new CloudEnvironment();
		}
		return environment;
	}
}
