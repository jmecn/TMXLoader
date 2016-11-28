package com.jme3.tiled.spatial;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.BatchNode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;

import tiled.core.Map;
import tiled.core.Map.StaggerAxis;
import tiled.core.Map.StaggerIndex;
import tiled.core.Tile;
import tiled.core.TileLayer;

public class HexagonalRender extends MapRender {

	static Logger logger = Logger.getLogger(HexagonalRender.class.getName());
	
	public HexagonalRender(Map map) {
		super(map);
	}

	@Override
	public Spatial createTileLayer(TileLayer layer) {
		int tileWidth = map.getTileWidth();
		int tileHeight = map.getTileHeight();
		
		boolean staggerX = map.getStaggerAxis() == StaggerAxis.X;
		boolean staggerEven = map.getStaggerIndex() == StaggerIndex.EVEN;
		
		int width = layer.getWidth();
		int height = layer.getHeight();
		
		BatchNode bathNode = new BatchNode(layer.getName());
		for(int y=0; y<height; y++) {
			for(int x=0; x<width; x++) {
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
					
					int odd = y % 2;
					geom.setLocalTranslation(x+odd*0.5f, (-y-1)*0.75f*aspect, 0);
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
		int odd = y % 2;
		return new Vector3f(x+odd*0.5f + 0.5f, (-y-1)*0.75f*aspect, 0);
	}

	@Override
	public Vector2f screenLoc2TileLoc(Vector3f location) {
		// TODO Auto-generated method stub
		return null;
	}

}
