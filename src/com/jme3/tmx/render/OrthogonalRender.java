package com.jme3.tmx.render;

import java.util.List;
import java.util.logging.Logger;

import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.BatchNode;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.tmx.core.ImageLayer;
import com.jme3.tmx.core.ObjectLayer;
import com.jme3.tmx.core.ObjectNode;
import com.jme3.tmx.core.Tile;
import com.jme3.tmx.core.TileLayer;
import com.jme3.tmx.core.TiledMap;

/**
 * Orthogonal render
 * @author yanmaoyuan
 *
 */
public class OrthogonalRender extends MapRender {
	
	static Logger logger = Logger.getLogger(OrthogonalRender.class.getName());
	
	public OrthogonalRender(TiledMap map) {
		super(map);
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
		
		bathNode.setQueueBucket(Bucket.Sky);
		
		return bathNode;
	}

	@Override
	public Spatial createObjectLayer(ObjectLayer layer) {
		List<ObjectNode> objects = layer.getObjects();
		int len = objects.size();
		
		Node node = new Node("ObjectGroup#" + layer.getName());
		for(int i=0; i<len; i++) {
			ObjectNode obj = objects.get(i);
			
			if (!obj.isVisible()) {
				//continue;
			}
			
			if (obj.getVisual() == null ) {
				logger.info("obj has no visual part:" + obj.toString());
				continue;
			}
			
			float x = (float) obj.getX();
			float y = (float) obj.getY();
			
			Spatial visual = obj.getVisual().clone();
			visual.setLocalTranslation(x, 0, y);
			node.attachChild(visual);
		}
		
		node.setQueueBucket(Bucket.Sky);
		return node;
	}

	@Override
	public Spatial createImageLayer(ImageLayer layer) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Vector3f tileLoc2ScreenLoc(float x, float y) {
		return new Vector3f(x * map.getTileWidth(), 0, y * map.getTileHeight());
	}

	@Override
	public Vector2f screenLoc2TileLoc(Vector3f location) {
		return null;
	}

}
