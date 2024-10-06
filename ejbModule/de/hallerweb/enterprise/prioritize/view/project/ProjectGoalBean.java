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
package de.hallerweb.enterprise.prioritize.view.project;

import de.hallerweb.enterprise.prioritize.controller.project.ProjectController;
import de.hallerweb.enterprise.prioritize.controller.security.AuthorizationController;
import de.hallerweb.enterprise.prioritize.controller.security.SessionController;
import de.hallerweb.enterprise.prioritize.model.project.goal.ProjectGoal;
import de.hallerweb.enterprise.prioritize.model.project.goal.ProjectGoalCategory;
import de.hallerweb.enterprise.prioritize.model.project.goal.ProjectGoalProperty;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.primefaces.event.NodeCollapseEvent;
import org.primefaces.event.NodeExpandEvent;
import org.primefaces.model.DefaultTreeNode;

import java.io.Serializable;
import java.util.List;

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
    private transient ProjectController controller;
    @Inject
    private SessionController sessionController;
    @EJB
    private transient AuthorizationController authController;

    transient ProjectGoalCategory selectedProjectGoalCategory;
    transient Object selectedGoal;
    transient List<ProjectGoalCategory> projectGoalCategories;
    private DefaultTreeNode<Object> root;

    private static final String TYPE_ROOT = "rootnode";
    private static final String TYPE_CATEGORY = "category";
    private static final String TYPE_GOAL = "goal";
    private static final String TYPE_GOAL_PROPERTY = "goalproperty";
    private transient Object selectedProjectGoalItem = null;
    private transient ProjectGoalCategory newProjectGoalCategory = new ProjectGoalCategory();
    private String currentTreeOrientation = "horizontal";

    public Object getSelectedProjectGoal() {
        return selectedGoal;
    }

    public void setSelectedProjectGoal(Object selectedProjectGoal) {
        if (selectedProjectGoalItem instanceof ProjectGoal) {
            this.selectedGoal = selectedProjectGoal;
        }

    }


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
    public DefaultTreeNode<Object> getRoot() {
        // Decide if ProjectGoals are AuthorizedObjects or if permission is
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
        DefaultTreeNode<Object> selectedNode = (DefaultTreeNode<Object>) this.selectedProjectGoalItem;
        if (!(selectedNode.getData() instanceof DefaultTreeNode)) {
            deleteProjectGoalItem(selectedNode.getData());
        }
        selectedNode.getChildren().clear();
        selectedNode.getParent().getChildren().remove(selectedNode);
        selectedNode.setParent(null);
    }

    public void addSubcategoryToSelectedItem() {
        DefaultTreeNode<Object> selectedNode = (DefaultTreeNode<Object>) this.selectedProjectGoalItem;
        if (selectedNode.getData() instanceof ProjectGoalCategory) {
            ProjectGoalCategory category = (ProjectGoalCategory) selectedNode.getData();
            controller.createProjectGoalCategory(newProjectGoalCategory.getName(), newProjectGoalCategory.getDescription(), category);
        }
    }

    @Named
    public String editItem(int skillItem) {
        return "skills";
    }

    private void traverseProjectGoalCategories(DefaultTreeNode<Object> parentNode, ProjectGoalCategory currentCategory) {

        // Create new Category node (root) and traverse direct children (Skills)
        DefaultTreeNode<Object> newNode = new DefaultTreeNode<Object>(TYPE_CATEGORY, currentCategory, parentNode);

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

    private void traverseProjectGoals(DefaultTreeNode<Object> parentNode, ProjectGoalCategory category) {
        List<ProjectGoal> goals = controller.getProjectGoalsForCategory(category);
        if (goals != null) {
            for (ProjectGoal goal : goals) {
                DefaultTreeNode<Object> goalNode = new DefaultTreeNode<Object>(TYPE_GOAL, goal, parentNode);
                List<ProjectGoalProperty> goalProperties = controller.getProjectGoalPropertiesForProjectGoal(goal);
                if (goalProperties != null) {
                    for (ProjectGoalProperty prop : goalProperties) {
                        new DefaultTreeNode<Object>(TYPE_GOAL_PROPERTY, prop, goalNode);
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
        root = new DefaultTreeNode<Object>(TYPE_ROOT, categories, null);
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
