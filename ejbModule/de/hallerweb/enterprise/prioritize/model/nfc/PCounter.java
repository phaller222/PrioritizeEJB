package de.hallerweb.enterprise.prioritize.model.nfc;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class PCounter {

	public int getId() {
		return id;
	}
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	protected int id;
	
	public abstract String getUuid();
	
	public abstract long getValue();

	public abstract void setValue(long value);

	public abstract void incCounter();

	public abstract void decCounter();
}
