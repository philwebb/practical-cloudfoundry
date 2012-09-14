package org.cloudfoundry.tools.io.local;

import org.cloudfoundry.tools.io.File;
import org.cloudfoundry.tools.io.local.LocalResourceStore.LocalFileStore;
import org.cloudfoundry.tools.io.store.FileStore;
import org.cloudfoundry.tools.io.store.StoredFile;

/**
 * A {@link File} implementation backed by standard {@link File java.io.File}s.
 * 
 * @see LocalFolder
 * 
 * @author Phillip Webb
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
