package io.github.jmecn.tiled.factory;

import com.jme3.scene.Geometry;
import io.github.jmecn.tiled.core.Tile;

/**
 * @author yanmaoyuan
 */
public interface SpriteFactory {

    Geometry getTileSprite(Tile tile);
}