package de.hallerweb.enterprise.prioritize.view.calendar;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.bean.ManagedBean;
import javax.inject.Named;

import org.primefaces.event.ScheduleEntryMoveEvent;
import org.primefaces.event.ScheduleEntryResizeEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultScheduleEvent;
import org.primefaces.model.LazyScheduleModel;
import org.primefaces.model.ScheduleEvent;
import org.primefaces.model.ScheduleModel;

import de.hallerweb.enterprise.prioritize.controller.CompanyController;
import de.hallerweb.enterprise.prioritize.controller.calendar.CalendarController;
import de.hallerweb.enterprise.prioritize.controller.resource.ResourceReservationController;
import de.hallerweb.enterprise.prioritize.controller.security.AuthorizationController;
import de.hallerweb.enterprise.prioritize.controller.security.SessionController;
import de.hallerweb.enterprise.prioritize.controller.security.UserRoleController;
import de.hallerweb.enterprise.prioritize.model.Department;
import de.hallerweb.enterprise.prioritize.model.calendar.TimeSpan;
import de.hallerweb.enterprise.prioritize.model.resource.ResourceReservation;
import de.hallerweb.enterprise.prioritize.model.security.User;

@ManagedBean
@Named(value = "calendarView")
@SessionScoped
public class CalendarView implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5946979998839647600L;

	private transient ScheduleModel lazyEventModel;
	private transient ScheduleModel lazyEventModelVacations;
	private transient ScheduleModel lazyEventModelIllness;
	private transient ScheduleEvent event = new DefaultScheduleEvent();

	transient List<Department> departments; // List of departments
	String selectedDepartmentId; // Currently selected Department
	transient Department selectedDepartment = null;

	@EJB
	private ResourceReservationController resourceReservationController;
	@EJB
	private CompanyController companyController;
	@EJB
	private UserRoleController userRoleController;
	@EJB
	private SessionController sessionController;
	@EJB
	private CalendarController calendarController;

	@Named
	public String getSelectedDepartmentId() {
		return selectedDepartmentId;
	}

	public void setSelectedDepartmentId(String departmentId) {
		this.selectedDepartmentId = departmentId;
		if ((departmentId != null) && (departmentId.length() > 0)) {
			this.selectedDepartment = companyController.findDepartmentById(Integer.parseInt(departmentId));
		} else {
			this.selectedDepartment = null;
		}
	}

	private void updateLazyModel() {
		if (lazyEventModel == null) {
			lazyEventModel = new LazyScheduleModel() {
				@Override
				public void loadEvents(Date start, Date end) {
					createReservationsModel(start, end);
					createVacationsModel(start, end);
					createIllnessModel(start, end);
				}
			};
		}
	}

	private void updateLazyVacationsModel() {
		if (lazyEventModelVacations == null) {
			lazyEventModelVacations = new LazyScheduleModel() {
				@Override
				public void loadEvents(Date start, Date end) {
					createVacationsModel(start, end);
				}
			};
		}
	}

	private void updateLazyIllnessModel() {
		if (lazyEventModelIllness == null) {
			lazyEventModelIllness = new LazyScheduleModel() {

				@Override
				public void loadEvents(Date start, Date end) {
					createIllnessModel(start, end);
				}
			};
		}
	}

	private void createReservationsModel(Date from, Date until) {
		List<ResourceReservation> reservations = null;

		TimeSpan requestedTimeSpan = new TimeSpan();
		requestedTimeSpan.setDateFrom(from);
		requestedTimeSpan.setDateUntil(until);

		if (this.selectedDepartment == null) {
			reservations = resourceReservationController.getAllResourceReservations();
		} else {
			reservations = resourceReservationController.getResourceReservationsForDepartment(this.selectedDepartment.getId());
		}

		if (reservations != null) {
			for (ResourceReservation res : reservations) {
				TimeSpan reservationTimeSpan = res.getTimeSpan();

				if (reservationTimeSpan.intersects(requestedTimeSpan)) {
					DefaultScheduleEvent scheduleEvent = new DefaultScheduleEvent(reservationTimeSpan.getDescription(),
							reservationTimeSpan.getDateFrom(), reservationTimeSpan.getDateUntil(), reservationTimeSpan);
					scheduleEvent.setStyleClass("resourcereservation");
					lazyEventModel.addEvent(scheduleEvent);
				}
			}
		}
	}

	private void createIllnessModel(Date from, Date until) {
		TimeSpan requestedTimeSpan = new TimeSpan();
		requestedTimeSpan.setDateFrom(from);
		requestedTimeSpan.setDateUntil(until);

		if (this.selectedDepartment == null) {
			List<User> users = userRoleController.getAllUsers(AuthorizationController.getSystemUser());
			addUsersIllnessWithinTimeSpan(requestedTimeSpan, users);
		} else {
			List<User> users = userRoleController.getUsersForDepartment(selectedDepartment, AuthorizationController.getSystemUser());
			addUsersIllnessWithinTimeSpan(requestedTimeSpan, users);
		}
	}

	private void addUsersIllnessWithinTimeSpan(TimeSpan requestedTimeSpan, List<User> users) {
		for (User user : users) {
			if (user.getIllness() != null) {
				TimeSpan illnessTimeSpan = user.getIllness();
				if (illnessTimeSpan.intersects(requestedTimeSpan)) {
					DefaultScheduleEvent scheduleEvent = new DefaultScheduleEvent(illnessTimeSpan.getDescription(),
							illnessTimeSpan.getDateFrom(), illnessTimeSpan.getDateUntil(), illnessTimeSpan);
					scheduleEvent.setStyleClass("illness");
					lazyEventModel.addEvent(scheduleEvent);
					lazyEventModelIllness.addEvent(scheduleEvent);
				}
			}
		}
	}

	private void createVacationsModel(Date from, Date until) {
		TimeSpan requestedTimeSpan = new TimeSpan();
		requestedTimeSpan.setDateFrom(from);
		requestedTimeSpan.setDateUntil(until);

		if (this.selectedDepartment == null) {
			List<User> users = userRoleController.getAllUsers(AuthorizationController.getSystemUser());
			addUserVaccationWithinTimeSpan(requestedTimeSpan, users);
		} else {
			List<User> users = userRoleController.getUsersForDepartment(selectedDepartment, AuthorizationController.getSystemUser());
			addUserVaccationWithinTimeSpan(requestedTimeSpan, users);
		}
	}

	private void addUserVaccationWithinTimeSpan(TimeSpan requestedTimeSpan, List<User> users) {
		for (User user : users) {
			for (TimeSpan vacationTimespan : user.getVacation()) {
				if (vacationTimespan.intersects(requestedTimeSpan)) {
					DefaultScheduleEvent scheduleEvent = new DefaultScheduleEvent(vacationTimespan.getDescription(),
							vacationTimespan.getDateFrom(), vacationTimespan.getDateUntil(), vacationTimespan);
					scheduleEvent.setStyleClass("vacations");
					lazyEventModel.addEvent(scheduleEvent);
					lazyEventModelVacations.addEvent(scheduleEvent);
				}
			}
		}
	}

	public ScheduleModel getLazyEventModel() {
		updateLazyModel();
		return lazyEventModel;
	}

	public ScheduleModel getLazyEventModelVacations() {
		updateLazyVacationsModel();
		return lazyEventModelVacations;
	}

	public ScheduleModel getLazyEventModelIllness() {
		updateLazyIllnessModel();
		return lazyEventModelIllness;
	}

	public ScheduleEvent getEvent() {
		return this.event;
	}

	public void setEvent(ScheduleEvent event) {
		this.event = event;
	}

	public void onEventSelect(SelectEvent selectEvent) {
		event = (ScheduleEvent) selectEvent.getObject();
	}

	public void onEventMove(ScheduleEntryMoveEvent event) {
		ScheduleEvent evt = event.getScheduleEvent();
		TimeSpan ts = (TimeSpan) evt.getData();
		ts.setDateFrom(evt.getStartDate());
		ts.setDateUntil(ts.getDateUntil());
		calendarController.mergeTimeSpan(ts);
	}

	public void onEventResize(ScheduleEntryResizeEvent event) {
		ScheduleEvent evt = event.getScheduleEvent();
		TimeSpan ts = (TimeSpan) evt.getData();
		ts.setDateFrom(evt.getStartDate());
		ts.setDateUntil(ts.getDateUntil());
		calendarController.mergeTimeSpan(ts);
	}

}
