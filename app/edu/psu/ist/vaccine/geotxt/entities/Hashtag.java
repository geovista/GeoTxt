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
public class Hashtag {

	public String hashtag = "";
	public String type = "";

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type.toLowerCase();
	}

	public String getHashtag() {
		return hashtag;
	}

	public void setHashtag(String hashtag) {
		this.hashtag = hashtag.toLowerCase();
	}

	public Hashtag(String tag, String type) {
		this.hashtag = StripStrings.strip(tag).toLowerCase();
		this.type = type.toLowerCase();
	}

	public Hashtag(String tag) {
		this.hashtag = StripStrings.strip(tag).toLowerCase();
		this.type = "hashtag".toLowerCase();
	}

	@Override
	public String toString() {
		return "Name:" + this.hashtag + " Type:" + this.type;
	}

	@Override
	public boolean equals(Object tag) {
		if (this.hashtag.toLowerCase().equals(((Hashtag) tag).getHashtag().toLowerCase())) {
			return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 29 * hash + (this.hashtag != null ? this.hashtag.toLowerCase().hashCode() : 0);
		hash = 29 * hash + (this.type != null ? this.type.hashCode() : 0);
		return hash;
	}

}
