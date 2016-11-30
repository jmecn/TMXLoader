package com.jme3.tmx.math2d;

import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

public class Ellipse extends Shape {
    /**
     * The X coordinate of the upper-left corner of the
     * framing rectangle of this {@code Ellipse2D}.
     * @since 1.2
     * @serial
     */
    public double x;

    /**
     * The Y coordinate of the upper-left corner of the
     * framing rectangle of this {@code Ellipse2D}.
     * @since 1.2
     * @serial
     */
    public double y;

    /**
     * The overall width of this <code>Ellipse2D</code>.
     * @since 1.2
     * @serial
     */
    public double width;

    /**
     * The overall height of the <code>Ellipse2D</code>.
     * @since 1.2
     * @serial
     */
    public double height;

    /**
     * Constructs a new <code>Ellipse2D</code>, initialized to
     * location (0,&nbsp;0) and size (0,&nbsp;0).
     * @since 1.2
     */
    public Ellipse() {
    }
    
    /**
     * Constructs and initializes an <code>Ellipse2D</code> from the
     * specified coordinates.
     *
     * @param x the X coordinate of the upper-left corner
     *        of the framing rectangle
     * @param y the Y coordinate of the upper-left corner
     *        of the framing rectangle
     * @param w the width of the framing rectangle
     * @param h the height of the framing rectangle
     * @since 1.2
     */
    public Ellipse(double x, double y, double w, double h) {
        setFrame(x, y, w, h);
    }

    /**
     * {@inheritDoc}
     * @since 1.2
     */
    public double getX() {
        return x;
    }

    /**
     * {@inheritDoc}
     * @since 1.2
     */
    public double getY() {
        return y;
    }

    /**
     * {@inheritDoc}
     * @since 1.2
     */
    public double getWidth() {
        return width;
    }

    /**
     * {@inheritDoc}
     * @since 1.2
     */
    public double getHeight() {
        return height;
    }

    /**
     * {@inheritDoc}
     * @since 1.2
     */
    public boolean isEmpty() {
        return (width <= 0.0 || height <= 0.0);
    }

    /**
     * {@inheritDoc}
     * @since 1.2
     */
    public void setFrame(double x, double y, double w, double h) {
        this.x = x;
        this.y = y;
        this.width = w;
        this.height = h;
    }

    /**
     * {@inheritDoc}
     * @since 1.2
     */
    public Rectangle2D getBounds2D() {
        return new Rectangle2D.Double(x, y, width, height);
    }
    
    /**
     * {@inheritDoc}
     * @since 1.2
     */
    public boolean contains(double x, double y) {
        // Normalize the coordinates compared to the ellipse
        // having a center at 0,0 and a radius of 0.5.
        double ellw = getWidth();
        if (ellw <= 0.0) {
            return false;
        }
        double normx = (x - getX()) / ellw - 0.5;
        double ellh = getHeight();
        if (ellh <= 0.0) {
            return false;
        }
        double normy = (y - getY()) / ellh - 0.5;
        return (normx * normx + normy * normy) < 0.25;
    }

    /**
     * {@inheritDoc}
     * @since 1.2
     */
    public boolean intersects(double x, double y, double w, double h) {
        if (w <= 0.0 || h <= 0.0) {
            return false;
        }
        // Normalize the rectangular coordinates compared to the ellipse
        // having a center at 0,0 and a radius of 0.5.
        double ellw = getWidth();
        if (ellw <= 0.0) {
            return false;
        }
        double normx0 = (x - getX()) / ellw - 0.5;
        double normx1 = normx0 + w / ellw;
        double ellh = getHeight();
        if (ellh <= 0.0) {
            return false;
        }
        double normy0 = (y - getY()) / ellh - 0.5;
        double normy1 = normy0 + h / ellh;
        // find nearest x (left edge, right edge, 0.0)
        // find nearest y (top edge, bottom edge, 0.0)
        // if nearest x,y is inside circle of radius 0.5, then intersects
        double nearx, neary;
        if (normx0 > 0.0) {
            // center to left of X extents
            nearx = normx0;
        } else if (normx1 < 0.0) {
            // center to right of X extents
            nearx = normx1;
        } else {
            nearx = 0.0;
        }
        if (normy0 > 0.0) {
            // center above Y extents
            neary = normy0;
        } else if (normy1 < 0.0) {
            // center below Y extents
            neary = normy1;
        } else {
            neary = 0.0;
        }
        return (nearx * nearx + neary * neary) < 0.25;
    }

    /**
     * {@inheritDoc}
     * @since 1.2
     */
    public boolean contains(double x, double y, double w, double h) {
        return (contains(x, y) &&
                contains(x + w, y) &&
                contains(x, y + h) &&
                contains(x + w, y + h));
    }
    
    /**
     * Returns the hashcode for this <code>Ellipse2D</code>.
     * @return the hashcode for this <code>Ellipse2D</code>.
     * @since 1.6
     */
    public int hashCode() {
        long bits = java.lang.Double.doubleToLongBits(getX());
        bits += java.lang.Double.doubleToLongBits(getY()) * 37;
        bits += java.lang.Double.doubleToLongBits(getWidth()) * 43;
        bits += java.lang.Double.doubleToLongBits(getHeight()) * 47;
        return (((int) bits) ^ ((int) (bits >> 32)));
    }

    /**
     * Determines whether or not the specified <code>Object</code> is
     * equal to this <code>Ellipse2D</code>.  The specified
     * <code>Object</code> is equal to this <code>Ellipse2D</code>
     * if it is an instance of <code>Ellipse2D</code> and if its
     * location and size are the same as this <code>Ellipse2D</code>.
     * @param obj  an <code>Object</code> to be compared with this
     *             <code>Ellipse2D</code>.
     * @return  <code>true</code> if <code>obj</code> is an instance
     *          of <code>Ellipse2D</code> and has the same values;
     *          <code>false</code> otherwise.
     * @since 1.6
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof Ellipse2D) {
            Ellipse2D e2d = (Ellipse2D) obj;
            return ((getX() == e2d.getX()) &&
                    (getY() == e2d.getY()) &&
                    (getWidth() == e2d.getWidth()) &&
                    (getHeight() == e2d.getHeight()));
        }
        return false;
    }
}
