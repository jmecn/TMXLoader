package io.github.jmecn.tiled.factory;

import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Spatial;
import com.jme3.util.IntMap;
import io.github.jmecn.tiled.animation.AnimatedTileControl;
import io.github.jmecn.tiled.core.Tile;
import io.github.jmecn.tiled.core.TiledMap;
import io.github.jmecn.tiled.core.Tileset;

import java.util.List;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public final class DefaultSpriteFactory implements SpriteFactory {

    private final MeshFactory meshFactory;

    private final IntMap<Geometry> tileSprites;

    public DefaultSpriteFactory(TiledMap tiledMap) {
        this(tiledMap, new DefaultMeshFactory(tiledMap));
    }

    public DefaultSpriteFactory(TiledMap tiledMap, MeshFactory meshFactory) {
        this.meshFactory = meshFactory;
        this.tileSprites = new IntMap<>();
        List<Tileset> tilesets = tiledMap.getTileSets();

        // create the visual part for the map
        for (Tileset tileset : tilesets) {
            createVisual(tileset);
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
            String name = "tile#" + tileset.getFirstGid() + "#" + tile.getId();

            Mesh mesh = meshFactory.getTileMesh(tile);

            Geometry geometry = new Geometry(name, mesh);
            geometry.setQueueBucket(RenderQueue.Bucket.Gui);

            if (tile.getMaterial() != null) {
                geometry.setMaterial(tile.getMaterial());
            } else {
                geometry.setMaterial(tileset.getMaterial());
            }

            if (tile.isAnimated()) {
                geometry.setBatchHint(Spatial.BatchHint.Never);
                AnimatedTileControl control = new AnimatedTileControl(tile);
                geometry.addControl(control);
            }

            tileSprites.put(tile.getGid(), geometry);
            tile.setVisual(geometry); // TODO remove it when I have a better animation system
        }
    }

    private Geometry createSprite(Tile tile) {
        Tileset tileset = tile.getTileset();

        String name = "tile#" + tile.getGid();

        Mesh mesh = meshFactory.getTileMesh(tile);

        Geometry geometry = new Geometry(name, mesh);
        geometry.setQueueBucket(RenderQueue.Bucket.Gui);

        if (tile.getMaterial() != null) {
            geometry.setMaterial(tile.getMaterial());
        } else {
            geometry.setMaterial(tileset.getMaterial());
        }

        if (tile.isAnimated()) {
            geometry.setBatchHint(Spatial.BatchHint.Never);
            AnimatedTileControl control = new AnimatedTileControl(tile);
            geometry.addControl(control);
        }

        return geometry;
    }

    @Override
    public Geometry getTileSprite(Tile tile) {
        if (tileSprites.containsKey(tile.getGid())) {
            return tileSprites.get(tile.getGid());
        }

        Geometry sprite = createSprite(tile);
        tileSprites.put(tile.getGid(), sprite);
        return sprite;
    }
}
