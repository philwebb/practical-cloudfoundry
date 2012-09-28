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

import java.net.MalformedURLException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudfoundry.client.lib.CloudFoundryClient;
import org.cloudfoundry.tools.env.CloudEnvironment;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * Spring Security {@link AuthenticationProvider} that authenticated using the active cloud foundry users credentials.
 * 
 * @author Phillip Webb
 */
public class CloudFoundryAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

	private static Log logger = LogFactory.getLog(CloudFoundryAuthenticationProvider.class);

	private List<GrantedAuthority> authorities;

	@Override
	protected UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken authentication)
			throws AuthenticationException {
		logger.debug("Attempting login of " + username + " via cloudfoundry");

		Object credentials = authentication.getCredentials();
		if (credentials == null) {
			logger.debug("Empty credentials provided for " + username);
			throw new BadCredentialsException("Bad credentials");
		}

		List<String> activeUsers = cloudEnvironment().getUsers();
		if (!activeUsers.contains(username)) {
			logger.debug("User " + username + " not found in active users " + activeUsers);
			throw new UsernameNotFoundException(username);
		}
		String token = login(username, credentials.toString());
		logger.debug("User " + username + " logged in via cloudfoundry");
		return new User(username, token, this.authorities);
	}

	private String login(String username, String password) {
		String controllerUrl = cloudEnvironment().getControllerUrl();
		try {
			return getCloudFoundryClient(username, password, controllerUrl).login();
		} catch (Exception e) {
			logger.debug("Unable to login " + username + " to " + controllerUrl, e);
			throw new BadCredentialsException("Bad credentials");
		}
	}

	/**
	 * Returns the cloud environment.
	 * @return the cloud environment.
	 */
	protected CloudEnvironment cloudEnvironment() {
		return CloudEnvironment.current();
	}

	/**
	 * Factory method used to create a {@link CloudFoundryClient}.
	 * @param username the user name
	 * @param password the password
	 * @param cloudControllerUrl the controller URL
	 * @return a new client
	 */
	protected CloudFoundryClient getCloudFoundryClient(String username, String password, String cloudControllerUrl) {
		try {
			return new CloudFoundryClient(username, password, cloudControllerUrl);
		} catch (MalformedURLException e) {
			throw new IllegalStateException("Malformed cloud controller URL " + cloudControllerUrl, e);
		}
	}

	@Override
	protected void additionalAuthenticationChecks(UserDetails userDetails,
			UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
	}

	/**
	 * @param authorities the granted authorities
	 */
	public void setAuthorities(String authorities) {
		this.authorities = AuthorityUtils.commaSeparatedStringToAuthorityList(authorities);
	}
}
