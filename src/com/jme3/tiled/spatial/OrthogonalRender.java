package com.jme3.tiled.spatial;

import java.util.logging.Logger;

import tiled.core.Map;
import tiled.core.Tile;
import tiled.core.TileLayer;

import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.BatchNode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;

/**
 * Orthogonal render
 * @author yanmaoyuan
 *
 */
public class OrthogonalRender extends MapRender {
	
	static Logger logger = Logger.getLogger(OrthogonalRender.class.getName());
	
	public Vector3f centerOffset;
	public OrthogonalRender(Map map) {
		super(map);
	}

	/** {@inheritDoc} */
	@Override
	public Spatial createTileLayer(TileLayer layer) {
		int width = layer.getWidth();
		int height = layer.getHeight();
		
		float h = map.getHeight() * aspect;
		
		BatchNode bathNode = new BatchNode(layer.getName());
		for(int y=0; y<height; y++) {
			for(int x=0; x<width; x++) {
				final Tile tile = layer.getTileAt(x, y);
				if (tile == null || tile.getGeom() == null) {
					continue;
				}
				
				Geometry geom = tile.getGeom().clone();
				geom.setLocalTranslation(x, h-(y+1)*aspect, 0);
				bathNode.attachChild(geom);
			}
		}
		bathNode.batch();
		
		return bathNode;
	}

	@Override
	public Vector3f tileLoc2ScreenLoc(int x, int y) {
		return new Vector3f(x, (height-y-1)*aspect, 0);
	}

	@Override
	public Vector2f screenLoc2TileLoc(Vector3f location) {
		return null;
	}
}
