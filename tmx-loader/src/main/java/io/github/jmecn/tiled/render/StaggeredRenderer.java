package io.github.jmecn.tiled.render;

import com.jme3.material.Material;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import io.github.jmecn.tiled.core.TiledMap;
import io.github.jmecn.tiled.math2d.Point;
import io.github.jmecn.tiled.render.grid.Diamond;
import io.github.jmecn.tiled.render.grid.Hexagon;
import io.github.jmecn.tiled.render.shape.Rect;

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

    @Override
    public Spatial createTileGrid(Material material) {
        // create a grid
        Diamond mesh = new Diamond(map.getTileWidth(), map.getTileHeight(), false);
        Geometry geom = new Geometry("TileGrid", mesh);
        geom.setMaterial(material);
        return geom;
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
        }

        return referencePoint;
    }
}
