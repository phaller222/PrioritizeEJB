package de.hallerweb.enterprise.prioritize.view.project;

import java.io.Serializable;
import java.util.List;

import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.Query;

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
import de.hallerweb.enterprise.prioritize.model.skill.Skill;
import de.hallerweb.enterprise.prioritize.model.skill.SkillProperty;


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

	ProjectGoalCategory selectedProjectGoalCategory;
	Object selectedGoal;
	List<ProjectGoalCategory> projectGoalCategories;
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
			// this.selectedProjectGoal = (selectedProjectGoal) selectedProjectGoalItem;
			this.selectedGoal = (ProjectGoal) selectedProjectGoal;
		}

	}

	private Object selectedProjectGoalItem = null;
	private ProjectGoalCategory newProjectGoalCategory = new ProjectGoalCategory();
	private String currentTreeOrientation = "horizontal";

	private static boolean dummyDataCreated = false; // TODO: Remove for production!

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

	public String getTYPE_ROOT() {
		return TYPE_ROOT;
	}

	public Object getSelectedProjectGoalItem() {
		return selectedProjectGoalItem;
	}

	public void setSelectedProjectGoalItem(Object selectedProjectGoalItem) {
		if (selectedProjectGoalItem != null) {
			this.selectedProjectGoalItem = selectedProjectGoalItem;
			if (((DefaultTreeNode) selectedProjectGoalItem).getData() instanceof ProjectGoal) {
				this.selectedGoal = (ProjectGoal) ((DefaultTreeNode) selectedProjectGoalItem).getData();
			}
		}
	}

	public String getTYPE_CATEGORY() {
		return TYPE_CATEGORY;
	}

	public String getTYPE_PROJECTGOAL() {
		return TYPE_GOAL;
	}

	public String getTYPE_PROJECTGOAL_PROPERTY() {
		return TYPE_GOAL_PROPERTY;
	}

	/**
	 * Build the goal hierarchy and return a DefaultTreeNode.
	 * 
	 * @return
	 */
	public DefaultTreeNode getRoot() {
		//TODO: Decide if ProjectGoals are AuthorizedObjects or if permissio  is
		// based on Project objects.
		return root;
//		Skill sk = new Skill();
//		if (authController.canRead(sk, sessionController.getUser())) {
//			return root;
//		} else {
//			return null;
//		}
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

		selectedNode = null;

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

//	@Named
//	public String deleteJavaCategory() {
//		SkillCategory cat = controller.getCategoryByName("Java");
//		controller.deleteSkillCategory(cat.getId(), sessionController.getUser());
//		return "/skills";
//	}

//	/**
//	 * Creates dummy data for debugging Purposes.
//	 * 
//	 * 
//	 * @return
//	 */
//	@PostConstruct
//	public void createDummyData() {
//		if (dummyDataCreated) {
//			return;
//		}
//		SkillCategory cat2 = controller.createSkillCategory("Programmierung", "Programmierung/Programmiersprachen", null);
//		SkillCategory cat22 = controller.createSkillCategory("Java", "Kenntnisse im Bereich Java", cat2);
//		SkillCategory cat23 = controller.createSkillCategory("Apple", "Apple Kenntnisse", cat2);
//
//		SkillCategory catCoaching = controller.createSkillCategory("Coaching", "Schulungen/Workshops/Coaching", null);
//		Skill workshops = controller.createSkill("Workshops", "Durchführung von Workshops", "coaching,workshops", catCoaching, null,
//				AuthorizationController.getSystemUser());
//
//		SkillPropertyNumeric prop1 = new SkillPropertyNumeric();
//		prop1.setName("JPA");
//		prop1.setDescription("JPA Kenntnisse (0 bis 6).");
//		prop1.setNumericProperty(true);
//		prop1.setMinValue(0);
//		prop1.setMaxValue(6);
//
//		SkillPropertyNumeric prop2 = new SkillPropertyNumeric();
//		prop2.setName("JDBC");
//		prop2.setDescription("JDBC Kenntnisse (0 bis 6).");
//		prop2.setNumericProperty(true);
//		prop2.setMinValue(0);
//		prop2.setMaxValue(6);
//
//		SkillPropertyText prop22 = new SkillPropertyText();
//		prop22.setName("JSF");
//		prop22.setDescription("JSF Kenntnisse (Tagnamen).");
//		prop22.setText("");
//		prop22.setNumericProperty(false);
//
//		HashSet props = new HashSet<SkillProperty>();
//		props.add(prop1);
//		props.add(prop2);
//		props.add(prop22);
//
//		SkillPropertyNumeric prop3 = new SkillPropertyNumeric();
//		prop3.setName("Objective-C");
//		prop3.setDescription("Objectiv-C Kenntnisse (0 bis 6).");
//		prop3.setNumericProperty(true);
//		prop3.setMinValue(0);
//		prop3.setMaxValue(6);
//
//		SkillPropertyNumeric prop4 = new SkillPropertyNumeric();
//		prop4.setName("Swift");
//		prop4.setDescription("Swift Kenntnisse (0 bis 6).");
//		prop4.setNumericProperty(true);
//		prop4.setMinValue(0);
//		prop4.setMaxValue(6);
//
//		HashSet props2 = new HashSet<SkillProperty>();
//		props2.add(prop3);
//		props2.add(prop4);
//
//		Skill sk1 = controller.createSkill("J2EE", "J2EE Kenntnisse", "JPA, JavaMail, EJB", cat22, props,
//				AuthorizationController.getSystemUser());
//		Skill sk2 = controller.createSkill("iOS", "iOS Kenntnisse", "Swift, objective-c...", cat23, props2,
//				AuthorizationController.getSystemUser());
//
//		// // Define Skill Record with properties
//		/*
//		 * SkillRecordProperty recProp = new SkillRecordProperty(); recProp.setProperty(prop1); recProp.setPropertyValueNumeric(4);
//		 * 
//		 * SkillRecordProperty recProp2 = new SkillRecordProperty(); recProp2.setProperty(prop22); recProp2.setPropertyValueString(
//		 * "form, panelgrid, panel");
//		 * 
//		 * HashSet<SkillRecordProperty> propsSkill = new HashSet<SkillRecordProperty>(); propsSkill.add(recProp); propsSkill.add(recProp2);
//		 * int enthusiasm = 5; SkillRecord rec = controller.createSkillRecord(sk1, propsSkill, enthusiasm); SkillRecord rec2 =
//		 * controller.createSkillRecord(workshops, null, enthusiasm);
//		 * 
//		 * roleController.addSkillToUser(roleController.findUserByUsername("admin").getId(), rec.getId());
//		 */
//		dummyDataCreated = true;
//
//		// List<SkillCategory> root = controller.getRootCategories();
//		// for (SkillCategory cat : root) {
//		// System.out.println(cat.getName());
//		//
//		// List<Skill> skills = controller.getSkillsForCategory(cat);
//		// if (skills != null) {
//		// for (Skill s : skills) {
//		// System.out.println("--->" + s.getName());
//		// }
//		// }
//		// }
//		// buildSkillTree();
//		// return "/skills";
//	}

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
						System.out.println("PROPERTY: " + prop.getName());
						new DefaultTreeNode(TYPE_GOAL_PROPERTY, prop, goalNode);
					}
				}
			}
		}
	}
	
//	public List<ProjectGoalProperty> getProjectPropertiesForProjectGoal(ProjectGoal goal) {
//		Query query = em.createNamedQuery("findSkillPropertiesForSkill");
//		query.setParameter("skillId", goal.getId());
//
//		@SuppressWarnings("unchecked")
//		List<SkillProperty> result = query.getResultList();
//		if (!result.isEmpty()) {
//			return result;
//		} else
//			return null;
//	}

	@Named
	public boolean isProjectGoalCategory(Object o) {
		return o instanceof ProjectGoalCategory;
	}

	@Named
	public boolean isProjectGoal(Object o) {
		return o instanceof ProjectGoal;
	}

	@Named
	public boolean isProjectGoalProperty(Object o) {
		return o instanceof ProjectGoal;
	}

	public String addProjectGoalCategory(Object category) {
		return "projectgoals";
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

//	public void assignSkillToUser(String userid) {
//
//		DefaultTreeNode node = (DefaultTreeNode) getSelectedSkillItem();
//		Skill selectedSkill = (Skill) node.getData();
//		HashSet<SkillRecordProperty> propertyRecords = new HashSet<SkillRecordProperty>();
//		Set<SkillProperty> skillProperties = selectedSkill.getSkillProperties();
//		for (SkillProperty prop : skillProperties) {
//			if (prop.getNumericProperty()) {
//				SkillPropertyNumeric numProperty = (SkillPropertyNumeric) prop;
//				SkillRecordProperty recNew = new SkillRecordProperty();
//				recNew.setProperty(numProperty);
//				recNew.setPropertyValueNumeric(numProperty.getTempValue());
//				propertyRecords.add(recNew);
//			} else {
//				SkillPropertyText txtProperty = (SkillPropertyText) prop;
//				SkillRecordProperty recNew = new SkillRecordProperty();
//				recNew.setProperty(txtProperty);
//				recNew.setPropertyValueString(txtProperty.getText());
//				propertyRecords.add(recNew);
//			}
//		}
//
//		User userToAssignSkill = roleController.getUserById(Integer.parseInt(userid), sessionController.getUser());
//		SkillRecord rec = controller.createSkillRecord(selectedSkill, propertyRecords, this.selectedEnthusiasmLevel);
//		roleController.addSkillToUser(userToAssignSkill.getId(), rec.getId(), sessionController.getUser());
//	}

//	public void assignSkillToResource(String resourceId) {
//
//		DefaultTreeNode node = (DefaultTreeNode) getSelectedSkillItem();
//		Skill selectedSkill = (Skill) node.getData();
//		Set<SkillRecordProperty> propertyRecords = new HashSet<SkillRecordProperty>();
//		Set<SkillProperty> skillProperties = selectedSkill.getSkillProperties();
//		for (SkillProperty prop : skillProperties) {
//			if (prop.getNumericProperty()) {
//				SkillPropertyNumeric numProperty = (SkillPropertyNumeric) prop;
//				SkillRecordProperty recNew = new SkillRecordProperty();
//				recNew.setProperty(numProperty);
//				recNew.setPropertyValueNumeric(numProperty.getTempValue());
//				propertyRecords.add(recNew);
//			} else {
//				SkillPropertyText txtProperty = (SkillPropertyText) prop;
//				SkillRecordProperty recNew = new SkillRecordProperty();
//				recNew.setProperty(txtProperty);
//				recNew.setPropertyValueString(txtProperty.getText());
//				propertyRecords.add(recNew);
//			}
//		}
//
//		Resource resourceToAssignSkill = resourceController.getResource(Integer.parseInt(resourceId), sessionController.getUser());
//		SkillRecord rec = controller.createSkillRecord(selectedSkill, propertyRecords, this.selectedEnthusiasmLevel);
//		resourceController.addSkillToResource(resourceToAssignSkill.getId(), rec.getId(), sessionController.getUser());
//	}

	public void nodeExpand(NodeExpandEvent event) {
		event.getTreeNode().setExpanded(true);
	}

	public void nodeCollapse(NodeCollapseEvent event) {
		event.getTreeNode().setExpanded(false);
	}

}
