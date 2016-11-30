package com.jme3.tmx.core;

/**
 * A layer of a map.
 * @author yanmaoyuan
 *
 */
public class Layer extends Base {
	
	protected TiledMap map;
	
	/**
	 * The name of the layer.
	 */
	protected String name;

	/**
	 * The x,y coordinate of the layer in tiles. Defaults to 0 and can no longer
	 * be changed in Tiled Qt.
	 */
	protected int x = 0;

	protected int y = 0;

	/**
	 * The width and height of the layer in tiles. Traditionally required, but
	 * as of Tiled Qt always the same as the map width.
	 */
	protected int width, height;

	/**
	 * The opacity of the layer as a value from 0 to 1. Defaults to 1.
	 */
	protected float opacity = 1.0f;

	/**
	 * Whether the layer is shown (1) or hidden (0). Defaults to 1.
	 */
	protected boolean visible = true;

	/**
	 * Rendering offset for this layer in pixels. Defaults to 0. (since 0.14)
	 */
	protected int offsetx = 0, offsety = 0;

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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public float getOpacity() {
		return opacity;
	}

	public void setOpacity(float opacity) {
		this.opacity = opacity;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public int getOffsetx() {
		return offsetx;
	}

	public int getOffsety() {
		return offsety;
	}
	
	public void setOffset(int offsetx, int offsety) {
		this.offsetx = offsetx;
		this.offsety = offsety;
	}
	
}
