package de.hallerweb.enterprise.prioritize.model.project.goal;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import de.hallerweb.enterprise.prioritize.model.document.DocumentInfo;

@Entity
public class ProjectGoalPropertyRecord {

	@Id
	@GeneratedValue
	int id;

	@OneToOne
	ProjectGoalProperty property;

	double value;

	@OneToOne
	DocumentInfo documentInfo;

	boolean documentPropertyRecord;
	boolean numericPropertyRecord;

	public boolean isDocumentPropertyRecord() {
		return documentPropertyRecord;
	}

	public void setDocumentPropertyRecord(boolean documentPropertyRecord) {
		this.documentPropertyRecord = documentPropertyRecord;
	}

	public boolean isNumericPropertyRecord() {
		return numericPropertyRecord;
	}

	public void setNumericPropertyRecord(boolean numericPropertyRecord) {
		this.numericPropertyRecord = numericPropertyRecord;
	}

	public DocumentInfo getDocumentInfo() {
		return documentInfo;
	}

	public void setDocumentInfo(DocumentInfo documentInfo) {
		this.documentInfo = documentInfo;
	}

	public ProjectGoalProperty getProperty() {
		return property;
	}

	public void setProperty(ProjectGoalProperty property) {
		this.property = property;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public int getId() {
		return id;
	}

}
