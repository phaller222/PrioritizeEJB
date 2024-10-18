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

package de.hallerweb.enterprise.prioritize.controller;

import de.hallerweb.enterprise.prioritize.controller.LoggingController.Action;
import de.hallerweb.enterprise.prioritize.controller.security.AuthorizationController;
import de.hallerweb.enterprise.prioritize.controller.security.SessionController;
import de.hallerweb.enterprise.prioritize.controller.security.UserRoleController;
import de.hallerweb.enterprise.prioritize.model.Address;
import de.hallerweb.enterprise.prioritize.model.Company;
import de.hallerweb.enterprise.prioritize.model.Department;
import de.hallerweb.enterprise.prioritize.model.document.DocumentGroup;
import de.hallerweb.enterprise.prioritize.model.document.DocumentInfo;
import de.hallerweb.enterprise.prioritize.model.resource.Resource;
import de.hallerweb.enterprise.prioritize.model.resource.ResourceGroup;
import de.hallerweb.enterprise.prioritize.model.security.PermissionRecord;
import de.hallerweb.enterprise.prioritize.model.security.Role;
import de.hallerweb.enterprise.prioritize.model.security.User;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DepartmentController.java - Controls the creation, modification and deletion of {@link Department} objects.
 */
@Stateless
public class DepartmentController {

    @PersistenceContext
    EntityManager em;
    @EJB
    AuthorizationController authController;
    @EJB
    UserRoleController userRoleController;
    @EJB
    LoggingController logger;
    @EJB
    CompanyController companyController;
    @Inject
    SessionController sessionController;

    public static final String LITERAL_COMPANY = "Company";
    public static final String LITERAL_DEPARTMENT = "Department";
    public static final String LITERAL_CREATED = "\" created.";
    public static final String LITERAL_SYSTEM = "SYSTEM";
    public static final String LITERAL_DEFAULT = "default";


    public Department createDepartment(Company company, String name, String description, Address adr, User sessionUser) {
        if (authController.canCreate(AuthorizationController.DEPARTMENT_TYPE, sessionUser)) {
            Department dept = new Department();
            dept.setName(name);
            dept.setDescription(description);
            Company c = em.find(Company.class, company.getId());
            dept.setCompany(c);

            if (departmentExists(name, c)) {
                return null;
            }
            Address address = getAddress(company, adr);

            DocumentGroup defaultDocumentGroup = new DocumentGroup();
            defaultDocumentGroup.setDepartment(dept);
            defaultDocumentGroup.setName(LITERAL_DEFAULT);

            ResourceGroup defaultResourceGroup = new ResourceGroup();
            defaultResourceGroup.setDepartment(dept);
            defaultResourceGroup.setName(LITERAL_DEFAULT);

            em.persist(defaultDocumentGroup);
            em.persist(defaultResourceGroup);

            em.persist(address);

            dept.setAddress(address);
            dept.addDocumentGroup(defaultDocumentGroup);
            dept.addResourceGroup(defaultResourceGroup);

            // Generate token
            if (c.getName().equalsIgnoreCase("Default Company") && (name.equals(LITERAL_DEFAULT))) {
                dept.setToken(InitializationController.DEFAULT_DEPARTMENT_TOKEN);
            } else {
                UUID token = UUID.randomUUID();
                dept.setToken(token.toString().replace("-", ""));
            }

            em.persist(dept);
            em.flush();

            // The user who created this department should automatically get all permissions on objects in this department

            Set<PermissionRecord> records = new HashSet<>();

            PermissionRecord deptDocs = new PermissionRecord(true, true, true, true);
            deptDocs.setAbsoluteObjectType(DocumentInfo.class.getCanonicalName());
            deptDocs.setDepartment(dept);
            records.add(deptDocs);

            PermissionRecord deptDocsDir = new PermissionRecord(true, true, true, true);
            deptDocsDir.setAbsoluteObjectType(DocumentGroup.class.getCanonicalName());
            deptDocsDir.setDepartment(dept);
            records.add(deptDocsDir);

            PermissionRecord deptResources = new PermissionRecord(true, true, true, true);
            deptResources.setAbsoluteObjectType(Resource.class.getCanonicalName());
            deptResources.setDepartment(dept);
            records.add(deptResources);

            PermissionRecord deptResourcesDir = new PermissionRecord(true, true, true, true);
            deptResourcesDir.setAbsoluteObjectType(ResourceGroup.class.getCanonicalName());
            deptResourcesDir.setDepartment(dept);
            records.add(deptResourcesDir);


            Role r = userRoleController.createRole(company.getName() + "-" + dept.getName() + "-Admin",
                company.getName() + " - " + dept.getName() + " - Admin", records, sessionUser);

            try {
                userRoleController.addRoleToUser(sessionController.getUser().getId(), r.getId(), sessionUser);
                sessionController.getUser().addRole(r);
            } catch (Exception ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.WARNING, ex.getMessage());
            }

            // Logging
            try {
                logger.log(sessionController.getUser().getUsername(), LITERAL_DEPARTMENT, Action.CREATE, c.getId(), " " + LITERAL_DEPARTMENT
                    + " " + dept.getName() + "\" in Company \"" + dept.getCompany().getName() + LITERAL_CREATED);
            } catch (Exception ex) {
                logger.log(LITERAL_SYSTEM, LITERAL_DEPARTMENT, Action.CREATE, dept.getId(), " " + LITERAL_DEPARTMENT + " " + dept.getName()
                    + "\" in Company \"" + dept.getCompany().getName() + LITERAL_CREATED);
            }

            return dept;
        } else {
            return null;
        }
    }

    private  Address getAddress(Company company, Address adr) {
        Address address = new Address();

        if (adr == null) {
            // Set Company Address as Address of Department.
            Address companyAddress = company.getMainAddress();
            address.setCity(companyAddress.getCity());
            address.setStreet(companyAddress.getStreet());
            address.setHousenumber(companyAddress.getHousenumber());
            address.setZipCode(companyAddress.getZipCode());
            address.setPhone(companyAddress.getPhone());
            address.setFax(companyAddress.getFax());

        } else {
            address.setCity(adr.getCity());
            address.setStreet(adr.getStreet());
            address.setHousenumber(adr.getHousenumber());
            address.setZipCode(adr.getZipCode());
            address.setPhone(adr.getPhone());
            address.setFax(adr.getFax());

        }
        return address;
    }

    private boolean departmentExists(String name, Company company) {
        if (company.getDepartments() != null) {
            for (Department d : company.getDepartments()) {
                if (d.getName().equals(name)) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }


    public void deleteDepartment(int id, User sessionUser) {
        Department managedDepartment = findDepartmentById(id);
        if (authController.canDelete(managedDepartment, sessionUser)) {
            Company managedCompany = em.find(Company.class, managedDepartment.getCompany().getId());

            deletePermissionRecordsWithDepartment(id, sessionUser);
            removeDepartmentFromAffectedUsers(id);
            managedCompany.getDepartments().remove(managedDepartment);

            em.remove(managedDepartment);
            em.flush();

            logger.log(sessionController.getUser().getUsername(), LITERAL_DEPARTMENT, Action.DELETE, id,
                " " + LITERAL_DEPARTMENT + " " + managedDepartment.getName() + "\" deleted.");
        }
    }

    private void deletePermissionRecordsWithDepartment(int departmentId, User sessionUser) {
        if (authController.canDelete(new PermissionRecord(), sessionUser)) {
            Query query = em.createNamedQuery("findPermissionRecordsByDepartment");
            query.setParameter("deptId", departmentId);
            List<PermissionRecord> records = query.getResultList();
            for (PermissionRecord rec : records) {
                List<Role> affectedRoles = getRolesForPermissionRecord(rec.getId());
                for (Role r : affectedRoles) {
                    userRoleController.deletePermissionRecord(r.getId(), rec.getId(), sessionUser);
                    if (r.getPermissions().isEmpty()) {
                        userRoleController.deleteRole(r.getId(), sessionUser);
                    }
                    logger.log(sessionController.getUser().getUsername(), "PermissionRecord", Action.DELETE, r.getId(),
                        " PermissionRecord " + "deleted.");
                }
                em.flush();
            }
        }
    }

    private void removeDepartmentFromAffectedUsers(int departmentId) {
        Query query = em.createNamedQuery("findUserByDepartment");
        query.setParameter("deptId", departmentId);
        List<User> users = query.getResultList();
        for (User u : users) {
            u.setDepartment(null);
        }
        em.flush();
    }

    private List<Role> getRolesForPermissionRecord(int recId) {
        Query query = em.createNamedQuery("findRolesForPermissionRecord");
        query.setParameter("recId", recId);
        return query.getResultList();
    }


    /**
     * Changes the data of a department in the underlying persistence architecture.
     *
     * @param department - the {@link Department} with the new {@link Department} data. The primary key (ID) must be set.
     */
    public void editDepartment(Department department, User sessionUser) {
        Department orig = getDepartmentById(department.getId(), sessionUser);
        if (orig != null && authController.canUpdate(orig, sessionUser)) {
            try {

                orig.setName(department.getName());
                orig.setDescription(department.getDescription());
                Address origAddress = orig.getAddress();
                Address changedAddress = department.getAddress();

                origAddress.setCity(changedAddress.getCity());
                origAddress.setStreet(changedAddress.getStreet());
                origAddress.setHousenumber(changedAddress.getHousenumber());
                origAddress.setZipCode(changedAddress.getZipCode());
                origAddress.setPhone(changedAddress.getPhone());
                origAddress.setFax(changedAddress.getFax());
                em.flush();

                logger.log(sessionUser.getName(), LITERAL_DEPARTMENT, Action.UPDATE, orig.getId(),
                    " Department \"" + orig.getName() + "\" changed.");
            } catch (Exception ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, ex.getMessage());
            }

        }
    }


    public Department getDepartmentById(int departmentId, User sessionUser) {
        Query query = em.createNamedQuery("findDepartmentById");
        query.setParameter(1, departmentId);
        try {
            Department department = (Department) query.getSingleResult();
            if (department != null && authController.canRead(department, sessionUser)) {
                return department;
            } else {
                return null;
            }
        } catch (Exception ex) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public List<Department> getAllDepartments(User sessionUser) {
        if (authController.canRead(AuthorizationController.DEPARTMENT_TYPE, sessionUser)) {
            Query query = em.createNamedQuery("findAllDepartments");
            return query.getResultList();
        } else {
            return new ArrayList<>();
        }
    }

    public Department getDefaultDepartmentInDefaultCompany() {
        Company company = companyController.findCompanyByName("Default Company");
        for (Department d : company.getDepartments()) {
            if (d.getName().equalsIgnoreCase(LITERAL_DEFAULT)) {
                return d;
            }
        }
        return null;
    }

    public Department getDepartmentByToken(String token, User sessionUser) {
        Department dept;
        Query query = em.createNamedQuery("findDepartmentByToken");
        query.setParameter(1, token);
        try {
            dept = (Department) query.getSingleResult();
        } catch (Exception ex) {
            return null;
        }
        if (authController.canRead(dept, sessionUser)) {
            return dept;
        } else {
            return null;
        }

    }


    public Department findDepartmentById(int id) {
        return em.find(Department.class, id);
    }

    public List<Department> findDepartmentsByCompany(int companyId, User sessionUser) {
        Query query = em.createNamedQuery("findDepartmentsByCompany");
        query.setParameter(1, companyId);
        List<Department> departments = query.getResultList();
        if (departments != null && !departments.isEmpty()) {
            if (authController.canRead(departments.get(0), sessionUser)) {
                return departments;
            } else {
                return new ArrayList<>();
            }
        } else {
            return new ArrayList<>();
        }
    }
}
