package de.hallerweb.enterprise.prioritize.model.nfc.counter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;

import de.hallerweb.enterprise.prioritize.model.Department;
import de.hallerweb.enterprise.prioritize.model.nfc.PCounter;
import de.hallerweb.enterprise.prioritize.model.security.PAuthorizedObject;

@Entity
@NamedQueries({ @NamedQuery(name = "findAllIndustrieCounters", query = "select ic FROM IndustrieCounter ic"),
		@NamedQuery(name = "findIndustrieCounterByUUID", query = "select ic FROM IndustrieCounter ic WHERE ic.counter.uuid = :uuid") })
public class IndustrieCounter implements PAuthorizedObject {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	int id;

	@OneToOne
	PCounter counter;

	String name;
	String description;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public PCounter getCounter() {
		return counter;
	}

	public void setCounter(PCounter counter) {
		this.counter = counter;
	}

	public int getId() {
		return id;
	}

	public void incCounter() {
		counter.incCounter();
	}

	public void decCounter() {
		counter.decCounter();
	}

	@Override
	public Department getDepartment() {
		return null;
	}

}
