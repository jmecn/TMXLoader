package com.jme3.tmx.render;

import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.BatchNode;
import com.jme3.scene.Spatial;
import com.jme3.tmx.core.Tile;
import com.jme3.tmx.core.TileLayer;
import com.jme3.tmx.core.TiledMap;
import com.jme3.tmx.core.TiledMap.Orientation;
import com.jme3.tmx.core.TiledMap.StaggerAxis;
import com.jme3.tmx.core.TiledMap.StaggerIndex;
import com.jme3.tmx.math2d.Point;

/**
 * Staggered render
 * 
 * @author yanmaoyuan
 *
 */
public class StaggeredRender extends OrthogonalRender {
	
	protected int sideLengthX;
    protected int sideOffsetX;
    protected int sideLengthY;
    protected int sideOffsetY;
    protected int rowHeight;
    protected int columnWidth;
	protected boolean staggerX = false;
	protected boolean staggerEven = false;
	protected int staggerIndex = 0;
	
	public StaggeredRender(TiledMap map) {
		super(map);
	}
	
	// 交错时景深在前的返回YES (StaggerOdd时，奇数返回YES。StaggerEven时，偶数返回YES)
	private boolean doStaggerX(int x ){
		int odd = (int)x % 2;
		if (staggerEven) {
			odd = 1 - odd;
		}
	    return staggerX && odd == 1;
	}

	// 交错时靠右的返回YES (StaggerOdd时，奇数返回YES。StaggerEven时，偶数返回YES)
	private boolean doStaggerY(int y) {
		int odd = (int)y % 2;
		if (staggerEven) {
			odd = 1 - odd;
		}
	    return !staggerX && odd == 1;
	}

	@Override
	public void updateRenderParams() {
		
		staggerX = map.getStaggerAxis() == StaggerAxis.X;
		staggerEven = map.getStaggerIndex() == StaggerIndex.EVEN;
		staggerIndex = staggerEven ? 0 : 1;
	    
	    sideLengthX = sideLengthY = 0;
	    if (map.getOrientation() == Orientation.HEXAGONAL) {
	        if (staggerX) {
	            sideLengthX = map.getHexSideLength();
	        } else {
	            sideLengthY = map.getHexSideLength();
	        }
	    }
	    
	    // 将奇数-1变为偶数
	    tileWidth = tileWidth & ~1;
	    tileHeight = tileHeight & ~1;
	    
	    sideOffsetX = (tileWidth - sideLengthX) / 2;
	    sideOffsetY = (tileHeight - sideLengthY) / 2;
	    
	    columnWidth = sideOffsetX + sideLengthX;
	    rowHeight = sideOffsetY + sideLengthY;
	    
	    // The map size is the same regardless of which indexes are shifted.
	    if (staggerX) {
	        mapSize.set(width * columnWidth + sideOffsetX,
	                                       height * (tileHeight + sideLengthY));
	        
	        if (width > 1)
	        	mapSize.set(mapSize.x, mapSize.y + rowHeight);
	        
	    } else {
	    	mapSize.set(width * (tileWidth + sideLengthX),
	                                       height * rowHeight + sideOffsetY);
	        
	        if (height > 1)
	        	mapSize.set(mapSize.x + columnWidth, mapSize.y);
	    }
	}
	
	@Override
	public void setupTileZOrder() {
	    Point startTile = new Point(0, 0);
	    Point startPos = this.tileToScreenCoords(startTile);
	    
	    int tileZIndex = 0;
	    
	    if (staggerX) {
	        boolean staggeredRow = doStaggerX(startTile.x);
	        
	        if (staggeredRow) {
	            startTile.x += 1;
	            startPos.x += columnWidth;
	            startPos.y += rowHeight;
	        }
	        
	        for (; startPos.y > 0 && startTile.y < height;) {
	            Point rowTile = startTile;
	            Point rowPos = startPos;
	            
	            for (; rowPos.x < mapSize.x && rowTile.x < width; rowTile.x += 2) {
	                if (rowTile.x>=0 && rowTile.y>=0 && rowTile.x < width && rowTile.y < height) {
	                    int tileId = rowTile.x + rowTile.y*width;
	                    tileZOrders[tileId] = tileZIndex++;
	                }
	                rowPos.x += tileWidth + sideLengthX;
	            }
	            
	            if (doStaggerX(startTile.x)) {
	                startTile.y += 1;
	            }
	            
	            if (staggeredRow) {
	                startTile.x -= 1;
	                startPos.x -= columnWidth;
	                staggeredRow = false;
	            } else {
	                startTile.x += 1;
	                startPos.x += columnWidth;
	                staggeredRow = true;
	            }
	            
	            startPos.y -= rowHeight;
	        }
	        
	    } else {
	        startPos.x = 0;
	        for (; startPos.y > 0 && startTile.y < height; startTile.y++) {
	            Point rowTile = startTile;
	            Point rowPos = startPos;
	            
	            if (doStaggerY(startTile.y))
	                rowPos.x += columnWidth;
	            
	            for (int i=0; i<width; i++) {
	                int tileId = rowTile.x + rowTile.y*width;
	                
	                // TODO debug
	                if (tileId >= tileZOrders.length) {
	                	logger.warning(tileId + " " + rowTile.x + ", " + rowTile.y);
	                } else {
	                	tileZOrders[tileId] = tileZIndex++;
	                }
	                
	                rowTile.x++;
	                rowPos.x += tileWidth + sideLengthX;
	            }
	            
	            startPos.y -= rowHeight;
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
	
	
	/**
	 * Converts tile to screen coordinates.
	 * Sub-tile return values are not supported by this renderer.
	 */
	public Point tileToScreenCoords(Point pos) {
	    int tileX = pos.x;
	    int tileY = pos.y;
	    int pixelX, pixelY;
	    
	    if (staggerX) {
	        pixelY = mapSize.y - tileY * (tileHeight + sideLengthY);
	        if (doStaggerX(tileX)) {
	            pixelY -= rowHeight;
	        }
	        pixelX = tileX * columnWidth;
	        
	    } else {
	        pixelX = tileX * (tileWidth + sideLengthX);
	        if (doStaggerY(tileY)) {
	            pixelX += columnWidth;
	        }
	        pixelY = mapSize.y - tileY * rowHeight;
	    }
	    
	    return new Point(pixelX, pixelY);
	}

	public Point tileToPixelCoords(Point pos) {
	    return screenToPixelCoords(tileToScreenCoords(pos));
	}

	/**
	 * Converts screen to tile coordinates.
	 * Sub-tile return values are not supported by this renderer.
	 */
	public Point screenToTileCoords(Point pos) {
	    
	    pos.y = mapSize.y - pos.y;
	    
	    if (staggerX)
	        pos.x -= staggerEven ? sideOffsetX : 0;
	    else
	        pos.y -= staggerEven ? sideOffsetY : 0;
	    
	    // Start with the coordinates of a grid-aligned tile
	    Point referencePoint = new Point((float)pos.x / tileWidth,
	    		(float)pos.y / tileHeight);
	    
	    // Relative x and y position on the base square of the grid-aligned tile
	    Point rel = new Point(pos.x - referencePoint.x * tileWidth,
	                              pos.y - referencePoint.y * tileHeight);
	    
	    // Adjust the reference point to the correct tile coordinates
	    if (staggerX) {
	        referencePoint.x *= 2;
	        if (staggerEven)
	            referencePoint.x++;
	    } else {
	        referencePoint.y *= 2;
	        if (staggerEven)
	            referencePoint.y++;
	    }
	    
	    float y_pos = rel.x * ((float)tileHeight / (float)tileWidth);
	    
	    // Check whether the cursor is in any of the corners (neighboring tiles)
	    if (sideOffsetY - y_pos > rel.y)
	        return topLeft(referencePoint.x, referencePoint.y);
	    if (-sideOffsetY + y_pos > rel.y)
	        return topRight(referencePoint.x, referencePoint.y);
	    
	    if (sideOffsetY + y_pos < rel.y)
	        return bottomLeft(referencePoint.x, referencePoint.y);
	    if (sideOffsetY * 3 - y_pos < rel.y)
	        return bottomRight(referencePoint.x, referencePoint.y);
	    
	    return referencePoint;
	    
	}

	public Point pixelToTileCoords(Point pos) {
	    return screenToTileCoords(pixelToScreenCoords(pos));
	}

	
	public Point topLeft(int x, int y) {
	    if (!staggerX) {
	        if (((y & 1) ^ staggerIndex) != 0)
	            return new Point(x, y - 1);
	        else
	            return new Point(x - 1, y - 1);
	    } else {
	        if (((x & 1) ^ staggerIndex) != 0)
	            return new Point(x - 1, y);
	        else
	            return new Point(x - 1, y - 1);
	    }
	}

	public Point topRight(int x, int y) {
	    if (!staggerX) {
	        if (((y & 1) ^ staggerIndex) != 0)
	            return new Point(x + 1, y - 1);
	        else
	            return new Point(x, y - 1);
	    } else {
	        if (((x & 1) ^ staggerIndex) != 0)
	            return new Point(x + 1, y);
	        else
	            return new Point(x + 1, y - 1);
	    }
	}

	public Point bottomLeft(int x, int y) {
	    if (!staggerX) {
	        if (((y & 1) ^ staggerIndex) != 0)
	            return new Point(x, y + 1);
	        else
	            return new Point(x - 1, y + 1);
	    } else {
	        if (((x & 1) ^ staggerIndex) != 0)
	            return new Point(x - 1, y + 1);
	        else
	            return new Point(x - 1, y);
	    }
	}

	public Point bottomRight(int x, int y) {
	    if (!staggerX) {
	        if (((y & 1) ^ staggerIndex) != 0)
	            return new Point(x + 1, y + 1);
	        else
	            return new Point(x, y + 1);
	    } else {
	        if (((x & 1) ^ staggerIndex) != 0)
	            return new Point(x + 1, y + 1);
	        else
	            return new Point(x + 1, y);
	    }
	}

}
