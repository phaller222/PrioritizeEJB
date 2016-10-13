package de.hallerweb.enterprise.prioritize.view.project;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

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
	
	private Project currentProject;		// Currently selected Project
	

	public Project getCurrentProject() {
		return currentProject;
	}

	public void setCurrentProject(Project currentProject) {
		this.currentProject = currentProject;
	}

	@PostConstruct
	public void init() {
		
	}

	public List<Project> getProjects() {
		this.projects = new ArrayList<Project>();
		User sessionUser = sessionController.getUser();
		if (sessionUser != null) {
			this.projects.addAll(getProjectsForUser(sessionUser.getId()));
			for (Role r : sessionUser.getRoles()) {
				List<Project> managerProjects = getProjectsByManagerRole(r.getId());
				for (Project p : managerProjects) {
					if (!p.getUsers().contains(sessionUser)) {
						this.projects.add(p);
					}
				}
			}
		}
		Collections.sort(projects, (projectA, projectB) -> projectA.getName().compareTo(projectB.getName()));
		return projects;
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
	
	public String editProject() {
		return "editproject";
	}
	
	public String showBlackboard() {
		return "blackboard";
	}
		
	public String showTasks(Project p) {
		setCurrentProject(p);
		return "tasks";
	}
	
}
