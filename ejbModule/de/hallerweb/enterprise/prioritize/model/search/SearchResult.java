package de.hallerweb.enterprise.prioritize.model.search;

import java.util.Set;

import de.hallerweb.enterprise.prioritize.model.security.PAuthorizedObject;

/**
 * JPA entity to represent a {@link SearchResult}. A SearchResult holds information about the outcome of an executed search.
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

public class SearchResult implements Comparable {

	public PAuthorizedObject getResult() {
		return result;
	}

	public void setResult(PAuthorizedObject result) {
		this.result = result;
	}

	public String getResultType() {
		return resultType;
	}

	public void setResultType(String resultType) {
		this.resultType = resultType;
	}

	public String getExcerpt() {
		return excerpt;
	}

	public void setExcerpt(String excerpt) {
		this.excerpt = excerpt;
	}

	public boolean isProvidesExcerpt() {
		return providesExcerpt;
	}

	public void setProvidesExcerpt(boolean providesExcerpt) {
		this.providesExcerpt = providesExcerpt;
	}

	public Set<SearchResult> getSubresults() {
		return subresults;
	}

	public void setSubresults(Set<SearchResult> subresults) {
		this.subresults = subresults;
	}

	PAuthorizedObject result;
	String resultType;
	String excerpt;
	boolean providesExcerpt;

	Set<SearchResult> subresults;

	@Override
	public int compareTo(Object obj) {
		SearchResult sr = (SearchResult) obj;
		return this.getExcerpt().compareTo(sr.getExcerpt());
	}

}
