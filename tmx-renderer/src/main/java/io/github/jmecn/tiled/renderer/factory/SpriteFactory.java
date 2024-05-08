package io.github.jmecn.tiled.renderer.factory;

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

    Material newMaterial(ColorRGBA color);

    Material newMaterial(Tileset tileset);

    Material newMaterial(Tile tile);

    Material newMaterial(TiledImage image);

    Geometry newTileSprite(Tile tile);

    Geometry newTileSprite(Tile tile, Material material);

    Spatial newObjectSprite(MapObject object, Material material);

    MaterialFactory getMaterialFactory();

    void setMaterialFactory(MaterialFactory materialFactory);

    MeshFactory getMeshFactory();

    void setMeshFactory(MeshFactory meshFactory);
}