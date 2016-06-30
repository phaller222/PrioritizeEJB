package de.hallerweb.enterprise.prioritize.model.usersetting;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;

import de.hallerweb.enterprise.prioritize.model.resource.Resource;
import de.hallerweb.enterprise.prioritize.model.security.User;

/**
 * JPA entity to represent an {@link UserPreference}.
 * UserPreferences store the current user settings.
 * 
 * <p> Copyright: (c) 2014
 * </p>
 * <p>
 * Peter Haller
 * </p>
 * 
 * @author peter
 */
@Entity
public class UserPreference {

	@Id
	@GeneratedValue
	int id;

	@OneToOne
	User owner;

	@ManyToMany(fetch = FetchType.EAGER)
	List<Resource> watchedResources;

	public UserPreference() {
		// Empty constructor due to EJB requirement
	}

	public UserPreference(User owner) {
		this.owner = owner;

	}

	public int getId() {
		return id;
	}

	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	public List<Resource> getWatchedResources() {
		return watchedResources;
	}

	public void setWatchedResources(List<Resource> resources) {
		this.watchedResources = resources;
	}

	public boolean addWatchedResource(Resource res) {
		if (!this.watchedResources.contains(res)) {
		boolean added = this.watchedResources.add(res);
		return added;
		} else return false;

	}

	public boolean removeWatchedResource(Resource res) {
		Resource resToRemove = null;
		for (Resource resource : this.watchedResources) {
			if (res.getId() == resource.getId()) {
				resToRemove = resource;
			}
		}
		if (resToRemove != null) {
			try {
				// TODO: Klären warum dies 2 mal aufgerufen werden muss!!!
				this.watchedResources.remove(resToRemove);

				this.watchedResources.remove(resToRemove);
			} catch (Exception ex) {
				ex.printStackTrace();
				// TODO: Klären warum dies 2 mal aufgerufen werden muss!!!
			}
			return true;
		} else
			return false;
	}
}
