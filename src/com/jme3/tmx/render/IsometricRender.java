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

public class IsometricRender extends MapRender {

	static Logger logger = Logger.getLogger(IsometricRender.class.getName());

	public IsometricRender(TiledMap map) {
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
		return new Vector3f((height + x - y) * 0.5f * map.getTileWidth(), 0, (x + y) * 0.5f * map.getTileHeight());
	}

	@Override
	public Vector2f screenLoc2TileLoc(Vector3f location) {
	    location.x -= height * 0.5f;
	    float tileY = location.y / aspect;
	    float tileX = location.x;

	    return new Vector2f((tileY + tileX), (tileY - tileX));
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
			float x = (float) (obj.getX());
			float y = (float) (obj.getY() + obj.getHeight());
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
