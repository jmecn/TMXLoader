package io.github.jmecn.tiled.core;

import com.jme3.math.ColorRGBA;
import com.jme3.scene.Node;

/**
 * A layer of a map.
 * @author yanmaoyuan
 *
 */
public class Layer extends Base {
    
    protected TiledMap map;

    protected GroupLayer parent;

    /**
     * Unique ID of the layer (defaults to 0, with valid IDs being at least 1).
     * Each layer that added to a map gets a unique id. Even if a layer is deleted,
     * no layer ever gets the same ID. Can not be changed in Tiled. (since Tiled 1.2)
     */
    protected int id;

    /**
     * The name of the layer. (defaults to “”)
     */
    protected String name;

    /**
     * The class of the layer (since 1.9, defaults to “”).
     */
    protected String clazz;

    /**
     * The x,y coordinate of the layer in tiles. Defaults to 0 and can no longer
     * be changed in Tiled.
     */
    protected int x = 0;

    protected int y = 0;

    /**
     * The width and height of the layer in tiles. Traditionally required, but
     * as of Tiled always the same as the map width and height for fixed-size maps.
     */
    protected int width;
    protected int height;

    /**
     * The opacity of the layer as a value from 0 to 1. Defaults to 1.
     */
    protected double opacity = 1.0;

    /**
     * Whether the layer is shown (1) or hidden (0). Defaults to 1.
     */
    protected boolean visible = true;

    protected boolean locked = false;

    /**
     * A tint color that is multiplied with any tiles drawn by this layer
     * in #AARRGGBB or #RRGGBB format (optional).
     */
    protected ColorRGBA tintColor = new ColorRGBA(1, 1, 1, 1);

    /**
     * Horizontal offset for this layer in pixels. Defaults to 0. (since 0.14)
     */
    protected int offsetX = 0;

    /**
     * Vertical offset for this layer in pixels. Defaults to 0. (since 0.14)
     */
    protected int offsetY = 0;

    /**
     * Horizontal parallax factor for this layer. Defaults to 1. (since 1.5)
     */
    protected float parallaxX = 1;

    /**
     * Vertical parallax factor for this layer. Defaults to 1. (since 1.5)
     */
    protected float parallaxY = 1;

    public Layer() {
        this.width = 0;
        this.height = 0;
    }
    /**
     * Constructor for TMXLayer.
     * 
     * @param width width in tiles
     * @param height height in tiles
     */
    public Layer(int width, int height) {
        this.width = width;
        this.height = height;
    }
    
    public Layer(TiledMap map) {
        this.map = map;
    }
    
    public TiledMap getMap() {
        return map;
    }

    public void setMap(TiledMap map) {
        this.map = map;
    }

    public GroupLayer getParent() {
        return parent;
    }

    public void setParent(GroupLayer parent) {
        this.parent = parent;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public double getOpacity() {
        return opacity;
    }

    public void setOpacity(double opacity) {
        this.opacity = opacity;
    }

    public boolean isVisible() {
        if (parent != null) {
            return parent.visible;
        } else {
            return visible;
        }
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isLocked() {
        if (parent != null) {
            return parent.isLocked();
        } else {
            return locked;
        }
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public ColorRGBA getTintColor() {
        if (parent != null) {
            return parent.getTintColor();
        } else {
            return tintColor;
        }
    }

    public void setTintColor(ColorRGBA tintColor) {
        this.tintColor = tintColor;
    }

    public int getOffsetX() {
        if (parent != null) {
            return parent.getOffsetX();
        } else {
            return offsetX;
        }
    }

    public int getOffsetY() {
        if (parent != null) {
            return parent.getOffsetY();
        } else {
            return offsetY;
        }
    }
    
    public void setOffset(int offsetX, int offsetY) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    // When the parallax scrolling factor is set on a group layer, it applies to all its child layers.
    // The effective parallax scrolling factor of a layer is determined by multiplying the parallax
    // scrolling factor by the scrolling factors of all parent layers.
    public float getParallaxX() {
        if (parent != null) {
            return parallaxX * parent.getParallaxX();
        } else {
            return parallaxX;
        }
    }

    public float getParallaxY() {
        if (parent != null) {
            return parallaxY * parent.getParallaxY();
        } else {
            return parallaxY;
        }
    }

    public void setParallaxFactor(float parallaxX, float parallaxY) {
        this.parallaxX = parallaxX;
        this.parallaxY = parallaxY;
    }

    public Node getParentVisual() {
        if (parent != null) {
            return parent.getVisual();
        }
        if (map != null) {
            return map.getVisual();
        }
        return null;
    }
}
