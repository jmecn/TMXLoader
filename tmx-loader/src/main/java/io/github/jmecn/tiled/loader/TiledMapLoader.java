package io.github.jmecn.tiled.loader;

import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetManager;
import com.jme3.math.ColorRGBA;
import io.github.jmecn.tiled.core.Layer;
import io.github.jmecn.tiled.core.TiledMap;
import io.github.jmecn.tiled.core.Tileset;
import io.github.jmecn.tiled.enums.Orientation;
import io.github.jmecn.tiled.enums.RenderOrder;
import io.github.jmecn.tiled.enums.StaggerAxis;
import io.github.jmecn.tiled.enums.StaggerIndex;
import io.github.jmecn.tiled.math2d.Point;
import io.github.jmecn.tiled.util.ColorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static io.github.jmecn.tiled.TiledConst.*;
import static io.github.jmecn.tiled.loader.Utils.getAttribute;
import static io.github.jmecn.tiled.loader.Utils.getAttributeValue;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public final class TiledMapLoader {

    private static final Logger logger = LoggerFactory.getLogger(TiledMapLoader.class);

    private final AssetManager assetManager;

    private final AssetKey<?> assetKey;

    private TiledMap map;

    private final TilesetLoader tilesetLoader;
    private final PropertyLoader propertiesLoader;
    private TileLayerLoader tileLayerReader;
    private ImageLayerLoader imageLayerReader;
    private ObjectLayerLoader objectLayerReader;
    private GroupLayerLoader groupLayerReader;

    public TiledMapLoader(AssetManager assetManager, AssetKey<?> key) {
        this.assetManager = assetManager;
        this.assetKey = key;

        this.tilesetLoader = new TilesetLoader(assetManager, key);
        this.propertiesLoader = new PropertyLoader();
    }
    private TileLayerLoader getTileLayerReader() {
        if (tileLayerReader == null) {
            tileLayerReader = new TileLayerLoader(assetManager, assetKey, map);
        }
        return tileLayerReader;
    }
    private ImageLayerLoader getImageLayerReader() {
        if (imageLayerReader == null) {
            imageLayerReader = new ImageLayerLoader(assetManager, assetKey, map);
        }
        return imageLayerReader;
    }

    private ObjectLayerLoader getObjectLayerReader() {
        if (objectLayerReader == null) {
            objectLayerReader = new ObjectLayerLoader(assetManager, assetKey, map);
        }
        return objectLayerReader;
    }
    private GroupLayerLoader getGroupLayerReader() {
        if (groupLayerReader == null) {
            groupLayerReader = new GroupLayerLoader(assetManager, assetKey, map);
        }
        return groupLayerReader;
    }

    /**
     * Load a Map from .tmx file
     *
     * @param inputStream InputStream
     * @return the TiledMap
     * @throws IOException if an error occurs while reading the map
     */
    public TiledMap load(InputStream inputStream) throws IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Document doc;
        try {
            factory.setIgnoringComments(true);
            factory.setIgnoringElementContentWhitespace(true);
            factory.setExpandEntityReferences(false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setEntityResolver((publicId, systemId) -> {
                if (systemId.equals("http://mapeditor.org/dtd/1.0/map.dtd")) {
                    return new InputSource(getClass().getResourceAsStream("/tiled/map.dtd"));
                }
                return null;
            });

            InputSource source = new InputSource(inputStream);
            source.setSystemId(assetKey.getFolder());
            source.setEncoding("UTF-8");
            doc = builder.parse(source);
        } catch (SAXException | ParserConfigurationException e) {
            logger.error("Error while parsing map file: {}", assetKey.getName(), e);
            throw new IllegalStateException("Error while parsing map file: " + assetKey.getName());
        }

        return readMap(doc);
    }

    public TiledMap readMap(Document doc) throws IOException {
        Node mapNode = doc.getDocumentElement();

        if (!MAP.equals(mapNode.getNodeName())) {
            throw new IllegalArgumentException("Not a valid tmx map file.");
        }

        // Get the map dimensions and create the map
        int mapWidth = getAttribute(mapNode, WIDTH, 0);
        int mapHeight = getAttribute(mapNode, HEIGHT, 0);

        if (mapWidth <= 0 || mapHeight <= 0) {
            // Maybe this map is still using the dimensions element
            Point mapSize = readDimensions(doc);
            mapWidth = mapSize.x;
            mapHeight = mapSize.y;
        }

        if (mapWidth > 0 && mapHeight > 0) {
            map = new TiledMap(mapWidth, mapHeight);
        } else {
            logger.warn("Couldn't locate map dimensions.");
            throw new IllegalArgumentException("Couldn't locate map dimensions.");
        }

        // Load other map attributes
        String version = getAttributeValue(mapNode, VERSION);
        String tiledVersion = getAttributeValue(mapNode, TILED_VERSION);
        String clazz = getAttribute(mapNode, CLASS, EMPTY);
        String orientation = getAttribute(mapNode, ORIENTATION, Orientation.ORTHOGONAL.getValue());
        String renderOrder = getAttribute(mapNode, RENDER_ORDER, RenderOrder.RIGHT_DOWN.getValue());
        int compressionLevel = getAttribute(mapNode, COMPRESSION_LEVEL, -1);
        int tileWidth = getAttribute(mapNode, TILE_WIDTH, 0);
        int tileHeight = getAttribute(mapNode, TILE_HEIGHT, 0);
        int hexSideLength = getAttribute(mapNode, HEX_SIDE_LENGTH, 0);
        String staggerAxis = getAttribute(mapNode, STAGGER_AXIS, StaggerAxis.Y.getValue());
        String staggerIndex = getAttribute(mapNode, STAGGER_INDEX, StaggerIndex.ODD.getValue());
        int parallaxOriginX = getAttribute(mapNode, PARALLAX_ORIGIN_X, 0);
        int parallaxOriginY = getAttribute(mapNode, PARALLAX_ORIGIN_Y, 0);
        String bgStr = getAttributeValue(mapNode, BACKGROUND_COLOR);
        int nextLayerId = getAttribute(mapNode, NEXT_LAYER_ID, 0);
        int nextObjectId = getAttribute(mapNode, NEXT_OBJECT_ID, 0);
        boolean infinite = getAttribute(mapNode, INFINITE, 0) == 1;

        map.setVersion(version);
        map.setTiledVersion(tiledVersion);
        map.setClazz(clazz);
        map.setOrientation(orientation);
        map.setRenderOrder(renderOrder.toLowerCase());
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

        map.setStaggerAxis(staggerAxis);
        map.setStaggerIndex(staggerIndex);
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
        Properties props = propertiesLoader.load(mapNode.getChildNodes());
        map.setProperties(props);

        // read tilesets
        readTilesets(doc);

        // read layers
        readLayers(mapNode);

        return map;
    }

    private Point readDimensions(Document doc) {
        Point mapSize = new Point(0, 0);
        // Maybe this map is still using the dimensions element
        NodeList l = doc.getElementsByTagName("dimensions");
        Node item;
        Node mapNode = doc.getDocumentElement();
        for (int i = 0; (item = l.item(i)) != null; i++) {
            if (item.getParentNode() == mapNode) {
                int mapWidth = getAttribute(item, WIDTH, 0);
                int mapHeight = getAttribute(item, HEIGHT, 0);

                mapSize.set(mapWidth, mapHeight);
            }
        }

        return mapSize;
    }

    public void readTilesets(Document doc) {
        NodeList tileSets = doc.getElementsByTagName(TILESET);
        Node item;
        for (int i = 0; (item = tileSets.item(i)) != null; i++) {
            Tileset set = tilesetLoader.readTileset(item, map);
            tilesetLoader.createVisual(set, map);
            map.addTileset(set);
        }
    }

    private void readLayers(Node mapNode) throws IOException {
        Node child = mapNode.getFirstChild();
        while (child != null) {
            String childName = child.getNodeName();
            switch (childName) {
                case LAYER: {
                    Layer layer = getTileLayerReader().load(child);
                    map.addLayer(layer);
                    break;
                }
                case OBJECTGROUP: {
                    Layer layer = getObjectLayerReader().load(child);
                    map.addLayer(layer);
                    break;
                }
                case IMAGELAYER: {
                    Layer layer = getImageLayerReader().load(child);
                    map.addLayer(layer);
                    break;
                }
                case GROUP: {
                    Layer layer = getGroupLayerReader().load(child);
                    map.addLayer(layer);
                    break;
                }
                default: {
                    if (!TILESET.equals(childName) && !PROPERTIES.equals(childName) && !TEXT_EMPTY.equals(childName)) {
                        logger.warn("Unsupported map element: {}", childName);
                    }
                    break;
                }
            }
            child = child.getNextSibling();
        }
    }

}
