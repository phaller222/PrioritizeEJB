package de.hallerweb.enterprise.prioritize.view;

import java.io.Serializable;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.extensions.event.timeline.TimelineSelectEvent;
import org.primefaces.extensions.model.timeline.TimelineEvent;
import org.primefaces.extensions.model.timeline.TimelineModel;

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

@Named
@SessionScoped
public class BasicTimelineController implements Serializable {

	private TimelineModel model;

	private boolean selectable = true;
	private boolean zoomable = true;
	private boolean moveable = true;
	private boolean stackEvents = true;
	private String eventStyle = "box";
	private boolean axisOnTop;
	private boolean showCurrentTime = true;
	private boolean showNavigation = false;

	private TimelineEvent selectedTime;

	@EJB
	private UserRoleController userController;
	@EJB
	private CompanyController companyController;
	@Inject
	private AuthorizationController authController;

	@Inject
	private SessionController sessionController;

	@PostConstruct
	protected void initialize() {
		updateTimeline();
	}

	public void updateTimeline() {
		if (sessionController.getUser() != null) {
			model = new TimelineModel();

			Calendar cal = Calendar.getInstance();
			// cal.set(2013, Calendar.MAY, 4, 0, 0, 0);
			selectedTime = new TimelineEvent("-Drag to travel in time-", cal.getTime(), true);
			model.add(selectedTime);

			// Add the current Users vacation to the Timeline
			List<TimeSpan> vacation = userController.getVacation(sessionController.getUser());
			if (vacation != null) {
				for (TimeSpan span : vacation) {
					model.add(new TimelineEvent("Vacation", span.getDateFrom(), span.getDateUntil(), false, "", "vacation"));
				}
			}

			// Add the current Users illness to the Timeline
			TimeSpan illness = userController.getIllness(sessionController.getUser());
			if (illness != null) {
				model.add(new TimelineEvent("Illness", illness.getDateFrom(), illness.getDateUntil(), false, "", "illness"));
			}
		}
	}

	public void displayResourcesTimeline() {
		model = new TimelineModel();

		Calendar cal = Calendar.getInstance();
		// cal.set(2013, Calendar.MAY, 4, 0, 0, 0);
		selectedTime = new TimelineEvent("TimeMachine(Beta)", cal.getTime(), true);
		model.add(selectedTime);

		List<Company> companies = companyController.getAllCompanies();
		for (Company c : companies) {
			List<Department> departments = c.getDepartments();
			for (Department d : departments) {
				if (authController.canRead(d, sessionController.getUser())) {
					List<ResourceGroup> groups = d.getResourceGroups();
					for (ResourceGroup g : groups) {
						Set<Resource> resources = g.getResources();
						for (Resource resource : resources) {
							if (authController.canRead(resource, sessionController.getUser())) {
								for (ResourceReservation res : resource.getReservations()) {
									model.add(new TimelineEvent(resource.getName(), res.getTimeSpan().getDateFrom(),
											res.getTimeSpan().getDateUntil(), false, "", "resourcereservation"));
								}
							}
						}

					}
				}

			}

		}

	}

	public void displayAgentsTimeline() {
		model = new TimelineModel();

		Calendar cal = Calendar.getInstance();
		// cal.set(2013, Calendar.MAY, 4, 0, 0, 0);
		selectedTime = new TimelineEvent("TimeMachine(Beta)", cal.getTime(), true);
		model.add(selectedTime);

		List<Company> companies = companyController.getAllCompanies();
		for (Company c : companies) {
			List<Department> departments = c.getDepartments();
			for (Department d : departments) {
				if (authController.canRead(d, sessionController.getUser())) {
					List<ResourceGroup> groups = d.getResourceGroups();
					for (ResourceGroup g : groups) {
						Set<Resource> resources = g.getResources();
						for (Resource resource : resources) {
							if (resource.isAgent()) {
								if (authController.canRead(resource, sessionController.getUser()) && resource.getMqttLastPing() != null) {
									model.add(new TimelineEvent(resource.getName(), resource.getMqttLastPing(), false, "",
											"resourcereservation"));
								}
							}
						}
					}
				}

			}

		}

	}

	public void displayDocumentsTimeline() {
		model = new TimelineModel();

		Calendar cal = Calendar.getInstance();
		// cal.set(2013, Calendar.MAY, 4, 0, 0, 0);
		selectedTime = new TimelineEvent("-Drag to travel in time-", cal.getTime(), true);
		model.add(selectedTime);

		List<Company> companies = companyController.getAllCompanies();
		for (Company c : companies) {
			List<Department> departments = c.getDepartments();
			for (Department d : departments) {
				List<DocumentGroup> groups = d.getDocumentGroups();
				for (DocumentGroup g : groups) {
					Set<DocumentInfo> documents = g.getDocuments();
					for (DocumentInfo docInfo : documents) {
						if (authController.canRead(docInfo, sessionController.getUser())) {
							model.add(new TimelineEvent(docInfo.getCurrentDocument().getName(),
									docInfo.getCurrentDocument().getLastModified()));
						}
					}

				}

			}

		}
	}

	public void onSelect(TimelineSelectEvent e) {
		TimelineEvent timelineEvent = e.getTimelineEvent();

		// FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Selected event:", timelineEvent.getData().toString());
		// FacesContext.getCurrentInstance().addMessage(null, msg);
	}

	public TimelineModel getModel() {
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