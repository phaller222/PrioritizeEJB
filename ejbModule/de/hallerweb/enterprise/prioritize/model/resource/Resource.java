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
package de.hallerweb.enterprise.prioritize.model.resource;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import de.hallerweb.enterprise.prioritize.model.Department;
import de.hallerweb.enterprise.prioritize.model.project.task.PActor;
import de.hallerweb.enterprise.prioritize.model.search.PSearchable;
import de.hallerweb.enterprise.prioritize.model.search.SearchProperty;
import de.hallerweb.enterprise.prioritize.model.search.SearchResult;
import de.hallerweb.enterprise.prioritize.model.search.SearchResultType;
import de.hallerweb.enterprise.prioritize.model.security.PAuthorizedObject;
import de.hallerweb.enterprise.prioritize.model.security.User;
import de.hallerweb.enterprise.prioritize.model.skill.SkillRecord;
import jakarta.persistence.*;

import java.util.*;

/**
 * JPA entity to represent a {@link Resource}. Demo UUID: 69178331-8dd9-4dd1-87f6-368f424006c2
 * <p>
 * Copyright: (c) 2014
 * </p>
 * <p>
 * Peter Haller
 * </p>
 * 
 * @author peter haller
 * 
 */

@NamedQuery(name = "findResourcesByResourceGroup", query = "select r FROM Resource r WHERE r.resourceGroup.id = :dgid")
		@NamedQuery(name = "findResourcesByResourceGroupAndName", query = "select r FROM Resource r WHERE r.resourceGroup.id = :dgid AND r.name = :name")
		@NamedQuery(name = "findResourceGroupById", query = "select rg FROM ResourceGroup rg WHERE rg.id = :resourceGroupId")
		@NamedQuery(name = "findResourceGroupByNameAndDepartment", query = "select rg FROM ResourceGroup rg WHERE rg.name = :name AND rg.department.id = :deptId")
		@NamedQuery(name = "findResourceById", query = "select r FROM Resource r WHERE r.id = :resId")
		@NamedQuery(name = "findResourceByUUId", query = "select r FROM Resource r WHERE r.mqttUUID = :uuid")
		@NamedQuery(name = "findResourceBySendTopic", query = "select r FROM Resource r WHERE r.mqttDataToSend = :sendTopic")
		@NamedQuery(name = "findAllMqttResourceUuids", query = "select r.mqttUUID FROM Resource r WHERE r.mqttUUID IS NOT NULL")
		@NamedQuery(name = "findAllOnlineMqttResources", query = "select r FROM Resource r WHERE r.mqttUUID IS NOT NULL AND r.mqttOnline = TRUE")
		@NamedQuery(name = "findAllResources", query = "select r FROM Resource r")
@Entity
public class Resource extends PActor implements PAuthorizedObject, PSearchable, Comparable<Object> {

	public static final String PROPERTY_NAME = "name";
	public static final String PROPERTY_DESCRIPTION = "description";
	public static final String PROPERTY_DEPARTMENT = "department";
	public static final String PROPERTY_RESOURCEGROUP = "resourceGroup";
	public static final String PROPERTY_GEO = "geo";
	public static final String PROPERTY_MQTTONLINE = "mqttOnline";

	private String name; // Name of the resource.
	private String description; // Human-readable description of the resource.
	private boolean isStationary; // Is the resource bound to a specific location?
	private boolean isRemote; // Can the resource be accessed remotely (e.G. IP Address?)
	private String ip; // IP Adress of the resource (if isRemote)
	private boolean isBusy; // Is the resource busy at the moment?
	private int maxSlots = 1; // How many slots are available for this resource (usually 1 in case of a unique resource)
	private String latitude; // latitude of geolocation for IoT devices
	private String longitude; // longitude of geolocation for IoT devices
	private boolean isMqttResource; // is this resource a MQTT resource?
	private String mqttUUID; // If MQTT Resource: UUID for the resource
	private String mqttDataSendTopic; // If MQTT Resource: Topic the resource is sending data to
	private String mqttDataReceiveTopic; // If MQTT Resource: Topic the resource expects data to read
	private boolean mqttOnline; // If MQTT Resource: Indicates if resource is online
	private Date mqttLastPing; // Every MQTT resource has to send ping every minute!
	private boolean agent; // Is resource an Agent?

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "mqtt_commands", joinColumns = @JoinColumn(name = "resource_id"))
	@Column(name = "commands")
	Set<String> mqttCommands; // List with commands a resource supports.

	@OneToMany(fetch = FetchType.EAGER)
	@OrderBy(value = "mqttName")
	private Set<NameValueEntry> mqttValues; // List with name/value entries for an mqtt Resource

	@Lob
	private byte[] mqttDataReceived; // Buffer for receiving data
	@Lob
	private byte[] mqttDataToSend; // Buffer to send data to the device.

	@OneToOne
	@JsonBackReference(value="departmentBackRef")
	private Department department; // To which department dowa the resource belong?

	@OneToOne
	private User busyBy; // Who is blocking the resource at the moment?

	@OneToOne(fetch = FetchType.EAGER)
	@JsonBackReference(value="resourceGroupBackRef")
	private ResourceGroup resourceGroup; // To which ResourceGroup does this resource belong?

	@OneToMany(fetch = FetchType.EAGER, orphanRemoval = true)
	@JsonBackReference(value="reservationsBackRef")
	private Set<ResourceReservation> reservations; // ResourceReservatios that exist for this resource.

	@JsonIgnore
	@OneToMany(fetch = FetchType.EAGER)
	private Set<SkillRecord> skills;

	transient List<SearchProperty> searchProperties;

	public boolean isAgent() {
		return agent;
	}

	public void setAgent(boolean agent) {
		this.agent = agent;
	}

	@Override
	public List<SearchResult> find(String phrase) {
		ArrayList<SearchResult> results = new ArrayList<>();
		// Search document name
		if (name.toLowerCase().contains(phrase.toLowerCase())) {
			// Match found
			SearchResult result = generateResult();
			results.add(result);
			return results;
		}
		if (this.description.toLowerCase().contains(phrase.toLowerCase())) {
			// Match found
			SearchResult result = generateResult();
			results.add(result);
		}
		return results;
	}

	@Override
	public List<SearchResult> find(String phrase, SearchProperty property) {
		return new ArrayList<>();
	}

	@Override
	public List<SearchProperty> getSearchProperties() {
		if (this.searchProperties == null) {
			searchProperties = new ArrayList<>();
			SearchProperty prop = new SearchProperty("RESOURCE");
			prop.setName("Resource");
			searchProperties.add(prop);
		}
		return this.searchProperties;
	}

	private SearchResult generateResult() {
		SearchResult result = new SearchResult();
		result.setResult(this);
		result.setResultType(SearchResultType.RESOURCE);
		result.setExcerpt(name + " : " + this.getDescription());
		result.setProvidesExcerpt(true);
		result.setSubresults(new HashSet<>());
		return result;
	}

	public Set<String> getMqttCommands() {
		return mqttCommands;
	}

	public void setMqttCommands(Set<String> mqttCommands) {
		this.mqttCommands = mqttCommands;
	}

	public Date getMqttLastPing() {
		return mqttLastPing;
	}

	public void setMqttLastPing(Date mqttLastPing) {
		this.mqttLastPing = mqttLastPing;
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public Set<NameValueEntry> getMqttValues() {
		return mqttValues;
	}

	public void setMqttValues(SortedSet<NameValueEntry> mqttValues) {
		this.mqttValues = mqttValues;
	}

	public String getMqttDataSendTopic() {
		return mqttDataSendTopic;
	}

	public void setMqttDataSendTopic(String mqttDataSendTopic) {
		this.mqttDataSendTopic = mqttDataSendTopic;
	}

	public String getMqttDataReceiveTopic() {
		return mqttDataReceiveTopic;
	}

	public void setMqttDataReceiveTopic(String mqttDataReceiveTopic) {
		this.mqttDataReceiveTopic = mqttDataReceiveTopic;
	}

	public boolean isMqttOnline() {
		return mqttOnline;
	}

	public void setMqttOnline(boolean mqttOnline) {
		this.mqttOnline = mqttOnline;
	}

	public Set<ResourceReservation> getReservations() {
		return reservations;
	}

	public void setReservations(Set<ResourceReservation> reservations) {
		this.reservations = reservations;
	}

	public void addReservation(ResourceReservation reservation) {
		if (!reservations.contains(reservation)) {
			this.reservations.add(reservation);
		}
	}

	public int getMaxSlots() {
		return maxSlots;
	}

	public void setMaxSlots(int maxSlots) {
		this.maxSlots = maxSlots;
	}

	public ResourceGroup getResourceGroup() {
		return resourceGroup;
	}

	public void setResourceGroup(ResourceGroup location) {
		this.resourceGroup = location;
	}

	public boolean isStationary() {
		return isStationary;
	}

	public void setStationary(boolean isStationary) {
		this.isStationary = isStationary;
	}

	public boolean isRemote() {
		return isRemote;
	}

	public void setRemote(boolean isRemote) {
		this.isRemote = isRemote;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public boolean isBusy() {
		return isBusy;
	}

	public void setBusy(boolean isBusy) {
		this.isBusy = isBusy;
	}

	public User getBusyBy() {
		return busyBy;
	}

	public void setBusyBy(User busyBy) {
		this.busyBy = busyBy;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setDepartment(Department department) {
		this.department = department;
	}

	@Override
	public Department getDepartment() {
		return department;
	}

	public boolean isMqttResource() {
		return isMqttResource;
	}

	public void setMqttResource(boolean isMqttResource) {
		this.isMqttResource = isMqttResource;
	}

	public String getMqttUUID() {
		return mqttUUID;
	}

	public void setMqttUUID(String mqttUUID) {
		this.mqttUUID = mqttUUID;
	}

	public byte[] getMqttDataReceived() {
		return mqttDataReceived;
	}

	public String getMqttDataReceivedAsString() {
		if (mqttDataReceived == null) {
			return "";
		} else {
			return new String(mqttDataReceived);
		}
	}

	public void setMqttDataReceived(byte[] mqttDataReceived) {
		this.mqttDataReceived = mqttDataReceived;
	}

	public byte[] getMqttDataToSend() {
		return mqttDataToSend;
	}

	/**
	 * Returns "" not null if dataToSend == null!
	 * 
	 * @return
	 */
	public String getMqttDataSentAsString() {
		if (mqttDataToSend == null) {
			return "";
		} else {
			return new String(mqttDataToSend);
		}
	}

	public void setMqttDataSentAsString(String data) {
		this.mqttDataToSend = data.getBytes();
	}

	public void setMqttDataToSend(byte[] mqttDataToSend) {
		this.mqttDataToSend = mqttDataToSend;
	}

	public void setDataSendTopic(String dataSendTopic) {
		this.mqttDataSendTopic = dataSendTopic;
	}

	public void setDataReceiveTopic(String dataReceiveTopic) {
		this.mqttDataReceiveTopic = dataReceiveTopic;
	}

	public Set<SkillRecord> getSkills() {
		return skills;
	}

	public void setSkills(Set<SkillRecord> skills) {
		this.skills = skills;
	}

	public void addSkill(SkillRecord skill) {
		this.skills.add(skill);
	}

	public void removeSkill(SkillRecord skill) {
		this.skills.remove(skill);
	}

	@Override
	public int compareTo(Object obj) {
		Resource res = (Resource) obj;
		return Integer.compare(id, res.getId());
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}

	@Override
	public String toString() {
		return this.getName();
	}

}
