package io.github.jmecn.tiled.render.factory;

import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import io.github.jmecn.tiled.core.MapObject;
import io.github.jmecn.tiled.core.Tile;
import io.github.jmecn.tiled.core.TiledImage;
import io.github.jmecn.tiled.core.Tileset;

/**
 * @author yanmaoyuan
 */
public interface SpriteFactory {

    void setTintColor(Material material, ColorRGBA tintColor);

    void setTintColor(Spatial spatial, ColorRGBA tintColor);

    Material newMaterial(ColorRGBA color);

    Material newMaterial(Tileset tileset);

    Material newMaterial(Tile tile);

    Material newMaterial(TiledImage image);

    Material newMaterial(Tileset tileset, ColorRGBA tintColor);

    Material newMaterial(Tile tile, ColorRGBA tintColor);

    Material newMaterial(TiledImage image, ColorRGBA tintColor);

    Material newMaterial(ColorRGBA color, ColorRGBA tintColor);

    Geometry newTileSprite(Tile tile);

    Geometry newTileSprite(Tile tile, Material material);

    Geometry getTileSprite(Tile tile);

    Spatial newObjectSprite(MapObject object, Material material);

}