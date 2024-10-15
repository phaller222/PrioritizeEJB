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

package de.hallerweb.enterprise.prioritize.view.resource;

import de.hallerweb.enterprise.prioritize.controller.CompanyController;
import de.hallerweb.enterprise.prioritize.controller.resource.MQTTResourceController;
import de.hallerweb.enterprise.prioritize.controller.resource.ResourceController;
import de.hallerweb.enterprise.prioritize.controller.resource.ResourceReservationController;
import de.hallerweb.enterprise.prioritize.controller.security.AuthorizationController;
import de.hallerweb.enterprise.prioritize.controller.security.SessionController;
import de.hallerweb.enterprise.prioritize.controller.usersetting.ItemCollectionController;
import de.hallerweb.enterprise.prioritize.controller.usersetting.UserPreferenceController;
import de.hallerweb.enterprise.prioritize.model.Department;
import de.hallerweb.enterprise.prioritize.model.resource.NameValueEntry;
import de.hallerweb.enterprise.prioritize.model.resource.Resource;
import de.hallerweb.enterprise.prioritize.model.resource.ResourceGroup;
import de.hallerweb.enterprise.prioritize.model.resource.ResourceReservation;
import de.hallerweb.enterprise.prioritize.model.skill.SkillRecord;
import de.hallerweb.enterprise.prioritize.model.usersetting.ItemCollection;
import de.hallerweb.enterprise.prioritize.model.usersetting.UserPreference;
import de.hallerweb.enterprise.prioritize.view.ViewUtilities;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ResourceBean - JSF Backing-Bean to store information about resources.
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
public class ResourceBean implements Serializable {

    private static final long serialVersionUID = -9021544577054017322L;

    @Inject
    SessionController sessionController;
    @EJB
    transient ResourceController resourceController;
    @EJB
    transient MQTTResourceController mqttResourceController;
    @EJB
    transient ResourceReservationController resourceReservationController;
    @EJB
    transient CompanyController companyController;
    @EJB
    transient AuthorizationController authController;
    @EJB
    transient ItemCollectionController itemCollectionController;
    @EJB
    transient UserPreferenceController preferenceController;

    transient Set<Resource> resources;                                // Current List with Resource objects
    transient List<ResourceGroup> resourceGroups;                    // Current list of resource groups within department
    transient List<Department> departments;                        // List of departments
    String selectedDepartmentId;                                    // Currently selected Department
    String selectedResourceGroupId;                                // Currently selected ResourceGroup
    String resourceGroupName;                                        // ResourceGroup to create
    transient Resource resource;                                    // Current Resource to create
    transient ResourceReservation resourceReservation;                // Current Resource Reservation to create
    transient List<ResourceReservation> resourceReservations;        // Current List with Resource Reservations
    Date from;                                                        // FROM-Date of ResourceReservation
    Date until;                                                    // UNTIL-Date of ResourceReservation
    String mqttDataSentAsString = "";                                // Represents the MQTT data sent as String
    String lastMqttValue;
    transient Set<String> mqttCommands;                            // A list of commands a MQTT resource understands (e.g. ON, OFF....)

    transient Department selectedDepartment = null;                // Department to change resource to
    transient Department selectedResourceGroup = null;                // ResourceGroup to change resource to

    transient Set<SkillRecord> skillRecords;

    String selectedItemCollectionName;                                // Selected ItemCollection to add a resource to


    transient Resource currentAgent;                                // If in agent view the currently select agent for viewing its data

    private static final String NAVIGATION_RESOURCERESERVATIONS = "resourcereservations";
    private static final String NAVIGATION_RESOURCES = "resources";

    public Resource getCurrentAgent() {
        return currentAgent;
    }

    public void setCurrentAgent(Resource currentAgent) {
        this.currentAgent = currentAgent;
    }

    public String getSelectedItemCollectionName() {
        return selectedItemCollectionName;
    }

    public void setSelectedItemCollectionName(String selectedItemCollectionName) {
        this.selectedItemCollectionName = selectedItemCollectionName;
    }

    public Department getSelectedDepartment() {
        return selectedDepartment;
    }

    public void setSelectedDepartment(Department selectedDepartment) {
        this.selectedDepartment = selectedDepartment;
    }

    transient ResourceReservation aquiredReservation = null;

    public String getMqttDataSentAsString() {
        return mqttDataSentAsString;
    }

    public void setMqttDataSentAsString(String mqttDataSentAsString) {
        this.mqttDataSentAsString = mqttDataSentAsString;
    }

    public Date getUntil() {
        return until;
    }

    public void setUntil(Date until) {
        this.until = until;
    }

    public Date getFrom() {
        return from;
    }

    public void setFrom(Date from) {
        this.from = from;
    }

    @Named
    public String createResourceReservation() {
        resourceReservationController.createResourceReservation(resource, from, until, sessionController.getUser());
        return NAVIGATION_RESOURCERESERVATIONS;
    }

    @Named
    public String aquireResource(Resource resource) {
        if (this.mqttCommands == null || this.mqttCommands.isEmpty()) {
            this.mqttCommands = mqttResourceController.getMqttCommands(resource, sessionController.getUser());
        }
        Date oneMinute = new Date(System.currentTimeMillis() + 60000L);
        this.aquiredReservation = resourceReservationController.createResourceReservation(resource, new Date(), oneMinute,
            sessionController.getUser());
        return NAVIGATION_RESOURCES;
    }

    @Named
    public String releaseResource(Resource resource) {
        if (aquiredReservation != null) {
            resourceReservationController.removeResourceReservation(aquiredReservation.getId());
        }
        return NAVIGATION_RESOURCES;
    }

    @Named
    public void setResourceReservation(ResourceReservation resourceReservation) {
        this.resourceReservation = resourceReservation;
    }

    public ResourceReservation getResourceReservation() {
        return resourceReservation;
    }

    public List<ResourceReservation> getResourceReservations() {
        if (!this.selectedResourceGroupId.isEmpty()) {
            return resourceReservationController.getResourceReservationsForResourceGroup(Integer.parseInt(this.selectedResourceGroupId));
        } else {
            return new ArrayList<>();
        }
    }

    public String removeReservation(ResourceReservation reservation) {
        resourceReservationController.removeResourceReservation(reservation.getId());
        return NAVIGATION_RESOURCERESERVATIONS;
    }

    public String refreshReservations() {
        return NAVIGATION_RESOURCERESERVATIONS;
    }

    /**
     * Initialize empty {@link Resource} and {@link ResourceGroup}
     */
    @PostConstruct
    public void init() {
        resource = new Resource();
        selectedResourceGroupId = "";

    }

    @Named
    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public Resource getResource() {
        return resource;
    }

    @Named
    public int getSlotsInUse(Resource res) {
        return resourceReservationController.getSlotsInUse(res);
    }

    public Set<Resource> getResources() {
        if ((this.selectedResourceGroupId != null) && (!this.selectedResourceGroupId.isEmpty())) {
            return resourceController.getResourcesInResourceGroup(Integer.parseInt(this.selectedResourceGroupId),
                sessionController.getUser());
        } else {
            return new HashSet<>();
        }
    }

    @Named
    public String createResource() {
        int resourceGroupId = Integer.parseInt(selectedResourceGroupId);

        if (resourceController.createResource(resource, resourceGroupId, sessionController.getUser()) != null) {

        } else {
            ViewUtilities.addErrorMessage("name", "Problems creating resource with the name " + resource.getName()
                + "!");
        }
        return NAVIGATION_RESOURCES;
    }

    @Named
    public void setResourceGroupName(String resourceGroup) {
        this.resourceGroupName = resourceGroup;
    }

    public String getResourceGroupName() {
        return resourceGroupName;
    }

    @Named
    public List<Department> getDepartments() {
        return companyController.getAllDepartments(sessionController.getUser());
    }

    @Named
    public String getSelectedDepartmentId() {
        return selectedDepartmentId;
    }

    public void setSelectedDepartmentId(String departmentId) {
        this.selectedDepartmentId = departmentId;
        if (this.resourceGroups != null) {
            this.resourceGroups.clear();
        }
        if ((departmentId != null) && (departmentId.length() > 0)) {

            this.resourceGroups = resourceController.getResourceGroupsForDepartment(Integer.parseInt(departmentId),
                sessionController.getUser());
            String id = String.valueOf(this.resourceGroups.get(0).getId());
            setSelectedResourceGroup(id);

            this.selectedDepartment = companyController.findDepartmentById(Integer.parseInt(departmentId));

        }
    }

    public String getSelectedResourceGroup() {
        return selectedResourceGroupId;
    }

    @Named
    public void setSelectedResourceGroup(String resourceGroupId) {
        this.selectedResourceGroupId = resourceGroupId;
        if (this.resources != null) {
            this.resources.clear();
        }
        if (this.selectedResourceGroupId != null) {
            this.resources = resourceController.getResourcesInResourceGroup(Integer.parseInt(this.selectedResourceGroupId),
                sessionController.getUser());
        }

    }

    public List<ResourceGroup> getResourceGroups() {
        if ((selectedDepartmentId != null) && (selectedDepartmentId.length() > 0)) {
            return resourceController.getResourceGroupsForDepartment(Integer.parseInt(selectedDepartmentId), sessionController.getUser());
        } else {
            return new ArrayList<>();
        }
    }

    @Named
    public String createResourceGroup() {
        if (resourceController.createResourceGroup(Integer.parseInt(selectedDepartmentId), resourceGroupName,
            sessionController.getUser()) != null) {
            init();
        } else {
            ViewUtilities.addErrorMessage("name", "A resource group with the name " + resourceGroupName + " already exists!");
        }
        return NAVIGATION_RESOURCES;
    }

    public String deleteResourceGroup() {
        resourceController.deleteResourceGroup(
            resourceController.getResourceGroup(Integer.parseInt(this.selectedResourceGroupId), sessionController.getUser()).getId(),
            sessionController.getUser());
        return "documents";
    }

    public String delete(Resource res) {
        resourceController.deleteResource(res.getId(), sessionController.getUser());
        return NAVIGATION_RESOURCES;
    }

    public String delete(Resource res, String resourceGroupId) {
        this.selectedResourceGroupId = resourceGroupId;
        return delete(res);
    }

    /**
     * Calls the reservations for a {@link Resource} object.
     *
     * @param res - {@link Resource} object.
     * @return resourcereservations.xhtml
     */
    public String reservations(Resource res) {
        this.resource = res;
        this.selectedResourceGroupId = String.valueOf(resource.getResourceGroup().getId());
        return NAVIGATION_RESOURCERESERVATIONS;
    }

    /**
     * Calls "editresource" for the given {@link Resource} object.
     *
     * @param resource {@link Resource} object to be edited.
     * @return editresource.xhtml
     */
    @Named
    public String edit(Resource resource) {
        this.resource = resource;
        this.selectedResourceGroupId = String.valueOf(resource.getResourceGroup().getId());
        this.selectedDepartmentId = String.valueOf(resource.getDepartment().getId());
        return "editresource";
    }

    @Named
    public Set<SkillRecord> getSkillRecords() {
        return resourceController.getSkillRecordsForResource(this.resource.getId(), sessionController.getUser());
    }

    public void setSkillRecords(Set<SkillRecord> skillRecords) {
        this.skillRecords = skillRecords;
    }

    public void removeSkillFromResource(SkillRecord skillRecord) {
        resourceController.removeSkillFromResource(skillRecord, resource, sessionController.getUser());
    }

    /**
     * Commit the edits of a resource to the underlying database by using the
     * {@link ResourceController}.
     *
     * @return "resources"
     */
    @Named
    public String commitEdits() {
        selectedDepartment = companyController.findDepartmentById(Integer.parseInt(this.selectedDepartmentId));
        ResourceGroup selectedResourceGroupObject = resourceController.getResourceGroup(Integer.parseInt(this.selectedResourceGroupId),
            sessionController.getUser());
        resourceController.editResource(resource, selectedDepartment, selectedResourceGroupObject, resource.getName(),
            resource.getDescription(), resource.getIp(), resource.isStationary(), resource.isRemote(), resource.getMaxSlots(),
            sessionController.getUser());
        return NAVIGATION_RESOURCES;
    }

    @Named
    public boolean canRead(Resource res) {
        if (res == null) {
            return false;
        }
        return authController.canRead(res, sessionController.getUser());
    }

    @Named
    public boolean canUpdate(Resource res) {
        if (res == null) {
            return false;
        }
        return authController.canUpdate(res, sessionController.getUser());
    }

    @Named
    public boolean canDelete(Resource res) {
        if (res == null) {
            return false;
        }
        return authController.canDelete(res, sessionController.getUser());
    }

    @Named
    public boolean canCreate() {
        try {
            return authController.canCreate(Integer.parseInt(this.selectedDepartmentId), new Resource(), sessionController.getUser());
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    public boolean isMqttResource(Resource res) {
        if (res != null) {
            return res.isMqttResource();
        } else {
            return false;
        }
    }

    public boolean isAgentResource(String valId) {
        if (!valId.isEmpty()) {
            Resource managedResource = resourceController.getResource(Integer.parseInt(valId), sessionController.getUser());
            return managedResource.isAgent();
        }
        return false;
    }

    public boolean isMqttResourceOnline(Resource res) {
        if (res != null) {
            return res.isMqttOnline();
        } else {
            return false;
        }
    }

    @Named
    public void sendDataToDevice(Resource res, String data) {
        if (data != null) {
            mqttResourceController.writeMqttDataToSend(res, data.getBytes());
        }
    }

    @Named
    public Set<NameValueEntry> getNameValuePairs() {
        return mqttResourceController.getNameValueEntries(resource);
    }

    @Named
    public Set<NameValueEntry> getNameValuePairs(Resource res) {
        return mqttResourceController.getNameValueEntries(res);
    }

    @Named
    public String getLastMqttValue(String name) {
        return mqttResourceController.getLastMqttValueForResource(resource, name);
    }

    @Named
    public String getLastMqttValue(Resource res, String name) {
        return mqttResourceController.getLastMqttValueForResource(res, name);
    }

    @Named
    public String getLastMqttValueForResource(Resource res, String name) {
        String value = mqttResourceController.getLastMqttValueForResource(res, name);
        if (value != null && value.contains(",")) {
            return value.split(",")[1];
        } else {
            return "-NO DATA-";
        }
    }

    @Named
    Set<String> getMqttCommands() {
        return mqttCommands;
    }

    @Named
    public void sendCommand(Resource resource, String command, String param) {
        mqttResourceController.sendCommand(resource, command, param);
    }

    @Named
    public void sendCommandFromResource(Resource resource, String command, String param) {
        mqttResourceController.sendCommandToResource(resource, command, param);
    }

    @Named
    public boolean isResourceActiveForCurrentUser(Resource res) {
        return resourceReservationController.getActiveSlotForUser(sessionController.getUser(), res.getReservations()) >= 0;
    }

    public int getActiveSlotForCurrentUser(Resource res) {
        return resourceReservationController.getActiveSlotForUser(sessionController.getUser(), res.getReservations());
    }

    public String getGMapCoordinateParameters() {
        return resource.getLatitude() + "," + resource.getLongitude();
    }

    public String getGMapCoordinateParameters(Resource res) {
        return res.getLatitude() + "," + res.getLongitude();
    }


    @Named
    public void addResourceToItemCollection(Resource resource) {
        ItemCollection managedCollection = itemCollectionController.getItemCollection(sessionController.getUser(),
            selectedItemCollectionName);
        if (managedCollection != null) {
            Resource managedResource = resourceController.getResource(resource.getId(), sessionController.getUser());
            itemCollectionController.addResource(managedCollection, managedResource);
        }
    }


    public boolean isNewRequest() {
        final FacesContext fc = FacesContext.getCurrentInstance();
        final boolean getMethod = ((HttpServletRequest) fc.getExternalContext().getRequest()).getMethod().equals("GET");
        final boolean ajaxRequest = fc.getPartialViewContext().isAjaxRequest();
        final boolean validationFailed = fc.isValidationFailed();
        return getMethod && !ajaxRequest && !validationFailed;
    }

    public String goBack() {
        return NAVIGATION_RESOURCES;
    }

    public String goBackToAgents() {
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        try {
            context.redirect(context.getApplicationContextPath() + "/client/resources/agents.xhtml");
        } catch (IOException e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, e.getMessage(), e);
        }
        return "agents";
    }

    public void updateResourceGroupId(String groupId) {
        this.selectedResourceGroupId = groupId;
    }

    /**
     * Watches or unwatches the given {@link Resource} for the current User.
     *
     * @param res The resource
     */
    public void toggleWatch(Resource res) {
        Resource managedResource = resourceController.getResource(res.getId(), sessionController.getUser());
        UserPreference pref = sessionController.getUser().getPreference();
        if (preferenceController.isResourceWached(pref, managedResource)) {
            preferenceController.deleteWatchedResource(pref, managedResource);
        } else {
            preferenceController.addWatchedResource(pref, managedResource);
        }
    }

    /**
     * Checks is the given resources is beeing watched by the current User.
     *
     * @param res The {@link Resource}
     * @return boolean
     */
    public boolean isWatched(Resource res) {
        return preferenceController.isResourceWached(sessionController.getUser().getPreference(), res);
    }

}
