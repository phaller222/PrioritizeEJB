package de.hallerweb.enterprise.prioritize.model.skill;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

/**
 * JPA entity to represent a numeric {@link SkillProperty}.
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
public class SkillPropertyNumeric extends SkillProperty implements SkillType {

	private int minimum; // Minimum value this property can hold (e.G. 0).
	private int maximum; // Maximum value this property can hold (e.G. 10).
	private int tempValue; // temp Value for e.G. setting up a SkillRecord.

	public int getTempValue() {
		return tempValue;
	}

	public void setTempValue(int tempValue) {
		this.tempValue = tempValue;
	}

	private String type;

	public int getMinValue() {
		return minimum;
	}

	public void setMinValue(int minValue) {
		this.minimum = minValue;
	}

	public int getMaxValue() {
		return maximum;
	}

	public void setMaxValue(int maxValue) {
		this.maximum = maxValue;
	}

	@Override
	public String toString() {
		return name;
	}

	public String getType() {
		return "NUMERIC";
	}

	public void setType(String type) {
		this.type = type;
	}

}
