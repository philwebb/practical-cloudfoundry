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
package org.cloudfoundry.practical.demo.web.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardLocation;

import org.apache.commons.io.output.WriterOutputStream;
import org.cloudfoundry.tools.compiler.CloudFoundryJavaCompiler;
import org.cloudfoundry.tools.io.Folder;
import org.cloudfoundry.tools.io.ResourceURL;
import org.cloudfoundry.tools.io.compiler.ResourceJavaFileManager;
import org.cloudfoundry.tools.io.virtual.VirtualFolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Phillip Webb
 */
@Controller
@RequestMapping("/compiler")
public class CompilerController {

	private static ThreadLocalCopyOutputStream threadOut;
	static {
		threadOut = new ThreadLocalCopyOutputStream(System.out);
		System.setOut(new PrintStream(threadOut));
	}

	private static final List<String> COMPILER_OPTIONS = Collections.unmodifiableList(Arrays
			.asList("-encoding", "utf8"));

	private Folder folder;

	// FIXME inject?
	private JavaCompiler compiler = new CloudFoundryJavaCompiler();

	@RequestMapping(method = RequestMethod.GET)
	public void compiler() {
	}

	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	public String compile(@RequestParam String input) throws Exception {
		StringWriter out = new StringWriter();
		Folders folders = new Folders();
		folders.getSource().getFile("Main.java").getContent().write(input);
		if (compile(folders, out)) {
			run(folders, out);
		}
		return out.toString();
	}

	private boolean compile(Folders folders, Writer out) throws IOException {
		JavaFileManager standardFileManager = this.compiler.getStandardFileManager(null, null, null);
		ResourceJavaFileManager fileManager = new ResourceJavaFileManager(standardFileManager);
		fileManager.setLocation(StandardLocation.SOURCE_PATH, folders.getSource());
		fileManager.setLocation(StandardLocation.CLASS_OUTPUT, folders.getOutput());
		Iterable<? extends JavaFileObject> units = fileManager.list(StandardLocation.SOURCE_PATH, "",
				EnumSet.of(Kind.SOURCE), true);
		CompilationTask task = this.compiler.getTask(null, fileManager, null, COMPILER_OPTIONS, null, units);
		return task.call();
	}

	private void run(Folders folders, Writer out) throws Exception {
		threadOut.setThreadOutputStream(new WriterOutputStream(out));
		try {
			URL url = ResourceURL.get(folders.getOutput(), true);
			URLClassLoader classLoader = URLClassLoader.newInstance(new URL[] { url });
			Class<?> main = Class.forName("Main", true, classLoader);
			Method method = main.getDeclaredMethod("main", String[].class);
			method.invoke(null, new Object[] { new String[] {} });
		} finally {
			threadOut.setThreadOutputStream(null);
		}
		System.out.println("done");
	}

	@Autowired
	public void setFolder(Folder folder) {
		this.folder = folder;
	}

	private static class Folders {

		private Folder source = new VirtualFolder();
		private Folder output = new VirtualFolder();

		public Folder getSource() {
			return this.source;
		}

		/**
		 * @return the output
		 */
		public Folder getOutput() {
			return this.output;
		}
	}

	private static class ThreadLocalCopyOutputStream extends OutputStream {

		private static ThreadLocal<OutputStream> threadOutputStream = new ThreadLocal<OutputStream>();

		private OutputStream parent;

		public ThreadLocalCopyOutputStream(OutputStream parent) {
			this.parent = parent;
		}

		@Override
		public void write(int b) throws IOException {
			getOutputStream().write(b);
		}

		@Override
		public void write(byte[] b) throws IOException {
			getOutputStream().write(b);
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			getOutputStream().write(b, off, len);
		}

		@Override
		public void flush() throws IOException {
			getOutputStream().flush();
		}

		private OutputStream getOutputStream() {
			OutputStream outputStream = threadOutputStream.get();
			return outputStream == null ? this.parent : outputStream;
		}

		public void setThreadOutputStream(OutputStream outputStream) throws IOException {
			getOutputStream().flush();
			threadOutputStream.set(outputStream);
		}
	}

}
