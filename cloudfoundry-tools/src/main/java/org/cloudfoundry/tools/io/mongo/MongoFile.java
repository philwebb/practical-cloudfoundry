package org.cloudfoundry.tools.io.mongo;

import org.cloudfoundry.tools.io.File;
import org.cloudfoundry.tools.io.mongo.MongoResourceStore.MongoFileStore;
import org.cloudfoundry.tools.io.store.FileStore;
import org.cloudfoundry.tools.io.store.StoredFile;

import com.mongodb.gridfs.GridFS;

/**
 * A {@link File} implementation backed by a mongo {@link GridFS}.
 * 
 * @see MongoFolder
 * 
 * @author Phillip Webb
 */
public class MongoFile extends StoredFile {

	private final MongoFileStore store;

	/**
	 * Package scope constructor, files should only be accessed via the {@link MongoFolder},
	 * 
	 * @param store the file store
	 */
	MongoFile(MongoFileStore store) {
		this.store = store;
	}

	@Override
	protected FileStore getStore() {
		return this.store;
	}

}
