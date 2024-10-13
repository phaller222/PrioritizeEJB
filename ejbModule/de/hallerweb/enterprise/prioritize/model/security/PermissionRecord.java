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

package de.hallerweb.enterprise.prioritize.model.security;

import de.hallerweb.enterprise.prioritize.model.Department;
import jakarta.persistence.*;

/**
 * {@link PermissionRecord} - Holds information about a specific access rule based on CRUD (CREATE/READ/UPDATE/DELETE) to specific objects
 * for a specific role. Objects protected by a PermissionRecord implement the interface {@link PAuthorizedObject}.
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
@NamedQueries(@NamedQuery(name = "findPermissionRecordsByDepartment", query = "select p FROM PermissionRecord p "
    + "WHERE p.department.id = :deptId"))
public class PermissionRecord implements PAuthorizedObject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    boolean createPermission;
    boolean readPermission;
    boolean updatePermission;
    boolean deletePermission;
    String absoluteObjectType;
    String objectName;
    int objectId;

    @ManyToOne(fetch = FetchType.EAGER)
    Department department;

    public PermissionRecord() {

    }

    public PermissionRecord(boolean create, boolean read, boolean update, boolean delete, String absoluteObjectType) {
        this.readPermission = read;
        this.createPermission = create;
        this.updatePermission = update;
        this.deletePermission = delete;
        this.absoluteObjectType = absoluteObjectType;
        setObjectNameFromAbsoluteObjectType();
    }

    public PermissionRecord(boolean create, boolean read, boolean update, boolean delete) {
        this.readPermission = read;
        this.createPermission = create;
        this.updatePermission = update;
        this.deletePermission = delete;
    }

    public boolean isCreatePermission() {
        return createPermission;
    }

    public void setCreatePermission(boolean createPermission) {
        this.createPermission = createPermission;
    }

    public boolean isReadPermission() {
        return readPermission;
    }

    public void setReadPermission(boolean readPermission) {
        this.readPermission = readPermission;
    }

    public boolean isUpdatePermission() {
        return updatePermission;
    }

    public void setUpdatePermission(boolean updatePermission) {
        this.updatePermission = updatePermission;
    }

    public boolean isDeletePermission() {
        return deletePermission;
    }

    public void setDeletePermission(boolean deletePermission) {
        this.deletePermission = deletePermission;
    }

    public int getObjectId() {
        return objectId;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectId(int objectId) {
        this.objectId = objectId;
    }

    public String getAbsoluteObjectType() {
        return absoluteObjectType;
    }

    public void setAbsoluteObjectType(String absoluteObjectType) {
        this.absoluteObjectType = absoluteObjectType;
        setObjectNameFromAbsoluteObjectType();
    }

    private void setObjectNameFromAbsoluteObjectType() {
        if (this.absoluteObjectType != null) {
            this.objectName = this.absoluteObjectType.substring(this.absoluteObjectType.lastIndexOf('.') + 1);
        }
    }

    public Department getDepartment() {
        return department;
    }

    public int getId() {
        return id;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }
}
