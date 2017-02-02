package de.hallerweb.enterprise.prioritize.view.security;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
import de.hallerweb.enterprise.prioritize.model.Company;
import de.hallerweb.enterprise.prioritize.model.Department;
import de.hallerweb.enterprise.prioritize.model.document.DocumentGroup;
import de.hallerweb.enterprise.prioritize.model.document.DocumentInfo;
import de.hallerweb.enterprise.prioritize.model.resource.Resource;
import de.hallerweb.enterprise.prioritize.model.resource.ResourceGroup;
import de.hallerweb.enterprise.prioritize.model.security.PAuthorizedObject;
import de.hallerweb.enterprise.prioritize.model.security.PermissionRecord;
import de.hallerweb.enterprise.prioritize.model.security.Role;
import de.hallerweb.enterprise.prioritize.model.security.User;
import de.hallerweb.enterprise.prioritize.model.skill.Skill;
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

	public static Map<String, Class<? extends PAuthorizedObject>> authorizedObjects;

	@Inject
	SessionController sessionController;
	@EJB
	UserRoleController controller;
	@EJB
	CompanyController companyController;
	@EJB
	AuthorizationController authController;

	Role role; 															// Current role
	List<Department> departments; 										// Departments
	String selectedDepartmentId; 										// currently selected department
	boolean createPermission; 											// (C)reate
	boolean readPermission; 											// (R)ead
	boolean updatePermission; 											// (U)pdate
	boolean deletePermission; 											// (D)elete
	String targetResourceType;
	List<String> resourceTypes = new ArrayList<String>();

	public List<String> getResourceTypes() {

		return resourceTypes;
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
		resourceTypes.clear();
		resourceTypes.add(Company.class.getCanonicalName());
		resourceTypes.add(DocumentInfo.class.getCanonicalName());
		resourceTypes.add(DocumentGroup.class.getCanonicalName());
		resourceTypes.add(User.class.getCanonicalName());
		resourceTypes.add(Role.class.getCanonicalName());
		resourceTypes.add(Resource.class.getCanonicalName());
		resourceTypes.add(ResourceGroup.class.getCanonicalName());
		resourceTypes.add(Skill.class.getCanonicalName());

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
			return "roles";
		} else {
			ViewUtilities.addErrorMessage("rolename", "The Rolename " + role.getName() + " already exists. Role has not been created!");
			return "roles";
		}
	}

	@Named
	public String delete(Role r) {
		controller.deleteRole(r.getId(), sessionController.getUser());
		init();
		return "roles";
	}

	@Named
	public String deletePermission(PermissionRecord rec) {
		controller.deletePermissionRecord(role.getId(), rec.getId());
		role.getPermissions().remove(rec);
		return "editrole";
	}

	@Named
	public String edit(Role r) {
		this.role = r;
		this.createPermission = false;
		this.readPermission = false;
		this.updatePermission = false;
		this.deletePermission = false;

		return "editrole";
	}

	@Named
	String editPermission(PermissionRecord rec) {
		return "editrole";
	}

	@Named
	public String save() {
		return "roles";
	}

	@Produces
	@Named
	public List<Department> getDepartments() {
		return companyController.getAllDepartments();

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

		controller.addPermissionRecord(role.getId(), rec);
		role.addPermission(rec);
		return "editrole";
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
			return authController.canCreate(deptId, new Role(), sessionController.getUser());
		} catch (NumberFormatException ex) {
			return authController.canCreate(-1, new Role(), sessionController.getUser());

		}
	}

}
