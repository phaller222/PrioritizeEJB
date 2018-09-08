package de.hallerweb.enterprise.prioritize.view.security;

import java.io.Serializable;

import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import de.hallerweb.enterprise.prioritize.controller.LoggingController;

/**
 *LoggingBean - JSF Backing-Bean to switch logging
 * 
 * <p>
 * Copyright: (c) 2015
 * </p>
 * <p>
 * Peter Haller
 * </p>
 * 
 * @author peter
 */
@Named
@SessionScoped
public class LoggingBean implements Serializable {

	@EJB
	LoggingController controller;

	boolean loggingEnabled;

	public String enableLogging() {
		LoggingController.enableLogging();
		return "index";
	}

	public String disableLogging() {
		LoggingController.disableLogging();
		return "index";
	}

	@Named
	public boolean isLoggingEnabled() {
		return controller.isLoggingEnabled();
	}

}
