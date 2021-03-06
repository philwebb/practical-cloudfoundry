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
package org.cloudfoundry.tools.io.local;

import org.cloudfoundry.tools.io.File;
import org.cloudfoundry.tools.io.local.LocalResourceStore.LocalFileStore;
import org.cloudfoundry.tools.io.store.FileStore;
import org.cloudfoundry.tools.io.store.StoredFile;

/**
 * A {@link File} implementation backed by standard {@link File java.io.File}s.
 * 
 * @author Phillip Webb
 * @see LocalFolder
 */
public class LocalFile extends StoredFile {

	private final LocalFileStore store;

	/**
	 * Package scope constructor, files should only be accessed via the {@link LocalFolder},
	 * 
	 * @param store the file store
	 */
	LocalFile(LocalFileStore store) {
		this.store = store;
	}

	@Override
	protected FileStore getStore() {
		return this.store;
	}

	/**
	 * Returns access to the underlying local {@link File}.
	 * 
	 * @return the underlying {@link File}
	 */
	public java.io.File getLocalFile() {
		return this.store.getFile();
	}
}
