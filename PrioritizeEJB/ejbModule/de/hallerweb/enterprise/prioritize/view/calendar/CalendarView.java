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
package de.hallerweb.enterprise.prioritize.view.calendar;

import de.hallerweb.enterprise.prioritize.controller.CompanyController;
import de.hallerweb.enterprise.prioritize.controller.calendar.CalendarController;
import de.hallerweb.enterprise.prioritize.controller.resource.ResourceReservationController;
import de.hallerweb.enterprise.prioritize.controller.security.AuthorizationController;
import de.hallerweb.enterprise.prioritize.controller.security.UserRoleController;
import de.hallerweb.enterprise.prioritize.model.Department;
import de.hallerweb.enterprise.prioritize.model.calendar.TimeSpan;
import de.hallerweb.enterprise.prioritize.model.resource.ResourceReservation;
import de.hallerweb.enterprise.prioritize.model.security.User;
import org.primefaces.event.ScheduleEntryMoveEvent;
import org.primefaces.event.ScheduleEntryResizeEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultScheduleEvent;
import org.primefaces.model.LazyScheduleModel;
import org.primefaces.model.ScheduleEvent;
import org.primefaces.model.ScheduleModel;

import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.bean.ManagedBean;
import javax.inject.Named;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

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
    private transient ScheduleEvent<Object> event = new DefaultScheduleEvent<>();

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
    private AuthorizationController authController;
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
                public void loadEvents(LocalDateTime start, LocalDateTime end) {
                    createReservationsModel(DateTimeUtil.toDate(start), DateTimeUtil.toDate(end));
                    createVacationsModel(DateTimeUtil.toDate(start), DateTimeUtil.toDate(end));
                    createIllnessModel(DateTimeUtil.toDate(start), DateTimeUtil.toDate(end));
                }
            };
        }
    }

    private void updateLazyVacationsModel() {
        if (lazyEventModelVacations == null) {
            lazyEventModelVacations = new LazyScheduleModel() {
                @Override
                public void loadEvents(LocalDateTime start, LocalDateTime end) {
                    createVacationsModel(DateTimeUtil.toDate(start), DateTimeUtil.toDate(end));
                }
            };
        }
    }

    private void updateLazyIllnessModel() {
        if (lazyEventModelIllness == null) {
            lazyEventModelIllness = new LazyScheduleModel() {

                @Override
                public void loadEvents(LocalDateTime start, LocalDateTime end) {
                    createIllnessModel(DateTimeUtil.toDate(start), DateTimeUtil.toDate(end));
                }
            };
        }
    }

    private void createReservationsModel(Date from, Date until) {
        List<ResourceReservation> reservations;

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
                    DefaultScheduleEvent<Object> scheduleEvent = DefaultScheduleEvent.builder()
                            .description(reservationTimeSpan.getDescription())
                            .startDate(DateTimeUtil.toLocalDateTime(reservationTimeSpan.getDateFrom()))
                            .endDate(DateTimeUtil.toLocalDateTime(reservationTimeSpan.getDateUntil()))
                            .data(reservationTimeSpan).build();
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
            List<User> users = userRoleController.getAllUsers(authController.getSystemUser());
            addUsersIllnessWithinTimeSpan(requestedTimeSpan, users);
        } else {
            List<User> users = userRoleController.getUsersForDepartment(selectedDepartment, authController.getSystemUser());
            addUsersIllnessWithinTimeSpan(requestedTimeSpan, users);
        }
    }

    private void addUsersIllnessWithinTimeSpan(TimeSpan requestedTimeSpan, List<User> users) {
        updateLazyModel();
        updateLazyIllnessModel();
        for (User user : users) {
            if (user.getIllness() != null) {
                TimeSpan illnessTimeSpan = user.getIllness();
                if (illnessTimeSpan.intersects(requestedTimeSpan)) {
                    DefaultScheduleEvent<Object> scheduleEvent = DefaultScheduleEvent.builder()
                            .description(illnessTimeSpan.getDescription())
                            .startDate(DateTimeUtil.toLocalDateTime(illnessTimeSpan.getDateFrom()))
                            .endDate(DateTimeUtil.toLocalDateTime(illnessTimeSpan.getDateUntil()))
                            .data(illnessTimeSpan).build();
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
            List<User> users = userRoleController.getAllUsers(authController.getSystemUser());
            addUserVaccationWithinTimeSpan(requestedTimeSpan, users);
        } else {
            List<User> users = userRoleController.getUsersForDepartment(selectedDepartment, authController.getSystemUser());
            addUserVaccationWithinTimeSpan(requestedTimeSpan, users);
        }
    }

    private void addUserVaccationWithinTimeSpan(TimeSpan requestedTimeSpan, List<User> users) {
        updateLazyModel();
        updateLazyVacationsModel();
        for (User user : users) {
            for (TimeSpan vacationTimespan : user.getVacation()) {
                if (vacationTimespan.intersects(requestedTimeSpan)) {
                    DefaultScheduleEvent<Object> scheduleEvent = DefaultScheduleEvent.builder()
                            .description(vacationTimespan.getDescription())
                            .startDate(DateTimeUtil.toLocalDateTime(vacationTimespan.getDateFrom()))
                            .endDate(DateTimeUtil.toLocalDateTime(vacationTimespan.getDateUntil()))
                            .data(vacationTimespan).build();
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

    public ScheduleEvent<Object> getEvent() {
        return this.event;
    }

    public void setEvent(ScheduleEvent<Object> event) {
        this.event = event;
    }

    public void onEventSelect(SelectEvent<Object> selectEvent) {
        event = (ScheduleEvent) selectEvent.getObject();
    }

    public void onEventMove(ScheduleEntryMoveEvent event) {
        ScheduleEvent<Object> evt = event.getScheduleEvent();
        TimeSpan ts = (TimeSpan) evt.getData();
        ts.setDateFrom(DateTimeUtil.toDate(evt.getStartDate()));
        ts.setDateUntil(ts.getDateUntil());
        calendarController.mergeTimeSpan(ts);
    }

    public void onEventResize(ScheduleEntryResizeEvent event) {
        ScheduleEvent<Object> evt = event.getScheduleEvent();
        TimeSpan ts = (TimeSpan) evt.getData();
        ts.setDateFrom(DateTimeUtil.toDate(evt.getStartDate()));
        ts.setDateUntil(ts.getDateUntil());
        calendarController.mergeTimeSpan(ts);
    }

}
