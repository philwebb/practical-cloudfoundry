/*
 *  Copyright (C) 2012 VMware, Inc. All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.wavemaker.tools.io;

/**
 * An operation that can be performed on a {@link Resource} or {@link Resources}.
 * 
 * @see Resources#performOperation(ResourceOperation)
 * @see Resource#performOperation(ResourceOperation)
 * @param <T> The resource type that the filter works with
 * @author Phillip Webb
 */
public interface ResourceOperation<T extends Resource> {

	/**
	 * Perform the given operation on the resource.
	 * 
	 * @param resource the resource
	 */
	void perform(T resource);
}