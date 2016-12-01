package com.jme3.tmx.render;

import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.tmx.core.ImageLayer;
import com.jme3.tmx.core.ObjectLayer;
import com.jme3.tmx.core.TileLayer;
import com.jme3.tmx.core.TiledMap;

public abstract class MapRender {
	
	protected TiledMap map;
	protected int width;
	protected int height;
	protected float aspect = 1f;
	protected float scale = 1f;
	public MapRender(TiledMap map) {
		this.map = map;
		this.width = map.getWidth();
		this.height = map.getHeight();
		this.aspect = (float)map.getTileHeight() / map.getTileWidth();
		this.aspect = 1f;
		this.scale = 1f / map.getTileWidth();
	}
	
	/**
	 * we don't really draw 2d image in a 3d game engine, instead we 
	 * create a spatial and apply Material to it.
	 * @param layer
	 * @return
	 */
	public abstract Spatial createTileLayer(TileLayer layer);

	public abstract Spatial createObjectLayer(ObjectLayer layer);

	public abstract Spatial createImageLayer(ImageLayer layer);
	
	/**
	 * convert tile location to screen location
	 * @param x
	 * @param y
	 * @return
	 */
	public abstract Vector3f tileLoc2ScreenLoc(float x, float y);
	
	public abstract Vector2f screenLoc2TileLoc(Vector3f location);
}
