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

package de.hallerweb.enterprise.prioritize.controller.project;

import de.hallerweb.enterprise.prioritize.controller.InitializationController;
import de.hallerweb.enterprise.prioritize.controller.LoggingController;
import de.hallerweb.enterprise.prioritize.controller.LoggingController.Action;
import de.hallerweb.enterprise.prioritize.controller.document.DocumentController;
import de.hallerweb.enterprise.prioritize.controller.project.task.TaskController;
import de.hallerweb.enterprise.prioritize.controller.resource.ResourceController;
import de.hallerweb.enterprise.prioritize.controller.security.SessionController;
import de.hallerweb.enterprise.prioritize.model.document.Document;
import de.hallerweb.enterprise.prioritize.model.document.DocumentInfo;
import de.hallerweb.enterprise.prioritize.model.project.Project;
import de.hallerweb.enterprise.prioritize.model.project.ProjectProgress;
import de.hallerweb.enterprise.prioritize.model.project.goal.*;
import de.hallerweb.enterprise.prioritize.model.project.task.Blackboard;
import de.hallerweb.enterprise.prioritize.model.project.task.Task;
import de.hallerweb.enterprise.prioritize.model.project.task.TaskStatus;
import de.hallerweb.enterprise.prioritize.model.resource.Resource;
import de.hallerweb.enterprise.prioritize.model.security.User;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.enterprise.context.ContextNotActiveException;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * TaskController - Manages tasks.
 *
 * @author peter
 */
@Stateless
public class ProjectController {

    private static final String QUERY_FIND_PROJECT_BY_ID = "findProjectById";
    private static final String QUERY_FIND_PROJECTS_BY_MANAGER = "findProjectsByManager";
    private static final String QUERY_FIND_PROJECTS_BY_MEMBER = "findProjectsByMember";
    private static final String QUERY_FIND_PROJECTGOAL_RECORD_BY_ID = "findProjectGoalRecordById";
    private static final String QUERY_FIND_PROJECTGOAL_RECORDS_BY_PROJECT = "findProjectGoalRecordByProject";
    private static final String QUERY_FIND_ACTIVE_PROJECTGOAL_RECORDS_BY_PROJECT = "findActiveProjectGoalRecordsByProject";

    private static final String PARAM_PROJECT_ID = "projectId";
    private static final String PARAM_MANAGER_ID = "managerId";
    private static final String PARAM_USER = "user";
    private static final String PARAM_PROJECTGOAL_RECORD_ID = "projectGoalRecordId";

    private static final String LITERAL_LOGGING_SYSTEM_USER = "SYSTEM";
    private static final String LITERAL_CREATED = "\" created.";
    private static final String LITERAL_DELETED = "\" deleted.";
    private static final String LITERAL_PROJECTGOAL = " ProjectGoal ";

    @PersistenceContext
    EntityManager em;


    @EJB
    DocumentController documentController;
    @EJB
    ResourceController resourceController;
    @EJB
    TaskController taskController;
    @EJB
    LoggingController logger;
    @EJB
    InitializationController initController;
    @Inject
    SessionController sessionController;

    public Project findProjectById(int id) {
        Query q = em.createNamedQuery(QUERY_FIND_PROJECT_BY_ID);
        q.setParameter(PARAM_PROJECT_ID, id);
        return (Project) q.getSingleResult();
    }

    public List<Project> findProjectsByManager(int managerId) {
        User user = em.find(User.class, managerId);
        Query q = em.createNamedQuery(QUERY_FIND_PROJECTS_BY_MANAGER);
        q.setParameter(PARAM_MANAGER_ID, user);
        List<Project> projects = q.getResultList();
        if (projects.isEmpty()) {
            return new ArrayList<>();
        } else {
            return projects;
        }
    }

    public List<Project> findProjectsByUser(User sessionUser) {
        Query q = em.createNamedQuery(QUERY_FIND_PROJECTS_BY_MEMBER);
        q.setParameter(PARAM_USER, sessionUser);
        List<Project> projects = q.getResultList();
        if (projects.isEmpty()) {
            return new ArrayList<>();
        } else {
            return projects;
        }
    }

    public ProjectGoalRecord findProjectGoalRecordById(int id) {
        Query q = em.createNamedQuery(QUERY_FIND_PROJECTGOAL_RECORD_BY_ID);
        q.setParameter(PARAM_PROJECTGOAL_RECORD_ID, id);
        return (ProjectGoalRecord) q.getSingleResult();
    }

    public List<ProjectGoalRecord> findProjectGoalRecordsByProject(int projectId) {
        Query q = em.createNamedQuery(QUERY_FIND_PROJECTGOAL_RECORDS_BY_PROJECT);
        return getProjectGoalRecords(projectId, q);
    }

    private List<ProjectGoalRecord> getProjectGoalRecords(int projectId, Query query) {
        query.setParameter(PARAM_PROJECT_ID, projectId);
        List<ProjectGoalRecord> projectGoalRecords = query.getResultList();
        if (projectGoalRecords.isEmpty()) {
            return new ArrayList<>();
        } else {
            return projectGoalRecords;
        }
    }

    public List<ProjectGoalRecord> findActiveProjectGoalRecordsByProject(int projectId) {
        Query q = em.createNamedQuery(QUERY_FIND_ACTIVE_PROJECTGOAL_RECORDS_BY_PROJECT);
        return getProjectGoalRecords(projectId, q);
    }

    public ProjectProgress findProjectProgressById(int id) {
        Query q = em.createNamedQuery("findProjectProgressById");
        q.setParameter("projectProgressId", id);
        return (ProjectProgress) q.getSingleResult();
    }

    public Project createProject(Project project, Blackboard bb) {

        em.persist(bb);
        em.persist(project);

        Project managedProject = em.find(Project.class, project.getId());
        Blackboard managedBlackboard = em.find(Blackboard.class, bb.getId());

        managedProject.setBlackboard(managedBlackboard);
        managedBlackboard.setProject(managedProject);

        List<ProjectGoalRecord> records = new ArrayList<>();
        if (managedBlackboard.getTasks() != null) {
            for (Task t : managedBlackboard.getTasks()) {
                ProjectGoalPropertyNumeric property = new ProjectGoalPropertyNumeric();
                property.setName("Task completeness)");
                property.setDescription("Indicates the percentage value of completeness of a task.");
                property.setMin(0);
                property.setMax(100);
                ArrayList<ProjectGoalProperty> props = new ArrayList<>();
                props.add(property);

                // Find task in db to edit original vvalues in db!
                Task task = taskController.findTaskById(t.getId());

                ProjectGoal goal = createProjectGoal(task.getName(), task.getDescription(), null, props);

                goal.setName(task.getName());
                goal.setDescription(task.getDescription());

                ProjectGoalRecord projectGoalRecord = new ProjectGoalRecord();
                projectGoalRecord.setTask(task);
                projectGoalRecord.setProject(managedProject);
                projectGoalRecord.setProjectGoal(goal);
                projectGoalRecord = createProjectGoalRecord(projectGoalRecord);
                task.setProjectGoalRecord(projectGoalRecord);

                ProjectGoalPropertyRecord managedRecord = createProjectGoalPropertyRecord(new ProjectGoalPropertyRecord());
                managedRecord.setNumericPropertyRecord(true);
                managedRecord.setProperty(property);
                projectGoalRecord.setPropertyRecord(managedRecord);
                projectGoalRecord.setProjectGoal(goal);
                projectGoalRecord.setPercentage(0);
                records.add(projectGoalRecord);
            }
        }

        ProjectProgress progress = createProjectProgress(managedProject.getId(), records, 0);
        managedProject.setProgress(progress);

        return managedProject;
    }

    public void removeProject(int projectId) {
        Project project = findProjectById(projectId);
        em.remove(project);
    }

    public List<DocumentInfo> getProjectDocuments(Project project) {
        if (project.getDocuments().isEmpty()) {
            return new ArrayList<>();
        } else {
            return project.getDocuments();
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
        Resource managedResource = resourceController.getResource(resource.getId(), sessionUser);
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
        if (result.isEmpty()) {
            return new ArrayList<>();
        } else {
            return result;
        }
    }

    public ProjectGoalCategory createProjectGoalCategory(String name, String description, ProjectGoalCategory parent) {
        boolean alreadyExists = false;
        if (parent != null) {
            List<ProjectGoalCategory> categories = getAllCategories();
            for (ProjectGoalCategory category : categories) {
                if (category.getName().equals(name)) {
                    // TODO: Allow same project goal categories in different categories!
                    alreadyExists = true;
                    break;
                }
            }
        }

        if (alreadyExists) {
            return null;
        } else {
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
                    " SkillCategory \"" + category.getName() + LITERAL_CREATED);
            } catch (ContextNotActiveException ex) {
                logger.log(LITERAL_LOGGING_SYSTEM_USER, "SkillCategory", Action.CREATE, category.getId(),
                    " SkillCategory \"" + category.getName() + LITERAL_CREATED);
            }
            return category;
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
                " ProjectGoalCategory \"" + category.getName() + LITERAL_DELETED);
        } catch (ContextNotActiveException ex) {
            logger.log(LITERAL_LOGGING_SYSTEM_USER, "ProjectGoalCategory", Action.DELETE, category.getId(),
                " ProjectGoalCategory \"" + category.getName() + LITERAL_DELETED);
        }
    }

    public void deleteProjectGoalsInCategory(ProjectGoalCategory category, User sessionUser) {
        List<ProjectGoal> goals = getProjectGoalsForCategory(category);
        if (goals != null) {
            for (ProjectGoal goal : goals) {
                deleteProjectGoal(goal.getId(), sessionUser);
            }
        }
    }

    public List<ProjectGoal> getProjectGoalsForCategory(ProjectGoalCategory cat) {
        Query query = em.createNamedQuery("findProjectGoalsForCategory");
        query.setParameter("catId", cat.getId());

        @SuppressWarnings("unchecked")
        List<ProjectGoal> result = query.getResultList();
        if (result.isEmpty()) {
            return new ArrayList<>();
        } else {
            return result;
        }
    }

    public List<ProjectGoalProperty> getProjectGoalPropertiesForProjectGoal(ProjectGoal goal) {
        Query query = em.createNamedQuery("findProjectGoalPropertiesForProjectGoal");
        query.setParameter("goalId", goal.getId());

        @SuppressWarnings("unchecked")
        List<ProjectGoalProperty> result = query.getResultList();
        if (result.isEmpty()) {
            return new ArrayList<>();
        } else {
            return result;
        }
    }

    public List<ProjectGoalCategory> findSubCategoriesForCategory(ProjectGoalCategory cat) {
        Query query = em.createNamedQuery("findProjectGoalSubCategoriesForCategory");
        query.setParameter("parentCategoryId", cat.getId());

        List<ProjectGoalCategory> result = query.getResultList();
        if (result.isEmpty()) {
            return new ArrayList<>();
        } else {
            return result;
        }
    }

    /**
     * Find all {@link ProjectGoalCategory} objects at the top level (parent=root).
     *
     * @return List with ProjectGoalCategories
     */
    public List<ProjectGoalCategory> getRootCategories() {
        Query query = em.createNamedQuery("findProjectGoalRootCategories");

        List<ProjectGoalCategory> result = query.getResultList();
        if (result.isEmpty()) {
            return new ArrayList<>();
        } else {
            return result;
        }
    }

    @SuppressWarnings("unchecked")
    public List<ProjectGoalRecord> getProjectGoalRecordsForProjectGoal(ProjectGoal goal) {
        Query query = em.createNamedQuery("findProjectGoalRecordsForProjectGoal");
        query.setParameter("goalId", goal.getId());
        return query.getResultList();
    }

    public void deleteProjectGoal(int projectGoalId, User sessionUser) {
        ProjectGoal goal = em.find(ProjectGoal.class, projectGoalId);
        List<ProjectGoalRecord> projectGoalRecords = getProjectGoalRecordsForProjectGoal(goal);

        // If there have been project records found for that project goal, it cannot be deleted.
        if (!projectGoalRecords.isEmpty()) {
            try {
                logger.log(sessionController.getUser().getUsername(), LITERAL_PROJECTGOAL, Action.DELETE, goal.getId(),
                    " " + LITERAL_PROJECTGOAL + "\"" + goal.getName() + "\" could NOT be deleted because active records in use!!!");
            } catch (ContextNotActiveException ex) {
                logger.log(LITERAL_LOGGING_SYSTEM_USER, LITERAL_PROJECTGOAL, Action.DELETE, goal.getId(),
                    " " + LITERAL_PROJECTGOAL + " \"" + goal.getName() + "\" could NOT be deleted because active records in use!!!");
            }
            return;
        }

        em.remove(goal);
        em.flush();
        try {
            logger.log(sessionController.getUser().getUsername(), LITERAL_PROJECTGOAL, Action.DELETE, goal.getId(),
                " " + LITERAL_PROJECTGOAL + "\"" + goal.getName() + LITERAL_DELETED);
        } catch (ContextNotActiveException ex) {
            logger.log(LITERAL_LOGGING_SYSTEM_USER, LITERAL_PROJECTGOAL, Action.DELETE, goal.getId(),
                "" + LITERAL_PROJECTGOAL + " \"" + goal.getName() + LITERAL_DELETED);
        }
    }

    public void deleteProjectGoalProperty(int propertyId) {
        ProjectGoalProperty prop = em.find(ProjectGoalProperty.class, propertyId);
        em.remove(prop);
        em.flush();
    }

    public ProjectGoal createProjectGoal(String name, String description, ProjectGoalCategory category,
                                         List<ProjectGoalProperty> properties) {
        ProjectGoal goal = new ProjectGoal();
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
                    goal.addProjectGoalProperty(property);
                }
            }
        }
        try {
            logger.log(sessionController.getUser().getUsername(), "ProjectGoal", Action.CREATE, goal.getId(),
                " ProjectGoal \"" + goal.getName() + LITERAL_CREATED);
        } catch (ContextNotActiveException ex) {
            logger.log(LITERAL_LOGGING_SYSTEM_USER, "ProjectGoal", Action.CREATE, goal.getId(),
                " ProjectGoal \"" + goal.getName() + LITERAL_CREATED);
        }
        return goal;
    }

    public ProjectGoalRecord createProjectGoalRecord(ProjectGoalRecord rec) {
        em.persist(rec);
        return rec;
    }

    public ProjectGoalPropertyNumeric createProjectGoalPropertyNumeric(ProjectGoalPropertyNumeric property) {
        em.persist(property);
        return property;
    }

    public ProjectGoalPropertyRecord createProjectGoalPropertyRecord(ProjectGoalPropertyRecord propertyRecord) {
        em.persist(propertyRecord);
        return propertyRecord;
    }

    public ProjectProgress createProjectProgress(int projectId, List<ProjectGoalRecord> targetGoals, int percentFinished) {
        ProjectProgress progress = new ProjectProgress();
        em.persist(progress);
        if (!targetGoals.isEmpty()) {
            for (ProjectGoalRecord projectGoalRecord : targetGoals) {
                if (projectGoalRecord.getTask() != null) {
                    Task t = projectGoalRecord.getTask();
                    if (t.getProjectGoalRecord() == null) {
                        t.setProjectGoalRecord(projectGoalRecord);
                    }
                }
                progress.addTargetGoal(projectGoalRecord);
            }
        }
        progress.setProgress(percentFinished);

        Project managedProject = findProjectById(projectId);
        managedProject.setProgress(progress);
        return progress;
    }

    /**
     * Updates the overall progress of a project by calculating the percentage value of completeness of
     * each active {@link ProjectGoalRecord}
     *
     * @param projectId The ID of the project.
     */
    public void updateProjectProgress(int projectId) {
        Project managedProject = findProjectById(projectId);
        List<ProjectGoalRecord> currentProjectGoals = findActiveProjectGoalRecordsByProject(managedProject.getId());
        ProjectProgress managedProgress = findProjectProgressById(managedProject.getProgress().getId());
        int numGoals = managedProgress.getTargetGoals().size();
        int sum = 0;
        for (ProjectGoalRecord currentProjectGoalRecord : currentProjectGoals) {
            ProjectGoalRecord origProjectGoalRecord = findOriginalProjectGoalRecord(managedProgress, currentProjectGoalRecord);
            if (origProjectGoalRecord != null && origProjectGoalRecord.getPropertyRecord().isNumericPropertyRecord()) {
                int percentage = calcNumericProgress(currentProjectGoalRecord, origProjectGoalRecord);
                sum += percentage;
            } else if (origProjectGoalRecord != null && origProjectGoalRecord.getPropertyRecord().isDocumentPropertyRecord()) {
                DocumentInfo docInfo = currentProjectGoalRecord.getPropertyRecord().getDocumentInfo();
                String targetTag = ((ProjectGoalPropertyDocument) origProjectGoalRecord.getPropertyRecord().getProperty()).getTag();

                if (targetTag.equals(docInfo.getCurrentDocument().getTag())) {
                    currentProjectGoalRecord.setPercentage(100);
                    sum += 100;
                } else {
                    if (docInfo.getRecentDocuments() != null) {
                        sum = calcRecentDocumentsProgress(sum, currentProjectGoalRecord, docInfo, targetTag);
                    }
                }
            }
        }
        managedProgress.setProgress(sum / numGoals);
    }

    private int calcRecentDocumentsProgress(int sum, ProjectGoalRecord currentProjectGoalRecord, DocumentInfo docInfo, String targetTag) {
        int sumCopy = sum;
        for (Document doc : docInfo.getRecentDocuments()) {
            if (targetTag.equals(doc.getTag())) {
                currentProjectGoalRecord.setPercentage(100);
                sumCopy += 100;
                break;
            }
        }
        return sumCopy;
    }

    private int calcNumericProgress(ProjectGoalRecord currentProjectGoalRecord, ProjectGoalRecord origProjectGoalRecord) {
        int percentage = currentProjectGoalRecord.getPercentage();
        origProjectGoalRecord.getPropertyRecord().setValue(percentage);
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
     *
     * @param projectGoalRecordId - ID of the original {@link ProjectGoalRecord}.
     * @return {@link ProjectGoalRecord} - An active copy of the original {@link ProjectGoalRecord}.
     */
    public ProjectGoalRecord activateProjectGoal(int projectGoalRecordId) {
        ProjectGoalRecord managedGoal = findProjectGoalRecordById(projectGoalRecordId);

        // Create a task for the ProjectGoalRecord
        Task task = new Task();
        task.setName(managedGoal.getPropertyRecord().getProperty().getName());
        task.setDescription(managedGoal.getPropertyRecord().getProperty().getDescription());
        task.setPriority(managedGoal.getProject().getPriority());
        task.setTaskStatus(TaskStatus.CREATED);

        // Create copy of ProjectGoalRecord and initialize it with the original value
        ProjectGoalPropertyRecord rec = new ProjectGoalPropertyRecord();
        rec.setProperty(managedGoal.getPropertyRecord().getProperty());
        rec.setValue(managedGoal.getPropertyRecord().getValue());
        rec.setDocumentInfo(managedGoal.getPropertyRecord().getDocumentInfo());
        em.persist(rec);

        Task managedTask = taskController.createTask(task);
        ProjectGoalRecord activeGoal = new ProjectGoalRecord(managedGoal, rec, managedTask);
        em.persist(activeGoal);
        task.setProjectGoalRecord(activeGoal);
        return activeGoal;
    }

    public void removeProjectProgress(int projectId) {
        Project managedProject = findProjectById(projectId);
        ProjectProgress progress = managedProject.getProgress();
        Set<ProjectGoalRecord> targetGoals = progress.getTargetGoals();
        for (ProjectGoalRecord rec : targetGoals) {
            em.remove(rec);
        }
        managedProject.setProgress(null);
        em.remove(progress);
    }
}
