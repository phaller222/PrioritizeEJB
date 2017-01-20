package de.hallerweb.enterprise.prioritize.controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

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
import de.hallerweb.enterprise.prioritize.model.project.ActionBoard;
import de.hallerweb.enterprise.prioritize.model.project.Project;
import de.hallerweb.enterprise.prioritize.model.project.goal.ProjectGoal;
import de.hallerweb.enterprise.prioritize.model.project.goal.ProjectGoalCategory;
import de.hallerweb.enterprise.prioritize.model.project.goal.ProjectGoalProperty;
import de.hallerweb.enterprise.prioritize.model.project.goal.ProjectGoalPropertyDocument;
import de.hallerweb.enterprise.prioritize.model.project.goal.ProjectGoalPropertyNumeric;
import de.hallerweb.enterprise.prioritize.model.project.goal.ProjectGoalPropertyRecord;
import de.hallerweb.enterprise.prioritize.model.project.goal.ProjectGoalRecord;
import de.hallerweb.enterprise.prioritize.model.project.task.Blackboard;
import de.hallerweb.enterprise.prioritize.model.project.task.Task;
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
	public static final String EVENT_DEFAULT_STRATEGY = "EVENT_DEFAULT_STRATEGY"; // Default Strategy value for events
	public static final String LISTENER_DEFAULT_TIMEOUT = "LISTENER_DEFAULT_TIMEOUT"; // Default timeout value for event listeners
	public static final String FIRE_RESOURCE_EVENTS = "FIRE_RESOURCE_EVENTS";
	public static final String FIRE_DOCUMENT_EVENTS = "FIRE_DOCUMENT_EVENTS";
	public static final String FIRE_DEPARTMENT_EVENTS = "FIRE_DEPARTMENT_EVENTS";
	public static final String FIRE_USER_EVENTS = "FIRE_USER_EVENTS";
	public static final String FIRE_ACTIONBOARD_EVENTS = "FIRE_ACTIONBOARD_EVENTS";
	public static final String FIRE_TASK_EVENTS = "FIRE_TASK_EVENTS";

	// !!! Use with caution! Admin auto login!
	public final static String ADMIN_AUTO_LOGIN = "ADMIN_AUTO_LOGIN";
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
			Department d = null;
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
					d = companyController.createDepartment(c, "default", "Auto generated default department", adr,
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

			User admin = userRoleController.createUser("admin", "13rikMyElTw", "admin", "", null, "", roles,
					AuthorizationController.getSystemUser());

			// TODO: Remove admin API-Key!
			admin.setApiKey("ABCDEFG");

			// TODO: Test implementation, REMOVE!
			eventRegistry.createEventListener(admin, admin, "name", 120000, true);

			// NOW PART OF CREATEUSER!!!!
//			ActionBoard adminBoard = actionBoardController.createActionBoard("admin", "Admin's board", admin);
//			actionBoardController.addSubscriber(adminBoard.getId(), admin);

			// TODO: Test implementation, REMOVE!
			// Task task = new Task();
			// task.setName("My demo task");
			// task.setDescription("This is my first test task");
			// task.setPriority(1);
			// task.setTaskStatus(TaskStatus.ASSIGNED);
			//
			// Task subtask = new Task();
			// subtask.setName("My demo subtask");
			// subtask.setDescription("This is my first test subtask");
			// subtask.setPriority(1);
			// subtask.setTaskStatus(TaskStatus.ASSIGNED);
			//
			// Task managedSubTask = taskController.createTask(subtask);
			// taskController.addTaskAssignee(managedSubTask.getId(), admin);
			//
			// Task managedTask = taskController.createTask(task);
			// taskController.addTaskAssignee(managedTask.getId(), admin);
			//
			// taskController.addSubTask(managedTask, managedSubTask);
			//
			// eventRegistry.createEventListener(managedTask, admin, "blackboard", 30000, false);
			//
			Blackboard bb = new Blackboard();
			bb.setTitle("My Blackboard");
			bb.setDescription("This is my first blackboard");
			bb.setFrozen(false);
			//
			// Blackboard managedBlackboard = blackboardController.createBlackboard(bb);
			// blackboardController.putTaskToBlackboard(managedTask.getId(), managedBlackboard.getId());

			// ------------- TEST Project --------------------------------------
			Project project = new Project();
			project.setName("Testproject");
			project.setDescription("Testbeschreibung");
			project.setBeginDate(new Date());
			project.setDueDate(new Date(new Date().getTime() + 3000000));
			project.setManager(userRoleController.findRoleByRolename("admin", admin));
			project.setMaxManDays(20);
			project.setPriority(1);
			
			// project.setBlackboard(managedBlackboard);
			Project managedProject = projectController.createProject(project, bb, new ArrayList<Task>());
			// -------------------------------------------------------------------------

			// --------------TEST Project Goals ---------------------------------------
			ProjectGoalPropertyNumeric property = new ProjectGoalPropertyNumeric();
			property.setName("Nominalumsatz");
			property.setDescription("Nominalumsatz im Unternehmen");
			property.setMin(10000);
			property.setMax(30000);

			ProjectGoalPropertyDocument property2 = new ProjectGoalPropertyDocument();
			property2.setName("Finale Spezifikation");
			property2.setDescription("Feinspezifikation komplett");
			property2.setTag("FINAL");

			List<ProjectGoalProperty> properties = new ArrayList<ProjectGoalProperty>();
			properties.add(property);
			properties.add(property2);

			ProjectGoalPropertyRecord propRecord = new ProjectGoalPropertyRecord();
			propRecord.setProperty(property);
			propRecord.setValue(5000);
			propRecord.setNumericPropertyRecord(true);

			ProjectGoalPropertyRecord propRecord2 = new ProjectGoalPropertyRecord();
			propRecord2.setProperty(property2);
			DocumentGroup temp = documentController.createDocumentGroup(d.getId(), "test", admin);
			DocumentInfo info = documentController.createDocument("ttt", temp.getId(), admin, "text/plain", false, new byte[] {}, "none");
			Document document = info.getCurrentDocument();
			document.setTag("FINAL");
			documentController.editDocument(info, document, "1212".getBytes(), "text/plain", admin, false);
			propRecord2.setDocumentInfo(info);
			propRecord2.setDocumentPropertyRecord(true);

			ProjectGoalCategory cat = projectController.createProjectGoalCategory("Financial", "Financial project goals", null);
			ProjectGoal goal = projectController.createProjectGoal("Umsatzsteigerung", "Wir brauchen mehr Umsatz!", cat, 
					properties, admin);
			// new ProjectGoal();
			goal.setCategory(cat);
			property2.setProjectGoal(goal);

			ProjectGoalRecord goalRecord = new ProjectGoalRecord();
			goalRecord.setPropertyRecord(propRecord);
			goalRecord.setProject(managedProject);
			goalRecord.setProjectGoal(goal);

			ProjectGoalRecord goalRecord2 = new ProjectGoalRecord();
			goalRecord2.setPropertyRecord(propRecord2);
			goalRecord2.setProject(managedProject);
			goalRecord2.setProjectGoal(goal);

			List<ProjectGoalRecord> projectGoalRecords = new ArrayList<ProjectGoalRecord>();
			projectGoalRecords.add(goalRecord);
			projectGoalRecords.add(goalRecord2);

			// // Create initial ProjectProgress
			// ProjectProgress managedProgress = projectController.createProjectProgress(project.getId(), projectGoalRecords, 0);
			// managedProject.setProgress(managedProgress);
			//
			// // Update project progress and create tasks
			// for (ProjectGoalRecord recOrig : project.getProgress().getTargetGoals()) {
			// ProjectGoalRecord updatedRecord = projectController.activateProjectGoal(recOrig.getId());
			// updatedRecord.getPropertyRecord().setValue(9800);
			// updatedRecord.getPropertyRecord().setDocumentInfo(info);
			// }
			//
			// projectController.updateProjectProgress(managedProject.getId());
			// System.out.println("-------------- Ergebnis: ---- " + managedProgress.getProgress());

			// ------------------------------------------------------------------

		} else {
			System.out.println("Deployment OK.");
		}
	}

}
