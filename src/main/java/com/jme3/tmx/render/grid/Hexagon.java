package com.jme3.tmx.render.grid;

import com.jme3.math.Vector2f;
import com.jme3.tmx.enums.StaggerAxis;
import com.jme3.tmx.render.shape.Polygon;

import java.util.ArrayList;
import java.util.List;

/**
 * This is the basic grid shape in hexagonal map. The origin point is in the top-left corner.
 *
 * <pre>
 * O-/----\---X
 * |/      \
 * |\      /
 * | \____/
 * |
 * Y
 *</pre>
 * @author yanmaoyuan
 */
public class Hexagon extends Polygon {

    public Hexagon(int mapTileWidth, int mapTileHeight, int hexSideLength, StaggerAxis staggerAxis, boolean border) {
        int sideLengthX = 0;
        int sideLengthY = 0;
        boolean isStaggerX = staggerAxis == StaggerAxis.X;
        if (isStaggerX) {
            sideLengthX = hexSideLength;
        } else {
            sideLengthY = hexSideLength;
        }

        int sideOffsetX = (mapTileWidth - sideLengthX) / 2;
        int sideOffsetY = (mapTileHeight - sideLengthY) / 2;

        int columnWidth = sideOffsetX + sideLengthX;
        int rowHeight = sideOffsetY + sideLengthY;

        int tileWidth = columnWidth + sideOffsetX;
        int tileHeight = rowHeight + sideOffsetY;

        List<Vector2f> polygon = new ArrayList<>(6);
        polygon.add(new Vector2f(0, rowHeight));
        if (!isStaggerX) {
            polygon.add(new Vector2f(0, sideOffsetY));
        }
        polygon.add(new Vector2f(sideOffsetX, 0));
        if (isStaggerX) {
            polygon.add(new Vector2f(columnWidth, 0));
        }
        polygon.add(new Vector2f(tileWidth, sideOffsetY));
        if (!isStaggerX) {
            polygon.add(new Vector2f(tileWidth, rowHeight));
        }
        polygon.add(new Vector2f(columnWidth, tileHeight));
        if (isStaggerX) {
            polygon.add(new Vector2f(0, tileHeight));
        }

        if (border) {
            polyline(polygon, true);
        } else {
            fill(polygon);
        }
    }
}