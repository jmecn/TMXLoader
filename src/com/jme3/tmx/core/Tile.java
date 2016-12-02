package com.jme3.tmx.core;

import java.util.ArrayList;
import java.util.List;

import com.jme3.material.Material;
import com.jme3.scene.Spatial;
import com.jme3.texture.Texture;

public class Tile extends Base {

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
	private Spatial visual = null;

	/**
	 * When you use the tile flipping feature added in Tiled Qt 0.7, the highest
	 * two bits of the gid store the flipped state. Bit 32 is used for storing
	 * whether the tile is horizontally flipped and bit 31 is used for the
	 * vertically flipped tiles. And since Tiled Qt 0.8, bit 30 means whether
	 * the tile is flipped (anti) diagonally, enabling tile rotation. These bits
	 * have to be read and cleared before you can find out which tileset a tile
	 * belongs to.
	 * 
	 * When rendering a tile, the order of operation matters. The diagonal flip
	 * (x/y axis swap) is done first, followed by the horizontal and vertical
	 * flips.
	 */
	// Bits on the far end of the 32-bit global tile ID are used for tile flags
	public final static int FLIPPED_HORIZONTALLY_FLAG = 0x80000000;
	public final static int FLIPPED_VERTICALLY_FLAG = 0x40000000;
	public final static int FLIPPED_DIAGONALLY_FLAG = 0x20000000;

	private boolean flippedHorizontally;
	private boolean flippedVertically;
	private boolean flippedAntiDiagonally;

	// animation
	private List<AnimatedFrame> animatedFrames = new ArrayList<AnimatedFrame>();

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

	/**
	 * getters && setters
	 * 
	 * @return
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

	public Texture getTexture() {
		return texture;
	}

	public void setTexture(Texture texture) {
		this.texture = texture;
	}

	public Material getMaterial() {
		return material;
	}

	public void setMaterial(Material material) {
		this.material = material;
	}

	public Spatial getVisual() {
		return visual;
	}
	
	public void setVisual(Spatial visual) {
		this.visual = visual;
	}

	public boolean isFlippedHorizontally() {
		return flippedHorizontally;
	}

	public void setFlippedHorizontally(boolean flippedHorizontally) {
		this.flippedHorizontally = flippedHorizontally;
	}

	public boolean isFlippedVertically() {
		return flippedVertically;
	}

	public void setFlippedVertically(boolean flippedVertically) {
		this.flippedVertically = flippedVertically;
	}

	public boolean isFlippedAntiDiagonally() {
		return flippedAntiDiagonally;
	}

	public void setFlippedAntiDiagonally(boolean flippedAntiDiagonally) {
		this.flippedAntiDiagonally = flippedAntiDiagonally;
	}

	public List<AnimatedFrame> getAnimatedFrames() {
		return animatedFrames;
	}

	public void setAnimatedFrames(List<AnimatedFrame> animatedFrames) {
		this.animatedFrames = animatedFrames;
	}

	public void addFrame(AnimatedFrame frame) {
		this.animatedFrames.add(frame);
	}

	public boolean isAnimated() {
		return animatedFrames.size() > 0;
	}

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
