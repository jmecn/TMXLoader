package com.jme3.tmx.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import com.jme3.material.Material;
import com.jme3.texture.Texture;
import com.jme3.tmx.enums.FillMode;
import com.jme3.tmx.enums.ObjectAlignment;
import com.jme3.tmx.enums.Orientation;
import com.jme3.tmx.enums.TileRenderSize;
import com.jme3.tmx.util.TileCutter;

/**
 * If there are multiple &lt;tileset&gt; elements, they are in ascending order of
 * their firstgid attribute. The first tileset always has a firstgid value of 1.
 * Since Tiled 0.15, image collection tilesets do not necessarily number their
 * tiles consecutively since gaps can occur when removing tiles.
 * 
 * Can contain at most one:
 *   &lt;image&gt;, &lt;tileoffset&gt;, &lt;grid&gt; (since 1.0),
 *   &lt;properties&gt;, &lt;terraintypes&gt;, &lt;wangsets&gt; (since 1.1),
 *   &lt;transformations&gt; (since 1.5)
 * Can contain any number: &lt;tile&gt;
 *
 * @author yanmaoyuan
 * 
 */
public class Tileset extends Base implements Iterable<Tile> {

    static Logger logger = Logger.getLogger(Tileset.class.getName());

    /**
     * The first global tile ID of this tileset (this global ID maps to the
     * first tile in this tileset).
     */
    private int firstGid;

    /**
     * If this tileset is stored in an external TSX (Tile Set XML) file, this
     * attribute refers to that file. That TSX file has the same structure as
     * the &lt;tileset> element described here. (There is the firstgid attribute
     * missing and this source attribute is also not there. These two attributes
     * are kept in the TMX map, since they are map specific.)
     */
    private String source;

    /**
     * The name of this tileset.
     */
    private String name;

    /**
     * The class of this tileset (since 1.9, defaults to “”).
     */
    private String clazz;

    /**
     * The (maximum) width and height of the tiles in this tileset.
     * Irrelevant for image collection tilesets, but stores the maximum
     * tile width and height.
     */
    private int tileWidth, tileHeight;

    /**
     * The spacing in pixels between the tiles in this tileset (applies to the
     * tileset image, defaults to 0). Irrelevant for image collection tilesets.
     */
    private int tileSpacing;

    /**
     * The margin around the tiles in this tileset (applies to the tileset
     * image, defaults to 0). Irrelevant for image collection tilesets.
     */
    private int tileMargin;

    /**
     * The number of tiles in this tileset (since 0.13). Note that there can
     * be tiles with a higher ID than the tile count, in case the tileset is
     * an image collection from which tiles have been removed.
     */
    private int tileCount;

    /**
     * The number of tile columns in the tileset. For image collection tilesets
     * it is editable and is used when displaying the tileset. (since 0.15)
     */
    private int columns;

    /**
     * Controls the alignment for tile objects. Valid values are "unspecified",
     * "topleft", "top", "topright", "left", "center", "right", "bottomleft",
     * "bottom" and "bottomright". The default value is "unspecified", for
     * compatibility reasons. When "unspecified", tile objects use "bottomleft"
     * in orthogonal mode and "bottom" in isometric mode. (since 1.4)
     */
    private ObjectAlignment objectAlignment;

    /**
     * The size to use when rendering tiles from this tileset on a tile layer.
     * Valid values are "tile" (the default) and "grid". When set to "grid",
     * the tile is drawn at the tile grid size of the map. (since 1.9)
     */
    private TileRenderSize tileRenderSize;

    /**
     * The fill mode to use when rendering tiles from this tileset. Valid values
     * are "stretch" (the default) and "preserve-aspect-fit". Only relevant when
     * the tiles are not rendered at their native size, so this applies to resized
     * tile objects or in combination with tilerendersize set to "grid". (since 1.9)
     */
    private FillMode fillMode;

    private boolean hasGrid = false;

    /**
     * Orientation of the grid for the tiles in this tileset (orthogonal or isometric, defaults to orthogonal)
     */
    private Orientation orientation = Orientation.ORTHOGONAL;

    /**
     * Width of a grid cell
     */
    private int gridWidth;

    /**
     * Height of a grid cell
     */
    private int gridHeight;

    /**
     * Horizontal offset in pixels.
     */
    private int tileOffsetX = 0;
    /**
     * Vertical offset in pixels (positive is down)
     */
    private int tileOffsetY = 0;

    private Transformations transformations;

    private String imageSource;
    private Texture texture;
    private Material material;

    /**
     * This element defines an array of terrain types, which can be referenced
     * from the terrain attribute of the tile element.
     *
     * @deprecated This element has been deprecated since Tiled 1.5, in favour
     * of the &lt;wangsets> element, which is more flexible. Tilesets containing
     * terrain types are automatically saved with a Wang set instead.
     */
    private List<Terrain> terrains = new ArrayList<>();

    private List<WangSet> wangSets = new ArrayList<>();

    private List<Tile> tiles = new ArrayList<>();

    /**
     * Default constructor
     */
    public Tileset() {
        this.tileWidth = 32;
        this.tileHeight = 32;
        this.tileSpacing = 0;
        this.tileMargin = 0;
    }

    public Tileset(int width, int height, int space, int margin) {
        this.tileWidth = width;
        this.tileHeight = height;
        this.tileSpacing = space;
        this.tileMargin = margin;
    }

    public int getFirstGid() {
        return firstGid;
    }

    public void setFirstGid(int firstGid) {
        this.firstGid = firstGid;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTileWidth() {
        return tileWidth;
    }

    public void setTileWidth(int tileWidth) {
        this.tileWidth = tileWidth;
    }

    public int getTileHeight() {
        return tileHeight;
    }

    public void setTileHeight(int tileHeight) {
        this.tileHeight = tileHeight;
    }

    public int getTileSpacing() {
        return tileSpacing;
    }

    public void setTileSpacing(int tileSpacing) {
        this.tileSpacing = tileSpacing;
    }

    public int getTileMargin() {
        return tileMargin;
    }

    public void setTileMargin(int tileMargin) {
        this.tileMargin = tileMargin;
    }

    public int getTileCount() {
        return tileCount;
    }

    public void setTileCount(int tileCount) {
        this.tileCount = tileCount;
    }

    public int getColumns() {
        return columns;
    }

    public void setColumns(int columns) {
        this.columns = columns;
    }

    public String getImageSource() {
        return imageSource;
    }

    public void setImageSource(String imageSource) {
        this.imageSource = imageSource;
    }

    public Texture getTexture() {
        return texture;
    }

    /**
     * Creates a tileset from a Texture2D. Tiles are cut by the passed cutter.
     *
     * @param texture
     *            the image to be used, must not be null
     */
    public void setTexture(Texture texture) {
        assert texture != null;

        this.texture = texture;

        TileCutter cutter = new TileCutter(texture, tileWidth, tileHeight, tileMargin, tileSpacing);
        cutter.setTileOffset(tileOffsetX, tileOffsetY);

        Tile tile = cutter.getNextTile();
        while (tile != null) {
            addNewTile(tile);
            tile = cutter.getNextTile();
        }
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public List<Terrain> getTerrains() {
        return terrains;
    }

    public void setTerrains(List<Terrain> terrains) {
        this.terrains = terrains;
    }

    public void addTerrain(Terrain terrain) {
        terrain.setId(terrains.size());
        terrains.add(terrain);
    }

    public Terrain getTerrain(int id) {
        return terrains.get(id);
    }

    public List<WangSet> getWangSets() {
        return wangSets;
    }
    public void setWangSets(List<WangSet> wangSets) {
        this.wangSets = wangSets;
    }
    public void addWangSet(WangSet wangSet) {
        wangSet.setId(wangSets.size());
        wangSet.setTileset(this);
        wangSets.add(wangSet);
    }

    public WangSet getWangSet(int id) {
        return wangSets.get(id);
    }

    public List<Tile> getTiles() {
        return tiles;
    }

    public void setTiles(List<Tile> tiles) {
        this.tiles = tiles;
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

        if (tileWidth < t.getWidth()) {
            tileWidth = t.getWidth();
        }

        if (tileHeight < t.getHeight()) {
            tileHeight = t.getHeight();
        }

        tiles.add(t);
        t.setTileset(this);

        return t.getId();
    }

    /**
     * This method takes a new Tile object as argument, and in addition to the
     * functionality of <code>addTile()</code>, sets the id of the tile to -1.
     *
     * @see Tileset#addTile(Tile)
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
        } catch (IndexOutOfBoundsException a) {
            logger.warning("Tileset.getTile(): No tile with id " + i + " exists.");
        }
        return null;
    }

    /**
     * Returns the first non-null tile in the set.
     *
     * @return The first tile in this tileset, or <code>null</code> if none exists.
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
     * Returns whether the tileset is derived from a tileset image.
     *
     * @return tileSetImage != null
     */
    public boolean isSetFromImage() {
        return texture != null;
    }

    @Override
    public Iterator<Tile> iterator() {
        return tiles.iterator();
    }

    public void setTileOffset(int tileOffsetX, int tileOffsetY) {
        this.tileOffsetX = tileOffsetX;
        this.tileOffsetY = tileOffsetY;
    }

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    public ObjectAlignment getObjectAlignment() {
        return objectAlignment;
    }

    public void setObjectAlignment(ObjectAlignment objectAlignment) {
        this.objectAlignment = objectAlignment;
    }

    public void setObjectAlignment(String objectAlignment) {
        this.objectAlignment = ObjectAlignment.fromString(objectAlignment);
    }

    public TileRenderSize getTileRenderSize() {
        return tileRenderSize;
    }

    public void setTileRenderSize(TileRenderSize tileRenderSize) {
        this.tileRenderSize = tileRenderSize;
    }

    public void setTileRenderSize(String tileRenderSize) {
        this.tileRenderSize = TileRenderSize.fromString(tileRenderSize);
    }

    public FillMode getFillMode() {
        return fillMode;
    }

    public void setFillMode(FillMode fillMode) {
        this.fillMode = fillMode;
    }

    public void setFillMode(String fillMode) {
        this.fillMode = FillMode.fromString(fillMode);
    }

    public boolean hasGrid() {
        return hasGrid;
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public int getGridWidth() {
        return gridWidth;
    }

    public int getGridHeight() {
        return gridHeight;
    }

    public void setGrid(Orientation orientation, int width, int height) {
        this.orientation = orientation;
        this.gridWidth = width;
        this.gridHeight = height;
        this.hasGrid = true;
    }

    public Transformations getTransformations() {
        return this.transformations;
    }

    public void setTransformations(Transformations transformations) {
        this.transformations = transformations;
    }
}
