package de.hallerweb.enterprise.prioritize.model.security;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Version;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import de.hallerweb.enterprise.prioritize.model.Department;
import de.hallerweb.enterprise.prioritize.model.calendar.TimeSpan;
import de.hallerweb.enterprise.prioritize.model.event.Event;
import de.hallerweb.enterprise.prioritize.model.event.PEventObject;
import de.hallerweb.enterprise.prioritize.model.event.PObjectType;
import de.hallerweb.enterprise.prioritize.model.search.PSearchable;
import de.hallerweb.enterprise.prioritize.model.search.SearchProperty;
import de.hallerweb.enterprise.prioritize.model.search.SearchResult;
import de.hallerweb.enterprise.prioritize.model.search.SearchResultType;
import de.hallerweb.enterprise.prioritize.model.skill.Skill;
import de.hallerweb.enterprise.prioritize.model.skill.SkillRecord;

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
		@NamedQuery(name = "findUserByUsername", query = "SELECT u FROM User u WHERE u.username=?1 ORDER BY u.name"),
		@NamedQuery(name = "findUserByApiKey", query = "select u FROM User u WHERE u.apiKey = :apiKey") })
@JsonIgnoreProperties(value = { "vacation", "searchProperties", })
public class User implements PAuthorizedObject, PSearchable, PEventObject {

	@Id
	@GeneratedValue
	int id;

	String name;
	String username;
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

	public Date getLastLogin() {
		return lastLogin;
	}

	public void setLastLogin(Date lastLogin) {
		this.lastLogin = lastLogin;
	}

	transient List<SearchProperty> searchProperties;

	@JsonIgnore
	@ManyToMany(fetch = FetchType.EAGER)
	List<TimeSpan> vacations = new ArrayList<TimeSpan>();

	@JsonIgnore
	@OneToOne
	TimeSpan illness;

	public TimeSpan getIllness() {
		return illness;
	}

	public void setIllness(TimeSpan illness) {
		this.illness = illness;
	}

	public List<TimeSpan> getVacation() {
		return vacations;
	}

	public void setVacation(List<TimeSpan> vacation) {
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

	@ManyToOne(fetch = FetchType.EAGER)
	@JsonBackReference
	Department department;

	@ManyToMany(fetch = FetchType.EAGER)
	@JsonBackReference
	Set<Role> roles;

	@JsonIgnore
	@OneToMany(fetch = FetchType.EAGER)
	Set<SkillRecord> skills;

	@Version
	private int entityVersion; // For optimistic locks

	public User() {
		super();
		roles = new HashSet<Role>();
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

	public void addRole(Role r) {
		if (!roles.contains(r)) {
			roles.add(r);
		}
	}

	public void removeRole(Role role) {
		List<Role> rolesToRemove = new ArrayList<Role>();
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
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
		User other = (User) obj;
		if (id != other.id)
			return false;
		return true;
	}

	@Override
	public List<SearchResult> find(String phrase) {
		ArrayList<SearchResult> results = new ArrayList<SearchResult>();
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
	public List<SearchResult> find(String phrase, SearchProperty property) {
		ArrayList<SearchResult> results = new ArrayList<SearchResult>();
		switch (property.getType()) {
		case SKILL:
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
			break;
		default:
			break;

		}
		return results;
	}

	@Override
	public List<SearchProperty> getSearchProperties() {
		if (this.searchProperties == null) {
			searchProperties = new ArrayList<SearchProperty>();
			SearchProperty prop = new SearchProperty("SKILL");
			prop.setName("Skill");
			searchProperties.add(prop);
		}
		return this.searchProperties;
	}

	@Override
	public PObjectType getObjectType() {
		return PObjectType.USER;
	}
}
