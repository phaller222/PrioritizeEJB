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
package de.hallerweb.enterprise.prioritize.controller.security;

import de.hallerweb.enterprise.prioritize.controller.InitializationController;
import de.hallerweb.enterprise.prioritize.controller.LoggingController;
import de.hallerweb.enterprise.prioritize.controller.LoggingController.Action;
import de.hallerweb.enterprise.prioritize.controller.project.ActionBoardController;
import de.hallerweb.enterprise.prioritize.controller.project.task.TaskController;
import de.hallerweb.enterprise.prioritize.controller.usersetting.ItemCollectionController;
import de.hallerweb.enterprise.prioritize.model.Address;
import de.hallerweb.enterprise.prioritize.model.Department;
import de.hallerweb.enterprise.prioritize.model.calendar.TimeSpan;
import de.hallerweb.enterprise.prioritize.model.project.task.Task;
import de.hallerweb.enterprise.prioritize.model.security.PermissionRecord;
import de.hallerweb.enterprise.prioritize.model.security.Role;
import de.hallerweb.enterprise.prioritize.model.security.User;
import de.hallerweb.enterprise.prioritize.model.security.payment.BankingAccount;
import de.hallerweb.enterprise.prioritize.model.skill.SkillRecord;
import de.hallerweb.enterprise.prioritize.model.usersetting.ItemCollection;
import de.hallerweb.enterprise.prioritize.model.usersetting.UserPreference;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.*;

/**
 * UserRoleController.java - Controls the creation, modification and deletion of
 * {@link Role} and {@link User} objects.
 */
@Stateless
public class UserRoleController {

    @PersistenceContext
    EntityManager em;

    @EJB
    LoggingController logger;
    @EJB
    AuthorizationController authController;
    @EJB
    ActionBoardController actionBoardController;
    @EJB
    ItemCollectionController itemCollectionController;
    @EJB
    TaskController taskController;
    @EJB
    InitializationController initController;
    @Inject
    SessionController sessionController;

    public static final String LITERAL_CREATED = "\" created.";
    public static final String LITERAL_SYSTEM = "SYSTEM";
    public static final String LITERAL_DELETED = "\" deleted.";
    public static final String LITERAL_PERMISSION_RECORD = "PermissionRecord";
    public static final String LITERAL_USER = "User";

    /**
     * Default constructor.
     */
    public UserRoleController() {
        // Empty default constructor
    }

    public Role findRoleByRolename(String roleName, User user) {
        Query query = em.createNamedQuery("findRoleByRolename");
        query.setParameter(1, roleName);
        try {
            Role r = (Role) query.getSingleResult();
            if (authController.canRead(r, user)) {
                return r;
            } else {
                return null;
            }
        } catch (Exception ex) {
            return null;
        }
    }

    public Role createRole(String name, String description, Set<PermissionRecord> permissions, User sessionUser) {
        if (findRoleByRolename(name, sessionUser) == null) {
            Role r = new Role();
            if (authController.canCreate(r, sessionUser)) {
                r.setName(name);
                r.setDescription(description);
                em.persist(r);
                em.flush();
                Set<PermissionRecord> managedPermissions = new HashSet<>();

                for (PermissionRecord rec : permissions) {
                    PermissionRecord recNew = persistPermissionRecord(rec);
                    managedPermissions.add(recNew);
                }
                r.setPermissions(managedPermissions);

                em.flush();
                try {
                    logger.log(sessionController.getUser().getUsername(), "Role", Action.CREATE, r.getId(),
                            "Role \"" + r.getName() + LITERAL_CREATED);
                } catch (Exception ex) {
                    logger.log(LITERAL_SYSTEM, "Role", Action.CREATE, r.getId(), " Role \"" + r.getName() + LITERAL_CREATED);
                }
                return r;
            } else {
                return null;
            }
        } else {
            // Rolename already exists, return null.
            return null;
        }
    }

    private PermissionRecord persistPermissionRecord(PermissionRecord rec) {
        Department managedDepartment;
        PermissionRecord recNew = new PermissionRecord();
        recNew.setCreatePermission(rec.isCreatePermission());
        recNew.setReadPermission(rec.isReadPermission());
        recNew.setUpdatePermission(rec.isUpdatePermission());
        recNew.setDeletePermission(rec.isDeletePermission());

        // Resource type can be null for ALL resource types!
        if (rec.getAbsoluteObjectType() != null) {
            recNew.setAbsoluteObjectType(rec.getAbsoluteObjectType());
        }
        // Also add explicitly assigned permissions
        recNew.setObjectId(rec.getObjectId());

        // rec.gertDepartment() can be null if permission record set for
        // all departments!
        if (rec.getDepartment() != null) {
            managedDepartment = em.find(Department.class, rec.getDepartment().getId());
            recNew.setDepartment(managedDepartment);
        }

        try {
            em.persist(recNew);
        } catch (Exception ex) {
            em.merge(recNew);
        }
        return recNew;
    }

    public void deleteRole(int id, User sessionUser) {
        Role r = findRoleById(id);
        if (authController.canDelete(r, sessionUser)) {
            Set<PermissionRecord> records = r.getPermissions();
            for (PermissionRecord rec : records) {
                PermissionRecord orig = em.find(PermissionRecord.class, rec.getId());
                em.remove(orig);
            }

            // Remove the role from all of it's users.
            Set<User> users = r.getUsers();
            for (User user : users) {
                User managedUser = em.find(User.class, user.getId());
                managedUser.removeRole(r);
            }

            em.remove(r);
            em.flush();

            logger.log(sessionController.getUser().getUsername(), "Role", Action.DELETE, r.getId(),
                    "Role \"" + r.getName() + LITERAL_DELETED);
        }
    }

    /**
     * Removes a permission record from a role.
     *
     * @param roleId       - id of the {@link Role}
     * @param permissionId - id of {@link PermissionRecord} to be removed.
     */
    public void deletePermissionRecord(int roleId, int permissionId, User sessionUser) {
        Role r = em.find(Role.class, roleId);
        if (r != null && authController.canDelete(AuthorizationController.PERMISSION_RECORD_TYPE, sessionUser)) {
            PermissionRecord rec = em.find(PermissionRecord.class, permissionId);

            if (rec.getDepartment() != null) {
                rec.setDepartment(null);
            }

            r.getPermissions().remove(rec);
            em.remove(rec);
            em.flush();

            logger.log(sessionController.getUser().getUsername(), LITERAL_PERMISSION_RECORD, Action.DELETE, rec.getId(),
                    LITERAL_PERMISSION_RECORD + " " + rec.getId() + LITERAL_DELETED);
        }
    }

    /**
     * Adds a {@link PermissionRecord} to the given {@link Role}
     *
     * @param roleId - ID of the {@link Role}
     * @param recNew - {@link PermissionRecord} to be added.
     */
    public void addPermissionRecord(int roleId, PermissionRecord recNew, User sessionUser) {
        Role r = em.find(Role.class, roleId);
        if (r != null && authController.canCreate(AuthorizationController.PERMISSION_RECORD_TYPE, sessionUser)) {
            try {
                em.persist(recNew);
                em.flush();
            } catch (Exception ex) {
                em.merge(recNew);
            }
            r.addPermission(recNew);
            em.flush();
            try {
                logger.log(sessionController.getUser().getUsername(), LITERAL_PERMISSION_RECORD, Action.CREATE, recNew.getId(),
                        LITERAL_PERMISSION_RECORD + " " + recNew.getId() + "\" added.");
            } catch (Exception ex) {
                logger.log(LITERAL_SYSTEM, LITERAL_PERMISSION_RECORD, Action.CREATE, recNew.getId(),
                        LITERAL_PERMISSION_RECORD + " " + recNew.getId() + "\" added.");
            }
        }
    }

    public List<Role> getAllRoles(User sessionUser) {
        Query query = em.createQuery("SELECT r FROM Role r ORDER BY r.name");
        List<Role> roles = query.getResultList();
        Role r = roles.get(0);
        if (authController.canRead(r, sessionUser)) {
            return roles;
        } else {
            return new ArrayList<>();
        }
    }

    public Role findRoleById(int id) {
        return em.find(Role.class, id);
    }

    // -------------------------- Users ---------------------------------------------------

    public User createUser(User newUser, Department initialDepartment, Set<Role> roles, User sessionUser) {
        if (authController.canCreate(newUser, sessionUser)) {
            if (findUserByUsername(newUser.getUsername(), authController.getSystemUser()) == null) {
                User userToCreate = User.newInstane(newUser);
                userToCreate.setPassword(String.valueOf(userToCreate.getPassword().hashCode()));
                userToCreate.setDepartment(initialDepartment);
                UserPreference preference = new UserPreference(userToCreate);
                em.persist(preference);
                userToCreate.setPreference(preference);
                em.persist(userToCreate);
                Address adr = newUser.getAddress();
                em.persist(adr);
                userToCreate.setAddress(adr);

                actionBoardController.createActionBoard(userToCreate.getName(), userToCreate.getName() + "'s board",
                        userToCreate);

                for (Role role : roles) {
                    Role managedRole = em.find(Role.class, role.getId());
                    userToCreate.addRole(managedRole);
                    managedRole.addUser(userToCreate);
                }

                em.flush();
                try {
                    logger.log(sessionController.getUser().getUsername(), "User", Action.CREATE, newUser.getId(),
                            LITERAL_USER + " " + newUser.getUsername() + LITERAL_CREATED);
                } catch (Exception ex) {
                    logger.log(LITERAL_SYSTEM, "User", Action.CREATE, newUser.getId(),
                            LITERAL_USER + " " + newUser.getUsername() + LITERAL_CREATED);
                }
                return userToCreate;
            } else {
                // Username already exists, return null.
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Edit User information.
     */
    public void editUser(int id, User newUserData, User sessionUser) {

        User user = em.find(User.class, id);
        if (authController.canUpdate(user, sessionUser)) {
            user.setUsername(newUserData.getUsername());
            user.setName(newUserData.getName());
            user.setFirstname(newUserData.getFirstname());
            user.setEmail(newUserData.getEmail());
            if ((newUserData.getPassword() != null) && (newUserData.getPassword().length() > 0)) {
                user.setPassword(String.valueOf(newUserData.getPassword().hashCode()));
            }
            user.setOccupation(newUserData.getOccupation());

            if (newUserData.getDepartment() != null) {
                Department dept = em.find(Department.class, newUserData.getDepartment().getId());
                user.setDepartment(dept);
            }

            em.flush();

            logger.log(sessionUser.getUsername(), "User", Action.UPDATE, user.getId(),
                    LITERAL_USER + " " + user.getUsername() + "\" updated.");
        }
    }

    public void editRole(Role role, String newName, String newDescription) {
        Role managedRole = em.find(Role.class, role.getId());
        managedRole.setName(newName);
        managedRole.setDescription(newDescription);
        em.flush();
    }

    public void setRoleDepartment(Role role, Department dept) {
        Role managedRole = em.find(Role.class, role.getId());
        managedRole.setDepartment(dept);
        em.flush();
    }


    public List<User> getAllUsers(User sessionUser) {
        Query query = em.createNamedQuery("findAllUsers");
        List<User> users = query.getResultList();
        if (authController.canRead(AuthorizationController.USER_TYPE, sessionUser)) {
            return users;
        } else {
            return new ArrayList<>();
        }

    }

    public List<String> getAllUserNames(User sessionUser) {
        if (authController.canRead(AuthorizationController.USER_TYPE, sessionUser)) {
            Query query = em.createNamedQuery("findAllUserNames");
            return query.getResultList();

        } else {
            return new ArrayList<>();
        }
    }

    public User getUserById(int id, User sessionUser) {
        User user = em.find(User.class, id);
        if (authController.canRead(user, sessionUser)) {
            return user;
        } else {
            return null;
        }

    }

    public List<User> getUsersForDepartment(Department dept, User sessionUser) {
        Query query = em.createNamedQuery("findUserByDepartment");
        query.setParameter("deptId", dept.getId());
        List<User> deptUsers = query.getResultList();
        if (deptUsers == null || deptUsers.isEmpty()) {
            return new ArrayList<>();
        }
        User userToCheck = deptUsers.get(0);
        if (authController.canRead(userToCheck, sessionUser)) {
            return deptUsers;
        } else {
            return new ArrayList<>();
        }
    }

    public User findUserByUsername(String username, User sessionUser) {
        Query query = em.createNamedQuery("findUserByUsername");
        query.setParameter(1, username);
        try {
            User u = (User) query.getSingleResult();
            if (authController.canRead(u, sessionUser)) {
                return u;
            } else {
                return null;
            }
        } catch (NoResultException ex) {
            return null;
        }
    }

    public User findUserByApiKey(String apiKey) {
        Query query = em.createNamedQuery("findUserByApiKey");
        query.setParameter("apiKey", apiKey);
        User user;
        try {
            user = (User) query.getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
        return user;
    }

    public User findUserById(int id, User sessionUser) {
        if (authController.canRead(AuthorizationController.USER_TYPE, sessionUser)) {
            return em.find(User.class, id);
        } else {
            return null;
        }
    }

    public void deleteUser(int id, User sessionUser) {
        User u = findUserById(id, sessionUser);
        if (authController.canDelete(u, sessionUser)) {

            Set<Role> roles = u.getRoles();
            for (Role r : roles) {
                r.removeUser(u);
                u.removeRole(r);
            }

            // Delete UserPreferences of user
            UserPreference p = u.getPreference();
            u.setPreference(null);
            if (p != null) {
                em.remove(p);
            }

            // Delete all user item collections
            List<ItemCollection> collection = itemCollectionController.getItemCollections(u);
            if (collection != null && !collection.isEmpty()) {
                for (ItemCollection c : collection) {
                    c.getUsers().clear();
                    em.remove(c);
                }
            }

            Set<BankingAccount> accounts = u.getBankingAccounts();
            em.flush();
            em.remove(u);
            em.flush();

            for (BankingAccount account : accounts) {
                em.remove(account);
            }

            int userId = u.getId();
            String userName = u.getUsername();
            logger.log(sessionController.getUser().getUsername(), LITERAL_USER, Action.DELETE, userId,
                    LITERAL_USER + " " + userName + LITERAL_DELETED);
        }
    }

    public void removeRoleFromUser(int userId, int roleId, User sessionUser) {
        // TODO: Sometimes either the role or the user is not beeing updated!
        // ISSUE:
        // http://mantis.hallerweb.de/view.php?id=67
        User u = em.find(User.class, userId);
        if (authController.canUpdate(u, sessionUser)) {
            Role r = em.find(Role.class, roleId);
            r.removeUser(u);
            u.removeRole(r);
            em.flush();

            if (r.getUsers().contains(u)) {
                r.removeUser(u);
            }
            if (u.getRoles().contains(r)) {
                u.removeRole(r);
            }
        }

    }

    public void removeAllRolesFromUser(int userId, User sessionUser) {
        User u = em.find(User.class, userId);
        if (authController.canUpdate(u, sessionUser)) {
            for (Role r : u.getRoles()) {
                removeRoleFromUser(u.getId(), r.getId(), sessionUser);
            }
        }
    }

    public void removeSkillFromUser(SkillRecord sRecord, User user, User sessionUser) {
        User u = em.find(User.class, user.getId());
        if (authController.canUpdate(u, sessionUser)) {
            SkillRecord skillRecord = em.find(SkillRecord.class, sRecord.getId());
            skillRecord.setUser(null);
            u.removeSkill(skillRecord);
            em.remove(skillRecord);
            em.flush();
        }
    }

    public void addRoleToUser(int userId, int roleId, User sessionUser) {
        User u = em.find(User.class, userId);
        if (authController.canUpdate(u, sessionUser)) {
            Role r = em.find(Role.class, roleId);

            u.addRole(r);
            r.addUser(u);
        }
    }

    public void addSkillToUser(int userId, int skillRecordId, User sessionUser) {
        boolean alreadyAssigned = false; // is set to true if skill type already assigned.
        User u = em.find(User.class, userId);
        if (authController.canUpdate(u, sessionUser)) {
            SkillRecord rec = em.find(SkillRecord.class, skillRecordId);
            for (SkillRecord userSkillRecord : u.getSkills()) {
                if (userSkillRecord.getSkill().getId() == rec.getId()) {
                    alreadyAssigned = true;
                    break;
                }
            }
            // Only assign new skill if user does not already have
            // a skill of that kind assigned.
            if (!alreadyAssigned) {
                u.addSkill(rec);
                rec.setUser(u);
                em.flush();
            }
        }
    }

    public Set<SkillRecord> getSkillRecordsForUser(int userId, User sessionUser) {
        User u = em.find(User.class, userId);
        if (authController.canRead(u, sessionUser)) {
            return u.getSkills();
        } else {
            return new HashSet<>();
        }
    }

    public void addVacation(User user, TimeSpan timespan, User sessionUser) {
        if (authController.canUpdate(user, sessionUser)) {
            boolean intersects = false;
            User managedUser = em.find(User.class, user.getId());
            Set<TimeSpan> userVacation = managedUser.getVacation();
            if (!userVacation.isEmpty()) {
                for (TimeSpan ts : userVacation) {
                    if (timespan.intersects(ts)) {
                        intersects = true; // Don't add vacation, it's a doublette
                    }
                }
            }
            if (!intersects) {
                em.persist(timespan);
                em.flush();
                managedUser.addVacation(timespan);
                logger.log(sessionController.getUser().getUsername(), LITERAL_USER, Action.UPDATE, user.getId(),
                        "Vacation added for User \"" + user.getUsername() + "\" .");
            }
        }
    }

    public void removeVacation(User user, int timeSpanId, User sessionUser) {
        if (authController.canUpdate(user, sessionUser)) {
            User managedUser = em.find(User.class, user.getId());
            TimeSpan managedTimeSpan = em.find(TimeSpan.class, timeSpanId);
            managedUser.removeVacation(managedTimeSpan.getId());
            em.remove(managedTimeSpan);
            em.flush();
            logger.log(sessionController.getUser().getUsername(), LITERAL_USER, Action.UPDATE, user.getId(),
                    "Vacation removed for User \"" + user.getUsername() + "\" .");
        }
    }

    public Set<TimeSpan> getVacation(User user, User sessionUser) {
        if (authController.canRead(user, sessionUser)) {
            return user.getVacation();
        } else {
            return new HashSet<>();
        }
    }

    public void setIllness(User user, TimeSpan timespan, User sessionUser) {
        User managedUser = em.find(User.class, user.getId());
        if (authController.canUpdate(managedUser, sessionUser)) {
            em.persist(timespan);
            em.flush();
            managedUser.setIllness(timespan);
            logger.log(sessionController.getUser().getUsername(), LITERAL_USER, Action.UPDATE, user.getId(),
                    "Illness added for User \"" + user.getUsername() + "\" .");
        }
    }

    public void removeIllness(User user, int timeSpanId, User sessionUser) {
        User managedUser = em.find(User.class, user.getId());
        if (authController.canUpdate(managedUser, sessionUser)) {
            TimeSpan managedTimeSpan = em.find(TimeSpan.class, timeSpanId);
            managedUser.removeIllness();
            em.remove(managedTimeSpan);
            em.flush();
            logger.log(sessionController.getUser().getUsername(), LITERAL_USER, Action.DELETE, user.getId(),
                    "Illness removed for User \"" + user.getUsername() + "\" .");
        }
    }

    public TimeSpan getIllness(User user, User sessionUser) {
        if (authController.canRead(user, sessionUser)) {
            return user.getIllness();
        } else {
            return null;
        }
    }

    public String generateApiKey(User user, User sessionUser) {
        if (authController.canUpdate(AuthorizationController.USER_TYPE, sessionUser)) {
            String apiKey = UUID.randomUUID().toString();
            User u = em.find(User.class, user.getId());
            if (authController.canUpdate(u, sessionUser)) {
                u.setApiKey(apiKey);
                return apiKey;
            } else {
                return null;
            }
        } else {
            return null;
        }

    }


    // -----------------------------------------------
    // TODO: Task assignment not checked at the moment, add PermissionRecord type if necessary in the future.
    public void assignTask(User user, Task task) {
        User managedUser = findUserById(user.getId(), user);
        managedUser.addAssignedTask(task);
    }

    public void removeAssignedTask(User user, Task task, User sessionUser) {
        User managedUser = findUserById(user.getId(), sessionUser);
        Task managedTask = taskController.findTaskById(task.getId());
        managedUser.removeAssignedTask(managedTask);
    }
    // -----------------------------------------------------------------

}
