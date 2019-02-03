/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.psu.ist.vaccine.geotxt.entities;

import edu.psu.ist.vaccine.geotxt.utils.StripStrings;
import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author ajaiswal
 */
public class Person {

	protected String name = "";
	protected String gender = "";
	protected String kind = "";
	protected ArrayList<Integer> positions = null;

	public Person(String name, String gender, String kind) {
		this.name = StripStrings.strip(name);
		this.gender = gender.toLowerCase();
		this.kind = kind.toLowerCase();
	}

	public Person(String name, int position) {
		this.name = StripStrings.strip(name);
		if (this.positions == null) {
			this.positions = new ArrayList<Integer>();
		}
		if (this.positions != null && !this.positions.contains(position)) {
			this.positions.add(position);
		}
	}

	public Person(String name, String gender, String kind, int position) {
		this.name = StripStrings.strip(name);
		this.gender = gender.toLowerCase();
		this.kind = kind.toLowerCase();
		if (this.positions == null) {
			this.positions = new ArrayList<Integer>();
		}
		if (this.positions != null && !this.positions.contains(position)) {
			this.positions.add(position);
		}
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender.toLowerCase();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getKind() {
		return kind;
	}

	public void setKind(String kind) {
		this.kind = kind.toLowerCase();
	}

	public void addPosition(int position) {
		if (this.positions == null) {
			this.positions = new ArrayList<Integer>();
		}
		if (!this.positions.contains(position)) {
			this.positions.add(position);
		}
		Collections.sort(positions);

	}

	public ArrayList<Integer> getPositions() {
		return positions;
	}

	public void setPositions(ArrayList<Integer> positions) {
		this.positions = positions;
	}

	@Override
	public boolean equals(Object per1) {
		Person person1 = (Person) per1;
		if (person1.getName().toLowerCase().equals(this.getName().toLowerCase()) && person1.getGender().toLowerCase().equals(this.getGender().toLowerCase()) && person1.getKind().toLowerCase().equals(this.getKind().toLowerCase())) {
			return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 89 * hash + (this.name != null ? this.name.toLowerCase().hashCode() : 0);
		hash = 89 * hash + (this.gender != null ? this.gender.hashCode() : 0);
		hash = 89 * hash + (this.kind != null ? this.kind.hashCode() : 0);
		return hash;
	}

	@Override
	public String toString() {
		return "Name: " + this.name + " Sex:" + this.gender + " Kind:" + this.kind;
	}
}
