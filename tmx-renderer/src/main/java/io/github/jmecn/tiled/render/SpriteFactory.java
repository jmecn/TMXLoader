package io.github.jmecn.tiled.render;

import com.jme3.math.Vector2f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.util.IntMap;
import io.github.jmecn.tiled.animation.AnimatedTileControl;
import io.github.jmecn.tiled.core.Tile;
import io.github.jmecn.tiled.core.TiledMap;
import io.github.jmecn.tiled.core.Tileset;
import io.github.jmecn.tiled.math2d.Point;
import io.github.jmecn.tiled.shape.TileMesh;

import java.util.List;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public final class SpriteFactory {
    private final TiledMap map;
    private final List<Tileset> tilesets;

    private final IntMap<Geometry> tileSprites;

    public SpriteFactory(TiledMap map) {
        this.map = map;
        this.tilesets = map.getTileSets();
        this.tileSprites = new IntMap<>();

        // create the visual part for the map
        for (Tileset tileset : tilesets) {
            createVisual(tileset, map);
        }
    }

    /**
     * Create the visual part for every tile of a given Tileset.
     *
     * @param tileset the Tileset
     * @param map the TiledMap
     */
    public void createVisual(Tileset tileset, TiledMap map) {

        Point tileOffset = tileset.getTileOffset();
        Vector2f offset = new Vector2f(tileOffset.getX(), tileOffset.getY());
        Vector2f origin = new Vector2f(0, map.getTileHeight());

        List<Tile> tiles = tileset.getTiles();
        for (Tile tile : tiles) {
            String name = "tile#" + tileset.getFirstGid() + "#" + tile.getId();

            Vector2f coord = new Vector2f(tile.getX(), tile.getY());
            Vector2f size = new Vector2f(tile.getWidth(), tile.getHeight());
            TileMesh mesh = new TileMesh(coord, size, offset, origin);

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
        Point tileOffset = tileset.getTileOffset();
        Vector2f offset = new Vector2f(tileOffset.getX(), tileOffset.getY());
        Vector2f origin = new Vector2f(0, map.getTileHeight());

        String name = "tile#" + tileset.getFirstGid() + "#" + tile.getId();

        Vector2f coord = new Vector2f(tile.getX(), tile.getY());
        Vector2f size = new Vector2f(tile.getWidth(), tile.getHeight());
        TileMesh mesh = new TileMesh(coord, size, offset, origin);

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

    public Geometry getTileSprite(Tile tile) {
        if (tileSprites.containsKey(tile.getGid())) {
            return tileSprites.get(tile.getGid());
        }

        Geometry sprite = createSprite(tile);
        tileSprites.put(tile.getGid(), sprite);
        return sprite;
    }
}
