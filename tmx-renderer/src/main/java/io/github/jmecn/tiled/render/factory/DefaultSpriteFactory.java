package io.github.jmecn.tiled.render.factory;

import com.jme3.material.Material;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.util.IntMap;
import io.github.jmecn.tiled.animation.AnimatedTileControl;
import io.github.jmecn.tiled.core.Tile;
import io.github.jmecn.tiled.core.TiledMap;
import io.github.jmecn.tiled.core.Tileset;
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

    private final MeshFactory meshFactory;

    private final IntMap<Geometry> cache;

    public DefaultSpriteFactory(TiledMap tiledMap) {
        this(tiledMap, new DefaultMeshFactory(tiledMap));
    }

    public DefaultSpriteFactory(TiledMap tiledMap, MeshFactory meshFactory) {
        this.meshFactory = meshFactory;
        this.cache = new IntMap<>();
        List<Tileset> tilesets = tiledMap.getTileSets();

        // create the visual part for the map
        for (Tileset tileset : tilesets) {
            createVisual(tileset);// TODO remove this when I have a better animation system
        }
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
    public Geometry newTileSprite(Tile tile) {
        Mesh mesh = meshFactory.getTileMesh(tile);
        Material material = getTileMaterial(tile);

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
            logger.debug("Reuse tile sprite:{}, total:{}", tile.getGid(), cache.size());
            return cache.get(tile.getGid());
        } else {
            Geometry sprite = newTileSprite(tile);
            cache.put(tile.getGid(), sprite);
            logger.debug("Create tile sprite:{}, total:{}", tile.getGid(), cache.size());
            return sprite;
        }
    }

    public Material getTileMaterial(Tile tile) {
        // TODO make a better material management
        Material material;
        if (tile.getMaterial() != null) {
            material = tile.getMaterial();
        } else {
            material = tile.getTileset().getMaterial();
        }
        return material;
    }
}
