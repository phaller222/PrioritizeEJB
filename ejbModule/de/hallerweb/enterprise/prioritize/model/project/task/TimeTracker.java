package de.hallerweb.enterprise.prioritize.model.project.task;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;

import de.hallerweb.enterprise.prioritize.model.Department;
import de.hallerweb.enterprise.prioritize.model.calendar.TimeSpan;
import de.hallerweb.enterprise.prioritize.model.nfc.NFCUnit;
import de.hallerweb.enterprise.prioritize.model.security.PAuthorizedObject;

@Entity(name = "TimeTracker")
@NamedQueries({ @NamedQuery(name = "findAllTimeTrackers", query = "select tc FROM TimeTracker tc"),
		@NamedQuery(name = "findTimeTrackerByUUID", query = "select tc FROM TimeTracker tc WHERE tc.nfcUnit.uuid = :uuid") })
public class TimeTracker implements PAuthorizedObject {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	int id;

	@OneToOne
	Task task;

	@OneToOne
	NFCUnit nfcUnit;

	boolean active;

	@OneToOne
	TimeSpan activeTimeSpan;

	public TimeSpan getActiveTimeSpan() {
		return activeTimeSpan;
	}

	public void setActiveTimeSpan(TimeSpan activeTimeSpan) {
		this.activeTimeSpan = activeTimeSpan;
	}

	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}

	public NFCUnit getNfcUnit() {
		return nfcUnit;
	}

	public void setNfcUnit(NFCUnit nfcUnit) {
		this.nfcUnit = nfcUnit;
	}

	public int getId() {
		return id;
	}

	@Override
	public Department getDepartment() {
		return null;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

}
