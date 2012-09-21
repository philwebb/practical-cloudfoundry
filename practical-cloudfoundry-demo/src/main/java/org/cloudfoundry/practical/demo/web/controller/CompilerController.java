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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
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
import org.cloudfoundry.tools.io.File;
import org.cloudfoundry.tools.io.FilterOn;
import org.cloudfoundry.tools.io.Folder;
import org.cloudfoundry.tools.io.ResourceURL;
import org.cloudfoundry.tools.io.Resources;
import org.cloudfoundry.tools.io.compiler.ResourceJavaFileManager;
import org.cloudfoundry.tools.io.local.LocalFile;
import org.cloudfoundry.tools.io.local.LocalFolder;
import org.cloudfoundry.tools.io.virtual.VirtualFolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * MVC Controller to demonstrate compiling.
 * 
 * @author Phillip Webb
 */
@Controller
@RequestMapping("/compiler")
public class CompilerController {

	private static ThreadOverrideOutputStream outOverride;
	private static ThreadOverrideOutputStream errOverride;
	static {
		// Replace System.out and System.err with a version we can override on a per-thread basis
		outOverride = new ThreadOverrideOutputStream(System.out);
		System.setOut(new PrintStream(outOverride));
		errOverride = new ThreadOverrideOutputStream(System.err);
		System.setErr(new PrintStream(errOverride));
	}

	private static final List<String> COMPILER_OPTIONS = Collections.unmodifiableList(Arrays
			.asList("-encoding", "utf8"));

	private Folder folder;

	private JavaCompiler compiler;

	@RequestMapping(method = RequestMethod.GET)
	public void compiler() {
	}

	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	public String compile(@RequestParam String input) throws Exception {
		StringWriter out = new StringWriter();
		Folders folders = new Folders(this.folder.getFolder("lib"));
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
		fileManager.setLocation(StandardLocation.CLASS_PATH, folders.getLibJars());
		Iterable<? extends JavaFileObject> units = fileManager.list(StandardLocation.SOURCE_PATH, "",
				EnumSet.of(Kind.SOURCE), true);
		CompilationTask task = this.compiler.getTask(out, fileManager, null, COMPILER_OPTIONS, null, units);
		return task.call();
	}

	private void run(final Folders folders, final Writer out) throws Exception {
		OutputStream outputStream = new WriterOutputStream(out);
		outOverride.setOutputStream(outputStream);
		errOverride.setOutputStream(outputStream);
		try {
			runWithRedirectedOutput(folders);
		} finally {
			outOverride.setOutputStream(null);
			errOverride.setOutputStream(null);
		}

	}

	private void runWithRedirectedOutput(final Folders folders) throws MalformedURLException, ClassNotFoundException,
			NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		LocalFolder temporaryLibFolder = LocalFolder.createTempFolder("pcf");
		try {
			URLClassLoader classLoader = createClassLoader(folders, temporaryLibFolder);
			invokeMain(classLoader);
		} finally {
			temporaryLibFolder.delete();
		}
	}

	private URLClassLoader createClassLoader(final Folders folders, LocalFolder temporaryLibFolder)
			throws MalformedURLException {
		List<URL> urls = new ArrayList<URL>();
		urls.add(ResourceURL.get(folders.getOutput(), true));
		folders.getLibJars().copyTo(temporaryLibFolder);
		for (File file : temporaryLibFolder.list().files().asList()) {
			urls.add(((LocalFile) file).getLocalFile().toURI().toURL());
		}
		return URLClassLoader.newInstance(urls.toArray(new URL[urls.size()]));
	}

	private void invokeMain(URLClassLoader classLoader) throws ClassNotFoundException, NoSuchMethodException,
			IllegalAccessException, InvocationTargetException {
		Class<?> main = Class.forName("Main", true, classLoader);
		Method method = main.getDeclaredMethod("main", String[].class);
		method.invoke(null, new Object[] { new String[] {} });
	}

	@Autowired
	public void setFolder(Folder folder) {
		this.folder = folder;
	}

	@Autowired
	public void setCompiler(JavaCompiler compiler) {
		this.compiler = compiler;
	}

	/**
	 * Folders used during compile and run.
	 */
	private static class Folders {

		private Folder source = new VirtualFolder();
		private Folder output = new VirtualFolder();
		private Folder lib;

		public Folders(Folder lib) {
			this.lib = lib;
		}

		public Folder getSource() {
			return this.source;
		}

		public Folder getOutput() {
			return this.output;
		}

		public Resources<File> getLibJars() {
			return this.lib.list().include(FilterOn.names().ending(".jar")).files();
		}
	}

	/**
	 * Output stream that can optionally redirect to a thread local copy. Used to provide {@link System#out} and
	 * {@link System#err} capture.
	 */
	private static class ThreadOverrideOutputStream extends OutputStream {

		private static ThreadLocal<OutputStream> threadOutputStream = new ThreadLocal<OutputStream>();

		private OutputStream defaultOutputStream;

		public ThreadOverrideOutputStream(OutputStream defaultOutputStream) {
			this.defaultOutputStream = defaultOutputStream;
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
			return outputStream == null ? this.defaultOutputStream : outputStream;
		}

		public void setOutputStream(OutputStream outputStream) throws IOException {
			getOutputStream().flush();
			threadOutputStream.set(outputStream);
		}
	}
}
