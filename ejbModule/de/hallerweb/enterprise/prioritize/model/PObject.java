package de.hallerweb.enterprise.prioritize.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

@Entity
@Inheritance(strategy=InheritanceType.JOINED)
public abstract class PObject {

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE)
	protected int id;

}
