package de.hallerweb.enterprise.prioritize.controller.resource;

import java.io.Serializable;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import de.hallerweb.enterprise.prioritize.model.resource.VirtualDevice;


@Stateless
public class VirtualDeviceController implements Serializable {

   @PersistenceContext
	EntityManager em;
//	
	public void createDevice() {
		VirtualDevice vd = new VirtualDevice();
		em.persist(vd);
		System.out.println("------------- VD CREATED. ------------");
	}
	
}
