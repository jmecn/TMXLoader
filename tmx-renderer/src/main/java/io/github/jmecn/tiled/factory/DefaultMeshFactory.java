package io.github.jmecn.tiled.factory;

import com.jme3.math.Vector2f;
import com.jme3.scene.Mesh;
import com.jme3.util.IntMap;
import io.github.jmecn.tiled.core.MapObject;
import io.github.jmecn.tiled.core.Tile;
import io.github.jmecn.tiled.core.TiledMap;
import io.github.jmecn.tiled.core.Tileset;
import io.github.jmecn.tiled.shape.TileMesh;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public class DefaultMeshFactory implements MeshFactory {

    private final TiledMap tiledMap;

    private final IntMap<TileMesh> tileMeshes;

    public DefaultMeshFactory(TiledMap tiledMap) {
        this.tiledMap = tiledMap;
        this.tileMeshes = new IntMap<>();
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
        if (tileMeshes.containsKey(tileId)) {
            return tileMeshes.get(tileId);
        }

        TileMesh mesh = newTileMesh(tileId);
        tileMeshes.put(tileId, mesh);
        return mesh;
    }

    @Override
    public TileMesh getTileMesh(Tile tile) {
        if (tileMeshes.containsKey(tile.getGid())) {
            return tileMeshes.get(tile.getGid());
        }

        TileMesh mesh = newTileMesh(tile);
        tileMeshes.put(tile.getGid(), mesh);
        return mesh;
    }


    @Override
    public Mesh createMesh(MapObject object) {
        return null;
    }
}
