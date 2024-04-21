package io.github.jmecn.tiled.render;

import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.*;
import io.github.jmecn.tiled.animation.AnimatedTileControl;
import io.github.jmecn.tiled.core.*;
import io.github.jmecn.tiled.enums.FillMode;
import io.github.jmecn.tiled.enums.Orientation;
import io.github.jmecn.tiled.math2d.Point;
import io.github.jmecn.tiled.render.shape.*;
import io.github.jmecn.tiled.util.ObjectMesh;

/**
 * desc: This class is used to create objects for the ObjectGroup
 *
 * @author yanmaoyuan
 */
public class ObjectRenderer {
    /**
     * This value used to generate ellipse mesh.
     */
    private static final int ELLIPSE_POINTS = 36;

    private final TiledMap map;
    private final Material mat;
    private final Material bgMat;

    public ObjectRenderer(ObjectGroup layer) {
        this.map = layer.getMap();

        ColorRGBA borderColor = layer.getColor();
        ColorRGBA bgColor = borderColor.mult(0.3f);

        mat = layer.getMaterial();
        bgMat = mat.clone();
        bgMat.setColor("Color", bgColor);

        ColorRGBA tintColor = layer.getTintColor();
        if (tintColor != null) {
            mat.setColor("TintColor", tintColor);
            bgMat.setColor("TintColor", tintColor);
        }
    }

    public Spatial create(MapObject obj) {
        switch (obj.getShape()) {
            case RECTANGLE: {
                rectangle(obj);
                break;
            }
            case ELLIPSE: {
                ellipse(obj);
                break;
            }
            case POLYGON: {
                polygon(obj);
                break;
            }
            case POLYLINE: {
                polyline(obj);
                break;
            }
            case POINT: {
                point(obj);
                break;
            }
            case IMAGE: {
                image(obj);
                break;
            }
            case TILE: {
                tile(obj);
                break;
            }
            case TEXT: {
                text(obj);
                break;
            }
            default: {
                break;
            }

        }

        Spatial visual = obj.getVisual();
        if (visual == null) {
            return null;
        }
        double deg = obj.getRotation();
        if (deg != 0) {
            float radian = (float) (FastMath.DEG_TO_RAD * deg);
            // rotate the spatial clockwise
            visual.rotate(0, -radian, 0);
        }

        return visual;
    }

    private void rectangle(MapObject obj) {
        Mesh borderMesh = new Rect((float)obj.getWidth(), (float)obj.getHeight(), true);
        Mesh backMesh = new Rect((float)obj.getWidth(), (float)obj.getHeight(), false);

        if (map.getOrientation() == Orientation.ISOMETRIC) {
            ObjectMesh.toIsometric(borderMesh, map.getTileWidth(), map.getTileHeight());
            ObjectMesh.toIsometric(backMesh, map.getTileWidth(), map.getTileHeight());
        }

        Geometry border = new Geometry("border", borderMesh);
        border.setMaterial(mat);
        border.setQueueBucket(RenderQueue.Bucket.Gui);

        Geometry back = new Geometry("rectangle", backMesh);
        back.setMaterial(bgMat);
        back.setQueueBucket(RenderQueue.Bucket.Gui);

        Node visual = new Node(obj.getName());
        visual.attachChild(back);
        visual.attachChild(border);
        visual.setQueueBucket(RenderQueue.Bucket.Gui);

        obj.setVisual(visual);
    }

    private void ellipse(MapObject obj) {
        Mesh borderMesh = new Ellipse((float)obj.getWidth(), (float)obj.getHeight(), ELLIPSE_POINTS, true);
        Mesh backMesh = new Ellipse((float)obj.getWidth(), (float)obj.getHeight(), ELLIPSE_POINTS, false);

        if (map.getOrientation() == Orientation.ISOMETRIC) {
            ObjectMesh.toIsometric(borderMesh, map.getTileWidth(), map.getTileHeight());
            ObjectMesh.toIsometric(backMesh, map.getTileWidth(), map.getTileHeight());
        }

        Geometry border = new Geometry("border", borderMesh);
        border.setMaterial(mat);
        border.setQueueBucket(RenderQueue.Bucket.Gui);

        Geometry back = new Geometry("ellipse", backMesh);
        back.setMaterial(bgMat);
        back.setQueueBucket(RenderQueue.Bucket.Gui);

        Node visual = new Node(obj.getName());
        visual.attachChild(back);
        visual.attachChild(border);
        visual.setQueueBucket(RenderQueue.Bucket.Gui);

        obj.setVisual(visual);
    }

    private void polygon(MapObject obj) {
        Mesh borderMesh = new Polygon(obj.getPoints(), true);
        Mesh backMesh = new Polygon(obj.getPoints(), false);

        if (map.getOrientation() == Orientation.ISOMETRIC) {
            ObjectMesh.toIsometric(borderMesh, map.getTileWidth(), map.getTileHeight());
            ObjectMesh.toIsometric(backMesh, map.getTileWidth(), map.getTileHeight());
        }

        Geometry border = new Geometry("border", borderMesh);
        border.setMaterial(mat);
        border.setQueueBucket(RenderQueue.Bucket.Gui);

        Geometry back = new Geometry("polygon", backMesh);
        back.setMaterial(bgMat);
        back.setQueueBucket(RenderQueue.Bucket.Gui);

        Node visual = new Node(obj.getName());
        visual.attachChild(back);
        visual.attachChild(border);
        visual.setQueueBucket(RenderQueue.Bucket.Gui);

        obj.setVisual(visual);
    }

    private void polyline(MapObject obj) {
        Mesh mesh = new Polyline(obj.getPoints(), false);

        if (map.getOrientation() == Orientation.ISOMETRIC) {
            ObjectMesh.toIsometric(mesh, map.getTileWidth(), map.getTileHeight());
        }

        Geometry visual = new Geometry(obj.getName(), mesh);
        visual.setMaterial(mat);
        visual.setQueueBucket(RenderQueue.Bucket.Gui);

        obj.setVisual(visual);
    }

    private void point(MapObject obj) {
        Geometry border = new Geometry("border", new Marker(16, ELLIPSE_POINTS, true));
        border.setMaterial(mat);
        border.setQueueBucket(RenderQueue.Bucket.Gui);

        Geometry back = new Geometry("marker", new Marker(16, ELLIPSE_POINTS, false));
        back.setMaterial(bgMat);
        back.setQueueBucket(RenderQueue.Bucket.Gui);

        Node visual = new Node(obj.getName());
        visual.attachChild(back);
        visual.attachChild(border);
        visual.setQueueBucket(RenderQueue.Bucket.Gui);

        obj.setVisual(visual);
    }

    private void image(MapObject obj) {
        Geometry visual = new Geometry(obj.getName(), new Rect((float)obj.getWidth(), (float)obj.getHeight(), false));
        TiledImage image = obj.getImage();
        visual.setMaterial(image.getMaterial());
        visual.setQueueBucket(RenderQueue.Bucket.Gui);

        obj.setVisual(visual);
    }

    private void tile(MapObject obj) {
        Geometry geometry = new Geometry(obj.getName(), getTileMesh(obj));
        geometry.setQueueBucket(RenderQueue.Bucket.Gui);

        Tile tile = obj.getTile();
        if (tile.getMaterial() != null) {
            geometry.setMaterial(tile.getMaterial());
        } else {
            geometry.setMaterial(tile.getTileset().getMaterial());
        }

        if (tile.isAnimated()) {
            geometry.setBatchHint(Spatial.BatchHint.Never);
            AnimatedTileControl control = new AnimatedTileControl(tile);
            geometry.addControl(control);
        }

        obj.setVisual(geometry);
    }

    private TileMesh getTileMesh(MapObject obj) {
        Tile tile = obj.getTile();
        float tw = tile.getWidth();

        Vector2f coord = new Vector2f(tile.getX(), tile.getY());
        Vector2f size = new Vector2f(tile.getWidth(), tile.getHeight());
        Vector2f offset;
        if (tile.getTileset() != null) {
            Tileset tileset = tile.getTileset();
            Point tileOffset = tileset.getTileOffset();
            offset = new Vector2f(tileOffset.getX(), tileOffset.getY());
            // scale the tile
            if (tileset.getFillMode() == FillMode.STRETCH) {
                size.set((int) obj.getWidth(), (int) obj.getHeight());
            }
        } else {
            offset = new Vector2f(0, 0);
        }

        Vector2f origin = new Vector2f(0, 0);// In orthogonal, it's aligned to the bottom-left
        if (map.getOrientation() == Orientation.ISOMETRIC) {
            origin.set(-tw * 0.5f, 0);// In isometric, it's aligned to the bottom-center.
        }

        // When the object has a gid set, then it is represented by
        // the image of the tile with that global ID. The image
        // alignment currently depends on the map orientation.
        return new TileMesh(coord, size, offset, origin, tile.getGid(), map.getOrientation());
    }

    private void text(MapObject obj) {
        // TODO render text
        ObjectText objectText = obj.getTextData();
    }

}
