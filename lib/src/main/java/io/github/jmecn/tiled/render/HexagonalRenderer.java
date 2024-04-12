package io.github.jmecn.tiled.render;

import com.jme3.material.Material;
import com.jme3.math.Vector2f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import io.github.jmecn.tiled.core.Tile;
import io.github.jmecn.tiled.core.TileLayer;
import io.github.jmecn.tiled.core.TiledMap;
import io.github.jmecn.tiled.enums.StaggerAxis;
import io.github.jmecn.tiled.enums.StaggerIndex;
import io.github.jmecn.tiled.math2d.Point;
import io.github.jmecn.tiled.render.grid.HexGrid;
import io.github.jmecn.tiled.render.grid.Hexagon;
import io.github.jmecn.tiled.render.shape.Rect;

/**
 * Hexagonal render
 * 
 * @author yanmaoyuan
 * 
 */
public class HexagonalRenderer extends OrthogonalRenderer {

    protected int sideLengthX;
    protected int sideLengthY;
    protected int sideOffsetX;
    protected int sideOffsetY;
    protected int rowHeight;
    protected int columnWidth;
    protected boolean staggerX;
    protected boolean staggerEven;
    protected int staggerIndex;

    public HexagonalRenderer(TiledMap map) {
        super(map);

        staggerX = map.getStaggerAxis() == StaggerAxis.X;
        staggerEven = map.getStaggerIndex() == StaggerIndex.EVEN;
        staggerIndex = staggerEven ? 0 : 1;

        sideLengthX = sideLengthY = 0;
        if (staggerX) {
            sideLengthX = map.getHexSideLength();
        } else {
            sideLengthY = map.getHexSideLength();
        }

        sideOffsetX = (map.getTileWidth() - sideLengthX) / 2;
        sideOffsetY = (map.getTileHeight() - sideLengthY) / 2;

        columnWidth = sideOffsetX + sideLengthX;
        rowHeight = sideOffsetY + sideLengthY;

        tileWidth = columnWidth + sideOffsetX;
        tileHeight = rowHeight + sideOffsetY;

        // The map size is the same regardless of which indexes are shifted.
        if (staggerX) {
            mapSize.set(width * columnWidth + sideOffsetX, height * (tileHeight + sideLengthY));

            if (width > 1) {
                mapSize.y += rowHeight;
            }

        } else {
            mapSize.set(width * (tileWidth + sideLengthX), height * rowHeight + sideOffsetY);

            if (height > 1) {
                mapSize.x += columnWidth;
            }
        }
    }

    private boolean doStaggerX(int x) {
        return staggerX && ((x & 1) ^ staggerIndex) == 0;
    }

    private boolean doStaggerY(int y) {
        return !staggerX && ((y & 1) ^ staggerIndex) == 0;
    }

    @Override
    public Spatial createTileGrid(Material material) {
        Hexagon mesh = new Hexagon(tileWidth, tileHeight, map.getHexSideLength(), map.getStaggerAxis(), true);
        // Rect mesh = new Rect(tileWidth, tileHeight, false);
        Geometry geom = new Geometry("HexGrid", mesh);
        geom.setMaterial(material);
        return geom;
    }

    @Override
    public Spatial render(TileLayer layer) {
        // instance the layer node
        if (layer.getVisual() == null) {
            Node layerNode = new Node("TileLayer#" + layer.getName());
            layerNode.setQueueBucket(Bucket.Gui);
            layer.setVisual(layerNode);
            layer.getParentVisual().attachChild(layerNode);
        }

        int tileCount;
        Point startTile = screenToTileCoords(0, 0);
        if (staggerX) {
            tileCount = renderStaggerX(layer, startTile);
        } else {
            tileCount = renderStaggerY(layer, startTile);
        }

        // make it thinner
        if (tileCount > 0) {
             layer.getVisual().setLocalScale(1, 1f / tileCount, 1);
        }

        return layer.getVisual();
    }

    private int renderStaggerX(TileLayer layer, Point startTile) {
        int tileZIndex = 0;
        boolean staggeredRow = doStaggerX(startTile.x);

        while (startTile.y < height) {
            Point rowTile = new Point(startTile.x, startTile.y);
            for (; rowTile.x < width; rowTile.x += 2) {
                // look up tile at rowTile
                final Tile tile = layer.getTileAt(rowTile.x, rowTile.y);
                if (tile == null || tile.getVisual() == null) {
                    continue;
                }

                if (layer.isNeedUpdateAt(rowTile.x, rowTile.y)) {

                    Spatial visual = tile.getVisual().clone();

                    flip(visual, tile);

                    // set its position with rowPos and tileZIndex
                    Vector2f pos = tileToScreenCoords(rowTile.x, rowTile.y);
                    visual.move(pos.x, tileZIndex, pos.y);
                    visual.setQueueBucket(Bucket.Gui);
                    layer.setSpatialAt(rowTile.x, rowTile.y, visual);

                }

                tileZIndex++;
            }

            if (staggeredRow) {
                startTile.x -= 1;
                startTile.y += 1;
                staggeredRow = false;
            } else {
                startTile.x += 1;
                staggeredRow = true;
            }
        }

        return tileZIndex;
    }
    private int renderStaggerY(TileLayer layer, Point startTile) {
        int tileZIndex = 0;

        for (; startTile.y < height; startTile.y++) {
            Point rowTile = startTile.clone();
            for (; rowTile.x < width; rowTile.x++) {
                // look up tile at rowTile
                final Tile tile = layer.getTileAt(rowTile.x, rowTile.y);
                if (tile == null || tile.getVisual() == null) {
                    continue;
                }

                if (layer.isNeedUpdateAt(rowTile.x, rowTile.y)) {

                    Spatial visual = tile.getVisual().clone();

                    flip(visual, tile);

                    // set its position with rowPos and tileZIndex
                    Vector2f pos = tileToScreenCoords(rowTile.x, rowTile.y);
                    visual.move(pos.x, tileZIndex, pos.y);
                    visual.setQueueBucket(Bucket.Gui);
                    layer.setSpatialAt(rowTile.x, rowTile.y, visual);
                }
                tileZIndex++;
            }
        }

        return tileZIndex;
    }

    @Override
    public void renderGrid(Node gridVisual, Material gridMaterial) {
        Mesh border = new Rect(mapSize.x, mapSize.y, true);
        Geometry rect = new Geometry("GridBorder", border);
        rect.setMaterial(gridMaterial);
        gridVisual.attachChild(rect);

        HexGrid grid = new HexGrid(width, height, map.getTileWidth(), map.getTileHeight(), map.getHexSideLength(), map.getStaggerAxis(), map.getStaggerIndex());
        Geometry geom = new Geometry("HexGrid", grid);
        geom.setMaterial(gridMaterial);
        gridVisual.attachChild(geom);
    }

    @Override
    public Vector2f tileToPixelCoords(float x, float y) {
        return tileToScreenCoords(x, y);
    }

    @Override
    public Point pixelToTileCoords(float x, float y) {
        return screenToTileCoords(x, y);
    }

    /**
     * Converts tile to screen coordinates. Sub-tile return values are not
     * supported by this renderer.
     */
    @Override
    public Vector2f tileToScreenCoords(float x, float y) {
        int tileX = (int) Math.floor(x);
        int tileY = (int) Math.floor(y);
        int pixelX;
        int pixelY;

        if (staggerX) {
            pixelY = tileY * (tileHeight + sideLengthY);
            if (doStaggerX(tileX))
                pixelY += rowHeight;

            pixelX = tileX * columnWidth;
        } else {
            pixelX = tileX * (tileWidth + sideLengthX);
            if (doStaggerY(tileY))
                pixelX += columnWidth;

            pixelY = tileY * rowHeight;
        }

        return new Vector2f(pixelX, pixelY);
    }

    /**
     * Converts screen to tile coordinates. Sub-tile return values are not
     * supported by this renderer.
     */
    @Override
    public Point screenToTileCoords(float x, float y) {

        if (staggerX) {
            x -= staggerEven ? tileWidth : sideOffsetX;
        } else {
            y -= staggerEven ? tileHeight : sideOffsetY;
        }

        // Start with the coordinates of a grid-aligned tile
        Point referencePoint = new Point(x / (columnWidth * 2), y / (rowHeight * 2));

        // Relative x and y position on the base square of the grid-aligned tile
        Point rel = new Point(x - referencePoint.x * columnWidth * 2, y - referencePoint.y * rowHeight * 2);

        // Adjust the reference point to the correct tile coordinates
        adjustReferencePoint(referencePoint);

        // Determine the nearest hexagon tile by the distance to the center
        Point[] centers = new Point[4];

        if (staggerX) {
            float left = sideLengthX * 0.5f;
            float centerX = left + columnWidth;
            float centerY = tileHeight * 0.5f;

            centers[0] = new Point(left, centerY);
            centers[1] = new Point(centerX, centerY - rowHeight);
            centers[2] = new Point(centerX, centerY + rowHeight);
            centers[3] = new Point(centerX + columnWidth, centerY);
        } else {
            float top = sideLengthY * 0.5f;
            float centerX = tileWidth * 0.5f;
            float centerY = top + rowHeight;

            centers[0] = new Point(centerX, top);
            centers[1] = new Point(centerX - columnWidth, centerY);
            centers[2] = new Point(centerX + columnWidth, centerY);
            centers[3] = new Point(centerX, centerY + rowHeight);
        }

        int nearest = 0;
        float minDist = Float.MAX_VALUE;

        for (int i = 0; i < 4; i++) {
            float dc = centers[i].distanceSquared(rel);
            if (dc < minDist) {
                minDist = dc;
                nearest = i;
            }
        }

        Point[] offsetsStaggerX = { new Point(0, 0), new Point(1, -1),
                new Point(1, 0), new Point(2, 0) };

        Point[] offsetsStaggerY = { new Point(0, 0), new Point(-1, 1),
                new Point(0, 1), new Point(0, 2) };

        final Point[] offsets = staggerX ? offsetsStaggerX : offsetsStaggerY;
        return referencePoint.add(offsets[nearest]);
    }

    /**
     * Adjust the reference point to the correct tile coordinates
     *
     * @param referencePoint the reference point
     */
    public void adjustReferencePoint(Point referencePoint) {
        if (staggerX) {
            referencePoint.x *= 2;
            if (staggerEven) {
                referencePoint.x++;
            }
        } else {
            referencePoint.y *= 2;
            if (staggerEven) {
                referencePoint.y++;
            }
        }
    }

    public Point topLeft(int x, int y) {
        if (!staggerX) {
            if (((y & 1) ^ staggerIndex) != 0)
                return new Point(x, y - 1);
            else
                return new Point(x - 1, y - 1);
        } else {
            if (((x & 1) ^ staggerIndex) != 0)
                return new Point(x - 1, y);
            else
                return new Point(x - 1, y - 1);
        }
    }

    public Point topRight(int x, int y) {
        if (!staggerX) {
            if (((y & 1) ^ staggerIndex) != 0) {
                return new Point(x + 1, y - 1);
            } else {
                return new Point(x, y - 1);
            }
        } else {
            if (((x & 1) ^ staggerIndex) != 0) {
                return new Point(x + 1, y);
            } else {
                return new Point(x + 1, y - 1);
            }
        }
    }

    public Point bottomLeft(int x, int y) {
        if (!staggerX) {
            if (((y & 1) ^ staggerIndex) != 0) {
                return new Point(x, y + 1);
            } else {
                return new Point(x - 1, y + 1);
            }
        } else {
            if (((x & 1) ^ staggerIndex) != 0) {
                return new Point(x - 1, y + 1);
            } else {
                return new Point(x - 1, y);
            }
        }
    }

    public Point bottomRight(int x, int y) {
        if (!staggerX) {
            if (((y & 1) ^ staggerIndex) != 0) {
                return new Point(x + 1, y + 1);
            } else {
                return new Point(x, y + 1);
            }
        } else {
            if (((x & 1) ^ staggerIndex) != 0) {
                return new Point(x + 1, y + 1);
            } else {
                return new Point(x + 1, y);
            }
        }
    }
}
