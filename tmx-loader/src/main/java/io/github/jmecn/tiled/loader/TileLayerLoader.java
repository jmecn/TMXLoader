package io.github.jmecn.tiled.loader;

import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetManager;
import io.github.jmecn.tiled.core.*;
import io.github.jmecn.tiled.enums.DataCompression;
import io.github.jmecn.tiled.enums.DataEncoding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
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
public class TileLayerLoader extends LayerLoader {

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
    @Override
    public TileLayer load(Node node) throws IOException {
        final int layerWidth = getAttribute(node, WIDTH, map.getWidth());
        final int layerHeight = getAttribute(node, HEIGHT, map.getHeight());

        TileLayer layer = new TileLayer(layerWidth, layerHeight);

        readLayerBase(node, layer);

        Node child = node.getFirstChild();
        while (child != null) {
            String nodeName = child.getNodeName();
            if (DATA.equals(nodeName)) {
                readData(layer, child);
            } else if ("tileproperties".equals(nodeName)) {
                readTileProperties(layer, child);
            }
            child = child.getNextSibling();
        }

        return layer;
    }

    private void readData(TileLayer layer, Node node) throws IOException {
        String enc = getAttributeValue(node, "encoding");
        String comp = getAttributeValue(node, "compression");

        DataEncoding encoding;
        if (enc == null) {
            encoding = DataEncoding.NONE;
        } else {
            encoding = DataEncoding.fromValue(enc);

            if (encoding == null) {
                logger.warn("Unsupported encoding:{}, layer:{}", enc, layer.getName());
                throw new IllegalArgumentException("Unsupported encoding:" + enc);
            }
        }

        DataCompression compression;
        if (comp == null) {
            compression = DataCompression.NONE;
        } else {
            compression = DataCompression.fromValue(comp);

            if (compression == null) {
                logger.warn("Unsupported compression:{}, layer:{}", comp, layer.getName());
                throw new IllegalArgumentException("Unsupported compression:" + comp);
            }
        }

        if (map.isInfinite()) {
            // read chunks
            NodeList children = node.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                String nodeName = child.getNodeName();
                if ("chunk".equals(nodeName)) {
                    Chunk chunk = readChunk(layer, child, encoding, compression);
                    layer.addChunk(chunk);
                    mergeChunk(layer, chunk);// TODO experimental
                }
            }
        } else {
            switch (encoding) {
                case BASE64:
                    decodeBase64Data(layer, node, compression);
                    break;
                case CSV:
                    decodeCsvData(layer, node);
                    break;
                default:
                    decodeTileData(layer, node);
                    break;
            }
        }
    }

    private void mergeChunk(TileLayer layer, Chunk chunk) {
        int x = chunk.getX();
        int y = chunk.getY();
        int width = chunk.getWidth();
        int height = chunk.getHeight();

        // set chunk to layer
        for (int cy = 0; cy < height; cy++) {
            for (int cx = 0; cx < width; cx++) {
                Tile tile = chunk.getTileAt(cx, cy);
                layer.setTileAt(x + cx, y + cy, tile);
            }
        }
    }
    /**
     * Get the InputStream for the data element.
     *
     * @param len the length of the data
     * @param compression the compression method
     * @param decode the decoded data
     * @return the InputStream
     * @throws IOException if an I/O error occurs
     */
    private InputStream getInputStream(int len, DataCompression compression, byte[] decode) throws IOException {
        InputStream is;
        switch (compression) {
            case GZIP: {
                is = new GZIPInputStream(new ByteArrayInputStream(decode), len);
                break;
            }
            case ZLIB: {
                is = new InflaterInputStream(new ByteArrayInputStream(decode));
                break;
            }
            case ZSTANDARD: {
                // TODO support z-standard later
                throw new UnsupportedEncodingException("Unsupported compression method:" + compression.getValue());
            }
            default: {
                is = new ByteArrayInputStream(decode);
                break;
            }
        }
        return is;
    }

    private void decodeBase64Data(TileContainer tileContainer, Node node, DataCompression compression) throws IOException {
        Node cdata = node.getFirstChild();
        if (cdata != null) {
            byte[] decodeData = Base64.getDecoder().decode(cdata.getNodeValue().trim());
            int width = tileContainer.getWidth();
            int height = tileContainer.getHeight();
            int len = width * height * 4;
            InputStream is = getInputStream(len, compression, decodeData);

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int tileId = 0;
                    tileId |= is.read();
                    tileId |= is.read() << 8;
                    tileId |= is.read() << 16;
                    tileId |= is.read() << 24;

                    map.setTileAtFromTileId(tileContainer, x, y, tileId);
                }
            }
        }
    }

    private void decodeCsvData(TileContainer tileContainer, Node node) throws IOException {
        String csvText = node.getTextContent();

        /*
         * trim 'space', 'tab', 'newline'. pay attention to
         * additional unicode chars like \u2028, \u2029, \u0085 if
         * necessary
         */
        String[] csvTileIds = csvText.trim().split("[\\s]*,[\\s]*");

        int width = tileContainer.getWidth();
        int height = tileContainer.getHeight();
        int len = width * height;

        if (csvTileIds.length != len) {
            throw new IOException("Number of tiles does not match the layer's width and height");
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                String sTileId = csvTileIds[x + y * width];
                int tileId = (int) Long.parseLong(sTileId);
                map.setTileAtFromTileId(tileContainer, x, y, tileId);
            }
        }
    }

    private void decodeTileData(TileContainer tileContainer, Node node) {
        int x = 0;
        int y = 0;
        int width = tileContainer.getWidth();
        int height = tileContainer.getHeight();
        Node child = node.getFirstChild();
        while (child != null) {
            if (TILE.equals(child.getNodeName())) {
                int tileId = getAttribute(child, GID, -1);
                map.setTileAtFromTileId(tileContainer, x, y, tileId);

                x++;
                if (x == width) {
                    x = 0;
                    y++;
                }
                if (y == height) {
                    break;
                }
            }
            child = child.getNextSibling();
        }
    }

    /**
     * <p>This is currently added only for infinite maps. The contents of a chunk element is
     * same as that of the data element, except it stores the data of the area specified
     * in the attributes.</p>
     *
     * <p>Can contain any number: &lt;tile&gt;</p>
     *
     * @param layer the layer
     * @param node the chunk node
     * @param encoding the encoding
     * @param compression the compression
     * @return the chunk
     */
    private Chunk readChunk(TileLayer layer, Node node, DataEncoding encoding, DataCompression compression) throws IOException {
        int x = getAttribute(node, X, 0);
        int y = getAttribute(node, Y, 0);
        int width = getAttribute(node, WIDTH, 0);
        int height = getAttribute(node, HEIGHT, 0);

        Chunk chunk = new Chunk(x, y, width, height);

        if (node.hasChildNodes()) {
            switch (encoding) {
                case BASE64:
                    decodeBase64Data(chunk, node, compression);
                    break;
                case CSV:
                    decodeCsvData(chunk, node);
                    break;
                default:
                    decodeTileData(chunk, node);
                    break;
            }
        } else {
            logger.warn("Chunk has no child nodes, layer:{}", layer.getName());
            throw new IllegalArgumentException("Chunk has no child nodes");
        }

        return chunk;
    }

    private void readTileProperties(TileLayer layer, Node node) {
        Node child = node.getFirstChild();
        while (child != null) {
            if (TILE.equalsIgnoreCase(child.getNodeName())) {
                int x = getAttribute(child, X, -1);
                int y = getAttribute(child, Y, -1);

                Properties tip = propertiesLoader.load(child.getChildNodes());
                layer.setTileInstancePropertiesAt(x, y, tip);
            }
            child = child.getNextSibling();
        }
    }
}
