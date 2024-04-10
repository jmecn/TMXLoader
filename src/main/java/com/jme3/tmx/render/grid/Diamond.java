package com.jme3.tmx.render.grid;

import com.jme3.math.Vector2f;
import com.jme3.scene.Mesh;

import java.util.ArrayList;
import java.util.List;

/**
 * This is the basic grid shape in isometric map.
 *
 * @author yanmaoyuan
 */
public class Diamond extends Mesh {

    public Diamond(int tileWidth, int tileHeight) {
        List<Vector2f> polygon = new ArrayList<>(4);
        polygon.add(new Vector2f(0, tileHeight * 0.5f));
        polygon.add(new Vector2f(tileWidth * 0.5f, tileHeight));
        polygon.add(new Vector2f(tileWidth, tileHeight * 0.5f));
        polygon.add(new Vector2f(tileWidth * 0.5f, 0));
    }
}
