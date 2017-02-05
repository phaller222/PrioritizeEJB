package de.hallerweb.enterprise.prioritize.view;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;

import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;

import de.hallerweb.enterprise.prioritize.controller.CompanyController;
import de.hallerweb.enterprise.prioritize.controller.InitializationController;
import de.hallerweb.enterprise.prioritize.controller.event.EventRegistry;
import de.hallerweb.enterprise.prioritize.controller.security.AuthorizationController;
import de.hallerweb.enterprise.prioritize.controller.security.SessionController;
import de.hallerweb.enterprise.prioritize.controller.security.UserRoleController;
import de.hallerweb.enterprise.prioritize.model.Address;
import de.hallerweb.enterprise.prioritize.model.Company;
import de.hallerweb.enterprise.prioritize.model.Department;
import de.hallerweb.enterprise.prioritize.model.event.PObjectType;
import de.hallerweb.enterprise.prioritize.model.security.User;

/**
 * CompanyBean - JSF Backing-Bean to store session information about companies and departments associated with them. this bean is mainly
 * used in the following JSF pages:
 * 
 * companies.xhtml editcompany.xhtml editdepartment.xhtml
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
@Named
@SessionScoped
public class CompanyBean implements Serializable {

	@EJB
	CompanyController controller;

	@Inject
	SessionController sessionController;

	@EJB
	AuthorizationController authController;

	Company company; 													// Current company
	Department department; 												// current department

	/**
	 * Initialize empty {@link Department} and {@link Company}
	 */
	@PostConstruct
	public void init() {
		company = new Company();
		Address adr = new Address();
		company.setName(" ");
		company.setMainAddress(adr);

		department = new Department();
		department.setAddress(adr);

	}

	private void initDepartment() {
		Address adr = new Address();
		department = new Department();
		department.setAddress(adr);
	}

	@Produces
	@Named
	public List<Company> getCompanies() {
		return controller.getAllCompanies(sessionController.getUser());
	}

	@Named
	public Company getCompany() {
		return company;
	}

	@Named
	public void setCompany(Company company) {
		this.company = company;
	}

	@Named
	public Department getDepartment() {
		return department;
	}

	@Named
	public void setDepartment(Department dept) {
		this.department = dept;
	}

	@Named
	public String createCompany() {
		if (controller.getCompanyByName(company.getName(), sessionController.getUser()) == null) {
			Address address = company.getMainAddress();
			Address adr = controller.createAddress(address.getStreet(), address.getZipCode(), address.getCity(), address.getPhone(),
					address.getFax());

			Company createdCompany = controller.createCompany(company.getName(), adr,sessionController.getUser());
			controller.createDepartment(createdCompany, "default", "Auto generated default department", adr, sessionController.getUser());

			return "companies";
		} else {
			ViewUtilities.addErrorMessage("name",
					"A company with name " + company.getName() + " already exists. Company has not been created!");
			return "companies";
		}
	}

	@Named
	public String createDepartment() {
		Department createdDepartment = controller.createDepartment(company, department.getName(), department.getDescription(),
				department.getAddress(), sessionController.getUser());
		if (createdDepartment == null) {
			ViewUtilities.addErrorMessage("messages",
					"The department " + department.getName() + " already exists or could not be created. Department has not been created!");
			return "editcompanies";
		}
		company.addDepartment(createdDepartment);
		department = new Department();
		return "editcompanies";
	}

	@Named
	public String delete(Company company) {
		controller.deleteCompany(company.getId(), sessionController.getUser());
		init();
		return "companies";
	}

	@Named
	public String deleteDepartment(Department department) {
		company.getDepartments().remove(department);
		controller.deleteDepartment(department.getId(), sessionController.getUser());
		return "editcompany";
	}

	/**
	 * Prepares Edits for the given {@link Company}
	 * 
	 * @param c
	 *            {@link Company} object to be edited.
	 * @return "editcompany".
	 */
	@Named
	public String edit(Company company) {
		this.company = company;
		initDepartment();
		return "editcompany";

	}

	/**
	 * Saves the edits for a {@link Company} object.
	 * 
	 * @return "companies"
	 */
	@Named
	public String save() {
		controller.editCompany(company, sessionController.getUser());
		init();
		return "companies";
	}

	/**
	 * Prepares Edits for the given {@link Department}.
	 * 
	 * @param d
	 *            {@link Department} to be edited.
	 * @return "editdepartment"
	 */
	@Named
	public String editDepartment(Department department) {
		this.department = department;
		return "editdepartment";
	}

	/**
	 * Saves the edited data for a {@link Department}.
	 * 
	 * @return "editcompany"
	 */
	@Named
	public String saveDepartment() {
		try {
			controller.editDepartment(department, sessionController.getUser());
		} catch (Exception ex) {
			Logger.getLogger(CompanyBean.class).log(Level.ERROR, "Could not edit Department");
		}
		initDepartment();
		return "editcompany";
	}

	@Named
	public String createDummyData() {

		return "companies";

	}

	@Named
	public boolean canRead(Company company) {
		if (company == null) {
			return false;
		}
		return authController.canRead(company, sessionController.getUser());
	}

	@Named
	public boolean canUpdate(Company company) {
		if (company == null) {
			return false;
		}
		return authController.canUpdate(company, sessionController.getUser());
	}

	@Named
	public boolean canDelete(Company company) {
		if (company == null) {
			return false;
		}
		return authController.canDelete(company, sessionController.getUser());
	}

	@Named
	public boolean canCreate() {
		return authController.canCreate(-1, AuthorizationController.COMPANY_TYPE, sessionController.getUser());

	}
}
