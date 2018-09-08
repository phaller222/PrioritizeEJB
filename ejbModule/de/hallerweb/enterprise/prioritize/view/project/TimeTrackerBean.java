package de.hallerweb.enterprise.prioritize.view.project;

import java.io.Serializable;
import java.util.List;

import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import de.hallerweb.enterprise.prioritize.controller.LoggingController;
import de.hallerweb.enterprise.prioritize.controller.nfc.NFCUnitController;
import de.hallerweb.enterprise.prioritize.controller.nfc.counter.IndustrieCounterController;
import de.hallerweb.enterprise.prioritize.controller.project.task.TaskController;
import de.hallerweb.enterprise.prioritize.controller.project.task.TimeTrackerController;
import de.hallerweb.enterprise.prioritize.controller.security.SessionController;
import de.hallerweb.enterprise.prioritize.model.calendar.TimeSpan;
import de.hallerweb.enterprise.prioritize.model.project.task.Task;
import de.hallerweb.enterprise.prioritize.model.project.task.TimeTracker;

@Named
@SessionScoped
public class TimeTrackerBean implements Serializable {

	@EJB
	LoggingController log;

	@Inject
	SessionController sessionController;

	@EJB
	IndustrieCounterController industrieCounterController;

	@EJB
	NFCUnitController nfcController;

	@EJB
	TimeTrackerController timeTrackerController;

	@EJB
	TaskController taskController;

	@Named
	public List<TimeTracker> getTrackers() {
		return timeTrackerController.getAllTimeTrackers(sessionController.getUser());
	}

	public List<TimeSpan> getTimeSpentForTask(Task task) {
		Task managedTask = taskController.findTaskById(task.getId());
		return managedTask.getTimeSpent();
	}

}
