/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.psu.ist.vaccine.geotxt.entities;

import edu.psu.ist.vaccine.geotxt.utils.PointGeometry;
import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author ajaiswal
 */
public class Organization {

	private String name = "";
	private String orgType = "";
	protected ArrayList<Integer> positions = null;
	protected PointGeometry geometry = null;

	public Organization(String name, String orgType) {
		this.name = name;
		this.orgType = orgType.toLowerCase();
		this.positions = new ArrayList<Integer>();
	}

	public Organization(String name, int position) {
		this.name = name;
		if (this.positions == null) {
			this.positions = new ArrayList<Integer>();
		}
		if (this.positions != null && !this.positions.contains(position)) {
			this.positions.add(position);
		}
	}

	public Organization(String name, String orgType, int position) {
		this.name = name;
		this.orgType = orgType.toLowerCase();
		if (this.positions == null) {
			this.positions = new ArrayList<Integer>();
		}
		if (this.positions != null && !this.positions.contains(position)) {
			this.positions.add(position);
		}
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

	public PointGeometry getGeometry() {
		return geometry;
	}

	public void setGeometry(PointGeometry geometry) {
		this.geometry = geometry;
	}

	@Override
	public boolean equals(Object obj) {
		Organization org1 = (Organization) obj;
		if (org1.getName().toLowerCase().equals(this.getName().toLowerCase()) && org1.getOrgType().toLowerCase().equals(this.getOrgType().toLowerCase())) {
			return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 59 * hash + (this.name != null ? this.name.toLowerCase().hashCode() : 0);
		hash = 59 * hash + (this.orgType != null ? this.orgType.hashCode() : 0);
		return hash;
	}

	@Override
	public String toString() {
		if (geometry != null) {
			return "Name: " + this.name + " Org Type:" + this.orgType + " Position:" + this.positions + " Geometry: " + this.geometry.toString();
		} else {
			return "Name: " + this.name + " Org Type:" + this.orgType + " Position:" + this.positions;
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getOrgType() {
		return orgType;
	}

	public void setOrgType(String orgType) {
		this.orgType = orgType.toLowerCase();
	}

}
