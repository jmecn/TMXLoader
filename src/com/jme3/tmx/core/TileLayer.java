package com.jme3.tmx.core;

import java.util.HashMap;
import java.util.Properties;

import com.jme3.tmx.math2d.Point;

/**
 * A TileLayer is a specialized MapLayer, used for tracking two dimensional tile
 * data.
 * 
 * @author yanmaoyuan
 */
public class TileLayer extends Layer {

	private Tile[][] map;
	
    protected HashMap<Object, Properties> tileInstanceProperties = new HashMap<>();

    /**
     * <p>getTileInstancePropertiesAt.</p>
     *
     * @param x a int.
     * @param y a int.
     * @return a {@link java.util.Properties} object.
     */
    public Properties getTileInstancePropertiesAt(int x, int y) {
        if (!contains(x, y)) {
            return null;
        }
        Object key = new Point(x, y);
        return tileInstanceProperties.get(key);
    }

    /**
     * <p>setTileInstancePropertiesAt.</p>
     *
     * @param x a int.
     * @param y a int.
     * @param tip a {@link java.util.Properties} object.
     */
    public void setTileInstancePropertiesAt(int x, int y, Properties tip) {
        if (contains(x, y)) {
            Object key = new Point(x, y);
            tileInstanceProperties.put(key, tip);
        }
    }
    
	/**
     * Default constructor.
     */
	public TileLayer() {
	}
	
    /**
     * Construct a TileLayer from the given width and height.
     *
     * @param w width in tiles
     * @param h height in tiles
     */
    public TileLayer(int w, int h) {
        super(w, h);
        
        map = new Tile[height][width];
    }
    
    /**
     * @param m the map this layer is part of
     */
    public TileLayer(TiledMap m) {
        setMap(m);
    }

    /**
     * <p>Constructor for TileLayer.</p>
     *
     * @param m the map this layer is part of
     * @param w width in tiles
     * @param h height in tiles
     */
    public TileLayer(TiledMap m, int w, int h) {
        super(w, h);
        setMap(m);
    }
    
	/**
	 * Returns whether the given tile coordinates fall within the map
	 * boundaries.
	 * 
	 * @param x
	 *            The tile-space x-coordinate
	 * @param y
	 *            The tile-space y-coordinate
	 * @return <code>true</code> if the point is within the map boundaries,
	 *         <code>false</code> otherwise
	 */
	public boolean contains(int x, int y) {
		return x >= 0 && y >= 0 && x < width && y < height;
	}
	
    /**
     * Removes any occurences of the given tile from this map layer. If layer is
     * locked, an exception is thrown.
     *
     * @param tile the Tile to be removed
     */
    public void removeTile(Tile tile) {
        for (int y = 0; y < this.height; y++) {
            for (int x = 0; x < this.width; x++) {
                if (map[y][x] == tile) {
                    setTileAt(x + this.x, y + this.y, null);
                }
            }
        }
    }

    /**
     * Sets the tile at the specified position. Does nothing if (tx, ty) falls
     * outside of this layer.
     *
     * @param tx x position of tile
     * @param ty y position of tile
     * @param ti the tile object to place
     */
    public void setTileAt(int tx, int ty, Tile ti) {
        if (this.contains(tx, ty)) {
            map[ty - this.y][tx - this.x] = ti;
        }
    }

    /**
     * Returns the tile at the specified position.
     *
     * @param tx Tile-space x coordinate
     * @param ty Tile-space y coordinate
     * @return tile at position (tx, ty) or <code>null</code> when (tx, ty) is
     * outside this layer
     */
    public Tile getTileAt(int tx, int ty) {
        return (contains(tx, ty))
                ? map[ty - this.y][tx - this.x] : null;
    }

    /**
     * Returns the first occurrence (using top down, left to right search) of
     * the given tile.
     *
     * @param t the {@link com.jme3.tmx.core.Tile} to look for
     * @return A {@link com.jme3.math.Vector2f} instance of the first instance of t, or
     * <code>null</code> if it is not found
     */
    public Point locationOf(Tile t) {
        for (int y = this.y; y < this.height + this.y; y++) {
            for (int x = this.x; x < this.width + this.x; x++) {
                if (getTileAt(x, y) == t) {
                    return new Point(x, y);
                }
            }
        }
        return null;
    }

    /**
     * Replaces all occurrences of the Tile <code>find</code> with the Tile
     * <code>replace</code> in the entire layer
     *
     * @param find the tile to replace
     * @param replace the replacement tile
     */
    public void replaceTile(Tile find, Tile replace) {
        for (int y = this.y; y < this.y + this.height; y++) {
            for (int x = this.x; x < this.x + this.width; x++) {
                if (getTileAt(x, y) == find) {
                    setTileAt(x, y, replace);
                }
            }
        }
    }
}
