package de.hallerweb.enterprise.prioritize.model.security;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

@Entity
@NamedQueries({ @NamedQuery(name = "findAllObjectTypes", query = "select ot FROM ObservedObjectType ot") })
/**
 * Holds all canonical (absolute) packages names of objects which can be secured by Prioritize PermissionRecord entrys.
 * By adding a new object type (e.g. my.absolute.package.MyEntity) this entity will appear in the administration GUI as
 * an object which can be selected to define permissions on.
 * @author peter
 *
 */
public class ObservedObjectType {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	int id;

	String objectType;

	public String getObjectType() {
		return objectType;
	}

	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}

	public int getId() {
		return id;
	}
	
	@Override
	public String toString() {
		return objectType.substring(objectType.lastIndexOf('.') + 1,objectType.length());
	}

}
