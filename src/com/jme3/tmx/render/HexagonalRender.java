package com.jme3.tmx.render;

import java.util.logging.Logger;

import com.jme3.tmx.core.TiledMap;
import com.jme3.tmx.math2d.Point;

/**
 * Hexagonal render
 * 
 * @author yanmaoyuan
 *
 */
public class HexagonalRender extends StaggeredRender {

	static Logger logger = Logger.getLogger(HexagonalRender.class.getName());
	
	public HexagonalRender(TiledMap map) {
		super(map);
	}
	
    
    static final Point[] offsetsStaggerX = {
        new Point(0, 0), new Point(1, -1), new Point(1, 0), new Point(2, 0)
    };
    
    static final Point[] offsetsStaggerY = {
    	new Point(0, 0), new Point(-1, 1), new Point(0, 1), new Point(0, 2)
    };
    
    
	/**
	 * Converts screen to tile coordinates.
	 * Sub-tile return values are not supported by this renderer.
	 */
	public Point screenToTileCoords(Point pos) {
	    
	    pos.y = mapSize.y - pos.y;
	    
	    if (staggerX)
	        pos.x -= staggerEven ? tileWidth : sideOffsetX;
	    else
	        pos.y -= staggerEven ? tileHeight : sideOffsetY;
	    
	    // Start with the coordinates of a grid-aligned tile
	    Point referencePoint = new Point((float)(pos.x / (tileWidth + sideLengthX)),
	    		(float)(pos.y / (tileHeight + sideLengthY)));
	    
	    // Relative x and y position on the base square of the grid-aligned tile
	    Point rel = new Point(pos.x - referencePoint.x * (tileWidth + sideLengthX),
	                              pos.y - referencePoint.y * (tileHeight + sideLengthY));
	    
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
	    
	    // Determine the nearest hexagon tile by the distance to the center
	    Point[] centers = new Point[4];
	    
	    if (staggerX) {
	        int left = sideLengthX / 2;
	        int centerX = left + columnWidth;
	        int centerY = tileHeight / 2;
	        
	        centers[0] = new Point(left, centerY);
	        centers[1] = new Point(centerX, centerY - rowHeight);
	        centers[2] = new Point(centerX, centerY + rowHeight);
	        centers[3] = new Point(centerX + columnWidth, centerY);
	    } else {
	        int top = sideLengthY / 2;
	        int centerX = tileWidth / 2;
	        int centerY = top + rowHeight;
	        
	        centers[0] = new Point(centerX, top);
	        centers[1] = new Point(centerX - columnWidth, centerY);
	        centers[2] = new Point(centerX + columnWidth, centerY);
	        centers[3] = new Point(centerX, centerY + rowHeight);
	    }
	    
	    int nearest = 0;
	    float minDist = Float.MAX_VALUE;
	    
	    for (int i = 0; i < 4; i++) {
	        Point center = centers[i];
	        float dx = center.x - rel.x;
	        float dy = center.y - rel.y;
	        //        float dc = (center - rel).lengthSquared();
	        float dc = dx*dx + dy+dy;
	        if (dc < minDist) {
	            minDist = dc;
	            nearest = i;
	        }
	    }
	    
	    final Point[] offsets = staggerX ? offsetsStaggerX : offsetsStaggerY;
	    return new Point(referencePoint.x + offsets[nearest].x, referencePoint.y + offsets[nearest].y);
	}

}
