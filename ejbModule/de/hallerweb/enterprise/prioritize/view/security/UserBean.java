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

package de.hallerweb.enterprise.prioritize.view.security;

import de.hallerweb.enterprise.prioritize.controller.CompanyController;
import de.hallerweb.enterprise.prioritize.controller.project.ActionBoardController;
import de.hallerweb.enterprise.prioritize.controller.security.AuthorizationController;
import de.hallerweb.enterprise.prioritize.controller.security.SessionController;
import de.hallerweb.enterprise.prioritize.controller.security.UserRoleController;
import de.hallerweb.enterprise.prioritize.controller.usersetting.ItemCollectionController;
import de.hallerweb.enterprise.prioritize.model.Department;
import de.hallerweb.enterprise.prioritize.model.calendar.TimeSpan;
import de.hallerweb.enterprise.prioritize.model.calendar.TimeSpan.TimeSpanType;
import de.hallerweb.enterprise.prioritize.model.project.ActionBoard;
import de.hallerweb.enterprise.prioritize.model.project.ActionBoardEntry;
import de.hallerweb.enterprise.prioritize.model.security.Role;
import de.hallerweb.enterprise.prioritize.model.security.User;
import de.hallerweb.enterprise.prioritize.model.skill.SkillRecord;
import de.hallerweb.enterprise.prioritize.model.usersetting.ItemCollection;
import de.hallerweb.enterprise.prioritize.view.ViewUtilities;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * UserBean - JSF Backing-Bean to store client state information about Users.
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
public class UserBean implements Serializable {

    @Inject
    SessionController sessionController;
    @EJB
    transient UserRoleController controller;
    @EJB
    transient AuthorizationController authController;
    @EJB
    transient CompanyController companyController;
    @EJB
    transient ItemCollectionController itemCollectionController;
    @EJB
    transient ActionBoardController actionboardController;

    transient User user;                                                        // Stores the user
    String selectedDepartmentId;                                    // Selected Department
    transient List<String> selectedRolesId;                                    // Selected Roles
    String roleToAddId;                                            // Role to add
    transient List<User> users;                                                // List of Users in the system.
    transient User currentUser;                                                // The user who is logged in into the admin pages.
    transient Set<SkillRecord> skillRecords;

    Date vacationFrom;
    Date vacationUntil;
    Date illnessFrom;
    Date illnessUntil;
    transient List<TimeSpan> vacations;

    String selectedItemCollectionName;

    transient List<ActionBoardEntry> actionBoardEntries;

    private static final String NAVIGATION_USERS = "users";
    private static final String NAVIGATION_EDITUSER = "edituser";

    public List<TimeSpan> getVacations() {
        return vacations;
    }

    public String getSelectedItemCollectionName() {
        return selectedItemCollectionName;
    }

    public void setSelectedItemCollectionName(String selectedItemCollectionName) {
        this.selectedItemCollectionName = selectedItemCollectionName;
    }

    public void setVacations(List<TimeSpan> vacations) {
        this.vacations = vacations;
    }

    public Date getVacationFrom() {
        return vacationFrom;
    }

    public void setVacationFrom(Date vacationFrom) {
        this.vacationFrom = vacationFrom;
    }

    public Date getVacationUntil() {
        return vacationUntil;
    }

    public void setVacationUntil(Date vacationUntil) {
        this.vacationUntil = vacationUntil;
    }

    @Named
    public Set<SkillRecord> getSkillRecords() {
        return controller.getSkillRecordsForUser(this.user.getId(), sessionController.getUser());
    }

    public void setSkillRecords(Set<SkillRecord> skillRecords) {
        this.skillRecords = skillRecords;
    }

    @Named
    public User getCurrentUser() {
        return sessionController.getUser();
    }

    @Named
    public String getRoleToAddId() {
        return roleToAddId;
    }

    public void setRoleToAddId(String roleToAddId) {
        this.roleToAddId = roleToAddId;
    }

    @Named
    public String getSelectedDepartmentId() {
        return selectedDepartmentId;
    }

    public void setSelectedDepartmentId(String selectedDepartmentId) {
        this.selectedDepartmentId = selectedDepartmentId;
    }

    @Named
    public List<String> getSelectedRolesId() {
        return selectedRolesId;
    }

    public void setSelectedRolesId(List<String> selectedRolesId) {
        this.selectedRolesId = selectedRolesId;
    }

    /**
     * Initialize empty {@link Role}
     */
    @PostConstruct

    public void init() {
        user = new User();
    }

    public void clearRoles() {
        user.setRoles(new HashSet<>());
    }

    @Produces
    @Named
    public List<User> getUsers() {
        return controller.getAllUsers(sessionController.getUser());
    }

    @Named
    public User getUser() {
        return user;
    }

    @Named
    public void setUser(User user) {
        this.user = user;
    }

    @Named
    public String createUser() {

        // Only if username does not exist yet
        if (controller.findUserByUsername(user.getUsername(), sessionController.getUser()) == null) {

            clearRoles();
            Department department = null;

            if (selectedDepartmentId != null && !selectedDepartmentId.isEmpty()) {
                department = companyController.findDepartmentById(Integer.parseInt(selectedDepartmentId));
                user.setDepartment(department);
            }

            addUserRole();

            controller.createUser(user, department, user.getRoles(), sessionController.getUser());

            init();
        } else {
            ViewUtilities.addErrorMessage("username", "The username " + user.getUsername() + " already exists. User has not been created!");
        }
        return NAVIGATION_USERS;
    }

    private void addUserRole() {
        if (selectedRolesId != null && !selectedRolesId.isEmpty()) {
            Role role;
            for (String roleId : selectedRolesId) {
                role = controller.findRoleById(Integer.parseInt((String) roleId));
                user.addRole(role);
            }
        }
    }

    @Named
    public String delete(User user) {
        controller.deleteUser(user.getId(), sessionController.getUser());
        init();
        return NAVIGATION_USERS;
    }

    @Named
    public String addRole() {
        Role r = controller.findRoleById(Integer.parseInt(roleToAddId));
        if (!user.getRoles().contains(r)) {
            user.addRole(r);
            controller.addRoleToUser(user.getId(), r.getId(), sessionController.getUser());
        }
        return NAVIGATION_EDITUSER;
    }

    @Named
    public String removeRole(Role role) {

        user.removeRole(role);
        role.removeUser(user);
        controller.removeRoleFromUser(user.getId(), role.getId(), sessionController.getUser());
        return NAVIGATION_EDITUSER;
    }

    public void removeSkillFromUser(SkillRecord skillRecord) {
        controller.removeSkillFromUser(skillRecord, user, sessionController.getUser());
    }

    @Named
    public String edit(User newUserData) {
        this.user = newUserData;
        if (user.getDepartment() != null) {
            this.selectedDepartmentId = String.valueOf(user.getDepartment().getId());
        }
        return NAVIGATION_EDITUSER;
    }

    @Named
    public String saveUser() {
        controller.editUser(user.getId(), user, sessionController.getUser());
        return NAVIGATION_EDITUSER;
    }

    @Named
    public boolean canRead(User info) {
        if (info == null) {
            return false;
        }
        return authController.canRead(info, sessionController.getUser());
    }

    @Named
    public boolean canUpdate(User info) {
        if (info == null) {
            return false;
        }
        return authController.canUpdate(info, sessionController.getUser());
    }

    public Date getIllnessFrom() {
        return illnessFrom;
    }

    public void setIllnessFrom(Date illnessFrom) {
        this.illnessFrom = illnessFrom;
    }

    public Date getIllnessUntil() {
        return illnessUntil;
    }

    public void setIllnessUntil(Date illnessUntil) {
        this.illnessUntil = illnessUntil;
    }

    @Named
    public boolean canDelete(User info) {
        if (info == null) {
            return false;
        }
        return authController.canDelete(info, sessionController.getUser());
    }

    @Named
    public boolean canCreate() {
        try {
            int deptId = Integer.parseInt(this.selectedDepartmentId);
            return authController.canCreate(deptId, AuthorizationController.USER_TYPE, sessionController.getUser());
        } catch (NumberFormatException ex) {
            return authController.canCreate(-1, AuthorizationController.USER_TYPE, sessionController.getUser());
        }
    }

    @Named
    public String addVacation() {
        TimeSpan ts = new TimeSpan();
        ts.setDateFrom(this.vacationFrom);
        ts.setDateUntil(this.vacationUntil);
        ts.setDescription(user.getName());
        ts.setTitle(user.getName());
        ts.addInvolvedUser(user);
        ts.setType(TimeSpanType.VACATION);

        controller.addVacation(user, ts, sessionController.getUser());
        boolean intersects = false;
        if (!user.getVacation().isEmpty()) {
            for (TimeSpan tsView : user.getVacation()) {
                if (tsView.intersects(ts)) {
                    intersects = true; // Don't add vacation, it's a doublette
                }
            }
        }
        if (!intersects) {
            user.addVacation(ts);
        }

        return NAVIGATION_EDITUSER;
    }

    @Named
    public String removeVacation(TimeSpan timespan) {
        controller.removeVacation(user, timespan.getId(), sessionController.getUser());
        return NAVIGATION_EDITUSER;
    }

    @Named
    public String removeIllness(TimeSpan timespan) {
        controller.removeIllness(user, timespan.getId(), sessionController.getUser());
        return NAVIGATION_EDITUSER;
    }

    @Named
    public String setIllness() {
        TimeSpan ts = new TimeSpan();
        ts.setDateFrom(this.illnessFrom);
        ts.setDateUntil(this.illnessUntil);
        ts.setDescription(user.getName());
        ts.setTitle(user.getName());
        ts.addInvolvedUser(user);
        ts.setType(TimeSpanType.ILLNESS);

        controller.setIllness(user, ts, sessionController.getUser());
        user.setIllness(ts);
        return NAVIGATION_EDITUSER;
    }

    @Named
    public String showCalendar() {
        return "calendar";
    }

    @Named
    public String generateApiKey() {
        this.user.setApiKey(controller.generateApiKey(this.user, sessionController.getUser()));
        return NAVIGATION_EDITUSER;
    }

    @Named
    public void addUserToItemCollection(User user) {
        ItemCollection managedCollection = itemCollectionController.getItemCollection(sessionController.getUser(),
                selectedItemCollectionName);
        if (managedCollection != null) {
            User managedUser = controller.getUserById(user.getId(), sessionController.getUser());
            itemCollectionController.addUser(managedCollection, managedUser);
        }
    }

    @Named
    public List<ActionBoardEntry> getActionBoardEntries() {
        ActionBoard board = actionboardController.findActionBoardByOwner(sessionController.getUser().getId());
        return board.getEntries();
    }
}
