package io.github.jmecn.tiled.render.factory;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.*;
import com.jme3.util.IntMap;
import io.github.jmecn.tiled.animation.AnimatedTileControl;
import io.github.jmecn.tiled.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public final class DefaultSpriteFactory implements SpriteFactory {

    static Logger logger = LoggerFactory.getLogger(DefaultSpriteFactory.class);

    private MeshFactory meshFactory;
    private MaterialFactory materialFactory;

    private final IntMap<Geometry> cache;

    public DefaultSpriteFactory(TiledMap tiledMap, AssetManager assetManager) {
        this(tiledMap, new DefaultMeshFactory(tiledMap), new DefaultMaterialFactory(assetManager));
    }

    public DefaultSpriteFactory(TiledMap tiledMap, MaterialFactory materialFactory) {
        this(tiledMap, new DefaultMeshFactory(tiledMap), materialFactory);
    }

    public DefaultSpriteFactory(TiledMap tiledMap, MeshFactory meshFactory, MaterialFactory materialFactory) {
        this.meshFactory = meshFactory;
        this.materialFactory = materialFactory;
        this.cache = new IntMap<>();
        List<Tileset> tilesets = tiledMap.getTileSets();

        // create the visual part for the map
        for (Tileset tileset : tilesets) {
            createVisual(tileset);// TODO remove this when I have a better animation system
        }
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

    /**
     * Create the visual part for every tile of a given Tileset.
     *
     * @param tileset the Tileset
     */
    public void createVisual(Tileset tileset) {
        List<Tile> tiles = tileset.getTiles();
        for (Tile tile : tiles) {
            Geometry sprite = getTileSprite(tile);
            tile.setVisual(sprite); // TODO remove it when I have a better animation system
        }
    }

    @Override
    public void setTintColor(Spatial spatial, ColorRGBA tintColor) {
        materialFactory.setTintColor(spatial, tintColor);
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
    public Material newMaterial(Tileset tileset, ColorRGBA tintColor) {
        return materialFactory.newMaterial(tileset, tintColor);
    }

    @Override
    public Material newMaterial(Tile tile, ColorRGBA tintColor) {
        return materialFactory.newMaterial(tile, tintColor);
    }

    @Override
    public Material newMaterial(TiledImage image, ColorRGBA tintColor) {
        return materialFactory.newMaterial(image, tintColor);
    }

    @Override
    public Material newMaterial(ColorRGBA color, ColorRGBA tintColor) {
        return materialFactory.newMaterial(color, tintColor);
    }

    @Override
    public void setTintColor(Material material, ColorRGBA tintColor) {
        materialFactory.setTintColor(material, tintColor);
    }

    @Override
    public Geometry newTileSprite(Tile tile) {
        Mesh mesh = meshFactory.getTileMesh(tile);
        Material material = materialFactory.newMaterial(tile);

        String name = "tile#" + tile.getGid();
        Geometry geometry = new Geometry(name, mesh);
        geometry.setQueueBucket(RenderQueue.Bucket.Gui);
        geometry.setMaterial(material);
        if (tile.isAnimated()) {
            geometry.addControl(new AnimatedTileControl(tile));
        }
        return geometry;
    }

    @Override
    public Geometry newTileSprite(Tile tile, Material material) {
        Mesh mesh = meshFactory.getTileMesh(tile);
        String name = "tile#" + tile.getGid();
        Geometry geometry = new Geometry(name, mesh);
        geometry.setQueueBucket(RenderQueue.Bucket.Gui);
        geometry.setMaterial(material);
        if (tile.isAnimated()) {
            geometry.addControl(new AnimatedTileControl(tile));
        }
        return geometry;
    }

    @Override
    public Geometry getTileSprite(Tile tile) {
        if (cache.containsKey(tile.getGid())) {
            return cache.get(tile.getGid());
        } else {
            Geometry sprite = newTileSprite(tile);
            cache.put(tile.getGid(), sprite);
            return sprite;
        }
    }

    public Spatial newObjectSprite(MapObject obj, Material material) {
        Mesh mesh = meshFactory.newObjectMesh(obj);
        if (mesh == null) {
            return null;
        }

        Geometry geometry = new Geometry(obj.getName(), mesh);
        geometry.setQueueBucket(RenderQueue.Bucket.Gui);

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
