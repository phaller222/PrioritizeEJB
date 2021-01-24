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
import de.hallerweb.enterprise.prioritize.controller.event.EventRegistry;
import de.hallerweb.enterprise.prioritize.controller.security.AuthorizationController;
import de.hallerweb.enterprise.prioritize.controller.security.SessionController;
import de.hallerweb.enterprise.prioritize.controller.security.UserRoleController;
import de.hallerweb.enterprise.prioritize.model.Address;
import de.hallerweb.enterprise.prioritize.model.Company;
import de.hallerweb.enterprise.prioritize.model.Department;
import de.hallerweb.enterprise.prioritize.model.PObject;
import de.hallerweb.enterprise.prioritize.model.document.DocumentGroup;
import de.hallerweb.enterprise.prioritize.model.document.DocumentInfo;
import de.hallerweb.enterprise.prioritize.model.event.Event;
import de.hallerweb.enterprise.prioritize.model.event.PEventConsumerProducer;
import de.hallerweb.enterprise.prioritize.model.resource.Resource;
import de.hallerweb.enterprise.prioritize.model.resource.ResourceGroup;
import de.hallerweb.enterprise.prioritize.model.security.PermissionRecord;
import de.hallerweb.enterprise.prioritize.model.security.Role;
import de.hallerweb.enterprise.prioritize.model.security.User;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.enterprise.context.ContextNotActiveException;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * CompanyController.java - Controls the creation, modification and deletion of {@link Company} objects. Also the associated {@link Address}
 * and {@link Department} objects are handled here.
 *
 */
@Stateless
public class CompanyController extends PEventConsumerProducer {

	@PersistenceContext
	EntityManager em;
	@EJB
	AuthorizationController authController;
	@EJB
	UserRoleController userRoleController;
	@EJB
	LoggingController logger;
	@EJB
	InitializationController initController;
	@Inject
	SessionController sessionController;
	@Inject
	EventRegistry eventRegistry;

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

	public Address createAddress(String street, String zipCode, String city, String phone, String fax, String mobile) {
		Address adr = new Address();
		adr.setStreet(street);
		adr.setZipCode(zipCode);
		adr.setCity(city);
		adr.setPhone(phone);
		adr.setFax(fax);
		adr.setMobile(mobile);

		em.persist(adr);
		em.flush();
		try {
			if (sessionController.getUser() != null) {
				logger.log(sessionController.getUser().getUsername(), LITERAL_ADDRESS, Action.CREATE, adr.getId(),
						" New Address \"" + adr.getId() + LITERAL_CREATED);
			}
		} catch (ContextNotActiveException ex) {
			logger.log(LITERAL_SYSTEM, LITERAL_ADDRESS, Action.CREATE, adr.getId(), " New Address \"" + adr.getId() + LITERAL_CREATED);
		}
		return adr;
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
			adr.setZipCode(detachedAddress.getZipCode());
			adr.setCity(detachedAddress.getCity());
			adr.setPhone(detachedAddress.getPhone());
			adr.setFax(detachedAddress.getFax());
			adr.setMobile(detachedAddress.getMobile());
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
			Address address = new Address();

			if (adr == null) {
				// Set Company Address as Address of Department.
				Address companyAddress = company.getMainAddress();

				address.setCity(companyAddress.getCity());
				address.setStreet(companyAddress.getStreet());
				address.setZipCode(companyAddress.getZipCode());
				address.setPhone(companyAddress.getPhone());
				address.setFax(companyAddress.getFax());

			} else {
				address.setCity(adr.getCity());
				address.setStreet(adr.getStreet());
				address.setZipCode(adr.getZipCode());
				address.setPhone(adr.getPhone());
				address.setFax(adr.getFax());

			}

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
				dept.setToken(token.toString().replaceAll("-", ""));
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

	public void deleteCompany(int id, User sessionUser) {
		Company c = findCompanyById(id);
		if (authController.canDelete(c, sessionUser)) {
			List<Department> departments = c.getDepartments();
			List<Department> departmentsToDelete = new ArrayList<>(departments);
			for (Department d : departmentsToDelete) {
				deleteDepartment(d.getId(), sessionUser);
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
	 * Edits the {@link Company} data in the underlying persistence architecture. The new data for the {@link Company} like name, Address
	 * and so on can be passed here as a detached {@link Company} object. Also changes to the companies departments will be persisted here.
	 *
	 * @param company
	 *            The {@link Company} object with the changed data of the {@link Company}. the primary key must be set.
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
			origAddress.setZipCode(changedAddress.getZipCode());
			origAddress.setPhone(changedAddress.getPhone());
			origAddress.setFax(changedAddress.getFax());
			origAddress.setMobile(changedAddress.getMobile());
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

	/**
	 * Changes the data of a department in the underlying persistence architecture.
	 * @param department - the {@link Department} with the new {@link Department} data. The primary key (ID) must be set.
	 */
	public void editDepartment(Department department, User sessionUser) {
		Department orig = getDepartmentById(department.getId(), sessionUser);
		if (orig != null && authController.canUpdate(orig, sessionUser)) {
			try {
				// Fire events if configured
				if (initController.getAsBoolean(InitializationController.FIRE_DEPARTMENT_EVENTS)) {
					if (!orig.getName().equals(department.getName())) {
						this.raiseEvent(orig, Department.PROPERTY_NAME, orig.getName(), department.getName(),
								initController.getAsInt(InitializationController.EVENT_DEFAULT_TIMEOUT));
					}
					if (!orig.getAddress().equals(department.getAddress())) {
						this.raiseEvent(orig, Department.PROPERTY_ADDRESS, orig.getAddress().toString(), department.getAddress().toString(),
								initController.getAsInt(InitializationController.EVENT_DEFAULT_TIMEOUT));
					}
					if (!orig.getDescription().equals(department.getDescription())) {
						this.raiseEvent(orig, Department.PROPERTY_DESCRIPTION, orig.getDescription(), department.getDescription(),
								initController.getAsInt(InitializationController.EVENT_DEFAULT_TIMEOUT));
					}
				}

				orig.setName(department.getName());
				orig.setDescription(department.getDescription());
				Address origAddress = orig.getAddress();
				Address changedAddress = department.getAddress();

				origAddress.setCity(changedAddress.getCity());
				origAddress.setStreet(changedAddress.getStreet());
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

	@SuppressWarnings("unchecked")
	public List<Company> getAllCompanies(User sessionUser) {
		Query query = em.createNamedQuery("findAllCompanies");
		List<Company> companies = query.getResultList();
		return companies.stream().filter(b -> authController.canRead(b, sessionUser)).collect(Collectors.toList());
	}

	public Company getCompanyByName(String name, User sessionUser) {
		Query query = em.createNamedQuery("findCompanyByName");
		query.setParameter(1, name);
		try {
			Company company = (Company) query.getSingleResult();
			if (company != null && authController.canRead(company, sessionUser)) {
				return company;
			} else {
				return null;
			}
		} catch (NoResultException ex) {
			return null;
		}
	}

	public Company getCompanyById(int companyId, User sessionUser) {
		Query query = em.createNamedQuery("findCompanyById");
		query.setParameter(1, companyId);
		try {
			Company company = (Company) query.getSingleResult();
			if (company != null && authController.canRead(company, sessionUser)) {
				return company;
			} else {
				return null;
			}
		} catch (NoResultException ex) {
			return null;
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
		return findDepartmentById(initController.getDefaultDepartmentId());
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

	/**
	 * Returns a {@link List} of all adresses.
	 *
	 * @return List<Company> the adresses.
	 * @throws EJBException
	 */
	@SuppressWarnings("unchecked")
	public List<Address> getAllAddresses() {
		Query query = em.createNamedQuery("findAllAddresses");
		return query.getResultList();
	}

	/**
	 * Deletes the {@link Address} with the given ID.
	 *
	 * @param id
	 *            - The primary key (int) of the {@link Department} to be deleted.
	 */
	public void deleteAddress(int id) {
		Address managedAddress = findAddressById(id);
		em.remove(managedAddress);
		logger.log(sessionController.getUser().getUsername(), LITERAL_ADDRESS, Action.DELETE, managedAddress.getId(),
				" Address \"" + managedAddress.getId() + "\" deleted.");
	}

	public Company findCompanyById(int id) {
		return em.find(Company.class, id);
	}

	public Address findAddressById(int id) {
		return em.find(Address.class, id);
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

	public void raiseEvent(PObject source, String name, String oldValue, String newValue, long lifetime) {
		if (initController.getAsBoolean(InitializationController.FIRE_DEPARTMENT_EVENTS)) {
			Event evt = eventRegistry.getEventBuilder().newEvent().setSource(source).setOldValue(oldValue).setNewValue(newValue)
					.setPropertyName(name).setLifetime(lifetime).getEvent();
			eventRegistry.addEvent(evt);
		}
	}

	@Override
	public void consumeEvent(PObject destination, Event evt) {
		Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Object " + evt.getSource() + " raised event: " + evt.getPropertyName()
				+ " with new Value: " + evt.getNewValue() + "--- Dept listening: " + destination.getClass());

	}

}
