package com.jme3.tmx.render;

import java.util.logging.Logger;

import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.BatchNode;
import com.jme3.scene.Spatial;
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
public class OrthogonalRender extends MapRender {
	
	static Logger logger = Logger.getLogger(OrthogonalRender.class.getName());
	
	public OrthogonalRender(TiledMap map) {
		super(map);
	}
	
	@Override
	public void setupTileZOrder() {
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
	    for (int y = startY; y != endY; y += incY) {
	        for (int x = startX; x != endX; x += incX) {
	        	tileZOrders[x + y * width] = tileZIndex++;
	        }
	    }
	}

	@Override
	public Spatial render(TileLayer layer) {
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
		return new Vector3f(x * tileWidth, 0, y * tileHeight);
	}

	@Override
	public Vector2f screenLoc2TileLoc(Vector3f location) {
		return null;
	}
	
	// Coordinates System Convert
	
	// OrthogonalRenderer, StaggeredRenderer, HexagonalRenderer
	public Point pixelToScreenCoords(Point pos) {
	    return new Point(pos.x, mapSize.y - pos.y);
	}
	
	public Point pixelToTileCoords(Point pos) {
	    return new Point((int)(pos.x / tileWidth), (int)(pos.y / tileHeight));
	}
	
	public Point tileToPixelCoords(Point pos) {
	    return new Point(pos.x * tileWidth, pos.y * tileHeight);
	}
	
	public Point tileToScreenCoords(Point pos) {
		Point pixel = tileToPixelCoords(pos);
	    return pixelToScreenCoords(pixel);
	}
	
	// OrthogonalRenderer, StaggeredRenderer, HexagonalRenderer
	public Point screenToPixelCoords(Point pos) {
	    return new Point(pos.x, mapSize.y - pos.y);
	}
	
	public Point screenToTileCoords(Point pos) {
		Point pixel = screenToPixelCoords(pos);
	    return pixelToTileCoords(pixel);
	}

}
