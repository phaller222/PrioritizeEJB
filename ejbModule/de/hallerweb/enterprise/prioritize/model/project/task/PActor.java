package de.hallerweb.enterprise.prioritize.model.project.task;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import de.hallerweb.enterprise.prioritize.model.PObject;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class PActor extends PObject {

}
