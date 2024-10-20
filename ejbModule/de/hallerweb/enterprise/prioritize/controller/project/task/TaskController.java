/*
 * Copyright 2015-2024 Peter Michael Haller and contributors
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

package de.hallerweb.enterprise.prioritize.controller.project.task;

import de.hallerweb.enterprise.prioritize.controller.InitializationController;
import de.hallerweb.enterprise.prioritize.controller.project.ProjectController;
import de.hallerweb.enterprise.prioritize.controller.security.UserRoleController;
import de.hallerweb.enterprise.prioritize.model.document.Document;
import de.hallerweb.enterprise.prioritize.model.project.Project;
import de.hallerweb.enterprise.prioritize.model.project.goal.ProjectGoalRecord;
import de.hallerweb.enterprise.prioritize.model.project.task.PActor;
import de.hallerweb.enterprise.prioritize.model.project.task.Task;
import de.hallerweb.enterprise.prioritize.model.project.task.TaskStatus;
import de.hallerweb.enterprise.prioritize.model.resource.Resource;
import de.hallerweb.enterprise.prioritize.model.security.User;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

import java.util.ArrayList;
import java.util.List;

/**
 * TaskController - Manages tasks.
 *
 * @author peter
 */
@Stateless
public class TaskController {

    @PersistenceContext
    EntityManager em;

    @EJB
    UserRoleController userRoleController;

    @EJB
    ProjectController projectController;

    @EJB
    InitializationController initController;

    private static final String LITERAL_ASSIGNEE = "assignee";

    public Task findTaskById(int id) {
        Query q = em.createNamedQuery("findTaskById");
        q.setParameter("taskId", id);
        return (Task) q.getSingleResult();
    }

    public List<Task> findTasksByAssignee(PActor assignee) {
        Query q = em.createNamedQuery("findTasksByAssignee");
        q.setParameter(LITERAL_ASSIGNEE, assignee);
        List<Task> tasks = q.getResultList();
        if (tasks.isEmpty()) {
            return new ArrayList<>();
        } else {
            return tasks;
        }
    }

    public List<Task> findTasksAssignedToUser(PActor assignee, Project p) {
        Query q = em.createNamedQuery("findTasksInProjectAssignedToUser");
        q.setParameter(LITERAL_ASSIGNEE, assignee);
        q.setParameter("project", p);
        List<Task> tasks = q.getResultList();
        if (tasks.isEmpty()) {
            return new ArrayList<>();
        } else {
            return tasks;
        }
    }


    public List<Task> findTasksNotAssignedToUser(PActor assignee, Project p) {
        Query q = em.createNamedQuery("findTasksInProjectNotAssignedToUser");
        q.setParameter(LITERAL_ASSIGNEE, assignee);
        q.setParameter("project", p);
        List<Task> tasks = q.getResultList();
        if (tasks.isEmpty()) {
            return new ArrayList<>();
        } else {
            return tasks;
        }
    }

    public Task createTask(Task task) {
        em.persist(task);
        return task;
    }

    public void removeTask(int taskId) {
        Task task = findTaskById(taskId);
        em.remove(task);
    }

    public void editTask(int taskId, Task detachedTask) {
        Task task = findTaskById(taskId);
        task.setName(detachedTask.getName());
        task.setDescription(detachedTask.getDescription());
        task.setPriority(detachedTask.getPriority());
        task.setTaskStatus(detachedTask.getTaskStatus());
        if (detachedTask.getAssignee() != null) {
            task.setAssignee(detachedTask.getAssignee());
        }
    }

    public void setTaskAssignee(Task task, PActor assignee) {
        findTaskById(task.getId()).setAssignee(assignee);
    }

    public void removeTaskAssignee(int taskId) {
        Task task = findTaskById(taskId);
        task.removeAssignee();
    }

    public void addTaskResource(int taskId, Resource resource) {
        Task task = findTaskById(taskId);
        List<Resource> resources = task.getResources();
        resources.add(resource);
        task.setResources(resources);
    }

    public void addTaskDocument(int taskId, Document document) {
        Task task = findTaskById(taskId);
        List<Document> documents = task.getDocuments();
        documents.add(document);
        task.setDocuments(documents);
    }

    public void updateTaskStatus(int taskId, TaskStatus newStatus) {
        Task task = findTaskById(taskId);
        task.setTaskStatus(newStatus);
    }

    public void resolveTask(Task task, User user) {
        Task managedTask = findTaskById(task.getId());
        ProjectGoalRecord rec = managedTask.getProjectGoalRecord();
        rec.setPercentage(100);

        removeTaskAssignee(managedTask.getId());
        userRoleController.removeAssignedTask(user, managedTask, user);
        updateTaskStatus(managedTask.getId(), TaskStatus.FINISHED);
        projectController.updateProjectProgress(task.getProjectGoalRecord().getProject().getId());
    }

    public void setTaskProgress(Task task, User user, int percentage) {
        if (percentage < 0 || percentage > 100) {
            return;
        }
        Task managedTask = findTaskById(task.getId());
        ProjectGoalRecord rec = managedTask.getProjectGoalRecord();
        rec.setPercentage(percentage);
        if (percentage == 100) {
            removeTaskAssignee(managedTask.getId());
            userRoleController.removeAssignedTask(user, managedTask, user);
            updateTaskStatus(managedTask.getId(), TaskStatus.FINISHED);
        } else if (percentage > 0) {
            updateTaskStatus(managedTask.getId(), TaskStatus.STARTED);
        } else {
            updateTaskStatus(managedTask.getId(), TaskStatus.STOPPED);
        }
        projectController.updateProjectProgress(task.getProjectGoalRecord().getProject().getId());
    }
}
