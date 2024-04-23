package io.github.jmecn.tiled.core;

import io.github.jmecn.tiled.math2d.Point;

/**
 * This is currently added only for infinite maps. The contents of a chunk element
 * is same as that of the data element, except it stores the data of the area
 * specified in the attributes.
 *
 * @author yanmaoyuan
 */
public class Chunk implements TileContainer {
    /**
     * The x coordinate of the chunk in tiles.
     */
    private final int x;
    /**
     * The y coordinate of the chunk in tiles.
     */
    private final int y;
    /**
     * The width of the chunk in tiles.
     */
    private final int width;
    /**
     * The height of the chunk in tiles.
     */
    private final int height;

    /**
     * The data stored in the chunk. Format is the same as data.
     */
    private final Tile[][] map;
    private final boolean[][] needUpdateSpatial;

    private boolean isNeedUpdate;

    public Chunk(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.map = new Tile[height][width];
        this.needUpdateSpatial = new boolean[height][width];
        this.isNeedUpdate = true;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    /**
     * Removes any occurences of the given tile from this map layer. If layer is
     * locked, an exception is thrown.
     *
     * @param tile
     *            the Tile to be removed
     */
    public void removeTile(Tile tile) {
        for (int row = 0; row < this.height; row++) {
            for (int col = 0; col < this.width; col++) {
                if (map[row][col] == tile) {
                    setTileAt(col + this.x, row + this.y, null);
                }
            }
        }
    }

    /**
     * Returns whether the given tile coordinates fall within the map
     * boundaries.
     *
     * @param tx
     *            The tile-space x-coordinate
     * @param ty
     *            The tile-space y-coordinate
     * @return <code>true</code> if the point is within the map boundaries,
     *         <code>false</code> otherwise
     */
    public boolean contains(int tx, int ty) {
        return tx >= x && ty >= y && tx < x + width && ty < y + height;
    }

    /**
     * Sets the tile at the specified position. Does nothing if (tx, ty) falls
     * outside of this layer.
     *
     * @param tx
     *            x position of tile
     * @param ty
     *            y position of tile
     * @param ti
     *            the tile object to place
     */
    @Override
    public void setTileAt(int tx, int ty, Tile ti) {
        if (contains(tx, ty)) {
            map[ty - y][tx - x] = ti;
            needUpdateSpatial[ty - y][tx - x] = true;

            // tell map renderer to update it
            setNeedUpdated(true);
        }
    }

    /**
     * Returns the tile at the specified position.
     *
     * @param tx
     *            Tile-space x coordinate
     * @param ty
     *            Tile-space y coordinate
     * @return tile at position (tx, ty) or <code>null</code> when (tx, ty) is
     *         outside this layer
     */
    @Override
    public Tile getTileAt(int tx, int ty) {
        return (contains(tx, ty)) ? map[ty - y][tx - x] : null;
    }

    /**
     * Returns the first occurrence (using top down, left to right search) of
     * the given tile.
     *
     * @param t
     *            the {@link io.github.jmecn.tiled.core.Tile} to look for
     * @return A {@link com.jme3.math.Vector2f} instance of the first instance
     *         of t, or <code>null</code> if it is not found
     */
    public Point locationOf(Tile t) {
        for (int row = this.y; row < this.height + this.y; row++) {
            for (int col = this.x; col < this.width + this.x; col++) {
                if (getTileAt(col, row) == t) {
                    return new Point(col, row);
                }
            }
        }
        return null;
    }

    /**
     * Replaces all occurrences of the Tile <code>find</code> with the Tile
     * <code>replace</code> in the entire layer
     *
     * @param find
     *            the tile to replace
     * @param replace
     *            the replacement tile
     */
    public void replaceTile(Tile find, Tile replace) {
        for (int row = this.y; row < this.y + this.height; row++) {
            for (int col = this.x; col < this.x + this.width; col++) {
                if (getTileAt(col, row) == find) {
                    setTileAt(col, row, replace);
                }
            }
        }
    }

    /**
     * Tell if the spatial at position(tx, ty) should be updated.
     *
     * @param tx
     *            Tile-space x coordinate
     * @param ty
     *            Tile-space y coordinate
     *
     * @return true if the spatial should be updated.
     */
    public boolean isNeedUpdateAt(int tx, int ty) {
        return contains(tx, ty) && needUpdateSpatial[ty - y][tx - x];
    }

    /**
     * Set the needUpdate flag at position(tx, ty).
     * @param tx Tile-space x coordinate
     * @param ty Tile-space y coordinate
     * @param needUpdate true if the spatial should be updated.
     */
    public void setNeedUpdateAt(int tx, int ty, boolean needUpdate) {
        if (contains(tx, ty)) {
            needUpdateSpatial[ty - y][tx - x] = needUpdate;
        }
    }

    /**
     * @return true if the spatial should be updated.
     */
    public boolean isNeedUpdated() {
        return isNeedUpdate;
    }

    /**
     * Set the needUpdate flag.
     * @param isSpatialUpdated true if the spatial should be updated.
     */
    public void setNeedUpdated(boolean isSpatialUpdated) {
        this.isNeedUpdate = isSpatialUpdated;
    }

}