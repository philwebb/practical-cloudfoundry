package org.cloudfoundry.tools.compiler;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;

import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardJavaFileManager;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.eclipse.jdt.internal.compiler.tool.EclipseFileManager;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Matchers;

/**
 * Tests for {@link CloudFoundryJavaCompiler}.
 * 
 * @author Phillip Webb
 */
public class CloudFoundryJavaCompilerTest {

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	private final CloudFoundryJavaCompiler javaCompiler = new CloudFoundryJavaCompiler();

	@Test
	public void shouldGetStandardFileManager() throws Exception {
		StandardJavaFileManager fileManager = this.javaCompiler.getStandardFileManager(null, null, null);
		assertThat(fileManager, is(notNullValue()));
		assertThat(fileManager, is(EclipseFileManager.class));
	}

	@Test
	public void shouldCompilePhysicalFile() throws Exception {
		StandardJavaFileManager fileManager = this.javaCompiler.getStandardFileManager(null, null, null);
		try {
			Iterable<? extends JavaFileObject> compilationUnits = fileManager
					.getJavaFileObjects(new File[] { createExampleJavaFile() });
			CompilationTask task = this.javaCompiler.getTask(null, fileManager, null, standardCompilerOptions(), null,
					compilationUnits);
			assertThat(task.call(), is(Boolean.TRUE));
			assertTrue(new File(this.tempFolder.getRoot(), "Example.class").exists());
		} finally {
			fileManager.close();
		}
	}

	@Test
	public void shouldCompileVirtualFile() throws Exception {
		JavaFileObject sourceFile = mock(JavaFileObject.class);
		given(sourceFile.getKind()).willReturn(Kind.SOURCE);
		given(sourceFile.getName()).willReturn("Example.java");
		given(sourceFile.getCharContent(anyBoolean())).willReturn(getExampleJavaContent());
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		JavaFileObject classFile = mock(JavaFileObject.class);
		given(classFile.openOutputStream()).willReturn(outputStream);

		JavaFileManager fileManager = mock(JavaFileManager.class);
		given(
				fileManager.getJavaFileForOutput(Matchers.any(Location.class), anyString(), eq(Kind.CLASS),
						eq(sourceFile))).willReturn(classFile);
		Iterable<? extends JavaFileObject> compilationUnits = Collections.singleton(sourceFile);
		CompilationTask task = this.javaCompiler.getTask(null, fileManager, null, standardCompilerOptions(), null,
				compilationUnits);
		assertThat(task.call(), is(Boolean.TRUE));
		assertThat(outputStream.toByteArray().length, is(greaterThan(0)));
	}

	private File createExampleJavaFile() throws Exception {
		File file = this.tempFolder.newFile("Example.java");
		FileOutputStream fileOutputStream = new FileOutputStream(file);
		try {
			IOUtils.copy(getExampleJavaContentStream(), fileOutputStream);
			return file;
		} finally {
			fileOutputStream.close();
		}
	}

	private InputStream getExampleJavaContentStream() {
		return new ByteArrayInputStream(getExampleJavaContent().getBytes());
	}

	private String getExampleJavaContent() {
		return "public class Example {}";
	}

	private Iterable<String> standardCompilerOptions() {
		return Arrays.asList("-encoding", "utf8");
	}
}
