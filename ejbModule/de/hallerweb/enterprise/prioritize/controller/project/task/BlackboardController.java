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
import de.hallerweb.enterprise.prioritize.model.project.task.Blackboard;
import de.hallerweb.enterprise.prioritize.model.project.task.PActor;
import de.hallerweb.enterprise.prioritize.model.project.task.Task;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

import java.util.List;

/**
 * BlackboardController - Manages blackboards and tasks on blackboards.
 *
 * @author peter
 */
@Stateless
public class BlackboardController {

    @PersistenceContext
    EntityManager em;

    @EJB
    TaskController taskController;
    @EJB
    InitializationController initController;

    public Blackboard findBlackboardById(int id) {
        Query q = em.createNamedQuery("findBlackboardById");
        q.setParameter("blackboardId", id);
        return (Blackboard) q.getSingleResult();
    }

    public Blackboard createBlackboard(Blackboard board) {
        em.persist(board);
        return board;
    }

    public void removeBlackboard(int blackboardId) {
        Blackboard bb = findBlackboardById(blackboardId);
        em.remove(bb);
    }

    public void editBlackboard(int blackboardId, String newTitle, String newDescription) {
        Blackboard bb = findBlackboardById(blackboardId);
        bb.setTitle(newTitle);
        bb.setDescription(newDescription);
    }

    public void putTaskToBlackboard(int taskId, int blackboardId) {
        Blackboard bb = findBlackboardById(blackboardId);
        if (!bb.isFrozen()) {
            Task task = taskController.findTaskById(taskId);
            bb.addTask(task);
        }
    }

    public void takeTaskFromBlackboard(int blackboardId, int taskId, PActor assignee) {
        Blackboard bb = findBlackboardById(blackboardId);
        if (!bb.isFrozen()) {
            Task task = taskController.findTaskById(taskId);
            task.setAssignee(assignee);
            bb.removeTask(task);
        }
    }

    public List<Task> getBlackboardTasks(Blackboard bb) {
        Query q = em.createNamedQuery("findBlackboardTasks");
        q.setParameter("blackboardId", bb.getId());
        return q.getResultList();
    }

    public void freezeBlackboard(int blackboardId) {
        Blackboard bb = findBlackboardById(blackboardId);
        bb.setFrozen(true);
    }

    public void defrostBlackboard(int blackboardId) {
        Blackboard bb = findBlackboardById(blackboardId);
        bb.setFrozen(false);
    }

}
