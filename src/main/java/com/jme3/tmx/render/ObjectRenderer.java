package com.jme3.tmx.render;

import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.tmx.core.*;
import com.jme3.tmx.enums.Orientation;
import com.jme3.tmx.util.ObjectMesh;

/**
 * desc: This class is used to create objects for the ObjectGroup
 *
 * @author yanmaoyuan
 * @date 2024/4/8
 */
public class ObjectRenderer {
    /**
     * This value used to generate ellipse mesh.
     */
    private static final int ELLIPSE_POINTS = 36;

    private ObjectGroup layer;
    private TiledMap map;

    private Material mat;
    private Material bgMat;

    public ObjectRenderer(ObjectGroup layer) {
        this.layer = layer;
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
        Mesh borderMesh = ObjectMesh.makeRectangleBorder(obj.getWidth(), obj.getHeight());
        Mesh backMesh = ObjectMesh.makeRectangle(obj.getWidth(), obj.getHeight());

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
        Mesh borderMesh = ObjectMesh.makeEllipseBorder(obj.getWidth(), obj.getHeight(), ELLIPSE_POINTS);
        Mesh backMesh = ObjectMesh.makeEllipse(obj.getWidth(), obj.getHeight(), ELLIPSE_POINTS);

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
        Mesh borderMesh = ObjectMesh.makePolyline(obj.getPoints(), true);
        Mesh backMesh = ObjectMesh.makePolygon(obj.getPoints());

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
        Mesh mesh = ObjectMesh.makePolyline(obj.getPoints(), false);

        if (map.getOrientation() == Orientation.ISOMETRIC) {
            ObjectMesh.toIsometric(mesh, map.getTileWidth(), map.getTileHeight());
        }

        Geometry visual = new Geometry(obj.getName(), mesh);
        visual.setMaterial(mat);
        visual.setQueueBucket(RenderQueue.Bucket.Gui);

        obj.setVisual(visual);
    }

    private void point(MapObject obj) {
        Geometry border = new Geometry("border", ObjectMesh.makeMarkerBorder(16, ELLIPSE_POINTS));
        border.setMaterial(mat);
        border.setQueueBucket(RenderQueue.Bucket.Gui);

        Geometry back = new Geometry("marker", ObjectMesh.makeMarker(16, ELLIPSE_POINTS));
        back.setMaterial(bgMat);
        back.setQueueBucket(RenderQueue.Bucket.Gui);

        Node visual = new Node(obj.getName());
        visual.attachChild(back);
        visual.attachChild(border);
        visual.setQueueBucket(RenderQueue.Bucket.Gui);

        obj.setVisual(visual);
    }

    private void image(MapObject obj) {
        Geometry visual = new Geometry(obj.getName(), ObjectMesh.makeRectangle(obj.getWidth(), obj.getHeight()));
        visual.setMaterial(obj.getMaterial());
        visual.setQueueBucket(RenderQueue.Bucket.Gui);

        obj.setVisual(visual);
    }

    private void tile(MapObject obj) {
        Tile tile = obj.getTile();

        Spatial visual = tile.getVisual().clone();
        visual.setQueueBucket(RenderQueue.Bucket.Gui);

        // flip(visual, obj.getTile());

        // When the object has a gid set, then it is represented by
        // the image of the tile with that global ID. The image
        // alignment currently depends on the map orientation.
        float th = tile.getHeight();
        if (map.getOrientation() == Orientation.ISOMETRIC) {
            // in isometric it's aligned to the bottom-center.
            float tw = tile.getWidth();
            visual.move(0, -tw * 0.5f, -th);
        } else {
            // In orthogonal orientation it's aligned to the
            // bottom-left
            visual.move(0, 0, -th);
        }

        obj.setVisual(visual);
    }

    private void text(MapObject obj) {
        // TODO render text
        ObjectText objectText = obj.getTextData();
    }

}
