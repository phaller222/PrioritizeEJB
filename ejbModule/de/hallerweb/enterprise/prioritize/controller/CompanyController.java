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

package de.hallerweb.enterprise.prioritize.controller;

import de.hallerweb.enterprise.prioritize.controller.LoggingController.Action;
import de.hallerweb.enterprise.prioritize.controller.security.AuthorizationController;
import de.hallerweb.enterprise.prioritize.controller.security.SessionController;
import de.hallerweb.enterprise.prioritize.controller.security.UserRoleController;
import de.hallerweb.enterprise.prioritize.model.Address;
import de.hallerweb.enterprise.prioritize.model.Company;
import de.hallerweb.enterprise.prioritize.model.Department;
import de.hallerweb.enterprise.prioritize.model.security.User;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * CompanyController.java - Controls the creation, modification and deletion of {@link Company} objects. Also the associated {@link Address}
 * and {@link Department} objects are handled here.
 */
@Stateless
public class CompanyController {

    @PersistenceContext
    EntityManager em;
    @EJB
    AuthorizationController authController;
    @EJB
    UserRoleController userRoleController;
    @EJB
    LoggingController logger;
    @EJB
    DepartmentController departmentController;
    @Inject
    SessionController sessionController;

    public static final String LITERAL_COMPANY = "Company";
    public static final String LITERAL_DEPARTMENT = "Department";
    public static final String LITERAL_CREATED = "\" created.";
    public static final String LITERAL_ADDRESS = "Address";
    public static final String LITERAL_SYSTEM = "SYSTEM";
    public static final String LITERAL_DEFAULT = "default";

    public Company createCompany(String name, Address mainAddress, User sessionUser) {
        Company c = new Company();
        if (authController.canCreate(c, sessionUser)) {
            c.setName(name);
            c.setDescription("");
            c.setMainAddress(em.find(Address.class, mainAddress.getId()));
            em.persist(c);
            em.flush();
            try {
                logger.log(sessionController.getUser().getUsername(), LITERAL_COMPANY, Action.CREATE, c.getId(),
                    " " + LITERAL_COMPANY + " " + c.getName() + LITERAL_CREATED);
            } catch (Exception ex) {
                logger.log(LITERAL_SYSTEM, LITERAL_COMPANY, Action.CREATE, c.getId(),
                    " " + LITERAL_COMPANY + " " + c.getName() + LITERAL_CREATED);
            }
            return c;
        } else {
            return null;
        }
    }

    public Company createCompany(Company detachedCompany, User sessionUser) {
        Company c = new Company();
        if (authController.canCreate(c, sessionUser)) {
            c.setName(detachedCompany.getName());
            c.setDescription(detachedCompany.getDescription());
            c.setUrl(detachedCompany.getUrl());
            c.setTaxId(detachedCompany.getTaxId());
            c.setVatNumber(detachedCompany.getVatNumber());

            Address adr = new Address();
            Address detachedAddress = detachedCompany.getMainAddress();
            adr.setStreet(detachedAddress.getStreet());
            adr.setHousenumber(detachedAddress.getHousenumber());
            adr.setZipCode(detachedAddress.getZipCode());
            adr.setCity(detachedAddress.getCity());
            adr.setPhone(detachedAddress.getPhone());
            adr.setFax(detachedAddress.getFax());
            adr.setMobile(detachedAddress.getMobile());
            adr.setCountry(detachedAddress.getCountry());

            em.persist(adr);
            em.flush();
            c.setMainAddress(adr);
            em.persist(c);

            try {
                logger.log(sessionController.getUser().getUsername(), LITERAL_COMPANY, Action.CREATE, c.getId(),
                    " " + LITERAL_COMPANY + " " + c.getName() + LITERAL_CREATED);
            } catch (Exception ex) {
                logger.log(LITERAL_SYSTEM, LITERAL_COMPANY, Action.CREATE, c.getId(),
                    " " + LITERAL_COMPANY + " " + c.getName() + LITERAL_CREATED);
            }
            return c;
        } else {
            return null;
        }
    }


    public void deleteCompany(int id, User sessionUser) {
        Company c = findCompanyById(id);
        if (authController.canDelete(c, sessionUser)) {
            List<Department> departments = c.getDepartments();
            List<Department> departmentsToDelete = new ArrayList<>(departments);
            for (Department d : departmentsToDelete) {
                departmentController.deleteDepartment(d.getId(), sessionUser);
            }

            em.remove(c);
            em.flush();
            try {
                logger.log(sessionController.getUser().getUsername(), LITERAL_COMPANY, Action.DELETE, c.getId(),
                    " " + LITERAL_COMPANY + " " + c.getName() + "\" and all related Objects deleted.");
            } catch (Exception ex) {
                logger.log(LITERAL_SYSTEM, LITERAL_COMPANY, Action.DELETE, c.getId(),
                    " " + LITERAL_COMPANY + " " + c.getName() + "\" and all related Objects deleted.");
            }
        }
    }


    /**
     * Edits the {@link Company} data in the underlying persistence architecture. The new data for the {@link Company} like name, Address
     * and so on can be passed here as a detached {@link Company} object. Also changes to the companies departments will be persisted here.
     *
     * @param company The {@link Company} object with the changed data of the {@link Company}. the primary key must be set.
     */
    public void editCompany(Company company, User sessionUser) {
        Company orig = em.find(Company.class, company.getId());
        if (orig != null && authController.canUpdate(orig, sessionUser)) {
            orig.setName(company.getName());
            orig.setDescription(company.getDescription());
            orig.setVatNumber(company.getVatNumber());
            orig.setTaxId(company.getTaxId());
            orig.setUrl(company.getUrl());
            // merge edited Address data
            Address changedAddress = company.getMainAddress();
            Address origAddress = em.find(Address.class, company.getMainAddress().getId());
            origAddress.setCity(changedAddress.getCity());
            origAddress.setStreet(changedAddress.getStreet());
            origAddress.setHousenumber(changedAddress.getHousenumber());
            origAddress.setZipCode(changedAddress.getZipCode());
            origAddress.setPhone(changedAddress.getPhone());
            origAddress.setFax(changedAddress.getFax());
            origAddress.setMobile(changedAddress.getMobile());
            origAddress.setCountry(changedAddress.getCountry());

            em.merge(origAddress);

            orig.setMainAddress(origAddress);

            // merge edited Departments data
            if (company.getDepartments() != null && orig.getDepartments() != null) {
                List<Department> origDepts = new ArrayList<>();
                for (Department d : company.getDepartments()) {
                    Department origDept = em.find(Department.class, d.getId());
                    origDept.setCompany(orig);
                    origDept.setName(d.getName());
                    origDept.setDescription(d.getDescription());
                    origDepts.add(origDept);
                    em.merge(origDept);
                }
                orig.setDepartments(origDepts);
            } else {
                company.setDepartments(new ArrayList<>());
            }
            em.flush();
            try {
                logger.log(sessionController.getUser().getUsername(), LITERAL_COMPANY, Action.UPDATE, company.getId(),
                    " " + LITERAL_COMPANY + " " + company.getName() + "\" changed.");
            } catch (Exception ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, ex.getMessage());
            }
        }
    }


    @SuppressWarnings("unchecked")
    public List<Company> getAllCompanies(User sessionUser) {
        Query query = em.createNamedQuery("findAllCompanies");
        List<Company> companies = query.getResultList();
        return companies.stream().filter(b -> authController.canRead(b, sessionUser)).collect(Collectors.toList());
    }

    public Company findCompanyById(int id) {
        return em.find(Company.class, id);
    }

    public Company findCompanyByName(String name) {
        Query query = em.createNamedQuery("findCompanyByName");
        query.setParameter("name", name);
        try {
            return (Company) query.getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }
}
