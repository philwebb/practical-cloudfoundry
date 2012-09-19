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
package org.cloudfoundry.practical.demo;

import org.cloudfoundry.practical.demo.cloud.CloudConfiguration;
import org.cloudfoundry.practical.demo.core.CoreConfiguration;
import org.cloudfoundry.practical.demo.local.LocalConfiguration;
import org.cloudfoundry.tools.timeout.ReplayingTimeoutProtectionStrategy;
import org.cloudfoundry.tools.timeout.TimeoutProtectionFilter;
import org.cloudfoundry.tools.timeout.TimeoutProtectionStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;

/**
 * Root spring configuration.
 * 
 * @author Phillip Webb
 */
@Configuration
@Import({ CoreConfiguration.class, LocalConfiguration.class, CloudConfiguration.class })
@ImportResource("classpath:spring/security.xml")
public class RootConfiguration {

	@Bean
	public TimeoutProtectionFilter timeoutProtectionFilter() {
		TimeoutProtectionFilter filter = new TimeoutProtectionFilter();
		filter.setProtector(timeoutProtectionStrategy());
		return filter;
	}

	@Bean
	public TimeoutProtectionStrategy timeoutProtectionStrategy() {
		ReplayingTimeoutProtectionStrategy strategy = new ReplayingTimeoutProtectionStrategy();
		return strategy;
	}

}
