package de.hallerweb.enterprise.prioritize.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Inheritance(strategy=InheritanceType.TABLE_PER_CLASS)
public abstract class PObject {

	public int getId() {
		return id;
	}

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE)
	protected int id;

}
