/*-
 * #%L
 * This file is part of libtiled-java.
 * %%
 * Copyright (C) 2004 - 2016 Thorbj?rn Lindeijer <thorbjorn@lindeijer.nl>
 * Copyright (C) 2004 - 2016 Adam Turk <aturk@biggeruniverse.com>
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package tiled.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.texture.Texture;
import com.jme3.texture.image.ImageRaster;

/**
 * todo: Update documentation
 * <p>
 * TileSet handles operations on tiles as a set, or group. It has several
 * advanced internal functions aimed at reducing unnecessary data replication. A
 * 'tile' is represented internally as two distinct pieces of data. The first
 * and most important is a {@link tiled.core.Tile} object, and these are held in
 * a {@link java.util.Vector}.
 * </p>
 * 
 * <p>
 * The other is the tile image.
 * </p>
 * 
 * @author Thorbj?rn Lindeijer
 * @author Adam Turk
 * @version 0.17
 */
public class TileSet implements Iterable<Tile> {

	private String base;
	final private List<Tile> tiles = new ArrayList<>();
	private Vector2f tileDimensions;
	private int tileWidth;
	private int tileHeight;
	private int tileSpacing;
	private int tileMargin;
	private String externalSource;
	private String name;
	private ColorRGBA transparentColor;
	private Texture tileSetTexture;

    
	/**
	 * Default constructor
	 */
	public TileSet() {
		tileDimensions = new Vector2f();
	}
	
	public TileSet(int width, int height, int space, int margin) {
		this.tileWidth = width;
		this.tileHeight = height;
		this.tileSpacing = space;
		this.tileMargin = margin;
		this.tileDimensions = new Vector2f(tileWidth, tileHeight);
	}

	/**
	 * Creates a tileset from a Texture2D. Tiles are cut by the passed cutter.
	 * 
	 * @param texture
	 *            the image to be used, must not be null
	 * @param cutter
	 *            the tile cutter, must not be null
	 */
	public void setTileSetTexture(Texture texture) {
		assert texture != null;

		this.tileSetTexture = texture;

		if (transparentColor != null) {
			trans(texture, transparentColor);
		}

		reset();
		
		Tile tile = getNextTile();
		while (tile != null) {
			tile.setTexture(tileSetTexture);
			addNewTile(tile);
			tile = getNextTile();
		}

	}
    
	/**
	 * This method is used for filtering out a given "transparent" color from an
	 * image. Sometimes known as magic pink.
	 * 
	 * @param tex
	 * @param transColor
	 */
	private void trans(final Texture tex, final ColorRGBA transColor) {
		com.jme3.texture.Image img = tex.getImage();
		ImageRaster raster = ImageRaster.create(img);

		ColorRGBA store = new ColorRGBA();
		int width = img.getWidth();
		int height = img.getHeight();
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				raster.getPixel(x, y, store);
				if (store.r == transColor.r && store.g == transColor.g
						&& store.b == transColor.b) {
					store.set(0, 0, 0, 0);
					raster.setPixel(x, y, store);
				}
			}
		}
	}

	/**
	 * Sets the URI path of the external source of this tile set. By setting
	 * this, the set is implied to be external in all other operations.
	 * 
	 * @param source
	 *            a URI of the tileset image file
	 */
	public void setSource(String source) {
		externalSource = source;
	}

	/**
	 * Sets the base directory for the tileset
	 * 
	 * @param base
	 *            a String containing the native format directory
	 */
	public void setBaseDir(String base) {
		this.base = base;
	}

	/**
	 * Sets the name of this tileset.
	 * 
	 * @param name
	 *            the new name for this tileset
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Sets the transparent color in the tileset image.
	 * 
	 * @param color
	 *            a {@link com.jme3.math.ColorRGBA} object.
	 */
	public void setTransparentColor(ColorRGBA color) {
		transparentColor = color;
	}

	/**
	 * Adds the tile to the set, setting the id of the tile only if the current
	 * value of id is -1.
	 * 
	 * @param t
	 *            the tile to add
	 * @return int The <b>local</b> id of the tile
	 */
	public int addTile(Tile t) {
		if (t.getId() < 0) {
			t.setId(tiles.size());
		}

		if (tileDimensions.x < t.getWidth()) {
			tileDimensions.x = t.getWidth();
		}

		if (tileDimensions.y < t.getHeight()) {
			tileDimensions.y = t.getHeight();
		}

		tiles.add(t);
		t.setTileSet(this);

		return t.getId();
	}

	/**
	 * This method takes a new Tile object as argument, and in addition to the
	 * functionality of <code>addTile()</code>, sets the id of the tile to -1.
	 * 
	 * @see TileSet#addTile(Tile)
	 * @param t
	 *            the new tile to add.
	 */
	public void addNewTile(Tile t) {
		t.setId(-1);
		addTile(t);
	}

	/**
	 * Removes a tile from this tileset. Does not invalidate other tile indices.
	 * Removal is simply setting the reference at the specified index to
	 * <b>null</b>.
	 * 
	 * @param i
	 *            the index to remove
	 */
	public void removeTile(int i) {
		tiles.set(i, null);
	}

	/**
	 * Returns the amount of tiles in this tileset.
	 * 
	 * @return the amount of tiles in this tileset
	 * @since 0.13
	 */
	public int size() {
		return tiles.size();
	}

	/**
	 * Returns the maximum tile id.
	 * 
	 * @return the maximum tile id, or -1 when there are no tiles
	 */
	public int getMaxTileId() {
		return tiles.size() - 1;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Returns an iterator over the tiles in this tileset.
	 */
	@Override
	public Iterator<Tile> iterator() {
		return tiles.iterator();
	}

	/**
	 * Returns the width of tiles in this tileset. All tiles in a tileset should
	 * be the same width, and the same as the tile width of the map the tileset
	 * is used with.
	 * 
	 * @return int - The maximum tile width
	 */
	public float getTileWidth() {
		return tileDimensions.x;
	}

	/**
	 * Returns the tile height of tiles in this tileset. Not all tiles in a
	 * tileset are required to have the same height, but the height should be at
	 * least the tile height of the map the tileset is used with.
	 * 
	 * If there are tiles with varying heights in this tileset, the returned
	 * height will be the maximum.
	 * 
	 * @return the max height of the tiles in the set
	 */
	public float getTileHeight() {
		return tileDimensions.y;
	}

	/**
	 * Returns the spacing between the tiles on the tileset image.
	 * 
	 * @return the spacing in pixels between the tiles on the tileset image
	 */
	public int getTileSpacing() {
		return tileSpacing;
	}

	/**
	 * Returns the margin around the tiles on the tileset image.
	 * 
	 * @return the margin in pixels around the tiles on the tileset image
	 */
	public int getTileMargin() {
		return tileMargin;
	}

	/**
	 * Gets the tile with <b>local</b> id <code>i</code>.
	 * 
	 * @param i
	 *            local id of tile
	 * @return A tile with local id <code>i</code> or <code>null</code> if no
	 *         tile exists with that id
	 */
	public Tile getTile(int i) {
		try {
			return tiles.get(i);
		} catch (ArrayIndexOutOfBoundsException a) {
		}
		return null;
	}

	/**
	 * Returns the first non-null tile in the set.
	 * 
	 * @return The first tile in this tileset, or <code>null</code> if none
	 *         exists.
	 */
	public Tile getFirstTile() {
		Tile ret = null;
		int i = 0;
		while (ret == null && i <= getMaxTileId()) {
			ret = getTile(i);
			i++;
		}
		return ret;
	}

	/**
	 * Returns the source of this tileset.
	 * 
	 * @return a filename if tileset is external or <code>null</code> if tileset
	 *         is internal.
	 */
	public String getSource() {
		return externalSource;
	}

	/**
	 * Returns the base directory for the tileset
	 * 
	 * @return a directory in native format as given in the tileset file or tag
	 */
	public String getBaseDir() {
		return base;
	}

	/**
	 * <p>
	 * Getter for the field <code>name</code>.
	 * </p>
	 * 
	 * @return the name of this tileset.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the transparent color of the tileset image, or <code>null</code>
	 * if none is set.
	 * 
	 * @return Color - The transparent color of the set
	 */
	public ColorRGBA getTransparentColor() {
		return transparentColor;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return getName() + " [" + size() + "]";
	}

	// TILE IMAGE CODE
	/**
	 * Returns whether the tileset is derived from a tileset image.
	 * 
	 * @return tileSetImage != null
	 */
	public boolean isSetFromImage() {
		return tileSetTexture != null;
	}

	public Texture getTexture() {
		return tileSetTexture;
	}
	
	int nextX;
	int nextY;

    /** {@inheritDoc} */
    private final void reset() {
        nextX = tileMargin;
        nextY = tileMargin;
    }
    
    /** {@inheritDoc} */
    public Vector2f getTileDimensions() {
        return new Vector2f(tileWidth, tileHeight);
    }

    /**
     * Returns the number of tiles per row in the tileset image.
     *
     * @return the number of tiles per row in the tileset image.
     */
    public int getTilesPerRow() {
        return (tileSetTexture.getImage().getWidth() - 2 * tileMargin + tileSpacing) / (tileWidth + tileSpacing);
    }
    
    /** {@inheritDoc} */
    private Tile getNextTile() {
    	Tile tile = new Tile();
        if (nextY + tileHeight + tileMargin <= tileSetTexture.getImage().getHeight()) {
        	
        	tile.setRectangle(nextX, nextY, tileWidth, tileHeight);
            
            nextX += tileWidth + tileSpacing;
            if (nextX + tileWidth + tileMargin > tileSetTexture.getImage().getWidth()) {
                nextX = tileMargin;
                nextY += tileHeight + tileSpacing;
            }

            return tile;
        }

        return null;
    }
}