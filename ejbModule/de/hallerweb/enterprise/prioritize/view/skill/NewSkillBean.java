package de.hallerweb.enterprise.prioritize.view.skill;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.ManagedBean;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import de.hallerweb.enterprise.prioritize.controller.security.SessionController;
import de.hallerweb.enterprise.prioritize.controller.skill.SkillController;
import de.hallerweb.enterprise.prioritize.model.skill.Skill;
import de.hallerweb.enterprise.prioritize.model.skill.SkillCategory;
import de.hallerweb.enterprise.prioritize.model.skill.SkillProperty;
import de.hallerweb.enterprise.prioritize.model.skill.SkillPropertyNumeric;
import de.hallerweb.enterprise.prioritize.model.skill.SkillPropertyText;
import de.hallerweb.enterprise.prioritize.service.mqtt.MQTTService;
import de.hallerweb.enterprise.prioritize.view.ViewUtilities;

/**
 * NewSkillBean - JSF Backing-Bean to manage new skills
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
@ManagedBean
public class NewSkillBean implements Serializable {

	@EJB
	SkillController skillController;
	@Inject
	SessionController sessionController;
	@Inject
	MQTTService service;

	String selectedSkillCategoryId;
	transient Skill newSkill;
	String newSkillName;
	String newSkillDescription;
	String propertyNumericName;
	String propertyNumericDescription;
	String propertyTextName;
	String propertyTextDescription;
	transient Set<SkillProperty> skillProperties = new HashSet<>();
	String propertyNumericMinValue;
	String propertyNumericMaxValue;
	transient Set<SkillProperty> propertiesNumeric;
	transient Set<SkillProperty> propertiesText;
	transient List<SkillCategory> skillCategories;

	public String getPropertyTextDescription() {
		return propertyTextDescription;
	}

	public Set<SkillProperty> getSkillProperties() {
		return skillProperties;
	}

	public void setPropertyTextDescription(String propertyTextDescription) {
		this.propertyTextDescription = propertyTextDescription;
	}

	public Set<SkillProperty> getPropertiesText() {
		return propertiesText;
	}

	public void setPropertiesText(Set<SkillProperty> propertiesText) {
		this.propertiesText = propertiesText;
	}

	public String getPropertyNumericDescription() {
		return propertyNumericDescription;
	}

	public void setPropertyNumericDescription(String propertyDescription) {
		this.propertyNumericDescription = propertyDescription;
	}

	public Set<SkillProperty> getPropertiesNumeric() {
		return propertiesNumeric;
	}

	public void setPropertiesNumeric(Set<SkillProperty> propertiesNumeric) {
		this.propertiesNumeric = propertiesNumeric;
	}

	public String getPropertyNumericName() {
		return propertyNumericName;
	}

	public void setPropertyNumericName(String propertyTextText) {
		this.propertyNumericName = propertyTextText;
	}

	public String getPropertyNumericMinValue() {
		return propertyNumericMinValue;
	}

	public void setPropertyNumericMinValue(String propertyNumericMinValue) {
		this.propertyNumericMinValue = propertyNumericMinValue;
	}

	public String getPropertyNumericMaxValue() {
		return propertyNumericMaxValue;
	}

	public void setPropertyNumericMaxValue(String propertyNumericMaxValue) {
		this.propertyNumericMaxValue = propertyNumericMaxValue;
	}

	public String getNewSkillName() {
		return newSkillName;
	}

	public String getPropertyTextName() {
		return propertyTextName;
	}

	public void setPropertyTextName(String propertyTextName) {
		this.propertyTextName = propertyTextName;
	}

	public void setNewSkillName(String newSkillName) {
		this.newSkillName = newSkillName;
	}

	public String getNewSkillDescription() {
		return newSkillDescription;
	}

	public void setNewSkillDescription(String newSkillDescription) {
		this.newSkillDescription = newSkillDescription;
	}



	public String getSelectedSkillCategoryId() {
		return selectedSkillCategoryId;
	}

	public void setSelectedSkillCategoryId(String selectedSkillCategoryId) {
		this.selectedSkillCategoryId = selectedSkillCategoryId;
	}

	public List<SkillCategory> getSkillCategories() {
		this.skillCategories = skillController.getAllCategories();
		return skillCategories;
	}

	public String createSkill() {
		if (newSkillName != null && newSkillDescription != null) {
			SkillCategory selectedCategory = skillController.getCategoryById(this.selectedSkillCategoryId);
			List<Skill> definedSkills = skillController.getSkillsForCategory(selectedCategory, sessionController.getUser());
			boolean exists = false;
			if (definedSkills != null) {
				exists = skillExists(definedSkills, exists);
			}
			if (!exists) {
				this.newSkill = skillController.createSkill(newSkillName, newSkillDescription, "", selectedCategory, skillProperties,
						sessionController.getUser());
				if (newSkill == null) {
					ViewUtilities.addErrorMessage(null, "You are not allowed to add new Skills!");
					return "skills";
				}
			}
		}

		// Cleanup old values so that new properties can be set.
		this.setPropertiesNumeric(null);
		this.setPropertiesText(null);
		this.skillProperties = null;
		this.setNewSkillDescription(null);
		this.setNewSkillName(null);
		this.setPropertyNumericName(null);
		this.setPropertyNumericDescription(null);
		this.setPropertyNumericMaxValue(null);
		this.setPropertyTextName(null);
		this.setPropertyTextDescription(null);
		this.setPropertyNumericMaxValue(null);
		this.setPropertyNumericMinValue(null);

		return "skills";
	}

	private boolean skillExists(List<Skill> definedSkills, boolean exists) {
		for (Skill s : definedSkills) {
			if (s.getName().equals(newSkillName)) {
				exists = true;
			}
		}
		return exists;
	}

	public void addNumericProperty() {
		if (skillProperties == null) {
			skillProperties = new HashSet<>();
		}
		if (propertiesNumeric == null) {
			this.propertiesNumeric = new HashSet<>();
		}
		SkillPropertyNumeric prop = new SkillPropertyNumeric();
		prop.setName(propertyNumericName);
		prop.setDescription(propertyNumericDescription);
		prop.setNumericProperty(true);
		prop.setMinValue(Integer.parseInt(propertyNumericMinValue));
		prop.setMaxValue(Integer.parseInt(propertyNumericMaxValue));
		propertiesNumeric.add(prop);
		skillProperties.add(prop);

		this.propertyNumericDescription = "";
		this.propertyNumericMaxValue = "0";
		this.propertyNumericMinValue = "0";
		this.propertyNumericName = "";

	}

	public void addTextProperty() {
		if (skillProperties == null) {
			skillProperties = new HashSet<>();
		}
		if (propertiesText == null) {
			this.propertiesText = new HashSet<>();
		}
		SkillPropertyText prop = new SkillPropertyText();
		prop.setName(propertyTextName);
		prop.setDescription(propertyTextDescription);
		prop.setNumericProperty(false);
		prop.setText("");
		propertiesText.add(prop);
		skillProperties.add(prop);
		this.propertyTextDescription = "";
		this.propertyTextName = "";
	}

}
