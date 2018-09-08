package de.hallerweb.enterprise.prioritize.view.security;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;

import de.hallerweb.enterprise.prioritize.controller.CompanyController;
import de.hallerweb.enterprise.prioritize.controller.security.AuthorizationController;
import de.hallerweb.enterprise.prioritize.controller.security.SessionController;
import de.hallerweb.enterprise.prioritize.controller.security.UserRoleController;
import de.hallerweb.enterprise.prioritize.model.Department;
import de.hallerweb.enterprise.prioritize.model.document.DocumentInfo;
import de.hallerweb.enterprise.prioritize.model.security.ObservedObjectType;
import de.hallerweb.enterprise.prioritize.model.security.PermissionRecord;
import de.hallerweb.enterprise.prioritize.model.security.Role;
import de.hallerweb.enterprise.prioritize.view.ViewUtilities;

/**
 * RoleBean - JSF Backing-Bean to store session information for clients about Roles.
 * <p>
 * Copyright: (c) 2014
 * </p>
 * <p>
 * Peter Haller
 * </p>
 * 
 * @author peter
 */
@Named
@SessionScoped
public class RoleBean implements Serializable {

	@Inject
	SessionController sessionController;
	@EJB
	UserRoleController controller;
	@EJB
	CompanyController companyController;
	@EJB
	AuthorizationController authController;

	transient Role role; 										// Current role
	transient List<Department> departments;						// Departments
	String selectedDepartmentId; 								// currently selected department
	boolean createPermission; 									// (C)reate
	boolean readPermission; 									// (R)ead
	boolean updatePermission; 									// (U)pdate
	boolean deletePermission; 									// (D)elete
	String targetResourceType;
	transient List<String> resourceTypes = new ArrayList<>();

	private static final String NAVIGATION_ROLES = "roles";
	private static final String NAVIGATION_EDITROLE = "editrole";

	public List<String> getObservableObjectTypesResourceTypes() {
		List<String> observableObjectResourceTypes = new ArrayList<>();
		List<ObservedObjectType> objectTypes = authController.getObservableObjectTypes();
		for (ObservedObjectType type : objectTypes) {
			observableObjectResourceTypes.add(type.getObjectType());
		}
		return observableObjectResourceTypes;
	}

	@Named
	public String getTargetResourceType() {
		return targetResourceType;
	}

	public void setTargetResourceType(String targetResourceType) {
		this.targetResourceType = targetResourceType;
	}

	@Named
	public String getSelectedDepartmentId() {
		return selectedDepartmentId;
	}

	public void setSelectedDepartmentId(String selectedDepartmentId) {
		this.selectedDepartmentId = selectedDepartmentId;
	}

	@Named
	public boolean getDeletePermission() {
		return deletePermission;
	}

	public void setDeletePermission(boolean deletePermission) {
		this.deletePermission = deletePermission;
	}

	@Named
	public boolean getUpdatePermission() {
		return updatePermission;
	}

	public void setUpdatePermission(boolean updatePermission) {
		this.updatePermission = updatePermission;
	}

	/**
	 * Initialize empty {@link Role}
	 */
	@PostConstruct
	public void init() {

		role = new Role();

		PermissionRecord rec = new PermissionRecord(false, false, false, false, DocumentInfo.class.getCanonicalName());
		role.setName(" ");

		role.addPermission(rec);

	}

	@Produces
	@Named
	public List<Role> getRoles() {
		return controller.getAllRoles(sessionController.getUser());
	}

	@Named
	public boolean getReadPermission() {
		return readPermission;
	}

	public void setReadPermission(boolean readPermission) {
		this.readPermission = readPermission;
	}

	@Named
	public boolean getCreatePermission() {
		return createPermission;
	}

	public void setCreatePermission(boolean createPermission) {
		this.createPermission = createPermission;
	}

	@Named
	public Role getRole() {
		return role;
	}

	@Named
	public void setRole(Role role) {
		this.role = role;
	}

	@Named
	public String createRole() {
		if (controller.findRoleByRolename(role.getName(), sessionController.getUser()) == null) {
			Set<PermissionRecord> permissionRecords = role.getPermissions();
			PermissionRecord rec;
			if (permissionRecords.isEmpty()) {
				rec = new PermissionRecord(false, false, false, false, targetResourceType);
			} else {
				rec = role.getPermissions().iterator().next();
			}
			rec.setCreatePermission(createPermission);
			rec.setReadPermission(readPermission);
			rec.setUpdatePermission(updatePermission);
			rec.setDeletePermission(deletePermission);

			rec.setAbsoluteObjectType(targetResourceType);

			// Set department only if permission record is not set for ALL departments!
			if (selectedDepartmentId != null) {
				Department d = companyController.findDepartmentById(Integer.valueOf(selectedDepartmentId));
				rec.setDepartment(d);
			}

			role.getPermissions().clear();
			role.addPermission(rec);

			controller.createRole(role.getName(), role.getDescription(), role.getPermissions(), sessionController.getUser());
			init();
			return NAVIGATION_ROLES;
		} else {
			ViewUtilities.addErrorMessage("rolename", "The Rolename " + role.getName() + " already exists. Role has not been created!");
			return NAVIGATION_ROLES;
		}
	}

	@Named
	public String delete(Role role) {
		controller.deleteRole(role.getId(), sessionController.getUser());
		init();
		return NAVIGATION_ROLES;
	}

	@Named
	public String deletePermission(PermissionRecord rec) {
		controller.deletePermissionRecord(role.getId(), rec.getId(), sessionController.getUser());
		role.getPermissions().remove(rec);
		return NAVIGATION_EDITROLE;
	}

	@Named
	public String edit(Role role) {
		this.role = role;
		this.createPermission = false;
		this.readPermission = false;
		this.updatePermission = false;
		this.deletePermission = false;

		return NAVIGATION_EDITROLE;
	}

	@Named
	String editPermission(PermissionRecord rec) {
		return NAVIGATION_EDITROLE;
	}

	@Named
	public String save() {
		return NAVIGATION_EDITROLE;
	}

	@Produces
	@Named
	public List<Department> getDepartments() {
		return companyController.getAllDepartments(sessionController.getUser());

	}

	@Named
	public String createNewPermissionForRole() {
		PermissionRecord rec = new PermissionRecord();
		rec.setCreatePermission(createPermission);
		rec.setDeletePermission(deletePermission);
		rec.setUpdatePermission(updatePermission);
		rec.setReadPermission(readPermission);

		rec.setAbsoluteObjectType(targetResourceType);

		// Only set department if record is for a specific department
		if (selectedDepartmentId != null) {
			Department d = companyController.findDepartmentById(Integer.valueOf(selectedDepartmentId));
			rec.setDepartment(d);
		}

		controller.addPermissionRecord(role.getId(), rec, sessionController.getUser());
		role.addPermission(rec);
		return NAVIGATION_EDITROLE;
	}

	@Named
	public boolean canRead(Role role) {
		if (role == null) {
			return false;
		}
		return authController.canRead(role, sessionController.getUser());
	}

	@Named
	public boolean canUpdate(Role role) {
		if (role == null) {
			return false;
		}
		return authController.canUpdate(role, sessionController.getUser());
	}

	@Named
	public boolean canDelete(Role role) {
		if (role == null) {
			return false;
		}
		return authController.canDelete(role, sessionController.getUser());
	}

	@Named
	public boolean canCreate() {
		try {
			int deptId = Integer.parseInt(this.selectedDepartmentId);
			return authController.canCreate(deptId, AuthorizationController.ROLE_TYPE, sessionController.getUser());
		} catch (NumberFormatException ex) {
			return authController.canCreate(-1, AuthorizationController.ROLE_TYPE, sessionController.getUser());

		}
	}

}
