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
		case "SKILL":
			this.type = SearchPropertyType.SKILL;
			break;
		case "VERSION":
			this.type = SearchPropertyType.VERSION;
			break;
		case "DESCRIPTION":
			this.type = SearchPropertyType.DESCRIPTION;
			break;
			case "NAME":
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
