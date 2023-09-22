package com.jme3.tmx;

import com.jme3.asset.*;
import com.jme3.material.Material;
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
import com.jme3.tmx.core.ObjectNode.ObjectType;
import com.jme3.tmx.core.TiledMap.Orientation;
import com.jme3.tmx.core.TiledMap.RenderOrder;
import com.jme3.tmx.util.ColorUtil;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

/**
 * Tiled map loader.
 *
 * @author yanmaoyuan
 */
public class TmxLoader implements AssetLoader {

    static Logger logger = Logger.getLogger(TmxLoader.class.getName());

    private AssetManager assetManager;
    private AssetKey<?> key;

    private TiledMap map;

    @Override
    public Object load(AssetInfo assetInfo) throws IOException {
        key = assetInfo.getKey();
        assetManager = assetInfo.getManager();

        String extension = key.getExtension();

        switch (extension) {
            case "tmx":
                return loadMap(assetInfo.openStream());
            case "tsx":
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
        } catch (SAXException e) {
            e.printStackTrace();
            throw new RuntimeException("Error while parsing map file: " + e);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            return null;
        }

        try {
            readMap(doc);
        } catch (Exception e) {
            e.printStackTrace();
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

            NodeList tsNodeList = doc.getElementsByTagName("tileset");

            // There can be only one tileset in a .tsx file.
            tsNode = tsNodeList.item(0);

            if (tsNode != null) {
                set = readTileset(tsNode);
                if (set.getSource() != null) {
                    logger.warning("Recursive external tilesets are not supported."
                            + set.getSource());
                }
                set.setSource(key.getName());
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed while loading " + key.getName(), e);
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
            logger.log(Level.WARNING, "Tileset " + source + " was not loaded correctly!", e);
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

    private static int getAttribute(Node node, String attribname, int def) {
        final String attr = getAttributeValue(node, attribname);
        if (attr != null) {
            return Integer.parseInt(attr);
        } else {
            return def;
        }
    }

    private static double getDoubleAttribute(Node node, String attribname,
                                             double def) {
        final String attr = getAttributeValue(node, attribname);
        if (attr != null) {
            return Double.parseDouble(attr);
        } else {
            return def;
        }
    }

    private void readMap(Document doc) throws Exception {
        Node item, mapNode;

        mapNode = doc.getDocumentElement();

        if (!"map".equals(mapNode.getNodeName())) {
            throw new Exception("Not a valid tmx map file.");
        }

        // Get the map dimensions and create the map
        int mapWidth = getAttribute(mapNode, "width", 0);
        int mapHeight = getAttribute(mapNode, "height", 0);

        if (mapWidth > 0 && mapHeight > 0) {
            map = new TiledMap(mapWidth, mapHeight);
        } else {
            // Maybe this map is still using the dimensions element
            NodeList l = doc.getElementsByTagName("dimensions");
            for (int i = 0; (item = l.item(i)) != null; i++) {
                if (item.getParentNode() == mapNode) {
                    mapWidth = getAttribute(item, "width", 0);
                    mapHeight = getAttribute(item, "height", 0);

                    if (mapWidth > 0 && mapHeight > 0) {
                        map = new TiledMap(mapWidth, mapHeight);
                    }
                }
            }
        }

        if (map == null) {
            logger.warning("Couldn't locate map dimensions.");
            throw new RuntimeException("Couldn't locate map dimensions.");
        }

        // Load other map attributes
        String orientation = getAttributeValue(mapNode, "orientation");
        String renderorder = getAttributeValue(mapNode, "renderorder");
        int tileWidth = getAttribute(mapNode, "tilewidth", 0);
        int tileHeight = getAttribute(mapNode, "tileheight", 0);
        int hexsidelength = getAttribute(mapNode, "hexsidelength", 0);
        String staggerAxis = getAttributeValue(mapNode, "staggeraxis");
        String staggerIndex = getAttributeValue(mapNode, "staggerindex");
        String bgStr = getAttributeValue(mapNode, "backgroundcolor");

        if (orientation != null) {
            map.setOrientation(orientation.toUpperCase());
        } else {
            map.setOrientation(Orientation.ORTHOGONAL);
        }

        if (renderorder != null) {
            map.setRenderOrder(renderorder.toLowerCase());
        } else {
            map.setRenderOrder(RenderOrder.RIGHT_DOWN);
        }

        if (tileWidth > 0) {
            map.setTileWidth(tileWidth);
        }
        if (tileHeight > 0) {
            map.setTileHeight(tileHeight);
        }
        if (hexsidelength > 0) {
            map.setHexSideLength(hexsidelength);
        }

        if (staggerAxis != null) {
            map.setStaggerAxis(staggerAxis);
        }

        if (staggerIndex != null) {
            map.setStaggerIndex(staggerIndex);
        }

        ColorRGBA backgroundColor = null;
        if (bgStr != null) {
            backgroundColor = ColorUtil.toColorRGBA(bgStr);
            map.setBackgroundColor(backgroundColor);
        }

        // Load properties
        Properties props = readProperties(mapNode.getChildNodes());
        map.setProperties(props);

        NodeList l = doc.getElementsByTagName("tileset");
        for (int i = 0; (item = l.item(i)) != null; i++) {
            Tileset set = readTileset(item);
            /**
             * update the visual part of tileset
             */
            createVisual(set);
            map.addTileset(set);
        }

        // Load the layers and objectgroups
        for (Node sibs = mapNode.getFirstChild(); sibs != null; sibs = sibs
                .getNextSibling()) {
            if ("layer".equals(sibs.getNodeName())) {
                Layer layer = readTileLayer(sibs);
                if (layer != null) {
                    map.addLayer(layer);
                }
            } else if ("objectgroup".equals(sibs.getNodeName())) {
                Layer layer = readObjectLayer(sibs);
                if (layer != null) {
                    map.addLayer(layer);
                }
            } else if ("imagelayer".equals(sibs.getNodeName())) {
                Layer layer = readImageLayer(sibs);
                if (layer != null) {
                    map.addLayer(layer);
                }
            }
        }
    }

    /**
     * read tileset
     * <p>
     * Can contain: tileoffset (since 0.8), properties (since 0.8), image,
     * terraintypes (since 0.9), tile
     *
     * @param t node
     * @return Tileset
     * @throws Exception
     */
    private Tileset readTileset(Node t) throws Exception {

        String source = getAttributeValue(t, "source");
        int firstGid = getAttribute(t, "firstgid", 1);

        if (source != null) {
            Tileset set = loadTileSet(key.getFolder() + source);
            set.setFirstGid(firstGid);
            return set;
        }

        final int tileWidth = getAttribute(t, "tilewidth", map != null ? map.getTileWidth() : 0);
        final int tileHeight = getAttribute(t, "tileheight", map != null ? map.getTileHeight() : 0);
        final int tileSpacing = getAttribute(t, "spacing", 0);
        final int tileMargin = getAttribute(t, "margin", 0);

        Tileset set = new Tileset(tileWidth, tileHeight, tileSpacing, tileMargin);
        set.setFirstGid(firstGid);

        final String name = getAttributeValue(t, "name");
        set.setName(name);

        boolean hasTilesetImage = false;
        NodeList children = t.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);

            String nodeName = child.getNodeName();
            if (nodeName.equalsIgnoreCase("image")) {
                if (hasTilesetImage) {
                    logger.warning("Ignoring illegal image element after tileset image.");
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
            } else if (nodeName.equalsIgnoreCase("terraintypes")) {
                NodeList terrainTypes = child.getChildNodes();
                for (int k = 0; k < terrainTypes.getLength(); k++) {
                    Node terrainNode = terrainTypes.item(k);
                    if (terrainNode.getNodeName().equalsIgnoreCase("terrain")) {
                        set.addTerrain(readTerrain(terrainNode));
                    }
                }

            } else if (nodeName.equalsIgnoreCase("tile")) {
                readTile(set, child);

            } else if (nodeName.equalsIgnoreCase("tileoffset")) {
                /*
                 * This element is used to specify an offset in pixels, to be
                 * applied when drawing a tile from the related tileset. When
                 * not present, no offset is applied.
                 */
                final int tileOffsetX = getAttribute(t, "x", 0);
                final int tileOffsetY = getAttribute(t, "y", 0);

                set.setTileOffset(tileOffsetX, tileOffsetY);
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

        List<Tile> tiles = tileset.getTiles();
        int mapTileHeight = map.getTileHeight();
        int tileHeight = tileset.getTileHeight();
        int offset = tileHeight - mapTileHeight;
        if (tileHeight > mapTileHeight) {
            logger.info("map - tile = " + offset);
        } else {
            offset = 0;
        }
        int len = tiles.size();
        for (int i = 0; i < len; i++) {
            Tile tile = tiles.get(i);

            String name = "tile#" + tileset.getFirstGid() + "#" + tile.getId();

            /**
             * If the tile has a texture, means that it don't use the shared
             * material.
             */
            boolean useSharedImage = tile.getTexture() == null;
            if (!useSharedImage) {
                if (tile.getMaterial() == null) {
                    // this shouldn't happen, just in case someone uses Tiles
                    // created by code.
                    logger.warning("The tile mush has a material if it don't use sharedImage:"
                            + name);
                    continue;
                }
            }

            float x = tile.getX();
            float y = tile.getY();
            float width = tile.getWidth();
            float height = tile.getHeight();

            /**
             * Calculate the texCoord of this tile in an Image.
             *
             * <pre>
             * (u0,v1)    (u1,v1)
             * *----------*
             * |        * |
             * |      *   |
             * |    *     |
             * |  *       |
             * *----------*
             * (u0,v0)    (u1,v0)
             * </pre>
             */
            float imageWidth;
            float imageHeight;
            if (useSharedImage) {
                imageWidth = image.getWidth();
                imageHeight = image.getHeight();
            } else {
                imageWidth = tile.getTexture().getImage().getWidth();
                imageHeight = tile.getTexture().getImage().getHeight();
            }

            float u0 = x / imageWidth;
            float v0 = (imageHeight - y - height) / imageHeight;
            float u1 = (x + width) / imageWidth;
            float v1 = (imageHeight - y) / imageHeight;

            float[] texCoord = new float[]{u0, v0, u1, v0, u1, v1, u0, v1};

            /**
             * Calculate the vertices' position of this tile.
             *
             * <pre>
             * 3          2
             * *----------*
             * |        * |
             * |      *   |
             * |    *     |
             * |  *       |
             * *----------*
             * 0          1
             * </pre>
             */
            float[] vertices = new float[]{
                    0, 0, height - offset,
                    width, 0, height - offset,
                    width, 0, 0 - offset,
                    0, 0, 0 - offset};

            short[] indexes = new short[]{0, 1, 2, 0, 2, 3};

            /**
             * Normals are all the same: to Vector3f.UNIT_Y
             */
            float[] normals = new float[]{0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0};

            Mesh mesh = new Mesh();
            mesh.setBuffer(Type.Position, 3, vertices);
            mesh.setBuffer(Type.TexCoord, 2, texCoord);
            mesh.setBuffer(Type.Normal, 3, normals);
            mesh.setBuffer(Type.Index, 3, indexes);
            mesh.updateBound();
            mesh.setStatic();

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
    private Tile readTile(Tileset set, Node t) throws Exception {

        Tile tile = null;

        final int id = getAttribute(t, "id", -1);

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
        final String terrainStr = getAttributeValue(t, "terrain");
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

        final double probability = getDoubleAttribute(t, "probability", -1.0);
        tile.setProbability((float) probability);

        NodeList children = t.getChildNodes();

        Properties props = readProperties(children);
        tile.setProperties(props);

        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if ("image".equalsIgnoreCase(child.getNodeName())) {
                AnImage image = readImage(child);
                tile.setTexture(image.texture);
                tile.setMaterial(image.createMaterial());
            } else if ("animation".equalsIgnoreCase(child.getNodeName())) {
                Animation animation = new Animation(null);
                NodeList frames = child.getChildNodes();
                for (int k = 0; k < frames.getLength(); k++) {
                    Node frameNode = frames.item(k);
                    if (frameNode.getNodeName().equalsIgnoreCase("frame")) {
                        Frame frame = new Frame();
                        frame.tileId = getAttribute(frameNode, "tileid", 0);
                        frame.duration = getAttribute(frameNode, "duration", 0);

                        animation.addFrame(frame);
                    }
                }
                tile.addAnimation(animation);
            }
        }

        return tile;
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
    private AnImage readImage(Node t) throws IOException {

        AnImage image = new AnImage();

        String source = getAttributeValue(t, "source");

        // load a image from file or decode from the CDATA.
        if (source != null) {
            String assetPath = toJmeAssetPath(key.getFolder() + source);
            image.source = assetPath;
            image.texture = loadTexture2D(assetPath);
        } else {
            NodeList nl = t.getChildNodes();
            for (int i = 0; i < nl.getLength(); i++) {
                Node node = nl.item(i);
                if ("data".equals(node.getNodeName())) {
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

        /*
         * // useless for jme3 image.format = getAttributeValue(t, "format");
         * image.width = getAttribute(t, "width", 0); image.height =
         * getAttribute(t, "height", 0);
         */

        return image;

    }

    /**
     * read the common part of a Layer
     *
     * @param node
     * @param layer
     */
    private void readLayerBase(Node node, Layer layer) {
        final String name = getAttributeValue(node, "name");
        final int offsetX = getAttribute(node, "offsetx", 0);
        final int offsetY = getAttribute(node, "offsety", 0);
        final int visible = getAttribute(node, "visible", 1);
        final String opacity = getAttributeValue(node, "opacity");

        layer.setName(name);
        if (opacity != null) {
            layer.setOpacity(Float.parseFloat(opacity));
        }

        // This is done at the end, otherwise the offset is applied during
        // the loading of the tiles.
        layer.setOffset(offsetX, offsetY);

        // Invisible layers are automatically locked, so it is important to
        // set the layer to potentially invisible _after_ the layer data is
        // loaded.
        // todo: Shouldn't this be just a user interface feature, rather than
        // todo: something to keep in mind at this level?
        layer.setVisible(visible == 1);

        // read properties
        Properties props = readProperties(node.getChildNodes());
        layer.setProperties(props);
    }

    /**
     * Loads a map layer from a layer node.
     *
     * @param t the node representing the "layer" element
     * @return the loaded map layer
     * @throws Exception
     */
    private Layer readTileLayer(Node t) throws Exception {
        final int layerWidth = getAttribute(t, "width", map.getWidth());
        final int layerHeight = getAttribute(t, "height", map.getHeight());

        TileLayer layer = new TileLayer(layerWidth, layerHeight);

        readLayerBase(t, layer);

        for (Node child = t.getFirstChild(); child != null; child = child
                .getNextSibling()) {
            String nodeName = child.getNodeName();
            if ("data".equalsIgnoreCase(nodeName)) {
                String encoding = getAttributeValue(child, "encoding");
                String comp = getAttributeValue(child, "compression");

                if ("base64".equalsIgnoreCase(encoding)) {
                    Node cdata = child.getFirstChild();
                    if (cdata != null) {
                        byte[] dec = Base64.getDecoder().decode(cdata.getNodeValue().trim());

                        InputStream is;
                        if ("gzip".equalsIgnoreCase(comp)) {
                            final int len = layerWidth * layerHeight * 4;
                            is = new GZIPInputStream(new ByteArrayInputStream(
                                    dec), len);
                        } else if ("zlib".equalsIgnoreCase(comp)) {
                            is = new InflaterInputStream(
                                    new ByteArrayInputStream(dec));
                        } else if (comp != null && !comp.isEmpty()) {
                            throw new IOException(
                                    "Unrecognized compression method \"" + comp
                                            + "\" for map layer "
                                            + layer.getName());
                        } else {
                            is = new ByteArrayInputStream(dec);
                        }

                        for (int y = 0; y < layer.getHeight(); y++) {
                            for (int x = 0; x < layer.getWidth(); x++) {
                                int tileId = 0;
                                tileId |= is.read();
                                tileId |= is.read() << 8;
                                tileId |= is.read() << 16;
                                tileId |= is.read() << 24;

                                map.setTileAtFromTileId(layer, y, x, tileId);
                            }
                        }
                    }
                } else if ("csv".equalsIgnoreCase(encoding)) {
                    String csvText = child.getTextContent();

                    if (comp != null && !comp.isEmpty()) {
                        throw new IOException(
                                "Unrecognized compression method \"" + comp
                                        + "\" for map layer " + layer.getName()
                                        + " and encoding " + encoding);
                    }

                    /*
                     * trim 'space', 'tab', 'newline'. pay attention to
                     * additional unicode chars like \u2028, \u2029, \u0085 if
                     * necessary
                     */
                    String[] csvTileIds = csvText.trim().split("[\\s]*,[\\s]*");

                    if (csvTileIds.length != layer.getHeight()
                            * layer.getWidth()) {
                        throw new IOException(
                                "Number of tiles does not match the layer's width and height");
                    }

                    for (int y = 0; y < layer.getHeight(); y++) {
                        for (int x = 0; x < layer.getWidth(); x++) {
                            String sTileId = csvTileIds[x + y
                                    * layer.getWidth()];
                            int tileId = Integer.parseInt(sTileId);

                            map.setTileAtFromTileId(layer, y, x, tileId);
                        }
                    }
                } else {
                    int x = 0, y = 0;
                    for (Node dataChild = child.getFirstChild(); dataChild != null; dataChild = dataChild
                            .getNextSibling()) {
                        if ("tile".equalsIgnoreCase(dataChild.getNodeName())) {
                            int tileId = getAttribute(dataChild, "gid", -1);
                            map.setTileAtFromTileId(layer, y, x, tileId);

                            x++;
                            if (x == layer.getWidth()) {
                                x = 0;
                                y++;
                            }
                            if (y == layer.getHeight()) {
                                break;
                            }
                        }
                    }
                }
            } else if ("tileproperties".equalsIgnoreCase(nodeName)) {
                for (Node tpn = child.getFirstChild(); tpn != null; tpn = tpn
                        .getNextSibling()) {
                    if ("tile".equalsIgnoreCase(tpn.getNodeName())) {
                        int x = getAttribute(tpn, "x", -1);
                        int y = getAttribute(tpn, "y", -1);

                        Properties tip = readProperties(tpn.getChildNodes());
                        layer.setTileInstancePropertiesAt(x, y, tip);
                    }
                }
            }
        }

        return layer;
    }

    /**
     * read ImageLayer
     *
     * @param node
     * @return
     * @throws Exception
     */
    private Layer readImageLayer(Node node) throws Exception {
        final int width = getAttribute(node, "width", map.getWidth());
        final int height = getAttribute(node, "height", map.getHeight());

        ImageLayer layer = new ImageLayer(width, height);
        readLayerBase(node, layer);

        boolean hasImage = false;
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);

            String nodeName = child.getNodeName();
            if (nodeName.equalsIgnoreCase("image")) {

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

        if (hasImage == false) {
            logger.warning("Imagelayer " + layer.getName() + " has no image");
            return null;
        }

        return layer;
    }

    private Layer readObjectLayer(Node node) throws Exception {

        final int width = getAttribute(node, "width", map.getWidth());
        final int height = getAttribute(node, "height", map.getHeight());

        ObjectLayer layer = new ObjectLayer(width, height);
        readLayerBase(node, layer);

        final String color = getAttributeValue(node, "color");
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

        final String draworder = getAttributeValue(node, "draworder");
        if (draworder != null) {
            layer.setDraworder(draworder);
        }

        // Add all objects from the objects group
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if ("object".equalsIgnoreCase(child.getNodeName())) {
                ObjectNode obj = readObjectNode(child);
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
    private ObjectNode readObjectNode(Node node) throws Exception {
        final int id = getAttribute(node, "id", 0);
        final String name = getAttributeValue(node, "name");
        final String type = getAttributeValue(node, "type");
        final double x = getDoubleAttribute(node, "x", 0);
        final double y = getDoubleAttribute(node, "y", 0);
        final double width = getDoubleAttribute(node, "width", 0);
        final double height = getDoubleAttribute(node, "height", 0);
        final int rotation = getAttribute(node, "rotation", 0);
        final String gid = getAttributeValue(node, "gid");
        final int visible = getAttribute(node, "visible", 1);

        ObjectNode obj = new ObjectNode(x, y, width, height);
        obj.setId(id);
        obj.setRotation(rotation);
        obj.setVisible(visible == 1);
        if (name != null) {
            obj.setName(name);
        }
        if (type != null) {
            obj.setType(type);
        }

        Properties props = readProperties(node.getChildNodes());
        obj.setProperties(props);

        /**
         * if an object have "gid" attribute means it references to a tile.
         */
        if (gid != null) {
            obj.setObjectType(ObjectType.Tile);

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
            if ("image".equalsIgnoreCase(nodeName)) {
                obj.setObjectType(ObjectType.Image);

                AnImage image = readImage(child);
                obj.setImageSource(image.source);
                obj.setTexture(image.texture);
                obj.setMaterial(image.createMaterial());

                break;
            } else if ("ellipse".equalsIgnoreCase(nodeName)) {
                obj.setObjectType(ObjectType.Ellipse);
                break;
            } else if ("polygon".equalsIgnoreCase(nodeName)) {
                obj.setObjectType(ObjectType.Polygon);
                obj.setPoints(readPoints(child));
                break;
            } else if ("polyline".equalsIgnoreCase(nodeName)) {
                obj.setObjectType(ObjectType.Polyline);
                obj.setPoints(readPoints(child));
                break;
            }
        }

        return obj;
    }

    /**
     * Read points of a polygon or polyline
     *
     * @param child
     * @return
     */
    private List<Vector2f> readPoints(Node child) {
        List<Vector2f> points = new ArrayList<Vector2f>();
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
            if ("properties".equals(child.getNodeName())) {
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
            if ("property".equalsIgnoreCase(child.getNodeName())) {
                final String key = getAttributeValue(child, "name");
                String value = getAttributeValue(child, "value");
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

                    /**
                     * type can be as follow: string (default) int: a int value
                     * float: a float value bool: has a value of either "true"
                     * or "false". color: stored in the format #AARRGGBB. file:
                     * stored as paths relative from the location of the map
                     * file.
                     */
                    final String type = getAttributeValue(child, "type");
                    if (type != null) {
                        if (type.equals("color")) {
                            val = ColorUtil.toColorRGBA(value);
                        } else if (type.equals("int")) {
                            val = (int) Long.parseLong(value);
                        } else if (type.equals("float")) {
                            val = Float.parseFloat(value);
                        } else if (type.equals("bool")) {
                            val = Boolean.parseBoolean(value);
                        } else if (type.equals("file")) {
                            val = toJmeAssetPath(this.key.getFolder() + value);
                        } else if (type.equals("string")) {
                            val = value;
                        } else {
                            val = value;
                            logger.warning("unknown type:" + type);
                        }
                    }

                    props.put(key, val);
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
            logger.log(Level.WARNING, "Can't load texture " + source, e);
        }

        return tex;
    }

    private Texture2D loadTexture2D(final byte[] data) {
        Class<?> LoaderClass = null;
        Object loaderInstance = null;
        Method loadMethod = null;

        try {
            // try Desktop first
            LoaderClass = Class.forName("com.jme3.texture.plugins.AWTLoader");
        } catch (ClassNotFoundException e) {
            logger.warning("Can't find AWTLoader.");

            try {
                // then try Android Native Image Loader
                LoaderClass = Class
                        .forName("com.jme3.texture.plugins.AndroidNativeImageLoader");
            } catch (ClassNotFoundException e1) {
                logger.warning("Can't find AndroidNativeImageLoader.");

                try {
                    // then try Android BufferImage Loader
                    LoaderClass = Class
                            .forName("com.jme3.texture.plugins.AndroidBufferImageLoader");
                } catch (ClassNotFoundException e2) {
                    logger.warning("Can't find AndroidNativeImageLoader.");
                }
            }
        }

        if (LoaderClass == null) {
            return null;
        } else {
            // try Desktop first
            try {
                loaderInstance = LoaderClass.newInstance();
                loadMethod = LoaderClass.getMethod("load", AssetInfo.class);
            } catch (ReflectiveOperationException e) {
                logger.log(Level.WARNING, "Can't find AWTLoader.", e);
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
            com.jme3.texture.Image img = (com.jme3.texture.Image) loadMethod
                    .invoke(loaderInstance, info);

            tex = new Texture2D();
            tex.setWrap(WrapMode.Repeat);
            tex.setMagFilter(MagFilter.Nearest);
            tex.setAnisotropicFilter(texKey.getAnisotropy());
            tex.setName(texKey.getName());
            tex.setImage(img);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Can't load texture from byte array", e);
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
        if (assetManager.locateAsset(new AssetKey<Object>(src)) != null) {
            return src;
        }

        /*
         * 2nd: In JME I suppose that all the files needed are in the same
         * folder, that's why I cut the filename and contact it to
         * key.getFolder().
         */
        String dest = src;
        src.replaceAll("\\\\", "/");
        int idx = src.lastIndexOf("/");
        if (idx >= 0) {
            dest = key.getFolder() + src.substring(idx + 1);
        } else {
            dest = key.getFolder() + dest;
        }

        /*
         * 3rd: try locate it again.
         */
        if (assetManager.locateAsset(new AssetKey<Object>(dest)) != null) {
            return dest;
        } else {
            throw new RuntimeException("Can't locate asset: " + src);
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

        /*
         * // useless for jme3 String format; int width; int height;
         */

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
