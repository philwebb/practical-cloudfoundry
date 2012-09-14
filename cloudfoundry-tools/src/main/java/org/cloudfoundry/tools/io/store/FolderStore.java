
package org.cloudfoundry.tools.io.store;

import org.cloudfoundry.tools.io.Folder;

/**
 * Store for a {@link Folder}.
 * 
 * @see StoredFolder
 * @author Phillip Webb
 */
public interface FolderStore extends ResourceStore {

    /**
     * List the contents of the folder.
     * 
     * @return the folder contents
     */
    Iterable<String> list();
}