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
import de.hallerweb.enterprise.prioritize.model.Department;
import de.hallerweb.enterprise.prioritize.model.PObject;
import de.hallerweb.enterprise.prioritize.model.event.Event;
import de.hallerweb.enterprise.prioritize.model.event.PEventConsumerProducer;
import de.hallerweb.enterprise.prioritize.model.resource.NameValueEntry;
import de.hallerweb.enterprise.prioritize.model.resource.Resource;
import de.hallerweb.enterprise.prioritize.model.resource.ResourceGroup;
import de.hallerweb.enterprise.prioritize.model.resource.ResourceReservation;
import de.hallerweb.enterprise.prioritize.model.security.User;
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
import java.util.logging.Level;
import java.util.logging.LogManager;

/**
 * MQTTResourceController.java - Controls the creation, modification and deletion of
 * {@link Resource}  objects. This controller takes care of the data handling of MQTT Resources which can automatically be
 * discovered and created by the system.
 */
@Stateless
public class MQTTResourceController extends PEventConsumerProducer {

    private static final String LITERAL_RESOURCE = "Resource";
    private static final String LITERAL_RESOURCE_SPACE = " Resource \"";
    private static final String LITERAL_RESOURCE_CREATED = "\" created.";

    @PersistenceContext
    EntityManager em;
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
    @EJB
    ResourceController resourceController;
    @EJB
    InitializationController initController;
    @Inject
    MQTTService mqttService;
    @Inject
    EventRegistry eventRegistry;

    public Resource createMqttResource(Resource resourceToCreate, String departmentToken, String resourceGroupName, User sessionUser) {

        Department departmentToAddResource = companyController.getDepartmentByToken(departmentToken, sessionUser);
        String name = resourceToCreate.getName();
        String description = resourceToCreate.getDescription();
        String ip = resourceToCreate.getIp();
        boolean stationary = resourceToCreate.isStationary();
        boolean remote = resourceToCreate.isRemote();
        boolean agent = resourceToCreate.isAgent();
        int maxSlots = resourceToCreate.getMaxSlots();
        String uuid = resourceToCreate.getMqttUUID();
        String dataReceiveTopic = resourceToCreate.getMqttDataReceiveTopic();
        String dataSendTopic = resourceToCreate.getMqttDataSendTopic();

        if (resourceController.findResourceByResourceGroupAndName(resourceController
                        .findResourceGroupByNameAndDepartment(resourceGroupName, departmentToAddResource.getId(), sessionUser).getId(), name,
                sessionUser) == null) {

            Resource resource = new Resource();
            if (authController.canCreate(resource, sessionUser)) {

                resource.setName(name);
                resource.setDescription(description.replaceAll(":", "").replaceAll(";", ""));
                resource.setIp(ip);
                resource.setMaxSlots(maxSlots);
                resource.setStationary(stationary);
                resource.setRemote(remote);
                resource.setAgent(agent);

                ResourceGroup groupToAddResource = resourceController.getResourceGroupInDepartment(departmentToAddResource.getId(),
                        resourceGroupName, sessionUser);
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
                    logger.log(sessionController.getUser().getUsername(), LITERAL_RESOURCE, Action.CREATE, resource.getId(),
                            LITERAL_RESOURCE_SPACE + resource.getName() + LITERAL_RESOURCE_CREATED);
                } catch (ContextNotActiveException ex) {
                    LogManager.getLogManager().getLogger(getClass().getName()).log(Level.WARNING, ex.getMessage());
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
     * the config.properties file in the manifest.
     *
     * @param resource
     */
    public void updateMqttPing(Resource resource) {
        Resource managedResource = em.find(Resource.class, resource.getId());
        managedResource.setMqttLastPing(new Date());
    }

    public List<Resource> getOnlineMqttResources(User sessionUser) {
        Query query = em.createNamedQuery("findAllOnlineMqttResources");
        @SuppressWarnings("unchecked")
        List<Resource> result = query.getResultList();
        if (result.isEmpty()) {
            return new ArrayList<>();
        } else {
            Resource res = result.get(0);
            if (authController.canRead(res, sessionUser)) {
                return result;
            } else {
                return new ArrayList<>();
            }
        }
    }

    /**
     * Same as getResource() in ResourceManager, but only returns resources which are MQTT capable.
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
        Resource res;
        try {
            res = (Resource) query.getSingleResult();
        } catch (NoResultException ex) {
            return false;
        }
        return (res != null);
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

    public Set<String> getMqttCommands(Resource resource, User sessionUser) {
        Resource res = em.find(Resource.class, resource.getId());
        if (authController.canRead(res, sessionUser)) {
            return res.getMqttCommands();
        } else {
            return new HashSet<>();
        }
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
            mqttService.writeToTopic(managedResource.getMqttDataReceiveTopic(), data);
        }
    }

    public void setMqttResourceOnline(Resource res) {
        Resource managed = em.find(Resource.class, res.getId());
        raiseEvent(managed, Resource.PROPERTY_MQTTONLINE, String.valueOf(managed.isMqttOnline()), "true",
                initController.getAsInt(InitializationController.EVENT_DEFAULT_TIMEOUT));
        managed.setMqttOnline(true);
    }

    public void setMqttResourceOffline(Resource res) {
        Resource managed = em.find(Resource.class, res.getId());
        raiseEvent(managed, Resource.PROPERTY_MQTTONLINE, String.valueOf(managed.isMqttOnline()), "false",
                initController.getAsInt(InitializationController.EVENT_DEFAULT_TIMEOUT));
        managed.setMqttOnline(false);
    }

    public void setMqttResourceStatus(Resource res, boolean online) {
        if (online) {
            setMqttResourceOnline(res);
        } else {
            setMqttResourceOffline(res);
        }
    }

    /**
     * Adds a new named value pair to the MQTT resource's NamedValue objects.
     *
     * @param res
     * @param name
     * @param value
     */
    public void addMqttValueForResource(Resource res, String name, String value) {
        //TODO: Umstellen auf Apache IOT-DB
        String valueCopy = "" + System.currentTimeMillis() + "," + value;
        Resource managedResource = em.find(Resource.class, res.getId());
        if (!managedResource.isMqttOnline()) {
            managedResource.setMqttOnline(true); // Automatically startup resource because data is coming...
        }
        int valuesSize = managedResource.getMqttValues().size();
        if (managedResource.getMqttValues().isEmpty()) {
            if (valuesSize <= Integer.parseInt(initController.getConfig().get(InitializationController.MQTT_MAX_DEVICE_VALUES))) {
                createMqttNameValuePair(name, valueCopy, managedResource);
            }
        } else {
            updateValueIfEntryAlreadyExists(name, valueCopy, managedResource, valuesSize);
        }
    }

    private void insertValue(String value, StringBuilder builder, NameValueEntry entry) {
        //TODO: Umstellen auf Apache IOT-DB
        String values = entry.getValues();
        if ((values == null)
                || (values.length() <= Integer.parseInt(initController.getConfig().get(InitializationController.MQTT_MAX_VALUES_BYTES)))) {
                    entry.setValues(entry.getValues() + ";" + value);
                } else {
            int firstEntryEnd = values.indexOf(';');
            builder.append(values.substring(firstEntryEnd + 1));
            entry.setValues(builder.toString() + ";" + value);
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
        //TODO: Umstellen auf Apache IOT-DB
        if (res.isMqttOnline()) {
            Resource managedResource = em.find(Resource.class, res.getId());
            if (!managedResource.getMqttValues().isEmpty()) {
                for (NameValueEntry entry : managedResource.getMqttValues()) {
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
        //TODO: Umstellen auf Apache IOT-DB
        NameValueEntry newEntry = new NameValueEntry();
        newEntry.setName(name);
        newEntry.setValues(value);
        em.persist(newEntry);
        raiseEvent(managedResource, name, "", newEntry.getValues(),
                initController.getAsInt(InitializationController.EVENT_DEFAULT_TIMEOUT));
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
        //TODO: Umstellen auf Apache IOT-DB
        Resource managedResource = em.find(Resource.class, res.getId());
        return managedResource.getMqttValues();
    }

    public String getLastMqttValueForResource(Resource res, String name) {
        //TODO: Umstellen auf Apache IOT-DB
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

    private void updateValueIfEntryAlreadyExists(String name, String value, Resource managedResource, int valuesSize) {
        //TODO: Umstellen auf Apache IOT-DB
        List<NameValueEntry> valuesCopy = new ArrayList<>(valuesSize);
        valuesCopy.addAll(managedResource.getMqttValues());

        Iterator<NameValueEntry> it = valuesCopy.iterator();
        StringBuilder buff = new StringBuilder();
        boolean found = false;
        while (it.hasNext()) {
            NameValueEntry entry = it.next();
            if (entry.getName().equals(name)) {
                insertValue(value, buff, entry);
                found = true;
            }
        }
        if (!found && valuesSize <= Integer.parseInt(initController.getConfig().get(InitializationController.MQTT_MAX_DEVICE_VALUES))) {
            createMqttNameValuePair(name, value, managedResource);
        }
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
        String paramCopy = param;
        if (param == null) {
            paramCopy = "0";
        }
        Resource managedResource = em.find(Resource.class, resource.getId());
        Set<ResourceReservation> reservations = managedResource.getReservations();
        if (resourceReservationcontroller.isResourceActiveForUser(sessionController.getUser(), reservations)) {
            int slot = resourceReservationcontroller.getActiveSlotForUser(sessionController.getUser(), reservations);
            mqttService.writeToTopic(managedResource.getMqttDataReceiveTopic(), (command + ";" + paramCopy + ":" + slot).getBytes());
        }
    }

    public void sendCommandToResource(Resource resource, String command, String param) {
        String paramCopy = param;
        if (param == null) {
            paramCopy = "0";
        }
        Resource managedResource = em.find(Resource.class, resource.getId());

        int slot = 0; // TODO: dynamically reserve and set slot
        mqttService.writeToTopic(managedResource.getMqttDataReceiveTopic(), (command + ";" + paramCopy + ":" + slot).getBytes());

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
        raiseEvent(res, "geo", resource.getLatitude() + ":" + resource.getLongitude(), latitude + ":" + longitude,
                initController.getAsInt(InitializationController.EVENT_DEFAULT_TIMEOUT));
        res.setLongitude(longitude);
        res.setLatitude(latitude);
    }

    public String getLatitude(Resource resource) {
        Resource res = em.find(Resource.class, resource.getId());
        if (res.getLatitude() == null) {
            return "";
        } else {
            return resource.getLatitude();
        }
    }

    public void raiseEvent(PObject source, String name, String oldValue, String newValue, long lifetime) {
        if (initController.getAsBoolean(InitializationController.FIRE_RESOURCE_EVENTS)) {
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

    public void fireScanResult(String sourceUuid, String scanResult) {
        Resource managedResource = getResource(sourceUuid, authController.getSystemUser());
        mqttService.writeToTopic(managedResource.getMqttDataReceiveTopic(), scanResult.getBytes());
    }

}
