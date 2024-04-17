package io.github.jmecn.tiled.math2d;

public class RectangleD {

	private double x;
	private double y;
	private double width;
	private double height;

	public RectangleD() {
		this(0, 0, 0, 0);
	}

	public RectangleD(RectangleD r) {
		setBounds(r);
	}

	public RectangleD(double width, double height) {
		this(0, 0, width, height);
	}

	public RectangleD(double x, double y, double width, double height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	public void setBounds(RectangleD r) {
		x = r.x;
		y = r.y;
		width = r.width;
		height = r.height;
	}

	public boolean contains(double x, double y) {
		double x0 = getX();
		double y0 = getY();
		return (x >= x0 &&
				y >= y0 &&
				x < x0 + getWidth() &&
				y < y0 + getHeight());
	}

	public void translate(int dx, int dy) {
		x += dx;
		y += dy;
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

	public double getWidth() {
		return width;
	}

	public void setWidth(double width) {
		this.width = width;
	}

	public double getHeight() {
		return height;
	}

	public void setHeight(double height) {
		this.height = height;
	}
}