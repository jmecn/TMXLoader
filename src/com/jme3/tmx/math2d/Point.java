package com.jme3.tmx.math2d;

/**
 * 
 * @author yanmaoyuan
 *
 */
public class Point {

	public int x;
	public int y;
	
	public Point() {
		x = y = 0;
	}
	
	public Point(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public Point(float x, float y) {
		this.x = (int) x;
		this.y = (int) y;
	}
	
	public void set(float x, float y) {
		this.x = (int) x;
		this.y = (int) y;
	}
	public void setLocation(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public double distanceSquare(int x, int y) {
		double a = this.x - x;
		double b = this.y - y;
		return a * a + b * b;
	}
	
	@Override
	public boolean equals(Object obj) {
		
		if (obj instanceof Point) {
			Point v2d = (Point)obj;
			return (v2d.x == x && v2d.y == y);
		}
		
		return super.equals(obj);
	}
	
	@Override
	public Point clone() {
		return new Point(x, y);
	}
}
