package io.github.jmecn.tiled.loader;

import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetManager;
import io.github.jmecn.tiled.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Properties;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

import static io.github.jmecn.tiled.TiledConst.*;
import static io.github.jmecn.tiled.loader.Utils.getAttribute;
import static io.github.jmecn.tiled.loader.Utils.getAttributeValue;

/**
 * The image loader.
 *
 * @author yanmaoyuan
 */
public class TileLayerLoader extends AbstractLayerLoader {

    private static final Logger logger = LoggerFactory.getLogger(TileLayerLoader.class);
    private final TiledMap map;

    public TileLayerLoader(AssetManager assetManager, AssetKey<?> key, TiledMap map) {
        super(assetManager, key);
        this.map = map;
    }

    /**
     * Loads a map layer from a layer node.
     *
     * @param node the node representing the "layer" element
     * @return the loaded map layer
     * @throws IOException if an I/O error occurs
     */
    public Layer load(Node node) throws IOException {
        final int layerWidth = getAttribute(node, WIDTH, map.getWidth());
        final int layerHeight = getAttribute(node, HEIGHT, map.getHeight());

        TileLayer layer = new TileLayer(layerWidth, layerHeight);

        readLayerBase(node, layer);

        Node child = node.getFirstChild();
        while (child != null) {
            String nodeName = child.getNodeName();
            if (DATA.equalsIgnoreCase(nodeName)) {
                readData(layer, child);
            } else if ("tileproperties".equals(nodeName)) {
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
        } else if ("zstd".equals(compression)) {
            throw new IOException("Unsupported compression method [" + compression + "] for map layer " + layer.getName());
        } else if (compression != null && !compression.isEmpty()) {
            logger.warn("Unrecognized compression method [{}] for map layer {}", compression, layer.getName());
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
                int tileId = (int) Long.parseLong(sTileId);
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
                int tileId = getAttribute(child, GID, -1);
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
                if (TILE.equals(child.getNodeName())) {
                    // Not to be confused with the tile element inside a tileset,
                    // this element defines the value of a single tile on a tile layer.
                    // This is however the most inefficient way of storing the tile
                    // layer data, and should generally be avoided.
                    int tileId = getAttribute(child, GID, -1);
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

                Properties tip = propertiesLoader.load(child.getChildNodes());
                layer.setTileInstancePropertiesAt(x, y, tip);
            }
            child = child.getNextSibling();
        }
    }
}
