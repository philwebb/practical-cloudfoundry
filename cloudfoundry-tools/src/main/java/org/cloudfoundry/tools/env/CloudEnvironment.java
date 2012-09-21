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

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.util.StringUtils;

/**
 * Provided access to cloud foundry environment information.
 * 
 * @author Phillip Webb
 */
public class CloudEnvironment extends org.cloudfoundry.runtime.env.CloudEnvironment {

	// FIXME test

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
	 * Returns the controller URL.
	 * @return the controller URL.
	 */
	public String getControllerUrl() {
		// FIXME work this out
		return "https://api.cloudfoundry.com";
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
