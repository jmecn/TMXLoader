package com.jme3.tmx.core;

import java.util.ArrayList;
import java.util.List;

import com.jme3.material.Material;
import com.jme3.scene.Geometry;
import com.jme3.texture.Texture;
import com.jme3.tmx.animation.Animation;
import com.jme3.tmx.animation.Frame;

public class Tile extends Base implements Cloneable {

	private Tileset tileset;
	private int id = -1;
	private int gid;

	/**
	 * position in the image
	 */
	private int x;
	private int y;
	private int width;
	private int height;

	private String imgSource;

	private Texture texture;

	// setup in jme3
	private Material material;

	// animation
	private List<Animation> animations = new ArrayList<Animation>();

	// Terrain
	/**
	 * Defines the terrain type of each corner of the tile, given as
	 * comma-separated indexes in the terrain types array in the order top-left,
	 * top-right, bottom-left, bottom-right. Leaving out a value means that
	 * corner has no terrain. (optional) (since 0.9)
	 */
	private int terrain = -1;// unsigned int
	/**
	 * A percentage indicating the probability that this tile is chosen when it
	 * competes with others while editing with the terrain tool. (optional)
	 * (since 0.9)
	 */
	private float probability = -1;

	/**
	 * Default constructor
	 */
	public Tile() {
		x = y = 0;
		width = height = -1;
	}

	public Tile(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	/*
	 * getters && setters
	 */
	
	public Tileset getTileset() {
		return tileset;
	}

	public void setTileset(Tileset tileset) {
		this.tileset = tileset;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getGid() {
		return gid;
	}

	public void setGid(int gid) {
		this.gid = gid;
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

	public String getImgSource() {
		return imgSource;
	}

	public void setImgSource(String imgSource) {
		this.imgSource = imgSource;
	}

	/*
	 * This is the visual part of a tile.
	 * 
	 * Texture and Material are set by TMXLoader when loading a tileset.
	 * 
	 * Spatial is created by MapRenderer in <code>createVisual(Tileset)</code>.
	 */

	public Texture getTexture() {
		return texture;
	}

	/**
	 * setTexture
	 * 
	 * @param texture
	 */
	public void setTexture(Texture texture) {
		this.texture = texture;
	}

	public Material getMaterial() {
		return material;
	}

	/**
	 * setMaterial
	 * 
	 * @param material
	 */
	public void setMaterial(Material material) {
		this.material = material;
	}

	public void setVisual(Geometry visual) {
		this.visual = visual;
	}

	/*
	 * This part is about the animation.
	 * 
	 * As of Tiled 0.10, each tile can have exactly one animation associated
	 * with it. In the future, there could be support for multiple named
	 * animations on a tile
	 */

	/**
	 * Add an animation to the tile.
	 * 
	 * @param animation
	 */
	public void addAnimation(Animation animation) {
		animation.setId(animations.size());
		animations.add(animation);
	}

	/**
	 * Add an animation to the tile.
	 * 
	 * @param name
	 *            animation's name
	 * @param frames
	 *            the frames
	 */
	public void addAnimation(String name, List<Frame> frames) {
		Animation animation = new Animation(name, frames);
		animation.setId(animations.size());
		animations.add(animation);
	}

	/**
	 * Add an animation to the tile.
	 * 
	 * @param frames
	 *            the frames
	 */
	public void addAnimation(List<Frame> frames) {
		Animation animation = new Animation(null, frames);
		animation.setId(animations.size());
		animations.add(animation);
	}

	public Animation getAnimation(String name) {
		int len = animations.size();
		if (len == 0 || name == null) {
			return null;
		}

		for (int i = 0; i < len; i++) {
			Animation anim = animations.get(i);
			if (anim.equalsIgnoreCase(name)) {
				return anim;
			}
		}

		return null;
	}

	public List<Animation> getAnimations() {
		return animations;
	}

	public boolean isAnimated() {
		return animations.size() > 0;
	}

	/*
	 * This part is about he terrain. It's useless in jme3.
	 */

	public int getTerrain() {
		return terrain;
	}

	public void setTerrain(int terrain) {
		this.terrain = terrain;
	}

	public float getProbability() {
		return probability;
	}

	public void setProbability(float probability) {
		this.probability = probability;
	}
	
}
