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

import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;

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
import de.hallerweb.enterprise.prioritize.model.security.PAuthorizedObject;
import de.hallerweb.enterprise.prioritize.model.security.PermissionRecord;
import de.hallerweb.enterprise.prioritize.model.security.Role;
import de.hallerweb.enterprise.prioritize.model.security.User;
import de.hallerweb.enterprise.prioritize.model.skill.Skill;
import de.hallerweb.enterprise.prioritize.view.security.RoleBean;

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
	SessionController sessionController;

	public static HashMap<String, String> config = new HashMap<String, String>();

	// Deployment configuration keys
	public final static String CREATE_DEFAULT_COMPANY = "CREATE_DEFAULT_COMPANY"; // Create a default company on deployment?
	public final static String CREATE_DEFAULT_DEPARTMENT = "CREATE_DEFAULT_DEPARTMENT"; // Create a default department on deployment?
	public final static String MAXIMUM_FILE_UPLOAD_SIZE = "MAXIMUM_FILE_UPLOAD_SIZE"; // Max.filesize for uploads in bytes
	public final static String ENABLE_MQTT_SERVICE = "ENABLE_MQTT_SERVICE"; // enable IoT MQTT Resource scanning?
	public final static String MQTT_HOST = "MQTT_HOST"; // Host of MQTT Broker service
	public final static String MQTT_PORT = "MQTT_PORT"; // Port of MQTT Broker Service
	public final static String MQTT_HOST_WRITE = "MQTT_HOST_WRITE"; // Host of MQTT Broker service (write commands to)
	public final static String MQTT_PORT_WRITE = "MQTT_PORT_WRITE"; // Port of MQTT Broker Service (write commands to)
	public final static String MQTT_MAX_COMMUNICATION_BYTES = "MQTT_MAX_COMMUNICATION_BYTES"; // Maximum amount of bytes allowed over MQTT
	public final static String MQTT_MAX_VALUES_BYTES = "MQTT_MAX_VALUES_BYTES"; // Max. bytes allowed for device key/value historical data.
	public final static String MQTT_PING_TIMEOUT = "MQTT_PING_TIMEOUT"; // Timeout after which idle mqtt resource are shutdown (ms).
	public final static String MQTT_MAX_DEVICE_VALUES = "MQTT_MAX_DEVICE_VALUES"; // Number of maximum allowed Name/value pair per MQTT
	public final static String DISCOVERY_ALLOW_DEFAULT_DEPARTMENT = "DISCOVERY_ALLOW_DEFAULT_DEPARTMENT"; // Can resources be added without
																											 // providing department token?
	public static final String EVENT_DEFAULT_TIMEOUT = "EVENT_DEFAULT_TIMEOUT"; // Default timeout value for events
	public static final String LISTENER_DEFAULT_TIMEOUT = "LISTENER_DEFAULT_TIMEOUT"; // Default timeout value for event listeners
	public static final String FIRE_RESOURCE_EVENTS="FIRE_RESOURCE_EVENTS";
	public static final String FIRE_DOCUMENT_EVENTS="FIRE_DOCUMENT_EVENTS";
	public static final String FIRE_DEPARTMENT_EVENTS="FIRE_DEPARTMENT_EVENTS";
	public static final String FIRE_USER_EVENTS="FIRE_USER_EVENTS";
	
	
	// resource / device.
	public final static String DEFAULT_DEPARTMENT_TOKEN = "09eb3067d0fe446bbe7788218fec9bdd";

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
		config.put(MQTT_HOST_WRITE, "steamrunner.info");
		config.put(MQTT_PORT_WRITE, "1883");

		config.put(MQTT_MAX_COMMUNICATION_BYTES, "5000");
		config.put(MQTT_MAX_VALUES_BYTES, "20");
		config.put(MQTT_MAX_DEVICE_VALUES, "1");
		config.put(MQTT_PING_TIMEOUT, "60000");

		config.put(DISCOVERY_ALLOW_DEFAULT_DEPARTMENT, "true");
		
		config.put(EVENT_DEFAULT_TIMEOUT, "120000"); // Default is 2 minutes
		config.put(LISTENER_DEFAULT_TIMEOUT, "120000"); // Default is 2 minutes
		config.put(FIRE_RESOURCE_EVENTS, "true"); 
		config.put(FIRE_DOCUMENT_EVENTS, "true"); 
		config.put(FIRE_USER_EVENTS, "true"); 
		config.put(FIRE_DEPARTMENT_EVENTS, "true"); 

		try {
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(getClass().getResourceAsStream("/META-INF/resources/config.ini")));
			String line;
			while ((line = reader.readLine()) != null) {
				if (!line.isEmpty() && !line.startsWith("#")) {
					System.out.println(line);
					String[] parameter = line.split("=");
					System.out.println("Setting " + parameter[0] + " to " + parameter[1]);
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

		RoleBean.authorizedObjects = new HashMap<String, Class<? extends PAuthorizedObject>>();
		RoleBean.authorizedObjects.put(DocumentInfo.class.getSimpleName(), DocumentInfo.class);
		RoleBean.authorizedObjects.put(Skill.class.getSimpleName(), Skill.class);
		RoleBean.authorizedObjects.put(DocumentGroup.class.getSimpleName(), DocumentGroup.class);
		RoleBean.authorizedObjects.put(User.class.getSimpleName(), User.class);
		RoleBean.authorizedObjects.put(Role.class.getSimpleName(), Role.class);
		RoleBean.authorizedObjects.put(Resource.class.getSimpleName(), Resource.class);
		RoleBean.authorizedObjects.put(ResourceGroup.class.getSimpleName(), ResourceGroup.class);
		RoleBean.authorizedObjects.put(Company.class.getSimpleName(), Company.class);
		if (userRoleController.getAllUsers(AuthorizationController.getSystemUser()).size() <= 0) {

			// No user present yet. Create admin user...
			System.out.println("No users present. Assuming clean deployment. recreating admin user...");

			// Create default company and default department
			boolean createDefaultCompany = Boolean.valueOf(config.get(CREATE_DEFAULT_COMPANY));

			if (createDefaultCompany) {
				Address adr = new Address();
				adr.setCity("City of Admin");
				adr.setFax("00000-00000");
				adr.setPhone("00000-00000");
				adr.setStreet("Street of Admins");
				adr.setZipCode("00000");
				Company c = companyController.createCompany("Default Company", adr);
				c.setMainAddress(adr);

				if (Boolean.valueOf(config.get(CREATE_DEFAULT_DEPARTMENT))) {
					Department d = companyController.createDepartment(c, "default", "Auto generated default department", adr,
							AuthorizationController.getSystemUser());
				}
			}

			HashSet<PermissionRecord> records = new HashSet<PermissionRecord>();
			PermissionRecord adminDocuments = new PermissionRecord(true, true, true, true, null);
			PermissionRecord adminSkills = new PermissionRecord(true, true, true, true, Skill.class);
			records.add(adminDocuments);
			records.add(adminSkills);

			Role r = userRoleController.createRole("admin", "admin Role", records, AuthorizationController.getSystemUser());

			HashSet<Role> roles = new HashSet<Role>();
			roles.add(r);

			User admin = userRoleController.createUser("admin", "admin", "admin", "", null, "", roles,
					AuthorizationController.getSystemUser());
			admin.setApiKey("ABCDEFG");

		} else {
			System.out.println("Deployment OK.");
		}
	}

}
