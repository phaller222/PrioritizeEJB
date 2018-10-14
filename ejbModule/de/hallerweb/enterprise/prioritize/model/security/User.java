package de.hallerweb.enterprise.prioritize.model.security;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import de.hallerweb.enterprise.prioritize.model.Address;
import de.hallerweb.enterprise.prioritize.model.Department;
import de.hallerweb.enterprise.prioritize.model.calendar.TimeSpan;
import de.hallerweb.enterprise.prioritize.model.project.task.PActor;
import de.hallerweb.enterprise.prioritize.model.project.task.Task;
import de.hallerweb.enterprise.prioritize.model.search.PSearchable;
import de.hallerweb.enterprise.prioritize.model.search.SearchProperty;
import de.hallerweb.enterprise.prioritize.model.search.SearchProperty.SearchPropertyType;
import de.hallerweb.enterprise.prioritize.model.search.SearchResult;
import de.hallerweb.enterprise.prioritize.model.search.SearchResultType;
import de.hallerweb.enterprise.prioritize.model.skill.Skill;
import de.hallerweb.enterprise.prioritize.model.skill.SkillRecord;
import de.hallerweb.enterprise.prioritize.model.usersetting.UserPreference;

/**
 * JPA entity to represent a {@link User}. A User is usually a human beeing, but could also be artificial or a machine. A User can have one
 * or more Roles and belongs to a Company and a Department.
 * 
 * <p>
 * Copyright: (c) 2014
 * </p>
 * <p>
 * Peter Haller
 * </p>
 * 
 * @author peter
 */
@Entity
@NamedQueries({ @NamedQuery(name = "findUserByDepartment", query = "select u FROM User u WHERE u.department.id = :deptId"),
		@NamedQuery(name = "findAllUsers", query = "SELECT u FROM User u ORDER BY u.name"),
		@NamedQuery(name = "findAllUserNames", query = "SELECT u.name FROM User u ORDER BY u.name"),
		@NamedQuery(name = "findUserByUsername", query = "SELECT u FROM User u WHERE u.username=?1 ORDER BY u.name"),
		@NamedQuery(name = "findUserByApiKey", query = "select u FROM User u WHERE u.apiKey = :apiKey") })
@JsonIgnoreProperties(value = { "vacation", "searchProperties", })
public class User extends PActor implements PAuthorizedObject, PSearchable {

	public User(String username) {
		this.username = username;
		this.name = username;
	}

	public static User newInstane(User userToCopy) {
		User clonedUser = new User();
		clonedUser.setApiKey(userToCopy.apiKey);
		clonedUser.setEmail(userToCopy.email);
		clonedUser.setIllness(userToCopy.illness);
		clonedUser.setLastLogin(userToCopy.lastLogin);
		clonedUser.setName(userToCopy.name);
		clonedUser.setOccupation(userToCopy.occupation);
		clonedUser.setPassword(userToCopy.password);
		clonedUser.setUsername(userToCopy.username);
		return clonedUser;
	}

	public static final String PROPERTY_NAME = "name";
	public static final String PROPERTY_EMAIL = "email";
	public static final String PROPERTY_OCCUPATION = "occupation";
	public static final String PROPERTY_DEPARTMENT = "department";
	public static final String PROPERTY_USERNAME = "username";

	String name;
	String username;

	@JsonIgnore
	@OneToMany(fetch = FetchType.LAZY)
	List<Task> assignedTasks;

	public List<Task> getAssignedTasks() {
		return assignedTasks;
	}

	public void setAssignedTasks(List<Task> assignedTasks) {
		this.assignedTasks = assignedTasks;
	}

	public void addAssignedTask(Task task) {
		if (this.assignedTasks == null) {
			this.assignedTasks = new ArrayList<>();
		}
		this.assignedTasks.add(task);
	}

	public void removeAssignedTask(Task task) {
		this.assignedTasks.remove(task);
	}

	@JsonIgnore
	String email;
	@JsonIgnore
	String occupation;
	@JsonIgnore
	String password;
	@JsonIgnore
	String apiKey;
	@JsonIgnore
	Date lastLogin;

	transient List<SearchProperty> searchProperties;

	@JsonIgnore
	@ManyToMany(fetch = FetchType.EAGER)
	Set<TimeSpan> vacations = new HashSet<>();

	@JsonIgnore
	@OneToOne
	TimeSpan illness;
	
	@ManyToOne
	@JsonBackReference
	Department department;

	@ManyToMany(fetch = FetchType.EAGER)
	@JsonBackReference
	Set<Role> roles;

	@JsonIgnore
	@OneToMany(fetch = FetchType.EAGER)
	Set<SkillRecord> skills;

	@JsonIgnore
	@OneToOne(fetch = FetchType.EAGER)
	UserPreference preference;
	
	@OneToOne
	Address address;
	

	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

	public Date getLastLogin() {
		return lastLogin;
	}

	public void setLastLogin(Date lastLogin) {
		this.lastLogin = lastLogin;
	}
	
	public TimeSpan getIllness() {
		return illness;
	}

	public void setIllness(TimeSpan illness) {
		this.illness = illness;
	}

	public Set<TimeSpan> getVacation() {
		return vacations;
	}

	public void setVacation(Set<TimeSpan> vacation) {
		this.vacations = vacation;
	}

	public void addVacation(TimeSpan timespan) {
		this.vacations.add(timespan);
	}

	public void removeVacation(int timeSpanId) {
		TimeSpan timespanToRemove = null;
		for (TimeSpan ts : vacations) {
			if (ts.getId() == timeSpanId) {
				timespanToRemove = ts;
			}
		}
		if (timespanToRemove != null) {
			this.vacations.remove(timespanToRemove);
		}
	}

	public void removeIllness() {
		this.illness = null;
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

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public UserPreference getPreference() {
		return preference;
	}

	public void setPreference(UserPreference preference) {
		this.preference = preference;
	}

	public User() {
		super();
		roles = new HashSet<>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getOccupation() {
		return occupation;
	}

	public void setOccupation(String occupation) {
		this.occupation = occupation;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apikey) {
		this.apiKey = apikey;
	}

	public Department getDepartment() {
		return department;
	}

	public void setDepartment(Department department) {
		this.department = department;
	}

	public Set<Role> getRoles() {
		return roles;
	}

	public void setRoles(Set<Role> roles) {
		this.roles = roles;
	}

	public int getId() {
		return id;
	}

	public void addRole(Role role) {
		if (!roles.contains(role)) {
			roles.add(role);
		}
	}

	public void removeRole(Role role) {
		List<Role> rolesToRemove = new ArrayList<>();
		for (Role r : roles) {
			if (r.getId() == role.getId()) {
				rolesToRemove.add(r);
				break;
			}
		}

		for (Role r : rolesToRemove) {
			roles.remove(r);
		}
	}

	private SearchResult generateResult(String excerpt) {
		SearchResult result = new SearchResult();
		result.setResult(this);
		result.setResultType(SearchResultType.USER);
		result.setExcerpt(excerpt);
		result.setProvidesExcerpt(true);
		result.setSubresults(new HashSet<SearchResult>());
		return result;
	}

	@Override
	public List<SearchResult> find(String phrase) {
		ArrayList<SearchResult> results = new ArrayList<>();
		SearchResult result;
		// Search username
		if (this.username.toLowerCase().indexOf(phrase) != -1) {
			// Match found
			result = generateResult(this.getUsername() + " - " + this.getOccupation() + " - " + this.getEmail());
			results.add(result);
			return results;
		}
		if (this.name.toLowerCase().indexOf(phrase) != -1) {
			// Match found
			result = generateResult(this.getUsername() + " - " + this.getOccupation() + " - " + this.getEmail());
			results.add(result);
			return results;
		}

		if (this.email.indexOf(phrase) != -1) {
			// Match found
			result = generateResult(this.getUsername() + " - " + this.getOccupation() + " - " + this.getEmail());
			results.add(result);
			return results;
		}

		if (this.occupation.indexOf(phrase) != -1) {
			// Match found
			result = generateResult(this.getUsername() + " - " + this.getOccupation() + " - " + this.getEmail());
			results.add(result);
			return results;
		}

		for (SkillRecord record : this.skills) {
			Skill skill = record.getSkill();
			if ((skill.getName().indexOf(phrase) != 0) || (skill.getDescription().indexOf(phrase) != 0)) {
				result = generateResult(this.getUsername() + " - " + skill.getName() + " - " + skill.getDescription());
				results.add(result);
				return results;
			}
		}

		return results;
	}

	@Override
	public List<SearchResult> find(String phrase, SearchProperty property) {
		ArrayList<SearchResult> results = new ArrayList<>(); 
		if (property.getType() == SearchPropertyType.SKILL) {
			// TODO: find(...) von Sub-Objekten (hier:SkillRecord) aufrufen
			for (SkillRecord skillRecord : this.getSkills()) {
				if (skillRecord.getSkill().getName().toLowerCase().indexOf(phrase.toLowerCase()) != -1) {
					// Match found
					SearchResult result = new SearchResult();
					result.setResult(this);
					result.setResultType(SearchResultType.USER);
					result.setProvidesExcerpt(true);
					result.setExcerpt(skillRecord.getUser().getName() + " Skill: " + skillRecord.getSkill().getDescription());
					result.setSubresults(new HashSet<SearchResult>());
					results.add(result);
				}
			}
		}
		return results;
	}

	@Override
	public List<SearchProperty> getSearchProperties() {
		if (this.searchProperties == null) {
			searchProperties = new ArrayList<>();
			SearchProperty prop = new SearchProperty("SKILL");
			prop.setName("Skill");
			searchProperties.add(prop);
		}
		return this.searchProperties;
	}

	@Override
	public String toString() {
		return name;
	}

}
