/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.psu.ist.vaccine.geotxt.entities;

import edu.psu.ist.vaccine.geotxt.utils.StripStrings;

/**
 *
 * @author ajaiswal
 */
public class OtherEntity {

	public OtherEntity(String name, String type) {
		this.name = StripStrings.strip(name);
		this.type = type.toLowerCase();
	}

	private String name = "";

	@Override
	public boolean equals(Object obj) {
		OtherEntity obj1 = (OtherEntity) obj;
		if (this.getName().toLowerCase().equals(obj1.getName().toLowerCase()) && this.getType().toLowerCase().equals(obj1.getType().toLowerCase()))
			return true;
		return false;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 37 * hash + (this.name != null ? this.name.hashCode() : 0);
		hash = 37 * hash + (this.type != null ? this.type.hashCode() : 0);
		return hash;
	}

	@Override
	public String toString() {
		return "Name: " + this.name + " Type:" + this.type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type.toLowerCase();
	}

	private String type = "";

}
