import java.io.InputStreamReader;
import java.net.URL;

import org.junit.Test;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.util.FileCopyUtils;

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

/**
 * @author pwebb
 * 
 */
public class TempTest {

	@Test
	public void test() throws Exception {
		final URL jarUrl = new URL("jar:file:///Users/pwebb/Downloads/example.zip!/");
		UrlResource resource = new UrlResource(jarUrl);
		Resource createRelative = resource.createRelative("example/test.txt");
		String copyToString = FileCopyUtils.copyToString(new InputStreamReader(createRelative.getInputStream()));
		System.out.println(copyToString);

	}

}
