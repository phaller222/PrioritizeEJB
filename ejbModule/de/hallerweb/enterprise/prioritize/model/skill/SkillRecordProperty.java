package de.hallerweb.enterprise.prioritize.model.skill;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Version;

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

	@Version
	private int entityVersion; // For optimistic locks

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
