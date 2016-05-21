package de.hallerweb.enterprise.prioritize.view.resource;

import de.hallerweb.enterprise.prioritize.model.document.DocumentInfo;
import de.hallerweb.enterprise.prioritize.model.resource.Resource;

public class ResourceTreeInfo {

	private boolean leaf;
	private String name;
	private Resource resource;

	public String getName() {
		return this.name;
	}

	public Resource getResource() {
		return this.resource;
	}

	public boolean isLeaf() {
		return this.leaf;
	}

	public ResourceTreeInfo(String name, boolean isLeaf, Resource resource) {
		this.leaf = isLeaf;
		this.name = name;
		this.resource = resource;
	}

}