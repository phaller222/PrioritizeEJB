package de.hallerweb.enterprise.prioritize.model.skill;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

/**
 * JPA entity to represent a textual {@link SkillProperty}.
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
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class SkillPropertyText extends SkillProperty implements SkillType {

	@Column(length = 3000)
	private String text;
	private String type;

	public void setType(String type) {
		this.type = type;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	@Override
	public String toString() {

		return name;
	}

	public String getType() {
		return "TEXT";
	}
}
