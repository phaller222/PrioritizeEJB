package de.hallerweb.enterprise.prioritize.model.project.goal;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import de.hallerweb.enterprise.prioritize.model.document.Document;

/**
 * JPA entity to represent a {@link ProjectGoalProperty} referring to a {@link Document}.
 * 
 * <p>
 * Copyright: (c) 2016
 * </p>
 * <p>
 * Peter Haller
 * </p>
 * 
 * @author peter
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class ProjectGoalPropertyDocument extends ProjectGoalProperty  {

	private String tag;		  // Target tag a document should have 	
	


	public String getTag() {
		return tag;
	}



	public void setTag(String tag) {
		this.tag = tag;
	}



	@Override
	public String toString() {
		return name;
	}
}
