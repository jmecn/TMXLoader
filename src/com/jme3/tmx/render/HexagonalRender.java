package com.jme3.tmx.render;

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
	
	private boolean staggerX = false;
	private boolean staggerEven = false;
	public HexagonalRender(Map map) {
		super(map);
		
		staggerX = map.getStaggerAxis() == StaggerAxis.X;
		staggerEven = map.getStaggerIndex() == StaggerIndex.EVEN;
	}

	@Override
	public Spatial createTileLayer(TileLayer layer) {
		
		int width = layer.getWidth();
		int height = layer.getHeight();
		
		BatchNode bathNode = new BatchNode(layer.getName());
		for(int y=0; y<height; y++) {
			for(int x=0; x<width; x++) {
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
		int odd = y % 2;
		if (staggerEven) {
			odd = 1 - odd;
		}
		
		if (staggerX) {
			return new Vector3f(x*0.75f, (height-y-odd*0.5f)*aspect, 0);
		} else {
			return new Vector3f(x+odd*0.5f, (height-y-1)*0.75f*aspect, 0);
		}
	}

	@Override
	public Vector2f screenLoc2TileLoc(Vector3f location) {
		return null;
	}

}
