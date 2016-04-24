package de.hallerweb.enterprise.prioritize.model.search;

import java.util.List;

/**
 * Each implementing class must guarantee to use a meaningful method in it's find(..) methods to retrieve a {@link SearchResult}. Example: A
 * Document could search the name, description and optionally the content itself (if readable).
 * 
 * @author peter
 *
 */
public interface PSearchable {
	// Find the phrase and return a List of SearchResults. If nothing found return an empty list.
	// If just one result rreturn a List withhh 1 item only.

	public List<SearchResult> find(String phrase);

	// Find the phrase and return a List of SearchResults. Perform the search only on the items indicated by SearchProperty.
	public List<SearchResult> find(String phrase, SearchProperty property);

	// If the client wants to search using item specific properties, they can be returned here by the implemenation .
	public List<SearchProperty> getSearchProperties();

}
