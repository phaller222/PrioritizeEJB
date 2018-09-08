package de.hallerweb.enterprise.prioritize.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity()
public class Location {

	public Location() {
		super();
	}

	public Location(double x, double y) {
		this.x = x;
		this.y = y;
	}

	@Id
	@GeneratedValue
	private int id;
	String name;
	double x;
	double y;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public int getId() {
		return id;
	}

}
