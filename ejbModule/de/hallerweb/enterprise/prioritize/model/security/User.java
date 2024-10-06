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
package de.hallerweb.enterprise.prioritize.model.security;

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
import de.hallerweb.enterprise.prioritize.model.security.payment.BankingAccount;
import de.hallerweb.enterprise.prioritize.model.skill.Skill;
import de.hallerweb.enterprise.prioritize.model.skill.SkillRecord;
import de.hallerweb.enterprise.prioritize.model.usersetting.UserPreference;
import jakarta.persistence.*;

import java.io.Serializable;
import java.util.*;

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
@NamedQuery(name = "findUserByDepartment", query = "select u FROM User u WHERE u.department.id = :deptId")
@NamedQuery(name = "findAllUsers", query = "SELECT u FROM User u ORDER BY u.name")
@NamedQuery(name = "findAllUserNames", query = "SELECT u.name FROM User u ORDER BY u.name")
@NamedQuery(name = "findUserByUsername", query = "SELECT u FROM User u WHERE u.username=?1 ORDER BY u.name")
@NamedQuery(name = "findUserByApiKey", query = "select u FROM User u WHERE u.apiKey = :apiKey")
@JsonIgnoreProperties(value = {"vacation", "searchProperties",})
public class User extends PActor implements PAuthorizedObject, PSearchable, Serializable {

    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_FIRSTNAME = "firstname";
    public static final String PROPERTY_EMAIL = "email";
    public static final String PROPERTY_OCCUPATION = "occupation";
    public static final String PROPERTY_DEPARTMENT = "department";
    public static final String PROPERTY_USERNAME = "username";

    public enum Gender {
        MALE,
        FEEMALE,
        OTHER,
        TECHNICAL_USER
    }

    String name;
    String firstname;
    String username;
    String email;

    String occupation;
    String password;

    @JsonIgnore
    String apiKey;
    @JsonIgnore
    Date lastLogin;

    Date dateOfBirth;
    Gender gender;

    @OneToOne
    Address address;

    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY)
    List<Task> assignedTasks;

    @JsonIgnore
    @ManyToMany(fetch = FetchType.EAGER)
    Set<TimeSpan> vacations = new HashSet<>();

    @JsonIgnore
    @OneToOne
    TimeSpan illness;

    @ManyToOne
    @JsonBackReference(value = "departmentBackRef")
    Department department;

    @ManyToMany(fetch = FetchType.EAGER)
    @JsonBackReference(value = "rolesBackRef")
    Set<Role> roles;

    transient List<SearchProperty> searchProperties;

    @JsonIgnore
    @OneToMany(fetch = FetchType.EAGER)
    Set<SkillRecord> skills;

    @JsonIgnore
    @OneToOne(fetch = FetchType.EAGER)
    UserPreference preference;

    @OneToMany(fetch = FetchType.EAGER)
    private Set<BankingAccount> bankingAccounts;

    public Set<BankingAccount> getBankingAccounts() {
        return bankingAccounts;
    }

    public void setBankingAccounts(Set<BankingAccount> bankingAccounts) {
        this.bankingAccounts = bankingAccounts;
    }

    public User(String username) {
        this.username = username;
        this.name = username;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Set<TimeSpan> getVacations() {
        return vacations;
    }

    public void setVacations(Set<TimeSpan> vacations) {
        this.vacations = vacations;
    }

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


    public static User newInstane(User userToCopy) {
        User clonedUser = new User();
        clonedUser.setApiKey(userToCopy.apiKey);
        clonedUser.setEmail(userToCopy.email);
        clonedUser.setIllness(userToCopy.illness);
        clonedUser.setLastLogin(userToCopy.lastLogin);
        clonedUser.setName(userToCopy.name);
        clonedUser.setFirstname(userToCopy.getFirstname());
        clonedUser.setOccupation(userToCopy.occupation);
        clonedUser.setPassword(userToCopy.password);
        clonedUser.setUsername(userToCopy.username);
        clonedUser.setAddress(userToCopy.getAddress());
        return clonedUser;
    }

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

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
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

    @Override
    public int getId() {
        return id;
    }

    public void addRole(Role role) {
        roles.add(role);
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

    public void removeBankingAccount(BankingAccount account) {
        List<BankingAccount> accountsToRemove = new ArrayList<>();
        for (BankingAccount ac : bankingAccounts) {
            if (ac.getId() == account.getId()) {
                accountsToRemove.add(ac);
                break;
            }
        }

        for (BankingAccount accountToRemove : accountsToRemove) {
            bankingAccounts.remove(accountToRemove);
        }
    }


    private SearchResult generateResult(String excerpt) {
        SearchResult result = new SearchResult();
        result.setResult(this);
        result.setResultType(SearchResultType.USER);
        result.setExcerpt(excerpt);
        result.setProvidesExcerpt(true);
        result.setSubresults(new HashSet<>());
        return result;
    }

    @Override
    public List<SearchResult> find(String phrase) {
        ArrayList<SearchResult> results = new ArrayList<>();
        SearchResult result;
        // Search username
        if (this.username.toLowerCase().contains(phrase)) {
            // Match found
            result = generateResult(this.getUsername() + " - " + this.getOccupation() + " - " + this.getEmail());
            results.add(result);
            return results;
        }
        if (this.name.toLowerCase().contains(phrase)) {
            // Match found
            result = generateResult(this.getUsername() + " - " + this.getOccupation() + " - " + this.getEmail());
            results.add(result);
            return results;
        }

        if (this.firstname.toLowerCase().contains(phrase)) {
            // Match found
            result = generateResult(this.getUsername() + " - " + this.getOccupation() + " - " + this.getEmail());
            results.add(result);
            return results;
        }


        if (this.email.contains(phrase)) {
            // Match found
            result = generateResult(this.getUsername() + " - " + this.getOccupation() + " - " + this.getEmail());
            results.add(result);
            return results;
        }

        if (this.occupation.contains(phrase)) {
            // Match found
            result = generateResult(this.getUsername() + " - " + this.getOccupation() + " - " + this.getEmail());
            results.add(result);
            return results;
        }

        for (SkillRecord skillRecord : this.skills) {
            Skill skill = skillRecord.getSkill();
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
                if (skillRecord.getSkill().getName().toLowerCase().contains(phrase.toLowerCase())) {
                    // Match found
                    SearchResult result = new SearchResult();
                    result.setResult(this);
                    result.setResultType(SearchResultType.USER);
                    result.setProvidesExcerpt(true);
                    result.setExcerpt(skillRecord.getUser().getName() + " Skill: " + skillRecord.getSkill().getDescription());
                    result.setSubresults(new HashSet<>());
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
