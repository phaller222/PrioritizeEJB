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
package de.hallerweb.enterprise.prioritize.model.skill;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Version;

import de.hallerweb.enterprise.prioritize.model.Department;
import de.hallerweb.enterprise.prioritize.model.search.PSearchable;
import de.hallerweb.enterprise.prioritize.model.search.SearchProperty;
import de.hallerweb.enterprise.prioritize.model.search.SearchResult;
import de.hallerweb.enterprise.prioritize.model.search.SearchResultType;
import de.hallerweb.enterprise.prioritize.model.security.PAuthorizedObject;

/**
 * JPA entity to represent a Skill. Skills are assigned to users and can be used to asses possible candidates for an upcoming project.
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
@Entity
@NamedQueries({ @NamedQuery(name = "findSkillRecordsForSkill", query = "select sr FROM SkillRecord sr WHERE sr.skill.id = :skillId"),
		@NamedQuery(name = "findAllSkills", query = "select sk FROM Skill sk"),
		@NamedQuery(name = "findSkillPropertiesForSkill", query = "select prop FROM SkillProperty prop WHERE prop.skill.id = :skillId") })
public class Skill implements PAuthorizedObject, PSearchable {

	@Id
	@GeneratedValue
	int id;

	String name;

	@Column(length = 3000)
	String description;

	@OneToOne
	SkillCategory category;

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "skill", cascade = CascadeType.ALL)
	Set<SkillProperty> skillProperties;

	String keywords;

	@Version
	private int entityVersion; // For optimistic locks

	public Skill() {
		super();
		this.skillProperties = new HashSet<>();
	}

	public SkillCategory getCategory() {
		return category;
	}

	public void setCategory(SkillCategory category) {
		this.category = category;
	}

	public Set<SkillProperty> getSkillProperties() {
		return skillProperties;
	}

	public void setSkillProperties(Set<SkillProperty> skillProperties) {
		this.skillProperties = skillProperties;
	}

	public void addSkillProperty(SkillProperty property) {
		this.skillProperties.add(property);
	}

	public String getKeywords() {
		return keywords;
	}

	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return this.getName();
	}

	transient List<SearchProperty> searchProperties;

	

	private SearchResult generateResult() {
		SearchResult result = new SearchResult();
		result.setResult(this);
		result.setResultType(SearchResultType.SKILL);
		result.setExcerpt(name + " : " + this.getDescription());
		result.setProvidesExcerpt(true);
		result.setSubresults(new HashSet<SearchResult>());
		return result;
	}

	@Override
	public List<SearchResult> find(String phrase) {
		ArrayList<SearchResult> results = new ArrayList<>();
		// Search skill name
		if (name.toLowerCase().indexOf(phrase.toLowerCase()) != -1) {
			// Match found
			SearchResult result = generateResult();
			results.add(result);
			return results;
		}
		if (this.description.toLowerCase().indexOf(phrase.toLowerCase()) != -1) {
			// Match found
			SearchResult result = generateResult();
			results.add(result);
		}
		if (this.keywords.contains(phrase.toLowerCase())) {
			// Match found
			SearchResult result = generateResult();
			results.add(result);
		}
		return results;
	}
	
	@Override
	public List<SearchResult> find(String phrase, SearchProperty property) {
		return new ArrayList<>();
	}

	@Override
	public List<SearchProperty> getSearchProperties() {
		if (this.searchProperties == null) {
			searchProperties = new ArrayList<>();
			SearchProperty prop = new SearchProperty("SKILL");
			prop.setName("Skill");
			searchProperties.add(prop);
		}
		return this.searchProperties;
	}

	@Override
	public Department getDepartment() {
		// Just to fulfill PAuthorized Object interface.
		return null;
	}

}
