package de.hallerweb.enterprise.prioritize.model.search;

public abstract class SearchResultType {
	private SearchResultType() {
		super();
	}
	
	public static final String DOCUMENT = "Document";
	public static final String DOCUMENTINFO = "DocumentInfo";
	public static final String RESOURCE = "Resource";
	public static final String USER = "User";
	public static final String ROLE = "Role";
	public static final String DEPARTMENT = "Department";
	public static final String SKILL = "Skill";
}
