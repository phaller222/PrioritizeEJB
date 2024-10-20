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

package de.hallerweb.enterprise.prioritize.model.resource;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

/**
 * JPA entity to represent a {@link NameValueEntry} pair. This entity is used to represent data variables sent from MQTT IoT devices (e.g.
 * SET:value:100)
 *
 * <p>
 * Copyright: (c) 2015
 * </p>
 * <p>
 * Peter Haller
 * </p>
 *
 * @author peter
 */
@Entity
public class NameValueEntry implements Comparable<Object> {

    @Id
    @GeneratedValue
    int id;

    private String mqttValues; // comma separated mqttValues (if historic data).
    private String mqttName; // Name of the mqttName/value pair.

    public int getId() {
        return id;
    }

    public String getName() {
        return mqttName;
    }

    public void setName(String name) {
        this.mqttName = name;
    }

    public String getValues() {
        return mqttValues;
    }

    public void setValues(String values) {
        this.mqttValues = values;
    }

    @Override
    public int compareTo(Object obj) {
        NameValueEntry e = (NameValueEntry) obj;
        return mqttName.compareTo(e.getName());
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return this.mqttName;
    }

}
