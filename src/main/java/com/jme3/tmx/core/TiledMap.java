package com.jme3.tmx.core;

import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.jme3.math.ColorRGBA;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Node;

/**
 * A map contains three different kinds of layers. Tile layers were once
 * the only type, and are simply called layer, object layers have the
 * objectgroup tag and image layers use the imagelayer tag. The order in
 * which these layers appear is the order in which the layers are
 * rendered by Tiled.
 * @author yanmaoyuan
 */
public class TiledMap extends Base {

	static Logger logger = Logger.getLogger(TiledMap.class.getName());

	/**
	 * The orientation of the map determines how it should be rendered. An
	 * Orthogonal map is using rectangular tiles that are aligned on a straight
	 * grid. An Isometric map uses diamond shaped tiles that are aligned on an
	 * isometric projected grid. A Hexagonal map uses hexagon shaped tiles that
	 * fit into each other by shifting every other row.
	 */
	public enum Orientation {
		ORTHOGONAL, ISOMETRIC,
		/**
		 * Hexagonal.
		 * 
		 * @since 0.11
		 */
		HEXAGONAL,
		/**
		 * Staggered (used for iso and hex).
		 */
		STAGGERED;
	}

	/**
	 * The different formats in which the tile layer data can be stored.
	 */
	public enum LayerDataFormat {
		XML, Base64, Base64Gzip, Base64Zlib, CSV;
	}

	/**
	 * The order in which tiles are rendered on screen. since Tiled 0.10, but
	 * only supported for orthogonal maps at the moment
	 */
	public enum RenderOrder {
		RIGHT_DOWN, RIGHT_UP, LEFT_DOWN, LEFT_UP;
	}

	/**
	 * For staggered and hexagonal maps, determines which axis ("x" or "y") is
	 * staggered. (since 0.11)
	 */
	public enum StaggerAxis {
		X, Y;
	}

	/**
	 * For staggered and hexagonal maps, determines whether the "even" or "odd"
	 * indexes along the staggered axis are shifted. (since 0.11)
	 */
	public enum StaggerIndex {
		EVEN, ODD;
	}

	/**
	 * Map orientation. Tiled supports "orthogonal", "isometric", "staggered"
	 * (since 0.9) and "hexagonal" (since 0.11).
	 */
	private Orientation orientation = Orientation.ORTHOGONAL;

	/**
	 * The order in which tiles on tile layers are rendered. Valid values are
	 * right-down (the default), right-up, left-down and left-up. In all cases,
	 * the map is drawn row-by-row. (since 0.10, but only supported for
	 * orthogonal maps at the moment)
	 */
	private RenderOrder renderOrder = RenderOrder.RIGHT_DOWN;

	/**
	 * The map width and height in tiles.
	 */
	private int width, height;

	/**
	 * The width and height of a tile.
	 */
	private int tileWidth, tileHeight;

	/**
	 * Only for hexagonal maps. Determines the width or height (depending on the
	 * staggered axis) of the tile's edge, in pixels.
	 */
	private int hexSideLength;

	/**
	 * For staggered and hexagonal maps, determines which axis ("x" or "y") is
	 * staggered. (since 0.11)
	 */
	private StaggerAxis staggerAxis = StaggerAxis.Y;

	/**
	 * For staggered and hexagonal maps, determines whether the "even" or "odd"
	 * indexes along the staggered axis are shifted. (since 0.11)
	 */
	private StaggerIndex staggerIndex = StaggerIndex.ODD;

	/**
	 * The background color of the map. (since 0.9, optional, may include alpha
	 * value since 0.15 in the form #AARRGGBB)
	 */
	private final ColorRGBA backgroundColor;

	/**
	 * Stores the next available ID for new objects. This number is stored to
	 * prevent reuse of the same ID after objects have been removed. (since
	 * 0.11)
	 */
	private int nextObjectId;

	private TreeMap<Integer, Tileset> tilesetPerFirstGid;
	private List<Tileset> tilesets;
	private Map<String, Tileset> tilesetMap;
	private List<Layer> layers;
	private Map<String, Layer> layerMap;

	/**
	 * <p>
	 * Constructor for Map.
	 * </p>
	 * 
	 * @param width
	 *            the map width in tiles.
	 * @param height
	 *            the map height in tiles.
	 */
	public TiledMap(int width, int height) {
		this.width = width;
		this.height = height;
		this.tileWidth = 0;
		this.tileHeight = 0;
		this.hexSideLength = 0;
		this.nextObjectId = 0;
		this.backgroundColor = new ColorRGBA(0f, 0f, 0f, 0f);
		this.tilesets = new ArrayList<>();
		this.tilesetMap = new HashMap<>();
		this.layers = new ArrayList<>();
		this.layerMap = new HashMap<>();

		// Load tilesets first, in case order is munged
		this.tilesetPerFirstGid = new TreeMap<>();

		// in a TiledMap I use Node as the spatial
		this.visual = new Node("TileMap");
		this.visual.setQueueBucket(Bucket.Gui);
	}

	/**
	 * Returns the total number of layers.
	 * 
	 * @return the size of the layer
	 */
	public int getLayerCount() {
		return layers.size();
	}

	/**
	 * addLayer.
	 * 
	 * @param layer
	 *            a {@link com.jme3.tmx.core.Layer} object.
	 * @return a {@link com.jme3.tmx.core.Layer} object.
	 */
	public Layer addLayer(Layer layer) {
		layer.setMap(this);
		layers.add(layer);
		layerMap.put(layer.getName(), layer);
		return layer;
	}

	/**
	 * setLayer
	 * 
	 * @param index
	 *            a int
	 * @param layer
	 *            a {@link com.jme3.tmx.core.Layer} object
	 */
	public void setLayer(int index, Layer layer) {
		layer.setMap(this);
		layers.set(index, layer);
		layerMap.put(layer.getName(), layer);
	}

	/**
	 * <p>
	 * insertLayer.
	 * </p>
	 * 
	 * @param index
	 *            a int.
	 * @param layer
	 *            a {@link com.jme3.tmx.core.Layer} object.
	 */
	public void insertLayer(int index, Layer layer) {
		layer.setMap(this);
		layers.add(index, layer);
		layerMap.put(layer.getName(), layer);
	}

	/**
	 * Removes the layer at the specified index. Layers above this layer will
	 * move down to fill the gap.
	 * 
	 * @param index
	 *            the index of the layer to be removed
	 * @return the layer that was removed from the list
	 */
	public Layer removeLayer(int index) {
		Layer layer = layers.remove(index);
		layerMap.remove(layer.getName());
		return layer;
	}

	/**
	 * Removes all layers from the plane.
	 */
	public void removeAllLayers() {
		layers.clear();
		layerMap.clear();
	}

	/**
	 * Returns the layer list.
	 * 
	 * @return List the layer list
	 */
	public List<Layer> getLayers() {
		return layers;
	}

	/**
	 * Returns the layer with the given name.
	 *
	 * @return Layer the layer with the given name
	 */
	public Map<String, Layer> getLayerMap() {
		return layerMap;
	}

	/**
	 * Sets the layer list to the given java.util.List.
	 * 
	 * @param layers the new set of layers
	 */
	public void setLayers(List<Layer> layers) {
		this.layers = layers;
		if (layers == null || layers.isEmpty()) {
			layerMap.clear();
		} else {
			this.layerMap = layers.stream().collect(Collectors.toMap(Layer::getName, layer -> layer));
		}
	}

	/**
	 * Returns the layer at the specified vector index.
	 * 
	 * @param i the index of the layer to return
	 * @return the layer at the specified index, or null if the index is out of
	 *         bounds
	 */
	public Layer getLayer(int i) {
		try {
			return layers.get(i);
		} catch (IndexOutOfBoundsException e) {
			logger.log(Level.WARNING, "can't find the layer with the index:"
					+ i + ".", e);
		}
		return null;
	}

	/**
	 * Returns the layer with the given name.
	 *
	 * @param name the name of the layer to return
	 * @return the layer with the given name, or null if no layer with that name
	 *         exists
	 */
	public Layer getLayer(String name) {
		return layerMap.get(name);
	}

	/**
	 * Adds a Tileset to this Map. If the set is already attached to this map,
	 * <code>addTileset</code> simply returns.
	 * 
	 * @param tileset
	 *            a tileset to add
	 */
	public void addTileset(Tileset tileset) {
		if (tileset == null || tilesets.contains(tileset)) {
			return;
		}

		Tile t = tileset.getTile(0);

		if (t != null) {
			setFirstGidForTileset(tileset, tileset.getFirstGid());
			
			int tw = t.getWidth();
			int th = t.getHeight();
			if (tw != tileWidth) {
				if (tileWidth == 0) {
					tileWidth = tw;
					tileHeight = th;
				}
			}
		}

		tilesets.add(tileset);
		tilesetMap.put(tileset.getName(), tileset);
	}

	/**
	 * Removes a {@link com.jme3.tmx.core.Tileset} from the map, and removes any tiles
	 * in the set from the map layers.
	 * 
	 * @param tileset
	 *            TileSet to remove
	 */
	public void removeTileset(Tileset tileset) {
		// Sanity check
		final int tilesetIndex = tilesets.indexOf(tileset);
		if (tilesetIndex == -1) {
			return;
		}

		// Go through the map and remove any instances of the tiles in the set
		for (Tile tile : tileset) {
			for (Layer ml : layers) {
				if (ml instanceof TileLayer) {
					((TileLayer) ml).removeTile(tile);
				}
			}
		}

		tilesets.remove(tileset);
		tilesetMap.remove(tileset.getName());
	}

	/**
	 * Returns a list with the currently loaded tileSets.
	 * 
	 * @return List
	 */
	public List<Tileset> getTileSets() {
		return tilesets;
	}

	/**
	 * Returns the tileset with the given name.
	 *
	 * @return Tileset the tileset with the given name
	 */
	public Map<String, Tileset> getTilesetMap() {
		return tilesetMap;
	}

	/**
	 * Sets the tileset list to the given java.util.List.
	 *
	 * @param name the name of the tileset to return
	 * @return the tileset with the given name, or null if no tileset with that
	 */
	public Tileset getTileset(String name) {
		return tilesetMap.get(name);
	}

	/**
	 * Get the tile set and its corresponding firstgid that matches the given
	 * global tile id.
	 * 
	 * @param gid
	 *            a global tile id
	 * @return the tileset containing the tile with the given global tile id, or
	 *         <code>null</code> when no such tileset exists
	 */
	private Entry<Integer, Tileset> findTileSetForTileGID(int gid) {
		return tilesetPerFirstGid.floorEntry(gid);
	}

	private void setFirstGidForTileset(Tileset tileset, int firstGid) {
		tilesetPerFirstGid.put(firstGid, tileset);
		tilesetMap.put(tileset.getName(), tileset);
	}
	
	/**
	 * Helper method to set the tile based on its global id.
	 * 
	 * @param ml tile layer
	 * @param y
	 *            y-coordinate
	 * @param x
	 *            x-coordinate
	 * @param tileId
	 *            global id of the tile as read from the file
	 */
	public void setTileAtFromTileId(TileLayer ml, int y, int x, int tileId) {
		// clear the flag
		int gid = tileId & ~Tile.FLIPPED_MASK;
		
		Tile tile = getTileForTileGID(gid);
		if (tile != null) {
			Tile t = tile.clone();
			t.setGid(tileId);
			ml.setTileAt(x, y, t);
		}
	}
	
	/**
	 * Helper method to get the tile based on its global id
	 * 
	 * @param gid global id of the tile
	 * @return <ul>
	 *         <li>{@link Tile} object corresponding to the global id, if found</li>
	 *         <li><code>null</code>, otherwise</li>
	 *         </ul>
	 */
	public Tile getTileForTileGID(final int gid) {

		Tile tile = null;
		Entry<Integer, Tileset> ts = findTileSetForTileGID(gid);
		if (ts != null) {
			tile = ts.getValue().getTile(gid - ts.getKey());
		}

		if (gid > 0 && tile == null) {
			logger.warning("can find tile with gid:" + gid);
		}
		return tile;
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

	public Orientation getOrientation() {
		return orientation;
	}

	public void setOrientation(String orient) {
		try {
			this.orientation = Orientation.valueOf(orient.toUpperCase());
		} catch (IllegalArgumentException e) {
			logger.warning("Unknown orientation '" + orientation + "'");
		}
	}

	public void setOrientation(Orientation orientation) {
		this.orientation = orientation;
	}

	public RenderOrder getRenderOrder() {
		return renderOrder;
	}

	public void setRenderOrder(String renderorder) {
		if ("right-down".equals(renderorder)) {
			renderOrder = RenderOrder.RIGHT_DOWN;
		} else if ("right-up".equals(renderorder)) {
			renderOrder = RenderOrder.RIGHT_UP;
		} else if ("left-down".equals(renderorder)) {
			renderOrder = RenderOrder.LEFT_DOWN;
		} else if ("left-up".equals(renderorder)) {
			renderOrder = RenderOrder.LEFT_UP;
		} else {
			// use default
			renderOrder = RenderOrder.RIGHT_DOWN;
			logger.warning("Unknown render order '" + renderorder + "'");
		}
	}
	
	public void setRenderOrder(RenderOrder renderOrder) {
		this.renderOrder = renderOrder;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
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

	public int getHexSideLength() {
		return hexSideLength;
	}

	public void setHexSideLength(int hexSideLength) {
		this.hexSideLength = hexSideLength;
	}

	public StaggerAxis getStaggerAxis() {
		return staggerAxis;
	}

	public void setStaggerAxis(String staggerAxis) {
		try {
			this.staggerAxis = StaggerAxis.valueOf(staggerAxis.toUpperCase());
		} catch (IllegalArgumentException e) {
			logger.warning("Unknown stagger axis '" + staggerAxis + "'");
		}
	}

	public void setStaggerAxis(StaggerAxis staggerAxis) {
		this.staggerAxis = staggerAxis;
	}

	public StaggerIndex getStaggerIndex() {
		return staggerIndex;
	}

	public void setStaggerIndex(String staggerIndex) {
		try {
			this.staggerIndex = StaggerIndex.valueOf(staggerIndex.toUpperCase());
		} catch (IllegalArgumentException e) {
			logger.warning("Unknown stagger index '" + staggerIndex + "'");
		}
	}

	public void setStaggerIndex(StaggerIndex staggerIndex) {
		this.staggerIndex = staggerIndex;
	}

	public ColorRGBA getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(ColorRGBA backgroundColor) {
		this.backgroundColor.set(backgroundColor);
	}

	public int getNextObjectId() {
		return nextObjectId;
	}

	public void setNextObjectId(int nextObjectId) {
		this.nextObjectId = nextObjectId;
	}
	
	@Override
	public Node getVisual() {
		return (Node)visual;
	}
}
