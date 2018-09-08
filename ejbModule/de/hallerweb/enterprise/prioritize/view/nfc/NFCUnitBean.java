package de.hallerweb.enterprise.prioritize.view.nfc;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import de.hallerweb.enterprise.prioritize.controller.LoggingController;
import de.hallerweb.enterprise.prioritize.controller.LoggingController.Action;
import de.hallerweb.enterprise.prioritize.controller.nfc.NFCUnitController;

@Named
@SessionScoped
public class NFCUnitBean implements Serializable {

	@EJB
	LoggingController log;
	@EJB
	NFCUnitController ctl;

	@PostConstruct
	public void init() {
		Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Init...NFCUnit ::::::::::         ::::::::: .......");
		log.log("TEST", "TEST", Action.CREATE, 0);

	}

	public void doTestLog() {
		Logger.getLogger(this.getClass().getName()).log(Level.INFO, "LOG... ::::::::::         ::::::::: .......");
		ctl.createNFCUnit();
	}

}
