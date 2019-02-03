package edu.psu.ist.vaccine.geotxt.ner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.ning.http.client.providers.grizzly.Utils;

import edu.psu.ist.vaccine.geotxt.entities.Location;
import edu.psu.ist.vaccine.geotxt.entities.Organization;
import edu.psu.ist.vaccine.geotxt.entities.Person;
import edu.psu.ist.vaccine.geotxt.utils.HashtagProcessor;

public class NamedEntities {
	public String text;
	public List<Location> locs = new ArrayList<Location>();
	public List<Organization> orgs = new ArrayList<Organization>();
	public List<Person> pers = new ArrayList<Person>();

	public void addLoc(Location loc) {
		if (!this.locs.contains(loc)) {
			this.locs.add(loc);
		} else {
			int index = this.locs.indexOf(loc);
			for (int p : loc.getPositions()) {
				this.locs.get(index).addPosition(p);
			}
		}
	}

	public void addPer(Person per) {
		if (!this.pers.contains(per) && !per.getName().equals("@")) {
			this.pers.add(per);
		} else {
			int index = this.pers.indexOf(per);
			for (int p : per.getPositions()) {
				this.pers.get(index).addPosition(p);
			}
		}
	}

	public void addOrg(Organization org) {
		if (!this.orgs.contains(org)) {
			this.orgs.add(org);
		} else {
			int index = this.orgs.indexOf(org);
			for (int p : org.getPositions()) {
				this.orgs.get(index).addPosition(p);
			}
		}
	}

	
	public void adjustCharIndexesForHashtags(HashtagProcessor processor) {
		this.shiftChars(processor.getHashtagCharIndexes(), 1);
		this.shiftChars(processor.getCapitalCharIndexes(), -1);
	}

	
	private void shiftChars(ArrayList<Integer> specialChars, int charShift) {
		for (int specialChar : specialChars) {
			for (Location l : this.locs) {
				for (int p : l.getPositions()) {
					if (p >= specialChar) {
						l.getPositions().set(l.getPositions().indexOf(p), p + charShift);
					}
				}
			}
			for (Organization o : this.orgs) {
				for (int p : o.getPositions()) {
					if (p >= specialChar) {
						o.getPositions().set(o.getPositions().indexOf(p), p + charShift);
					}
				}
			}
			for (Person pe : this.pers) {
				for (int p : pe.getPositions()) {
					if (p >= specialChar) {
						pe.getPositions().set(pe.getPositions().indexOf(p), p + charShift);
					}
				}
			}
		}
	}

	@Override
	public String toString() {
		return "Original Text = " + text + ", Locations: " + locs.toString() + ", Organizations: " + orgs.toString() + ", Persons: " + pers.toString();
	}
}
