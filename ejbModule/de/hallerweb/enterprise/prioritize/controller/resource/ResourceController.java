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
package de.hallerweb.enterprise.prioritize.controller.resource;

import de.hallerweb.enterprise.prioritize.controller.CompanyController;
import de.hallerweb.enterprise.prioritize.controller.InitializationController;
import de.hallerweb.enterprise.prioritize.controller.LoggingController;
import de.hallerweb.enterprise.prioritize.controller.LoggingController.Action;
import de.hallerweb.enterprise.prioritize.controller.event.EventRegistry;
import de.hallerweb.enterprise.prioritize.controller.security.AuthorizationController;
import de.hallerweb.enterprise.prioritize.controller.security.SessionController;
import de.hallerweb.enterprise.prioritize.controller.security.UserRoleController;
import de.hallerweb.enterprise.prioritize.model.Department;
import de.hallerweb.enterprise.prioritize.model.PObject;
import de.hallerweb.enterprise.prioritize.model.event.Event;
import de.hallerweb.enterprise.prioritize.model.event.PEventConsumerProducer;
import de.hallerweb.enterprise.prioritize.model.resource.NameValueEntry;
import de.hallerweb.enterprise.prioritize.model.resource.Resource;
import de.hallerweb.enterprise.prioritize.model.resource.ResourceGroup;
import de.hallerweb.enterprise.prioritize.model.resource.ResourceReservation;
import de.hallerweb.enterprise.prioritize.model.security.User;
import de.hallerweb.enterprise.prioritize.model.skill.SkillRecord;
import de.hallerweb.enterprise.prioritize.service.mqtt.MQTTService;
import org.jboss.resteasy.logging.Logger;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.enterprise.context.ContextNotActiveException;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.*;

/**
 * ResourceController.java - Controls the creation, modification and deletion of
 * {@link Resource} and {@link ResourceGroup} objects.
 * 
 */
@Stateless
public class ResourceController extends PEventConsumerProducer {
	@PersistenceContext
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
	@EJB
	ResourceReservationController resourceReservationcontroller;
	@Inject
	MQTTService mqttService;
	@Inject
	EventRegistry eventRegistry;

	private static final String PARAM_DEPARTMENT_ID = "deptId";
	private static final String LITERAL_RESOURCE = "Resource";
	private static final String LITERAL_RESOURCE_SPACE = " Resource \"";
	private static final String LITERAL_RESOURCE_CREATED = "\" created.";
	
	
	public Resource createResource(Resource resourceToCreate, int groupId, User sessionUser) {
		ResourceGroup managedGroup = em.find(ResourceGroup.class, groupId);

		String name = resourceToCreate.getName();
		String description = resourceToCreate.getDescription();
		String ip = resourceToCreate.getIp();
		int maxSlots = resourceToCreate.getMaxSlots();
		boolean stationary = resourceToCreate.isStationary();
		boolean remote = resourceToCreate.isRemote();

		if (findResourceByResourceGroupAndName(managedGroup.getId(), name, sessionUser) == null) {
			Resource resource = new Resource();
			if (authController.canCreate(resource, sessionUser)) {
				resource.setName(name);
				resource.setResourceGroup(managedGroup);
				resource.setDescription(description);
				resource.setIp(ip);
				resource.setMaxSlots(maxSlots);
				resource.setStationary(stationary);
				resource.setRemote(remote);
				resource.setDepartment(managedGroup.getDepartment());

				// Add the DocumentInfo to the DocumentGroup
				managedGroup.addResource(resource);
				em.persist(resource);
				em.flush();
				try {
					logger.log(sessionUser.getUsername(), LITERAL_RESOURCE, Action.CREATE, resource.getId(),
							LITERAL_RESOURCE_SPACE + resource.getName() + LITERAL_RESOURCE_CREATED);
				} catch (Exception ex) {
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

	public ResourceGroup createResourceGroup(int departmentId, String name, User sessionUser) {

		// first get department and check if group already exists
		Department managedDepartment = em.find(Department.class, departmentId);
		if (findResourceGroupByNameAndDepartment(name, managedDepartment.getId(), sessionUser) == null) {
			ResourceGroup resourceGroup = new ResourceGroup();
			if (authController.canCreate(resourceGroup, sessionUser)) {
				resourceGroup.setName(name);
				resourceGroup.setDepartment(managedDepartment);
				resourceGroup.setResources(new TreeSet<>());

				em.persist(resourceGroup);
				managedDepartment.addResourceGroup(resourceGroup);

				logger.log(sessionController.getUser().getUsername(), "ResourceGroup", Action.CREATE, resourceGroup.getId(),
						" Resource Group \"" + resourceGroup.getName() + LITERAL_RESOURCE_CREATED);

				return resourceGroup;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	public Set<Resource> getResourcesInResourceGroup(int resourceGroupId, User sessionUser) {
		ResourceGroup rg = em.find(ResourceGroup.class, resourceGroupId);
		Set<Resource> result = rg.getResources();
		if (!result.isEmpty()) {
			Resource res = result.iterator().next();
			if (authController.canRead(res, sessionUser)) {
				return result;
			} else {
				return new HashSet<>();
			}
		} else {
			return new HashSet<>();
		}
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

	public List<Resource> getAllResources(User sessionUser) {
		if (authController.canRead(new Resource(), sessionUser)) {
			Query query = em.createNamedQuery("findAllResources");
			return query.getResultList();
		} else {
			return new ArrayList<>();
		}
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
		return (ResourceGroup) query.getSingleResult();
	}

	@SuppressWarnings("unchecked")
	public List<ResourceGroup> getResourceGroupsForDepartment(int departmentId, User sessionUser) {
		Query query = em.createNamedQuery("findResourceGroupsForDepartment");
		query.setParameter(PARAM_DEPARTMENT_ID, departmentId);
		List<ResourceGroup> groups = query.getResultList();
		ResourceGroup group = groups.get(0);
		if (authController.canRead(group, sessionUser)) {
			return groups;
		} else {
			return new ArrayList<>();
		}
	}

	public ResourceGroup getResourceGroupInDepartment(int departmentId, String groupName, User sessionUser) {
		Query query = em.createNamedQuery("findResourceGroupInDepartment");
		query.setParameter(PARAM_DEPARTMENT_ID, departmentId);
		query.setParameter("groupName", groupName);
		ResourceGroup group = (ResourceGroup) query.getSingleResult();
		if (group != null && authController.canRead(group, sessionUser)) {
			return (ResourceGroup) query.getSingleResult();
		} else {
			return null;
		}
	}

	public void deleteResource(int resourceId, User sessionUser) {
		Resource res = em.find(Resource.class, resourceId);
		if (authController.canDelete(res, sessionUser)) {
			Set<ResourceReservation> reservations = res.getReservations();
			try {
				if (!reservations.isEmpty()) {
					reservations.clear();
					res.setReservations(reservations);
				}
				res.getResourceGroup().removeResource(res);
			} catch (NullPointerException ex) {
				Logger.getLogger(this.getClass()).error(ex.getMessage());
			}

			for (NameValueEntry entry : res.getMqttValues()) {
				NameValueEntry managedEntry = em.find(NameValueEntry.class, entry.getId());
				em.remove(managedEntry);
			}
			res.getMqttValues().clear();

			em.remove(res);
			em.flush();
			try {
				logger.log(sessionUser.getUsername(), LITERAL_RESOURCE, Action.DELETE, res.getId(),
						LITERAL_RESOURCE_SPACE + res.getName() + "\" deleted.");
			} catch (ContextNotActiveException ex) {
				logger.log("SYSTEM", LITERAL_RESOURCE, Action.DELETE, res.getId(),
						LITERAL_RESOURCE_SPACE + res.getName() + "\" deleted.");
			}
		}
	}

	public void deleteResourceGroup(int resourceGroupId, User user) {
		ResourceGroup group = em.find(ResourceGroup.class, resourceGroupId);
		// ------------------ AUTH check-------------------------
		if (authController.canDelete(group, user)) {
			Set<Resource> resources = group.getResources();
			if (resources != null) {
				for (Resource resource : resources) {
					deleteResource(resource.getId(), user);
				}
			}

			group.getDepartment().getResourceGroups().remove(group);
			group.setDepartment(null);
			em.remove(group);
			em.flush();
		}
	}

	public Resource editResource(Resource resource) {
		Resource managedResource = em.find(Resource.class, resource.getId());
		managedResource.setAgent(resource.isAgent());
		managedResource.setBusy(resource.isBusy());
		managedResource.setDataReceiveTopic(resource.getMqttDataReceiveTopic());
		managedResource.setDataSendTopic(resource.getMqttDataSendTopic());
		managedResource.setDescription(resource.getDescription());
		managedResource.setIp(resource.getIp());
		managedResource.setLatitude(resource.getLatitude());
		managedResource.setLongitude(resource.getLongitude());
		managedResource.setMaxSlots(resource.getMaxSlots());
		managedResource.setMqttCommands(resource.getMqttCommands());
		managedResource.setMqttDataReceiveTopic(resource.getMqttDataReceiveTopic());
		managedResource.setMqttDataSendTopic(resource.getMqttDataSendTopic());
		managedResource.setMqttLastPing(resource.getMqttLastPing());
		managedResource.setMqttOnline(resource.isMqttOnline());
		managedResource.setMqttResource(resource.isMqttResource());
		managedResource.setName(resource.getName());
		managedResource.setRemote(resource.isRemote());
		managedResource.setSkills(resource.getSkills());
		managedResource.setStationary(resource.isStationary());
		return managedResource;
	}

	public Resource editResource(Resource resourceToEdit, Department newDept, ResourceGroup newGroup, String newName, String newDescription,
			String newIp, boolean newIsStationary, boolean newIsRemote, int newMaxSlots, User user) {
		Resource managedResource = em.find(Resource.class, resourceToEdit.getId());
		Department managedDepartment = null;
		ResourceGroup managedGroup = null;
		if (newDept != null) {
			managedDepartment = em.find(Department.class, newDept.getId());
			managedGroup = em.find(ResourceGroup.class, newGroup.getId());
		}

		if (authController.canUpdate(managedResource, user) && managedDepartment != null) {

			if (!newName.equals(managedResource.getName())) {
				this.raiseEvent(managedResource, Resource.PROPERTY_NAME, managedResource.getName(), newName, 60000);
			}
			if (!newDescription.equals(managedResource.getDescription())) {
				this.raiseEvent(managedResource, Resource.PROPERTY_DESCRIPTION, managedResource.getDescription(), newDescription, 60000);
			}
			if (managedResource.getDepartment().getId() != managedDepartment.getId()) {
				this.raiseEvent(managedResource, Resource.PROPERTY_DEPARTMENT, String.valueOf(managedResource.getDepartment().getId()),
						String.valueOf(newDept.getId()), 60000);
			}
			if (managedResource.getResourceGroup().getId() != managedGroup.getId()) {
				this.raiseEvent(managedResource, Resource.PROPERTY_RESOURCEGROUP,
						String.valueOf(managedResource.getResourceGroup().getId()), String.valueOf(newGroup.getId()),
						InitializationController.getAsInt(InitializationController.EVENT_DEFAULT_TIMEOUT));
			}

			managedResource.setName(newName);
			managedResource.setDescription(newDescription);
			managedResource.setIp(newIp);
			managedResource.setStationary(newIsStationary);
			managedResource.setRemote(newIsRemote);
			managedResource.setMaxSlots(newMaxSlots);
			managedResource.setDepartment(managedDepartment);
			managedResource.getResourceGroup().removeResource(managedResource);
			em.flush();

			managedGroup.addResource(managedResource);
			managedResource.setResourceGroup(managedGroup);

			em.flush();

			logger.log(user.getUsername(), LITERAL_RESOURCE, Action.UPDATE, managedResource.getId(),
					LITERAL_RESOURCE_SPACE + managedResource.getId() + "\" changed.");

			return managedResource;
		} else {
			return null;
		}
	}

	public void setResourceName(Resource res, String name, User user) {
		Resource managedResource = em.find(Resource.class, res.getId());
		if (authController.canUpdate(managedResource, user)) {
			managedResource.setName(name);
		}
	}

	public void setResourceDescription(Resource res, String description, User user) {
		Resource managedResource = em.find(Resource.class, res.getId());
		if (authController.canUpdate(managedResource, user)) {
			managedResource.setDescription(description);
		}
	}

	
	public String getLongitude(Resource resource) {
		Resource res = em.find(Resource.class, resource.getId());
		if (res.getLongitude() != null) {
			return resource.getLongitude();
		} else {
			return "";
		}
	}

	public void addSkillToResource(int resourceId, int skillRecordId, User sessionUser) {
		boolean alreadyAssigned = false; // is set to true if skill type already
										 // assigned
		Resource res = em.find(Resource.class, resourceId);
		if (authController.canUpdate(res, sessionUser)) {
			SkillRecord rec = em.find(SkillRecord.class, skillRecordId);

			for (SkillRecord resSkillRecord : res.getSkills())
				if (resSkillRecord.getSkill().getId() == rec.getId()) {
					alreadyAssigned = true;
				}

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
			return new HashSet<>();
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
		query.setParameter(PARAM_DEPARTMENT_ID, deptId);
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

	public void raiseEvent(PObject source, String name, String oldValue, String newValue, long lifetime) {
		if (InitializationController.getAsBoolean(InitializationController.FIRE_RESOURCE_EVENTS)) {
			Event evt = eventRegistry.getEventBuilder().newEvent().setSource(source).setOldValue(oldValue).setNewValue(newValue)
					.setPropertyName(name).setLifetime(lifetime).getEvent();
			eventRegistry.addEvent(evt);
		}
	}

	@Override
	public void consumeEvent(PObject destination, Event evt) {
		Logger.getLogger(this.getClass()).info("Object " + evt.getSource() + " raised event: " + evt.getPropertyName() + " with new Value: "
				+ evt.getNewValue() + "--- Resource listening: " + (destination).getId());
	}
}
