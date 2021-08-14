package com.jme3.tmx.render;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.jme3.math.Vector2f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.tmx.core.Tile;
import com.jme3.tmx.core.TileLayer;
import com.jme3.tmx.core.TiledMap;
import com.jme3.tmx.core.TiledMap.Orientation;
import com.jme3.tmx.core.TiledMap.StaggerAxis;
import com.jme3.tmx.core.TiledMap.StaggerIndex;
import com.jme3.tmx.math2d.Point;

/**
 * Hexagonal render
 * 
 * @author yanmaoyuan
 * 
 */
public class HexagonalRenderer extends OrthogonalRenderer {

	static Logger logger = Logger.getLogger(HexagonalRenderer.class.getName());

	protected int tileWidth;
	protected int tileHeight;
	protected int sideLengthX;
	protected int sideOffsetX;
	protected int sideLengthY;
	protected int sideOffsetY;
	protected int rowHeight;
	protected int columnWidth;
	protected boolean staggerX = false;
	protected boolean staggerEven = false;
	protected int staggerIndex = 0;

	public HexagonalRenderer(TiledMap map) {
		super(map);

		staggerX = map.getStaggerAxis() == StaggerAxis.X;
		staggerEven = map.getStaggerIndex() == StaggerIndex.EVEN;
		staggerIndex = staggerEven ? 0 : 1;

		sideLengthX = sideLengthY = 0;
		if (map.getOrientation() == Orientation.HEXAGONAL) {
			if (staggerX) {
				sideLengthX = map.getHexSideLength();
			} else {
				sideLengthY = map.getHexSideLength();
			}
		}

		tileWidth = super.tileWidth & ~1;
		tileHeight = super.tileHeight & ~1;

		sideOffsetX = (tileWidth - sideLengthX) / 2;
		sideOffsetY = (tileHeight - sideLengthY) / 2;

		columnWidth = sideOffsetX + sideLengthX;
		rowHeight = sideOffsetY + sideLengthY;

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
	public Spatial render(TileLayer layer) {
		Point startTile = new Point(0, 0);
		int tileZIndex = 0;

		// instance the layer node
		if (layer.getVisual() == null) {
			Node layerNode = new Node("TileLayer#" + layer.getName());
			layerNode.setQueueBucket(Bucket.Gui);
			layer.setVisual(layerNode);
			
			map.getVisual().attachChild(layerNode);
		}
		
	    if (staggerX) {
	        boolean staggeredRow = doStaggerX(0);

	        for (; startTile.y < height;) {
	            Point rowTile = startTile.clone();
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
	    } else {
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
	    }
	    // make it thinner
 		if (tileZIndex > 0) {
 			layer.getVisual().setLocalScale(1, 1f / tileZIndex, 1);
 		}

 		return layer.getVisual();
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
		int pixelX, pixelY;

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

		if (staggerX)
			x -= staggerEven ? tileWidth : sideOffsetX;
		else
			y -= staggerEven ? tileHeight : sideOffsetY;

		// Start with the coordinates of a grid-aligned tile
		Point referencePoint = new Point(x / (columnWidth * 2), y
				/ (rowHeight * 2));

		// Relative x and y position on the base square of the grid-aligned tile
		Point rel = new Point(x - referencePoint.x * columnWidth * 2, y
				- referencePoint.y * rowHeight * 2);

		// Adjust the reference point to the correct tile coordinates
		if (staggerX) {
			referencePoint.x *= 2;
			if (staggerEven)
				referencePoint.x++;
		} else {
			referencePoint.y *= 2;
			if (staggerEven)
				referencePoint.y++;
		}

		// Determine the nearest hexagon tile by the distance to the center
		Point[] centers = new Point[4];

		if (staggerX) {
			int left = sideLengthX / 2;
			int centerX = left + columnWidth;
			int centerY = tileHeight / 2;

			centers[0] = new Point(left, centerY);
			centers[1] = new Point(centerX, centerY - rowHeight);
			centers[2] = new Point(centerX, centerY + rowHeight);
			centers[3] = new Point(centerX + columnWidth, centerY);
		} else {
			int top = sideLengthY / 2;
			int centerX = tileWidth / 2;
			int centerY = top + rowHeight;

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
			if (((y & 1) ^ staggerIndex) != 0)
				return new Point(x + 1, y - 1);
			else
				return new Point(x, y - 1);
		} else {
			if (((x & 1) ^ staggerIndex) != 0)
				return new Point(x + 1, y);
			else
				return new Point(x + 1, y - 1);
		}
	}

	public Point bottomLeft(int x, int y) {
		if (!staggerX) {
			if (((y & 1) ^ staggerIndex) != 0)
				return new Point(x, y + 1);
			else
				return new Point(x - 1, y + 1);
		} else {
			if (((x & 1) ^ staggerIndex) != 0)
				return new Point(x - 1, y + 1);
			else
				return new Point(x - 1, y);
		}
	}

	public Point bottomRight(int x, int y) {
		if (!staggerX) {
			if (((y & 1) ^ staggerIndex) != 0)
				return new Point(x + 1, y + 1);
			else
				return new Point(x, y + 1);
		} else {
			if (((x & 1) ^ staggerIndex) != 0)
				return new Point(x + 1, y + 1);
			else
				return new Point(x + 1, y);
		}
	}

	// TODO nothing to do with this code
	public List<Vector2f> tileToScreenPolygon(int x, int y) {

		ArrayList<Vector2f> polygon = new ArrayList<Vector2f>(8);
		polygon.add(new Vector2f(0, tileHeight - sideOffsetY));
		polygon.add(new Vector2f(0, sideOffsetY));
		polygon.add(new Vector2f(sideOffsetX, 0));
		polygon.add(new Vector2f(tileWidth - sideOffsetX, 0));
		polygon.add(new Vector2f(tileWidth, sideOffsetY));
		polygon.add(new Vector2f(tileWidth, tileHeight - sideOffsetY));
		polygon.add(new Vector2f(tileWidth - sideOffsetX, tileHeight));
		polygon.add(new Vector2f(sideOffsetX, tileHeight));
		
		Vector2f topRight = tileToScreenCoords(x, y);
		for(Vector2f p : polygon) {
			p.addLocal(topRight);
		}
		return polygon;
	}
}
