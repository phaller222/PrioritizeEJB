package de.hallerweb.enterprise.prioritize.view.project;

import java.io.Serializable;
import java.util.List;

import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.event.NodeCollapseEvent;
import org.primefaces.event.NodeExpandEvent;
import org.primefaces.model.DefaultTreeNode;

import de.hallerweb.enterprise.prioritize.controller.project.ProjectController;
import de.hallerweb.enterprise.prioritize.controller.resource.ResourceController;
import de.hallerweb.enterprise.prioritize.controller.security.AuthorizationController;
import de.hallerweb.enterprise.prioritize.controller.security.SessionController;
import de.hallerweb.enterprise.prioritize.controller.security.UserRoleController;
import de.hallerweb.enterprise.prioritize.model.project.goal.ProjectGoal;
import de.hallerweb.enterprise.prioritize.model.project.goal.ProjectGoalCategory;
import de.hallerweb.enterprise.prioritize.model.project.goal.ProjectGoalProperty;

/**
 * ProjectGoalBean - JSF Backing-Bean to manage project goals.
 * 
 * <p>
 * Copyright: (c) 2015
 * </p>
 * <p>
 * Peter Haller
 * </p>
 * 
 * @author peter
 */
@Named
@SessionScoped
public class ProjectGoalBean implements Serializable {

	@EJB
	ProjectController controller;
	@EJB
	AuthorizationController authController;
	@Inject
	SessionController sessionController;
	@EJB
	UserRoleController roleController;
	@EJB
	ResourceController resourceController;

	transient ProjectGoalCategory selectedProjectGoalCategory;
	transient Object selectedGoal;
	transient List<ProjectGoalCategory> projectGoalCategories;
	private DefaultTreeNode root;

	private static final String TYPE_ROOT = "rootnode";
	private static final String TYPE_CATEGORY = "category";
	private static final String TYPE_GOAL = "goal";
	private static final String TYPE_GOAL_PROPERTY = "goalproperty";

	public Object getSelectedProjectGoal() {
		return selectedGoal;
	}

	public void setSelectedProjectGoal(Object selectedProjectGoal) {
		if (selectedProjectGoalItem instanceof ProjectGoal) {
			this.selectedGoal = selectedProjectGoal;
		}

	}

	private transient Object selectedProjectGoalItem = null;
	private transient ProjectGoalCategory newProjectGoalCategory = new ProjectGoalCategory();
	private String currentTreeOrientation = "horizontal";

	public String getCurrentTreeOrientation() {
		return currentTreeOrientation;
	}

	public void setCurrentTreeOrientation(String currentTreeOrientation) {
		this.currentTreeOrientation = currentTreeOrientation;
	}

	public ProjectGoalCategory getNewProjectGoalCategory() {
		return newProjectGoalCategory;
	}

	public void setNewProjectGoalCategory(ProjectGoalCategory newProjectGoalCategory) {
		this.newProjectGoalCategory = newProjectGoalCategory;
	}

	public Object getSelectedProjectGoalItem() {
		return selectedProjectGoalItem;
	}

	public void setSelectedProjectGoalItem(Object selectedProjectGoalItem) {
		if (selectedProjectGoalItem != null) {
			this.selectedProjectGoalItem = selectedProjectGoalItem;
			if (((DefaultTreeNode) selectedProjectGoalItem).getData() instanceof ProjectGoal) {
				this.selectedGoal = ((DefaultTreeNode) selectedProjectGoalItem).getData();
			}
		}
	}

	/**
	 * Build the goal hierarchy and return a DefaultTreeNode.
	 * 
	 * @return
	 */
	public DefaultTreeNode getRoot() {
		// TODO: Decide if ProjectGoals are AuthorizedObjects or if permission is
		// based on Project objects.
		return root;
	}

	@Named
	public ProjectGoalCategory createProjectGoalCategory(String name, String description, ProjectGoalCategory parent) {
		return controller.createProjectGoalCategory(name, description, parent);
	}

	@Named
	public String deleteProjectGoalItem(Object pgItem) {
		if (pgItem instanceof ProjectGoalCategory) {
			ProjectGoalCategory cat = (ProjectGoalCategory) pgItem;
			controller.deleteProjectGoalCategory(cat.getId(), sessionController.getUser());
		} else if (pgItem instanceof ProjectGoal) {
			ProjectGoal g = (ProjectGoal) pgItem;
			controller.deleteProjectGoal(g.getId(), sessionController.getUser());
		}
		return "projectgoals";
	}

	public void deleteSelectedProjectGoalItem() {
		DefaultTreeNode selectedNode = (DefaultTreeNode) this.selectedProjectGoalItem;
		if (!(selectedNode.getData() instanceof DefaultTreeNode)) {
			deleteProjectGoalItem(selectedNode.getData());
		}
		selectedNode.getChildren().clear();
		selectedNode.getParent().getChildren().remove(selectedNode);
		selectedNode.setParent(null);
	}

	public void addSubcategoryToSelectedItem() {
		DefaultTreeNode selectedNode = (DefaultTreeNode) this.selectedProjectGoalItem;
		if (selectedNode.getData() instanceof ProjectGoalCategory) {
			ProjectGoalCategory category = (ProjectGoalCategory) selectedNode.getData();
			controller.createProjectGoalCategory(newProjectGoalCategory.getName(), newProjectGoalCategory.getDescription(), category);
		}
	}

	@Named
	public String editItem(int skillItem) {
		return "skills";
	}

	private void traverseProjectGoalCategories(DefaultTreeNode parentNode, ProjectGoalCategory currentCategory) {

		// Create new Category node (root) and traverse direct children (Skills)
		DefaultTreeNode newNode = new DefaultTreeNode(TYPE_CATEGORY, currentCategory, parentNode);

		// check if Category has subcategories and recursively traverse through
		// them
		List<ProjectGoalCategory> categories = controller.findSubCategoriesForCategory(currentCategory);
		if (categories != null) {
			for (ProjectGoalCategory category : categories) {
				traverseProjectGoalCategories(newNode, category);
			}
		}

		traverseProjectGoals(newNode, currentCategory);
	}

	private void traverseProjectGoals(DefaultTreeNode parentNode, ProjectGoalCategory category) {
		List<ProjectGoal> goals = controller.getProjectGoalsForCategory(category, AuthorizationController.getSystemUser());
		if (goals != null) {
			for (ProjectGoal goal : goals) {
				DefaultTreeNode goalNode = new DefaultTreeNode(TYPE_GOAL, goal, parentNode);
				List<ProjectGoalProperty> goalProperties = controller.getProjectGoalPropertiesForProjectGoal(goal);
				if (goalProperties != null) {
					for (ProjectGoalProperty prop : goalProperties) {
						new DefaultTreeNode(TYPE_GOAL_PROPERTY, prop, goalNode);
					}
				}
			}
		}
	}

	@Named
	public boolean isProjectGoalCategory(Object obj) {
		return obj instanceof ProjectGoalCategory;
	}

	@Named
	public boolean isProjectGoal(Object obj) {
		return obj instanceof ProjectGoal;
	}

	@Named
	public boolean isProjectGoalProperty(Object obj) {
		return obj instanceof ProjectGoal;
	}

	@Named
	public void buildProjectGoalTree() {
		List<ProjectGoalCategory> categories = controller.getRootCategories();
		root = new DefaultTreeNode(TYPE_ROOT, categories, null);
		root.setData("Projectgoals");

		if (categories != null) {
			for (ProjectGoalCategory cat : categories) {
				traverseProjectGoalCategories(root, cat);
			}
		}
	}

	public void nodeExpand(NodeExpandEvent event) {
		event.getTreeNode().setExpanded(true);
	}

	public void nodeCollapse(NodeCollapseEvent event) {
		event.getTreeNode().setExpanded(false);
	}

}
