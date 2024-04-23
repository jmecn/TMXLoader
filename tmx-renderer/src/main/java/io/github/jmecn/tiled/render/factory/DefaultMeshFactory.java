package io.github.jmecn.tiled.render.factory;

import com.jme3.math.Matrix3f;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.util.IntMap;
import io.github.jmecn.tiled.core.MapObject;
import io.github.jmecn.tiled.core.Tile;
import io.github.jmecn.tiled.core.TiledMap;
import io.github.jmecn.tiled.core.Tileset;
import io.github.jmecn.tiled.enums.FillMode;
import io.github.jmecn.tiled.enums.Orientation;
import io.github.jmecn.tiled.render.shape.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.FloatBuffer;
import java.util.List;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public class DefaultMeshFactory implements MeshFactory {

    private static final int ELLIPSE_POINTS = 36;// This value used to generate ellipse mesh.
    public static final float MARKER_RADIUS = 16f;

    static Logger logger = LoggerFactory.getLogger(DefaultMeshFactory.class);

    private final TiledMap tiledMap;
    private final Orientation orientation;
    private final float ratio;
    private final IntMap<TileMesh> cache;

    public DefaultMeshFactory(TiledMap tiledMap) {
        this.tiledMap = tiledMap;
        this.orientation = tiledMap.getOrientation();
        this.ratio = (float) tiledMap.getTileWidth() / tiledMap.getTileHeight();
        this.cache = new IntMap<>();
    }

    @Override
    public TileMesh newTileMesh(int tileId) {
        // clear the flag
        int gid = tileId & ~Tile.FLIPPED_MASK;

        Tile tile = tiledMap.getTileForTileGID(gid);
        if (tile == null) {
            throw new IllegalArgumentException("Tile not found, id: " + tileId);
        }

        if (tile.getGid() != tileId) {
            Tile t = tile.copy();
            t.setGid(tileId);
            tile = t;
        }

        return newTileMesh(tile);
    }

    @Override
    public TileMesh newTileMesh(Tile tile) {
        Tileset tileset = tile.getTileset();
        Vector2f offset = tileset.getTileOffset();
        Vector2f origin = new Vector2f(0, tiledMap.getTileHeight());

        Vector2f coord = new Vector2f(tile.getX(), tile.getY());
        Vector2f size = new Vector2f(tile.getWidth(), tile.getHeight());

        return new TileMesh(coord, size, offset, origin, tile.getGid(), orientation);
    }

    @Override
    public TileMesh getTileMesh(int tileId) {
        if (cache.containsKey(tileId)) {
            return cache.get(tileId);
        } else {
            TileMesh mesh = newTileMesh(tileId);
            cache.put(tileId, mesh);
            return mesh;
        }
    }

    @Override
    public TileMesh getTileMesh(Tile tile) {
        if (cache.containsKey(tile.getGid())) {
            return cache.get(tile.getGid());
        } else {
            TileMesh mesh = newTileMesh(tile);
            cache.put(tile.getGid(), mesh);
            return mesh;
        }
    }

    @Override
    public Mesh newObjectMesh(MapObject obj) {
        Mesh mesh;
        switch (obj.getShape()) {
            case RECTANGLE: {
                mesh = rectangle((float) obj.getWidth(), (float) obj.getHeight(), true);
                break;
            }
            case ELLIPSE: {
                mesh = ellipse((float) obj.getWidth(), (float) obj.getHeight(), true);
                break;
            }
            case POLYGON: {
                mesh = polygon(obj.getPoints(), true);
                break;
            }
            case POLYLINE: {
                mesh = polyline(obj.getPoints(), false);
                break;
            }
            case POINT: {
                mesh = marker(MARKER_RADIUS, true);
                break;
            }
            case IMAGE: {
                mesh = image((float) obj.getWidth(), (float) obj.getHeight());
                break;
            }
            case TEXT: {
                mesh = text((float) obj.getWidth(), (float) obj.getHeight());
                break;
            }
            case TILE: {
                mesh = tile(obj);
                break;
            }
            default: {
                mesh = null;
                logger.warn("Unsupported object type: {}", obj.getShape());
                break;
            }
        }
        return mesh;
    }

    public void toIsometric(Mesh mesh) {
        Matrix3f mat3 = new Matrix3f(
                1f, 0f, -1f,
                0f, 1f, 0f,
                ratio, 0f, ratio);

        VertexBuffer vb = mesh.getBuffer(VertexBuffer.Type.Position);
        FloatBuffer fb = (FloatBuffer) vb.getData();
        for (int i = 0; i < fb.capacity(); i += 3) {
            Vector3f v = new Vector3f(fb.get(i), 0f, fb.get(i + 2));
            mat3.multLocal(v);
            fb.put(i, v.x);
            fb.put(i + 2, v.z);
        }
        mesh.updateBound();
    }

    public Rect rectangle(float width, float height, boolean fill) {
        Rect mesh = new Rect(width, height, fill);
        if (orientation == Orientation.ISOMETRIC) {
            toIsometric(mesh);
        }
        return mesh;
    }

    public Ellipse ellipse(float width, float height, boolean fill) {
        Ellipse mesh = new Ellipse(width, height, ELLIPSE_POINTS, fill);
        if (orientation == Orientation.ISOMETRIC) {
            toIsometric(mesh);
        }
        return mesh;
    }

    public Polygon polygon(List<Vector2f> points, boolean fill) {
        Polygon mesh = new Polygon(points, fill);
        if (orientation == Orientation.ISOMETRIC) {
            toIsometric(mesh);
        }
        return mesh;
    }

    public Polyline polyline(List<Vector2f> points, boolean closePath) {
        Polyline mesh = new Polyline(points, closePath);
        if (orientation == Orientation.ISOMETRIC) {
            toIsometric(mesh);
        }
        return mesh;
    }

    public Marker marker(float radius, boolean fill) {
        return new Marker(radius, ELLIPSE_POINTS, fill);
    }

    public Rect image(float width, float height) {
        return new Rect(width, height, true);
    }

    public Rect text(float width, float height) {
        return new Rect(width, height, true);
    }

    public TileMesh tile(MapObject obj) {
        Tile tile = obj.getTile();
        float tw = tile.getWidth();

        Vector2f coord = new Vector2f(tile.getX(), tile.getY());
        Vector2f size = new Vector2f(tile.getWidth(), tile.getHeight());
        Vector2f offset;
        if (tile.getTileset() != null) {
            Tileset tileset = tile.getTileset();
            offset = tileset.getTileOffset();
            // scale the tile
            if (tileset.getFillMode() == FillMode.STRETCH) {
                size.set((int) obj.getWidth(), (int) obj.getHeight());
            }
        } else {
            offset = new Vector2f(0, 0);
        }

        Vector2f origin = new Vector2f(0, 0);// In orthogonal, it's aligned to the bottom-left
        if (orientation == Orientation.ISOMETRIC) {
            origin.set(-tw * 0.5f, 0);// In isometric, it's aligned to the bottom-center.
        }

        // When the object has a gid set, then it is represented by
        // the image of the tile with that global ID. The image
        // alignment currently depends on the map orientation.
        return new TileMesh(coord, size, offset, origin, tile.getGid(), orientation);
    }
}
