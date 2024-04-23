package io.github.jmecn.tiled.render.factory;

import com.jme3.scene.Geometry;
import io.github.jmecn.tiled.core.Tile;

/**
 * @author yanmaoyuan
 */
public interface SpriteFactory {

    Geometry newTileSprite(Tile tile);

    Geometry getTileSprite(Tile tile);

    Geometry copyTileSprite(Tile tile);
}