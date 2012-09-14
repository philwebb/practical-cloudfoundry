/*
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
package org.cloudfoundry.tools.io.compiler;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collections;

import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;

import org.cloudfoundry.tools.compiler.CloudFoundryJavaCompiler;
import org.cloudfoundry.tools.io.ClassPathFile;
import org.cloudfoundry.tools.io.virtual.VirtualFolder;
import org.junit.Test;

/**
 * @author Phillip Webb
 */
public class ResourceJavaFileManagerTest {

	@Test
	public void shouldCompile() throws Exception {
		ClassPathFile exampleJar = new ClassPathFile(getClass(), "example.jar");
		VirtualFolder classOutputFolder = new VirtualFolder();
		VirtualFolder sourceFolder = new VirtualFolder();

		sourceFolder.getFile("org/test/Example.java").getContent()
				.write("package org.test; import example.InsideJar; public class Example {}");

		CloudFoundryJavaCompiler compiler = new CloudFoundryJavaCompiler();
		StandardJavaFileManager standardFileManager = compiler.getStandardFileManager(null, null, null);
		ResourceJavaFileManager fileManager = new ResourceJavaFileManager(standardFileManager);
		fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Collections.singleton(classOutputFolder));
		fileManager.setLocation(StandardLocation.SOURCE_PATH, Collections.singleton(sourceFolder));
		fileManager.setLocation(StandardLocation.CLASS_PATH, Collections.singleton(exampleJar));

		Iterable<? extends JavaFileObject> compilationUnits = fileManager.list(StandardLocation.SOURCE_PATH, "",
				Collections.singleton(JavaFileObject.Kind.SOURCE), true);
		CompilationTask task = compiler.getTask(null, fileManager, null, standardCompilerOptions(), null,
				compilationUnits);
		assertThat(task.call(), is(true));
		assertThat(classOutputFolder.getFile("org/test/Example.class").exists(), is(true));
	}

	private Iterable<String> standardCompilerOptions() {
		return Arrays.asList("-encoding", "utf8");
	}

}
