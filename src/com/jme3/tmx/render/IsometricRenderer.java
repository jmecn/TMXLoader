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
import com.jme3.tmx.core.ObjectNode.ObjectType;
import com.jme3.tmx.math2d.Point;

/**
 * Isometric render
 * 
 * @author yanmaoyuan
 *
 */
public class IsometricRenderer extends MapRenderer {

	static Logger logger = Logger.getLogger(IsometricRenderer.class.getName());

	public IsometricRenderer(TiledMap map) {
		super(map);
		
	    int side = width + height;
	    mapSize.set(side * tileWidth * 0.5f, side * tileHeight * 0.5f);
	}

	@Override
	public Spatial render(TileLayer layer) {
	    int tileZIndex = 0;
	    
	    BatchNode batchNode = new BatchNode(layer.getName());
	    batchNode.setQueueBucket(Bucket.Gui);
	    for(int p=0; p < height + width - 1; p++) {
	        for(int y=0; y <= p; y++) {
	            int x = p-y;
	            if(y < height && x < width) {
	            	final Tile tile = layer.getTileAt(x, y);
					if (tile == null || tile.getVisual() == null) {
						continue;
					}

					Spatial visual = tile.getVisual().clone();
					
					flip(visual, tile, layer.isFlippedHorizontally(x, y),
							layer.isFlippedVertically(x, y),
							layer.isFlippedAntiDiagonally(x, y));
					
					visual.move(
							(height + x - y) * 0.5f * tileWidth, 
							tileZIndex++,
							(x + y) * 0.5f * tileHeight);
					batchNode.attachChild(visual);
					
	            }
	        }
	    }
	    batchNode.batch();
	    if (tileZIndex > 0) {
	    	batchNode.setLocalScale(1, 1f / tileZIndex, 1);
	    }
		return batchNode;
	}

	@Override
	public Spatial render(ObjectLayer layer) {
		
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

			if (obj.getObjectType() == ObjectType.Tile) {
				flip(visual, obj.getTile(), obj.isFlippedHorizontally(),
					obj.isFlippedVertically(),
					obj.isFlippedAntiDiagonally());
			}
			
			float x = (float) (obj.getX());
			float y = (float) (obj.getY() + obj.getHeight());
			visual.move(tileLoc2ScreenLoc(x, y));
			node.attachChild(visual);
			
		}
		return node;
	}

	@Override
	public Spatial render(ImageLayer layer) {
		return layer.getVisual();
	}
	

	@Override
	public Vector3f tileLoc2ScreenLoc(float x, float y) {
		return new Vector3f((height + x - y) * 0.5f * tileWidth, 0, (x + y) * 0.5f * tileHeight);
	}

	// Coordinates System Convert
	@Override
	public Point pixelToTileCoords(float x, float y) {
	    return new Point(x / tileHeight, y / tileHeight);
	}

	@Override
	public Vector2f tileToPixelCoords(float x, float y) {
	    return new Vector2f(x * tileHeight, y * tileHeight);
	}

	@Override
	public Point screenToTileCoords(float x, float y) {
		x -= height * tileWidth * 0.5f;
	    float tileY = y / tileHeight;
	    float tileX = x / tileWidth;
	    
	    return new Point(tileY + tileX, tileY - tileX);
	}
	
	@Override
	public Vector2f tileToScreenCoords(float x, float y) {
		
		final float originX = height * tileWidth * 0.5f;

	    return new Vector2f((x - y) * tileWidth * 0.5f + originX,
	                   (x + y) * tileHeight * 0.5f);
	}
	
	@Override
	public Vector2f screenToPixelCoords(float x, float y) {
		
	    x -= height * tileWidth * 0.5f;
	    float tileY = y / tileHeight;
	    float tileX = x / tileWidth;

	    return new Vector2f((tileY + tileX) * tileHeight,
	                   (tileY - tileX) * tileHeight);
	}

	@Override
	public Vector2f pixelToScreenCoords(float x, float y) {
		
		final float originX = height * tileWidth * 0.5f;

		final float tileY = y / tileHeight;
		final float tileX = x / tileHeight;
	    
	    return new Vector2f((tileX - tileY) * tileWidth * 0.5f + originX,
                (tileX + tileY) * tileHeight * 0.5f);
	}

}
