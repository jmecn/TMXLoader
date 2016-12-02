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
import com.jme3.tmx.math2d.Point;

/**
 * Isometric render
 * 
 * @author yanmaoyuan
 *
 */
public class IsometricRender extends MapRender {

	static Logger logger = Logger.getLogger(IsometricRender.class.getName());

    float m_tile_width_half;
    float m_tile_height_half;
    float m_origin_x;
    Vector2f m_factor;
    
	public IsometricRender(TiledMap map) {
		super(map);
	}

	@Override
	public void updateRenderParams() {
	    m_tile_width_half = tileWidth * 0.5f;
	    m_tile_height_half = tileHeight * 0.5f;
	    m_origin_x = height * m_tile_width_half;
	    
	    m_factor = new Vector2f();
	    m_factor.x = m_tile_width_half/tileHeight;
	    m_factor.y = m_tile_height_half/tileHeight;
	    
	    float maxW = (width + height) * m_tile_width_half;
	    float maxH = (width + height) * m_tile_height_half;
	    mapSize.set(maxW, maxH);
	}
	
	@Override
	public void setupTileZOrder() {
	    int columnLength = width;
	    int rowLenght = height;
	    
	    int tileZIndex = 0;
	    for(int p=0; p < rowLenght + columnLength - 1; p++) {
	        for(int r=0; r <= p; r++) {
	            int c = p-r;
	            if(r < rowLenght && c < columnLength) {
	                int tileIndex = c + r*columnLength;
	                tileZOrders[tileIndex] = tileZIndex++;
	            }
	        }
	    }
	}
	
	@Override
	public Spatial render(TileLayer layer) {
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
			float x = (float) (obj.getX());
			float y = (float) (obj.getY() + obj.getHeight());
			visual.setLocalTranslation(tileLoc2ScreenLoc(x, y));
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
		return new Vector3f((height + x - y) * 0.5f * map.getTileWidth(), 0, (x + y) * 0.5f * map.getTileHeight());
	}

	@Override
	public Vector2f screenLoc2TileLoc(Vector3f location) {
	    location.x -= height * 0.5f;
	    float tileY = location.z;
	    float tileX = location.x;

	    return new Vector2f((tileY + tileX), (tileY - tileX));
	}
	
	// Coordinates System Convert
	public Point pixelToTileCoords(Point pos) {
	    return new Point((int)(pos.x / tileHeight), (int)(pos.y / tileHeight));
	}

	public Point tileToPixelCoords(Point pos) {
	    return new Point(pos.x * tileHeight, pos.y * tileHeight);
	}


	public Point tileToScreenCoords(Point pos) {
	    return new Point((pos.x - pos.y) * m_tile_width_half + m_origin_x,
	                       mapSize.y - (pos.x + pos.y) * m_tile_height_half);
	}

	public Point pixelToScreenCoords(Point pos) {
	    return new Point((pos.x - pos.y) * m_factor.x + m_origin_x,
	                       mapSize.y - (pos.x + pos.y) * m_factor.y);
	}

	public Point screenToTileCoords(Point pos) {
	    float tx = ((pos.x - m_origin_x)/m_tile_width_half + (mapSize.y - pos.y)/m_tile_height_half) * 0.5f;
	    float ty = ((mapSize.y - pos.y)/m_tile_height_half - (pos.x - m_origin_x)/m_tile_width_half) * 0.5f;
	    return new Point((int)tx, (int)ty);
	}

	public Point screenToPixelCoords(Point pos) {
		float tx = ((pos.x - m_origin_x)/m_factor.x + (mapSize.y - pos.y)/m_factor.y) * 0.5f;
		float ty = ((mapSize.y - pos.y)/m_factor.y - (pos.x - m_origin_x)/m_factor.x) * 0.5f;
	    return new Point((int)tx, (int)ty);
	}
}
