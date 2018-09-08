package de.hallerweb.enterprise.prioritize.model.search;

/**
 * JPA entity to represent a {@link SearchProperty}. All Objects which are searchable can return such properties to indicate which kinds of
 * searches are possible on them (e.G. name, age...)
 * 
 * <p>
 * Copyright: (c) 2014
 * </p>
 * <p>
 * Peter Haller
 * </p>
 * 
 * @author peter
 */

public class SearchProperty {

	public enum SearchPropertyType {
		NAME, DESCRIPTION, SKILL, VERSION
	}

	String name;
	SearchPropertyType type;

	public SearchProperty(String type) {
		switch (type) {
		case "NAME":
			this.type = SearchPropertyType.NAME;
			break;
		case "SKILL":
			this.type = SearchPropertyType.SKILL;
			break;
		case "VERSION":
			this.type = SearchPropertyType.VERSION;
			break;
		case "DESCRIPTION":
			this.type = SearchPropertyType.DESCRIPTION;
			break;
		default:
			this.type = SearchPropertyType.NAME;
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public SearchPropertyType getType() {
		return type;
	}

	public void setType(SearchPropertyType type) {
		this.type = type;
	}
}
