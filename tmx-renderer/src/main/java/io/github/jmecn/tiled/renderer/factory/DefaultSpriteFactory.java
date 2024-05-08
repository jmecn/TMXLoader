package io.github.jmecn.tiled.renderer.factory;

import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.scene.*;
import io.github.jmecn.tiled.animation.AnimatedTileControl;
import io.github.jmecn.tiled.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public final class DefaultSpriteFactory implements SpriteFactory {

    static Logger logger = LoggerFactory.getLogger(DefaultSpriteFactory.class);

    private MeshFactory meshFactory;
    private MaterialFactory materialFactory;

    public DefaultSpriteFactory() {
        this(null, null);
    }

    public DefaultSpriteFactory(MeshFactory meshFactory, MaterialFactory materialFactory) {
        this.meshFactory = meshFactory;
        this.materialFactory = materialFactory;
    }

    /**
     * Get the MaterialFactory for creating tile material.
     * @return the MaterialFactory
     */
    public MaterialFactory getMaterialFactory() {
        return materialFactory;
    }

    /**
     * Set the MaterialFactory for creating tile material.
     * @param materialFactory the MaterialFactory
     */
    public void setMaterialFactory(MaterialFactory materialFactory) {
        this.materialFactory = materialFactory;
    }

    /**
     * Get the MeshFactory for creating tile mesh.
     * @return the MeshFactory
     */
    public MeshFactory getMeshFactory() {
        return meshFactory;
    }

    /**
     * Set the MeshFactory for creating tile mesh.
     * @param meshFactory the MeshFactory
     */
    public void setMeshFactory(MeshFactory meshFactory) {
        this.meshFactory = meshFactory;
    }

    @Override
    public Material newMaterial(ColorRGBA color) {
        return materialFactory.newMaterial(color);
    }

    @Override
    public Material newMaterial(Tileset tileset) {
        return materialFactory.newMaterial(tileset);
    }

    @Override
    public Material newMaterial(Tile tile) {
        return materialFactory.newMaterial(tile);
    }

    @Override
    public Material newMaterial(TiledImage image) {
        return materialFactory.newMaterial(image);
    }

    @Override
    public Geometry newTileSprite(Tile tile) {
        Material material = materialFactory.newMaterial(tile);
        return newTileSprite(tile, material);
    }

    @Override
    public Geometry newTileSprite(Tile tile, Material material) {
        Mesh mesh = meshFactory.getTileMesh(tile);
        String name = "tile#" + tile.getGid();
        Geometry geometry = new Geometry(name, mesh);
        geometry.setMaterial(material);
        if (tile.isAnimated()) {
            geometry.addControl(new AnimatedTileControl(tile));
        }
        return geometry;
    }

    public Spatial newObjectSprite(MapObject obj, Material material) {
        Mesh mesh = meshFactory.newObjectMesh(obj);
        if (mesh == null) {
            return null;
        }

        Geometry geometry = new Geometry("Obj:" + obj.getName() + "#" + obj.getId(), mesh);

        double deg = obj.getRotation();
        if (deg != 0) {
            float radian = (float) (FastMath.DEG_TO_RAD * deg);
            // rotate the spatial clockwise
            geometry.rotate(0, -radian, 0);
        }

        switch (obj.getShape()) {
            case IMAGE: {
                Material mat = materialFactory.newMaterial(obj.getImage());
                geometry.setMaterial(mat);
                break;
            }
            case TILE: {
                Material mat =  materialFactory.newMaterial(obj.getTile());
                geometry.setMaterial(mat);
                if (obj.getTile().isAnimated()) {
                    geometry.addControl(new AnimatedTileControl(obj.getTile()));
                }
                break;
            }
            case TEXT: {
                // TODO not supported yet
                geometry.setMaterial(material);
                break;
            }
            default: {
                geometry.setMaterial(material);
                break;
            }
        }

        return geometry;
    }

    private Geometry text(MapObject obj) {
        // TODO render text
        ObjectText objectText = obj.getTextData();
        return null;
    }

}
