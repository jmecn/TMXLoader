package io.github.jmecn.tiled.render;

import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import io.github.jmecn.tiled.core.*;
import io.github.jmecn.tiled.enums.DrawOrder;
import io.github.jmecn.tiled.math2d.Point;
import io.github.jmecn.tiled.shape.Rect;
import io.github.jmecn.tiled.shape.TileMesh;

import java.util.*;

/**
 * <p>
 * we don't really draw 2d image in a 3d game engine, instead I create spatials
 * and apply Material to tiles and objects.
 * </p>
 * 
 * In Tiled Qt they use XOY axis, X positive to right and Y positive to down
 * 
 * <pre>
 * O------- X
 * |
 * |
 * |
 * Y
 * </pre>
 * 
 * Once in jme3 I choose XOY plane, which means I have to modify the Y for every
 * tile and every object. Now I choose XOZ plane, it's much easier to do the
 * math.
 * 
 * <p>The Point(x,y) in Tiled now converted to Vector3f(x, 0, y).</p>
 * 
 * <pre>
 * O------- X
 * |
 * |
 * |
 * Z
 * </pre>
 * 
 * @author yanmaoyuan
 */
public abstract class MapRenderer {

    public static final String TINT_COLOR = "TintColor";
    public static final String COLOR = "Color";

    protected TiledMap map;
    protected int width;
    protected int height;
    protected int tileWidth;
    protected int tileHeight;

    protected Node rootNode;
    protected List<Layer> sortedLayers;
    protected Map<Layer, Node> layerNodeMap;// save the layer node
    protected Map<Layer, Spatial[]> layerSpatialMap;// save the layer spatial

    protected SpriteFactory spriteFactory;

    /**
     * The whole map size in pixel
     */
    protected Point mapSize;

    protected MapRenderer(TiledMap map) {
        this.map = map;
        this.width = map.getWidth();
        this.height = map.getHeight();
        this.tileWidth = map.getTileWidth();
        this.tileHeight = map.getTileHeight();

        this.mapSize = new Point(width * tileWidth, height * tileHeight);

        this.rootNode = new Node("TileMap");
        this.rootNode.setQueueBucket(Bucket.Gui);

        this.layerNodeMap = new HashMap<>();
        this.layerSpatialMap = new HashMap<>();

        this.spriteFactory = new SpriteFactory(map);
        sortLayers();
    }

    public void sortLayers() {
        List<Layer> layers = new ArrayList<>();

        for (Layer layer : map.getLayers()) {
            if (layer instanceof GroupLayer) {
                sortLayers(layers, (GroupLayer) layer);
            } else {
                layers.add(layer);
            }
        }

        sortedLayers = Collections.unmodifiableList(layers);
    }

    public List<Layer> getSortedLayers() {
        return sortedLayers;
    }

    /**
     * Depth first search to sort layers
     * @param list
     * @param group
     */
    private void sortLayers(List<Layer> list, GroupLayer group) {
        for (Layer layer : group.getLayers()) {
            if (layer instanceof GroupLayer) {
                sortLayers(list, (GroupLayer) layer);
            } else {
                list.add(layer);
            }
        }
    }

    public Node getLayerNode(Layer layer) {
        return layerNodeMap.computeIfAbsent(layer, key -> {
            Node node = new Node(layer.getName());
            node.setQueueBucket(Bucket.Gui);
            rootNode.attachChild(node);
            return node;
        });
    }

    private Spatial[] getLayerSpatials(TileLayer layer) {
        if (layerSpatialMap.containsKey(layer)) {
            return layerSpatialMap.get(layer);
        }
        Spatial[] spatials = new Spatial[layer.getHeight() * layer.getWidth()];
        layerSpatialMap.put(layer, spatials);
        return spatials;
    }

    /**
     * Sets the spatial at the specified position. Does nothing if (tx, ty) falls
     * outside of this layer.
     *
     * @param tx
     *            x position of tile
     * @param ty
     *            y position of tile
     * @param spatial
     *            the spatial to place
     */
    public void setSpatialAt(TileLayer layer, int tx, int ty, Spatial spatial) {
        if (layer.contains(tx, ty)) {

            Node parent = getLayerNode(layer);
            Spatial[] spatials = getLayerSpatials(layer);

            int index = ty * layer.getWidth() + tx;
            Spatial old = spatials[index];
            if (old != null) {
                parent.detachChild(old);
            }

            parent.attachChild(spatial);
            spatials[index] = spatial;

            layer.setNeedUpdateAt(tx, ty, false);

            // set tint color
            ColorRGBA tintColor = layer.getTintColor();
            if (tintColor != null) {
                applyTineColor(spatial, layer.getTintColor());
            }
        }
    }

    private void applyTineColor(Spatial spatial, ColorRGBA tintColor) {
        if (spatial instanceof Geometry) {
            Geometry geom = (Geometry) spatial;
            geom.getMaterial().setColor(TINT_COLOR, tintColor);
        } else {
            Node node = (Node) spatial;
            for (Spatial child : node.getChildren()) {
                if (child instanceof Geometry) {
                    Geometry geom = (Geometry) child;
                    geom.getMaterial().setColor(TINT_COLOR, tintColor);
                }
            }
        }
    }

    public abstract Spatial createTileGrid(Material material);

    /**
     * Render the tiled map
     * 
     * @return return a Spatial for the whole map.
     */
    public Spatial render() {

        if (map == null) {
            return null;
        }

        int len = sortedLayers.size();
        for (int i = 0; i < len; i++) {
            Layer layer = sortedLayers.get(i);

            // skip invisible layer
            if (!layer.isVisible() || !layer.isNeedUpdated() || (layer instanceof GroupLayer)) {
                continue;
            }

            Spatial visual = null;
            if (layer instanceof TileLayer) {
                visual = render((TileLayer) layer);
            }

            if (layer instanceof ObjectGroup) {
                visual = render((ObjectGroup) layer);
            }

            if (layer instanceof ImageLayer) {
                visual = render((ImageLayer) layer);
            }

            if (visual != null) {
                // this is a little magic to make let top layer block off the
                // bottom layer
                visual.setLocalTranslation(0, i, 0);
                layer.setNeedUpdated(false);
            }
        }

        return rootNode;
    }

    private void setTintColor(Material material, Layer layer) {
        ColorRGBA tintColor = layer.getTintColor();
        if (tintColor != null) {
            material.setColor(TINT_COLOR, tintColor);
        }
    }

    protected abstract Spatial render(TileLayer layer);

    /**
     * Create the visual part for every ObjectNode in a ObjectLayer.
     * 
     * @param layer A ObjectLayer object
     * 
     * @return a Spatial for this layer
     */
    protected Spatial render(ObjectGroup layer) {
        List<MapObject> objects = layer.getObjects();
        Node layerNode = getLayerNode(layer);

        final ColorRGBA borderColor = layer.getColor();
        final ColorRGBA bgColor = borderColor.mult(0.3f);
        Material mat = layer.getMaterial();
        Material bgMat = mat.clone();
        bgMat.setColor(COLOR, bgColor);
        // set tint color
        setTintColor(mat, layer);
        setTintColor(bgMat, layer);

        int len = objects.size();

        if (len > 0) {
            layerNode.setLocalScale(1f, 1f / len, 1f);

            // sort draw order
            if (Objects.requireNonNull(layer.getDrawOrder()) == DrawOrder.TOPDOWN) {
                objects.sort(new CompareTopdown());
            } else if (layer.getDrawOrder() == DrawOrder.INDEX) {
                objects.sort(new CompareIndex());
            }
        }

        ObjectFactory objectFactory = new ObjectFactory(layer);
        for (int i = 0; i < len; i++) {
            MapObject obj = objects.get(i);

            if (obj.isVisible() && obj.isNeedUpdated()) {

                Spatial visual = objectFactory.create(obj);
                if (visual == null) {
                    continue;
                }

                float x = (float) obj.getX();
                float y = (float) obj.getY();

                Vector2f screenCoord = pixelToScreenCoords(x, y);
                visual.move(screenCoord.x, i, screenCoord.y);
                layerNode.attachChild(visual);
            }
        }

        layerNode.setLocalScale(1f, 1f / len, 1f);
        return layerNode;
    }

    protected Spatial render(ImageLayer layer) {
        Node layerNode = getLayerNode(layer);

        if (layer.isNeedUpdated()) {
            layerNode.detachAllChildren();

            TiledImage image = layer.getImage();

            Material mat = image.getMaterial();
            setTintColor(mat, layer);

            Mesh mesh = new Rect(mapSize.getX(), mapSize.getY(), false);
            Geometry geom = new Geometry(layer.getName(), mesh);
            geom.setMaterial(mat);
            geom.setQueueBucket(Bucket.Gui);

            layerNode.attachChild(geom);

            layer.setNeedUpdated(false);
        }

        return layerNode;
    }

    public abstract void renderGrid(Node gridVisual, Material gridMaterial);

    /******************************
     * Coordinates System Convert *
     ******************************/

    /**
     * Convert the pixel coordinates to screen coordinates.
     * @param x the x coordinate in pixel
     * @param y the y coordinate in pixel
     * @return the screen coordinates
     */
    public abstract Vector2f pixelToScreenCoords(float x, float y);

    /**
     * Convert the screen coordinates to pixel coordinates.
     * @param x the x coordinate in screen
     * @param y the y coordinate in screen
     * @return the pixel coordinates
     */
    public abstract Point pixelToTileCoords(float x, float y);

    /**
     * Convert the tile coordinates to pixel coordinates.
     * @param x the x coordinate in tile
     * @param y the y coordinate in tile
     * @return the pixel coordinates
     */
    public abstract Vector2f tileToPixelCoords(float x, float y);

    /**
     * Convert the tile coordinates to screen coordinates.
     * @param x the x coordinate in tile
     * @param y the y coordinate in tile
     * @return the screen coordinates
     */
    public abstract Vector2f tileToScreenCoords(float x, float y);

    /**
     * Convert the screen coordinates to pixel coordinates.
     * @param x the x coordinate in screen
     * @param y the y coordinate in screen
     * @return the pixel coordinates
     */
    public abstract Vector2f screenToPixelCoords(float x, float y);

    /**
     * Convert the screen coordinates to tile coordinates.
     * @param x the x coordinate in screen
     * @param y the y coordinate in screen
     * @return the tile coordinates
     */
    public abstract Point screenToTileCoords(float x, float y);

    protected void removeTileSprite(TileLayer layer, int x, int y) {
        Spatial[] spatials = getLayerSpatials(layer);
        int index = y * layer.getWidth() + x;
        if (spatials[index] != null) {
            Node parent = getLayerNode(layer);
            parent.detachChild(spatials[index]);
            spatials[index] = null;
        }
    }

    protected void putTileSprite(TileLayer layer, int x, int y, int z, Tile tile, Vector2f pixelCoord) {
        Geometry visual = copySprite(tile);
        flip(visual, tile);
        visual.move(pixelCoord.x, z, pixelCoord.y);
        setSpatialAt(layer, x, y, visual);
    }

    protected Geometry copySprite(Tile tile) {
        return spriteFactory.getTileSprite(tile).clone();
    }

    /**
     * Flip the tile
     *
     * @param visual The spatial for this tile.
     * @param tile The image of this tile.
     */
    protected void flip(Geometry visual, Tile tile) {
        if (tile.getGidNoMask() == tile.getGid()) {
            // no flip
            return;
        }
        TileMesh mesh = (TileMesh) visual.getMesh();
        TileMesh newMesh = new TileMesh(mesh.getCoord(), mesh.getSize(), mesh.getOffset(), mesh.getOrigin(), tile.getGid(), map.getOrientation());
        visual.setMesh(newMesh);
    }

    /**
     * Get the map node
     * @return the map node
     */
    public Node getRootNode() {
        return rootNode;
    }

    private static final class CompareTopdown implements Comparator<MapObject> {
        @Override
        public int compare(MapObject o1, MapObject o2) {
            double a = o1.getY();
            double b = o2.getY();

            return Double.compare(a, b);
        }
    }

    private static final class CompareIndex implements Comparator<MapObject> {
        @Override
        public int compare(MapObject o1, MapObject o2) {
            int a = o1.getId();
            int b = o2.getId();

            return Integer.compare(a, b);
        }
    }

    public Point getMapDimension() {
        return new Point(mapSize.getX(), mapSize.getY());// read only
    }

    public Vector2f getMapDimensionF() {
        return new Vector2f(mapSize.getX(), mapSize.getY());
    }
}
