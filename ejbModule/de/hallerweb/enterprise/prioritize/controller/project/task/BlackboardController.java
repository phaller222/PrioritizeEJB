package de.hallerweb.enterprise.prioritize.controller.project.task;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import de.hallerweb.enterprise.prioritize.controller.InitializationController;
import de.hallerweb.enterprise.prioritize.controller.event.EventRegistry;
import de.hallerweb.enterprise.prioritize.model.PObject;
import de.hallerweb.enterprise.prioritize.model.event.Event;
import de.hallerweb.enterprise.prioritize.model.event.PEventConsumerProducer;
import de.hallerweb.enterprise.prioritize.model.project.task.Blackboard;
import de.hallerweb.enterprise.prioritize.model.project.task.PActor;
import de.hallerweb.enterprise.prioritize.model.project.task.Task;

/**
 * BlackboardController - Manages blackboards and tasks on blackboards.
 * @author peter
 *
 */
@Stateless
public class BlackboardController extends PEventConsumerProducer {

	@PersistenceContext(unitName = "MySqlDS")
	EntityManager em;

	@EJB
	TaskController taskController;

	@Inject
	EventRegistry eventRegistry;

	// public ActionBoard findActionBoardByName(String name) {
	// Query q = em.createNamedQuery("findActionBoardByName");
	// q.setParameter("actionBoardName", name);
	// ActionBoard board = (ActionBoard) q.getSingleResult();
	// return board;
	// }

	public Blackboard findBlackboardById(int id) {
		Query q = em.createNamedQuery("findBlackboardById");
		q.setParameter("blackboardId", id);
		Blackboard board = (Blackboard) q.getSingleResult();
		return board;
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
			raiseEvent(task, "blackboard", "", String.valueOf(bb.getId()), -1);
		}
	}

	public void TakeTaskFromBlackboard(int blackboardId, int taskId, PActor assignee) {
		Blackboard bb = findBlackboardById(blackboardId);
		if (!bb.isFrozen()) {
			Task task = taskController.findTaskById(taskId);
			task.addAssignee(assignee);
			bb.removeTask(task);
		}
	}

	public void freezeBlackboard(int blackboardId) {
		Blackboard bb = findBlackboardById(blackboardId);
		bb.setFrozen(true);
	}
	
	public void defrostBlackboard(int blackboardId) {
		Blackboard bb = findBlackboardById(blackboardId);
		bb.setFrozen(false);
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
