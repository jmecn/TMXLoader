package io.github.jmecn.tiled.renderer;

import com.jme3.material.Material;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import io.github.jmecn.tiled.core.*;
import io.github.jmecn.tiled.enums.DrawOrder;
import io.github.jmecn.tiled.renderer.factory.SpriteFactory;
import io.github.jmecn.tiled.math2d.Point;
import io.github.jmecn.tiled.renderer.shape.Rect;

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

    protected double layerDistance = 16f;// the distance between layers
    protected double layerGap = 1f;// the gap between layers
    protected double step;

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
        this.step = layerDistance / (height * width);
        this.mapSize = new Point(width * tileWidth, height * tileHeight);

        this.rootNode = new Node("TileMap");
        this.rootNode.setQueueBucket(RenderQueue.Bucket.Opaque);

        this.layerNodeMap = new HashMap<>();
        this.layerSpatialMap = new HashMap<>();
        sortLayers();
    }

    public static MapRenderer create(TiledMap tiledMap) {
        switch (tiledMap.getOrientation()) {
            case ORTHOGONAL:
                return new OrthogonalRenderer(tiledMap);
            case ISOMETRIC:
                return new IsometricRenderer(tiledMap);
            case STAGGERED:
                return new StaggeredRenderer(tiledMap);
            case HEXAGONAL:
                return new HexagonalRenderer(tiledMap);
            default:
                throw new IllegalArgumentException("Unsupported orientation: " + tiledMap.getOrientation());
        }
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
     * @param list the list to save sorted layers
     * @param group the group layer
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

    /**
     * Set the sprite factory
     * @param spriteFactory the sprite factory
     */
    public void setSpriteFactory(SpriteFactory spriteFactory) {
        this.spriteFactory = spriteFactory;
    }

    /**
     * Get the sprite factory
     * @return the sprite factory
     */
    public SpriteFactory getSpriteFactory() {
        return spriteFactory;
    }

    public Node getLayerNode(Layer layer) {
        return layerNodeMap.computeIfAbsent(layer, key -> {
            Node node = new Node(layer.getName());
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
     * @param layer the layer
     * @param tx x position of tile
     * @param ty y position of tile
     * @param spatial the spatial to place
     */
    public void setSpatialAt(TileLayer layer, int tx, int ty, Spatial spatial) {
        if (layer.contains(tx, ty)) {

            Node parent = getLayerNode(layer);
            Spatial[] spatials = getLayerSpatials(layer);

            int index = (ty - layer.getY()) * layer.getWidth() + (tx - layer.getX());
            Spatial old = spatials[index];
            if (old != null) {
                parent.detachChild(old);
            }

            parent.attachChild(spatial);
            spatials[index] = spatial;

            layer.setNeedUpdateAt(tx, ty, false);

            spriteFactory.setTintColor(spatial, layer.getTintColor());
            spriteFactory.setLayerOpacity(spatial, (float) layer.getOpacity());
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
                Vector3f loc = visual.getLocalTranslation();
                visual.setLocalTranslation(loc.x, getLayerYIndex(i), loc.z);
                layer.setNeedUpdated(false);
            }
        }

        return rootNode;
    }

    public abstract void visitTiles(TileVisitor visitor);

    protected Spatial render(TileLayer layer) {
        Node layerNode = getLayerNode(layer);
        layerNode.detachAllChildren();

        visitTiles((x, y, z) -> {
            if (layer.isNeedUpdateAt(x, y)) {
                final Tile tile = layer.getTileAt(x, y);
                if (tile == null) {
                    removeTileSprite(layer, x, y);
                } else {
                    Vector2f pixelCoord = tileToScreenCoords(x, y);
                    putTileSprite(layer, x, y, getTileYAxis(z), tile, pixelCoord);
                }
            }
        });

        return layerNode;
    }

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

        int len = objects.size();

        if (len > 0) {
            // sort draw order
            if (Objects.requireNonNull(layer.getDrawOrder()) == DrawOrder.TOPDOWN) {
                objects.sort(new CompareTopdown());
            } else if (layer.getDrawOrder() == DrawOrder.INDEX) {
                objects.sort(new CompareIndex());
            }
        }

        Material material = spriteFactory.newMaterial(layer.getColor(), layer.getTintColor());

        for (int i = 0; i < len; i++) {
            MapObject obj = objects.get(i);

            if (obj.isVisible()) {
                Spatial spatial = spriteFactory.newObjectSprite(obj, material);
                if (spatial == null) {
                    continue;
                }
                spriteFactory.setTintColor(spatial, layer.getTintColor());
                spriteFactory.setLayerOpacity(spatial, (float) layer.getOpacity());

                float x = (float) obj.getX();
                float y = (float) obj.getY();

                // sort top-down
                // don't support sorting by index
                float z = getObjectTopDownYIndex(y);

                Vector2f screenCoord = pixelToScreenCoords(x, y);
                spatial.move(screenCoord.x, z, screenCoord.y);
                layerNode.attachChild(spatial);
            }
        }

        return layerNode;
    }

    protected Spatial render(ImageLayer layer) {
        Node layerNode = getLayerNode(layer);

        if (layer.isNeedUpdated()) {
            layerNode.detachAllChildren();

            Material material = spriteFactory.newMaterial(layer.getImage(), layer.getTintColor());

            Mesh mesh = new Rect(mapSize.getX(), mapSize.getY(), true);
            Geometry geom = new Geometry(layer.getName(), mesh);
            geom.setMaterial(material);

            spriteFactory.setTintColor(geom, layer.getTintColor());
            spriteFactory.setLayerOpacity(geom, (float) layer.getOpacity());

            layerNode.attachChild(geom);

            layer.setNeedUpdated(false);
        }

        return layerNode;
    }

    public abstract void renderGrid(Node gridVisual, Material gridMaterial);

    ///////////////////////////////////////////////
    ///////// Coordinates System Convert //////////
    ///////////////////////////////////////////////

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
        int index = (y - layer.getY()) * layer.getWidth() + (x - layer.getX());
        if (spatials[index] != null) {
            Node parent = getLayerNode(layer);
            parent.detachChild(spatials[index]);
            spatials[index] = null;
        }
    }

    protected void putTileSprite(TileLayer layer, int x, int y, float z, Tile tile, Vector2f pixelCoord) {
        Material material = spriteFactory.newMaterial(tile);
        Geometry visual = spriteFactory.newTileSprite(tile, material);
        visual.move(pixelCoord.x, z, pixelCoord.y);
        setSpatialAt(layer, x, y, visual);
    }

    /**
     * Get the map node
     * @return the map node
     */
    public Node getRootNode() {
        return rootNode;
    }

    public void updateLayerYAxis() {
        for (Layer layer : sortedLayers) {
            layer.setNeedUpdated(true);
        }
    }

    public void setLayerDistance(double layerDistance) {
        this.layerDistance = layerDistance;
        this.step = layerDistance / (height * width);
        updateLayerYAxis();
    }

    public void setLayerGap(double layerGap) {
        this.layerGap = layerGap;
        updateLayerYAxis();
    }

    public float getLayerYIndex(int index) {
        return  (float) (index *(layerDistance + layerGap));
    }

    /**
     * this is the z-index in the layer
     * @param tileZIndex the z-index in the layer, range from [0 , width * height)
     * @return the y-axis in the layer
     */
    protected float getTileYAxis(int tileZIndex) {
        return (float) (tileZIndex * step);
    }

    public float getObjectTopDownYIndex(float y) {
        float tileY = y / mapSize.getY();
        return (float) (tileY * layerDistance);
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
        return mapSize;
    }

    public int getTileWidth() {
        return tileWidth;
    }

    public int getTileHeight() {
        return tileHeight;
    }
}
