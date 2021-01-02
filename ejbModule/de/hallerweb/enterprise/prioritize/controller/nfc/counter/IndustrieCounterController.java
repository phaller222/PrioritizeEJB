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
package de.hallerweb.enterprise.prioritize.controller.nfc.counter;

import de.hallerweb.enterprise.prioritize.controller.nfc.NFCUnitController;
import de.hallerweb.enterprise.prioritize.controller.security.AuthorizationController;
import de.hallerweb.enterprise.prioritize.model.nfc.NFCUnit;
import de.hallerweb.enterprise.prioritize.model.nfc.PCounter;
import de.hallerweb.enterprise.prioritize.model.nfc.counter.IndustrieCounter;
import de.hallerweb.enterprise.prioritize.model.security.User;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Stateless
public class IndustrieCounterController implements Serializable {

	@PersistenceContext
	EntityManager em;

	@EJB
	AuthorizationController authController;

	public enum CounterType {
		NFC
	}

	@EJB
	NFCUnitController nfcUnitController;

	public IndustrieCounter createCounter(long initialValue, CounterType type, String uuid, String name, String description,
			User sessionUser) {
		if (authController.canCreate(new IndustrieCounter(), sessionUser)) {
			IndustrieCounter counter = createCounter(initialValue, type, uuid, sessionUser);
			editCounter(counter.getCounter().getUuid(), name, description, initialValue);
			return counter;
		} else {
			return null;
		}
	}

	public IndustrieCounter createCounter(long initialValue, CounterType type, String uuid, User sessionUser) {
		if (authController.canCreate(new IndustrieCounter(), sessionUser)) {
			PCounter embeddedCounter;
			IndustrieCounter existingCounter = getIndustrieCounter(uuid);
			if (existingCounter == null) {

				embeddedCounter = nfcUnitController.createNFCCounterWithUUID(uuid);
				IndustrieCounter counter = new IndustrieCounter();
				counter.setCounter(embeddedCounter);
				em.persist(counter);
				return counter;
			} else {
				return existingCounter;
			}
		} else {
			return null;
		}
	}

	public IndustrieCounter createCounter(long initialValue, CounterType type) {
		PCounter embeddedCounter;

		if (type == CounterType.NFC) {
			embeddedCounter = nfcUnitController.createNFCCounter();
			embeddedCounter.setValue(initialValue);
		} else {
			embeddedCounter = nfcUnitController.createNFCCounter();
			embeddedCounter.setValue(initialValue);
		}

		IndustrieCounter counter = new IndustrieCounter();
		counter.setCounter(embeddedCounter);
		em.persist(counter);
		return counter;
	}

	public IndustrieCounter incCounter(IndustrieCounter counter) {
		String uuid = counter.getCounter().getUuid();
		NFCUnit managedUnit = nfcUnitController.findNFCUnitByUUID(uuid);
		long oldValue = Long.parseLong(managedUnit.getPayload());
		long newValue = oldValue;
		if (oldValue < Long.MAX_VALUE) {
			newValue = oldValue + 1;
		}
		managedUnit.setPayload(String.valueOf(newValue));
		nfcUnitController.updateNFCUnit(uuid, managedUnit, null);
		return getIndustrieCounter(uuid);
	}

	public IndustrieCounter decCounter(IndustrieCounter counter) {
		String uuid = counter.getCounter().getUuid();
		NFCUnit managedUnit = nfcUnitController.findNFCUnitByUUID(uuid);
		long oldValue = Long.parseLong(managedUnit.getPayload());
		long newValue = oldValue;
		if (oldValue > 0) {
			newValue = oldValue - 1;
		}
		managedUnit.setPayload(String.valueOf(newValue));
		nfcUnitController.updateNFCUnit(uuid, managedUnit, null);
		return getIndustrieCounter(uuid);
	}

	// TODO: Perform the reset to the original initialValue of the counter,
	//not just to 0!
	public IndustrieCounter resetCounter(IndustrieCounter counter) {
		String uuid = counter.getCounter().getUuid();
		NFCUnit managedUnit = nfcUnitController.findNFCUnitByUUID(uuid);
		long newValue = 0;
		managedUnit.setPayload(String.valueOf(newValue));
		nfcUnitController.updateNFCUnit(uuid, managedUnit, null);
		return getIndustrieCounter(uuid);
	}


	public IndustrieCounter getIndustrieCounter(String uuid) {
		Query q = em.createNamedQuery("findAllIndustrieCounters");
		try {
			List<IndustrieCounter> counters = q.getResultList();
			for (IndustrieCounter c : counters) {
				if (c.getCounter().getUuid().equals(uuid)) {
					return c;
				}
			}
			return null;
		} catch (Exception ex) {
			return null;
		}
	}

	public List<IndustrieCounter> getAllCounters(User sessionUser) {
		if (authController.canRead(new IndustrieCounter(), sessionUser)) {
			Query q = em.createNamedQuery("findAllIndustrieCounters");
			return q.getResultList();
		} else {
			return new ArrayList<>();
		}
	}

	public IndustrieCounter editCounter(String uuid, String name, String description, long value) {
		IndustrieCounter counter = getIndustrieCounter(uuid);
		counter.setName(name);
		counter.setDescription(description);
		counter.getCounter().setValue(value);
		return counter;
	}

}
