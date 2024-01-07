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
package de.hallerweb.enterprise.prioritize.model.project.goal;

import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;

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
public class ProjectGoalPropertyNumeric extends ProjectGoalProperty {

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
