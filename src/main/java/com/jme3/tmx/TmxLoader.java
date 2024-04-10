package com.jme3.tmx;

import com.jme3.asset.*;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Spatial.BatchHint;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.MagFilter;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.texture.Texture2D;
import com.jme3.tmx.animation.AnimatedTileControl;
import com.jme3.tmx.animation.Animation;
import com.jme3.tmx.animation.Frame;
import com.jme3.tmx.core.*;
import com.jme3.tmx.enums.*;
import com.jme3.tmx.render.shape.TileMesh;
import com.jme3.tmx.util.ColorUtil;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

/**
 * Tiled map loader.
 *
 * @author yanmaoyuan
 */
public class TmxLoader implements AssetLoader {

    public static final String TMX_EXTENSION = "tmx";
    public static final String TSX_EXTENSION = "tsx";
    public static final String NAME = "name";
    public static final String VALUE = "value";
    public static final String TYPE = "type";
    public static final String ORIENTATION = "orientation";
    public static final String RENDERORDER = "renderorder";
    public static final String COMPRESSIONLEVEL = "compressionlevel";
    public static final String TILE_WIDTH = "tilewidth";
    public static final String TILE_HEIGHT = "tileheight";
    public static final String MARGIN = "margin";
    public static final String SPACING = "spacing";
    public static final String HEXSIDELNGTH = "hexsidelength";
    public static final String STAGGER_AXIS = "staggeraxis";
    public static final String STAGGER_INDEX = "staggerindex";
    public static final String PARALLAX_ORIGIN_X = "parallaxoriginx";
    public static final String PARALLAX_ORIGIN_Y = "parallaxoriginy";
    public static final String BACKGROUND_COLOR = "backgroundcolor";
    public static final String NEXT_LAYER_ID = "nextlayerid";
    public static final String NEXT_OBJECT_ID = "nextobjectid";
    public static final String INFINITE = "infinite";
    public static final String VERSION = "version";
    public static final String TILEDVERSION = "tiledversion";
    public static final String CLASS = "class";
    public static final String COLOR = "color";
    public static final String TILESET = "tileset";
    public static final String TILE = "tile";
    public static final String SOURCE = "source";
    public static final String IMAGE = "image";
    public static final String WIDTH = "width";
    public static final String HEIGHT = "height";
    public static final String DATA = "data";
    public static final String LAYER = "layer";
    public static final String IMAGELAYER = "imagelayer";
    public static final String OBJECTGROUP = "objectgroup";
    public static final String OBJECT = "object";
    public static final String PROPERTIES = "properties";
    public static final String PROPERTY = "property";
    public static final String POINT = "point";
    public static final String POLYLINE = "polyline";
    public static final String POLYGON = "polygon";
    public static final String ELLIPSE = "ellipse";
    public static final String TEXT = "text";
    public static final String GROUP = "group";
    public static final String WANGSETS = "wangsets";
    public static final String WANGSET = "wangset";
    public static final String WANGCOLOR = "wangcolor";
    public static final String WANGTILE = "wangtile";
    public static final String TERRAINTYPES = "terraintypes";
    public static final String TERRAIN = "terrain";

    static Logger logger = LoggerFactory.getLogger(TmxLoader.class.getName());

    private AssetManager assetManager;
    private AssetKey<?> key;

    private TiledMap map;

    @Override
    public Object load(AssetInfo assetInfo) throws IOException {
        key = assetInfo.getKey();
        assetManager = assetInfo.getManager();

        String extension = key.getExtension();

        switch (extension) {
            case TMX_EXTENSION:
                return loadMap(assetInfo.openStream());
            case TSX_EXTENSION:
                return loadTileSet(assetInfo.openStream());
            default:
                return null;
        }

    }

    /**
     * Load a Map from .tmx file
     *
     * @param inputStream InputStream
     * @return TiledMap
     * @throws IOException
     */
    private TiledMap loadMap(InputStream inputStream) throws IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Document doc;
        try {
            factory.setIgnoringComments(true);
            factory.setIgnoringElementContentWhitespace(true);
            factory.setExpandEntityReferences(false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setEntityResolver(new EntityResolver() {
                @Override
                public InputSource resolveEntity(String publicId, String systemId) {
                    if (systemId.equals("http://mapeditor.org/dtd/1.0/map.dtd")) {
                        return new InputSource(getClass().getResourceAsStream("resources/map.dtd"));
                    }
                    return null;
                }
            });

            InputSource source = new InputSource(inputStream);
            source.setSystemId(key.getFolder());
            source.setEncoding("UTF-8");
            doc = builder.parse(source);
        } catch (SAXException | ParserConfigurationException e) {
            logger.error("Error while parsing map file: {}", key.getName(), e);
            throw new IllegalStateException("Error while parsing map file: " + key.getName());
        }

        try {
            readMap(doc);
        } catch (Exception e) {
            logger.error("Error while parsing map file: {}", key.getName(), e);
        }

        return map;
    }

    /**
     * Load a TileSet from .tsx file.
     *
     * @param inputStream
     * @return
     */
    private Tileset loadTileSet(final InputStream inputStream) {
        Tileset set = null;
        Node tsNode;

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Document doc;
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            doc = builder.parse(inputStream);

            NodeList tsNodeList = doc.getElementsByTagName(TILESET);

            // There can be only one tileset in a .tsx file.
            tsNode = tsNodeList.item(0);

            if (tsNode != null) {
                set = readTileset(tsNode);
                if (set.getSource() != null) {
                    logger.warn("Recursive external tilesets are not supported.{}", set.getSource());
                }
                set.setSource(key.getName());
            }
        } catch (Exception e) {
            logger.error("Failed while loading {}", key.getName(), e);
        }

        return set;
    }

    /**
     * Load TileSet from a ".tsx" file.
     *
     * @param source
     * @return
     */
    private Tileset loadTileSet(final String source) {
        String assetPath = toJmeAssetPath(source);

        // load it with assetManager
        Tileset ext = null;
        try {
            ext = (Tileset) assetManager.loadAsset(assetPath);
        } catch (Exception e) {
            logger.error("Tileset {} was not loaded correctly!", source, e);
        }

        return ext;
    }

    private static String getAttributeValue(Node node, String attribname) {
        final NamedNodeMap attributes = node.getAttributes();
        String value = null;
        if (attributes != null) {
            Node attribute = attributes.getNamedItem(attribname);
            if (attribute != null) {
                value = attribute.getNodeValue();
            }
        }
        return value;
    }

    private static String getAttribute(Node node, String attribname, String def) {
        final String attr = getAttributeValue(node, attribname);
        if (attr != null) {
            return attr;
        } else {
            return def;
        }
    }

    private static int getAttribute(Node node, String attribname, int def) {
        final String attr = getAttributeValue(node, attribname);
        if (attr != null) {
            return Integer.parseInt(attr);
        } else {
            return def;
        }
    }

    private static double getDoubleAttribute(Node node, String attribname, double def) {
        final String attr = getAttributeValue(node, attribname);
        if (attr != null) {
            return Double.parseDouble(attr);
        } else {
            return def;
        }
    }

    private void readMap(Document doc) throws IOException {
        Node item;
        Node mapNode = doc.getDocumentElement();

        if (!"map".equals(mapNode.getNodeName())) {
            throw new IllegalArgumentException("Not a valid tmx map file.");
        }

        // Get the map dimensions and create the map
        int mapWidth = getAttribute(mapNode, WIDTH, 0);
        int mapHeight = getAttribute(mapNode, HEIGHT, 0);

        if (mapWidth > 0 && mapHeight > 0) {
            map = new TiledMap(mapWidth, mapHeight);
        } else {
            // Maybe this map is still using the dimensions element
            NodeList l = doc.getElementsByTagName("dimensions");
            for (int i = 0; (item = l.item(i)) != null; i++) {
                if (item.getParentNode() == mapNode) {
                    mapWidth = getAttribute(item, WIDTH, 0);
                    mapHeight = getAttribute(item, HEIGHT, 0);

                    if (mapWidth > 0 && mapHeight > 0) {
                        map = new TiledMap(mapWidth, mapHeight);
                    }
                }
            }
        }

        if (map == null) {
            logger.warn("Couldn't locate map dimensions.");
            throw new IllegalArgumentException("Couldn't locate map dimensions.");
        }

        // Load other map attributes
        String version = getAttributeValue(mapNode, VERSION);
        String tiledVersion = getAttributeValue(mapNode, TILEDVERSION);
        String clazz = getAttribute(mapNode, CLASS, "");
        String orientation = getAttributeValue(mapNode, ORIENTATION);
        String renderOrder = getAttributeValue(mapNode, RENDERORDER);
        int compressionLevel = getAttribute(mapNode, COMPRESSIONLEVEL, -1);
        int tileWidth = getAttribute(mapNode, TILE_WIDTH, 0);
        int tileHeight = getAttribute(mapNode, TILE_HEIGHT, 0);
        int hexSideLength = getAttribute(mapNode, HEXSIDELNGTH, 0);
        String staggerAxis = getAttributeValue(mapNode, STAGGER_AXIS);
        String staggerIndex = getAttributeValue(mapNode, STAGGER_INDEX);
        int parallaxOriginX = getAttribute(mapNode, PARALLAX_ORIGIN_X, 0);
        int parallaxOriginY = getAttribute(mapNode, PARALLAX_ORIGIN_Y, 0);
        String bgStr = getAttributeValue(mapNode, BACKGROUND_COLOR);
        int nextLayerId = getAttribute(mapNode, NEXT_LAYER_ID, 0);
        int nextObjectId = getAttribute(mapNode, NEXT_OBJECT_ID, 0);
        int infinite = getAttribute(mapNode, INFINITE, 0);

        map.setVersion(version);
        map.setTiledVersion(tiledVersion);
        map.setClazz(clazz);

        if (orientation != null) {
            map.setOrientation(orientation);
        } else {
            map.setOrientation(Orientation.ORTHOGONAL);
        }

        if (renderOrder != null) {
            map.setRenderOrder(renderOrder.toLowerCase());
        } else {
            map.setRenderOrder(RenderOrder.RIGHT_DOWN);
        }

        map.setCompressionLevel(compressionLevel);

        if (tileWidth > 0) {
            map.setTileWidth(tileWidth);
        }
        if (tileHeight > 0) {
            map.setTileHeight(tileHeight);
        }
        if (hexSideLength > 0) {
            map.setHexSideLength(hexSideLength);
        }

        if (staggerAxis != null) {
            map.setStaggerAxis(staggerAxis);
        }

        if (staggerIndex != null) {
            map.setStaggerIndex(staggerIndex);
        }

        map.setParallaxOriginX(parallaxOriginX);
        map.setParallaxOriginY(parallaxOriginY);

        ColorRGBA backgroundColor;
        if (bgStr != null) {
            backgroundColor = ColorUtil.toColorRGBA(bgStr);
            map.setBackgroundColor(backgroundColor);
        }

        if (nextLayerId > 0) {
            map.setNextLayerId(nextLayerId);
        }
        if (nextObjectId > 0) {
            map.setNextObjectId(nextObjectId);
        }
        map.setInfinite(infinite);

        // Load properties
        Properties props = readProperties(mapNode.getChildNodes());
        map.setProperties(props);

        NodeList tileSets = doc.getElementsByTagName(TILESET);
        for (int i = 0; (item = tileSets.item(i)) != null; i++) {
            Tileset set = readTileset(item);
            /*
             * update the visual part of tileset
             */
            createVisual(set);
            map.addTileset(set);
        }

        // Load the layers and objectgroups
        Node child = mapNode.getFirstChild();
        while (child != null) {
            String childName = child.getNodeName();
            switch (childName) {
                case LAYER: {
                    Layer layer = readTileLayer(child);
                    map.addLayer(layer);
                    break;
                }
                case OBJECTGROUP: {
                    Layer layer = readObjectLayer(child);
                    map.addLayer(layer);
                    break;
                }
                case IMAGELAYER: {
                    Layer layer = readImageLayer(child);
                    map.addLayer(layer);
                    break;
                }
                case GROUP: {
                    Layer layer = readGroupLayer(child);
                    map.addLayer(layer);
                    break;
                }
                default: {
                    if (TILESET.equals(childName) || PROPERTIES.equals(childName) || "#text".equals(childName)) {
                        // Ignore, already processed
                    } else {
                        logger.warn("Unsupported map element: {}", childName);
                    }
                    break;
                }
            }
            child = child.getNextSibling();
        }
    }

    /**
     * read tileset
     *
     * Can contain at most one:
     *   <image>, <tileoffset>, <grid> (since 1.0),
     *   <properties>, <terraintypes>, <wangsets> (since 1.1),
     *   <transformations> (since 1.5)
     * Can contain any number :
     *   <tile>
     *
     * @param node node
     * @return Tileset
     */
    private Tileset readTileset(Node node) {

        String source = getAttributeValue(node, SOURCE);
        int firstGid = getAttribute(node, "firstgid", 1);

        if (source != null) {
            Tileset set = loadTileSet(key.getFolder() + source);
            set.setFirstGid(firstGid);
            return set;
        }

        final int tileWidth = getAttribute(node, TILE_WIDTH, map != null ? map.getTileWidth() : 0);
        final int tileHeight = getAttribute(node, TILE_HEIGHT, map != null ? map.getTileHeight() : 0);
        final int tileSpacing = getAttribute(node, SPACING, 0);
        final int tileMargin = getAttribute(node, MARGIN, 0);

        Tileset set = new Tileset(tileWidth, tileHeight, tileSpacing, tileMargin);
        set.setFirstGid(firstGid);

        final String name = getAttributeValue(node, NAME);
        String clazz = getAttribute(node, CLASS, "");
        String objectAlignment = getAttribute(node, "objectalignment", "unspecified");
        String tileRenderSize = getAttribute(node, "tilerendersize", "tile");
        String fillMode = getAttribute(node, "fillmode", "stretch");

        set.setName(name);
        set.setClazz(clazz);

        if (objectAlignment != null) {
            set.setObjectAlignment(objectAlignment);
        } else {
            set.setObjectAlignment(ObjectAlignment.UNSPECIFIED);
        }

        if (tileRenderSize != null) {
            set.setTileRenderSize(tileRenderSize);
        } else {
            set.setTileRenderSize(TileRenderSize.TILE);
        }

        if (fillMode != null) {
            set.setFillMode(fillMode);
        } else {
            set.setFillMode(FillMode.STRETCH);
        }

        boolean hasTilesetImage = false;
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);

            String nodeName = child.getNodeName();
            if (nodeName.equalsIgnoreCase(IMAGE)) {
                if (hasTilesetImage) {
                    logger.warn("Ignoring illegal image element after tileset image.");
                    continue;
                }

                AnImage image = readImage(child);
                if (image.texture != null) {
                    // Not a shared image, but an entire set in one image
                    // file. There should be only one image element in this
                    // case.
                    hasTilesetImage = true;

                    set.setImageSource(image.source);
                    set.setTexture(image.texture);
                    set.setMaterial(image.createMaterial());
                }
            } else if (nodeName.equalsIgnoreCase("grid")) {
                /*
                 * This element is only used in case of isometric orientation,
                 * and determines how tile overlays for terrain and collision
                 * information are rendered.
                 */
                String orientation = getAttribute(node, ORIENTATION, "orthogonal");
                Orientation gridOrientation = Orientation.fromString(orientation);
                int gridWidth = getAttribute(node, WIDTH, 0);
                int gridHeight = getAttribute(node, HEIGHT, 0);
                set.setGrid(gridOrientation, gridWidth, gridHeight);
            } else if (nodeName.equalsIgnoreCase(TERRAINTYPES)) {
                NodeList terrainTypes = child.getChildNodes();
                for (int k = 0; k < terrainTypes.getLength(); k++) {
                    Node terrainNode = terrainTypes.item(k);
                    if (terrainNode.getNodeName().equalsIgnoreCase(TERRAIN)) {
                        set.addTerrain(readTerrain(terrainNode));
                    }
                }
            } else if (nodeName.equalsIgnoreCase(TILE)) {
                readTile(set, child);
            } else if (nodeName.equalsIgnoreCase("tileoffset")) {
                /*
                 * This element is used to specify an offset in pixels, to be
                 * applied when drawing a tile from the related tileset. When
                 * not present, no offset is applied.
                 */
                final int tileOffsetX = getAttribute(child, "x", 0);
                final int tileOffsetY = getAttribute(child, "y", 0);

                set.setTileOffset(tileOffsetX, tileOffsetY);
            } else if (nodeName.equalsIgnoreCase("transformations")) {
                // This element is used to describe which transformations can be applied to the tiles
                // (e.g. to extend a Wang set by transforming existing tiles).
                // Whether the tiles in this set can be flipped horizontally (default 0)
                int hflip = getAttribute(node, "hflip", 0);
                // Whether the tiles in this set can be flipped vertically (default 0)
                int vflip = getAttribute(node, "vflip", 0);
                // Whether the tiles in this set can be rotated in 90 degree increments (default 0)
                int rotate = getAttribute(node, "rotate", 0);
                // Whether untransformed tiles remain preferred, otherwise transformed tiles are used to produce more variations (default 0)
                int preferUntransformed = getAttribute(node, "preferuntransformed", 0);
                set.setTransformations(new Transformations(hflip, vflip, rotate, preferUntransformed));
            } else if (nodeName.equalsIgnoreCase(WANGSETS)) {
                NodeList wangSets = child.getChildNodes();
                for (int k = 0; k < wangSets.getLength(); k++) {
                    Node wangSetNode = wangSets.item(k);
                    if (wangSetNode.getNodeName().equalsIgnoreCase(WANGSET)) {
                        set.addWangSet(readWangSet(wangSetNode));
                    }
                }
            }
        }

        return set;
    }

    /**
     * Create the visual part for every tile of a given Tileset.
     *
     * @param tileset the Tileset
     * @return
     */
    private void createVisual(Tileset tileset) {

        Texture texture = tileset.getTexture();
        Material sharedMat = null;
        Image image = null;
        /**
         * If this tileset has a texture, means that most of the tiles are share
         * the same TextureAltas, I just need to apply the shared material to
         * their visual part.
         *
         * Some tiles like "Player" or "Monster" maybe use their own texture to
         * perform animation, should be handled differently. Such as create a
         * com.jme3.scene.Node instead of com.jme3.scene.Geometry for them, and
         * create a Control to make them animated.
         *
         */
        boolean hasSharedImage = texture != null;

        if (hasSharedImage) {
            image = texture.getImage();
            sharedMat = tileset.getMaterial();
        }

        int offsetX = tileset.getTileOffsetX();
        int offsetY = tileset.getTileOffsetY();

        // if the tileset tilesize is larger than the map tilesize, adjust the offset
        int diffY = tileset.getTileHeight() - map.getTileHeight();
        if (diffY > 0) {
            offsetY = offsetY - diffY;
        }

        List<Tile> tiles = tileset.getTiles();
        int len = tiles.size();
        for (int i = 0; i < len; i++) {
            Tile tile = tiles.get(i);

            String name = "tile#" + tileset.getFirstGid() + "#" + tile.getId();

            /**
             * If the tile has a texture, means that it don't use the shared
             * material.
             */
            boolean useSharedImage = tile.getTexture() == null;
            if (!useSharedImage && tile.getMaterial() == null) {
                // this shouldn't happen, just in case someone uses Tiles created by code.
                logger.warn("The tile mush has a material if it don't use sharedImage: {}", name);
                continue;
            }

            int x = tile.getX();
            int y = tile.getY();
            int width = tile.getWidth();
            int height = tile.getHeight();

            int imageWidth;
            int imageHeight;
            if (useSharedImage) {
                imageWidth = image.getWidth();
                imageHeight = image.getHeight();
            } else {
                imageWidth = tile.getTexture().getImage().getWidth();
                imageHeight = tile.getTexture().getImage().getHeight();
            }

            TileMesh mesh = new TileMesh(x, y, width, height, imageWidth, imageHeight, offsetX, offsetY);

            Geometry geometry = new Geometry(name, mesh);
            geometry.setQueueBucket(Bucket.Gui);

            if (useSharedImage) {
                geometry.setMaterial(sharedMat);
            } else {
                geometry.setMaterial(tile.getMaterial());
            }

            if (tile.isAnimated()) {
                geometry.setBatchHint(BatchHint.Never);

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
        final String name = getAttributeValue(node, "name");
        final int tile = getAttribute(node, "tile", -1);

        Terrain terrain = new Terrain(name);
        terrain.setTile(tile);

        // read properties
        Properties props = readProperties(node.getChildNodes());
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
        String clazz = getAttribute(node, TYPE, "");
        int tile = getAttribute(node, TILE, -1);

        WangSet wangSet = new WangSet(name);
        wangSet.setClazz(clazz);
        wangSet.setTile(tile);

        // read properties
        Properties props = readProperties(node.getChildNodes());
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
        String clazz = getAttribute(node, CLASS, "");
        String color = getAttributeValue(node, COLOR);
        int tile = getAttribute(node, TILE, -1);
        float probability = (float) getDoubleAttribute(node, "probability", 0.0);

        WangColor wangColor = new WangColor();
        wangColor.setName(name);
        wangColor.setClazz(clazz);
        wangColor.setColor(ColorUtil.toColorRGBA(color));
        wangColor.setTile(tile);
        wangColor.setProbability(probability);

        // read properties
        Properties props = readProperties(node.getChildNodes());
        wangColor.setProperties(props);

        return wangColor;
    }

    private WangTile readWangTile(Node node) {
        int tileId = getAttribute(node, "tileid", -1);
        String wangId = getAttribute(node, "wangid", "");
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

        int id = getAttribute(t, "id", -1);

        if (!set.isSetFromImage() || id > set.getMaxTileId()) {
            tile = new Tile();
            tile.setId(id);
            tile.setWidth(set.getTileWidth());
            tile.setHeight(set.getTileHeight());

            set.addTile(tile);
        } else {
            tile = set.getTile(id);
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

        float probability = (float) getDoubleAttribute(t, "probability", 0.0);
        tile.setProbability(probability);

        NodeList children = t.getChildNodes();

        Properties props = readProperties(children);
        tile.setProperties(props);

        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (IMAGE.equalsIgnoreCase(child.getNodeName())) {
                AnImage image = readImage(child);
                tile.setTexture(image.texture);
                tile.setMaterial(image.createMaterial());
            } else if ("animation".equalsIgnoreCase(child.getNodeName())) {
                Animation animation = new Animation(null);
                NodeList frames = child.getChildNodes();
                for (int k = 0; k < frames.getLength(); k++) {
                    Node frameNode = frames.item(k);
                    if (frameNode.getNodeName().equalsIgnoreCase("frame")) {
                        int tileId = getAttribute(frameNode, "tileid", 0);
                        int duration = getAttribute(frameNode, "duration", 0);
                        animation.addFrame(new Frame(tileId, duration));
                    }
                }
                tile.addAnimation(animation);
            }
        }
    }

    /**
     * load a image from file or decode from the data elements.
     * <p>
     * Note that it is not currently possible to use Tiled to create maps with
     * embedded image data, even though the TMX format supports this. It is
     * possible to create such maps using libtiled (Qt/C++) or tmxlib (Python).
     *
     * @param t
     * @return
     * @throws IOException
     */
    private AnImage readImage(Node t) {

        AnImage image = new AnImage();

        String source = getAttributeValue(t, SOURCE);

        // load a image from file or decode from the CDATA.
        if (source != null) {
            String assetPath = toJmeAssetPath(key.getFolder() + source);
            image.source = assetPath;
            image.texture = loadTexture2D(assetPath);
        } else {
            NodeList nl = t.getChildNodes();
            for (int i = 0; i < nl.getLength(); i++) {
                Node node = nl.item(i);
                if (DATA.equals(node.getNodeName())) {
                    Node cdata = node.getFirstChild();
                    if (cdata != null) {
                        String sdata = cdata.getNodeValue();
                        byte[] imageData = Base64.getDecoder().decode(sdata.trim());

                        image.texture = loadTexture2D(imageData);
                    }
                    break;
                }
            }
        }

        image.trans = getAttributeValue(t, "trans");
        // useless for jme3
        image.format = getAttributeValue(t, "format");
        image.width = getAttribute(t, WIDTH, 0);
        image.height = getAttribute(t, HEIGHT, 0);

        return image;

    }

    /**
     * read the common part of a Layer
     *
     * @param node
     * @param layer
     */
    private void readLayerBase(Node node, Layer layer) {
        String id = getAttributeValue(node, "id");
        if (id != null) {
            layer.setId(Integer.parseInt(id));
        }

        final String name = getAttributeValue(node, NAME);
        String clazz = getAttribute(node, CLASS, "");
        double opacity = getDoubleAttribute(node, "opacity", 1.0);
        boolean visible = getAttribute(node, "visible", 1) == 1;
        boolean locked = getAttribute(node, "locked", 0) == 1;
        String tintColor = getAttributeValue(node, "tintcolor");
        int offsetX = getAttribute(node, "offsetx", 0);
        int offsetY = getAttribute(node, "offsety", 0);
        float parallaxX = (float) getDoubleAttribute(node, "parallaxx", 1.0);
        float parallaxY = (float) getDoubleAttribute(node, "parallaxy", 1.0);

        layer.setName(name);
        layer.setClazz(clazz);
        layer.setOpacity(opacity);

        if (tintColor != null) {
            layer.setTintColor(ColorUtil.toColorRGBA(tintColor));
        }

        // This is done at the end, otherwise the offset is applied during
        // the loading of the tiles.
        layer.setOffset(offsetX, offsetY);

        // The parallax scrolling factor determines the amount by which the layer
        // moves in relation to the camera.
        layer.setParallaxFactor(parallaxX, parallaxY);

        // Invisible layers are automatically locked, so it is important to
        // set the layer to potentially invisible _after_ the layer data is
        // loaded.
        layer.setVisible(visible);

        layer.setLocked(locked);

        // read properties
        Properties props = readProperties(node.getChildNodes());
        layer.setProperties(props);
    }

    /**
     * Loads a map layer from a layer node.
     *
     * @param node the node representing the "layer" element
     * @return the loaded map layer
     * @throws Exception
     */
    private Layer readTileLayer(Node node) throws IOException{
        final int layerWidth = getAttribute(node, WIDTH, map.getWidth());
        final int layerHeight = getAttribute(node, HEIGHT, map.getHeight());

        TileLayer layer = new TileLayer(layerWidth, layerHeight);

        readLayerBase(node, layer);

        Node child = node.getFirstChild();
        while (child != null) {
            String nodeName = child.getNodeName();
            if (DATA.equalsIgnoreCase(nodeName)) {
                readData(layer, child);
            } else if ("tileproperties".equalsIgnoreCase(nodeName)) {
                readTileProperties(layer, child);
            }
            child = child.getNextSibling();
        }

        return layer;
    }

    private void readData(TileLayer layer, Node node) throws IOException {
        String encoding = getAttributeValue(node, "encoding");
        String comp = getAttributeValue(node, "compression");

        if ("base64".equalsIgnoreCase(encoding)) {
            decodeBase64Data(layer, node, comp);
        } else if ("csv".equalsIgnoreCase(encoding)) {
            if (comp != null && !comp.isEmpty()) {
                throw new IOException("Unrecognized compression method [" + comp + "] for map layer " + layer.getName() + " and encoding " + encoding);
            }
            decodeCsvData(layer, node);
        } else {
            decodeTileData(layer, node);
        }

        // read chunks
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            String nodeName = child.getNodeName();
            if ("chunk".equalsIgnoreCase(nodeName)) {
                readChunk(layer, child);
            }
        }
    }

    /**
     * Get the InputStream for the data element.
     *
     * @param layer
     * @param compression
     * @param decode
     * @return
     * @throws IOException
     */
    private InputStream getInputStream(TileLayer layer, String compression, byte[] decode) throws IOException {
        InputStream is;
        if ("gzip".equalsIgnoreCase(compression)) {
            final int len = layer.getWidth() * layer.getHeight() * 4;
            is = new GZIPInputStream(new ByteArrayInputStream(decode), len);
        } else if ("zlib".equalsIgnoreCase(compression)) {
            is = new InflaterInputStream(new ByteArrayInputStream(decode));
        } else if (compression != null && !compression.isEmpty()) {
            throw new IOException("Unrecognized compression method [" + compression + "] for map layer " + layer.getName());
        } else {
            is = new ByteArrayInputStream(decode);
        }
        return is;
    }

    private void decodeBase64Data(TileLayer layer, Node node, String compression) throws IOException {
        Node cdata = node.getFirstChild();
        if (cdata != null) {
            byte[] dec = Base64.getDecoder().decode(cdata.getNodeValue().trim());

            InputStream is = getInputStream(layer, compression, dec);

            for (int y = 0; y < layer.getHeight(); y++) {
                for (int x = 0; x < layer.getWidth(); x++) {
                    int tileId = 0;
                    tileId |= is.read();
                    tileId |= is.read() << 8;
                    tileId |= is.read() << 16;
                    tileId |= is.read() << 24;

                    map.setTileAtFromTileId(layer, x, y, tileId);
                }
            }
        }
    }

    private void decodeCsvData(TileLayer layer, Node node) throws IOException {
        String csvText = node.getTextContent();

        /*
         * trim 'space', 'tab', 'newline'. pay attention to
         * additional unicode chars like \u2028, \u2029, \u0085 if
         * necessary
         */
        String[] csvTileIds = csvText.trim().split("[\\s]*,[\\s]*");

        if (csvTileIds.length != layer.getHeight() * layer.getWidth()) {
            throw new IOException("Number of tiles does not match the layer's width and height");
        }

        for (int y = 0; y < layer.getHeight(); y++) {
            for (int x = 0; x < layer.getWidth(); x++) {
                String sTileId = csvTileIds[x + y * layer.getWidth()];
                int tileId = Integer.parseInt(sTileId);
                map.setTileAtFromTileId(layer, x, y, tileId);
            }
        }
    }

    private void decodeTileData(TileLayer layer, Node node) {
        int x = 0;
        int y = 0;
        Node child = node.getFirstChild();
        while (child != null) {
            if (TILE.equalsIgnoreCase(child.getNodeName())) {
                int tileId = getAttribute(child, "gid", -1);
                map.setTileAtFromTileId(layer, x, y, tileId);

                x++;
                if (x == layer.getWidth()) {
                    x = 0;
                    y++;
                }
                if (y == layer.getHeight()) {
                    break;
                }
            }
            child = child.getNextSibling();
        }
    }
    /**
     * This is currently added only for infinite maps. The contents of a chunk element is
     * same as that of the data element, except it stores the data of the area specified
     * in the attributes.
     *
     * Can contain any number: &lt;tile>
     * @param layer
     * @param node
     */
    private void readChunk(TileLayer layer, Node node) {
        int x = getAttribute(node, "x", 0);
        int y = getAttribute(node, "y", 0);
        int width = getAttribute(node, WIDTH, 0);
        int height = getAttribute(node, HEIGHT, 0);

        Chunk chunk = new Chunk(x, y, width, height);

        if (node.hasChildNodes()) {
            int ix = 0;
            int iy = 0;
            Node child = node.getFirstChild();
            while (child != null) {
                if ("tile".equalsIgnoreCase(child.getNodeName())) {
                    // Not to be confused with the tile element inside a tileset,
                    // this element defines the value of a single tile on a tile layer.
                    // This is however the most inefficient way of storing the tile
                    // layer data, and should generally be avoided.
                    int tileId = getAttribute(child, "gid", -1);
                    // TODO need to get some samples to test this
                    Tile tile = map.getTileForTileGID(tileId);
                    chunk.setTileAt(ix, iy, tile);
                    ix++;
                    if (ix == width) {
                        ix = 0;
                        iy++;
                    }
                    if (iy == height) {
                        break;
                    }
                }
                child = child.getNextSibling();
            }
        } else {
            for (int iy = 0; iy < height; iy++) {
                for (int ix = 0; ix < width; ix++) {
                    Tile tile = layer.getTileAt(x + ix, y + iy);
                    chunk.setTileAt(ix, iy, tile);
                }
            }
        }

        layer.addChunk(chunk);
    }

    private void readTileProperties(TileLayer layer, Node node) {
        Node child = node.getFirstChild();
        while (child != null) {
            if (TILE.equalsIgnoreCase(child.getNodeName())) {
                int x = getAttribute(child, "x", -1);
                int y = getAttribute(child, "y", -1);

                Properties tip = readProperties(child.getChildNodes());
                layer.setTileInstancePropertiesAt(x, y, tip);
            }
            child = child.getNextSibling();
        }
    }

    /**
     * read ImageLayer
     *
     * @param node
     * @return
     * @throws Exception
     */
    private Layer readImageLayer(Node node) {
        int width = getAttribute(node, WIDTH, map.getWidth());
        int height = getAttribute(node, HEIGHT, map.getHeight());
        boolean repeatX = getAttribute(node, "repeatx", 0) == 1;
        boolean repeatY = getAttribute(node, "repeaty", 0) == 1;

        ImageLayer layer = new ImageLayer(width, height);
        readLayerBase(node, layer);
        layer.setRepeatX(repeatX);
        layer.setRepeatY(repeatY);

        boolean hasImage = false;
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);

            String nodeName = child.getNodeName();
            if (nodeName.equalsIgnoreCase(IMAGE)) {

                AnImage image = readImage(child);
                if (image.texture != null) {
                    layer.setSource(image.source);
                    layer.setTexture(image.texture);
                    layer.setMaterial(image.createMaterial());

                    hasImage = true;
                    break;
                }
            }
        }

        if (!hasImage) {
            logger.warn("ImageLayer {} has no image", layer.getName());
            throw new IllegalArgumentException("ImageLayer " + layer.getName() + " has no image");
        }

        return layer;
    }

    private Layer readObjectLayer(Node node) {

        final int width = getAttribute(node, WIDTH, map.getWidth());
        final int height = getAttribute(node, HEIGHT, map.getHeight());

        ObjectGroup layer = new ObjectGroup(width, height);
        readLayerBase(node, layer);

        final String color = getAttributeValue(node, COLOR);
        final ColorRGBA borderColor;
        if (color != null) {
            borderColor = ColorUtil.toColorRGBA(color);
        } else {
            borderColor = ColorRGBA.LightGray.clone();
        }
        layer.setColor(borderColor);

        /**
         * This material applies to the shapes in this ObjectGroup using
         * LineMesh
         */
        Material mat = new Material(assetManager, "com/jme3/tmx/resources/Tiled.j3md");
        mat.setColor("Color", borderColor);
        layer.setMaterial(mat);

        final String drawOrder = getAttributeValue(node, "draworder");
        if (drawOrder != null) {
            layer.setDrawOrder(drawOrder);
        }

        // Add all objects from the objects group
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (OBJECT.equalsIgnoreCase(child.getNodeName())) {
                MapObject obj = readObjectNode(child);
                layer.add(obj);
            }
        }

        return layer;
    }

    /**
     * Read an object of the ObjectGroup.
     *
     * @param node
     * @return
     * @throws Exception
     */
    private MapObject readObjectNode(Node node) {
        int id = getAttribute(node, "id", 0);
        String name = getAttributeValue(node, "name");
        String type = getAttributeValue(node, "type");
        double x = getDoubleAttribute(node, "x", 0);
        double y = getDoubleAttribute(node, "y", 0);
        double width = getDoubleAttribute(node, WIDTH, 0);
        double height = getDoubleAttribute(node, HEIGHT, 0);
        double rotation = getDoubleAttribute(node, "rotation", 0);
        String gid = getAttributeValue(node, "gid");
        int visible = getAttribute(node, "visible", 1);
        String template = getAttributeValue(node, "template");
        // TODO need some samples to figure out how template works.

        MapObject obj = new MapObject(x, y, width, height);
        obj.setId(id);
        obj.setRotation(rotation);
        obj.setTemplate(template);
        obj.setVisible(visible == 1);
        if (name != null) {
            obj.setName(name);
        }
        if (type != null) {
            obj.setType(type);
        }

        Properties props = readProperties(node.getChildNodes());
        obj.setProperties(props);

        /*
         * if an object have "gid" attribute means it references to a tile.
         */
        if (gid != null) {
            obj.setShape(ObjectType.TILE);

            int gidValue = (int) Long.parseLong(gid);

            // clear the flag
            gidValue = gidValue & 0x1FFFFFFF;

            Tile tile = map.getTileForTileGID(gidValue);

            Tile t = tile.clone();
            t.setGid(gidValue);
            obj.setTile(t);
        }

        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            String nodeName = child.getNodeName();
            // 把if-else 改写成 switch-case
            switch (nodeName) {
                case ELLIPSE: {
                    obj.setShape(ObjectType.ELLIPSE);
                    break;
                }
                case POINT: {
                    obj.setShape(ObjectType.POINT);
                    break;
                }
                case POLYGON: {
                    obj.setShape(ObjectType.POLYGON);
                    obj.setPoints(readPoints(child));
                    break;
                }
                case POLYLINE: {
                    obj.setShape(ObjectType.POLYLINE);
                    obj.setPoints(readPoints(child));
                    break;
                }
                case TEXT: {
                    obj.setShape(ObjectType.TEXT);
                    obj.setTextData(readTextObject(child));
                    break;
                }
                case IMAGE: {
                    obj.setShape(ObjectType.IMAGE);
                    AnImage image = readImage(child);
                    obj.setImageSource(image.source);
                    obj.setTexture(image.texture);
                    obj.setMaterial(image.createMaterial());
                    break;
                }
                default: {
                    if ("#text".equals(nodeName)) {
                        // ignore
                    } else {
                        logger.warn("unknown object type:{}", nodeName);
                    }
                    break;
                }
            }
        }

        return obj;
    }

    /**
     * Read points of a polygon or polyline
     *
     * @param child the node containing the points
     * @return
     */
    private List<Vector2f> readPoints(Node child) {
        List<Vector2f> points = new ArrayList<>();
        final String pointsAttribute = getAttributeValue(child, "points");
        StringTokenizer st = new StringTokenizer(pointsAttribute, ", ");
        while (st.hasMoreElements()) {
            Vector2f p = new Vector2f();
            p.x = Float.parseFloat(st.nextToken());
            p.y = Float.parseFloat(st.nextToken());

            points.add(p);
        }

        return points;
    }

    private ObjectText readTextObject(Node node) {
        String fontFamily = getAttribute(node, "fontfamily", "sans-serif");
        int pixelSize = getAttribute(node, "pixelsize", 16);
        boolean wrap = getAttribute(node, "wrap", 0) == 1;
        String color = getAttributeValue(node, COLOR);
        boolean bold = getAttribute(node, "bold", 0) == 1;
        boolean italic = getAttribute(node, "italic", 0) == 1;
        boolean underline = getAttribute(node, "underline", 0) == 1;
        boolean strikeout = getAttribute(node, "strikeout", 0) == 1;
        boolean kerning = getAttribute(node, "kerning", 1) == 1;
        String horizontalAlignment = getAttribute(node, "halign", "left");// Left, Center, Right, Justify
        String verticalAlignment = getAttribute(node, "valign", "top");// Top, Center, Bottom
        String text = node.getTextContent();

        ObjectText objectText = new ObjectText(text);
        objectText.setFontFamily(fontFamily);
        objectText.setPixelSize(pixelSize);
        objectText.setWrap(wrap);
        if (color != null) {
            objectText.setColor(ColorUtil.toColorRGBA(color));
        }
        objectText.setBold(bold);
        objectText.setItalic(italic);
        objectText.setUnderline(underline);
        objectText.setStrikeout(strikeout);
        objectText.setKerning(kerning);
        objectText.setHorizontalAlignment(horizontalAlignment);
        objectText.setVerticalAlignment(verticalAlignment);

        return objectText;
    }

    private GroupLayer readGroupLayer(Node node) throws IOException {
        GroupLayer groupLayer = new GroupLayer();
        readLayerBase(node, groupLayer);
        groupLayer.setMap(map);

        Node child = node.getFirstChild();
        while (child != null) {
            switch (child.getNodeName()) {
                case LAYER: {
                    Layer layer = readTileLayer(child);
                    groupLayer.addLayer(layer);
                    break;
                }
                case OBJECTGROUP: {
                    Layer layer = readObjectLayer(child);
                    groupLayer.addLayer(layer);
                    break;
                }
                case IMAGELAYER: {
                    Layer layer = readImageLayer(child);
                    groupLayer.addLayer(layer);
                    break;
                }
                case GROUP: {
                    Layer layer = readGroupLayer(child);
                    groupLayer.addLayer(layer);
                    break;
                }
                default: {
                    logger.warn("unknown layer type:{}", child.getNodeName());
                    break;
                }
            }
            child = child.getNextSibling();
        }
        return groupLayer;
    }

    /**
     * Reads properties from amongst the given children. When a "properties"
     * element is encountered, it recursively calls itself with the children of
     * this node. This function ensures backward compatibility with tmx version
     * 0.99a.
     * <p>
     * Support for reading property values stored as character data was added in
     * Tiled 0.7.0 (tmx version 0.99c).
     *
     * @param children the children amongst which to find properties
     */
    private Properties readProperties(NodeList children) {
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (PROPERTIES.equals(child.getNodeName())) {
                Properties props = new Properties();
                readProperty(child, props);
                return props;
            }
        }

        return null;
    }

    /**
     * read every property in a properties
     *
     * @param node
     * @param props
     */
    private void readProperty(Node node, Properties props) {
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (PROPERTY.equalsIgnoreCase(child.getNodeName())) {
                final String keyName = getAttributeValue(child, NAME);
                String value = getAttributeValue(child, VALUE);
                if (value == null) {
                    Node grandChild = child.getFirstChild();
                    if (grandChild != null) {
                        value = grandChild.getNodeValue();
                        if (value != null) {
                            value = value.trim();
                        }
                    }
                }

                if (value != null) {
                    Object val = value;

                    /*
                     * type can be as follows:
                     * file: stored as paths relative from the location of the map file. (since 0.17)
                     *
                     * object: can reference any object on the same map and are stored as an integer (the ID of
                     * the referenced object, or 0 when no object is referenced). When used on objects in the
                     * Tile Collision Editor, they can only refer to other objects on the same tile. (since 1.4)
                     *
                     * class: will have their member values stored in a nested <properties> element. Only the
                     * actually set members are saved. When no members have been set the properties element is
                     * left out entirely.(since 1.8)
                     */
                    final String type = getAttribute(child, TYPE, "string");
                    switch (type) {
                        // string (default) (since 0.16)
                        case "string":
                            break;
                        // a int value (since 0.16)
                        case "int":
                            val = (int) Long.parseLong(value);
                            break;
                        // a float value (since 0.16)
                        case "float":
                            val = Float.parseFloat(value);
                            break;
                        // has a value of either "true" or "false". (since 0.16)
                        case "bool":
                            val = Boolean.parseBoolean(value);
                            break;
                        // stored in the format #AARRGGBB. (since 0.17)
                        case COLOR:
                            val = ColorUtil.toColorRGBA(value);
                            break;
                        // stored as paths relative from the location of the map file. (since 0.17)
                        case "file":
                            val = toJmeAssetPath(this.key.getFolder() + value);
                            break;
                        // can reference any object on the same map and are stored as an integer
                        // (the ID of the referenced object, or 0 when no object is referenced).
                        // When used on objects in the Tile Collision Editor, they can only refer
                        // to other objects on the same tile. (since 1.4)
                        case OBJECT:
                            // Don't know the usage of this type, so I just convert that value to an int
                            val = Integer.parseInt(value);
                            break;
                        // will have their member values stored in a nested <properties> element.
                        // Only the actually set members are saved. When no members have been set
                        // the properties element is left out entirely. (since 1.8)
                        case CLASS:
                            // TODO not support yet. I need a example to test.
                            break;
                        default:
                            logger.warn("unknown type:{}", type);
                            break;
                    }

                    props.put(keyName, val);
                }
            }
        }
    }

    /**
     * Load a Texture from source
     *
     * @param source
     * @return
     */
    private Texture2D loadTexture2D(final String source) {
        Texture2D tex = null;
        try {
            TextureKey texKey = new TextureKey(source, true);
            texKey.setGenerateMips(false);
            tex = (Texture2D) assetManager.loadTexture(texKey);
            tex.setWrap(WrapMode.Repeat);
            tex.setMagFilter(MagFilter.Nearest);
        } catch (Exception e) {
            logger.error("Can't load texture {}", source, e);
        }

        return tex;
    }

    private Texture2D loadTexture2D(final byte[] data) {
        Class<?> loaderClass = null;
        Object loaderInstance = null;
        Method loadMethod = null;

        try {
            // try Desktop first
            loaderClass = Class.forName("com.jme3.texture.plugins.AWTLoader");
        } catch (ClassNotFoundException e) {
            logger.info("Can't find AWTLoader.");

            try {
                // then try Android Native Image Loader
                loaderClass = Class
                        .forName("com.jme3.texture.plugins.AndroidNativeImageLoader");
            } catch (ClassNotFoundException e1) {
                logger.info("Can't find AndroidNativeImageLoader.");

                try {
                    // then try Android BufferImage Loader
                    loaderClass = Class
                            .forName("com.jme3.texture.plugins.AndroidBufferImageLoader");
                } catch (ClassNotFoundException e2) {
                    logger.info("Can't find AndroidNativeImageLoader.");
                }
            }
        }

        if (loaderClass == null) {
            return null;
        } else {
            // try Desktop first
            try {
                loaderInstance = loaderClass.getConstructor().newInstance();
                loadMethod = loaderClass.getMethod("load", AssetInfo.class);
            } catch (ReflectiveOperationException e) {
                logger.error("Can't find loader class.", e);
                throw new IllegalArgumentException("Can't find AWTLoader.");
            }
        }

        TextureKey texKey = new TextureKey();
        AssetInfo info = new AssetInfo(assetManager, texKey) {
            public InputStream openStream() {
                return new ByteArrayInputStream(data);
            }
        };

        Texture2D tex = null;
        try {
            Image img = (Image) loadMethod.invoke(loaderInstance, info);

            tex = new Texture2D();
            tex.setWrap(WrapMode.Repeat);
            tex.setMagFilter(MagFilter.Nearest);
            tex.setAnisotropicFilter(texKey.getAnisotropy());
            tex.setName(texKey.getName());
            tex.setImage(img);
        } catch (Exception e) {
            logger.error("Can't load texture from byte array", e);
        }

        return tex;

    }

    /**
     * Utilities method to correct the asset path.
     *
     * @param src
     * @return
     */
    private String toJmeAssetPath(final String src) {

        /*
         * 1st: try to locate it with assetManager. No need to handle the src
         * path unless assetManager can't locate it.
         */
        if (assetManager.locateAsset(new AssetKey<>(src)) != null) {
            return src;
        }

        /*
         * 2nd: In JME I suppose that all the files needed are in the same
         * folder, that's why I cut the filename and contact it to
         * key.getFolder().
         */
        String dest = src.replace("\\\\", "/");
        int idx = dest.lastIndexOf("/");
        if (idx >= 0) {
            dest = key.getFolder() + src.substring(idx + 1);
        } else {
            dest = key.getFolder() + dest;
        }

        /*
         * 3rd: try to locate it again.
         */
        if (assetManager.locateAsset(new AssetKey<>(dest)) != null) {
            return dest;
        } else {
            throw new IllegalArgumentException("Can't locate asset: " + src);
        }
    }

    /**
     * When read a &lt;image&gt; element there 5 attribute there. This class is
     * just a data struct to return the whole image node;
     *
     * @author yanmaoyuan
     */
    private class AnImage {
        String source;
        String trans;
        // useless for jme3
        String format;
        int width;
        int height;

        Texture2D texture = null;

        private Material createMaterial() {
            Material mat = new Material(assetManager, "com/jme3/tmx/resources/Tiled.j3md");
            mat.setTexture("ColorMap", texture);
            if (trans != null) {
                ColorRGBA transparentColor = ColorUtil.toColorRGBA(trans);
                mat.setColor("TransColor", transparentColor);
            }
            return mat;
        }
    }

}
