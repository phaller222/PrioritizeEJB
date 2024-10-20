/*
 * Copyright 2015-2024 Peter Michael Haller and contributors
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

package de.hallerweb.enterprise.prioritize.model.nfc.counter;

import de.hallerweb.enterprise.prioritize.model.Department;
import de.hallerweb.enterprise.prioritize.model.nfc.PCounter;
import de.hallerweb.enterprise.prioritize.model.security.PAuthorizedObject;
import jakarta.persistence.*;

@Entity
@NamedQuery(name = "findAllIndustrieCounters", query = "select ic FROM IndustrieCounter ic")
public class IndustrieCounter implements PAuthorizedObject {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    int id;

    @OneToOne
    PCounter counter;

    String name;
    String description;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public PCounter getCounter() {
        return counter;
    }

    public void setCounter(PCounter counter) {
        this.counter = counter;
    }

    public int getId() {
        return id;
    }

    public void incCounter() {
        counter.incCounter();
    }

    public void decCounter() {
        counter.decCounter();
    }

    @Override
    public Department getDepartment() {
        return null;
    }

}
