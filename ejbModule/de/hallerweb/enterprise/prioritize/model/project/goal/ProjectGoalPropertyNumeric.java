package de.hallerweb.enterprise.prioritize.model.project.goal;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

/**
 * JPA entity to represent a numeric {@link ProjectGoalProperty}.
 * 
 * <p>
 * Copyright: (c) 2016
 * </p>
 * <p>
 * Peter Haller
 * </p>
 * 
 * @author peter
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class ProjectGoalPropertyNumeric extends ProjectGoalProperty  {

	private double min; // Minimum value this property can hold (e.G. 0).
	private double max; // Maximum value this property can hold (e.G. 10).
	private double tempValue; // temp Value for e.G. setting up a SkillRecord.

	public double getTempValue() {
		return tempValue;
	}

	public void setTempValue(double tempValue) {
		this.tempValue = tempValue;
	}

	public double getMin() {
		return min;
	}

	public void setMin(double minValue) {
		this.min = minValue;
	}

	public double getMax() {
		return max;
	}

	public void setMax(double maxValue) {
		this.max = maxValue;
	}

	@Override
	public String toString() {
		return name;
	}
}
