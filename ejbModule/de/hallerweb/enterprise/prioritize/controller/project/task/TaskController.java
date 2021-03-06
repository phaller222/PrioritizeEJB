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
package de.hallerweb.enterprise.prioritize.controller.project.task;

import de.hallerweb.enterprise.prioritize.controller.InitializationController;
import de.hallerweb.enterprise.prioritize.controller.event.EventRegistry;
import de.hallerweb.enterprise.prioritize.controller.project.ProjectController;
import de.hallerweb.enterprise.prioritize.controller.security.UserRoleController;
import de.hallerweb.enterprise.prioritize.model.PObject;
import de.hallerweb.enterprise.prioritize.model.document.Document;
import de.hallerweb.enterprise.prioritize.model.event.Event;
import de.hallerweb.enterprise.prioritize.model.event.PEventConsumerProducer;
import de.hallerweb.enterprise.prioritize.model.project.Project;
import de.hallerweb.enterprise.prioritize.model.project.goal.ProjectGoalRecord;
import de.hallerweb.enterprise.prioritize.model.project.task.PActor;
import de.hallerweb.enterprise.prioritize.model.project.task.Task;
import de.hallerweb.enterprise.prioritize.model.project.task.TaskStatus;
import de.hallerweb.enterprise.prioritize.model.resource.Resource;
import de.hallerweb.enterprise.prioritize.model.security.User;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

/**
 * TaskController - Manages tasks.
 * @author peter
 *
 */
@Stateless
public class TaskController extends PEventConsumerProducer {

	@PersistenceContext
	EntityManager em;

	@Inject
	EventRegistry eventRegistry;

	@EJB
	UserRoleController userRoleController;

	@EJB
	ProjectController projectController;

	@EJB
	InitializationController initController;

	public Task findTaskById(int id) {
		Query q = em.createNamedQuery("findTaskById");
		q.setParameter("taskId", id);
		return (Task) q.getSingleResult();
	}

	public List<Task> findTasksByAssignee(PActor assignee) {
		Query q = em.createNamedQuery("findTasksByAssignee");
		q.setParameter("assignee", assignee);
		List<Task> tasks = (List<Task>) q.getResultList();
		if (tasks.isEmpty()) {
			return new ArrayList<>();
		} else {
			return tasks;
		}
	}
	
	public List<Task> findTasksAssignedToUser(PActor assignee, Project p) {
		Query q = em.createNamedQuery("findTasksInProjectAssignedToUser");
		q.setParameter("assignee", assignee);
		q.setParameter("project", p);
		List<Task> tasks = (List<Task>) q.getResultList();
		if (tasks.isEmpty()) {
			return new ArrayList<>();
		} else {
			return tasks;
		}
	}
	

	public List<Task> findTasksNotAssignedToUser(PActor assignee,Project p) {
		Query q = em.createNamedQuery("findTasksInProjectNotAssignedToUser");
		q.setParameter("assignee", assignee);
		q.setParameter("project", p);
		List<Task> tasks = (List<Task>) q.getResultList();
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

	public void removeTaskAssignee(int taskId, PActor assignee, User sessionUser) {
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

		removeTaskAssignee(managedTask.getId(), user, user);
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
			removeTaskAssignee(managedTask.getId(), user, user);
			userRoleController.removeAssignedTask(user, managedTask, user);
			updateTaskStatus(managedTask.getId(), TaskStatus.FINISHED);
		} else if (percentage > 0) {
			updateTaskStatus(managedTask.getId(), TaskStatus.STARTED);
		} else {
			updateTaskStatus(managedTask.getId(), TaskStatus.STOPPED);
		}
		projectController.updateProjectProgress(task.getProjectGoalRecord().getProject().getId());
	}

	@Override
	public void raiseEvent(PObject source, String name, String oldValue, String newValue, long lifetime) {
		if (initController.getAsBoolean(InitializationController.FIRE_TASK_EVENTS)) {
			Event evt = eventRegistry.getEventBuilder().newEvent().setSource(source).setOldValue(oldValue).setNewValue(newValue)
					.setPropertyName(name).setLifetime(lifetime).getEvent();
			eventRegistry.addEvent(evt);
		}
	}

	@Override
	public void consumeEvent(PObject destination, Event evt) {
		// Auto-generated method stub

	}
}
