package de.hallerweb.enterprise.prioritize.controller.nfc.counter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import de.hallerweb.enterprise.prioritize.controller.nfc.NFCUnitController;
import de.hallerweb.enterprise.prioritize.controller.security.AuthorizationController;
import de.hallerweb.enterprise.prioritize.model.nfc.NFCUnit;
import de.hallerweb.enterprise.prioritize.model.nfc.PCounter;
import de.hallerweb.enterprise.prioritize.model.nfc.counter.IndustrieCounter;
import de.hallerweb.enterprise.prioritize.model.security.User;

@Stateless
public class IndustrieCounterController implements Serializable {

	@PersistenceContext(unitName = "MySqlDS")
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

				switch (type) {
				case NFC:
					embeddedCounter = nfcUnitController.createNFCCounterWithUUID(uuid);
					break;
				default:
					embeddedCounter = nfcUnitController.createNFCCounterWithUUID(uuid);
				}

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

		switch (type) {
		case NFC:
			embeddedCounter = nfcUnitController.createNFCCounter();
			embeddedCounter.setValue(initialValue);
			break;

		default:
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
			List<IndustrieCounter> counters = q.getResultList();
			return counters;
		} else {
			return new ArrayList<IndustrieCounter>();
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
