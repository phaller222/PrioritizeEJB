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

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;

/**
 * JPA entity to represent a {@link SkillRecordProperty}. A SkillRecordProperty is a concrete manifestation of a SkillProperty for a User.
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
// @NamedQueries(
// @NamedQuery(name="findSkillPropertiesForSkill", query = "select prop FROM SkillPropertyNumeric prop WHERE prop.skill.id = :skillId"))
public class SkillRecordProperty {

	@Id
	@GeneratedValue
	int id;

	@OneToOne
	SkillProperty property;

	int propertyValueNumeric;
	String propertyValueString;

	public SkillRecordProperty() {
		super();
	}

	public int getId() {
		return id;
	}

	public SkillProperty getProperty() {
		return property;
	}

	public void setProperty(SkillProperty property) {
		this.property = property;
	}

	public int getPropertyValueNumeric() {
		return propertyValueNumeric;
	}

	public void setPropertyValueNumeric(int propertyValue) {
		this.propertyValueNumeric = propertyValue;
	}

	public String getPropertyValueString() {
		return propertyValueString;
	}

	public void setPropertyValueString(String propertyValueString) {
		this.propertyValueString = propertyValueString;
	}

}
