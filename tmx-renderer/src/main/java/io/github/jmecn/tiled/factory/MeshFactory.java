package io.github.jmecn.tiled.factory;

import com.jme3.scene.Mesh;
import io.github.jmecn.tiled.core.MapObject;
import io.github.jmecn.tiled.core.Tile;

/**
 * Mesh factory for creating tile mesh and object mesh.
 *
 * @author yanmaoyuan
 */
public interface MeshFactory {

    /**
     * Create a new mesh for a tile by global id. The mesh will be flipped if necessary.
     * @param tileId global id. The flag bits will be used.
     * @return the mesh
     */
    Mesh newTileMesh(int tileId);

    /**
     * Create a new mesh for a tile. The mesh will be flipped if necessary.
     * @param tile the tile. The tile.getGid() flag bits will be used.
     * @return the mesh
     */
    Mesh newTileMesh(Tile tile);

    /**
     * Get the mesh for a tile by global id. The mesh will be cached by its gid once created.
     * @param tileId global id with flag bits.
     * @return the mesh
     */
    Mesh getTileMesh(int tileId);

    /**
     * Get the mesh for a tile. The mesh will be cached by its gid once created.
     * @param tile the tile. The tile.getGid() flag bits will be used.
     * @return the mesh
     */
    Mesh getTileMesh(Tile tile);

    Mesh createMesh(MapObject object);
}