package com.jme3.tiled.spatial;

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
	}

	@Override
	public Spatial createTileLayer(TileLayer layer) {
		int width = layer.getWidth();
		int height = layer.getHeight();

		BatchNode bathNode = new BatchNode(layer.getName());
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				final Tile tile = layer.getTileAt(x, y);
				if (tile == null) {
					continue;
				}

				Geometry tileGeom = tile.getGeom();
				if (tileGeom == null) {
					logger.warning("Tile#" + tile.getId() + " has no texture.");
					continue;
				}

				Geometry geom = null;
				if (tileGeom != null) {
					geom = tileGeom.clone();
					geom.setLocalTranslation((x - y) * 0.5f - 0.5f, (- y - x - 1.5f) * 0.5f * aspect, 0);
					geom.scale(1f, aspect, 1f);
					bathNode.attachChild(geom);
				}
			}
		}
		bathNode.batch();

		return bathNode;
	}

	@Override
	public Vector3f tileLoc2ScreenLoc(int x, int y) {
		return new Vector3f((x - y) * 0.5f - 0.5f, (- y - x - 1.5f) * 0.5f * aspect, 0);
	}

	@Override
	public Vector2f screenLoc2TileLoc(Vector3f location) {
		return null;
	}

}
