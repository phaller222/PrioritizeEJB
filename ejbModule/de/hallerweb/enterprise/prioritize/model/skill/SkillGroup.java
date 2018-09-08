package de.hallerweb.enterprise.prioritize.model.skill;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;

/**
 * SkillGroup - A collection of SkillRecords indicating required skills
 * @author peter
 *
 */
@Entity
public class SkillGroup {

	@Id
	@GeneratedValue
	int id;
	
	@OneToOne
	Skill skill;
	int amount;
	
}
