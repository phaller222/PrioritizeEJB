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

public  final class SearchResultType {
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
