package com.jme3.tiled.spatial;

import tiled.core.Map;
import tiled.core.TileLayer;

import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

public abstract class MapRender {
	
	protected Map map;
	protected float aspect = 1f;
	
	public MapRender(Map map) {
		this.map = map;
		aspect = (float)map.getTileHeight() / map.getTileWidth();
	}
	
	/**
	 * we don't really draw 2d image in a 3d game engine, instead we 
	 * create a spatial and apply Material to it.
	 * @param layer
	 * @return
	 */
	public abstract Spatial createTileLayer(TileLayer layer);

	/**
	 * convert tile location to screen location
	 * @param x
	 * @param y
	 * @return
	 */
	public abstract Vector3f tileLoc2ScreenLoc(int x, int y);
	
	public abstract Vector2f screenLoc2TileLoc(Vector3f location);
}
