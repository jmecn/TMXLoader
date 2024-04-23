package io.github.jmecn.tiled.render.factory;

import com.jme3.math.Vector2f;
import com.jme3.scene.Mesh;
import com.jme3.util.IntMap;
import io.github.jmecn.tiled.core.MapObject;
import io.github.jmecn.tiled.core.Tile;
import io.github.jmecn.tiled.core.TiledMap;
import io.github.jmecn.tiled.core.Tileset;
import io.github.jmecn.tiled.render.shape.TileMesh;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public class DefaultMeshFactory implements MeshFactory {

    static Logger logger = LoggerFactory.getLogger(DefaultMeshFactory.class);

    private final TiledMap tiledMap;

    private final IntMap<TileMesh> cache;

    public DefaultMeshFactory(TiledMap tiledMap) {
        this.tiledMap = tiledMap;
        this.cache = new IntMap<>();
    }

    @Override
    public TileMesh newTileMesh(int tileId) {
        // clear the flag
        int gid = tileId & ~Tile.FLIPPED_MASK;

        Tile tile = tiledMap.getTileForTileGID(gid);
        if (tile == null) {
            throw new IllegalArgumentException("Tile not found, id: " + tileId);
        }

        if (tile.getGid() != tileId) {
            Tile t = tile.copy();
            t.setGid(tileId);
            tile = t;
        }

        return newTileMesh(tile);
    }

    @Override
    public TileMesh newTileMesh(Tile tile) {
        Tileset tileset = tile.getTileset();
        Vector2f offset = tileset.getTileOffset();
        Vector2f origin = new Vector2f(0, tiledMap.getTileHeight());

        Vector2f coord = new Vector2f(tile.getX(), tile.getY());
        Vector2f size = new Vector2f(tile.getWidth(), tile.getHeight());

        return new TileMesh(coord, size, offset, origin, tile.getGid(), tiledMap.getOrientation());
    }

    @Override
    public TileMesh getTileMesh(int tileId) {
        if (cache.containsKey(tileId)) {
            return cache.get(tileId);
        }

        TileMesh mesh = newTileMesh(tileId);
        cache.put(tileId, mesh);
        return mesh;
    }

    @Override
    public TileMesh getTileMesh(Tile tile) {
        if (cache.containsKey(tile.getGid())) {
            logger.debug("reuse mesh for gid: {}, cacheSize:{}", tile.getGid(), cache.size());
            return cache.get(tile.getGid());
        } else {
            TileMesh mesh = newTileMesh(tile);
            cache.put(tile.getGid(), mesh);
            logger.debug("create mesh for gid:{}, cacheSize:{}", tile.getGid(), cache.size());
            return mesh;
        }
    }

    @Override
    public Mesh createMesh(MapObject object) {
        return null;
    }
}
