/*
 * Copyright 2015-2020 Peter Michael Haller and contributors
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
package de.hallerweb.enterprise.prioritize.view.resource;

import de.hallerweb.enterprise.prioritize.model.resource.Resource;

public class ResourceTreeInfo {

	private boolean leaf;
	private boolean resourceGroupNode;
	private String name;
	private Resource resource;
	private String resourceGroupId;

	public boolean isResourceGroupNode() {
		return resourceGroupNode;
	}

	public void setResourceGroupNode(boolean resourceGroupNode) {
		this.resourceGroupNode = resourceGroupNode;
	}

	public String getResourceGroupId() {
		return resourceGroupId;
	}

	public String getName() {
		return this.name;
	}

	public Resource getResource() {
		return this.resource;
	}

	public boolean isLeaf() {
		return this.leaf;
	}

	public ResourceTreeInfo(String name, boolean isLeaf, boolean isResourceGroupNode, String resourceGroupId, Resource resource) {
		this.leaf = isLeaf;
		this.name = name;
		this.resource = resource;
		this.resourceGroupId = resourceGroupId;
		this.resourceGroupNode = isResourceGroupNode;
	}

}