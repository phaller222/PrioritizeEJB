package de.hallerweb.enterprise.prioritize.model.resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;

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

/**
 * JPA entity to represent a {@link Resource}. Demo UUID: 69178331-8dd9-4dd1-87f6-368f424006c2
 * <p>
 * Copyright: (c) 2014
 * </p>
 * <p>
 * Peter Haller
 * </p>
 * 
 * @author peter
 * 
 */
@Entity
@NamedQueries({ @NamedQuery(name = "findResourcesByResourceGroup", query = "select r FROM Resource r WHERE r.resourceGroup.id = :dgid"),
		@NamedQuery(name = "findResourcesByResourceGroupAndName", query = "select r FROM Resource r WHERE r.resourceGroup.id = :dgid AND r.name = :name"),
		@NamedQuery(name = "findResourceGroupById", query = "select rg FROM ResourceGroup rg WHERE rg.id = :resourceGroupId"),
		@NamedQuery(name = "findResourceGroupByNameAndDepartment", query = "select rg FROM ResourceGroup rg WHERE rg.name = :name AND rg.department.id = :deptId"),
		@NamedQuery(name = "findResourceById", query = "select r FROM Resource r WHERE r.id = :resId"),
		@NamedQuery(name = "findResourceByUUId", query = "select r FROM Resource r WHERE r.mqttUUID = :uuid"),
		@NamedQuery(name = "findResourceBySendTopic", query = "select r FROM Resource r WHERE r.mqttDataToSend = :sendTopic"),
		@NamedQuery(name = "findAllMqttResourceUuids", query = "select r.mqttUUID FROM Resource r WHERE r.mqttUUID IS NOT NULL"),
		@NamedQuery(name = "findAllOnlineMqttResources", query = "select r FROM Resource r WHERE r.mqttUUID IS NOT NULL AND r.mqttOnline = TRUE"),
		@NamedQuery(name = "findAllResources", query = "select r FROM Resource r") })
public class Resource extends PActor implements PAuthorizedObject, PSearchable, Comparable<Object> {

	public static final String PROPERTY_NAME = "name";
	public static final String PROPERTY_DESCRIPTION = "description";
	public static final String PROPERTY_DEPARTMENT = "department";
	public static final String PROPERTY_RESOURCEGROUP = "resourceGroup";
	public static final String PROPERTY_GEO = "geo";
	public static final String PROPERTY_MQTTONLINE = "mqttOnline";
	

	private String name; // Name of the resource.
	@Column(length = 65535)
	private String description; // Human readable description of the resource.
	private boolean isStationary; // Is the resource bound to a specific location?
	private boolean isRemote; // Can the resource be accessed remotely (e.G. IP Address?)
	private String ip; // IP Adress of the resource (if isRemote)
	private boolean isBusy; // Is the resource busy at the moment?
	private int maxSlots = 1; // How many slots are available for this resource (usually 1 in case of a unique resource)
	private String latitude; // latitude of geo location for IoT devices
	private String longitude; // longitude of geo location for IoT devices
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
	private SortedSet<NameValueEntry> mqttValues; // List with name/value entries for an mqtt Resource

	@Lob
	private byte[] mqttDataReceived; // Buffer for receiving data
	@Lob
	private byte[] mqttDataToSend; // Buffer to send data to the device.

	@OneToOne
	@JsonBackReference
	private Department department; // To which department dowa the resource belong?

	@OneToOne
	private User busyBy; // Who is blocking the resource at the moment?

	@OneToOne(fetch = FetchType.EAGER)
	@JsonBackReference
	private ResourceGroup resourceGroup; // To which ResourceGroup does this resource belong?

	@OneToMany(fetch = FetchType.EAGER, orphanRemoval = true)
	@JsonBackReference
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
		ArrayList<SearchResult> results = new ArrayList<SearchResult>();
		// Search document name
		if (name.toLowerCase().indexOf(phrase.toLowerCase()) != -1) {
			// Match found
			SearchResult result = generateResult();
			results.add(result);
			return results;
		}
		if (this.description.toLowerCase().indexOf(phrase.toLowerCase()) != -1) {
			// Match found
			SearchResult result = generateResult();
			results.add(result);
		}
		return results;
	}

	private SearchResult generateResult() {
		SearchResult result = new SearchResult();
		result.setResult(this);
		result.setResultType(SearchResultType.RESOURCE);
		result.setExcerpt(name + " : " + this.getDescription());
		result.setProvidesExcerpt(true);
		result.setSubresults(new HashSet<SearchResult>());
		return result;
	}

	@Override
	public List<SearchResult> find(String phrase, SearchProperty property) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<SearchProperty> getSearchProperties() {
		if (this.searchProperties == null) {
			searchProperties = new ArrayList<SearchProperty>();
			SearchProperty prop = new SearchProperty("RESOURCE");
			prop.setName("Resource");
			searchProperties.add(prop);
		}
		return this.searchProperties;
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
		if (mqttDataReceived != null) {
			return new String(mqttDataReceived);
		} else
			return "";
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
		if (mqttDataToSend != null) {
			return new String(mqttDataToSend);
		} else
			return "";
	}

	public void setMqttDataSentAsString(String data) {
		this.mqttDataToSend = data.getBytes();
	}

	public void setMqttDataToSend(byte[] mqttDataToSend) {
		this.mqttDataToSend = mqttDataToSend;
	}

	public String getDataSendTopic() {
		return mqttDataSendTopic;
	}

	public void setDataSendTopic(String dataSendTopic) {
		this.mqttDataSendTopic = dataSendTopic;
	}

	public String getDataReceiveTopic() {
		return mqttDataReceiveTopic;
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
	public int compareTo(Object o) {
		Resource res = (Resource) o;
		if (res.getId() == id) {
			return 0;
		} else if (res.getId() > id) {
			return -1;
		} else
			return 1;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((busyBy == null) ? 0 : busyBy.hashCode());
		result = prime * result + ((department == null) ? 0 : department.hashCode());
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + id;
		result = prime * result + ((ip == null) ? 0 : ip.hashCode());
		result = prime * result + (isBusy ? 1231 : 1237);
		result = prime * result + (isMqttResource ? 1231 : 1237);
		result = prime * result + (isRemote ? 1231 : 1237);
		result = prime * result + (isStationary ? 1231 : 1237);
		result = prime * result + ((latitude == null) ? 0 : latitude.hashCode());
		result = prime * result + ((longitude == null) ? 0 : longitude.hashCode());
		result = prime * result + maxSlots;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Resource other = (Resource) obj;
		if (busyBy == null) {
			if (other.busyBy != null)
				return false;
		} else if (!busyBy.equals(other.busyBy))
			return false;
		if (department == null) {
			if (other.department != null)
				return false;
		} else if (!department.equals(other.department))
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (id != other.id)
			return false;
		if (ip == null) {
			if (other.ip != null)
				return false;
		} else if (!ip.equals(other.ip))
			return false;
		if (isBusy != other.isBusy)
			return false;
		if (isMqttResource != other.isMqttResource)
			return false;
		if (isRemote != other.isRemote)
			return false;
		if (isStationary != other.isStationary)
			return false;
		if (latitude == null) {
			if (other.latitude != null)
				return false;
		} else if (!latitude.equals(other.latitude))
			return false;
		if (longitude == null) {
			if (other.longitude != null)
				return false;
		} else if (!longitude.equals(other.longitude))
			return false;
		if (maxSlots != other.maxSlots)
			return false;
		if (mqttCommands == null) {
			if (other.mqttCommands != null)
				return false;
		} else if (!mqttCommands.equals(other.mqttCommands))
			return false;
		if (mqttDataReceiveTopic == null) {
			if (other.mqttDataReceiveTopic != null)
				return false;
		} else if (!mqttDataReceiveTopic.equals(other.mqttDataReceiveTopic))
			return false;
		if (!Arrays.equals(mqttDataReceived, other.mqttDataReceived))
			return false;
		if (mqttDataSendTopic == null) {
			if (other.mqttDataSendTopic != null)
				return false;
		} else if (!mqttDataSendTopic.equals(other.mqttDataSendTopic))
			return false;
		if (!Arrays.equals(mqttDataToSend, other.mqttDataToSend))
			return false;
		if (mqttLastPing == null) {
			if (other.mqttLastPing != null)
				return false;
		} else if (!mqttLastPing.equals(other.mqttLastPing))
			return false;
		if (mqttOnline != other.mqttOnline)
			return false;
		if (mqttUUID == null) {
			if (other.mqttUUID != null)
				return false;
		} else if (!mqttUUID.equals(other.mqttUUID))
			return false;
		if (mqttValues == null) {
			if (other.mqttValues != null)
				return false;
		} else if (!mqttValues.equals(other.mqttValues))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (reservations == null) {
			if (other.reservations != null)
				return false;
		} else if (!reservations.equals(other.reservations))
			return false;
		if (resourceGroup == null) {
			if (other.resourceGroup != null)
				return false;
		} else if (!resourceGroup.equals(other.resourceGroup))
			return false;
		return true;
	}
}
