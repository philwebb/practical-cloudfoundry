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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.junit.Test;

/**
 * Tests for {@link TimeoutProtectionHttpRequest}.
 * 
 * @author Phillip Webb
 */
public class TimeoutProtectionHttpRequestTest {

	private static final String UID = "xxxx-xxxx-xxxx-xxxx";

	@Test
	public void shouldNotGetFromNotHttp() throws Exception {
		assertNull(TimeoutProtectionHttpRequest.get(null));
		assertNull(TimeoutProtectionHttpRequest.get(mock(ServletRequest.class)));
	}

	@Test
	public void shouldGetInitialRequest() throws Exception {
		HttpServletRequest request = mock(HttpServletRequest.class);
		given(request.getHeader("x-cloudfoundry-timeout-protection-initial-request")).willReturn(UID);
		TimeoutProtectionHttpRequest protectionRequest = TimeoutProtectionHttpRequest.get(request);
		assertNotNull(protectionRequest);
		assertThat(protectionRequest.getServletRequest(), is(request));
		assertThat(protectionRequest.getType(), is(TimeoutProtectionHttpRequest.Type.INITIAL_REQUEST));
		assertThat(protectionRequest.getUid(), is(UID));
	}

	@Test
	public void shouldGetPollRequest() throws Exception {
		HttpServletRequest request = mock(HttpServletRequest.class);
		given(request.getHeader("x-cloudfoundry-timeout-protection-poll")).willReturn(UID);
		TimeoutProtectionHttpRequest protectionRequest = TimeoutProtectionHttpRequest.get(request);
		assertNotNull(protectionRequest);
		assertThat(protectionRequest.getServletRequest(), is(request));
		assertThat(protectionRequest.getType(), is(TimeoutProtectionHttpRequest.Type.POLL));
		assertThat(protectionRequest.getUid(), is(UID));
	}

	@Test
	public void shouldNotGetIfNoHeader() throws Exception {
		HttpServletRequest request = mock(HttpServletRequest.class);
		TimeoutProtectionHttpRequest protectionRequest = TimeoutProtectionHttpRequest.get(request);
		assertNull(protectionRequest);
	}

	@Test
	public void shouldNotGetIfEmptyHeader() throws Exception {
		HttpServletRequest request = mock(HttpServletRequest.class);
		given(request.getHeader("x-cloudfoundry-timeout-protection-poll")).willReturn("");
		TimeoutProtectionHttpRequest protectionRequest = TimeoutProtectionHttpRequest.get(request);
		assertNull(protectionRequest);
	}

}
