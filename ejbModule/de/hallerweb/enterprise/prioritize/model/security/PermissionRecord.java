package de.hallerweb.enterprise.prioritize.model.security;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import de.hallerweb.enterprise.prioritize.model.Department;
import de.hallerweb.enterprise.prioritize.view.security.RoleBean;

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
public class PermissionRecord {

	public PermissionRecord() {

	}

	public PermissionRecord(boolean create, boolean read, boolean update, boolean delete,
			String absoluteObjectType) {
		this.readPermission = read;
		this.createPermission = create;
		this.updatePermission = update;
		this.deletePermission = delete;
		this.absoluteObjectType = absoluteObjectType;
		//this.targetResourceType = targetResourceType;
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

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	int id;

	boolean createPermission;
	boolean readPermission;
	boolean updatePermission;
	boolean deletePermission;
	//Class<? extends PAuthorizedObject> targetResourceType;
	String absoluteObjectType;

	public String getAbsoluteObjectType() {
		return absoluteObjectType;
	}

	public void setAbsoluteObjectType(String absoluteObjectType) {
		this.absoluteObjectType = absoluteObjectType;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	Department department;

	public Department getDepartment() {
		return department;
	}

//	public Class<? extends PAuthorizedObject> getTargetResourceType() {
//		return targetResourceType;
//	}
//
//	public void setTargetResourceType(String targetResource) {
//		this.targetResourceType = (Class<? extends PAuthorizedObject>) RoleBean.authorizedObjects.get(targetResource);
//	}

	public int getId() {
		return id;
	}

	public void setDepartment(Department department) {
		this.department = department;
	}
}
