package de.hallerweb.enterprise.prioritize.model.resource;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Entity
public class VirtualDevice {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	int id;

	@OneToOne
	Resource wrappedResource;

	public Resource getWrappedResource() {
		return wrappedResource;
	}

	public void setWrappedResource(Resource wrappedResource) {
		this.wrappedResource = wrappedResource;
	}

}
