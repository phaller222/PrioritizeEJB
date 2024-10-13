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

import java.util.List;

/**
 * Each implementing class must guarantee to use a meaningful method in it's find(..) methods to retrieve a {@link SearchResult}. Example: A
 * Document could search the name, description and optionally the content itself (if readable).
 *
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
