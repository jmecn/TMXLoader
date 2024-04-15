package io.github.jmecn.tiled.render.grid;

import com.jme3.math.Vector2f;
import com.jme3.scene.Mesh;
import io.github.jmecn.tiled.render.shape.Polygon;
import io.github.jmecn.tiled.render.shape.Polyline;

import java.util.ArrayList;
import java.util.List;

/**
 * This is the basic grid shape in isometric map.
 *
 * @author yanmaoyuan
 */
public class Diamond extends Polygon {

    public Diamond(int tileWidth, int tileHeight, boolean border) {
        List<Vector2f> polygon = new ArrayList<>(4);
        polygon.add(new Vector2f(-tileWidth * 0.5f, tileHeight * 0.5f));
        polygon.add(new Vector2f(0f, tileHeight));
        polygon.add(new Vector2f(tileWidth * 0.5f, tileHeight * 0.5f));
        polygon.add(new Vector2f(0f, 0));
        if (border) {
            super.polyline(polygon, true);
        } else {
            super.fill(polygon);
        }
    }
}
