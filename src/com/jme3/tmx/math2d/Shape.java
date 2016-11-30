package com.jme3.tmx.math2d;

public abstract class Shape {

    /**
     * Returns the X coordinate of the upper-left corner of
     * the framing rectangle in <code>double</code> precision.
     * @return the X coordinate of the upper-left corner of
     * the framing rectangle.
     * @since 1.2
     */
    public abstract double getX();

    /**
     * Returns the Y coordinate of the upper-left corner of
     * the framing rectangle in <code>double</code> precision.
     * @return the Y coordinate of the upper-left corner of
     * the framing rectangle.
     * @since 1.2
     */
    public abstract double getY();

    /**
     * Returns the width of the framing rectangle in
     * <code>double</code> precision.
     * @return the width of the framing rectangle.
     * @since 1.2
     */
    public abstract double getWidth();

    /**
     * Returns the height of the framing rectangle
     * in <code>double</code> precision.
     * @return the height of the framing rectangle.
     * @since 1.2
     */
    public abstract double getHeight();

    /**
     * Returns the smallest X coordinate of the framing
     * rectangle of the <code>Shape</code> in <code>double</code>
     * precision.
     * @return the smallest X coordinate of the framing
     *          rectangle of the <code>Shape</code>.
     * @since 1.2
     */
    public double getMinX() {
        return getX();
    }

    /**
     * Returns the smallest Y coordinate of the framing
     * rectangle of the <code>Shape</code> in <code>double</code>
     * precision.
     * @return the smallest Y coordinate of the framing
     *          rectangle of the <code>Shape</code>.
     * @since 1.2
     */
    public double getMinY() {
        return getY();
    }

    /**
     * Returns the largest X coordinate of the framing
     * rectangle of the <code>Shape</code> in <code>double</code>
     * precision.
     * @return the largest X coordinate of the framing
     *          rectangle of the <code>Shape</code>.
     * @since 1.2
     */
    public double getMaxX() {
        return getX() + getWidth();
    }

    /**
     * Returns the largest Y coordinate of the framing
     * rectangle of the <code>Shape</code> in <code>double</code>
     * precision.
     * @return the largest Y coordinate of the framing
     *          rectangle of the <code>Shape</code>.
     * @since 1.2
     */
    public double getMaxY() {
        return getY() + getHeight();
    }

    /**
     * Returns the X coordinate of the center of the framing
     * rectangle of the <code>Shape</code> in <code>double</code>
     * precision.
     * @return the X coordinate of the center of the framing rectangle
     *          of the <code>Shape</code>.
     * @since 1.2
     */
    public double getCenterX() {
        return getX() + getWidth() / 2.0;
    }

    /**
     * Returns the Y coordinate of the center of the framing
     * rectangle of the <code>Shape</code> in <code>double</code>
     * precision.
     * @return the Y coordinate of the center of the framing rectangle
     *          of the <code>Shape</code>.
     * @since 1.2
     */
    public double getCenterY() {
        return getY() + getHeight() / 2.0;
    }
}
