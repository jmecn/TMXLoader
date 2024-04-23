package io.github.jmecn.tiled.render.factory;

import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import io.github.jmecn.tiled.core.Tile;
import io.github.jmecn.tiled.core.TiledImage;
import io.github.jmecn.tiled.core.Tileset;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public interface MaterialFactory {

    Material newMaterial(Tileset tileset);

    Material newMaterial(Tile tile);

    Material newMaterial(TiledImage image);

    Material newMaterial(ColorRGBA color);
}