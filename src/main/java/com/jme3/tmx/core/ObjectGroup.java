package com.jme3.tmx.core;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;
import java.util.List;

import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Node;
import com.jme3.tmx.enums.DrawOrder;

/**
 * The object group is in fact a map layer, and is hence called "object layer"
 * in Tiled Qt.
 * 
 * @author yanmaoyuan
 * 
 */
public class ObjectGroup extends Layer {

    /**
     * The color used to display the objects in this group.
     */
    private ColorRGBA color;
    
    /**
     * This material applies to the shapes in this ObjectGroup using
     * LineMesh
     */
    private Material material;

    /**
     * Whether the objects are drawn according to the order of appearance
     * ("index") or sorted by their y-coordinate ("topdown"). Defaults to
     * "topdown".
     */
    private DrawOrder drawOrder = DrawOrder.TOPDOWN;

    private final List<MapObject> objects = new LinkedList<>();

    public ObjectGroup() {
    }

    public ObjectGroup(int width, int height) {
        super(width, height);
    }

    public ColorRGBA getColor() {
        return color;
    }

    public void setColor(ColorRGBA color) {
        this.color = color;
    }
    
    public Material getMaterial() {
        return this.material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public DrawOrder getDrawOrder() {
        return drawOrder;
    }

    public void setDrawOrder(String drawOrder) {
        this.drawOrder = DrawOrder.fromValue(drawOrder);
    }

    public void setDrawOrder(DrawOrder drawOrder) {
        this.drawOrder = drawOrder;
    }

    public List<MapObject> getObjects() {
        return objects;
    }

    public MapObject get(int id) {
        int len = objects.size();
        for (MapObject obj : objects) {
            if (obj.getId() == id) {
                return obj;
            }
        }
        return null;
    }
    
    public void add(MapObject obj) {
        obj.setObjectGroup(this);
        objects.add(obj);
    }

    public void remove(MapObject o) {
        objects.remove(o);
        o.setObjectGroup(null);
    }

    /**
     * <p>
     * getObjectAt.
     * </p>
     * 
     * @param x
     *            a double.
     * @param y
     *            a double.
     * @return a {@link MapObject} object.
     */
    public MapObject getObjectAt(double x, double y) {
        for (MapObject obj : objects) {
            // Attempt to get an object bordering the point that has no width
            if (obj.getWidth() == 0 && obj.getX() + this.x == x) {
                return obj;
            }

            // Attempt to get an object bordering the point that has no height
            if (obj.getHeight() == 0 && obj.getY() + this.y == y) {
                return obj;
            }

            Rectangle2D.Double rect = new Rectangle2D.Double(obj.getX()
                    + this.x * map.getTileWidth(), obj.getY() + this.y
                    * map.getTileHeight(), obj.getWidth(), obj.getHeight());
            if (rect.contains(x, y)) {
                return obj;
            }
        }
        return null;
    }

    // This method will work at any zoom level, provided you provide the correct
    // zoom factor. It also adds a one pixel buffer (that doesn't change with
    // zoom).
    /**
     * <p>
     * getObjectNear.
     * </p>
     * 
     * @param x a int.
     * @param y a int.
     * @param zoom a double.
     * @return a {@link MapObject} object.
     */
    public MapObject getObjectNear(int x, int y, double zoom) {
        Rectangle2D mouse = new Rectangle2D.Double(x - zoom - 1, y - zoom - 1,
                2 * zoom + 1, 2 * zoom + 1);
        Shape shape;

        for (MapObject obj : objects) {
            if (obj.getWidth() == 0 && obj.getHeight() == 0) {
                shape = new Ellipse2D.Double(obj.getX() * zoom, obj.getY()
                        * zoom, 10 * zoom, 10 * zoom);
            } else {
                shape = new Rectangle2D.Double(obj.getX() + this.x
                        * map.getTileWidth(), obj.getY() + this.y
                        * map.getTileHeight(),
                        obj.getWidth() > 0 ? obj.getWidth() : zoom,
                        obj.getHeight() > 0 ? obj.getHeight() : zoom);
            }

            if (shape.intersects(mouse)) {
                return obj;
            }
        }

        return null;
    }
    
    @Override
    public Node getVisual() {
        return (Node)visual;
    }
    
}
