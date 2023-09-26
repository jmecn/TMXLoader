package com.jme3.tmx.render;

import com.jme3.tmx.core.TiledMap;
import com.jme3.tmx.math2d.Point;

/**
 * Staggered render
 * 
 * @author yanmaoyuan
 * 
 */
public class StaggeredRenderer extends HexagonalRenderer {

    public StaggeredRenderer(TiledMap map) {
        super(map);
    }

    /**
     * Converts screen to tile coordinates. Sub-tile return values are not
     * supported by this renderer.
     * 
     * This override exists because the method used by the HexagonalRenderer
     * does not produce nice results for isometric shapes in the tile corners.
     */
    public Point screenToTileCoords(float x, float y) {

        if (staggerX)
            x -= staggerEven ? sideOffsetX : 0;
        else
            y -= staggerEven ? sideOffsetY : 0;

        // Start with the coordinates of a grid-aligned tile
        Point referencePoint = new Point(x / tileWidth, y / tileHeight);

        // Relative x and y position on the base square of the grid-aligned tile
        Point rel = new Point(x - referencePoint.x * tileWidth, y - referencePoint.y * tileHeight);

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

        float y_pos = rel.x * ((float) tileHeight / tileWidth);

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

}
