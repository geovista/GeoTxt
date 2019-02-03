package edu.psu.ist.vaccine.geotxt.utils;

public class BBox {

	private double minx;
	private double miny;
	private double maxx;
	private double maxy;
	private int numPoints = 0;
	private double multiplier = 0.001;
	
	public BBox() {
		this.minx = -180;
		this.miny = -90;
		this.maxx = 180;
		this.maxy = 90;
	}
	
	public void expand(double x, double y) {
		if (numPoints == 0) {
			minx = x-(Math.abs(x)*multiplier);
			miny = y-(Math.abs(y)*multiplier);
			maxx = x+(Math.abs(x)*multiplier);
			maxy = y+(Math.abs(y)*multiplier);
		} else {
			if (x < minx) {
				minx = x;
			}
			if (x > maxx) {
				maxx = x;
			}
			if (y < miny) {
				miny = y;
			}
			if (y > maxy) {
				maxy = y;
			}
		}
		minx = minx > -180 ? minx : -180;
		miny = miny > -90 ? miny : -90;
		maxx = maxx < 180 ? maxx : 180;
		maxy = maxy < 90 ? maxy : 90;
		numPoints++;
	}
	
	public void compress(double x, double y) {
		if (numPoints == 0) {
			minx = x-(Math.abs(x)*multiplier);
			miny = y-(Math.abs(y)*multiplier);
			maxx = x+(Math.abs(x)*multiplier);
			maxy = y+(Math.abs(y)*multiplier);
		} else {
			if (x > minx && x < maxx) {
				minx = x;
			}
			if (x < maxx && x > minx) {
				maxx = x;
			}
			if (y > miny && y < maxy) {
				miny = y;
			}
			if (y < maxy && y > miny) {
				maxy = y;
			}
		}
		minx = minx > -180 ? minx : -180;
		miny = miny > -90 ? miny : -90;
		maxx = maxx < 180 ? maxx : 180;
		maxy = maxy < 90 ? maxy : 90;
		numPoints++;
	}
	
	public double getMinx() {
		return minx;
	}

	public double getMiny() {
		return miny;
	}

	public double getMaxx() {
		return maxx;
	}

	public double getMaxy() {
		return maxy;
	}
	
}
