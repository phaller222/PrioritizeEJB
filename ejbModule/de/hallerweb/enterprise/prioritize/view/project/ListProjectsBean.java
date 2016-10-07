package de.hallerweb.enterprise.prioritize.view.project;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import de.hallerweb.enterprise.prioritize.controller.project.ProjectController;
import de.hallerweb.enterprise.prioritize.controller.security.SessionController;
import de.hallerweb.enterprise.prioritize.model.project.Project;
import de.hallerweb.enterprise.prioritize.model.security.Role;
import de.hallerweb.enterprise.prioritize.model.security.User;

@Named
@SessionScoped
public class ListProjectsBean implements Serializable {

	@EJB
	ProjectController projectController;
	@Inject
	SessionController sessionController;

	private List<Project> projects;

	private Map<Integer, DefaultStreamedContent> projectProgressIcons;

	@PostConstruct
	public void init() {
		this.projects = new ArrayList<Project>();
		this.projectProgressIcons = new HashMap<Integer, DefaultStreamedContent>();

		projects = new ArrayList<Project>();
		User sessionUser = sessionController.getUser();
		if (sessionUser != null) {
			this.projects.addAll(getProjectsForUser(sessionUser.getId()));
			for (Role r : sessionUser.getRoles()) {
				this.projects.addAll(getProjectsByManagerRole(r.getId()));
			}

			// Build graphic for project progress
			for (Project p : projects) {
				renderProgress(p);
			}
		}

	}

	public List<Project> getProjects() {
		return projectController.findProjectsByManagerRole(sessionController.getUser().getRoles().iterator().next().getId());

	}

	public void setProjects(List<Project> projects) {
		this.projects = projects;
	}

	private List<Project> getProjectsByManagerRole(int roleId) {
		List<Project> projects = projectController.findProjectsByManagerRole(roleId);
		if (projects != null && !projects.isEmpty()) {
			return projects;
		} else {
			return new ArrayList<Project>();
		}
	}

	private List<Project> getProjectsForUser(int userId) {
		List<Project> projects = projectController.findProjectsByUser(userId);
		if (projects != null && !projects.isEmpty()) {
			return projects;
		} else {
			return new ArrayList<Project>();
		}
	}

	public String showBlackboard() {
		return "blackboard";
	}

	public String editProject() {
		return "editproject";
	}

	private DefaultStreamedContent renderProgress(Project project) {
		
		Project p = projectController.findProjectById(project.getId());
		
		int percentage = p.getProgress().getProgress();
		BufferedImage img = new BufferedImage(130, 10, BufferedImage.TYPE_INT_ARGB);
		Graphics2D gc = img.createGraphics();
		
		gc.setColor(Color.BLACK);
		//gc.drawLine(0, 0, 2, 10);
		//gc.drawLine(100, 0, 2, 10);
		
//		gc.drawString("0%", 1, 1);
//		gc.drawString("100%", 100, 1);
		
		gc.setColor(new Color(204,186,244));
		gc.fillRect(0, 0, percentage, 10);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			ImageIO.write(img, "png", os);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new DefaultStreamedContent(new ByteArrayInputStream(os.toByteArray()), "image/png");
	}

	public StreamedContent getProjectProgressIcon(Project project) {
		Project p = projectController.findProjectById(project.getId());
		return renderProgress(p);
	}

}
