/*
 * Copyright 2015-2020 Peter Michael Haller and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
		controller.enableLogging();
		return "index";
	}

	public String disableLogging() {
		controller.disableLogging();
		return "index";
	}

	@Named
	public boolean isLoggingEnabled() {
		return controller.isLoggingEnabled();
	}

}
