package com.jme3.tmx.render;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.tmx.core.ImageLayer;
import com.jme3.tmx.core.Layer;
import com.jme3.tmx.core.ObjectGroup;
import com.jme3.tmx.core.MapObject;
import com.jme3.tmx.core.Tile;
import com.jme3.tmx.core.TileLayer;
import com.jme3.tmx.core.TiledMap;
import com.jme3.tmx.enums.Orientation;
import com.jme3.tmx.math2d.Point;
import com.jme3.tmx.util.ObjectMesh;

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
 * The Point(x,y) in Tiled now converted to Vector3f(x, 0, y).
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
 * 
 */
public abstract class MapRenderer {

    static Logger logger = Logger.getLogger(MapRenderer.class.getName());

    /**
     * This value used to generate ellipse mesh.
     */
    private final static int ELLIPSE_POINTS = 36;

    protected TiledMap map;
    protected int width;
    protected int height;
    protected int tileWidth;
    protected int tileHeight;

    /**
     * The whole map size in pixel
     */
    protected Point mapSize;

    public MapRenderer(TiledMap map) {
        this.map = map;
        this.width = map.getWidth();
        this.height = map.getHeight();
        this.tileWidth = map.getTileWidth();
        this.tileHeight = map.getTileHeight();

        this.mapSize = new Point();
        this.mapSize.set(width * tileWidth, height * tileHeight);
    }

    /**
     * Render the tiled map
     * 
     * @return return a Spatial for the whole map.
     */
    public Spatial render() {

        if (map == null) {
            return null;
        }

        int len = map.getLayerCount();
        for (int i = 0; i < len; i++) {
            Layer layer = map.getLayer(i);

            // skip invisible layer
            if (!layer.isVisible()) {
                continue;
            }

            if (!layer.isNeedUpdated()) {
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
        return map.getVisual();
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
        // instance the layer node
        if (layer.getVisual() == null) {
            Node layerNode = new Node("ObjectGroup#" + layer.getName());
            layerNode.setQueueBucket(Bucket.Gui);
            layer.setVisual(layerNode);
            map.getVisual().attachChild(layerNode);
        }

        final ColorRGBA borderColor = layer.getColor();
        final ColorRGBA bgColor = borderColor.mult(0.3f);
        Material mat = layer.getMaterial();
        Material bgMat = mat.clone();
        bgMat.setColor("Color", bgColor);

        int len = objects.size();

        if (len > 0) {
            layer.getVisual().setLocalScale(1f, 1f / len, 1f);

            // sort draw order
            switch (layer.getDrawOrder()) {
            case TOPDOWN:
                objects.sort(new CompareTopdown());
                break;
            case INDEX:
                objects.sort(new CompareIndex());
                break;
            }
        }

        for (int i = 0; i < len; i++) {
            MapObject obj = objects.get(i);

            if (!obj.isVisible()) {
                continue;
            }

            if (obj.isNeedUpdated()) {

                switch (obj.getShape()) {
                case RECTANGLE: {
                    Geometry border = new Geometry("border",
                            ObjectMesh.makeRectangleBorder(obj.getWidth(), obj.getHeight()));
                    border.setMaterial(mat);
                    border.setQueueBucket(Bucket.Gui);

                    Geometry back = new Geometry("rectangle",
                            ObjectMesh.makeRectangle(obj.getWidth(), obj.getHeight()));
                    back.setMaterial(bgMat);
                    back.setQueueBucket(Bucket.Gui);

                    Node visual = new Node(obj.getName());
                    visual.attachChild(back);
                    visual.attachChild(border);
                    visual.setQueueBucket(Bucket.Gui);

                    obj.setVisual(visual);
                    break;
                }
                case ELLIPSE: {
                    Geometry border = new Geometry("border",
                            ObjectMesh.makeEllipseBorder(obj.getWidth(), obj.getHeight(), ELLIPSE_POINTS));
                    border.setMaterial(mat);
                    border.setQueueBucket(Bucket.Gui);

                    Geometry back = new Geometry("ellipse",
                            ObjectMesh.makeEllipse(obj.getWidth(), obj.getHeight(), ELLIPSE_POINTS));
                    back.setMaterial(bgMat);
                    back.setQueueBucket(Bucket.Gui);

                    Node visual = new Node(obj.getName());
                    visual.attachChild(back);
                    visual.attachChild(border);
                    visual.setQueueBucket(Bucket.Gui);

                    obj.setVisual(visual);
                    break;
                }
                case POLYGON: {
                    Geometry border = new Geometry("border", ObjectMesh.makePolyline(obj.getPoints(), true));
                    border.setMaterial(mat);
                    border.setQueueBucket(Bucket.Gui);

                    Geometry back = new Geometry("polygon", ObjectMesh.makePolygon(obj.getPoints()));
                    back.setMaterial(bgMat);
                    back.setQueueBucket(Bucket.Gui);

                    Node visual = new Node(obj.getName());
                    visual.attachChild(back);
                    visual.attachChild(border);
                    visual.setQueueBucket(Bucket.Gui);

                    obj.setVisual(visual);
                    break;
                }
                case POLYLINE: {
                    Geometry visual = new Geometry("polyline", ObjectMesh.makePolyline(obj.getPoints(), false));
                    visual.setMaterial(mat);
                    visual.setQueueBucket(Bucket.Gui);

                    obj.setVisual(visual);
                    break;
                }
                case IMAGE: {
                    Geometry geom = new Geometry(obj.getName(),
                            ObjectMesh.makeRectangle(obj.getWidth(), obj.getHeight()));
                    geom.setMaterial(obj.getMaterial());
                    geom.setQueueBucket(Bucket.Gui);

                    obj.setVisual(geom);
                    break;
                }
                case TILE: {
                    Tile tile = obj.getTile();

                    Spatial visual = tile.getVisual().clone();
                    visual.setQueueBucket(Bucket.Gui);

                    flip(visual, obj.getTile());

                    // When the object has a gid set, then it is represented by
                    // the image of the tile with that global ID. The image
                    // alignment currently depends on the map orientation.
                    float height = tile.getHeight();
                    if (map.getOrientation() == Orientation.ISOMETRIC) {
                        // in isometric it's aligned to the bottom-center.
                        float width = tile.getWidth();
                        visual.move(0, -width * 0.5f, -height);
                    } else {
                        // In orthogonal orientation it's aligned to the
                        // bottom-left
                        visual.move(0, 0, -height);
                    }

                    obj.setVisual(visual);
                    break;
                }
                }

                double deg = obj.getRotation();
                if (deg != 0) {
                    float radian = (float) (FastMath.DEG_TO_RAD * deg);
                    Spatial visual = obj.getVisual();
                    // rotate the spatial clockwise
                    visual.rotate(0, -radian, 0);
                }

                float x = (float) obj.getX();
                float y = (float) obj.getY();

                // TODO if tileset .getTileHeight > map.getTileHeight, the object need to move down a little.
                
                
                Vector2f screenCoord = pixelToScreenCoords(x, y);
                obj.getVisual().move(screenCoord.x, i, screenCoord.y);
                layer.getVisual().attachChild(obj.getVisual());
            }
        }

        return layer.getVisual();
    }

    protected Spatial render(ImageLayer layer) {
        Mesh mesh = ObjectMesh.makeRectangle(mapSize.x, mapSize.y);
        Geometry geom = new Geometry(layer.getName(), mesh);
        geom.setMaterial(layer.getMaterial());
        geom.setQueueBucket(Bucket.Gui);

        layer.setVisual(geom);

        return layer.getVisual();
    }

    /******************************
     * Coordinates System Convert *
     ******************************/

    public abstract Vector2f pixelToScreenCoords(float x, float y);

    public abstract Point pixelToTileCoords(float x, float y);

    public abstract Vector2f tileToPixelCoords(float x, float y);

    public abstract Vector2f tileToScreenCoords(float x, float y);

    public abstract Vector2f screenToPixelCoords(float x, float y);

    public abstract Point screenToTileCoords(float x, float y);

    /**
     * Flip the tile
     * 
     * TODO how about change the uv coord?
     * 
     * @param visual The spatial for this tile.
     * @param tile The image of this tile.
     */
    protected void flip(Spatial visual, Tile tile) {
        if (tile.isFlippedHorizontally()) {
            visual.rotate(0, 0, FastMath.PI);
            visual.move(tile.getWidth(), 0, 0);
        }

        if (tile.isFlippedVertically()) {
            visual.rotate(FastMath.PI, 0, 0);
            visual.move(0, 0, tile.getHeight());
        }

        /*
         * TODO flip diagonally
         * <pre>
         * [      *]
         * [    *  ]
         * [  *    ]
         * [*      ]
         * </pre>
         */
        if (tile.isFlippedAntiDiagonally()) {

        }
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

    public Vector2f getMapDimension() {
        return new Vector2f(mapSize.x, mapSize.y);
    }
}
