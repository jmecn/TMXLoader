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
import com.jme3.tmx.core.TiledMap.RenderOrder;
import com.jme3.tmx.math2d.Point;

/**
 * Orthogonal render
 * @author yanmaoyuan
 *
 */
public class OrthogonalRenderer extends MapRenderer {
	
	static Logger logger = Logger.getLogger(OrthogonalRenderer.class.getName());
	
	public OrthogonalRenderer(TiledMap map) {
		super(map);
	}

	@Override
	public Spatial render(TileLayer layer) {
	    int startX = 0;
	    int startY = 0;
	    int endX = width - 1;
	    int endY = height - 1;
	    
	    int incX = 1, incY = 1;
	    int tmp;
	    RenderOrder renderOrder = map.getRenderOrder();
	    switch (renderOrder) {
	        case RightUp: {
	        	// swap y
	        	tmp = endY;
	        	endY = startY;
	        	startY = tmp;
	            incY = -1;
	            break;
	        }
	        case LeftDown: {
	        	// swap x
	        	tmp = endX;
	        	endX = startX;
	        	startX = tmp;
	            incX = -1;
	            break;
	        }
	        case LeftUp: {
	        	// swap x
	        	tmp = endX;
	        	endX = startX;
	        	startX = tmp;
	        	incX = -1;
	        	
	        	// swap y
	        	tmp = endY;
	        	endY = startY;
	        	startY = tmp;
	            incY = -1;
	            break;
	        }
	        case RightDown:{
	            break;
	        }
	    }
	    endX += incX;
	    endY += incY;
	    
	    int tileZIndex = 0;
	    
	    BatchNode bathNode = new BatchNode(layer.getName());
	    for (int y = startY; y != endY; y += incY) {
	        for (int x = startX; x != endX; x += incX) {
	        	final Tile tile = layer.getTileAt(x, y);
				if (tile == null || tile.getVisual() == null) {
					continue;
				}
				
				Spatial visual = tile.getVisual().clone();
				visual.setLocalTranslation(x * tileWidth, tileZIndex++, y * tileHeight);
				bathNode.attachChild(visual);
	        }
	    }
		bathNode.batch();
		
		// make it thinner
		if (tileZIndex > 0) {
			bathNode.setLocalScale(1, 1f / tileZIndex, 1);
		}
		
		return bathNode;
	}
	
	@Override
	public Spatial render(ObjectLayer layer) {
		List<ObjectNode> objects = layer.getObjects();
		int len = objects.size();
		int visualCnt = 0;

		Node node = new Node("ObjectGroup#" + layer.getName());
		for (int i = 0; i < len; i++) {
			ObjectNode obj = objects.get(i);

			if (!obj.isVisible()) {
				continue;
			}

			if (obj.getVisual() == null) {
				logger.info("obj has no visual part:" + obj.toString());
				continue;
			}

			float x = (float) obj.getX();
			float y = (float) obj.getY();
			Spatial visual = obj.getVisual().clone();
			visual.setLocalTranslation(x, visualCnt++, y);
			node.attachChild(visual);
		}
		
		if (visualCnt > 0) {
			node.setLocalScale(1f, 1f / visualCnt, 1f);
		}

		return node;
	}

	@Override
	public Spatial render(ImageLayer layer) {
		return layer.getVisual();
	}
	
	@Override
	public Vector3f tileLoc2ScreenLoc(float x, float y) {
		return new Vector3f(x * tileWidth, 0, y * tileHeight);
	}

	// Coordinates System Convert
	
	// OrthogonalRenderer, StaggeredRenderer, HexagonalRenderer
	@Override
	public Vector2f pixelToScreenCoords(float x, float y) {
	    return new Vector2f(x, y);
	}
	
	@Override
	public Point pixelToTileCoords(float x, float y) {
	    return new Point(x / tileWidth, y / tileHeight);
	}
	
	@Override
	public Vector2f tileToPixelCoords(float x, float y) {
	    return new Vector2f(x * tileWidth, y * tileHeight);
	}
	
	@Override
	public Vector2f tileToScreenCoords(float x, float y) {
		return new Vector2f(x * tileWidth, y * tileHeight);
	}
	
	@Override
	public Vector2f screenToPixelCoords(float x, float y) {
	    return new Vector2f(x, y);
	}
	
	@Override
	public Point screenToTileCoords(float x, float y) {
		return new Point(x / tileWidth, y / tileHeight);
	}

}
