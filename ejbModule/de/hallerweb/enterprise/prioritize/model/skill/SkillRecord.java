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

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Version;

import de.hallerweb.enterprise.prioritize.model.resource.Resource;
import de.hallerweb.enterprise.prioritize.model.security.User;

/**
 * JPA entity to represent a {@link SkillRecord}. A SkillRecord is a concrete manifestation of a Record for a User. For example their might
 * be only one Skill "X", but many SkillRecords of type "X" for different users. Demo-Scenario: A {@link Skill}: "Java" with a
 * {@link SkillPropertyNumeric} named "Level" ranging from 1 to 10. User 1: {@link SkillRecord} of Type "Java" with Level=3 (
 * {@link SkillRecordProperty} ) User 2: {@link SkillRecord} of Type "Java" with Level=10 ( {@link SkillRecordProperty} )
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
public class SkillRecord {

	@Id
	@GeneratedValue
	int id;

	@OneToOne
	Skill skill;

	@OneToOne
	User user;

	@OneToOne
	Resource resource;

	@OneToMany(fetch = FetchType.EAGER)
	Set<SkillRecordProperty> skillProperties;

	int enthusiasm; // How much does the User "love" to perform tasks needing that skill?

	public SkillRecord() {
		super();
		this.skillProperties = new HashSet<>();
	}

	public int getId() {
		return id;
	}

	public int getEnthusiasm() {
		return enthusiasm;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public User getUser() {
		return this.user;
	}

	public void setResource(Resource res) {
		this.resource = res;
	}

	public Resource getResource() {
		return this.resource;
	}

	public void setEnthusiasm(int enthusiasm) {
		if (enthusiasm >= 0 && enthusiasm <= 10) {
			this.enthusiasm = enthusiasm;
		}
	}

	public Skill getSkill() {
		return skill;
	}

	public void setSkill(Skill skill) {
		this.skill = skill;
	}

	public Set<SkillRecordProperty> getSkillProperties() {
		return skillProperties;
	}

	public void setSkillProperties(Set<SkillRecordProperty> skillProperties) {
		this.skillProperties = skillProperties;
	}

}
