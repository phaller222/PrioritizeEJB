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
import de.hallerweb.enterprise.prioritize.model.Company;
import de.hallerweb.enterprise.prioritize.model.Department;
import de.hallerweb.enterprise.prioritize.model.resource.NameValueEntry;
import de.hallerweb.enterprise.prioritize.model.resource.Resource;
import de.hallerweb.enterprise.prioritize.model.resource.ResourceGroup;
import de.hallerweb.enterprise.prioritize.model.resource.ResourceReservation;
import de.hallerweb.enterprise.prioritize.model.skill.SkillRecord;
import de.hallerweb.enterprise.prioritize.model.usersetting.ItemCollection;
import de.hallerweb.enterprise.prioritize.model.usersetting.UserPreference;
import de.hallerweb.enterprise.prioritize.view.ViewUtilities;
import org.primefaces.event.NodeCollapseEvent;
import org.primefaces.event.NodeExpandEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.primefaces.model.chart.*;
import org.primefaces.model.map.DefaultMapModel;
import org.primefaces.model.map.LatLng;
import org.primefaces.model.map.MapModel;
import org.primefaces.model.map.Marker;
import org.primefaces.model.mindmap.DefaultMindmapNode;
import org.primefaces.model.mindmap.MindmapNode;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
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
	ResourceController resourceController;
	@EJB
	MQTTResourceController mqttResourceController;
	@EJB
	ResourceReservationController resourceReservationController;
	@EJB
	CompanyController companyController;
	@EJB
	AuthorizationController authController;
	@EJB
	ItemCollectionController itemCollectionController;
	@EJB
	UserPreferenceController preferenceController;

	transient Set<Resource> resources; 								// Current List with Resource objects
	transient List<ResourceGroup> resourceGroups; 					// Current list of resource groups within department
	transient List<Department> departments; 						// List of departments
	String selectedDepartmentId; 									// Currently selected Department
	String selectedResourceGroupId; 								// Currently selected ResourceGroup
	String resourceGroupName; 										// ResourceGroup to create
	transient Resource resource; 									// Current Resource to create
	transient ResourceReservation resourceReservation; 				// Current Resource Reservation to create
	transient List<ResourceReservation> resourceReservations; 		// Current List with Resource Reservations
	Date from; 														// FROM-Date of ResourceReservation
	Date until; 													// UNTIL-Date of ResourceReservation
	String mqttDataSentAsString = ""; 								// Represents the MQTT data sent as String
	String lastMqttValue;
	transient Set<String> mqttCommands; 							// A list of commands a MQTT resource understands (e.g. ON, OFF....)
	LineChartModel valueModel; 										// LineChartModel for the primefaces <gmap> tag (Resource location).

	private transient MindmapNode selectedNode; 					// selectedNode for agent mindmap.

	transient Department selectedDepartment = null; 				// Department to change resource to
	transient Department selectedResourceGroup = null; 				// ResourceGroup to change resource to

	transient Set<SkillRecord> skillRecords;

	String selectedItemCollectionName;								// Selected ItemCollection to add a resource to

	transient TreeNode<Object> resourceTreeRoot;							// Tree for resources
	transient TreeNode<Object> agentTreeRoot;								// Tree for agent resources

	transient Resource currentAgent;								// If in agent view the currently select agent for viewing its data

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
		this.resourceTreeRoot = createResourceTree();
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
			updateResourceTree();
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
	 * @param res
	 *            - {@link Resource} object.
	 * @return
	 */
	public String reservations(Resource res) {
		this.resource = res;
		this.selectedResourceGroupId = String.valueOf(resource.getResourceGroup().getId());
		return NAVIGATION_RESOURCERESERVATIONS;
	}

	/**
	 * Calls "editresource" for the given {@link Resource} object.
	 * 
	 * @param resource
	 *            {@link Resource} object to be edited.
	 * @return
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
	public LineChartModel getValueModel(NameValueEntry entry) {
		createValueModel(entry);
		return valueModel;
	}

	/**
	 * Returns the model for a JSF(Primefaces) line chart to represent
	 * historical data of a resource's NamedValue.
	 * 
	 * @param entry
	 */
	private void createValueModel(NameValueEntry entry) {
		valueModel = new LineChartModel();

		LineChartSeries values = new LineChartSeries();
		values.setFill(true);
		values.setLabel(entry.getName());

		String[] propertyValues = entry.getValues().split(";");

		float max = 0.0f;
		float min = 1000.0f;
		for (String value : propertyValues) {
			String[] entryValue = value.split(",");
			float fValue = Float.parseFloat(entryValue[1]);
			Date d = new Date(Long.parseLong(entryValue[0]));
			if (fValue > max) {
				max = fValue;
			}
			if (fValue < min) {
				min = fValue;
			}
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(d);
			String hourString = calendar.get(Calendar.HOUR) < 10 ? "0" + calendar.get(Calendar.HOUR) : "" + calendar.get(Calendar.HOUR);
			String minuteString = calendar.get(Calendar.MINUTE) < 10 ? "0" + calendar.get(Calendar.MINUTE)
					: "" + calendar.get(Calendar.MINUTE);
			String secondString = calendar.get(Calendar.SECOND) < 10 ? "0" + calendar.get(Calendar.SECOND)
					: "" + calendar.get(Calendar.SECOND);

			values.set(hourString + ":" + minuteString + ":" + secondString, fValue);
		}

		valueModel.addSeries(values);
		valueModel.setTitle("Values");
		valueModel.setShadow(true);
		valueModel.setMouseoverHighlight(true);
		valueModel.setShowDatatip(true);
		valueModel.setZoom(true);
		valueModel.setShowPointLabels(true);
		valueModel.setTitle(entry.getName());

		Axis xAxis = new CategoryAxis("Time");
		valueModel.getAxes().put(AxisType.X, xAxis);
		Axis yAxis = valueModel.getAxis(AxisType.Y);
		yAxis.setLabel("Value");
		yAxis.setMin(min);
		yAxis.setMax(max);
	}

	/**
	 * Returns a MapModel for a Primefaces <gmap> tag. Basically the coordinates
	 * (Latitude / Longitude) are returned. This method checks if
	 * latitude/longitude information is available and creates a Marker based on
	 * that information.
	 * 
	 * @return
	 */
	public MapModel getResourcesMapModel() {
		MapModel simpleModel = new DefaultMapModel();

		// Shared coordinates
		LatLng coord;
		if ((resource.getLatitude() != null) && (resource.getLongitude() != null)) {
			try {
				coord = new LatLng(Float.parseFloat(resource.getLatitude()), Float.parseFloat(resource.getLongitude()));

				// Basic marker
				simpleModel.addOverlay(new Marker(coord, resource.getName()));
			} catch (Exception ex) {
				Logger.getLogger(getClass().getName()).log(Level.SEVERE, ex.getMessage(), ex);
			}

		}

		return simpleModel;
	}

	public MindmapNode getNetworkRoot() {
		List<Resource> allResources = resourceController.getAllResources(sessionController.getUser());
		if (allResources != null) {
			MindmapNode root = new DefaultMindmapNode("Origin", "Root", "C0C0FF", false);
			for (Resource res : allResources) {
				if (authController.canRead(res, sessionController.getUser()) && res.isAgent()) {
					root.addNode(
							new DefaultMindmapNode(res.getName() + " " + res.getIp(), res, res.isMqttOnline() ? "00C000" : "C0C0C0", true));
				}
			}
			return root;
		} else {
			return new DefaultMindmapNode("No department selected");
		}
	}

	public MindmapNode getSelectedNode() {
		return selectedNode;
	}

	public void setSelectedNode(MindmapNode selectedNode) {
		this.selectedNode = selectedNode;
	}

	public void onNodeDblselect(SelectEvent<Object> event) {
		this.selectedNode = (MindmapNode) event.getObject();
	}

	@Named
	public String getSelectedNodeData() {
		if (this.selectedNode != null) {
			Resource res = (Resource) this.selectedNode.getData();
			Set<NameValueEntry> data = res.getMqttValues();
			StringBuilder currentEntry = new StringBuilder();
			for (NameValueEntry entry : data) {
				String values = entry.getValues();
				currentEntry.append(entry.getName()).append(" : ").append(values.substring(values.lastIndexOf(',')))
						.append("\n");
			}
			return currentEntry.toString();
		} else {
			return "";
		}
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

	// --------------------------------- Client view ---------------------------------

	public TreeNode<Object> getResourceTree() {
		return this.resourceTreeRoot;
	}

	public TreeNode<Object> getAgentTree() {
		return this.agentTreeRoot;
	}

	// Create Tree for resources view
	public TreeNode<Object> createResourceTree() {
		TreeNode<Object> root = new DefaultTreeNode<>("My Devices", null);

		List<Company> companies = companyController.getAllCompanies(sessionController.getUser());
		for (Company company : companies) {
			TreeNode<Object> companyTreeNode = new DefaultTreeNode<>(new ResourceTreeInfo(company.getName(), false, false, null, null), root);
			List<Department> companyDepartments = company.getDepartments();
			for (Department d : companyDepartments) {
				TreeNode<Object> department = new DefaultTreeNode<>(new ResourceTreeInfo(d.getName(), false, false, null, null), companyTreeNode);
				Set<ResourceGroup> groups = d.getResourceGroups();
				for (ResourceGroup g : groups) {
					buildGroupResourceSubtree(department, g);
				}
			}
		}
		return root;
	}

	private void buildGroupResourceSubtree(TreeNode<Object> department, ResourceGroup resourceGroup) {
		if (authController.canRead(resourceGroup, sessionController.getUser())) {
			TreeNode<Object> groupTreeNode;
			if (authController.canCreate(resourceGroup, sessionController.getUser())) {
				groupTreeNode = new DefaultTreeNode<>(
						new ResourceTreeInfo(resourceGroup.getName(), false, true, String.valueOf(resourceGroup.getId()), null),
						department);
			} else {
				groupTreeNode = new DefaultTreeNode<>(new ResourceTreeInfo(resourceGroup.getName(), false, false, null, null), department);
			}
			Set<Resource> resourcesInGroup = resourceGroup.getResources();
			for (Resource res : resourcesInGroup) {
				if (!res.isAgent() && authController.canRead(res, sessionController.getUser())) {
					new DefaultTreeNode<>(new ResourceTreeInfo(res.getName(), true, false, null, res), groupTreeNode);
				}
			}
		}
	}

	// Create Tree for resources view
	public TreeNode<Object> createAgentTree() {
		TreeNode<Object> root = new DefaultTreeNode<>("My Agents", null);

		List<Company> companies = companyController.getAllCompanies(sessionController.getUser());
		for (Company c : companies) {
			TreeNode<Object> company = new DefaultTreeNode<>(new ResourceTreeInfo(c.getName(), false, false, null, null), root);
			List<Department> companyDepartments = c.getDepartments();
			for (Department d : companyDepartments) {
				TreeNode<Object> department = new DefaultTreeNode<>(new ResourceTreeInfo(d.getName(), false, false, null, null), company);
				Set<ResourceGroup> groups = d.getResourceGroups();
				for (ResourceGroup g : groups) {
					buildGroupAgentSubtree(department, g);
				}

			}
		}
		return root;
	}

	private void buildGroupAgentSubtree(TreeNode<Object> department, ResourceGroup resourceGroup) {
		if (authController.canRead(resourceGroup, sessionController.getUser())) {
			TreeNode<Object> groupTreeNode;
			if (authController.canCreate(resourceGroup, sessionController.getUser())) {
				groupTreeNode = new DefaultTreeNode<>(
						new ResourceTreeInfo(resourceGroup.getName(), false, true, String.valueOf(resourceGroup.getId()), null),
						department);
			} else {
				groupTreeNode = new DefaultTreeNode<>(new ResourceTreeInfo(resourceGroup.getName(), false, false, null, null), department);
			}
			Set<Resource> groupResources = resourceGroup.getResources();
			for (Resource res : groupResources) {
				if (res.isAgent() && authController.canRead(res, sessionController.getUser())) {
					new DefaultTreeNode<Object>(new ResourceTreeInfo(res.getName(), true, false, null, res), groupTreeNode);
				}
			}

		}
	}

	public void updateResourceTree() {
		if (isNewRequest()) {
			this.resourceTreeRoot = createResourceTree();
		}
	}

	public void updateAgentTree() {
		if (isNewRequest()) {
			this.agentTreeRoot = createAgentTree();
		}
	}

	public void nodeExpand(NodeExpandEvent event) {
		event.getTreeNode().setExpanded(true);
	}

	public void nodeCollapse(NodeCollapseEvent event) {
		event.getTreeNode().setExpanded(false);
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
	 * @param res
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
	 * @param res The {@link Resource}
	 * @return boolean
	 */
	public boolean isWatched(Resource res) {
		return preferenceController.isResourceWached(sessionController.getUser().getPreference(), res);
	}

}
