package io.github.jmecn.tiled.render.factory;

import com.jme3.material.Material;
import com.jme3.scene.Geometry;
import io.github.jmecn.tiled.core.Tile;

/**
 * @author yanmaoyuan
 */
public interface SpriteFactory {

    Geometry newTileSprite(Tile tile);

    Geometry newTileSprite(Tile tile, Material material);

    Geometry getTileSprite(Tile tile);
}