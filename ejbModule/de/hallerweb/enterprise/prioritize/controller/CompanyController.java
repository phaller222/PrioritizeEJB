package de.hallerweb.enterprise.prioritize.controller;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.enterprise.context.ContextNotActiveException;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import de.hallerweb.enterprise.prioritize.controller.LoggingController.Action;
import de.hallerweb.enterprise.prioritize.controller.security.SessionController;
import de.hallerweb.enterprise.prioritize.controller.security.UserRoleController;
import de.hallerweb.enterprise.prioritize.model.Address;
import de.hallerweb.enterprise.prioritize.model.Company;
import de.hallerweb.enterprise.prioritize.model.Department;
import de.hallerweb.enterprise.prioritize.model.document.DocumentGroup;
import de.hallerweb.enterprise.prioritize.model.resource.ResourceGroup;
import de.hallerweb.enterprise.prioritize.model.security.PermissionRecord;
import de.hallerweb.enterprise.prioritize.model.security.Role;
import de.hallerweb.enterprise.prioritize.model.security.User;

/**
 * CompanyController.java - Controls the creation, modification and deletion of {@link Company} objects. Also the associated {@link Address}
 * and {@link Department} objects are handled here.
 * 
 */
@Stateless
public class CompanyController {

	@PersistenceContext(unitName = "MySqlDS")
	EntityManager em;

	@EJB
	UserRoleController userRoleController;
	@EJB
	LoggingController logger;
	@Inject
	SessionController sessionController;

	public Company createCompany(String name, Address mainAddress) {
		Company c = new Company();
		c.setName(name);
		c.setDescription("");
		c.setMainAddress(em.find(Address.class, mainAddress.getId()));
		em.persist(c);
		em.flush();
		try {
			logger.log(sessionController.getUser().getUsername(), "Company", Action.CREATE, c.getId(),
					" Company \"" + c.getName() + "\" created.");
		} catch (ContextNotActiveException ex) {
			logger.log("SYSTEM", "Company", Action.CREATE, c.getId(), " Company \"" + c.getName() + "\" created.");
		}
		return c;
	}

	public Address createAddress(String street, String zipCode, String city, String phone, String fax) {
		Address adr = new Address();
		adr.setStreet(street);
		adr.setZipCode(zipCode);
		adr.setCity(city);
		adr.setPhone(phone);
		adr.setFax(fax);

		em.persist(adr);
		em.flush();
		try {
			if (sessionController.getUser() != null) {
				logger.log(sessionController.getUser().getUsername(), "Address", Action.CREATE, adr.getId(),
						" New Address \"" + adr.getId() + "\" created.");
			}
		} catch (ContextNotActiveException ex) {
			logger.log("SYSTEM", "Address", Action.CREATE, adr.getId(),
					" New Address \"" + adr.getId() + "\" created.");
		}
		return adr;
	}

	public Department createDepartment(Company company, String name, String description, Address adr, User sessionUser) {
		Department dept = new Department();
		dept.setName(name);
		dept.setDescription(description);
		Company c = em.find(Company.class, company.getId());
		dept.setCompany(c);

		// Set Company Address as Address of Department.
		Address companyAddress = company.getMainAddress();

		Address address = new Address();
		address.setCity(companyAddress.getCity());
		address.setStreet(companyAddress.getStreet());
		address.setZipCode(companyAddress.getZipCode());
		address.setPhone(companyAddress.getPhone());
		address.setFax(companyAddress.getFax());

		DocumentGroup defaultDocumentGroup = new DocumentGroup();
		defaultDocumentGroup.setDepartment(dept);
		defaultDocumentGroup.setName("default");

		ResourceGroup defaultResourceGroup = new ResourceGroup();
		defaultResourceGroup.setDepartment(dept);
		defaultResourceGroup.setName("default");

		em.persist(defaultDocumentGroup);
		em.persist(defaultResourceGroup);

		em.persist(address);

		dept.setAddress(address);
		dept.addDocumentGroup(defaultDocumentGroup);
		dept.addResourceGroup(defaultResourceGroup);

		// Generate token
		if (c.getName().equalsIgnoreCase("Default Company") && (name.equals("default"))) {
			dept.setToken(InitializationController.DEFAULT_DEPARTMENT_TOKEN);
		} else {
			UUID token = UUID.randomUUID();
			dept.setToken(token.toString().replaceAll("-", ""));
		}

		em.persist(dept);
		em.flush();

		// The user who created this department should automatically get all permissions on objects in this department

		Set<PermissionRecord> records = new HashSet<PermissionRecord>();

		PermissionRecord deptDocs = new PermissionRecord(true, true, true, true);
		deptDocs.setTargetResourceType("DocumentInfo");
		deptDocs.setDepartment(dept);
		records.add(deptDocs);

		PermissionRecord deptResources = new PermissionRecord(true, true, true, true);
		deptResources.setTargetResourceType("Resource");
		deptResources.setDepartment(dept);
		records.add(deptResources);

		Role r = userRoleController.createRole(company.getName() + "-" + dept.getName() + "-Admin",
				company.getName() + " - " + dept.getName() + " - Admin", records, sessionUser);

		try {
			userRoleController.addRoleToUser(sessionController.getUser().getId(), r.getId(), sessionUser);
			sessionController.getUser().addRole(r);
		} catch (ContextNotActiveException ex) {
			// If called by initialization process, don't assign role here.
			// ex.printStackTrace();
		}

		// Logging
		try {
			logger.log(sessionController.getUser().getUsername(), "Department", Action.CREATE, c.getId(),
					" Department \"" + dept.getName() + "\" in Company \"" + dept.getCompany().getName()
							+ "\" created.");
		} catch (ContextNotActiveException ex) {
			logger.log("SYSTEM", "Department", Action.CREATE, dept.getId(), " Department \"" + dept.getName()
					+ "\" in Company \"" + dept.getCompany().getName() + "\" created.");
		}

		return dept;
	}

	public void deleteCompany(int id, User sessionUser) {
		Company c = findCompanyById(id);
		List<Department> departments = c.getDepartments();
		List<Department> departmentsToDelete = new ArrayList<Department>();
		for (Department dept : departments) {
			departmentsToDelete.add(dept);
		}
		for (Department d : departmentsToDelete) {
			deleteDepartment(d.getId(), sessionUser);
		}

		em.remove(c);
		em.flush();

		logger.log(sessionController.getUser().getUsername(), "Company", Action.DELETE, c.getId(),
				" Company \"" + c.getName() + "\" and all related Objects deleted.");
	}

	public void deleteDepartment(int id, User sessionUser) {
		Department managedDepartment = findDepartmentById(id);
		Company managedCompany = em.find(Company.class, managedDepartment.getCompany().getId());

		deletePermissionRecordsWithDepartment(id, sessionUser);
		removeDepartmentFromAffectedUsers(id);
		managedCompany.getDepartments().remove(managedDepartment);

		em.remove(managedDepartment);
		em.flush();

		logger.log(sessionController.getUser().getUsername(), "Department", Action.DELETE, id,
				" Department \"" + managedDepartment.getName() + "\" deleted.");
	}

	private void deletePermissionRecordsWithDepartment(int departmentId, User sessionUser) {
		Query query = em.createNamedQuery("findPermissionRecordsByDepartment");
		query.setParameter("deptId", departmentId);
		List<PermissionRecord> records = query.getResultList();
		for (PermissionRecord rec : records) {
			List<Role> affectedRoles = getRolesForPermissionRecord(rec.getId());
			for (Role r : affectedRoles) {
				userRoleController.deletePermissionRecord(r.getId(), rec.getId());
				if (r.getPermissions().isEmpty()) {
					userRoleController.deleteRole(r.getId(), sessionUser);
				}
				logger.log(sessionController.getUser().getUsername(), "PermissionRecord", Action.DELETE, r.getId(),
						" PermissionRecord " + "deleted.");
			}
			em.flush();
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
	 * @param c
	 *            The {@link Company} object with the changed data of the {@link Company}. the primary key must be set.
	 */
	public void editCompany(Company c) {
		Company orig = em.find(Company.class, c.getId());
		if (orig != null) {
			orig.setName(c.getName());
			orig.setDescription(c.getDescription());
			// merge edited Address data
			Address changedAddress = c.getMainAddress();
			Address origAddress = em.find(Address.class, c.getMainAddress().getId());
			origAddress.setCity(changedAddress.getCity());
			origAddress.setStreet(changedAddress.getStreet());
			origAddress.setZipCode(changedAddress.getZipCode());
			origAddress.setPhone(changedAddress.getPhone());
			origAddress.setFax(changedAddress.getFax());
			em.merge(origAddress);

			orig.setMainAddress(origAddress);

			// merge edited Departments data
			if (c.getDepartments() != null && orig.getDepartments() != null) {
				List<Department> origDepts = new ArrayList<Department>();
				for (Department d : c.getDepartments()) {
					Department origDept = em.find(Department.class, d.getId());
					origDept.setCompany(orig);
					origDept.setName(d.getName());
					origDept.setDescription(d.getDescription());
					origDepts.add(origDept);
					em.merge(origDept);
				}
				orig.setDepartments(origDepts);
			} else {
				c.setDepartments(new ArrayList<Department>());
			}
			em.flush();
			logger.log(sessionController.getUser().getUsername(), "Company", Action.UPDATE, c.getId(),
					" Company \"" + c.getName() + "\" changed.");

		} else {
			// Company not found so no edit.
		}
	}

	/**
	 * Changes the data of a department in the underlying persistence architecture.
	 * @param d - the {@link Department} with the new {@link Department} data. The primary key (ID) must be set.
	 */
	public void editDepartment(Department d) throws Exception {
		Department orig = em.find(Department.class, d.getId());
		if (orig != null) {
			orig.setName(d.getName());
			orig.setDescription(d.getDescription());
			Address origAddress = orig.getAddress();
			Address changedAddress = d.getAddress();

			origAddress.setCity(changedAddress.getCity());
			origAddress.setStreet(changedAddress.getStreet());
			origAddress.setZipCode(changedAddress.getZipCode());
			origAddress.setPhone(changedAddress.getPhone());
			origAddress.setFax(changedAddress.getFax());
			em.flush();

			logger.log(sessionController.getUser().getUsername(), "Department", Action.UPDATE, orig.getId(),
					" Department \"" + orig.getName() + "\" changed.");

		} else {
			throw new Exception("Error editing department or Department not found");
			// Something went wrong. Department not found!
		}
	}

	@SuppressWarnings("unchecked")
	public List<Company> getAllCompanies() throws EJBException {
		Query query = em.createNamedQuery("findAllCompanies");
		return query.getResultList();
	}

	public Company getCompanyByName(String name) {
		Query query = em.createNamedQuery("findCompanyByName");
		query.setParameter(1, name);
		try {
			return (Company) query.getSingleResult();
		} catch (NoResultException ex) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public List<Department> getAllDepartments() throws EJBException {
		Query query = em.createNamedQuery("findAllDepartments");
		return query.getResultList();
	}

	public Department getDefaultDepartmentInDefaultCompany() throws EJBException {
		Query query = em.createNamedQuery("findDefaultDepartmentAndCompany");
		return (Department) query.getSingleResult();
	}

	public Department getDepartmentByToken(String token) {
		Query query = em.createNamedQuery("findDepartmentByToken");
		query.setParameter("token", token);
		return (Department) query.getSingleResult();
	}

	/**
	 * Returns a {@link List} of all adresses.
	 * 
	 * @return List<Company> the adresses.
	 * @throws EJBException
	 */
	@SuppressWarnings("unchecked")
	public List<Address> getAllAddresses() throws EJBException {
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
		logger.log(sessionController.getUser().getUsername(), "Address", Action.DELETE, managedAddress.getId(),
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
}
