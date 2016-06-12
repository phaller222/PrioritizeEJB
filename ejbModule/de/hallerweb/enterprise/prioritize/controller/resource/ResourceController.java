package de.hallerweb.enterprise.prioritize.controller.resource;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.enterprise.context.ContextNotActiveException;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TemporalType;

import de.hallerweb.enterprise.prioritize.controller.CompanyController;
import de.hallerweb.enterprise.prioritize.controller.InitializationController;
import de.hallerweb.enterprise.prioritize.controller.LoggingController;
import de.hallerweb.enterprise.prioritize.controller.LoggingController.Action;
import de.hallerweb.enterprise.prioritize.controller.event.EventRegistry;
import de.hallerweb.enterprise.prioritize.controller.security.AuthorizationController;
import de.hallerweb.enterprise.prioritize.controller.security.SessionController;
import de.hallerweb.enterprise.prioritize.controller.security.UserRoleController;
import de.hallerweb.enterprise.prioritize.model.Department;
import de.hallerweb.enterprise.prioritize.model.calendar.TimeSpan;
import de.hallerweb.enterprise.prioritize.model.calendar.TimeSpan.TimeSpanType;
import de.hallerweb.enterprise.prioritize.model.event.Event;
import de.hallerweb.enterprise.prioritize.model.event.PEventConsumerProducer;
import de.hallerweb.enterprise.prioritize.model.event.PObjectType;
import de.hallerweb.enterprise.prioritize.model.resource.NameValueEntry;
import de.hallerweb.enterprise.prioritize.model.resource.Resource;
import de.hallerweb.enterprise.prioritize.model.resource.ResourceGroup;
import de.hallerweb.enterprise.prioritize.model.resource.ResourceReservation;
import de.hallerweb.enterprise.prioritize.model.security.User;
import de.hallerweb.enterprise.prioritize.model.skill.SkillRecord;
import de.hallerweb.enterprise.prioritize.service.mqtt.MQTTService;

/**
 * ResourceController.java - Controls the creation, modification and deletion of
 * {@link Resource} and {@link ResourceGroup} objects. This controller also
 * takes care of the data handling of MQTT Resources which can automatically be
 * discovered and created by the system.
 * 
 */
@Stateless
public class ResourceController extends PEventConsumerProducer {
	@PersistenceContext(unitName = "MySqlDS")
	EntityManager em;

	@EJB
	UserRoleController userRoleController;
	@EJB
	CompanyController companyController;
	@Inject
	SessionController sessionController;
	@EJB
	AuthorizationController authController;
	@EJB
	LoggingController logger;
	@Inject
	MQTTService mqttService;
	@Inject
	EventRegistry eventRegistry;

	public Resource createResource(String name, int groupId, User user, String description, String ip, int maxSlots, boolean isStationary,
			boolean isRemote) {
		ResourceGroup managedGroup = em.find(ResourceGroup.class, groupId);
		if (findResourceByResourceGroupAndName(managedGroup.getId(), name, user) == null) {
			Resource resource = new Resource();
			if (authController.canCreate(resource, user)) {
				resource.setName(name);
				resource.setResourceGroup(managedGroup);
				resource.setDescription(description);
				resource.setIp(ip);
				resource.setMaxSlots(maxSlots);
				resource.setStationary(isStationary);
				resource.setRemote(isRemote);
				resource.setDepartment(managedGroup.getDepartment());

				// Add the DocumentInfo to the DocumentGroup
				managedGroup.addResource(resource);
				em.persist(resource);
				em.flush();
				try {
					logger.log(sessionController.getUser().getUsername(), "Resource", Action.CREATE, resource.getId(),
							" Resource \"" + resource.getName() + "\" created.");
				} catch (ContextNotActiveException ex) {
					// Log message omitted here because this can only happen during
					// automatic tests (no session).
				}
				return resource;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	/**
	 * Same as createResource(), but creates a MQTT Resource.
	 * 
	 * @param name
	 * @param groupId
	 * @param user
	 * @param description
	 * @param ip
	 * @param maxSlots
	 * @param isStationary
	 * @param isRemote
	 * @param uuid
	 * @param dataSendTopic
	 * @param dataReceiveTopic
	 * @return
	 */
	public Resource createMqttResource(String name, String token, String group, User user, String description, String ip, int maxSlots,
			boolean isStationary, boolean isRemote, boolean agent, String uuid, String dataSendTopic, String dataReceiveTopic) {

		Department departmentToAddResource = companyController.getDepartmentByToken(token);
		if (findResourceByResourceGroupAndName(findResourceGroupByNameAndDepartment(group, departmentToAddResource.getId(), user).getId(),
				name, user) == null) {

			Resource resource = new Resource();
			if (authController.canCreate(resource, user)) {

				resource.setName(name);

				resource.setDescription(description.replaceAll(":", "").replaceAll(";", ""));
				resource.setIp(ip);
				resource.setMaxSlots(maxSlots);
				resource.setStationary(isStationary);
				resource.setRemote(isRemote);
				resource.setAgent(agent);

				ResourceGroup groupToAddResource = getResourceGroupInDepartment(departmentToAddResource.getId(), group);
				resource.setResourceGroup(groupToAddResource);
				resource.setDepartment(departmentToAddResource);

				resource.setMqttUUID(uuid);
				resource.setMqttResource(true);
				resource.setDataReceiveTopic(dataReceiveTopic);
				resource.setDataSendTopic(dataSendTopic);
				resource.setMqttLastPing(new java.util.Date());

				// Add the Resource to the ResourceGroup
				groupToAddResource.addResource(resource);
				em.persist(resource);
				em.flush();
				try {
					logger.log(sessionController.getUser().getUsername(), "Resource", Action.CREATE, resource.getId(),
							" Resource \"" + resource.getName() + "\" created.");
				} catch (ContextNotActiveException ex) {
					// Log message omitted here because this can only happen during
					// automatic tests (no session).
				}
				return resource;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	/**
	 * Updates the "ping" of a mqtt resource in the database to avoid the
	 * resource getting shut down. The timeout period for this is configured in
	 * the config.ini file in the manifest.
	 * 
	 * @param resource
	 */
	public void updateMqttPing(Resource resource) {
		Resource managedResource = em.find(Resource.class, resource.getId());
		managedResource.setMqttLastPing(new Date());
	}

	public ResourceGroup createResourceGroup(int departmentId, String name, User sessionUser) {

		// first get department and check if group already exists
		Department managedDepartment = em.find(Department.class, departmentId);
		if (findResourceGroupByNameAndDepartment(name, managedDepartment.getId(), sessionUser) == null) {
			ResourceGroup resourceGroup = new ResourceGroup();
			if (authController.canCreate(resourceGroup, sessionUser)) {
				resourceGroup.setName(name);
				resourceGroup.setDepartment(managedDepartment);
				resourceGroup.setResources(new TreeSet<Resource>());

				em.persist(resourceGroup);
				managedDepartment.addResourceGroup(resourceGroup);

				logger.log(sessionController.getUser().getUsername(), "ResourceGroup", Action.CREATE, resourceGroup.getId(),
						" Resource Group \"" + resourceGroup.getName() + "\" created.");

				return resourceGroup;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	public Set<Resource> getResourcesInResourceGroup(int resourceGroupId, User sessionUser) {
		ResourceGroup rg = (ResourceGroup) em.find(ResourceGroup.class, resourceGroupId);
		Set<Resource> result = rg.getResources();
		if (!result.isEmpty()) {
			Resource res = (Resource) result.iterator().next();
			if (authController.canRead(res, sessionUser)) {
				return result;
			} else {
				return null;
			}
		} else
			return null;
	}

	public List<Resource> getOnlineMqttResources(User sessionUser) {
		Query query = em.createNamedQuery("findAllOnlineMqttResources");
		@SuppressWarnings("unchecked")
		List<Resource> result = query.getResultList();
		if (!result.isEmpty()) {
			Resource res = (Resource) result.get(0);
			if (authController.canRead(res, sessionUser)) {
				return result;
			} else {
				return null;
			}
		} else
			return null;
	}

	public Resource getResource(int id, User sessionUser) {
		Query query = em.createNamedQuery("findResourceById");
		query.setParameter("resId", id);
		Resource res = (Resource) query.getSingleResult();
		if (authController.canRead(res, sessionUser)) {
			return res;
		} else {
			return null;
		}
	}

	/**
	 * Same as getResource(), but just returns resources which are MQTT capable.
	 * 
	 * @param uuid
	 * @return
	 */
	public Resource getResource(String uuid, User sessionUser) {
		Query query = em.createNamedQuery("findResourceByUUId");
		query.setParameter("uuid", uuid);
		try {
			Resource res = (Resource) query.getSingleResult();
			if (authController.canRead(res, sessionUser)) {
				return res;
			} else {
				return null;
			}
		} catch (NoResultException ex) {
			return null;
		}
	}

	public List<Resource> getAllResources(User user) {
		Query query = em.createNamedQuery("findAllResources");
		return query.getResultList();
	}

	/**
	 * Returns all MQTT UUID's currently registered with an MQTT Resource
	 * object.
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<String> getAllMqttUuids() {
		Query query = em.createNamedQuery("findAllMqttResourceUuids");
		return (List<String>) query.getResultList();
	}

	/**
	 * Checks if an MQTT resource for the given UUID exists.
	 * 
	 * @param uuid
	 * @return
	 */
	public boolean exists(String uuid) {
		Query query = em.createNamedQuery("findResourceByUUId");
		query.setParameter("uuid", uuid);
		Resource res = null;
		try {
			res = (Resource) query.getSingleResult();
		} catch (NoResultException ex) {
			return false;
		}
		if (res == null) {
			return false;
		} else
			return true;
	}

	/**
	 * Writes streaming data received from an MQTT resource to the resource's
	 * common data buffer.
	 * 
	 * @param res
	 * @param data
	 */
	public void writeMqttDataReceived(Resource res, byte[] data) {
		Resource managed = em.find(Resource.class, res.getId());
		if (data != null) {
			managed.setMqttDataReceived(data);
		}
		em.flush();
	}

	/**
	 * Writes streaming data about to be send to the MQTT resource to the
	 * resource's read buffer and sends the data to the specified topic to be
	 * received by the resource.
	 * 
	 * @param res
	 * @param data
	 */
	public void writeMqttDataToSend(Resource res, byte[] data) {
		Resource managedResource = em.find(Resource.class, res.getId());
		if (managedResource != null) {
			managedResource.setMqttDataToSend(data);
			mqttService.writeToTopic(managedResource.getDataReceiveTopic(), data);
		}
	}

	public boolean isResourceActiveForUser(User user, Set<ResourceReservation> reservations) {
		Date now = new Date();
		Date now2 = new Date(System.currentTimeMillis() + 10000);
		for (ResourceReservation reservation : reservations) {
			if (reservation.getReservedBy().equals(user)) {
				if (isWithinTimeframe(now, now2, reservation)) {
					return true;
				}
			}
		}
		return false;
	}

	public int getActiveSlotForUser(User user, Set<ResourceReservation> reservations) {
		Date now = new Date();
		Date now2 = new Date(System.currentTimeMillis() + 10000);
		for (ResourceReservation reservation : reservations) {
			if (reservation.getReservedBy().equals(user)) {
				if (isWithinTimeframe(now, now2, reservation)) {
					return reservation.getSlotNumber();
				}
			}
		}
		return -1;
	}

	public void setMqttResourceOnline(Resource res) {
		Resource managed = em.find(Resource.class, res.getId());
		raiseEvent(PObjectType.RESOURCE, managed.getId(), Resource.PROPERTY_MQTTONLINE, String.valueOf(managed.isMqttOnline()), "true",
				InitializationController.getAsInt(InitializationController.EVENT_DEFAULT_TIMEOUT));
		managed.setMqttOnline(true);
	}

	public void setMqttResourceOffline(Resource res) {
		Resource managed = em.find(Resource.class, res.getId());
		raiseEvent(PObjectType.RESOURCE, managed.getId(), Resource.PROPERTY_MQTTONLINE, String.valueOf(managed.isMqttOnline()), "false",
				InitializationController.getAsInt(InitializationController.EVENT_DEFAULT_TIMEOUT));
		managed.setMqttOnline(false);
	}

	/**
	 * Adds a new named value pair to the MQTT resource's NamedValue objects.
	 * 
	 * @param res
	 * @param name
	 * @param value
	 */
	public void addMqttValueForResource(Resource res, String name, String value) {
		value = "" + System.currentTimeMillis() + "," + value;
		Resource managedResource = em.find(Resource.class, res.getId());
		if (!managedResource.isMqttOnline()) {
			managedResource.setMqttOnline(true); // Automatically startup resource because data is coming...
		}
		int valuesSize = managedResource.getMqttValues().size();
		if (!managedResource.getMqttValues().isEmpty()) {
			List<NameValueEntry> valuesCopy = new ArrayList<NameValueEntry>(valuesSize);
			for (NameValueEntry e : managedResource.getMqttValues()) {
				valuesCopy.add(e);
			}

			Iterator<NameValueEntry> it = valuesCopy.iterator();
			StringBuffer buff = new StringBuffer();
			boolean found = false;
			while (it.hasNext()) {
				NameValueEntry entry = it.next();
				if (entry.getName().equals(name)) {
					insertValue(value, buff, entry);
					found = true;
				}
			}
			if (!found) {
				if (valuesSize <= Integer.valueOf(InitializationController.config.get(InitializationController.MQTT_MAX_DEVICE_VALUES))) {
					createMqttNameValuePair(name, value, managedResource);
				}
			}
		} else {
			if (valuesSize <= Integer.valueOf(InitializationController.config.get(InitializationController.MQTT_MAX_DEVICE_VALUES))) {
				createMqttNameValuePair(name, value, managedResource);
			}
		}
	}

	private void insertValue(String value, StringBuffer buff, NameValueEntry entry) {
		String values = entry.getValues();
		if (values != null
				&& values.length() > Integer.valueOf(InitializationController.config.get(InitializationController.MQTT_MAX_VALUES_BYTES))) {
			int firstEntryEnd = values.indexOf(";");
			buff.append(values.substring(firstEntryEnd + 1, values.length()));
			entry.setValues(buff.toString() + ";" + value);
		} else {
			entry.setValues(entry.getValues() + ";" + value);
		}
	}

	/**
	 * Deletes the named value with the given name and all historical data for
	 * it from the given Resource.
	 * 
	 * @param res
	 * @param name
	 */
	public void clearMqttValueForResource(Resource res, String name) {
		if (res.isMqttOnline()) {
			Resource managedResource = em.find(Resource.class, res.getId());
			if (!managedResource.getMqttValues().isEmpty()) {
				Iterator<NameValueEntry> it = managedResource.getMqttValues().iterator();
				while (it.hasNext()) {
					NameValueEntry entry = it.next();
					if (entry.getName().equals(name)) {
						NameValueEntry managedEntry = em.find(NameValueEntry.class, entry.getId());
						managedResource.getMqttValues().remove(managedEntry);
						em.remove(managedEntry);
						em.remove(managedEntry);
					}
				}
			}
		}
	}

	private void createMqttNameValuePair(String name, String value, Resource managedResource) {
		NameValueEntry newEntry = new NameValueEntry();
		newEntry.setName(name);
		newEntry.setValues(value);
		em.persist(newEntry);
		raiseEvent(PObjectType.RESOURCE, managedResource.getId(), name, "", newEntry.getValues(),
				InitializationController.getAsInt(InitializationController.EVENT_DEFAULT_TIMEOUT));
		managedResource.getMqttValues().add(newEntry);
	}

	/**
	 * Returns all NameValueEntry objects currently defined for the given
	 * resource.
	 * 
	 * @param res
	 * @return
	 */
	public Set<NameValueEntry> getNameValueEntries(Resource res) {
		Resource managedResource = em.find(Resource.class, res.getId());
		return managedResource.getMqttValues();
	}

	public String getLastMqttValueForResource(Resource res, String name) {
		Resource managedResource = em.find(Resource.class, res.getId());
		if (managedResource != null) {
			for (NameValueEntry entry : managedResource.getMqttValues()) {
				if (entry.getName().equals(name)) {
					if (entry.getValues().contains(";")) {
						String[] values = entry.getValues().split(";");
						return values[values.length - 1];
					} else {
						return entry.getValues();
					}
				}
			}
		}
		return "";
	}

	/**
	 * Returns all ResourceReservation objects which reservation timespans are
	 * in the past. Should be used to remove obsolete reservation entries.
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<ResourceReservation> getPastResourceReservations() {
		Query query = em.createNamedQuery("findPastResoureReservations");
		query.setParameter("now", new Date(), TemporalType.TIMESTAMP);
		return query.getResultList();
	}

	/**
	 * Finds out how many slots (=available items or whatever) of a given
	 * resource are available at the moment.
	 * 
	 * @param resource
	 * @return
	 */
	public int getSlotsInUse(Resource resource) {
		int slotsInUse = 0;
		Resource res = em.find(Resource.class, resource.getId());
		if (res != null) {
			Set<ResourceReservation> reservations = res.getReservations();
			if (reservations != null) {
				for (ResourceReservation reservation : reservations) {
					Date now = new Date(System.currentTimeMillis());
					if (reservation.getTimeSpan().getDateFrom().before(now) && reservation.getTimeSpan().getDateUntil().after(now)) {
						slotsInUse++;
					}
				}
			}
		}
		return slotsInUse;
	}

	/**
	 * Return all ResourceReservation entries for resources assigned into the
	 * given ResourceGroup.
	 * 
	 * @param resourceGroupId
	 * @return
	 */
	public List<ResourceReservation> getResourceReservationsForResourceGroup(int resourceGroupId) {

		Query query = em.createNamedQuery("findResourceReservationsForResourceGroup");
		query.setParameter("resourceGroupId", resourceGroupId);

		@SuppressWarnings("unchecked")
		List<ResourceReservation> reservations = (List<ResourceReservation>) query.getResultList();
		return reservations;
	}

	public List<ResourceReservation> getResourceReservationsForDepartment(int departmentId) {
		Query query = em.createNamedQuery("findResourceReservationsForDepartment");
		query.setParameter("departmentId", departmentId);

		@SuppressWarnings("unchecked")
		List<ResourceReservation> reservations = (List<ResourceReservation>) query.getResultList();
		return reservations;
	}

	public List<ResourceReservation> getAllResourceReservations() {
		Query query = em.createNamedQuery("findAllResourceReservations");
		@SuppressWarnings("unchecked")
		List<ResourceReservation> reservations = (List<ResourceReservation>) query.getResultList();
		return reservations;
	}

	public List<ResourceReservation> getResourceReservationsForUser(User user) {
		Query query = em.createNamedQuery("findResourceReservationsForUser");
		query.setParameter("UserId", user.getId());
		return query.getResultList();
	}

	/**
	 * Returns the ResourceGroup object with the given id. The parameter User
	 * will be used later to eventually check the permissions.
	 * 
	 * @param id
	 * @param user
	 * @return
	 */
	public ResourceGroup getResourceGroup(int id, User user) {
		Query query = em.createNamedQuery("findResourceGroupById");
		query.setParameter("resourceGroupId", id);
		ResourceGroup group = (ResourceGroup) query.getSingleResult();
		return group;
	}

	@SuppressWarnings("unchecked")
	public List<ResourceGroup> getResourceGroupsForDepartment(int departmentId) {
		Query query = em.createNamedQuery("findResourceGroupsForDepartment");
		query.setParameter("deptId", departmentId);
		return (List<ResourceGroup>) query.getResultList();
	}

	public ResourceGroup getResourceGroupInDepartment(int departmentId, String groupName) {
		Query query = em.createNamedQuery("findResourceGroupInDepartment");
		query.setParameter("deptId", departmentId);
		query.setParameter("groupName", groupName);
		return (ResourceGroup) query.getSingleResult();
	}

	public void deleteResource(int resourceId) {
		Resource res = em.find(Resource.class, resourceId);
		Set<ResourceReservation> reservations = res.getReservations();
		try {
			if (!reservations.isEmpty()) {
				reservations.clear();
				res.setReservations(reservations);
			}
			res.getResourceGroup().removeResource(res);
		} catch (NullPointerException ex) {
			System.out.println(ex.getMessage());
		}

		for (NameValueEntry entry :  res.getMqttValues()) {
			NameValueEntry managedEntry = em.find(NameValueEntry.class, entry.getId());
			em.remove(managedEntry);
		}
		res.getMqttValues().clear();
		
		em.remove(res);
		em.flush();
		try {
			logger.log(sessionController.getUser().getUsername(), "Resource", Action.DELETE, res.getId(),
					" Resource \"" + res.getName() + "\" deleted.");
		} catch (ContextNotActiveException ex) {
			logger.log("SYSTEM", "Resource", Action.DELETE, res.getId(), " Resource \"" + res.getName() + "\" deleted.");
		}
	}

	public void deleteResourceGroup(int resourceGroupId, User user) {
		ResourceGroup group = em.find(ResourceGroup.class, resourceGroupId);
		// ------------------ AUTH check-------------------------
		if (authController.canDelete(group, user)) {
			Set<Resource> resources = group.getResources();
			if (resources != null) {
				for (Resource resource : resources) {
					deleteResource(resource.getId());
				}
			}

			group.getDepartment().getResourceGroups().remove(group);
			group.setDepartment(null);
			em.remove(group);
			group = null;
			em.flush();

		}
	}

	public Resource editResource(Resource resource, Department newDept, ResourceGroup newGroup, String newName, String newDescription,
			String newIp, boolean newIsStationary, boolean newIsRemote, int newMaxSlots, User user) {
		Resource managedResource = em.find(Resource.class, resource.getId());
		Department managedDepartment = null;
		ResourceGroup managedGroup = null;
		if (newDept != null) {
			managedDepartment = em.find(Department.class, newDept.getId());
			managedGroup = em.find(ResourceGroup.class, newGroup.getId());
		}

		if (authController.canUpdate(managedResource, user)) {

			if (!newName.equals(managedResource.getName())) {
				this.raiseEvent(PObjectType.RESOURCE, managedResource.getId(), Resource.PROPERTY_NAME, managedResource.getName(), newName,
						60000);
			}
			if (!newDescription.equals(managedResource.getDescription())) {
				this.raiseEvent(PObjectType.RESOURCE, managedResource.getId(), Resource.PROPERTY_DESCRIPTION,
						managedResource.getDescription(), newDescription, 60000);
			}
			if (managedResource.getDepartment().getId() != managedDepartment.getId()) {
				this.raiseEvent(PObjectType.RESOURCE, managedResource.getId(), Resource.PROPERTY_DEPARTMENT,
						String.valueOf(managedResource.getDepartment().getId()), String.valueOf(newDept.getId()), 60000);
			}
			if (managedResource.getResourceGroup().getId() != managedGroup.getId()) {
				this.raiseEvent(PObjectType.RESOURCE, managedResource.getId(), Resource.PROPERTY_RESOURCEGROUP,
						String.valueOf(managedResource.getResourceGroup().getId()), String.valueOf(newGroup.getId()),
						InitializationController.getAsInt(InitializationController.EVENT_DEFAULT_TIMEOUT));
			}

			managedResource.setName(newName);
			managedResource.setDescription(newDescription);
			managedResource.setIp(newIp);
			managedResource.setStationary(newIsStationary);
			managedResource.setRemote(newIsRemote);
			managedResource.setMaxSlots(newMaxSlots);
			if (newDept != null) {
				managedResource.setDepartment(managedDepartment);
				managedResource.getResourceGroup().removeResource(managedResource);
				em.flush();
				managedGroup.addResource(managedResource);
				managedResource.setResourceGroup(managedGroup);
			}
			em.flush();

			logger.log(sessionController.getUser().getUsername(), "Resource", Action.UPDATE, managedResource.getId(),
					" Resource \"" + managedResource.getId() + "\" changed.");

			return managedResource;
		} else
			return null;
	}

	public void setResourceName(Resource res, String name, User user) {
		Resource managedResource = em.find(Resource.class, res.getId());
		if (authController.canUpdate(managedResource, user)) {
			managedResource.setName(name);
		}
	}

	public void setResourceDescription(Resource res, String description, User user) {
		Resource managedResource = (Resource) em.find(Resource.class, res.getId());
		if (authController.canUpdate(managedResource, user)) {
			managedResource.setDescription(description);
		}
	}

	public ResourceReservation createResourceReservation(Resource resource, Date from, Date until, User user) {
		Resource res = em.find(Resource.class, resource.getId());
		User managedUser = em.find(User.class, user.getId());
		Set<ResourceReservation> reservations = res.getReservations();
		int slotsAvailable = calcFreeSlots(reservations, res.getMaxSlots(), from, until);
		if (slotsAvailable <= 0) {
			return null;
		} else {
			// Create resource reservation for resource
			ResourceReservation reservation = new ResourceReservation();
			reservation.setReservedBy(user);

			// Generate TimeSpan and persist
			TimeSpan ts = new TimeSpan();
			ts.setTitle(res.getName());
			ts.setDescription(res.getResourceGroup().getName() + ":" + res.getName() + "[" + user.getName() + "]");
			ts.setDateFrom(from);
			ts.setDateUntil(until);
			ts.addInvolvedResource(res);
			ts.addInvolvedUser(managedUser);
			ts.setType(TimeSpanType.RESOURCE_RESERVATION);

			em.persist(ts);
			em.flush();

			reservation.setTimeSpan(ts);
			reservation.setResource(res);

			// If Resource has more than 1 slot, assign available slot,
			// otherwise just 1.
			if (res.getMaxSlots() == 1) {
				reservation.setSlotNumber(1);
			} else {
				reservation.setSlotNumber(getNextAvailableSlot(res, reservations, from, until));
			}

			em.persist(reservation);
			res.addReservation(reservation);
			em.flush();

			logger.log(sessionController.getUser().getUsername(), "ResourceReservation", Action.CREATE, reservation.getId(),
					" ResourceReservation created.");
			return reservation;
		}

	}

	private int getNextAvailableSlot(Resource resource, Set<ResourceReservation> reservations, Date from, Date to) {
		boolean[] bookedSlots = new boolean[resource.getMaxSlots()];
		for (ResourceReservation reservation : reservations) {
			if (isWithinTimeframe(from, to, reservation)) {
				bookedSlots[reservation.getSlotNumber()] = true;
			}
		}
		for (int i = 0; i < bookedSlots.length; i++) {
			if (bookedSlots[i] == false) {
				return i;
			}
		}
		return -1;

	}

	public ResourceReservation removeResourceReservation(int reservationId) {
		ResourceReservation reservation = em.find(ResourceReservation.class, reservationId);
		reservation.getResource().getReservations().remove(reservation);
		em.remove(reservation);
		em.flush();
		logger.log(sessionController.getUser().getUsername(), "ResourceReservation", Action.DELETE, reservation.getId(),
				" ResourceReservation deleted.");

		return reservation;
	}

	private int calcFreeSlots(Set<ResourceReservation> reservations, int maxSlots, Date from, Date to) {
		int slots = maxSlots;
		if (reservations == null) {
			return slots;
		}
		for (ResourceReservation reservation : reservations) {
			if (isWithinTimeframe(from, to, reservation)) {
				slots--;
			}
		}
		return slots;
	}

	private boolean isWithinTimeframe(Date requestedFrom, Date requestedTo, ResourceReservation res) {

		if (requestedFrom.after(res.getTimeSpan().getDateUntil())) {
			return false;
		} else if (requestedTo.before(res.getTimeSpan().getDateFrom())) {
			return false;
		} else
			return true;
	}

	public void cleanupReservations() {
		// clean up all Resource Reservations which have past
		List<ResourceReservation> reservations = getPastResourceReservations();
		if (reservations != null) {
			for (ResourceReservation reservation : reservations) {
				reservation.getResource().getReservations().remove(reservation);
				em.remove(reservation);
				logger.log("SYSTEM", "ResourceReservation", Action.DELETE, reservation.getId(),
						"ResourceReservation no longer valid, so i deleted it.");
			}
		}
	}

	/**
	 * Set the current geographic coordinates of a resource (Latitude and
	 * Longitude, String, as expected by GoogleEarth)
	 * 
	 * @param resource
	 * @param latitude
	 * @param longitude
	 */
	public void setCoordinates(Resource resource, String latitude, String longitude) {
		Resource res = em.find(Resource.class, resource.getId());
		raiseEvent(PObjectType.RESOURCE, resource.getId(), "geo", resource.getLatitude() + ":" + resource.getLongitude(),
				latitude + ":" + longitude, InitializationController.getAsInt(InitializationController.EVENT_DEFAULT_TIMEOUT));
		res.setLongitude(longitude);
		res.setLatitude(latitude);
	}

	public String getLatitude(Resource resource) {
		Resource res = em.find(Resource.class, resource.getId());
		if (res.getLatitude() != null) {
			return resource.getLatitude();
		} else
			return "";
	}

	public String getLongitude(Resource resource) {
		Resource res = em.find(Resource.class, resource.getId());
		if (res.getLongitude() != null) {
			return resource.getLongitude();
		} else
			return "";
	}

	public void addCommand(Resource res, String command) {
		Resource resource = em.find(Resource.class, res.getId());
		Set<String> commands = resource.getMqttCommands();
		if (commands != null && !commands.contains(command)) {
			commands.add(command);
			resource.setMqttCommands(commands);
		}
	}

	public void setCommands(Resource res, Set<String> commands) {
		Resource resource = em.find(Resource.class, res.getId());
		resource.setMqttCommands(commands);
	}

	public void clearCommands(Resource res) {
		Resource resource = em.find(Resource.class, res.getId());
		resource.getMqttCommands().clear();
	}

	public void sendCommand(Resource resource, String command, String param) {
		if (param == null) {
			param = "0";
		}
		Resource managedResource = em.find(Resource.class, resource.getId());
		Set<ResourceReservation> reservations = managedResource.getReservations();
		if (isResourceActiveForUser(sessionController.getUser(), reservations)) {
			int slot = getActiveSlotForUser(sessionController.getUser(), reservations);
			mqttService.writeToTopic(managedResource.getDataReceiveTopic(), (command + ";" + param + ":" + slot).getBytes());
		}
	}

	public void sendCommandToResource(Resource resource, String command, String param) {
		if (param == null) {
			param = "0";
		}
		Resource managedResource = em.find(Resource.class, resource.getId());

		int slot = 0; // TODO: dynamically reserve and set slot
		mqttService.writeToTopic(managedResource.getDataReceiveTopic(), (command + ";" + param + ":" + slot).getBytes());

	}

	public void fireScanResult(String sourceUuid, String scanResult) {
		Resource managedResource = getResource(sourceUuid, AuthorizationController.getSystemUser());
		mqttService.writeToTopic(managedResource.getDataReceiveTopic(), scanResult.getBytes());
	}

	public Set<String> getMqttCommands(Resource resource, User sessionUser) {
		Resource res = em.find(Resource.class, resource.getId());
		if (authController.canRead(res, sessionUser)) {
			return res.getMqttCommands();
		} else {
			return null;
		}
	}

	public void addSkillToResource(int resourceId, int skillRecordId, User sessionUser) {
		boolean alreadyAssigned = false; // is set to true if skill type already
										 // assigned
		Resource res = em.find(Resource.class, resourceId);
		if (authController.canUpdate(res, sessionUser)) {
			SkillRecord rec = em.find(SkillRecord.class, skillRecordId);

			for (SkillRecord resSkillRecord : res.getSkills()) {
				if (resSkillRecord.getSkill().getId() == rec.getId()) {
					alreadyAssigned = true;
				}
			}

			// Only assign new skill if resource does not already have
			// a skill of that kind assigned.
			if (!alreadyAssigned) {
				res.addSkill(rec);
				rec.setResource(res);
				em.flush();
			}
		}

	}

	public void removeSkillFromResource(SkillRecord record, Resource resource, User sessionUser) {
		Resource res = em.find(Resource.class, resource.getId());
		if (authController.canUpdate(res, sessionUser)) {
			SkillRecord skillRecord = em.find(SkillRecord.class, record.getId());
			skillRecord.setResource(null);
			res.removeSkill(skillRecord);
			em.remove(skillRecord);
			em.flush();
		}
	}

	public Set<SkillRecord> getSkillRecordsForResource(int resourceId, User sessionUser) {
		Resource res = em.find(Resource.class, resourceId);
		if (authController.canRead(res, sessionUser)) {
			return res.getSkills();
		} else {
			return null;
		}
	}

	public Resource findResourceByResourceGroupAndName(int resourceGroupId, String name, User sessionUser) {
		Query query = em.createNamedQuery("findResourcesByResourceGroupAndName");
		query.setParameter("dgid", resourceGroupId);
		query.setParameter("name", name);
		try {
			Resource res = (Resource) query.getSingleResult();
			if (authController.canRead(res, sessionUser)) {
				return res;
			} else {
				return null;
			}
		} catch (NoResultException ex) {
			return null;
		}
	}

	public ResourceGroup findResourceGroupByNameAndDepartment(String groupName, int deptId, User sessionUser) {
		Query query = em.createNamedQuery("findResourceGroupByNameAndDepartment");
		query.setParameter("name", groupName);
		query.setParameter("deptId", deptId);
		try {
			ResourceGroup group = (ResourceGroup) query.getSingleResult();
			if (authController.canRead(group, sessionUser)) {
				return group;
			} else {
				return null;
			}
		} catch (NoResultException ex) {
			return null;
		}
	}

	public void raiseEvent(PObjectType type, int id, String name, String oldValue, String newValue, long lifetime) {
		if (InitializationController.getAsBoolean(InitializationController.FIRE_RESOURCE_EVENTS)) {
			Event evt = eventRegistry.getEventBuilder().newEvent().setSourceType(type).setSourceId(id).setOldValue(oldValue)
					.setNewValue(newValue).setPropertyName(name).setLifetime(lifetime).getEvent();
			eventRegistry.addEvent(evt);
		}
	}

	@Override
	public void consumeEvent(int id, Event evt) {
		System.out.println("Object " + evt.getSourceType() + " with ID " + evt.getSourceId() + " raised event: " + evt.getPropertyName()
				+ " with new Value: " + evt.getNewValue() + "--- Resource listening: " + id);

	}

}
