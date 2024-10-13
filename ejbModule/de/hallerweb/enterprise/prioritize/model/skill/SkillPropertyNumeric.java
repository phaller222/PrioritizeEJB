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

import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;

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
 *
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
}
