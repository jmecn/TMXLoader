package io.github.jmecn.tiled.loader;

import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.Vector4f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import io.github.jmecn.tiled.animation.AnimatedTileControl;
import io.github.jmecn.tiled.animation.Animation;
import io.github.jmecn.tiled.animation.Frame;
import io.github.jmecn.tiled.core.*;
import io.github.jmecn.tiled.enums.FillMode;
import io.github.jmecn.tiled.enums.ObjectAlignment;
import io.github.jmecn.tiled.enums.Orientation;
import io.github.jmecn.tiled.enums.TileRenderSize;
import io.github.jmecn.tiled.math2d.Point;
import io.github.jmecn.tiled.render.shape.TileMesh;
import io.github.jmecn.tiled.util.ColorUtil;
import io.github.jmecn.tiled.util.TileCutter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import static io.github.jmecn.tiled.TiledConst.*;
import static io.github.jmecn.tiled.loader.Utils.*;
import static io.github.jmecn.tiled.loader.Utils.getAttribute;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public final class TilesetLoader {

    private static final Logger logger = LoggerFactory.getLogger(TilesetLoader.class);

    private final AssetManager assetManager;
    private final AssetKey<?> assetKey;

    private final TiledImageLoader tiledImageLoader;
    private final PropertyLoader propertiesLoader;
    public TilesetLoader(AssetManager assetManager, AssetKey<?> assetKey) {
        this.assetManager = assetManager;
        this.assetKey = assetKey;

        this.tiledImageLoader = new TiledImageLoader(assetManager, assetKey);
        this.propertiesLoader = new PropertyLoader();
    }

    /**
     * Load a TileSet from .tsx file.
     *
     * @param inputStream the input stream of the tileset file
     * @return the loaded tileset
     */
    public Tileset load(final InputStream inputStream) {
        Tileset set = null;
        Node root;

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Document doc;
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            doc = builder.parse(inputStream);

            NodeList nodeList = doc.getElementsByTagName(TILESET);

            // There can be only one tileset in a .tsx file.
            root = nodeList.item(0);

            if (root != null) {
                set = readTileset(root, null);
                if (set.getSource() != null) {
                    logger.warn("Recursive external tilesets are not supported.{}", set.getSource());
                }
                set.setSource(assetKey.getName());
            }
        } catch (Exception e) {
            logger.error("Failed while loading {}", assetKey.getName(), e);
        }

        return set;
    }

    /**
     * Load TileSet from a ".tsx" file.
     *
     * @param source the path to the tileset file
     * @return the loaded tileset
     */
    private Tileset load(String source) {
        String assetPath = toJmeAssetPath(assetManager, assetKey, source);

        // load it with assetManager
        Tileset ext = null;
        try {
            AssetInfo assetInfo = assetManager.locateAsset(new AssetKey<>(assetPath));
            ext = load(assetInfo.openStream());
            return ext;
        } catch (Exception e) {
            throw new IllegalArgumentException("Tileset " + source + " was not loaded correctly!", e);
        }

    }

    /**
     * <p>read tileset</p>
     *
     * Can contain at most one:
     *   &lt;image&gt;, &lt;tileoffset&gt;, &lt;grid&gt; (since 1.0),
     *   &lt;properties&gt;, &lt;terraintypes&gt;, &lt;wangsets&gt; (since 1.1),
     *   &lt;transformations&gt; (since 1.5)
     * Can contain any number :
     *   &lt;tile&gt;
     *
     * @param node node
     * @param map the TiledMap. Can be null when load a sperated tileset.
     * @return Tileset
     */
    public Tileset readTileset(Node node, TiledMap map) {

        String source = getAttributeValue(node, SOURCE);
        int firstGid = getAttribute(node, FIRST_GID, 1);

        if (source != null) {
            Tileset set = load(assetKey.getFolder() + source);
            set.setFirstGid(firstGid);
            return set;
        }

        int tileWidth = getAttribute(node, TILE_WIDTH, map != null ? map.getTileWidth() : 0);
        int tileHeight = getAttribute(node, TILE_HEIGHT, map != null ? map.getTileHeight() : 0);
        int tileSpacing = getAttribute(node, SPACING, 0);
        int tileMargin = getAttribute(node, MARGIN, 0);

        Tileset set = new Tileset(tileWidth, tileHeight, tileSpacing, tileMargin);
        set.setFirstGid(firstGid);

        String name = getAttributeValue(node, NAME);
        String clazz = getAttribute(node, CLASS, "");
        String objectAlignment = getAttribute(node, OBJECT_ALIGNMENT, ObjectAlignment.UNSPECIFIED.getValue());
        String tileRenderSize = getAttribute(node, TILE_RENDER_SIZE, TileRenderSize.TILE.getValue());
        String fillMode = getAttribute(node, FILL_MODE, FillMode.STRETCH.getValue());

        set.setName(name);
        set.setClazz(clazz);
        set.setObjectAlignment(objectAlignment);
        set.setTileRenderSize(tileRenderSize);
        set.setFillMode(fillMode);

        boolean hasTilesetImage = false;
        TiledImage image;
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);

            String nodeName = child.getNodeName();
            switch (nodeName) {
                case IMAGE: {
                    if (hasTilesetImage) {
                        logger.warn("Ignoring illegal image element after tileset image.");
                        continue;
                    }

                    image = tiledImageLoader.load(child);
                    if (image.getTexture() != null) {
                        // Not a shared image, but an entire set in one image
                        // file. There should be only one image element in this
                        // case.
                        hasTilesetImage = true;

                        Material material = image.getMaterial();
                        material.setBoolean("UseTilesetImage", true);
                        material.setVector4("TileSize", new Vector4f(tileWidth, tileHeight, tileMargin, tileSpacing));

                        set.setImageSource(image.getSource());
                        set.setTexture(image.getTexture());
                        set.setMaterial(material);

                        TileCutter cutter = new TileCutter(image.getWidth(), image.getHeight(), tileWidth, tileHeight, tileMargin, tileSpacing);
                        set.setColumns(cutter.getColumns());
                        set.setTileCount(cutter.getTileCount());

                        Tile tile = cutter.getNextTile();
                        while (tile != null) {
                            set.addNewTile(tile);
                            tile = cutter.getNextTile();
                        }
                    }
                    break;
                }
                case "grid": {
                    /*
                     * This element is only used in case of isometric orientation,
                     * and determines how tile overlays for terrain and collision
                     * information are rendered.
                     */
                    String orientation = getAttribute(node, ORIENTATION, Orientation.ORTHOGONAL.getValue());
                    Orientation gridOrientation = Orientation.fromString(orientation);
                    int gridWidth = getAttribute(node, WIDTH, 0);
                    int gridHeight = getAttribute(node, HEIGHT, 0);
                    set.setGrid(gridOrientation, gridWidth, gridHeight);
                    break;
                }
                case TERRAIN_TYPES: {
                    NodeList terrainTypes = child.getChildNodes();
                    for (int k = 0; k < terrainTypes.getLength(); k++) {
                        Node terrainNode = terrainTypes.item(k);
                        if (terrainNode.getNodeName().equalsIgnoreCase(TERRAIN)) {
                            set.addTerrain(readTerrain(terrainNode));
                        }
                    }
                    break;
                }
                case TILE:
                    readTile(set, child);
                    break;
                case TILE_OFFSET: {
                    /*
                     * This element is used to specify an offset in pixels, to be
                     * applied when drawing a tile from the related tileset. When
                     * not present, no offset is applied.
                     */
                    final int tileOffsetX = getAttribute(child, X, 0);
                    final int tileOffsetY = getAttribute(child, Y, 0);

                    set.setTileOffset(tileOffsetX, tileOffsetY);
                    break;
                }
                case TRANSFORMATIONS: {
                    // This element is used to describe which transformations can be applied to the tiles
                    // (e.g. to extend a Wang set by transforming existing tiles).
                    // Whether the tiles in this set can be flipped horizontally (default 0)
                    int hFlip = getAttribute(node, H_FLIP, 0);
                    // Whether the tiles in this set can be flipped vertically (default 0)
                    int vFlip = getAttribute(node, V_FLIP, 0);
                    // Whether the tiles in this set can be rotated in 90 degree increments (default 0)
                    int rotate = getAttribute(node, ROTATE, 0);
                    // Whether untransformed tiles remain preferred, otherwise transformed tiles are used to produce more variations (default 0)
                    int preferUntransformed = getAttribute(node, PREFER_UNTRANSFORMED, 0);
                    set.setTransformations(new Transformations(hFlip, vFlip, rotate, preferUntransformed));
                    break;
                }
                case WANGSETS: {
                    NodeList wangSets = child.getChildNodes();
                    for (int k = 0; k < wangSets.getLength(); k++) {
                        Node wangSetNode = wangSets.item(k);
                        if (wangSetNode.getNodeName().equalsIgnoreCase(WANGSET)) {
                            set.addWangSet(readWangSet(wangSetNode));
                        }
                    }
                    break;
                }
                default: {
                    if (!PROPERTIES.equals(nodeName) && !TEXT_EMPTY.equals(nodeName)) {
                        logger.warn("Unsupported tileset element: {}", nodeName);
                    }
                    break;

                }
            }
        }

        return set;
    }

    /**
     * Create the visual part for every tile of a given Tileset.
     *
     * @param tileset the Tileset
     * @param map the TiledMap
     */
    public void createVisual(Tileset tileset, TiledMap map) {

        Point offset = new Point(tileset.getTileOffsetX(), tileset.getTileOffsetY());
        Point origin = new Point(0, map.getTileHeight());

        List<Tile> tiles = tileset.getTiles();
        int len = tiles.size();
        for (int i = 0; i < len; i++) {
            Tile tile = tiles.get(i);

            String name = "tile#" + tileset.getFirstGid() + "#" + tile.getId();

            Point coord = new Point(tile.getX(), tile.getY());
            Point size = new Point(tile.getWidth(), tile.getHeight());
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

            tile.setVisual(geometry);
        }
    }

    /**
     * read terrain.
     *
     * @param node
     * @return
     */
    private Terrain readTerrain(Node node) {
        final String name = getAttributeValue(node, NAME);
        final int tile = getAttribute(node, TILE, -1);

        Terrain terrain = new Terrain(name);
        terrain.setTile(tile);

        // read properties
        Properties props = propertiesLoader.readProperties(node);
        terrain.setProperties(props);

        return terrain;
    }

    /**
     * read wangset
     *
     * @param node
     * @return
     */
    private WangSet readWangSet(Node node) {
        String name = getAttributeValue(node, NAME);
        String clazz = getAttribute(node, TYPE, EMPTY);
        int tile = getAttribute(node, TILE, -1);

        WangSet wangSet = new WangSet(name);
        wangSet.setClazz(clazz);
        wangSet.setTile(tile);

        // read properties
        Properties props = propertiesLoader.readProperties(node);
        wangSet.setProperties(props);

        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            String nodeName = child.getNodeName();
            if (nodeName.equalsIgnoreCase(WANGCOLOR)) {
                wangSet.addWangColor(readWangColor(child));
            } else if (nodeName.equalsIgnoreCase(WANGTILE)) {
                wangSet.addWangTile(readWangTile(child));
            }
        }
        return wangSet;
    }

    private WangColor readWangColor(Node node) {
        String name = getAttributeValue(node, NAME);
        String clazz = getAttribute(node, CLASS, EMPTY);
        String color = getAttributeValue(node, COLOR);
        int tile = getAttribute(node, TILE, -1);
        float probability = (float) getDoubleAttribute(node, PROBABILITY, 0.0);

        WangColor wangColor = new WangColor();
        wangColor.setName(name);
        wangColor.setClazz(clazz);
        wangColor.setColor(ColorUtil.toColorRGBA(color));
        wangColor.setTile(tile);
        wangColor.setProbability(probability);

        // read properties
        Properties props = propertiesLoader.readProperties(node);
        wangColor.setProperties(props);

        return wangColor;
    }

    private WangTile readWangTile(Node node) {
        int tileId = getAttribute(node, TILE_ID, -1);
        String wangId = getAttribute(node, WANGID, EMPTY);
        return new WangTile(tileId, wangId);
    }


    /**
     * Read Tile for tileset
     * <p>
     * Can contain: properties, image (since 0.9), objectgroup (since 0.10),
     * animation (since 0.10)
     *
     * @param set
     * @param t
     * @return
     * @throws Exception
     */
    private void readTile(Tileset set, Node t) {

        Tile tile;

        int id = getAttribute(t, ID, -1);

        if (!set.isImageBased() || id > set.getMaxTileId()) {
            tile = new Tile();
            tile.setId(id);
            tile.setGid(id + set.getFirstGid());

            set.addTile(tile);
        } else {
            tile = set.getTile(id);
        }

        // since 1.9
        int x = getAttribute(t, X, -1);
        int y = getAttribute(t, Y, -1);
        int width = getAttribute(t, WIDTH, -1);
        int height = getAttribute(t, HEIGHT, -1);
        if (x > -1) {
            tile.setX(x);
        }
        if (y > -1) {
            tile.setY(y);
        }
        if (width > -1) {
            tile.setWidth(width);
        }
        if (height > -1) {
            tile.setHeight(height);
        }

        // in <tileset> we need id, terrain, probability
        String terrainStr = getAttributeValue(t, TERRAIN);
        if (terrainStr != null) {
            String[] tileIds = terrainStr.split("[\\s]*,[\\s]*");

            assert tileIds.length == 4;

            int terrain = 0;
            for (int i = 0; i < 4; i++) {
                int tid = Integer.parseInt(tileIds[i]);
                terrain |= tid << 8 * (3 - i);
            }

            tile.setTerrain(terrain);
        }

        float probability = (float) getDoubleAttribute(t, PROBABILITY, 0.0);
        tile.setProbability(probability);

        NodeList children = t.getChildNodes();

        Properties props = propertiesLoader.readProperties(t);
        tile.setProperties(props);

        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (IMAGE.equals(child.getNodeName())) {
                TiledImage image = tiledImageLoader.load(child);
                tile.setImage(image);
                // use the tile image size as tile size by default
                if (tile.getWidth() <= 0 && tile.getHeight() <= 0) {
                    tile.setWidth(image.getWidth());
                    tile.setHeight(image.getHeight());
                }

                Material material = image.getMaterial();
                material.setBoolean("UseTilesetImage", true);
                material.setVector4("TileSize", new Vector4f(tile.getWidth(), tile.getHeight(), 0f, 0f));

                tile.setTexture(image.getTexture());
                tile.setMaterial(material);
            } else if (ANIMATION.equals(child.getNodeName())) {
                Animation animation = new Animation(null);
                NodeList frames = child.getChildNodes();
                for (int k = 0; k < frames.getLength(); k++) {
                    Node frameNode = frames.item(k);
                    if (frameNode.getNodeName().equals(FRAME)) {
                        int tileId = getAttribute(frameNode, TILE_ID, 0);
                        int duration = getAttribute(frameNode, DURATION, 0);
                        animation.addFrame(new Frame(tileId, duration));
                    }
                }
                tile.addAnimation(animation);
            }
        }
    }
}