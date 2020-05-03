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

	PAuthorizedObject result;
	String resultType;
	String excerpt;
	boolean providesExcerpt;

	Set<SearchResult> subresults;

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


	@Override
	public int compareTo(Object obj) {
		SearchResult sr = (SearchResult) obj;
		return this.getExcerpt().compareTo(sr.getExcerpt());
	}

}
