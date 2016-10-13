package de.hallerweb.enterprise.prioritize.controller.project;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.enterprise.context.ContextNotActiveException;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import de.hallerweb.enterprise.prioritize.controller.InitializationController;
import de.hallerweb.enterprise.prioritize.controller.LoggingController;
import de.hallerweb.enterprise.prioritize.controller.LoggingController.Action;
import de.hallerweb.enterprise.prioritize.controller.document.DocumentController;
import de.hallerweb.enterprise.prioritize.controller.event.EventRegistry;
import de.hallerweb.enterprise.prioritize.controller.project.task.TaskController;
import de.hallerweb.enterprise.prioritize.controller.resource.ResourceController;
import de.hallerweb.enterprise.prioritize.controller.security.SessionController;
import de.hallerweb.enterprise.prioritize.controller.security.UserRoleController;
import de.hallerweb.enterprise.prioritize.model.PObject;
import de.hallerweb.enterprise.prioritize.model.document.Document;
import de.hallerweb.enterprise.prioritize.model.document.DocumentInfo;
import de.hallerweb.enterprise.prioritize.model.event.Event;
import de.hallerweb.enterprise.prioritize.model.event.PEventConsumerProducer;
import de.hallerweb.enterprise.prioritize.model.project.Project;
import de.hallerweb.enterprise.prioritize.model.project.ProjectProgress;
import de.hallerweb.enterprise.prioritize.model.project.goal.ProjectGoal;
import de.hallerweb.enterprise.prioritize.model.project.goal.ProjectGoalCategory;
import de.hallerweb.enterprise.prioritize.model.project.goal.ProjectGoalProperty;
import de.hallerweb.enterprise.prioritize.model.project.goal.ProjectGoalPropertyDocument;
import de.hallerweb.enterprise.prioritize.model.project.goal.ProjectGoalPropertyNumeric;
import de.hallerweb.enterprise.prioritize.model.project.goal.ProjectGoalPropertyRecord;
import de.hallerweb.enterprise.prioritize.model.project.goal.ProjectGoalRecord;
import de.hallerweb.enterprise.prioritize.model.project.task.Blackboard;
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
	@EJB
	UserRoleController userRoleController;
	@EJB
	LoggingController logger;
	@Inject
	SessionController sessionController;

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

	public List<Project> findProjectsByUser(int userId) {
		Query q = em.createNamedQuery("findProjectsByMember");
		User user = userRoleController.findUserById(userId);
		q.setParameter("user", user);
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

	public Project createProject(Project project, Blackboard bb) {
		em.persist(bb);
		em.persist(project);
		
		Project managedProject = em.find(Project.class, project.getId());
		Blackboard managedBlackboard = em.find(Blackboard.class,bb.getId());
		
		managedProject.setBlackboard(managedBlackboard);
		managedBlackboard.setProject(managedProject);
				
		return managedProject;
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

	public List<ProjectGoalCategory> getAllCategories() {
		Query query = em.createNamedQuery("findAllProjectGoalCategories");

		List<ProjectGoalCategory> result = query.getResultList();
		if (!result.isEmpty()) {
			return result;
		} else
			return null;
	}

	public ProjectGoalCategory createProjectGoalCategory(String name, String description, ProjectGoalCategory parent) {
		boolean alreadyExists = false;
		if (parent != null) {
			List<ProjectGoalCategory> categories = getAllCategories();
			for (ProjectGoalCategory category : categories) {
				if (category.getName().equals(name)) {
					// TODO: Dubletten in unterschiedlichen ebenen erlauben!
					alreadyExists = true;
				}
			}
		}

		if (!alreadyExists) {
			ProjectGoalCategory category = new ProjectGoalCategory();
			category.setName(name);
			category.setDescription(description);
			category.setParentCategory(parent);

			em.persist(category);
			if (parent != null) {
				parent.addSubCategory(category);
			}

			em.flush();
			try {
				logger.log(sessionController.getUser().getUsername(), "SkillCategory", Action.CREATE, category.getId(),
						" SkillCategory \"" + category.getName() + "\" created.");
			} catch (ContextNotActiveException ex) {
				logger.log("SYSTEM", "SkillCategory", Action.CREATE, category.getId(),
						" SkillCategory \"" + category.getName() + "\" created.");
			}
			return category;
		} else {
			return null;
		}
	}

	public void deleteProjectGoalCategory(int categoryId, User sessionUser) {
		ProjectGoalCategory category = em.find(ProjectGoalCategory.class, categoryId);

		// First traverse all subcategories and delete all ProjectGoals within
		List<ProjectGoalCategory> subcategories = findSubCategoriesForCategory(category);
		if (subcategories != null) {
			for (ProjectGoalCategory cat : subcategories) {
				deleteProjectGoalsInCategory(cat, sessionUser);
				deleteProjectGoalCategory(cat.getId(), sessionUser);
			}
		}

		// then delete all Projectgoals within THIS category and finally remove this category.
		deleteProjectGoalsInCategory(category, sessionUser);
		em.remove(category);
		em.flush();

		try {
			logger.log(sessionController.getUser().getUsername(), "ProjectGoalCategory", Action.DELETE, category.getId(),
					" ProjectGoalCategory \"" + category.getName() + "\" deleted.");
		} catch (ContextNotActiveException ex) {
			logger.log("SYSTEM", "ProjectGoalCategory", Action.DELETE, category.getId(),
					" ProjectGoalCategory \"" + category.getName() + "\" deleted.");
		}
	}

	public void deleteProjectGoalsInCategory(ProjectGoalCategory category, User sessionUser) {
		List<ProjectGoal> goals = getProjectGoalsForCategory(category, sessionUser);
		if (goals != null) {
			for (ProjectGoal goal : goals) {
				deleteProjectGoal(goal.getId(), sessionUser);
			}
		}
	}

	public List<ProjectGoal> getProjectGoalsForCategory(ProjectGoalCategory cat, User sessionUser) {
		Query query = em.createNamedQuery("findProjectGoalsForCategory");
		query.setParameter("catId", cat.getId());

		@SuppressWarnings("unchecked")
		List<ProjectGoal> result = query.getResultList();
		if (!result.isEmpty()) {
			ProjectGoal g = (ProjectGoal) result.get(0);
			// TODO: permission in project object...?
			// if (authController.canRead(s, sessionUser)) {
			return result;
			// } else {
			// return null;
			// }
		} else
			return null;
	}

	public List<ProjectGoalProperty> getProjectGoalPropertiesForProjectGoal(ProjectGoal goal) {
		Query query = em.createNamedQuery("findProjectGoalPropertiesForProjectGoal");
		query.setParameter("goalId", goal.getId());

		@SuppressWarnings("unchecked")
		List<ProjectGoalProperty> result = query.getResultList();
		if (!result.isEmpty()) {
			return result;
		} else
			return null;
	}

	public List<ProjectGoalCategory> findSubCategoriesForCategory(ProjectGoalCategory cat) {
		Query query = em.createNamedQuery("findProjectGoalSubCategoriesForCategory");
		query.setParameter("parentCategoryId", cat.getId());

		List<ProjectGoalCategory> result = query.getResultList();
		if (!result.isEmpty()) {
			return result;
		} else
			return null;
	}

	/**
	 * Find all {@link ProjectGoalCategory} objects at the top level (parent=root).
	 * 
	 * @return
	 */
	public List<ProjectGoalCategory> getRootCategories() {
		Query query = em.createNamedQuery("findProjectGoalRootCategories");

		List<ProjectGoalCategory> result = query.getResultList();
		if (!result.isEmpty()) {
			return result;
		} else
			return null;
	}

	@SuppressWarnings("unchecked")
	public List<ProjectGoalRecord> getProjectGoalRecordsForProjectGoal(ProjectGoal goal) {
		Query query = em.createNamedQuery("findProjectGoalRecordsForProjectGoal");
		query.setParameter("goalId", goal.getId());
		return (List<ProjectGoalRecord>) query.getResultList();
	}

	public void deleteProjectGoal(int projectGoalId, User sessionUser) {
		ProjectGoal goal = em.find(ProjectGoal.class, projectGoalId);
		// if (authController.canDelete(skill, sessionUser)) {
		// TODO: permissions in project object...? add permission check here.
		// Remove all SkillRecords for that skill from Users and delete them.
		List<ProjectGoalRecord> projectGoalRecords = getProjectGoalRecordsForProjectGoal(goal);

		// If there have been project records found for that project goal, it cannot be deleted.
		if (!projectGoalRecords.isEmpty()) {
			try {
				logger.log(sessionController.getUser().getUsername(), "ProjectGoal", Action.DELETE, goal.getId(),
						" ProjectGoal \"" + goal.getName() + "\" could NOT be deleted because active records in use!!!");
			} catch (ContextNotActiveException ex) {
				logger.log("SYSTEM", "ProjectGoal", Action.DELETE, goal.getId(),
						" ProjectGoal \"" + goal.getName() + "\" could NOT be deleted because active records in use!!!");
			}
			return;
		}

		em.remove(goal);
		em.flush();
		try {
			logger.log(sessionController.getUser().getUsername(), "ProjectGoal", Action.DELETE, goal.getId(),
					" ProjectGoal \"" + goal.getName() + "\" deleted.");
		} catch (ContextNotActiveException ex) {
			logger.log("SYSTEM", "ProjectGoal", Action.DELETE, goal.getId(), " ProjectGoal \"" + goal.getName() + "\" deleted.");
		}
		// }
	}

	public void deleteProjectGoalProperty(int propertyId) {
		ProjectGoalProperty prop = em.find(ProjectGoalProperty.class, propertyId);
		em.remove(prop);
		em.flush();
	}

	public ProjectGoal createProjectGoal(String name, String description, ProjectGoalCategory category,
			List<ProjectGoalProperty> properties, User sessionUser) {
		ProjectGoal goal = new ProjectGoal();
		// TODO: Check permissions first
		// if (authController.canCreate(skill, sessionUser)) {

		goal.setName(name);
		goal.setDescription(description);
		goal.setCategory(category);
		em.persist(goal);
		em.flush();
		if (properties != null) {
			for (ProjectGoalProperty prop : properties) {
				if (prop instanceof ProjectGoalPropertyNumeric) {
					ProjectGoalPropertyNumeric property = (ProjectGoalPropertyNumeric) prop;
					property.setProjectGoal(goal);
					em.persist(property);
					//em.flush();
					goal.addProjectGoalProperty(property);
				}
			}
		}

		// em.flush();
		try {
			logger.log(sessionController.getUser().getUsername(), "ProjectGoal", Action.CREATE, goal.getId(),
					" ProjectGoal \"" + goal.getName() + "\" created.");
		} catch (ContextNotActiveException ex) {
			logger.log("SYSTEM", "ProjectGoal", Action.CREATE, goal.getId(), " ProjectGoal \"" + goal.getName() + "\" created.");
		}
		return goal;
		// } else {
		// return null;
		// }
	}

	public ProjectGoalRecord createProjectGoalRecord(ProjectGoalRecord rec) {
		em.persist(rec);
		return rec;
	}
	
	public ProjectGoalPropertyNumeric createProjectGoalPropertyNumeric(ProjectGoalPropertyNumeric property) {
		em.persist(property);
		return property;
	}
	
	public ProjectGoalPropertyRecord createProjectGoalPropertyRecord(ProjectGoalPropertyRecord record) {
		em.persist(record);
		return record;
	}
	
	public ProjectProgress createProjectProgress(int projectId, List<ProjectGoalRecord> targetGoals, int percentFinished) {
		ProjectProgress progress = new ProjectProgress();
		em.persist(progress);
		if (!targetGoals.isEmpty()) {
			for (ProjectGoalRecord projectGoalRecord : targetGoals) {
				//em.persist(projectGoal.getPropertyRecord().getProperty());
				//em.persist(projectGoal.getPropertyRecord());
				//em.persist(projectGoal.getProjectGoal());
				//em.persist(projectGoalRecord);
				if (projectGoalRecord.getTask() != null) {
					Task t = projectGoalRecord.getTask();
					//em.persist(t);
					t.setProjectGoal(projectGoalRecord);
				}
				
				//em.persist(projectGoal);
				progress.addTargetGoal(projectGoalRecord);
			}
		}
		progress.setProgress(percentFinished);
		
		Project managedProject = findProjectById(projectId);
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
		List<ProjectGoalRecord> currentProjectGoals = findActiveProjectGoalRecordsByProject(managedProject.getId());
		ProjectProgress managedProgress = findProjectProgressById(managedProject.getProgress().getId());
		int numGoals = managedProgress.getTargetGoals().size();
		int sum = 0;
		for (ProjectGoalRecord currentProjectGoalRecord : currentProjectGoals) {
			ProjectGoalRecord origProjectGoalRecord = findOriginalProjectGoalRecord(managedProgress, currentProjectGoalRecord);
			if (origProjectGoalRecord.getPropertyRecord().isNumericPropertyRecord()) {
				int percentage = calcNumericGoalPercentage(currentProjectGoalRecord, origProjectGoalRecord);
				currentProjectGoalRecord.setPercentage(percentage);
				sum += percentage;
			} else if (origProjectGoalRecord.getPropertyRecord().isDocumentPropertyRecord()) {
				DocumentInfo docInfo = currentProjectGoalRecord.getPropertyRecord().getDocumentInfo();
				String targetTag = ((ProjectGoalPropertyDocument) origProjectGoalRecord.getPropertyRecord().getProperty()).getTag();

				if (targetTag.equals(docInfo.getCurrentDocument().getTag())) {
					currentProjectGoalRecord.setPercentage(100);
					sum += 100;
					continue;
				} else {
					if (docInfo.getRecentDocuments() != null) {
						for (Document doc : docInfo.getRecentDocuments()) {
							if (targetTag.equals(doc.getTag())) {
								currentProjectGoalRecord.setPercentage(100);
								sum += 100;
								break;
							}
						}
					}
				}
			}
		}
		// System.out.println("SUMME: " + sum);
		// System.out.println("NUMGOALS: " + numGoals);
		managedProgress.setProgress((int) sum / numGoals);

	}

	private int calcNumericGoalPercentage(ProjectGoalRecord currentProjectGoalRecord, ProjectGoalRecord origProjectGoalRecord) {
		double baseValue = origProjectGoalRecord.getPropertyRecord().getValue();
		double minValue = ((ProjectGoalPropertyNumeric) origProjectGoalRecord.getPropertyRecord().getProperty()).getMin();
		double maxValue = ((ProjectGoalPropertyNumeric) origProjectGoalRecord.getPropertyRecord().getProperty()).getMax();
		double currentValue = currentProjectGoalRecord.getPropertyRecord().getValue();
		int percentage = calculatePercentageComplete(baseValue, minValue, maxValue, currentValue);
		return percentage;
	}

	private ProjectGoalRecord findOriginalProjectGoalRecord(ProjectProgress progress, ProjectGoalRecord currentRecord) {
		for (ProjectGoalRecord origProjectGoalRecord : progress.getTargetGoals()) {
			if (origProjectGoalRecord.getPropertyRecord().getProperty().equals(currentRecord.getPropertyRecord().getProperty())) {
				return origProjectGoalRecord;
			}
		}
		return null;
	}

	/**
	 * Creates a copy of the original projectGoalRecord to hold the current progress.
	 * Also creates a new Task for this ProjectGoalRecord and assigns the copy of the
	 * {@link ProjectGoalRecord} to this task.
	 * @param projectGoalRecordId - ID of the original {@link ProjectGoalRecord}.
	 * @return {@link ProjectGoalRecord} - An active copy of the original {@link ProjectGoalRecord}. 
	 */
	public ProjectGoalRecord activateProjectGoal(int projectGoalRecordId) {
		ProjectGoalRecord managedGoal = findProjectGoalRecordById(projectGoalRecordId);

		// Create a task for the ProjectGoalRecord
		Task t = new Task();
		t.setName(managedGoal.getPropertyRecord().getProperty().getName());
		t.setDescription(managedGoal.getPropertyRecord().getProperty().getDescription());
		t.setPriority(managedGoal.getProject().getPriority());
		t.setTaskStatus(TaskStatus.CREATED);
		Task managedTask = taskController.createTask(t);

		// Create copy of ProjectGoalRecord and initialize it with the original value
		ProjectGoalPropertyRecord rec = new ProjectGoalPropertyRecord();
		rec.setProperty(managedGoal.getPropertyRecord().getProperty());
		rec.setValue(managedGoal.getPropertyRecord().getValue());
		rec.setDocumentInfo(managedGoal.getPropertyRecord().getDocumentInfo());
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

	/**
	 * Calculates the percentage of completeness for a given {@link ProjectGoalRecord}.
	 * There is a minimum and a maximum value. If the initial value was greater than max, 100 % is reached
	 * when the current value goes below the max value. If the initial value was less then min, 100% is reached
	 * when the current value goes above the min value.
	 * Example: min=10000, max=30000; current=20000 percentage = 50%
	 * @param baseValue double - the initial value of the goal before task started. 
	 * @param minValue - the minimum value to reach 100%
	 * @param maxValue - the maximum value to reach 100%
	 * @param currentValue - The current value of the goal.
	 * @return int - The percentage value
	 */
	private int calculatePercentageComplete(double baseValue, double minValue, double maxValue, double currentValue) {
		int percentage;
		// The goal is to increase value until it is more than min
		if (baseValue <= minValue) {
			double targetValue = minValue;
			double base = targetValue - baseValue;
			percentage = (int) (100 - ((targetValue - currentValue) * 100) / base);
			// the goal is to decrease value until it is less than max
		} else if (baseValue >= maxValue) {
			double targetValue = maxValue;
			double base = baseValue - targetValue;
			percentage = (int) (100 + ((targetValue - currentValue) * 100) / base);
			// Goal has already been reached during definition of goal!
		} else {
			percentage = 100;
		}
		return percentage;
	}

}
