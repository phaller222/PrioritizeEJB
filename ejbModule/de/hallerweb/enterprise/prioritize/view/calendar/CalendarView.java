package de.hallerweb.enterprise.prioritize.view.calendar;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
//import javax.faces.application.FacesMessage;
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
import de.hallerweb.enterprise.prioritize.controller.resource.ResourceController;
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
	private ScheduleModel lazyEventModel;
	private ScheduleModel lazyEventModelVacations;
	private ScheduleModel lazyEventModelIllness;
	private ScheduleEvent event = new DefaultScheduleEvent();

	List<Department> departments; // List of departments
	String selectedDepartmentId; // Currently selected Department
	Department selectedDepartment = null;

	@EJB
	private ResourceController resourceController;

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
		} else
			this.selectedDepartment = null;
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

		if (this.selectedDepartment == null) {
			reservations = resourceController.getAllResourceReservations();
		} else {
			reservations = resourceController.getResourceReservationsForDepartment(this.selectedDepartment.getId());
		}

		if (reservations != null) {
			for (ResourceReservation res : reservations) {
				DefaultScheduleEvent event = new DefaultScheduleEvent(res.getTimeSpan().getDescription(), res.getTimeSpan().getDateFrom(),
						res.getTimeSpan().getDateUntil(), res.getTimeSpan());
				event.setStyleClass("resourcereservation");
				lazyEventModel.addEvent(event);
			}
		}
	}

	private void createIllnessModel(Date from, Date until) {
		if (this.selectedDepartment == null) {
			List<User> users = userRoleController.getAllUsers(AuthorizationController.getSystemUser());
			for (User user : users) {
				if (user.getIllness() != null) {
					TimeSpan illness = user.getIllness();
					DefaultScheduleEvent event = new DefaultScheduleEvent(illness.getDescription(), illness.getDateFrom(),
							illness.getDateUntil(), illness);
					event.setStyleClass("illness");
					lazyEventModel.addEvent(event);
					lazyEventModelIllness.addEvent(event);
				}
			}
		} else {
			List<User> users = userRoleController.getUsersForDepartment(selectedDepartment, AuthorizationController.getSystemUser());
			for (User user : users) {
				if (user.getIllness() != null) {
					TimeSpan illness = user.getIllness();
					DefaultScheduleEvent event = new DefaultScheduleEvent(illness.getDescription(), illness.getDateFrom(),
							illness.getDateUntil(), illness);
					event.setStyleClass("illness");
					lazyEventModel.addEvent(event);
					lazyEventModelIllness.addEvent(event);
				}
			}
		}
	}

	private void createVacationsModel(Date from, Date until) {

		if (this.selectedDepartment == null) {
			List<User> users = userRoleController.getAllUsers(AuthorizationController.getSystemUser());
			for (User user : users) {
				for (TimeSpan timespan : user.getVacation()) {
					DefaultScheduleEvent event = new DefaultScheduleEvent(timespan.getDescription(), timespan.getDateFrom(),
							timespan.getDateUntil(), timespan);
					event.setStyleClass("vacations");
					lazyEventModel.addEvent(event);
					lazyEventModelVacations.addEvent(event);
				}
			}
		} else {

			List<User> users = userRoleController.getUsersForDepartment(selectedDepartment, AuthorizationController.getSystemUser());
			for (User user : users) {
				for (TimeSpan timespan : user.getVacation()) {
					DefaultScheduleEvent event = new DefaultScheduleEvent(timespan.getDescription(), timespan.getDateFrom(),
							timespan.getDateUntil(), timespan);
					event.setStyleClass("vacations");
					lazyEventModel.addEvent(event);
					lazyEventModelVacations.addEvent(event);
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
		System.out.println("Moved.");
	}

	public void onEventResize(ScheduleEntryResizeEvent event) {
		ScheduleEvent evt = event.getScheduleEvent();
		TimeSpan ts = (TimeSpan) evt.getData();
		ts.setDateFrom(evt.getStartDate());
		ts.setDateUntil(ts.getDateUntil());
		calendarController.mergeTimeSpan(ts);
		System.out.println("Resized.");
	}

}
