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
package de.hallerweb.enterprise.prioritize.view.nfc.counter;

import java.io.Serializable;
import java.util.List;

import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import de.hallerweb.enterprise.prioritize.controller.LoggingController;
import de.hallerweb.enterprise.prioritize.controller.nfc.NFCUnitController;
import de.hallerweb.enterprise.prioritize.controller.nfc.counter.IndustrieCounterController;
import de.hallerweb.enterprise.prioritize.controller.nfc.counter.IndustrieCounterController.CounterType;
import de.hallerweb.enterprise.prioritize.controller.security.SessionController;
import de.hallerweb.enterprise.prioritize.model.nfc.NFCCounter;
import de.hallerweb.enterprise.prioritize.model.nfc.counter.IndustrieCounter;

@Named
@SessionScoped
public class IndustrieCounterBean implements Serializable {

	@Inject
	SessionController sessionController;

	@EJB
	IndustrieCounterController industrieCounterController;

	@Named
	public List<IndustrieCounter> getCounters() {
		return industrieCounterController.getAllCounters(sessionController.getUser());
	}

	public void createDummyCounter() {
		industrieCounterController.createCounter(0);
	}

	public void createCounter(String uuid) {
		industrieCounterController.createCounter(uuid, sessionController.getUser());
	}

	public String incCounter(String uuid) {
		industrieCounterController.incCounter(industrieCounterController.getIndustrieCounter(uuid));
		return "index";
	}

	public String decCounter(String uuid) {
		industrieCounterController.decCounter(industrieCounterController.getIndustrieCounter(uuid));
		return "index";
	}

	public long getCounterValue(String uuid) {
		try {
			NFCCounter c = (NFCCounter) industrieCounterController.getIndustrieCounter(uuid).getCounter();
			if (c != null && c.getNfcUnit().getPayload().length() > 0) {
				return c.getValue();
			} else {
				return 10;
			}
		} catch (Exception ex) {
			return -1;
		}
	}

}
