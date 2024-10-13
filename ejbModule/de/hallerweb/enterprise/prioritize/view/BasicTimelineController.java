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

package de.hallerweb.enterprise.prioritize.view;

import de.hallerweb.enterprise.prioritize.controller.CompanyController;
import de.hallerweb.enterprise.prioritize.controller.security.AuthorizationController;
import de.hallerweb.enterprise.prioritize.controller.security.SessionController;
import de.hallerweb.enterprise.prioritize.controller.security.UserRoleController;
import de.hallerweb.enterprise.prioritize.model.Company;
import de.hallerweb.enterprise.prioritize.model.Department;
import de.hallerweb.enterprise.prioritize.model.calendar.TimeSpan;
import de.hallerweb.enterprise.prioritize.model.document.DocumentGroup;
import de.hallerweb.enterprise.prioritize.model.document.DocumentInfo;
import de.hallerweb.enterprise.prioritize.model.resource.Resource;
import de.hallerweb.enterprise.prioritize.model.resource.ResourceGroup;
import de.hallerweb.enterprise.prioritize.model.resource.ResourceReservation;
import de.hallerweb.enterprise.prioritize.view.calendar.DateTimeUtil;
import de.hallerweb.enterprise.prioritize.view.document.DocumentBean;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.primefaces.event.timeline.TimelineModificationEvent;
import org.primefaces.model.timeline.TimelineEvent;
import org.primefaces.model.timeline.TimelineModel;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Named
@RequestScoped
public class BasicTimelineController {

    private TimelineModel<Object, Object> model;

    private boolean selectable = true;
    private boolean zoomable = true;
    private boolean moveable = true;
    private boolean stackEvents = true;
    private String eventStyle = "box";
    private boolean axisOnTop;
    private boolean showCurrentTime = true;

    private boolean showNavigation = false;

    private TimelineEvent<Object> selectedTime;

    @EJB
    private UserRoleController userController;
    @EJB
    private CompanyController companyController;
    @Inject
    private DocumentBean documentBean;
    @Inject
    private AuthorizationController authController;

    @Inject
    private SessionController sessionController;
    String contextPath;

    private Date selectedDate;

    private static final String TIMETRAVEL_DRAG = "-Drag to travel in time-";

    public Date getSelectedDate() {
        return selectedDate;
    }

    public void setSelectedDate(Date selectedDate) {
        this.selectedDate = selectedDate;
    }

    @PostConstruct
    protected void initialize() {

        contextPath = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath();
        updateTimeline();
        Calendar calSelected = Calendar.getInstance();
        this.selectedDate = calSelected.getTime();
    }

    public void updateTimeline() {

        if (sessionController.getUser() != null) {
            model = new TimelineModel<>();

            Calendar cal = Calendar.getInstance();
            if (selectedDate == null) {
                selectedDate = cal.getTime();
            }
            selectedTime = TimelineEvent.builder()
                .title(TIMETRAVEL_DRAG)
                .startDate(DateTimeUtil.toLocalDateTime(selectedDate))
                .endDate(DateTimeUtil.toLocalDateTime(selectedDate))
                .editable(true).build();
            model.add(selectedTime);

            // Add the current Users vacation to the Timeline
            Set<TimeSpan> vacation = userController.getVacation(sessionController.getUser(), sessionController.getUser());
            if (vacation != null) {
                for (TimeSpan span : vacation) {
                    TimelineEvent<Object> ev = TimelineEvent.builder()
                        .title("Vacation")
                        .startDate(DateTimeUtil.toLocalDateTime(span.getDateFrom()))
                        .endDate(DateTimeUtil.toLocalDateTime(span.getDateUntil()))
                        .editable(false).build();
                    model.add(ev);
                }
            }

            // Add the current Users illness to the Timeline
            TimeSpan illness = userController.getIllness(sessionController.getUser(), sessionController.getUser());
            if (illness != null) {
                TimelineEvent<Object> ev = TimelineEvent.builder()
                    .title("Illness")
                    .startDate(DateTimeUtil.toLocalDateTime(illness.getDateFrom()))
                    .endDate(DateTimeUtil.toLocalDateTime(illness.getDateUntil()))
                    .editable(false).build();
                model.add(ev);
            }

            // Set zoomlevel
            model.add(TimelineEvent.builder().data("START").startDate(LocalDate.of(2022, 1, 1)).build());
            model.add(TimelineEvent.builder().data("ENDE").startDate(LocalDate.of(2022, 12, 31)).build());


        }
    }

    public void displayResourcesTimeline() {
        model = new TimelineModel<>();
        selectedTime = TimelineEvent.builder().title("TimeMachine(Beta)")
            .startDate(DateTimeUtil.toLocalDateTime(selectedDate))
            .endDate(DateTimeUtil.toLocalDateTime(selectedDate))
            .editable(true).build();
        model.add(selectedTime);

        List<Company> companies = companyController.getAllCompanies(sessionController.getUser());
        for (Company c : companies) {
            List<Department> departments = c.getDepartments();
            for (Department d : departments) {
                if (authController.canRead(d, sessionController.getUser())) {
                    Set<ResourceGroup> groups = d.getResourceGroups();
                    for (ResourceGroup g : groups) {
                        Set<Resource> resources = g.getResources();
                        addResoucesToTimeline(resources);

                    }
                }

            }

        }

    }

    private void addResoucesToTimeline(Set<Resource> resources) {
        for (Resource resource : resources) {

            if (!resource.isAgent() && authController.canRead(resource, sessionController.getUser())) {
                for (ResourceReservation res : resource.getReservations()) {
                    if (resource.isMqttResource() && resource.isMqttOnline()) {
                        model.add(TimelineEvent.builder().title(resource.getName())
                            .startDate(DateTimeUtil.toLocalDateTime(res.getTimeSpan().getDateFrom()))
                            .endDate(DateTimeUtil.toLocalDateTime(res.getTimeSpan().getDateUntil()))
                            .editable(false).styleClass("resourcereservationonline").build());
                    } else {
                        model.add(TimelineEvent.builder().title(resource.getName())
                            .startDate(DateTimeUtil.toLocalDateTime(res.getTimeSpan().getDateFrom()))
                            .endDate(DateTimeUtil.toLocalDateTime(res.getTimeSpan().getDateUntil()))
                            .editable(false).styleClass("resourcereservationoffline").build());
                    }

                }
            }

        }
    }

    public LocalDateTime getMin() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -12);
        return DateTimeUtil.toLocalDateTime(cal.getTime());
    }

    public LocalDateTime getMax() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, 12);
        return DateTimeUtil.toLocalDateTime(cal.getTime());
    }

    public void displayAgentsTimeline() {
        model = new TimelineModel<>();
        selectedTime = TimelineEvent.builder().title("TimeMachine(Beta)")
            .startDate(DateTimeUtil.toLocalDateTime(selectedDate))
            .endDate(DateTimeUtil.toLocalDateTime(selectedDate))
            .editable(true).build();
        model.add(selectedTime);

        List<Company> companies = companyController.getAllCompanies(sessionController.getUser());
        for (Company c : companies) {
            List<Department> departments = c.getDepartments();
            for (Department d : departments) {
                if (authController.canRead(d, sessionController.getUser())) {
                    Set<ResourceGroup> groups = d.getResourceGroups();
                    for (ResourceGroup g : groups) {
                        Set<Resource> resources = g.getResources();
                        addAgentsToTimeline(resources);
                    }
                }

            }

        }

    }

    private void addAgentsToTimeline(Set<Resource> resources) {
        for (Resource resource : resources) {
            if (resource.isAgent() && authController.canRead(resource, sessionController.getUser()) && resource.getMqttLastPing() != null) {
                model.add(TimelineEvent.builder().title(resource.getName())
                    .startDate(DateTimeUtil.toLocalDateTime(resource.getMqttLastPing()))
                    .endDate(DateTimeUtil.toLocalDateTime(resource.getMqttLastPing()))
                    .editable(false).styleClass("resourcereservation").build());
            }

        }
    }

    public void displayDocumentsTimeline() {
        model = new TimelineModel<>();
        selectedTime = TimelineEvent.builder().title(TIMETRAVEL_DRAG).startDate(DateTimeUtil.toLocalDateTime(selectedDate))
            .endDate(DateTimeUtil.toLocalDateTime(selectedDate))
            .editable(true).build();
        model.add(selectedTime);

        List<Company> companies = companyController.getAllCompanies(sessionController.getUser());
        for (Company c : companies) {
            List<Department> departments = c.getDepartments();
            for (Department d : departments) {
                handleDocumentsForTimeline(d);

            }

        }

    }

    private void handleDocumentsForTimeline(Department d) {
        Set<DocumentGroup> groups = d.getDocumentGroups();
        for (DocumentGroup g : groups) {
            Set<DocumentInfo> documents = g.getDocuments();
            for (DocumentInfo docInfo : documents) {
                if (authController.canRead(docInfo, sessionController.getUser())) {
                    if (docInfo.getCurrentDocument().getLastModified().before(selectedDate)) {
                        String iconName = lookupMimeIcon(docInfo.getCurrentDocument().getMimeType());
                        model.add(TimelineEvent.builder().title("<div>" + docInfo.getCurrentDocument().getName() + "</div><img src='" + contextPath + "/images/"
                                + iconName + ".png' style='width:26px;height:26px;'>")
                            .startDate(DateTimeUtil.toLocalDateTime(docInfo.getCurrentDocument().getLastModified()))
                            .endDate(DateTimeUtil.toLocalDateTime(docInfo.getCurrentDocument().getLastModified()))
                            .build());
                    }
                    documentBean.updateDocumentTree();
                }
            }

        }
    }

    public String lookupMimeIcon(String mimeType) {
        switch (mimeType) {
            case "application/msword":
            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document":
                return "icon_word";
            case "application/vnd.ms-excel":
            case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet":
                return "icon_excel";
            case " application/vnd.ms-powerpoint":
            case " application/vnd.openxmlformats-officedocument.presentationml.presentation":
                return "icon_powerpoint";
            case "image/jpg":
            case "image/jpeg":
                return "icon_jpg";
            case "image/png":
                return "icon_png";
            case "image/gif":
                return "icon_gif";
            case "application/pdf":
                return "icon_pdf";
            default:
                return "documentsbig";
        }
    }


    public void onChanged(TimelineModificationEvent<Object> event) {
        this.selectedDate = DateTimeUtil.toDate(event.getTimelineEvent().getStartDate());
        selectedTime = TimelineEvent.builder().title(TIMETRAVEL_DRAG)
            .startDate(DateTimeUtil.toLocalDateTime(selectedDate))
            .endDate(DateTimeUtil.toLocalDateTime(selectedDate)).editable(true).build();
    }

    public TimelineModel<Object, Object> getModel() {
        return model;
    }

    public boolean isSelectable() {
        return selectable;
    }

    public void setSelectable(boolean selectable) {
        this.selectable = selectable;
    }

    public boolean isZoomable() {
        return zoomable;
    }

    public void setZoomable(boolean zoomable) {
        this.zoomable = zoomable;
    }

    public boolean isMoveable() {
        return moveable;
    }

    public void setMoveable(boolean moveable) {
        this.moveable = moveable;
    }

    public boolean isStackEvents() {
        return stackEvents;
    }

    public void setStackEvents(boolean stackEvents) {
        this.stackEvents = stackEvents;
    }

    public String getEventStyle() {
        return eventStyle;
    }

    public void setEventStyle(String eventStyle) {
        this.eventStyle = eventStyle;
    }

    public boolean isAxisOnTop() {
        return axisOnTop;
    }

    public void setAxisOnTop(boolean axisOnTop) {
        this.axisOnTop = axisOnTop;
    }

    public boolean isShowCurrentTime() {
        return showCurrentTime;
    }

    public void setShowCurrentTime(boolean showCurrentTime) {
        this.showCurrentTime = showCurrentTime;
    }

    public boolean isShowNavigation() {
        return showNavigation;
    }

    public void setShowNavigation(boolean showNavigation) {
        this.showNavigation = showNavigation;
    }


}