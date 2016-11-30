package com.jme3.tmx.core;

/**
 * This element defines an array of terrain types, which can be referenced from
 * the terrain attribute of the tile element.
 * 
 * @author yanmaoyuan
 * 
 */
public class Terrain extends Base {

	/**
	 * the tileset this terrain type belongs to.
	 */
	private Tileset tileset;
	
	/**
	 * ID of this terrain type.
	 */
	private int id;

	/**
	 * The name of the terrain type.
	 */
	private String name;
	
	/**
	 * The local tile-id of the tile that represents the terrain visually.
	 * 
	 * if tile == -1 then no image display
	 */
	private int tile;

}
