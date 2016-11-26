package tiled.util;

import com.jme3.math.Vector2f;
import com.jme3.texture.Image;

/**
 * Cuts tiles from a tileset texture according to a regular rectangular pattern.
 * Supports a variable spacing between tiles and a margin around them.
 *
 * @author yanmaoyuan
 */
public class JmeTileCutter {
    private int nextX, nextY;
    private Image image;
    private final int tileWidth;
    private final int tileHeight;
    private final int tileSpacing;
    private final int tileMargin;

    /**
     * <p>Constructor for BasicTileCutter.</p>
     *
     * @param tileWidth a int.
     * @param tileHeight a int.
     * @param tileSpacing a int.
     * @param tileMargin a int.
     */
    public JmeTileCutter(int tileWidth, int tileHeight, int tileSpacing,
            int tileMargin) {
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        this.tileSpacing = tileSpacing;
        this.tileMargin = tileMargin;

        reset();
    }

    /** {@inheritDoc} */
    public String getName() {
        return "Basic";
    }

    /** {@inheritDoc} */
    public void setImage(Image image) {
        this.image = image;
    }

    /** {@inheritDoc} */
    public Image getNextTile() {
        if (nextY + tileHeight + tileMargin <= image.getHeight()) {
        	
        	// TODO calculate uv with nextX/nextY/tileWidth/tileHeight
        	
            Image tile = null;
            //image.getSubimage(nextX, nextY, tileWidth, tileHeight);
            
            nextX += tileWidth + tileSpacing;
            if (nextX + tileWidth + tileMargin > image.getWidth()) {
                nextX = tileMargin;
                nextY += tileHeight + tileSpacing;
            }

            return tile;
        }

        return null;
    }

    /** {@inheritDoc} */
    public final void reset() {
        nextX = tileMargin;
        nextY = tileMargin;
    }

    /** {@inheritDoc} */
    public Vector2f getTileDimensions() {
        return new Vector2f(tileWidth, tileHeight);
    }

    /**
     * Returns the spacing between tile images.
     *
     * @return the spacing between tile images.
     */
    public int getTileSpacing() {
        return tileSpacing;
    }

    /**
     * Returns the margin around the tile images.
     *
     * @return the margin around the tile images.
     */
    public int getTileMargin() {
        return tileMargin;
    }

    /**
     * Returns the number of tiles per row in the tileset image.
     *
     * @return the number of tiles per row in the tileset image.
     */
    public int getTilesPerRow() {
        return (image.getWidth() - 2 * tileMargin + tileSpacing) / (tileWidth + tileSpacing);
    }
}
