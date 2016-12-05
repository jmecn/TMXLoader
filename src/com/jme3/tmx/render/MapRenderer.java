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
import com.jme3.scene.Spatial.BatchHint;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.tmx.animation.AnimatedTileControl;
import com.jme3.tmx.core.ImageLayer;
import com.jme3.tmx.core.Layer;
import com.jme3.tmx.core.ObjectLayer;
import com.jme3.tmx.core.ObjectNode;
import com.jme3.tmx.core.Tile;
import com.jme3.tmx.core.TileLayer;
import com.jme3.tmx.core.TiledMap;
import com.jme3.tmx.core.Tileset;
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
	 */
	public Spatial render() {

		if (map == null) {
			return null;
		}

		// TODO set background color

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

			if (layer instanceof ObjectLayer) {
				visual = render((ObjectLayer) layer);
			}

			if (layer instanceof ImageLayer) {
				visual = render((ImageLayer) layer);
			}

			if (visual != null) {
				// this is a little magic to make let top layer block off the
				// bottom layer
				visual.setLocalTranslation(0, i, 0);
			}
		}
		return map.getVisual();
	}
	
	protected abstract Spatial render(TileLayer layer);

	/**
	 * Create the visual part for every ObjectNode in a ObjectLayer.
	 * 
	 * @param layer
	 */
	protected Spatial render(ObjectLayer layer) {
		List<ObjectNode> objects = layer.getObjects();
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
			switch (layer.getDraworder()) {
			case TOPDOWN:
				Collections.sort(objects, new CompareTopdown());
				break;
			case INDEX:
				Collections.sort(objects, new CompareIndex());
				break;
			}
		}
		
		for (int i = 0; i < len; i++) {
			ObjectNode obj = objects.get(i);

			if (!obj.isVisible()) {
				continue;
			}

			if (obj.isNeedUpdated()) {
				
				switch (obj.getObjectType()) {
				case Rectangle: {
					Geometry border = new Geometry("border",
							ObjectMesh.makeRectangleBorder(obj.getWidth(),
									obj.getHeight()));
					border.setMaterial(mat);
					border.setQueueBucket(Bucket.Gui);

					Geometry back = new Geometry("rectangle",
							ObjectMesh.makeRectangle(obj.getWidth(),
									obj.getHeight()));
					back.setMaterial(bgMat);
					back.setQueueBucket(Bucket.Gui);

					Node visual = new Node(obj.getName());
					visual.attachChild(back);
					visual.attachChild(border);
					visual.setQueueBucket(Bucket.Gui);

					obj.setVisual(visual);
					break;
				}
				case Ellipse: {
					Geometry border = new Geometry("border",
							ObjectMesh.makeEllipseBorder(obj.getWidth(),
									obj.getHeight(), ELLIPSE_POINTS));
					border.setMaterial(mat);
					border.setQueueBucket(Bucket.Gui);

					Geometry back = new Geometry("ellipse", ObjectMesh.makeEllipse(
							obj.getWidth(), obj.getHeight(), ELLIPSE_POINTS));
					back.setMaterial(bgMat);
					back.setQueueBucket(Bucket.Gui);

					Node visual = new Node(obj.getName());
					visual.attachChild(back);
					visual.attachChild(border);
					visual.setQueueBucket(Bucket.Gui);

					obj.setVisual(visual);
					break;
				}
				case Polygon: {
					Geometry border = new Geometry("border",
							ObjectMesh.makePolyline(obj.getPoints(), true));
					border.setMaterial(mat);
					border.setQueueBucket(Bucket.Gui);

					Geometry back = new Geometry("polygon",
							ObjectMesh.makePolygon(obj.getPoints()));
					back.setMaterial(bgMat);
					back.setQueueBucket(Bucket.Gui);

					Node visual = new Node(obj.getName());
					visual.attachChild(back);
					visual.attachChild(border);
					visual.setQueueBucket(Bucket.Gui);

					obj.setVisual(visual);
					break;
				}
				case Polyline: {
					Geometry visual = new Geometry("polyline",
							ObjectMesh.makePolyline(obj.getPoints(), false));
					visual.setMaterial(mat);
					visual.setQueueBucket(Bucket.Gui);

					obj.setVisual(visual);
					break;
				}
				case Image: {
					Geometry geom = new Geometry(obj.getName(),
							ObjectMesh.makeRectangle(obj.getWidth(),
									obj.getHeight()));
					geom.setMaterial(obj.getMaterial());
					geom.setQueueBucket(Bucket.Gui);

					obj.setVisual(geom);
					break;
				}
				case Tile: {
					Tile tile = obj.getTile();
					Spatial visual = tile.getVisual().clone();
					visual.setQueueBucket(Bucket.Gui);
					
					flip(visual, obj.getTile(), obj.isFlippedHorizontally(), obj.isFlippedVertically(),
							obj.isFlippedAntiDiagonally());
					
					obj.setVisual(visual);
					break;
				}
				}

				float deg = obj.getRotation();
				if (deg != 0) {
					float radian = FastMath.DEG_TO_RAD * deg;
					Spatial visual = obj.getVisual();
					// rotate the spatial clockwise
					visual.rotate(0, -radian, 0);
				}
				
				float x = (float) obj.getX();
				float y = (float) obj.getY();
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
	 * update the visual part of tileset
	 */
	public void updateVisual() {
		List<Tileset> sets = map.getTileSets();
		for (int i = 0; i < sets.size(); i++) {
			createVisual(sets.get(i));
		}
	}

	/**
	 * Create the visual part for every tile of a given Tileset.
	 * 
	 * @param tileset
	 *            the Tileset
	 * @return
	 */
	public void createVisual(Tileset tileset) {

		Texture texture = tileset.getTexture();
		Material sharedMat = null;
		Image image = null;
		/**
		 * If this tileset has a texture, means that most of the tiles are share
		 * the same TextureAltas, I just need to apply the shared material to
		 * their visual part.
		 * 
		 * Some tiles like "Player" or "Monster" maybe use their own texture to
		 * perform animation, should be handled differently. Such as create a
		 * com.jme3.scene.Node instead of com.jme3.scene.Geometry for them, and
		 * create a Control to make them animated.
		 * 
		 */
		boolean hasSharedImage = texture != null;

		if (hasSharedImage) {
			image = texture.getImage();
			sharedMat = tileset.getMaterial();
		}

		List<Tile> tiles = tileset.getTiles();
		int len = tiles.size();
		for (int i = 0; i < len; i++) {
			Tile tile = tiles.get(i);

			String name = "tile#" + tileset.getFirstgid() + "#" + tile.getId();

			/**
			 * If the tile has a texture, means that it don't use the shared
			 * material.
			 */
			boolean useSharedImage = tile.getTexture() == null;
			if (!useSharedImage) {
				if (tile.getMaterial() == null) {
					// this shouldn't happen, just in case someone uses Tiles
					// created by code.
					logger.warning("The tile mush has a material if it don't use sharedImage:"
							+ name);
					continue;
				}
			}

			float x = tile.getX();
			float y = tile.getY();
			float width = tile.getWidth();
			float height = tile.getHeight();

			/**
			 * Calculate the texCoord of this tile in an Image.
			 * 
			 * <pre>
			 * (u0,v1)    (u1,v1)
			 * *----------*
			 * |        * |
			 * |      *   |
			 * |    *     |
			 * |  *       |
			 * *----------*
			 * (u0,v0)    (u1,v0)
			 * </pre>
			 */
			float imageWidth;
			float imageHeight;
			if (useSharedImage) {
				imageWidth = image.getWidth();
				imageHeight = image.getHeight();
			} else {
				imageWidth = tile.getTexture().getImage().getWidth();
				imageHeight = tile.getTexture().getImage().getHeight();
			}

			float u0 = x / imageWidth;
			float v0 = (imageHeight - y - height) / imageHeight;
			float u1 = (x + width) / imageWidth;
			float v1 = (imageHeight - y) / imageHeight;

			float[] texCoord = new float[] { u0, v0, u1, v0, u1, v1, u0, v1 };

			/**
			 * Calculate the vertices' position of this tile.
			 * 
			 * <pre>
			 * 3          2
			 * *----------*
			 * |        * |
			 * |      *   |
			 * |    *     |
			 * |  *       |
			 * *----------*
			 * 0          1
			 * </pre>
			 */
			float[] vertices = new float[] { 0, 0, height, width, 0, height,
					width, 0, 0, 0, 0, 0 };

			short[] indexes = new short[] { 0, 1, 2, 0, 2, 3 };

			/**
			 * Normals are all the same: to Vector3f.UNIT_Y
			 */
			float[] normals = new float[] { 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0 };

			Mesh mesh = new Mesh();
			mesh.setBuffer(Type.Position, 3, vertices);
			mesh.setBuffer(Type.TexCoord, 2, texCoord);
			mesh.setBuffer(Type.Normal, 3, normals);
			mesh.setBuffer(Type.Index, 3, indexes);
			mesh.updateBound();
			mesh.setStatic();

			Geometry geometry = new Geometry(name, mesh);
			geometry.setQueueBucket(Bucket.Gui);

			if (useSharedImage) {
				geometry.setMaterial(sharedMat);
			} else {
				geometry.setMaterial(tile.getMaterial());
			}

			if (tile.isAnimated()) {
				geometry.setBatchHint(BatchHint.Never);

				AnimatedTileControl control = new AnimatedTileControl(tile);
				// TODO currently just set it to the first animation
				control.setAnim(0);
				geometry.addControl(control);
			}

			tile.setVisual(geometry);
		}

	}

	/**
	 * Create the visual part for every ObjectNode in a ObjectLayer.
	 * 
	 * @param layer
	 */
	public void createVisual(ObjectLayer layer) {
		final ColorRGBA borderColor = layer.getColor();

		Material mat = layer.getMaterial();

		// Material for background color
		Material bgMat = mat.clone();
		final ColorRGBA bgColor = borderColor.mult(0.3f);
		bgMat.setColor("Color", bgColor);

		List<ObjectNode> objects = layer.getObjects();
		int len = objects.size();
		for (int i = 0; i < len; i++) {
			ObjectNode obj = objects.get(i);

			switch (obj.getObjectType()) {
			case Rectangle: {
				Geometry border = new Geometry("border",
						ObjectMesh.makeRectangleBorder(obj.getWidth(),
								obj.getHeight()));
				border.setMaterial(mat);
				border.setQueueBucket(Bucket.Gui);

				Geometry back = new Geometry("rectangle",
						ObjectMesh.makeRectangle(obj.getWidth(),
								obj.getHeight()));
				back.setMaterial(bgMat);
				back.setQueueBucket(Bucket.Gui);

				Node visual = new Node(obj.getName());
				visual.attachChild(back);
				visual.attachChild(border);
				visual.setQueueBucket(Bucket.Gui);

				obj.setVisual(visual);
				break;
			}
			case Ellipse: {
				Geometry border = new Geometry("border",
						ObjectMesh.makeEllipseBorder(obj.getWidth(),
								obj.getHeight(), ELLIPSE_POINTS));
				border.setMaterial(mat);
				border.setQueueBucket(Bucket.Gui);

				Geometry back = new Geometry("ellipse", ObjectMesh.makeEllipse(
						obj.getWidth(), obj.getHeight(), ELLIPSE_POINTS));
				back.setMaterial(bgMat);
				back.setQueueBucket(Bucket.Gui);

				Node visual = new Node(obj.getName());
				visual.attachChild(back);
				visual.attachChild(border);
				visual.setQueueBucket(Bucket.Gui);

				obj.setVisual(visual);
				break;
			}
			case Polygon: {
				Geometry border = new Geometry("border",
						ObjectMesh.makePolyline(obj.getPoints(), true));
				border.setMaterial(mat);
				border.setQueueBucket(Bucket.Gui);

				Geometry back = new Geometry("polygon",
						ObjectMesh.makePolygon(obj.getPoints()));
				back.setMaterial(bgMat);
				back.setQueueBucket(Bucket.Gui);

				Node visual = new Node(obj.getName());
				visual.attachChild(back);
				visual.attachChild(border);
				visual.setQueueBucket(Bucket.Gui);

				obj.setVisual(visual);
				break;
			}
			case Polyline: {
				Geometry visual = new Geometry("polyline",
						ObjectMesh.makePolyline(obj.getPoints(), false));
				visual.setMaterial(mat);
				visual.setQueueBucket(Bucket.Gui);

				obj.setVisual(visual);
				break;
			}
			case Image: {
				Geometry geom = new Geometry(obj.getName(),
						ObjectMesh.makeRectangle(obj.getWidth(),
								obj.getHeight()));
				geom.setMaterial(obj.getMaterial());
				geom.setQueueBucket(Bucket.Gui);

				obj.setVisual(geom);
				break;
			}
			case Tile: {
				Tile tile = obj.getTile();
				Spatial visual = tile.getVisual().clone();
				visual.setQueueBucket(Bucket.Gui);
				obj.setVisual(visual);
				break;
			}
			}

			float deg = obj.getRotation();
			if (deg != 0) {
				float radian = FastMath.DEG_TO_RAD * deg;
				Spatial visual = obj.getVisual();
				// rotate the spatial clockwise
				visual.rotate(0, -radian, 0);
			}
		}

		// sort draw order
		switch (layer.getDraworder()) {
		case TOPDOWN:
			Collections.sort(objects, new CompareTopdown());
			break;
		case INDEX:
			Collections.sort(objects, new CompareIndex());
			break;
		}
	}

	/**
	 * Flip the tile
	 * 
	 * @param h
	 * @param v
	 * @param ad
	 */
	protected void flip(Spatial visual, Tile tile, boolean isHorizontally,
			boolean isVertically, boolean isAntiDiagonally) {
		// TODO
		if (isHorizontally) {
			visual.rotate(0, 0, FastMath.PI);
			visual.move(tile.getWidth(), 0, 0);
		}
		
		if (isVertically) {
			visual.rotate(FastMath.PI, 0, 0);
			visual.move(0, 0, tile.getHeight());
		}
		
		/**
		 * <pre>
		 * [      *]
		 * [    *  ]
		 * [  *    ]
		 * [*      ]
		 * </pre>
		 */
		if (isAntiDiagonally) {

		}
	}

	private final class CompareTopdown implements Comparator<ObjectNode> {
		@Override
		public int compare(ObjectNode o1, ObjectNode o2) {
			double a = o1.getY();
			double b = o2.getY();

			if (a > b)
				return 1;
			else if (a == b)
				return 0;
			else
				return -1;
		}
	}

	private final class CompareIndex implements Comparator<ObjectNode> {
		@Override
		public int compare(ObjectNode o1, ObjectNode o2) {
			int a = o1.getId();
			int b = o2.getId();

			if (a > b)
				return 1;
			else if (a == b)
				return 0;
			else
				return -1;
		}
	}

	public Vector2f getMapDimension() {
		return new Vector2f(mapSize.x, mapSize.y);
	}
}
