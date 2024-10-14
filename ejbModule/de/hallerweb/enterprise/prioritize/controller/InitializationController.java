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

import de.hallerweb.enterprise.prioritize.controller.security.AuthorizationController;
import de.hallerweb.enterprise.prioritize.controller.security.UserRoleController;
import de.hallerweb.enterprise.prioritize.model.Address;
import de.hallerweb.enterprise.prioritize.model.Company;
import de.hallerweb.enterprise.prioritize.model.Department;
import de.hallerweb.enterprise.prioritize.model.document.Document;
import de.hallerweb.enterprise.prioritize.model.document.DocumentGroup;
import de.hallerweb.enterprise.prioritize.model.document.DocumentInfo;
import de.hallerweb.enterprise.prioritize.model.nfc.counter.IndustrieCounter;
import de.hallerweb.enterprise.prioritize.model.project.task.TimeTracker;
import de.hallerweb.enterprise.prioritize.model.resource.Resource;
import de.hallerweb.enterprise.prioritize.model.resource.ResourceGroup;
import de.hallerweb.enterprise.prioritize.model.security.PermissionRecord;
import de.hallerweb.enterprise.prioritize.model.security.Role;
import de.hallerweb.enterprise.prioritize.model.security.User;
import de.hallerweb.enterprise.prioritize.model.skill.Skill;
import de.hallerweb.enterprise.prioritize.model.skill.SkillCategory;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    AuthorizationController authController;

    static final String LITERAL_ADMIN = "admin";
    static final String LITERAL_LOCALHOST = "localhost";
    static final String FALSE = "false";
    static final String TRUE = "true";

    // Deployment configuration keys
    public static final String CREATE_DEFAULT_COMPANY = "CREATE_DEFAULT_COMPANY"; // Create a default company on deployment?
    public static final String CREATE_DEFAULT_DEPARTMENT = "CREATE_DEFAULT_DEPARTMENT"; // Create a default department on deployment?
    public static final String CREATE_DEFAULT_APIKEY = "CREATE_DEFAULT_APIKEY"; // Create a default REST-api key for admin for development
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
    public static final String MQTT_USERNAME = "MQTT_USERNAME"; // Username to access mqtt broker
    public static final String MQTT_PASSWORD = "MQTT_PASSWORD"; // Password to access mqtt broker
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

    // resource / device.
    public static final String DEFAULT_DEPARTMENT_TOKEN = "09eb3067d0fe446bbe7788218fec9bdd";

    // Use KEycloak as authorization server
    public static final String USE_KEYCLOAK_AUTH = "USE_KEYCLOAK_AUTH";
    // Keycloak logout URL
    public static final String KEYCLOAK_LOGOUT_URL = "KEYCLOAK_LOGOUT_URL";

    protected static final Map<String, String> config = new HashMap<>();

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
        config.put(CREATE_DEFAULT_APIKEY, "false");
        config.put(MAXIMUM_FILE_UPLOAD_SIZE, "50000000");

        config.put(ENABLE_MQTT_SERVICE, "false");
        config.put(MQTT_HOST, LITERAL_LOCALHOST);
        config.put(MQTT_PORT, "1883");
        config.put(MQTT_HOST_WRITE, LITERAL_LOCALHOST);
        config.put(MQTT_PORT_WRITE, "1883");

        config.put(MQTT_MAX_COMMUNICATION_BYTES, "5000");
        config.put(MQTT_MAX_VALUES_BYTES, "20");
        config.put(MQTT_MAX_DEVICE_VALUES, "1");
        config.put(MQTT_PING_TIMEOUT, "60000");

        config.put(MQTT_USERNAME, LITERAL_LOCALHOST);
        config.put(MQTT_PASSWORD, "");

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

        config.put(USE_KEYCLOAK_AUTH, "false");
        config.put(KEYCLOAK_LOGOUT_URL,
            "https://localhost:8443/auth/realms/master/protocol/openid-connect/logout?"
                + "redirect_uri=https://localhost/PrioritizeWeb/client/dashboard/dashboard.xhtml");


        // Read config.properties and update configuration
        try {
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(getClass().getResourceAsStream("/META-INF/resources/config.properties")));
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isEmpty() && !line.startsWith("#")) {
                    String[] parameter = line.split("=");
                    config.put(parameter[0].trim(), parameter[1].trim());
                }
            }
            reader.close();
        } catch (Exception ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Could not load configuration. Using default values...", ex);
        }

        // Add all standard objects which can be protected by PermissionRecord entries.
        authController.addObservedObjectType(Company.class.getCanonicalName());
        authController.addObservedObjectType(Department.class.getCanonicalName());
        authController.addObservedObjectType(User.class.getCanonicalName());
        authController.addObservedObjectType(Role.class.getCanonicalName());
        authController.addObservedObjectType(PermissionRecord.class.getCanonicalName());

        authController.addObservedObjectType(DocumentInfo.class.getCanonicalName());
        authController.addObservedObjectType(DocumentGroup.class.getCanonicalName());
        authController.addObservedObjectType(Resource.class.getCanonicalName());
        authController.addObservedObjectType(ResourceGroup.class.getCanonicalName());
        authController.addObservedObjectType(Skill.class.getCanonicalName());
        authController.addObservedObjectType(SkillCategory.class.getCanonicalName());

        authController.addObservedObjectType(IndustrieCounter.class.getCanonicalName());
        authController.addObservedObjectType(TimeTracker.class.getCanonicalName());
    }

    public String get(String name) {
        return config.get(name);
    }

    public int getAsInt(String name) {
        return Integer.parseInt(config.get(name));
    }

    public boolean getAsBoolean(String name) {
        return Boolean.parseBoolean(config.get(name));
    }

    public void createAdminAccountIfNotPresent() {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO,
            "Checking if admin user exists...");
        User adminUser = userRoleController.findUserByUsername(LITERAL_ADMIN, authController.getSystemUser());

        // If no admin user can be found the system had not been started and configured before
        // The admin-User and the initial minimum number of objects like the default company and
        // department objects must be created. admin user gets the default password "admin".
        // ATTENTION: admin user password should be changed as soon as possible!

        if (Objects.isNull(adminUser)) {
            Department d = null;
            Logger.getLogger(this.getClass().getName()).log(Level.INFO,
                "No admin user present. recreating admin user...");


            // If configured, a default company and default department is created.
            boolean createDefaultCompany = Boolean.parseBoolean(config.get(CREATE_DEFAULT_COMPANY));
            if (createDefaultCompany) {

                Address adr = new Address();
                adr.setCity("City of Admin");
                adr.setFax("00000-00000");
                adr.setPhone("00000-00000");
                adr.setStreet("Street of Admins");
                adr.setZipCode("00000");
                Company company = companyController.createCompany("Default Company", adr, authController.getSystemUser());
                company.setMainAddress(adr);

                // If configured, a default department is created for the company.
                if (Boolean.parseBoolean(config.get(CREATE_DEFAULT_DEPARTMENT))) {
                    d = companyController.createDepartment(company, "default", "Auto generated default department", adr,
                        authController.getSystemUser());
                }
            }

            // Admin will get all permissions on all objects!
            // A PermissionRecord-Set is used to store that information.
            HashSet<PermissionRecord> records = new HashSet<>();

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
            PermissionRecord adminTimeTracker = new PermissionRecord(true, true, true, true, TimeTracker.class.getCanonicalName());
            PermissionRecord adminCounter = new PermissionRecord(true, true, true, true, IndustrieCounter.class.getCanonicalName());

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
            records.add(adminTimeTracker);
            records.add(adminCounter);

            // A Role "admin" must be created to hold the PermissionRecord.
            Role r = userRoleController.createRole(LITERAL_ADMIN, "admin Role", records, authController.getSystemUser());
            userRoleController.setRoleDepartment(r, d);
            Set<Role> roles = new HashSet<>();
            roles.add(r);

            // Now we create the admin user including his/her address.
            User admin = new User();
            admin.setName(LITERAL_ADMIN);
            admin.setUsername(LITERAL_ADMIN);
            admin.setEmail("nobody@localhost");
            admin.setPassword(LITERAL_ADMIN);
            admin.setOccupation("Systemadministrator");

            Address adr = new Address();
            adr.setCity("default");
            adr.setZipCode("00000");
            adr.setStreet("default street");
            adr.setFax("0");
            adr.setPhone("0");
            adr.setHousenumber("0");
            adr.setCountry("DE");
            adr.setMobile("0");
            admin.setAddress(adr);

            if (Boolean.parseBoolean(config.get(CREATE_DEFAULT_APIKEY))) {
                admin.setApiKey("ABCDEFG");
                Logger.getLogger(this.getClass().getName()).log(Level.INFO, "--- ATTENTION: DEFAULT API-KEY HAS BEEN CREATED: ABCDEFG. DON'T USE THIS"
                    + " INSTALLATION IN PRODUCTION! ---");

            }

            // Finally the admin user is created with the admin-role including all possible permissions.
            userRoleController.createUser(admin, null, roles, authController.getSystemUser());

        } else {
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "--------->  Admin-User present.Deployment OK.");
        }
    }
}
