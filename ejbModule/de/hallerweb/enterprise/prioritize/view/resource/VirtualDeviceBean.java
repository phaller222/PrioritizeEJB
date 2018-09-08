package de.hallerweb.enterprise.prioritize.view.resource;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import de.hallerweb.enterprise.prioritize.controller.LoggingController;
import de.hallerweb.enterprise.prioritize.controller.LoggingController.Action;
import de.hallerweb.enterprise.prioritize.controller.resource.VirtualDeviceController;

@Named
@SessionScoped
public class VirtualDeviceBean implements Serializable {

	@EJB
	LoggingController log;

	@EJB
	VirtualDeviceController ctl;

	@PostConstruct
	public void init() {
		Logger.getLogger(getClass().getName()).log(Level.INFO,"Init...Virtual Device ::::::::::         ::::::::: .......");
		log.log("TEST", "TEST", Action.CREATE, 0);
	}

	public void doTestLog() {
		Logger.getLogger(getClass().getName()).log(Level.INFO,"LOG... ::::::::::         ::::::::: .......");
		ctl.createDevice();
	}

}
