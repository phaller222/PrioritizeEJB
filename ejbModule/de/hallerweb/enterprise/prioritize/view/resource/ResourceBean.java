package de.hallerweb.enterprise.prioritize.view.resource;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.primefaces.event.NodeCollapseEvent;
import org.primefaces.event.NodeExpandEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.CategoryAxis;
import org.primefaces.model.chart.LineChartModel;
import org.primefaces.model.chart.LineChartSeries;
import org.primefaces.model.diagram.Connection;
import org.primefaces.model.diagram.DefaultDiagramModel;
import org.primefaces.model.diagram.DiagramModel;
import org.primefaces.model.diagram.Element;
import org.primefaces.model.diagram.connector.FlowChartConnector;
import org.primefaces.model.diagram.endpoint.DotEndPoint;
import org.primefaces.model.diagram.endpoint.EndPointAnchor;
import org.primefaces.model.map.DefaultMapModel;
import org.primefaces.model.map.LatLng;
import org.primefaces.model.map.MapModel;
import org.primefaces.model.map.Marker;
import org.primefaces.model.mindmap.DefaultMindmapNode;
import org.primefaces.model.mindmap.MindmapNode;

import de.hallerweb.enterprise.prioritize.controller.CompanyController;
import de.hallerweb.enterprise.prioritize.controller.resource.ResourceController;
import de.hallerweb.enterprise.prioritize.controller.security.AuthorizationController;
import de.hallerweb.enterprise.prioritize.controller.security.SessionController;
import de.hallerweb.enterprise.prioritize.controller.usersetting.ItemCollectionController;
import de.hallerweb.enterprise.prioritize.model.Company;
import de.hallerweb.enterprise.prioritize.model.Department;
import de.hallerweb.enterprise.prioritize.model.resource.NameValueEntry;
import de.hallerweb.enterprise.prioritize.model.resource.Resource;
import de.hallerweb.enterprise.prioritize.model.resource.ResourceGroup;
import de.hallerweb.enterprise.prioritize.model.resource.ResourceReservation;
import de.hallerweb.enterprise.prioritize.model.skill.SkillRecord;
import de.hallerweb.enterprise.prioritize.model.usersetting.ItemCollection;
import de.hallerweb.enterprise.prioritize.view.ViewUtilities;

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
@ManagedBean
public class ResourceBean implements Serializable {

	private static final long serialVersionUID = -9021544577054017322L;

	@Inject
	SessionController sessionController; 							// Reference to SessionController EJB
	@EJB
	ResourceController resourceController; 							// Reference to ResourceController EJB
	@EJB
	CompanyController companyController; 							// Reference to CompanyController EJB
	@EJB
	AuthorizationController authController; 						// Reference to AuthorizationController EJB
	@EJB
	ItemCollectionController itemCollectionController; 				// Reference to ItemCollectionController EJB

	Set<Resource> resources; 										// Current List with Resource objects
	List<ResourceGroup> resourceGroups; 							// Current list of resource groups within department
	List<Department> departments; 									// List of departments
	String selectedDepartmentId; 									// Currently selected Department
	String selectedResourceGroupId; 								// Currently selected ResourceGroup
	String resourceGroupName; 										// ResourceGroup to create
	Resource resource; 												// Current Resource to create
	ResourceReservation resourceReservation; 						// Current Resource Reservation to create
	List<ResourceReservation> resourceReservations; 				// Current List with Resource Reservations
	Date from; 														// FROM-Date of ResourceReservation
	Date until; 													// UNTIL-Date of ResourceReservation
	String mqttDataSentAsString = ""; 								// Represents the MQTT data sent as String
	String lastMqttValue;
	Set<String> mqttCommands; 										// A list of commands a MQTT resource understands (e.g. ON, OFF....)
	LineChartModel valueModel; 										// LineChartModel for the primefaces <gmap> tag (Resource location).

	private MindmapNode root; 										// RootNode for agent mindmap.
	private MindmapNode selectedNode; 								// selectedNode for agent mindmap.

	Department selectedDepartment = null; 							// Department to change resource to
	Department selectedResourceGroup = null; 						// ResourceGroup to change resource to

	Set<SkillRecord> skillRecords;

	String selectedItemCollectionName;								// Selected ItemCollection to add a resource to

	TreeNode resourceTreeRoot;										// Tree for resources
	TreeNode agentTreeRoot;											// Tree for agent resources
	
	Resource currentAgent;											// If in agent view the currently select agent for viewing its data

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

	ResourceReservation aquiredReservation = null;

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
		resourceController.createResourceReservation(resource, from, until, sessionController.getUser());
		return "resourcereservations";
	}

	@Named
	public String aquireResource(Resource resource) {
		if (this.mqttCommands == null || this.mqttCommands.isEmpty()) {
			this.mqttCommands = resourceController.getMqttCommands(resource, sessionController.getUser());
		}
		Date oneMinute = new Date(System.currentTimeMillis() + 60000L);
		this.aquiredReservation = resourceController.createResourceReservation(resource, new Date(), oneMinute,
				sessionController.getUser());
		return "resources";
	}

	@Named
	public String releaseResource(Resource resource) {
		if (aquiredReservation != null) {
			resourceController.removeResourceReservation(aquiredReservation.getId());
		}
		return "resources";
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
			return resourceController.getResourceReservationsForResourceGroup(Integer.parseInt(this.selectedResourceGroupId));
		} else
			return null;
	}

	public String removeReservation(ResourceReservation reservation) {
		resourceController.removeResourceReservation(reservation.getId());
		return "resourcereservations";
	}

	public String refreshReservations() {
		return "resourcereservations";
	}

	/**
	 * Initialize empty {@link Resource} and {@link ResourceGroup}
	 */
	@PostConstruct
	public void init() {
		resource = new Resource();
		selectedResourceGroupId = new String();
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
		return resourceController.getSlotsInUse(res);
	}

	public Set<Resource> getResources() {
		if ((this.selectedResourceGroupId != null) && (!this.selectedResourceGroupId.isEmpty())) {
			return resourceController.getResourcesInResourceGroup(Integer.parseInt(this.selectedResourceGroupId),
					sessionController.getUser());
		} else
			return null;
	}

	@Named
	public String createResource() {
		int resourceGroupId = Integer.parseInt(selectedResourceGroupId);
		System.out.println("---------------------------   " + sessionController.getUser() + "----------------");
		if (resourceController.createResource(resource.getName(), resourceGroupId, sessionController.getUser(), resource.getDescription(),
				resource.getIp(), resource.getMaxSlots(), resource.isStationary(), resource.isRemote()) != null) {
			updateResourceTree();
			return "resources";
		} else {
			ViewUtilities.addErrorMessage("name", "A resource with the name " + resource.getName()
					+ " already exists in this resource group. Please change name or select a different Resource Group!");
			return "resources";
		}
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
		return companyController.getAllDepartments();
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

			this.resourceGroups = resourceController.getResourceGroupsForDepartment(Integer.parseInt(departmentId));
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
			List<ResourceGroup> groups = resourceController.getResourceGroupsForDepartment(Integer.parseInt(selectedDepartmentId));
			return groups;
		} else
			return new ArrayList<ResourceGroup>();
	}

	@Named
	public String createResourceGroup() {
		if (resourceController.createResourceGroup(Integer.parseInt(selectedDepartmentId), resourceGroupName,
				sessionController.getUser()) != null) {
			init();
			return "resources";
		} else {
			ViewUtilities.addErrorMessage("name", "A resource group with the name " + resourceGroupName + " already exists!");
			return "resource";
		}
	}

	public String deleteResourceGroup() {
		resourceController.deleteResourceGroup(
				resourceController.getResourceGroup(Integer.parseInt(this.selectedResourceGroupId), sessionController.getUser()).getId(),
				sessionController.getUser());
		return "documents";
	}

	public String delete(Resource res) {
		resourceController.deleteResource(res.getId());
		return "resources";
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
		return "resourcereservations";
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
		return "resources";
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
			return authController.canCreate(Integer.parseInt(this.selectedDepartmentId), Resource.class, sessionController.getUser());
		} catch (NumberFormatException ex) {
			return false;
		}
	}

	public boolean isMqttResource(Resource res) {
		if (res != null) {
			return res.isMqttResource();
		} else
			return false;
	}

	public boolean isAgentResource(String valId) {
		if (!valId.isEmpty()) {
			Resource managedResource = resourceController.getResource(Integer.parseInt(valId), sessionController.getUser());
			System.out.println("Resource: " + managedResource.getName() + " AGENT: " + managedResource.isAgent());
			return managedResource.isAgent();
		} else {
			System.out.println("Resource is null. return false");
		}
		return false;
	}

	public boolean isMqttResourceOnline(Resource res) {
		if (res != null) {
			return res.isMqttOnline();
		} else
			return false;
	}

	@Named
	public void sendDataToDevice(Resource res, String data) {
		System.out.println("Data: " + data);
		System.out.println("Resource: " + res.getId());
		if (data != null) {
			resourceController.writeMqttDataToSend(res, data.getBytes());
		}
	}

	@Named
	public Set<NameValueEntry> getNameValuePairs() {
		return resourceController.getNameValueEntries(resource);
	}

	@Named
	public String getLastMqttValue(String name) {
		return resourceController.getLastMqttValueForResource(resource, name);
	}

	@Named
	public String getLastMqttValueForResource(Resource res, String name) {
		String value = resourceController.getLastMqttValueForResource(res, name);
		if (value != null && value.contains(",")) {
			return value.split(",")[1];
		} else
			return "-NO DATA-";
	}

	@Named
	Set<String> getMqttCommands() {
		return mqttCommands;
	}

	@Named
	public void sendCommand(Resource resource, String command, String param) {
		System.out.println("Sending command: " + command + " " + param + "...");
		resourceController.sendCommand(resource, command, param);
	}

	@Named
	public void sendCommandFromResource(Resource resource, String command, String param) {
		System.out.println("Sending command: " + command + " " + param + "...");
		resourceController.sendCommandToResource(resource, command, param);
	}

	@Named
	public boolean isResourceActiveForCurrentUser(Resource res) {
		return resourceController.getActiveSlotForUser(sessionController.getUser(), res.getReservations()) >= 0;
	}

	public int getActiveSlotForCurrentUser(Resource res) {
		return resourceController.getActiveSlotForUser(sessionController.getUser(), res.getReservations());
	}

	public String getGMapCoordinateParameters() {
		return resource.getLatitude() + "," + resource.getLongitude();
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
			Calendar calendar = GregorianCalendar.getInstance();
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
				coord = new LatLng(Float.valueOf(resource.getLatitude()), Float.valueOf(resource.getLongitude()));

				// Basic marker
				simpleModel.addOverlay(new Marker(coord, resource.getName()));
			} catch (Exception ex) {
				ex.printStackTrace();
			}

		}

		return simpleModel;
	}

	public MindmapNode getNetworkRoot() {
		List<Resource> resources = resourceController.getAllResources(sessionController.getUser());
		if (resources != null) {
			MindmapNode root = new DefaultMindmapNode("Origin", "Root", "C0C0FF", false);
			for (Resource res : resources) {
				if (authController.canRead(res, sessionController.getUser())) {
					if (res.isAgent()) {
						root.addNode(new DefaultMindmapNode(res.getName() + " " + res.getIp(), res,
								res.isMqttOnline() ? "00C000" : "C0C0C0", true));
					}
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

	public void onNodeDblselect(SelectEvent event) {
		this.selectedNode = (MindmapNode) event.getObject();
	}

	@Named
	public String getSelectedNodeData() {
		if (this.selectedNode != null) {
			Resource res = (Resource) this.selectedNode.getData();
			Set<NameValueEntry> data = res.getMqttValues();
			String currentEntry = "";
			for (NameValueEntry entry : data) {
				String values = entry.getValues();
				currentEntry += entry.getName() + " : " + values.substring(values.lastIndexOf(","), values.length()) + "\n";
			}
			return currentEntry;
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

	public TreeNode getResourceTree() {
		return this.resourceTreeRoot;
	}

	public TreeNode getAgentTree() {
		return this.agentTreeRoot;
	}

	// Create Tree for resources view
	public TreeNode createResourceTree() {
		TreeNode root = new DefaultTreeNode("My Devices", null);

		List<Company> companies = companyController.getAllCompanies();
		for (Company c : companies) {
			TreeNode company = new DefaultTreeNode(new ResourceTreeInfo(c.getName(), false, false, null, null), root);
			List<Department> departments = c.getDepartments();
			for (Department d : departments) {
				TreeNode department = new DefaultTreeNode(new ResourceTreeInfo(d.getName(), false, false, null, null), company);
				List<ResourceGroup> groups = d.getResourceGroups();
				for (ResourceGroup g : groups) {
					if (authController.canRead(g, sessionController.getUser())) {
						TreeNode group = null;
						if (authController.canCreate(g, sessionController.getUser())) {
							group = new DefaultTreeNode(new ResourceTreeInfo(g.getName(), false, true, String.valueOf(g.getId()), null),
									department);
						} else {
							group = new DefaultTreeNode(new ResourceTreeInfo(g.getName(), false, false, null, null), department);
						}
						Set<Resource> resources = g.getResources();
						for (Resource res : resources) {
							if (!res.isAgent()) {
								if (authController.canRead(res, sessionController.getUser())) {
									TreeNode resourceInfoNode = new DefaultTreeNode(
											new ResourceTreeInfo(res.getName(), true, false, null, res), group);
								}
							}
						}

					}

				}

			}
		}
		return root;
	}

	// Create Tree for resources view
	public TreeNode createAgentTree() {
		TreeNode root = new DefaultTreeNode("My Agents", null);

		List<Company> companies = companyController.getAllCompanies();
		for (Company c : companies) {
			TreeNode company = new DefaultTreeNode(new ResourceTreeInfo(c.getName(), false, false, null, null), root);
			List<Department> departments = c.getDepartments();
			for (Department d : departments) {
				TreeNode department = new DefaultTreeNode(new ResourceTreeInfo(d.getName(), false, false, null, null), company);
				List<ResourceGroup> groups = d.getResourceGroups();
				for (ResourceGroup g : groups) {
					if (authController.canRead(g, sessionController.getUser())) {
						TreeNode group = null;
						if (authController.canCreate(g, sessionController.getUser())) {
							group = new DefaultTreeNode(new ResourceTreeInfo(g.getName(), false, true, String.valueOf(g.getId()), null),
									department);
						} else {
							group = new DefaultTreeNode(new ResourceTreeInfo(g.getName(), false, false, null, null), department);
						}
						Set<Resource> resources = g.getResources();
						for (Resource res : resources) {
							if (res.isAgent()) {
								if (authController.canRead(res, sessionController.getUser())) {
									TreeNode resourceInfoNode = new DefaultTreeNode(
											new ResourceTreeInfo(res.getName(), true, false, null, res), group);
								}
							}
						}

					}

				}

			}
		}
		return root;
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
		return "resources";
	}

	public String goBackToAgents() {
		ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
		try {
			context.redirect(context.getApplicationContextPath() + "/client/resources/agents.xhtml");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "agents";
	}

	public void updateResourceGroupId(String groupId) {
		this.selectedResourceGroupId = groupId;
	}

}
