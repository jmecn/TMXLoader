package io.github.jmecn.tiled.renderer.factory;

import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Spatial;
import io.github.jmecn.tiled.core.Tile;
import io.github.jmecn.tiled.core.TiledImage;
import io.github.jmecn.tiled.core.Tileset;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public interface MaterialFactory {

    Material newMaterial(Tile tile);

    Material newMaterial(Tileset tileset);

    Material newMaterial(TiledImage image);

    Material newMaterial(ColorRGBA color);

    Material newMaterial(Tile tile, ColorRGBA tintColor);

    Material newMaterial(Tileset tileset, ColorRGBA tintColor);

    Material newMaterial(TiledImage image, ColorRGBA tintColor);

    Material newMaterial(ColorRGBA color, ColorRGBA tintColor);

    void setTintColor(Material material, ColorRGBA tintColor);

    void setTintColor(Spatial spatial, ColorRGBA tintColor);

    void setLayerOpacity(Material material, float opacity);

    void setLayerOpacity(Spatial spatial, float opacity);

    void setOpacity(Material material, float opacity);

    void setOpacity(Spatial spatial, float opacity);
}