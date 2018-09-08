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

	@EJB
	LoggingController log;

	@Inject
	SessionController sessionController;

	@EJB
	IndustrieCounterController industrieCounterController;

	@EJB
	NFCUnitController nfcController;

	@Named
	public List<IndustrieCounter> getCounters() {
		return industrieCounterController.getAllCounters(sessionController.getUser());
	}

	public void createDummyCounter() {
		industrieCounterController.createCounter(0, CounterType.NFC);
	}

	public void createCounter(String uuid) {
		industrieCounterController.createCounter(0, CounterType.NFC, uuid, sessionController.getUser());
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
