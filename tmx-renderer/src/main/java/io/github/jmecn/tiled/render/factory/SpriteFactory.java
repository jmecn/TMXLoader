package io.github.jmecn.tiled.render.factory;

import com.jme3.material.Material;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import io.github.jmecn.tiled.core.MapObject;
import io.github.jmecn.tiled.core.Tile;

/**
 * @author yanmaoyuan
 */
public interface SpriteFactory {

    Geometry newTileSprite(Tile tile);

    Geometry newTileSprite(Tile tile, Material material);

    Geometry getTileSprite(Tile tile);

    Spatial newObjectSprite(MapObject object, Material material);
}