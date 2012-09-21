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
package org.cloudfoundry.practical.demo.web.webdav;

import java.io.InputStream;
import java.security.Principal;
import java.util.Date;
import java.util.List;

import net.sf.webdav.ITransaction;
import net.sf.webdav.IWebdavStore;
import net.sf.webdav.StoredObject;

import org.cloudfoundry.tools.io.File;
import org.cloudfoundry.tools.io.Folder;
import org.cloudfoundry.tools.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * {@link IWebdavStore} implementation backed by a {@link Folder}.
 * 
 * @author Phillip Webb
 */
public class FolderWebdavStore implements IWebdavStore {

	private static final Date NO_DATE = new Date(0);

	private Folder root;

	public FolderWebdavStore(Folder root) {
		Assert.notNull(root, "Root must not be null");
		this.root = root.jail();
	}

	@Override
	public ITransaction begin(Principal principal) {
		this.root.createIfMissing();
		return null;
	}

	@Override
	public void checkAuthentication(ITransaction transaction) {
	}

	@Override
	public void commit(ITransaction transaction) {
	}

	@Override
	public void rollback(ITransaction transaction) {
	}

	@Override
	public void createFolder(ITransaction transaction, String folderUri) {
		this.root.getFolder(folderUri).createIfMissing();
	}

	@Override
	public void createResource(ITransaction transaction, String resourceUri) {
		this.root.getFile(resourceUri).createIfMissing();
	}

	@Override
	public InputStream getResourceContent(ITransaction transaction, String resourceUri) {
		return this.root.getFile(resourceUri).getContent().asInputStream();
	}

	@Override
	public long setResourceContent(ITransaction transaction, String resourceUri, InputStream content,
			String contentType, String characterEncoding) {
		File file = this.root.getFile(resourceUri);
		file.getContent().write(content);
		return file.getSize();
	}

	@Override
	public String[] getChildrenNames(ITransaction transaction, String folderUri) {
		List<Resource> childResources = this.root.getFolder(folderUri).list().asList();
		String[] children = new String[childResources.size()];
		for (int i = 0; i < children.length; i++) {
			children[i] = childResources.get(i).getName();
		}
		return children;
	}

	@Override
	public long getResourceLength(ITransaction transaction, String path) {
		Resource resource = this.root.getExisting(path);
		if (resource instanceof File) {
			return ((File) resource).getSize();
		}
		return 0;
	}

	@Override
	public void removeObject(ITransaction transaction, String uri) {
		this.root.getExisting(uri).delete();
	}

	@Override
	public StoredObject getStoredObject(ITransaction transaction, String uri) {
		Resource resource = getStoredObjectResource(uri);
		if (resource == null) {
			return null;
		}
		StoredObject storedObject = new StoredObject();
		storedObject.setFolder(resource instanceof Folder);
		storedObject.setCreationDate(NO_DATE);
		storedObject.setLastModified(NO_DATE);
		if (resource instanceof File) {
			File file = (File) resource;
			storedObject.setLastModified(new Date());
			storedObject.setCreationDate(new Date(file.getLastModified()));
			storedObject.setResourceLength(file.getSize());
		}
		return storedObject;
	}

	private Resource getStoredObjectResource(String uri) {
		if (!StringUtils.hasLength(uri) || uri.equals("/")) {
			return this.root;
		}
		if (this.root.hasExisting(uri)) {
			return this.root.getExisting(uri);
		}
		return null;
	}
}
