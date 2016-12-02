package com.jme3.tmx.render;

import java.util.List;
import java.util.logging.Logger;

import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.BatchNode;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.tmx.core.ImageLayer;
import com.jme3.tmx.core.ObjectLayer;
import com.jme3.tmx.core.ObjectNode;
import com.jme3.tmx.core.Tile;
import com.jme3.tmx.core.TileLayer;
import com.jme3.tmx.core.TiledMap;
import com.jme3.tmx.core.TiledMap.StaggerAxis;
import com.jme3.tmx.core.TiledMap.StaggerIndex;

public class HexagonalRender extends MapRender {

	static Logger logger = Logger.getLogger(HexagonalRender.class.getName());
	
	private boolean staggerX = false;
	private boolean staggerEven = false;
	public HexagonalRender(TiledMap map) {
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
				if (tile == null || tile.getVisual() == null) {
					continue;
				}
				
				Spatial visual = tile.getVisual().clone();
				visual.setLocalTranslation(tileLoc2ScreenLoc(x, y));
				bathNode.attachChild(visual);
				
			}
		}
		bathNode.batch();
		
		return bathNode;
	}

	@Override
	public Vector3f tileLoc2ScreenLoc(float x, float y) {
		int odd;
		if (staggerX) {
			odd = (int)x % 2;
		} else {
			odd = (int)y % 2;
		}
		
		if (staggerEven) {
			odd = 1 - odd;
		}
		
		if (staggerX) {
			return new Vector3f(x*0.75f * map.getTileWidth(), 0, (y+odd*0.5f)*map.getTileHeight());
		} else {
			return new Vector3f((x+odd*0.5f) * map.getTileWidth(), 0, y*0.75f*map.getTileHeight());
		}
	}

	@Override
	public Vector2f screenLoc2TileLoc(Vector3f location) {
		return null;
	}

	@Override
	public Spatial createObjectLayer(ObjectLayer layer) {
		List<ObjectNode> objects = layer.getObjects();
		int len = objects.size();
		
		Node node = new Node("ObjectGroup#" + layer.getName());
		for(int i=0; i<len; i++) {
			ObjectNode obj = objects.get(i);
			
			if (obj.getVisual() == null ) {
				logger.info("obj has no visual part:" + obj.toString());
				continue;
			}
			
			Spatial visual = obj.getVisual().clone();
			float x = (float) obj.getX();
			float y = (float) obj.getY();
			visual.setLocalTranslation(tileLoc2ScreenLoc(x, y));
			node.attachChild(visual);
			
		}
		
		return node;
	}

	@Override
	public Spatial createImageLayer(ImageLayer layer) {
		// TODO Auto-generated method stub
		return null;
	}

}
