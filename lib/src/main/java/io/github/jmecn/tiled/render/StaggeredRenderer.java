package io.github.jmecn.tiled.render;

import io.github.jmecn.tiled.core.TiledMap;
import io.github.jmecn.tiled.math2d.Point;

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
     * This override exists because the method used by the HexagonalRenderer
     * does not produce nice results for isometric shapes in the tile corners.
     */
    @Override
    public Point screenToTileCoords(float x, float y) {

        if (staggerX) {
            x -= staggerEven ? sideOffsetX : 0;
        } else {
            y -= staggerEven ? sideOffsetY : 0;
        }

        // Start with the coordinates of a grid-aligned tile
        Point referencePoint = new Point(x / tileWidth, y / tileHeight);

        // Relative x and y position on the base square of the grid-aligned tile
        Point rel = new Point(x - referencePoint.x * tileWidth, y - referencePoint.y * tileHeight);

        // Adjust the reference point to the correct tile coordinates
        adjustReferencePoint(referencePoint);

        float yPos = rel.x * ((float) tileHeight / tileWidth);

        // Check whether the cursor is in any of the corners (neighboring tiles)
        if (sideOffsetY - yPos > rel.y) {
            return topLeft(referencePoint.x, referencePoint.y);
        } else if (-sideOffsetY + yPos > rel.y) {
            return topRight(referencePoint.x, referencePoint.y);
        } else if (sideOffsetY + yPos < rel.y) {
            return bottomLeft(referencePoint.x, referencePoint.y);
        } else if (sideOffsetY * 3 - yPos < rel.y) {
            return bottomRight(referencePoint.x, referencePoint.y);
        } else {
            return referencePoint;
        }
    }
}
