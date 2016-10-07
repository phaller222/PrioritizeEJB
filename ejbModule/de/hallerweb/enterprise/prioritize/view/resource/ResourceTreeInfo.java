package de.hallerweb.enterprise.prioritize.view.resource;

import de.hallerweb.enterprise.prioritize.model.document.DocumentInfo;
import de.hallerweb.enterprise.prioritize.model.resource.Resource;

public class ResourceTreeInfo {

	private boolean leaf;
	private boolean resourceGroupNode;
	
	public boolean isResourceGroupNode() {
		return resourceGroupNode;
	}

	public void setResourceGroupNode(boolean resourceGroupNode) {
		this.resourceGroupNode = resourceGroupNode;
	}

	private String name;
	private Resource resource;
	private String resourceGroupId;

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