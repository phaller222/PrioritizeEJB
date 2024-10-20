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

package de.hallerweb.enterprise.prioritize.model.security;

import jakarta.persistence.*;

/**
 * Holds all canonical (absolute) packages names of objects which can be secured by Prioritize PermissionRecord entrys.
 * By adding a new object type (e.g. my.absolute.package.MyEntity) this entity will appear in the administration GUI as
 * an object which can be selected to define permissions on.
 *
 * @author peter
 */
@Entity
@NamedQuery(name = "findAllObjectTypes", query = "select ot FROM ObservedObjectType ot")
public class ObservedObjectType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    String objectType;

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return objectType.substring(objectType.lastIndexOf('.') + 1);
    }

}
