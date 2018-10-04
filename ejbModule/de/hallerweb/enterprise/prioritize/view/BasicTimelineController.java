package de.hallerweb.enterprise.prioritize.view;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.event.timeline.TimelineModificationEvent;
import org.primefaces.event.timeline.TimelineRangeEvent;
import org.primefaces.model.timeline.TimelineEvent;
import org.primefaces.model.timeline.TimelineModel;

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
import de.hallerweb.enterprise.prioritize.view.document.DocumentBean;

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
	
//	private Date currentStartDate;
//	private Date currentEndDate;
	
	public Date getSelectedDate() {
		return selectedDate;
	}

	public void setSelectedDate(Date selectedDate) {
		this.selectedDate = selectedDate;
	}

	private boolean showNavigation = false;

	private TimelineEvent selectedTime;

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
	
	@PostConstruct
	protected void initialize() {
		
		// initialize range of timeline to 4 days
//		Calendar cal = Calendar.getInstance();
//		Calendar cal2 = Calendar.getInstance();
//		cal.add(Calendar.HOUR, -48);
//		cal2.add(Calendar.HOUR, 48);
//		this.currentStartDate = cal.getTime();
//		this.currentEndDate = cal2.getTime();
		
		contextPath = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath();
		updateTimeline();
		Calendar calSelected = Calendar.getInstance();
		this.selectedDate = calSelected.getTime();
	}

//	public Date getCurrentStartDate() {
//		return currentStartDate;
//	}
//
//	public void setCurrentStartDate(Date currentStartDate) {
//		this.currentStartDate = currentStartDate;
//	}
//
//	public Date getCurrentEndDate() {
//		return currentEndDate;
//	}
//
//	public void setCurrentEndDate(Date currentEndDate) {
//		this.currentEndDate = currentEndDate;
//	}

	public void updateTimeline() {
		
		if (sessionController.getUser() != null) {
			model = new TimelineModel();

			Calendar cal = Calendar.getInstance();
			Calendar cal2 = Calendar.getInstance();
			// cal.set(2013, Calendar.MAY, 4, 0, 0, 0);
			if (selectedDate == null) {
				selectedDate = cal.getTime();
			}
			selectedTime = new TimelineEvent("-Drag to travel in time-", selectedDate,true);
			model.add(selectedTime);

			// Add the current Users vacation to the Timeline
			Set<TimeSpan> vacation = userController.getVacation(sessionController.getUser(), sessionController.getUser());
			if (vacation != null) {
				for (TimeSpan span : vacation) {
					model.add(new TimelineEvent("Vacation", span.getDateFrom(), span.getDateUntil(), false, "", "vacation"));
				}
			}

			// Add the current Users illness to the Timeline
			TimeSpan illness = userController.getIllness(sessionController.getUser(), sessionController.getUser());
			if (illness != null) {
				model.add(new TimelineEvent("Illness", illness.getDateFrom(), illness.getDateUntil(), false, "", "illness"));
			}
		}
	}

	public void displayResourcesTimeline() {
		model = new TimelineModel();

		Calendar cal = Calendar.getInstance();
		selectedTime = new TimelineEvent("TimeMachine(Beta)", selectedDate, true);
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
						model.add(new TimelineEvent(resource.getName(), res.getTimeSpan().getDateFrom(), res.getTimeSpan().getDateUntil(),
								false, "", "resourcereservationonline"));
					} else {
						model.add(new TimelineEvent(resource.getName(), res.getTimeSpan().getDateFrom(), res.getTimeSpan().getDateUntil(),
								false, "", "resourcereservationoffline"));
					}

				}
			}

		}
	}

	public Date getMin() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, -3);
		return cal.getTime();
	}

	public Date getMax() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, 3);
		return cal.getTime();
	}

	public void displayAgentsTimeline() {
		model = new TimelineModel();
		Calendar cal = Calendar.getInstance();
		selectedTime = new TimelineEvent("TimeMachine(Beta)", selectedDate, true);
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
				model.add(new TimelineEvent(resource.getName(), resource.getMqttLastPing(), false, "", "resourcereservation"));
			}

		}
	}

	public void displayDocumentsTimeline() {
		model = new TimelineModel();

		Calendar cal = Calendar.getInstance();
		selectedTime = new TimelineEvent("-Drag to travel in time-", selectedDate,true);
		model.add(selectedTime);

		List<Company> companies = companyController.getAllCompanies(sessionController.getUser());
		for (Company c : companies) {
			List<Department> departments = c.getDepartments();
			for (Department d : departments) {
				Set<DocumentGroup> groups = d.getDocumentGroups();
				for (DocumentGroup g : groups) {
					Set<DocumentInfo> documents = g.getDocuments();
					for (DocumentInfo docInfo : documents) {
						if (authController.canRead(docInfo, sessionController.getUser())) {
							if(docInfo.getCurrentDocument().getLastModified().before(selectedDate)) {
							String iconName = lookupMimeIcon(docInfo.getCurrentDocument().getMimeType());
							model.add(new TimelineEvent(
									"<div>" + docInfo.getCurrentDocument().getName() + "</div><img src='" + contextPath + "/images/"
											+ iconName + ".png' style='width:26px;height:26px;'>",
									docInfo.getCurrentDocument().getLastModified()));
							}
							documentBean.updateDocumentTree();
						}
					}

				}

			}

		}

	}

	public String lookupMimeIcon(String mimeType) {
		switch (mimeType) {
		case "application/msword":
			return "icon_word";
		case "application/vnd.openxmlformats-officedocument.wordprocessingml.document":
			return "icon_word";
		case "application/vnd.ms-excel":
			return "icon_excel";
		case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet":
			return "icon_excel";
		case " application/vnd.ms-powerpoint":
			return "icon_powerpoint";
		case " application/vnd.openxmlformats-officedocument.presentationml.presentation":
			return "icon_powerpoint";
		case "image/jpg":
			return "icon_jpg";
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

//	public void onSelect(TimelineSelectEvent event) {
//		System.out.println("SELECT: Start " + event.getTimelineEvent().getStartDate());
//		System.out.println("SELECT: End " + event.getTimelineEvent().getEndDate());
//		System.out.println("Object:  " + ((Timeline)event.getSource()).getEventNames().toString());
//	}
//	
//	public void onRangeChange(TimelineRangeEvent event) {
//		
//		this.currentStartDate = event.getStartDate();
//		this.currentEndDate = event.getEndDate();
//	}
	
	public void onChanged(TimelineModificationEvent event) {
	  this.selectedDate = event.getTimelineEvent().getStartDate();
	  selectedTime = new TimelineEvent("-Drag to travel in time-", selectedDate, true);
	}
	
//	public void onEdit(TimelineModificationEvent event) {
//		System.out.println("EDIT: Start " + event.getTimelineEvent().getStartDate());
//		System.out.println("EDIT: End " + event.getTimelineEvent().getEndDate());
//		System.out.println("Object:  " + event.getSource().toString());
//	}
	
	
	public void onRangeChanged(TimelineRangeEvent event) {
		
	}
//	public void onSelect(TimelineSelectEvent event) {
//		TimelineEvent timelineEvent = event.getTimelineEvent();
//		this.selectedEvent = timelineEvent;
//		if (selectedEvent.getData() instanceof DocumentTreeInfo) {
//			DocumentTreeInfo info = (DocumentTreeInfo) selectedEvent.getData();
//			DocumentInfo docInfo = info.getDocumentInfo();
//			try {
//				sendDocumentFromTree(info.getName(), docInfo.getCurrentDocument().getMimeType(), docInfo.getCurrentDocument().getData());
//			} catch (IOException e1) {
//				Logger.getLogger(getClass().getName()).log(Level.SEVERE, e1.getMessage(), e1);
//			}
//		}
//		FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Selected event:", timelineEvent.getData().toString());
//		FacesContext.getCurrentInstance().addMessage(null, msg);
//	}
//
//	public void sendDocumentFromTree(String filename, String contentType, byte[] data) throws IOException {
//		FacesContext fc = FacesContext.getCurrentInstance();
//		ExternalContext ec = fc.getExternalContext();
//
//		ec.responseReset();
//		ec.setResponseContentType(contentType);
//		ec.setResponseContentLength(data.length);
//		ec.setResponseHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
//
//		OutputStream output = ec.getResponseOutputStream();
//		output.write(data);
//
//		fc.responseComplete(); // Important! Otherwise JSF will attempt to render the response which obviously will fail since it's already
//								 // written with a file and closed.
//	}

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