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

	@Version
	private int entityVersion; // For optimistic locks

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
