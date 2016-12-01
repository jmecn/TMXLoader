package com.jme3.tmx.core;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import com.jme3.math.ColorRGBA;

/**
 * The object group is in fact a map layer, and is hence called "object layer"
 * in Tiled Qt.
 * 
 * @author yanmaoyuan
 * 
 */
public class ObjectLayer extends Layer {

	static Logger logger = Logger.getLogger(ObjectLayer.class.getName());

	/**
	 * Whether the objects are drawn according to the order of appearance
	 * ("index") or sorted by their y-coordinate ("topdown"). Defaults to
	 * "topdown".
	 */
	public enum DrawOrderType {
		INDEX, TOPDOWN;
	}

	/**
	 * The color used to display the objects in this group.
	 */
	private ColorRGBA color;

	/**
	 * Whether the objects are drawn according to the order of appearance
	 * ("index") or sorted by their y-coordinate ("topdown"). Defaults to
	 * "topdown".
	 */
	private DrawOrderType draworder = DrawOrderType.TOPDOWN;

	private List<ObjectNode> objects = new LinkedList<ObjectNode>();

	public ObjectLayer() {
	}

	public ObjectLayer(int width, int height) {
		super(width, height);
	}

	public ColorRGBA getColor() {
		return color;
	}

	public void setColor(ColorRGBA color) {
		this.color = color;
	}

	public DrawOrderType getDraworder() {
		return draworder;
	}

	public void setDraworder(String draworder) {
		try {
			this.draworder = DrawOrderType.valueOf(draworder.toUpperCase());
		} catch (IllegalArgumentException e) {
			logger.warning("Unknown draworder '" + draworder + "'");
		}
	}

	public void setDraworder(DrawOrderType draworder) {
		this.draworder = draworder;
	}

	public List<ObjectNode> getObjects() {
		return objects;
	}

	public void add(ObjectNode obj) {
		obj.setId(objects.size());
		obj.setObjectGroup(this);
		objects.add(obj);
	}

	public void remove(ObjectNode o) {
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
	 * @return a {@link tiled.core.MapObject} object.
	 */
	public ObjectNode getObjectAt(double x, double y) {
		for (ObjectNode obj : objects) {
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
	 * @param x
	 *            a int.
	 * @param y
	 *            a int.
	 * @param zoom
	 *            a double.
	 * @return a {@link tiled.core.MapObject} object.
	 */
	public ObjectNode getObjectNear(int x, int y, double zoom) {
		Rectangle2D mouse = new Rectangle2D.Double(x - zoom - 1, y - zoom - 1,
				2 * zoom + 1, 2 * zoom + 1);
		Shape shape;

		for (ObjectNode obj : objects) {
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

}
