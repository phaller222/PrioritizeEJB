package de.hallerweb.enterprise.prioritize.controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;

import de.hallerweb.enterprise.prioritize.controller.document.DocumentController;
import de.hallerweb.enterprise.prioritize.controller.event.EventRegistry;
import de.hallerweb.enterprise.prioritize.controller.project.ActionBoardController;
import de.hallerweb.enterprise.prioritize.controller.project.ProjectController;
import de.hallerweb.enterprise.prioritize.controller.project.task.BlackboardController;
import de.hallerweb.enterprise.prioritize.controller.project.task.TaskController;
import de.hallerweb.enterprise.prioritize.controller.security.AuthorizationController;
import de.hallerweb.enterprise.prioritize.controller.security.SessionController;
import de.hallerweb.enterprise.prioritize.controller.security.UserRoleController;
import de.hallerweb.enterprise.prioritize.model.Address;
import de.hallerweb.enterprise.prioritize.model.Company;
import de.hallerweb.enterprise.prioritize.model.Department;
import de.hallerweb.enterprise.prioritize.model.document.Document;
import de.hallerweb.enterprise.prioritize.model.document.DocumentGroup;
import de.hallerweb.enterprise.prioritize.model.document.DocumentInfo;
import de.hallerweb.enterprise.prioritize.model.resource.Resource;
import de.hallerweb.enterprise.prioritize.model.resource.ResourceGroup;
import de.hallerweb.enterprise.prioritize.model.security.PermissionRecord;
import de.hallerweb.enterprise.prioritize.model.security.Role;
import de.hallerweb.enterprise.prioritize.model.security.User;
import de.hallerweb.enterprise.prioritize.model.skill.Skill;
import de.hallerweb.enterprise.prioritize.model.skill.SkillCategory;

/**
 * Session Bean implementation class InitializationController. Singleton implementation and starts on Startup. this initializer bean is used
 * to create the necessary initial data for an admin account and example companies, departments and {@link PermissionRecord} objects.
 */
@Singleton
@LocalBean
@Startup
public class InitializationController {

	@EJB
	UserRoleController userRoleController;
	@EJB
	CompanyController companyController;
	@EJB
	ActionBoardController actionBoardController;
	@EJB
	TaskController taskController;
	@EJB
	ProjectController projectController;
	@EJB
	BlackboardController blackboardController;
	@EJB
	SessionController sessionController;
	@EJB
	DocumentController documentController;
	@Inject
	EventRegistry eventRegistry;

	public static HashMap<String, String> config = new HashMap<String, String>();

	// Deployment configuration keys
	public static final String CREATE_DEFAULT_COMPANY = "CREATE_DEFAULT_COMPANY"; // Create a default company on deployment?
	public static final String CREATE_DEFAULT_DEPARTMENT = "CREATE_DEFAULT_DEPARTMENT"; // Create a default department on deployment?
	public static final String MAXIMUM_FILE_UPLOAD_SIZE = "MAXIMUM_FILE_UPLOAD_SIZE"; // Max.filesize for uploads in bytes
	public static final String ENABLE_MQTT_SERVICE = "ENABLE_MQTT_SERVICE"; // enable IoT MQTT Resource scanning?
	public static final String MQTT_HOST = "MQTT_HOST"; // Host of MQTT Broker service
	public static final String MQTT_PORT = "MQTT_PORT"; // Port of MQTT Broker Service
	public static final String MQTT_HOST_WRITE = "MQTT_HOST_WRITE"; // Host of MQTT Broker service (write commands to)
	public static final String MQTT_PORT_WRITE = "MQTT_PORT_WRITE"; // Port of MQTT Broker Service (write commands to)
	public static final String MQTT_MAX_COMMUNICATION_BYTES = "MQTT_MAX_COMMUNICATION_BYTES"; // Maximum amount of bytes allowed over MQTT
	public static final String MQTT_MAX_VALUES_BYTES = "MQTT_MAX_VALUES_BYTES"; // Max. bytes allowed for device key/value historical data.
	public static final String MQTT_PING_TIMEOUT = "MQTT_PING_TIMEOUT"; // Timeout after which idle mqtt resource are shutdown (ms).
	public static final String MQTT_MAX_DEVICE_VALUES = "MQTT_MAX_DEVICE_VALUES"; // Number of maximum allowed Name/value pair per MQTT
	public static final String DISCOVERY_ALLOW_DEFAULT_DEPARTMENT = "DISCOVERY_ALLOW_DEFAULT_DEPARTMENT"; // Can resources be added without
	// providing department token?
	public static final String EVENT_DEFAULT_TIMEOUT = "EVENT_DEFAULT_TIMEOUT"; // Default timeout value for events
	public static final String EVENT_DEFAULT_STRATEGY = "EVENT_DEFAULT_STRATEGY"; // Default Strategy value for events
	public static final String LISTENER_DEFAULT_TIMEOUT = "LISTENER_DEFAULT_TIMEOUT"; // Default timeout value for event listeners
	public static final String FIRE_RESOURCE_EVENTS = "FIRE_RESOURCE_EVENTS";
	public static final String FIRE_DOCUMENT_EVENTS = "FIRE_DOCUMENT_EVENTS";
	public static final String FIRE_DEPARTMENT_EVENTS = "FIRE_DEPARTMENT_EVENTS";
	public static final String FIRE_USER_EVENTS = "FIRE_USER_EVENTS";
	public static final String FIRE_ACTIONBOARD_EVENTS = "FIRE_ACTIONBOARD_EVENTS";
	public static final String FIRE_TASK_EVENTS = "FIRE_TASK_EVENTS";

	// !!! Use with caution! Admin auto login!
	public static final String ADMIN_AUTO_LOGIN = "ADMIN_AUTO_LOGIN";
	// resource / device.
	public static final String DEFAULT_DEPARTMENT_TOKEN = "09eb3067d0fe446bbe7788218fec9bdd";

	@PostConstruct
	public void initialize() {
		loadConfiguration();
		createAdminAccountIfNotPresent();
	}

	private void loadConfiguration() {

		// Set default values
		config.clear();
		config.put(CREATE_DEFAULT_COMPANY, "true");
		config.put(CREATE_DEFAULT_DEPARTMENT, "true");
		config.put(MAXIMUM_FILE_UPLOAD_SIZE, "50000000");

		config.put(ENABLE_MQTT_SERVICE, "true");
		config.put(MQTT_HOST, "prioritize-iot.com");
		config.put(MQTT_PORT, "1883");
		config.put(MQTT_HOST_WRITE, "prioritize-iot.com");
		config.put(MQTT_PORT_WRITE, "1883");

		config.put(MQTT_MAX_COMMUNICATION_BYTES, "5000");
		config.put(MQTT_MAX_VALUES_BYTES, "20");
		config.put(MQTT_MAX_DEVICE_VALUES, "1");
		config.put(MQTT_PING_TIMEOUT, "60000");

		config.put(DISCOVERY_ALLOW_DEFAULT_DEPARTMENT, "true");

		config.put(EVENT_DEFAULT_TIMEOUT, "120000"); // Default is 2 minutes
		config.put(EVENT_DEFAULT_STRATEGY, "IMMEDIATE"); // Default is IMMEDIATE
		config.put(LISTENER_DEFAULT_TIMEOUT, "120000"); // Default is 2 minutes
		config.put(FIRE_RESOURCE_EVENTS, "true");
		config.put(FIRE_DOCUMENT_EVENTS, "true");
		config.put(FIRE_USER_EVENTS, "true");
		config.put(FIRE_DEPARTMENT_EVENTS, "true");
		config.put(FIRE_ACTIONBOARD_EVENTS, "true");
		config.put(FIRE_TASK_EVENTS, "true");

		config.put(ADMIN_AUTO_LOGIN, "false");

		try {
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(getClass().getResourceAsStream("/META-INF/resources/config.ini")));
			String line;
			while ((line = reader.readLine()) != null) {
				if (!line.isEmpty() && !line.startsWith("#")) {
					String[] parameter = line.split("=");
					config.put(parameter[0].trim(), parameter[1].trim());
				}
			}
			reader.close();
		} catch (Exception ex) {
			Logger.getLogger(this.getClass()).log(Level.WARN, "Could not load configuration. Using default values...");
		}
	}

	public static String get(String name) {
		return config.get(name);
	}

	public static int getAsInt(String name) {
		return Integer.parseInt(config.get(name));
	}

	public static boolean getAsBoolean(String name) {
		return Boolean.parseBoolean(config.get(name));
	}

	private void createAdminAccountIfNotPresent() {
		if (userRoleController.getAllUsers(AuthorizationController.getSystemUser()).size() <= 0) {

			// No user present yet. Create admin user...
			System.out.println("No users present. Assuming clean deployment. recreating admin user...");

			// Create default company and default department
			boolean createDefaultCompany = Boolean.valueOf(config.get(CREATE_DEFAULT_COMPANY));
			Department d = null;
			if (createDefaultCompany) {
				Address adr = new Address();
				adr.setCity("City of Admin");
				adr.setFax("00000-00000");
				adr.setPhone("00000-00000");
				adr.setStreet("Street of Admins");
				adr.setZipCode("00000");
				Company c = companyController.createCompany("Default Company", adr,AuthorizationController.getSystemUser());
				c.setMainAddress(adr);

				if (Boolean.valueOf(config.get(CREATE_DEFAULT_DEPARTMENT))) {
					d = companyController.createDepartment(c, "default", "Auto generated default department", adr,
							AuthorizationController.getSystemUser());
				}
			}

			// Admin will get all permissions on all objects!
			HashSet<PermissionRecord> records = new HashSet<PermissionRecord>();
			
			PermissionRecord adminCompanies = new PermissionRecord(true, true, true, true, Company.class.getCanonicalName());
			PermissionRecord adminDepartments = new PermissionRecord(true, true, true, true, Department.class.getCanonicalName());
			PermissionRecord adminRoles = new PermissionRecord(true, true, true, true, Role.class.getCanonicalName());
			PermissionRecord adminUsers = new PermissionRecord(true, true, true, true, User.class.getCanonicalName());
			PermissionRecord adminPermissions = new PermissionRecord(true, true, true, true, PermissionRecord.class.getCanonicalName());
			
			PermissionRecord adminDocumentGroups = new PermissionRecord(true, true, true, true, DocumentGroup.class.getCanonicalName());
			PermissionRecord adminDocumentInfos = new PermissionRecord(true, true, true, true, DocumentInfo.class.getCanonicalName());
			PermissionRecord adminDocuments = new PermissionRecord(true, true, true, true, Document.class.getCanonicalName());

			PermissionRecord adminResourceGroups = new PermissionRecord(true, true, true, true, ResourceGroup.class.getCanonicalName());
			PermissionRecord adminResources = new PermissionRecord(true, true, true, true, Resource.class.getCanonicalName());

			PermissionRecord adminSkills = new PermissionRecord(true, true, true, true, Skill.class.getCanonicalName());
			PermissionRecord adminSkillCategories = new PermissionRecord(true, true, true, true, SkillCategory.class.getCanonicalName());

			records.add(adminCompanies);
			records.add(adminDepartments);
			records.add(adminRoles);
			records.add(adminUsers);
			records.add(adminPermissions);
			records.add(adminDocumentGroups);
			records.add(adminDocumentInfos);
			records.add(adminDocuments);
			records.add(adminResourceGroups);
			records.add(adminResources);
			records.add(adminSkillCategories);
			records.add(adminSkills);

			Role r = userRoleController.createRole("admin", "admin Role", records, AuthorizationController.getSystemUser());
			HashSet<Role> roles = new HashSet<Role>();
			roles.add(r);

			User admin = userRoleController.createUser("admin", "13rikMyElTw", "admin", "", null, "", roles,
					AuthorizationController.getSystemUser());
			// TODO: Remove admin API-Key!
			admin.setApiKey("ABCDEFG");

			// ---------------------------------------------------------------------------------------------------------------------
			
			// Blackboard bb = new Blackboard();
			// bb.setTitle("My Blackboard");
			// bb.setDescription("This is my first blackboard");
			// bb.setFrozen(false);
		} else {
			System.out.println("Deployment OK.");
		}
	}
}
