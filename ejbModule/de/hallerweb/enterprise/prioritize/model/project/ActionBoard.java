package de.hallerweb.enterprise.prioritize.model.project;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import de.hallerweb.enterprise.prioritize.model.PObject;
import de.hallerweb.enterprise.prioritize.model.security.User;


/**
 * ActionBoard.java - Holds ActionBoardEntry's. It is assigned either to a department, a user or a project
 * and other objects can "subscribe" themselves to receive changes.
 * @author peter
 *
 */
@Entity
public class ActionBoard {

	@Id
	@GeneratedValue
	private int id;
	
	private String name;
	private String description;
	
	@OneToOne
	private PObject owner;
	
	@OneToMany
	private List<ActionBoardEntry> entries;
	
	@OneToMany
	private List<User> subscribers;

	
	public String getName() {
		return name;
	}

	public void setDescriprion(String desc) {
		this.description = desc;
	}

	public String getDescriprion() {
		return description;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getId() {
		return id;
	}
}
