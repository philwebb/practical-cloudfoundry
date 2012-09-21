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
package org.cloudfoundry.practical.demo.local;

import javax.tools.JavaCompiler;

import org.cloudfoundry.tools.compiler.CloudFoundryJavaCompiler;
import org.cloudfoundry.tools.io.Folder;
import org.cloudfoundry.tools.io.local.LocalFolder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Configuration when not running in the cloud.
 * 
 * @author Phillip Webb
 */
@Configuration
@Profile("default")
@ComponentScan
public class LocalConfiguration {

	@Bean
	public Folder folder() {
		Folder folder = LocalFolder.home().getFolder("practical-cloudfoundry").jail();
		folder.createIfMissing();
		return folder;
	}

	@Bean
	JavaCompiler javaCompiler() {
		return new CloudFoundryJavaCompiler();
		// FIXME return ToolProvider.getSystemJavaCompiler();
	}
}
