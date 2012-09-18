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
package org.cloudfoundry.tools.security;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.Map;

import org.cloudfoundry.client.lib.CloudFoundryClient;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

/**
 * Tests for {@link CloudFoundryAuthenticationProvider}.
 * 
 * @author Phillip Webb
 */
public class CloudFoundryAuthenticationProviderTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private CloudFoundryAuthenticationProvider authenticationProvider = new TestableCloudFoundryAuthenticationProvider();

	private Map<String, String> environment = new HashMap<String, String>();

	private CloudFoundryClientFactory cloudFoundryClientFactory;

	@Mock
	private CloudFoundryClient cloudFoundryClient;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		this.authenticationProvider.setAuthorities("GRANTED_ROLE");
		this.cloudFoundryClientFactory = new CloudFoundryClientFactory() {
			@Override
			public CloudFoundryClient getCloudFoundryClient(String username, String password, String cloudControllerUrl) {
				return CloudFoundryAuthenticationProviderTest.this.cloudFoundryClient;
			}
		};
	}

	@Test
	public void shouldAuthenticateUsingCloudFoundryLogin() throws Exception {
		setupEnvironment("user@cloudfoundry.com");
		given(this.cloudFoundryClient.login()).willReturn("token");
		this.cloudFoundryClientFactory = new CloudFoundryClientFactory() {
			@Override
			public CloudFoundryClient getCloudFoundryClient(String username, String password, String cloudControllerUrl) {
				assertThat(username, is("user@cloudfoundry.com"));
				assertThat(password, is("password"));
				assertThat(cloudControllerUrl, is("https://api.cloudfoundry.com"));
				return CloudFoundryAuthenticationProviderTest.this.cloudFoundryClient;
			}
		};
		Authentication authentication = new UsernamePasswordAuthenticationToken("user@cloudfoundry.com", "password");
		Authentication authenticate = this.authenticationProvider.authenticate(authentication);
		verify(this.cloudFoundryClient).login();
		assertThat(authenticate, is(not(nullValue())));
		assertThat(authenticate.getAuthorities().iterator().next().getAuthority(), is("GRANTED_ROLE"));
	}

	@Test
	public void shouldNotAuthenticateWithNullCredentials() throws Exception {
		Authentication authentication = new UsernamePasswordAuthenticationToken("user@cloudfoundry.com", null);
		this.thrown.expect(BadCredentialsException.class);
		this.authenticationProvider.authenticate(authentication);
	}

	@Test
	public void shouldNotAuthenticateIfNotInActiveUsers() throws Exception {
		setupEnvironment("user@cloudfoundry.com");
		Authentication authentication = new UsernamePasswordAuthenticationToken("missing@cloudfoundry.com", "password");
		this.thrown.expect(BadCredentialsException.class);
		this.authenticationProvider.authenticate(authentication);
	}

	@Test
	public void shouldNotAuthenticateIfCantLoginUsingCloudFoundryClientDueToMissingToken() throws Exception {
		setupEnvironment("user@cloudfoundry.com");
		given(this.cloudFoundryClient.login()).willReturn("");
		Authentication authentication = new UsernamePasswordAuthenticationToken("user@cloudfoundry.com", null);
		this.thrown.expect(BadCredentialsException.class);
		this.authenticationProvider.authenticate(authentication);
	}

	@Test
	public void shouldNotAuthenticateIfCantLoginUsingCloudFoundryClientDueToException() throws Exception {
		setupEnvironment("user@cloudfoundry.com");
		given(this.cloudFoundryClient.login()).willThrow(new RuntimeException());
		Authentication authentication = new UsernamePasswordAuthenticationToken("user@cloudfoundry.com", null);
		this.thrown.expect(BadCredentialsException.class);
		this.authenticationProvider.authenticate(authentication);
	}

	private void setupEnvironment(String user) {
		StringBuilder vcap = new StringBuilder();
		vcap.append("{");
		vcap.append("\"users\":[\"" + user + "\"]");
		vcap.append("}");
		this.environment.put("VCAP_APPLICATION", vcap.toString());
	}

	private static interface CloudFoundryClientFactory {
		CloudFoundryClient getCloudFoundryClient(String username, String password, String cloudControllerUrl);
	}

	private class TestableCloudFoundryAuthenticationProvider extends CloudFoundryAuthenticationProvider {

		@Override
		protected CloudFoundryClient getCloudFoundryClient(String username, String password, String cloudControllerUrl) {
			return CloudFoundryAuthenticationProviderTest.this.cloudFoundryClientFactory.getCloudFoundryClient(
					username, password, cloudControllerUrl);
		}

		@Override
		protected String getValue(String name) {
			return CloudFoundryAuthenticationProviderTest.this.environment.get(name);
		}
	}

}
