package io.github.jmecn.tiled.loader.layer;

import com.jme3.asset.*;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import io.github.jmecn.tiled.core.*;
import io.github.jmecn.tiled.enums.DrawOrder;
import io.github.jmecn.tiled.enums.ObjectType;
import io.github.jmecn.tiled.loader.LayerLoader;
import io.github.jmecn.tiled.loader.Utils;
import io.github.jmecn.tiled.util.ColorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static io.github.jmecn.tiled.TiledConst.*;
import static io.github.jmecn.tiled.loader.Utils.*;
import static io.github.jmecn.tiled.loader.Utils.getAttribute;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public class ObjectLayerLoader extends LayerLoader {
    private static final Logger logger = LoggerFactory.getLogger(ObjectLayerLoader.class);
    private final TiledMap map;

    public ObjectLayerLoader(AssetManager assetManager, AssetKey<?> key) {
        this(assetManager, key, null);
    }

    public ObjectLayerLoader(AssetManager assetManager, AssetKey<?> key, TiledMap map) {
        super(assetManager, key);
        this.map = map;
    }

    @Override
    public ObjectGroup load(Node node) {
        int defWidth = map == null ? 0 : map.getWidth();
        int defHeight = map == null ? 0 : map.getHeight();
        final int width = getAttribute(node, WIDTH, defWidth);
        final int height = getAttribute(node, HEIGHT, defHeight);

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

        final String drawOrder = getAttributeValue(node, DRAW_ORDER);
        if (drawOrder != null) {
            layer.setDrawOrder(DrawOrder.fromValue(drawOrder));
        }

        // Add all objects from the objects group
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (OBJECT.equals(child.getNodeName())) {
                MapObject obj = readObjectNode(child);
                layer.add(obj);
            }
        }

        return layer;
    }


    /**
     * Load an ObjectTemplate from .tx file.
     *
     * @param inputStream the input stream of the template file
     * @return the loaded ObjectTemplate
     */
    public ObjectTemplate loadObjectTemplate(final InputStream inputStream) {
        ObjectTemplate template;
        Node root;

        DocumentBuilderFactory factory;
        try {
            factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(inputStream);

            NodeList nodeList = doc.getElementsByTagName(TEMPLATE);

            // There can be only one template in a .tx file.
            root = nodeList.item(0);

            if (root == null) {
                logger.warn("Not a valid template file.");
                throw new IllegalArgumentException("Not a valid template file");
            }

            template = readObjectTemplate(root);
            template.setSource(assetKey.getName());
            return template;
        } catch (Exception e) {
            throw new AssetLoadException("Error while parsing template file.", e);
        }
    }

    /**
     * Load Template from a ".tx" file.
     *
     * @param source the source of the template
     * @return the loaded template
     */
    private ObjectTemplate loadObjectTemplate(String source) {
        String assetPath = toJmeAssetPath(assetManager, assetKey, source);

        ObjectTemplate objectTemplate;

        // try to load cached objectTemplate from the map
        if (map != null) {
            objectTemplate = map.getObjectTemplate(assetPath);
            if (objectTemplate != null) {
                return objectTemplate;
            }
        }

        // load it with assetManager
        try {
            logger.info("Loading template: {}", assetPath);
            objectTemplate = assetManager.loadAsset(new AssetKey<>(assetPath));
            if (map != null) {// cache the objectTemplate
                map.addObjectTemplate(objectTemplate);
            }
            return objectTemplate;
        } catch (Exception e) {
            logger.error("Template {} was not loaded correctly!", source, e);
            throw new AssetLoadException("Template not found: " + source);
        }
    }

    private ObjectTemplate readObjectTemplate(Node node) {
        Tileset tileset = null;

        int firstGid = 0;
        String source = null;

        Node tilesetNode = getChildByTag(node, TILESET);// not null if the object is a tile object.
        if (tilesetNode != null) {
            // The readObjectNode method will automatically set the tileset from tiled map,
            // so it doesn't need to load tileset again here.
            firstGid = Utils.getAttribute(tilesetNode, FIRST_GID, 1);
            source = Utils.getAttributeValue(tilesetNode, SOURCE);
        }

        Node objectNode = getChildByTag(node, OBJECT);
        if (objectNode == null) {
            logger.error("Template must have an object node.");
            throw new IllegalArgumentException("Template must have an object node.");
        }

        MapObject obj = readObjectNode(objectNode);
        if (obj.getTile() != null) {
            tileset = obj.getTile().getTileset();
        }

        // for debug, in case the tileset is different from the tileset in the map.
        if (tileset != null && firstGid != tileset.getFirstGid()) {
            logger.warn("Template firstGid:{}, source:{}, Tileset firstGid:{}, source:{}", firstGid, source, tileset.getFirstGid(), tileset.getSource());
        }

        ObjectTemplate template = new ObjectTemplate();
        template.setTileset(tileset);
        template.setObject(obj);
        return template;
    }

    /**
     * Read an object of the ObjectGroup.
     *
     * @param node the node containing the object
     * @return MapObject
     */
    private MapObject readObjectNode(Node node) {
        int id = getAttribute(node, ID, 0);
        String name = getAttributeValue(node, NAME);
        String clazz = getAttribute(node, TYPE, getAttribute(node, CLASS, EMPTY));// compatibility with 1.8 or earlier
        double x = getDoubleAttribute(node, X, 0);
        double y = getDoubleAttribute(node, Y, 0);
        double width = getDoubleAttribute(node, WIDTH, 0);
        double height = getDoubleAttribute(node, HEIGHT, 0);
        double rotation = getDoubleAttribute(node, ROTATION, 0);
        String gid = getAttributeValue(node, GID);
        int visible = getAttribute(node, VISIBLE, 1);

        MapObject obj = new MapObject(x, y, width, height);
        obj.setId(id);
        obj.setRotation(rotation);
        obj.setVisible(visible == 1);
        if (name != null) {
            obj.setName(name);
        }
        if (clazz != null) {
            obj.setClazz(clazz);
        }

        ObjectTemplate template;
        String templateSource = getAttributeValue(node, TEMPLATE);
        if (templateSource != null) {
            template = loadObjectTemplate(templateSource);

            obj.setTemplate(templateSource);
            template.copyTo(obj);

            // merge the properties, behavior like inheritance.
            Properties props = propertiesLoader.readProperties(node);
            obj.getProperties().putAll(props);
            return obj;
        }

        Properties props = propertiesLoader.readProperties(node);
        obj.setProperties(props);

        /*
         * if an object have "gid" attribute means it references to a tile.
         */
        if (gid != null) {
            setTileByGid(obj, gid);
        } else {
            readShape(node, obj);
        }

        return obj;
    }

    /**
     * Set tile by gid
     * @param obj the object
     * @param id the gid
     */
    private void setTileByGid(MapObject obj, String id) {
        int gid = (int) Long.parseLong(id);
        obj.setShape(ObjectType.TILE);
        obj.setGid(gid);

        // clear the flag
        int tileId = gid & ~Tile.FLIPPED_MASK;
        Tile tile = map.getTileForTileGID(tileId);
        Tile t = tile.copy();
        t.setGid(gid);
        obj.setTile(t);
    }

    /**
     * Read the shape of the object
     * @param node the node containing the shape
     * @param obj the object
     */
    private void readShape(Node node, MapObject obj) {
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            String nodeName = child.getNodeName();

            if (!PROPERTIES.equals(nodeName) && !TEXT_EMPTY.equals(nodeName)) {
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
                        TiledImage image = imageLoader.load(child);
                        obj.setImage(image);
                        break;
                    }
                    default: {
                        logger.warn("unknown object type:{}", nodeName);
                        break;
                    }
                }
            }
        }
    }

    /**
     * Read points of a polygon or polyline
     *
     * @param child the node containing the points
     * @return a list of points
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
}
