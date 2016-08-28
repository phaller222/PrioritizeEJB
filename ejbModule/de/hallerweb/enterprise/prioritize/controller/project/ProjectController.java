package de.hallerweb.enterprise.prioritize.controller.project;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import de.hallerweb.enterprise.prioritize.controller.InitializationController;
import de.hallerweb.enterprise.prioritize.controller.document.DocumentController;
import de.hallerweb.enterprise.prioritize.controller.event.EventRegistry;
import de.hallerweb.enterprise.prioritize.controller.project.task.TaskController;
import de.hallerweb.enterprise.prioritize.controller.resource.ResourceController;
import de.hallerweb.enterprise.prioritize.model.PObject;
import de.hallerweb.enterprise.prioritize.model.document.DocumentInfo;
import de.hallerweb.enterprise.prioritize.model.event.Event;
import de.hallerweb.enterprise.prioritize.model.event.PEventConsumerProducer;
import de.hallerweb.enterprise.prioritize.model.project.Project;
import de.hallerweb.enterprise.prioritize.model.project.ProjectProgress;
import de.hallerweb.enterprise.prioritize.model.project.goal.ProjectGoalPropertyRecord;
import de.hallerweb.enterprise.prioritize.model.project.goal.ProjectGoalRecord;
import de.hallerweb.enterprise.prioritize.model.project.task.Task;
import de.hallerweb.enterprise.prioritize.model.project.task.TaskStatus;
import de.hallerweb.enterprise.prioritize.model.resource.Resource;
import de.hallerweb.enterprise.prioritize.model.security.User;

/**
 * TaskController - Manages tasks.
 * @author peter
 *
 */
@Stateless
public class ProjectController extends PEventConsumerProducer {

	@PersistenceContext(unitName = "MySqlDS")
	EntityManager em;

	@Inject
	EventRegistry eventRegistry;

	@EJB
	DocumentController documentController;
	@EJB
	ResourceController resourceController;
	@EJB
	TaskController taskController;

	public Project findProjectById(int id) {
		Query q = em.createNamedQuery("findProjectById");
		q.setParameter("projectId", id);
		Project project = (Project) q.getSingleResult();
		return project;
	}

	public List<Project> findProjectsByManagerRole(int managerRoleId) {
		Query q = em.createNamedQuery("findProjectsByManagerRole");
		q.setParameter("roleId", managerRoleId);
		List<Project> projects = (List<Project>) q.getResultList();
		if (projects.isEmpty()) {
			return new ArrayList<Project>();
		} else {
			return projects;
		}
	}

	public ProjectGoalRecord findProjectGoalRecordById(int id) {
		Query q = em.createNamedQuery("findProjectGoalRecordById");
		q.setParameter("projectGoalRecordId", id);
		ProjectGoalRecord projectGoalRecord = (ProjectGoalRecord) q.getSingleResult();
		return projectGoalRecord;
	}
	
	public List<ProjectGoalRecord> findProjectGoalRecordsByProject(int projectId) {
		Query q = em.createNamedQuery("findProjectGoalRecordByProject");
		q.setParameter("projectId", projectId);
		List<ProjectGoalRecord> projectGoalRecords = (List<ProjectGoalRecord>) q.getResultList();
		if (!projectGoalRecords.isEmpty()) {
			return projectGoalRecords;
		} else {
			return new ArrayList<ProjectGoalRecord>();
		}
	}
	
	public List<ProjectGoalRecord> findActiveProjectGoalRecordsByProject(int projectId) {
		Query q = em.createNamedQuery("findActiveProjectGoalRecordsByProject");
		q.setParameter("projectId", projectId);
		List<ProjectGoalRecord> projectGoalRecords = (List<ProjectGoalRecord>) q.getResultList();
		if (!projectGoalRecords.isEmpty()) {
			return projectGoalRecords;
		} else {
			return new ArrayList<ProjectGoalRecord>();
		}
	}

	public ProjectProgress findProjectProgressById(int id) {
		Query q = em.createNamedQuery("findProjectProgressById");
		q.setParameter("projectProgressId", id);
		ProjectProgress projectProgress = (ProjectProgress) q.getSingleResult();
		return projectProgress;
	}

	public Project createProject(Project project) {
		em.persist(project);
		return project;
	}

	public void removeProject(int projectId) {
		Project project = findProjectById(projectId);
		em.remove(project);
	}

	public List<DocumentInfo> getProjectDocuments(Project p) {
		if (!p.getDocuments().isEmpty()) {
			return p.getDocuments();
		} else {
			return new ArrayList<DocumentInfo>();
		}
	}

	public void addProjectDocument(Project project, DocumentInfo document, User sessionUser) {
		Project managedProject = findProjectById(project.getId());
		DocumentInfo managedDocumentInfo = documentController.getDocumentInfo(document.getId(), sessionUser);
		managedProject.addDocument(managedDocumentInfo);
	}

	public void removeProjectDocument(int projectId, DocumentInfo docInfo) {
		Project project = findProjectById(projectId);
		project.removeDocument(docInfo);
	}

	public void addProjectResource(int projectId, Resource resource, User sessionUser) {
		Project managedProject = findProjectById(projectId);
		Resource managedResource = (Resource) resourceController.getResource(resource.getId(), sessionUser);
		List<Resource> resources = managedProject.getResources();
		managedProject.addResource(managedResource);
	}

	public void removeProjectResource(int projectId, Resource resource) {
		Project project = findProjectById(projectId);
		project.removeResource(resource);
	}

	public void editProject(int projectId, Project detachedProject) {
		Project project = findProjectById(projectId);
		project.setName(detachedProject.getName());
		project.setDescription(detachedProject.getDescription());
		project.setPriority(detachedProject.getPriority());
		project.setBeginDate(detachedProject.getBeginDate());
		project.setDueDate(detachedProject.getDueDate());
		project.setMaxManDays(detachedProject.getMaxManDays());

	}

	public ProjectProgress createProjectProgress(int projectId, List<ProjectGoalRecord> targetGoals, int percentFinished) {
		Project managedProject = findProjectById(projectId);
		ProjectProgress progress = new ProjectProgress();
		if (!targetGoals.isEmpty()) {
			for (ProjectGoalRecord projectGoal : targetGoals) {
				em.persist(projectGoal.getPropertyRecord().getProperty());
				em.persist(projectGoal.getPropertyRecord());
				em.persist(projectGoal.getProjectGoal());
				em.persist(projectGoal);
				progress.addTargetGoal(projectGoal);
			}
		}
		progress.setProgress(percentFinished);
		em.persist(progress);
		managedProject.setProgress(progress);
		return progress;
	}

	// Todo: ??? ziel-Goals müssen als Eigenschaft im Progress-Objekt stehen ??? - evtl. doch nicht.
	/**
	 * Updates the overall progress of a project by calculating the percentage value of completeness of
	 * each active {@link ProjectGoalRecord}
	 * @param projectId
	 * @param currentGoals
	 */
	public void updateProjectProgress(int projectId) {
		Project managedProject = findProjectById(projectId);
		List<ProjectGoalRecord> currentGoals = findActiveProjectGoalRecordsByProject(managedProject.getId());
		ProjectProgress managedProgress = findProjectProgressById(managedProject.getProgress().getId());
		int numGoals = managedProgress.getTargetGoals().size();
		int sum = 0;
		if (!currentGoals.isEmpty()) {
			for (ProjectGoalRecord currentProjectGoalRecord : currentGoals) {
				for (ProjectGoalRecord origProjectGoalRecord : managedProgress.getTargetGoals()) {
					if (origProjectGoalRecord.getProjectGoal().equals(currentProjectGoalRecord.getProjectGoal())) {
						double baseValue = origProjectGoalRecord.getPropertyRecord().getValue();
						double minValue = origProjectGoalRecord.getPropertyRecord().getProperty().getMin();
						double maxValue = origProjectGoalRecord.getPropertyRecord().getProperty().getMax();
						double currentValue = currentProjectGoalRecord.getPropertyRecord().getValue();
						int percentage = 0;
						// The goal is to increase value until it is more than min
						if (baseValue <= minValue) {
							double targetValue = origProjectGoalRecord.getPropertyRecord().getProperty().getMin();
							double base = targetValue - baseValue;
							percentage = (int) (100 - ((targetValue - currentValue) * 100) / base);
							// the goal is to decrease value until it is less than max
						} else if (baseValue >= maxValue) {
							double targetValue = origProjectGoalRecord.getPropertyRecord().getProperty().getMax();
							double base = baseValue - targetValue;
							percentage = (int) (100 + ((targetValue - currentValue) * 100) / base);
							// Goal has already been reached during definition of goal!
						} else {
							percentage = 100;
						}
						currentProjectGoalRecord.setPercentage(percentage);
						sum += percentage;
					}
				}
			}
			managedProgress.setProgress((int) sum / numGoals);
		}
	}

	/**
	 * Creates a copy of the original projectGoalRecord to hold the current progress.
	 * Also creates a new Task for this ProjectGoalRecord and assigns the copy of the
	 * {@link ProjectGoalRecord} to this task.
	 * @param projectGoalRecordId - ID of the original {@link ProjectGoalRecord}.
	 * @return {@link ProjectGoalRecord} - An active copy of the original {@link ProjectGoalRecord}. 
	 */
	public ProjectGoalRecord createTaskForProjectGoal(int projectGoalRecordId) {
		ProjectGoalRecord managedGoal = findProjectGoalRecordById(projectGoalRecordId);
		Task t = new Task();
		t.setName(managedGoal.getPropertyRecord().getProperty().getName());
		t.setDescription(managedGoal.getPropertyRecord().getProperty().getDescription());
		t.setPriority(managedGoal.getProject().getPriority());
		t.setTaskStatus(TaskStatus.CREATED);

		Task managedTask = taskController.createTask(t);
		ProjectGoalPropertyRecord rec = new ProjectGoalPropertyRecord();
		rec.setProperty(managedGoal.getPropertyRecord().getProperty());
		rec.setValue(managedGoal.getPropertyRecord().getValue());
		em.persist(rec);
		ProjectGoalRecord activeGoal = new ProjectGoalRecord(managedGoal, rec, managedTask);
		em.persist(activeGoal);
		t.setProjectGoal(activeGoal);
		return activeGoal;
	}

	public void removeProjectProgress(int projectId) {
		Project managedProject = findProjectById(projectId);
		ProjectProgress progress = managedProject.getProgress();
		List<ProjectGoalRecord> targetGoals = progress.getTargetGoals();
		for (ProjectGoalRecord rec : targetGoals) {
			em.remove(rec);
		}
		managedProject.setProgress(null);
		em.remove(progress);
	}

	@Override
	public void raiseEvent(PObject source, String name, String oldValue, String newValue, long lifetime) {
		if (InitializationController.getAsBoolean(InitializationController.FIRE_TASK_EVENTS)) {
			Event evt = eventRegistry.getEventBuilder().newEvent().setSource(source).setOldValue(oldValue).setNewValue(newValue)
					.setPropertyName(name).setLifetime(lifetime).getEvent();
			eventRegistry.addEvent(evt);
		}
	}

	@Override
	public void consumeEvent(PObject destination, Event evt) {
		// TODO Auto-generated method stub

	}
}
