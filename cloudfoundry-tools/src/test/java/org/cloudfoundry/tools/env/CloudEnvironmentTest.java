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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

/**
 * Tests for {@link CloudEnvironment}.
 * 
 * @author Phillip Webb
 */
public class CloudEnvironmentTest {

	private Map<String, String> environment = new HashMap<String, String>();
	private CloudEnvironment cloudEnvironment = new CloudEnvironment() {
		@Override
		public String getValue(String key) {
			return CloudEnvironmentTest.this.environment.get(key);
		};
	};

	@Test
	public void shouldGetUsers() throws Exception {
		this.environment.put("VCAP_APPLICATION", "{\"users\":[\"user1@domain.com\",\"user2@domain.com\"]}");
		assertThat(this.cloudEnvironment.getUsers(), is(Arrays.asList("user1@domain.com", "user2@domain.com")));
	}

	@Test
	public void shouldGetEmptyUsersIfMissing() throws Exception {
		assertThat(this.cloudEnvironment.getUsers(), is(Collections.<String> emptyList()));
		this.environment.put("VCAP_APPLICATION", "{}");
		assertThat(this.cloudEnvironment.getUsers(), is(Collections.<String> emptyList()));
	}

	@Test
	public void shouldGetUser() throws Exception {
		this.environment.put("VCAP_APPLICATION", "{\"users\":[\"user@domain.com\"]}");
		assertThat(this.cloudEnvironment.getUser(), is("user@domain.com"));
	}

	@Test
	public void shouldGetFirstUserIfMultiple() throws Exception {
		this.environment.put("VCAP_APPLICATION", "{\"users\":[\"user1@domain.com\",\"user2@domain.com\"]}");
		assertThat(this.cloudEnvironment.getUser(), is("user1@domain.com"));
	}

	@Test
	public void shouldGetNullUserIfNone() throws Exception {
		assertThat(this.cloudEnvironment.getUser(), is(nullValue()));
	}

	@Test
	public void shouldGetControllerUrlFromApplicationUrl() throws Exception {
		assertThat(getControllerUri("https://myapp.cloudfoundry.com"), is("https://api.cloudfoundry.com"));
		assertThat(getControllerUri("https://my.app.cloudfoundry.com"), is("https://api.cloudfoundry.com"));
		assertThat(getControllerUri("http://myapp.CLOUDFOUNDRY.com"), is("https://api.cloudfoundry.com"));
		assertThat(getControllerUri("http://my.app.cloUDfoundry.com"), is("https://api.cloudfoundry.com"));
		assertThat(getControllerUri("http://myapp.vcap.me"), is("http://api.vcap.me"));
		assertThat(getControllerUri("https://myapp.vcap.me"), is("https://api.vcap.me"));
		assertThat(getControllerUri("https://my.app.vcap.me"), is("https://api.app.vcap.me"));
	}

	@Test
	public void shouldGetControllerUrlIfDefined() throws Exception {
		this.environment.put("cloudcontroller", "https://myurl.com");
		assertThat(getControllerUri("https://myapp.cloudfoundry.com"), is("https://myurl.com"));
		assertThat(getControllerUri("https://myapp.vcap.me"), is("https://myurl.com"));
	}

	private String getControllerUri(String url) {
		this.environment.put("VCAP_APPLICATION", "{\"instance_index\":0,\"port\":1234,\"uris\":[\"" + url + "\"]}");
		return this.cloudEnvironment.getControllerUrl();
	}
}
