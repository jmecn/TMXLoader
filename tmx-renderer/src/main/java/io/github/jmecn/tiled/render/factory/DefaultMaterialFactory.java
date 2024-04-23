package io.github.jmecn.tiled.render.factory;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector4f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.texture.Texture;
import io.github.jmecn.tiled.core.Tile;
import io.github.jmecn.tiled.core.TiledImage;
import io.github.jmecn.tiled.core.Tileset;
import io.github.jmecn.tiled.render.MaterialConst;
import io.github.jmecn.tiled.util.ColorUtil;

import static io.github.jmecn.tiled.render.MaterialConst.*;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public class DefaultMaterialFactory implements MaterialFactory {

    private final AssetManager assetManager;

    public DefaultMaterialFactory(AssetManager assetManager) {
        this.assetManager = assetManager;
    }

    @Override
    public Material newMaterial(Tileset tileset) {
        TiledImage image = tileset.getImage();
        Material material = newMaterial(image);

        int tileWidth = tileset.getTileWidth();
        int tileHeight = tileset.getTileHeight();
        int tileMargin = tileset.getMargin();
        int tileSpacing = tileset.getSpacing();

        material.setBoolean(USE_TILESET_IMAGE, true);
        material.setVector4(TILE_SIZE, new Vector4f(tileWidth, tileHeight, tileMargin, tileSpacing));

        return material;
    }

    @Override
    public Material newMaterial(Tile tile) {
        Tileset tileset = tile.getTileset();
        if (tileset != null) {
            if (tileset.isImageBased()) {
                // this tile comes from a collection of images.
                return newMaterial(tileset);
            } else {
                // this tile comes from an imaged based tileset.
                Material material = newMaterial(tile.getImage());
                material.setBoolean(USE_TILESET_IMAGE, true);
                material.setVector4(TILE_SIZE, new Vector4f(tile.getWidth(), tile.getHeight(), 0f, 0f));
                return newMaterial(tile);
            }
        } else {
            if (tile.getImage() != null) {
                Material material = newMaterial(tile.getImage());
                material.setBoolean(USE_TILESET_IMAGE, true);
                material.setVector4(TILE_SIZE, new Vector4f(tile.getWidth(), tile.getHeight(), 0f, 0f));
                return material;
            } else {
                throw new IllegalArgumentException("Tileset or Image is required!");
            }
        }
    }

    @Override
    public Material newMaterial(TiledImage image) {
        Texture texture = image.getTexture();

        // create material
        Material mat = new Material(assetManager, TILED_J3MD);
        mat.setTexture(COLOR_MAP, texture);
        if (image.getTrans() != null) {
            ColorRGBA transparentColor = ColorUtil.toColorRGBA(image.getTrans());
            mat.setColor(TRANS_COLOR, transparentColor);
        }
        mat.setVector2(IMAGE_SIZE, new Vector2f(image.getWidth(), image.getHeight()));

        return mat;
    }

    @Override
    public Material newMaterial(ColorRGBA color) {
        Material mat = new Material(assetManager, TILED_J3MD);
        mat.setColor(COLOR, color);
        return mat;
    }

    @Override
    public void setTintColor(Material material, ColorRGBA tintColor) {
        if (tintColor != null) {
            material.setBoolean(USE_TINT_COLOR, true);
            material.setColor(TINT_COLOR, tintColor);
        } else {
            material.setBoolean(USE_TINT_COLOR, false);
        }
    }

    @Override
    public void setTintColor(Spatial spatial, ColorRGBA tintColor) {
        if (spatial instanceof Geometry) {
            Geometry geometry = (Geometry) spatial;
            setTintColor(geometry.getMaterial(), tintColor);
        } else {
            Node node = (Node) spatial;
            for (Spatial child : node.getChildren()) {
                if (child instanceof Geometry) {
                    Geometry geometry = (Geometry) child;
                    setTintColor(geometry.getMaterial(), tintColor);
                }
            }
        }
    }
}
