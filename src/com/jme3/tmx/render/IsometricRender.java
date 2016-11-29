package com.jme3.tmx.render;

import java.util.logging.Logger;

import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.BatchNode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;

import tiled.core.Map;
import tiled.core.Tile;
import tiled.core.TileLayer;

public class IsometricRender extends MapRender {

	static Logger logger = Logger.getLogger(IsometricRender.class.getName());

	public IsometricRender(Map map) {
		super(map);
		
		aspect = 0.7071f;
	}

	@Override
	public Spatial createTileLayer(TileLayer layer) {
		int width = layer.getWidth();
		int height = layer.getHeight();

		BatchNode bathNode = new BatchNode(layer.getName());
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				final Tile tile = layer.getTileAt(x, y);
				if (tile == null || tile.getGeom() == null) {
					continue;
				}

				Geometry geom = tile.getGeom().clone();
				geom.scale(1f, aspect, 1f);
				geom.setLocalTranslation(tileLoc2ScreenLoc(x, y));
				bathNode.attachChild(geom);
			}
		}
		bathNode.batch();

		return bathNode;
	}

	@Override
	public Vector3f tileLoc2ScreenLoc(int x, int y) {
		return new Vector3f((height + x - y) * 0.5f, (width + height - x - y - 2) * 0.5f * aspect, 0);
	}

	@Override
	public Vector2f screenLoc2TileLoc(Vector3f location) {
	    location.x -= height * 0.5f;
	    float tileY = location.y / aspect;
	    float tileX = location.x;

	    return new Vector2f((tileY + tileX), (tileY - tileX));
	}

}
