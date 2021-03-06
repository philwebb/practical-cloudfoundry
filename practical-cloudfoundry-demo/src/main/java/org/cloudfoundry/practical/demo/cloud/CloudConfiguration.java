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
package org.cloudfoundry.practical.demo.cloud;

import javax.tools.JavaCompiler;

import org.cloudfoundry.runtime.service.document.CloudMongoDbFactoryBean;
import org.cloudfoundry.tools.compiler.CloudFoundryJavaCompiler;
import org.cloudfoundry.tools.io.Folder;
import org.cloudfoundry.tools.io.mongo.MongoFolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.MongoDbFactory;

/**
 * Configuration when running in the cloud.
 * 
 * @author Phillip Webb
 */
@Configuration
@Profile("cloud")
@ComponentScan(basePackageClasses = CloudConfiguration.class)
public class CloudConfiguration {

	@Autowired
	private MongoDbFactory mongo;

	@Bean
	public CloudMongoDbFactoryBean mongo() {
		return new CloudMongoDbFactoryBean();
	}

	@Bean
	public Folder folder() {
		return new MongoFolder(this.mongo.getDb());
	}

	@Bean
	public JavaCompiler javaCompiler() {
		return new CloudFoundryJavaCompiler();
	}
}
