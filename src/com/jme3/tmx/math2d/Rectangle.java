package com.jme3.tmx.math2d;


public class Rectangle extends Shape {
	/**
     * The X coordinate of this <code>Rectangle2D</code>.
     */
    public double x;

    /**
     * The Y coordinate of this <code>Rectangle2D</code>.
     */
    public double y;

    /**
     * The width of this <code>Rectangle2D</code>.
     */
    public double width;

    /**
     * The height of this <code>Rectangle2D</code>.
     */
    public double height;

    /**
     * Constructs a new <code>Rectangle</code>, initialized to
     * location (0,&nbsp;0) and size (0,&nbsp;0).
     */
    public Rectangle() {
    }

    /**
     * Constructs and initializes a <code>Rectangle</code>
     * from the specified <code>double</code> coordinates.
     *
     * @param x the X coordinate of the upper-left corner
     *          of the newly constructed <code>Rectangle</code>
     * @param y the Y coordinate of the upper-left corner
     *          of the newly constructed <code>Rectangle</code>
     * @param w the width of the newly constructed
     *          <code>Rectangle</code>
     * @param h the height of the newly constructed
     *          <code>Rectangle</code>
     */
    public Rectangle(double x, double y, double w, double h) {
        setRect(x, y, w, h);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public boolean isEmpty() {
        return (width <= 0.0) || (height <= 0.0);
    }

    public void setRect(double x, double y, double w, double h) {
        this.x = x;
        this.y = y;
        this.width = w;
        this.height = h;
    }

    /**
     * Returns the <code>String</code> representation of this
     * <code>Rectangle2D</code>.
     * @return a <code>String</code> representing this
     * <code>Rectangle2D</code>.
     * @since 1.2
     */
    public String toString() {
        return getClass().getName()
            + "[x=" + x +
            ",y=" + y +
            ",w=" + width +
            ",h=" + height + "]";
    }
}
