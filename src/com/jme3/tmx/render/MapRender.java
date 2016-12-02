package com.jme3.tmx.render;

import java.util.List;
import java.util.logging.Logger;

import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Spatial;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.tmx.core.ImageLayer;
import com.jme3.tmx.core.Layer;
import com.jme3.tmx.core.ObjectLayer;
import com.jme3.tmx.core.ObjectNode;
import com.jme3.tmx.core.Tile;
import com.jme3.tmx.core.TileLayer;
import com.jme3.tmx.core.TiledMap;
import com.jme3.tmx.core.Tileset;
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
public abstract class MapRender {

	static Logger logger = Logger.getLogger(MapRender.class.getName());

	protected TiledMap map;
	protected int width;
	protected int height;
	protected float aspect = 1f;

	public MapRender(TiledMap map) {
		this.map = map;
		this.width = map.getWidth();
		this.height = map.getHeight();
		this.aspect = (float) map.getTileHeight() / map.getTileWidth();
		this.aspect = 1f;
	}

	public void createVisual() {
		List<Tileset> sets = map.getTileSets();
		for(int i=0; i<sets.size(); i++) {
			createVisual(sets.get(i));
		}
		
		int len = map.getLayerCount();
		for(int i=0; i<len; i++) {
			Layer layer = map.getLayer(i);
			
			// skip invisible layer
			if (!layer.isVisible()) {
				continue;
			}
			
			if (layer instanceof ObjectLayer) {
				createVisual((ObjectLayer) layer);
			}
		}
		
	}
	/**
	 * Create the visual part for every tile of a given Tileset.
	 * 
	 * @param tileset
	 *            the Tileset
	 * @return
	 */
	protected void createVisual(Tileset tileset) {

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
			geometry.setQueueBucket(Bucket.Translucent);

			if (useSharedImage) {
				geometry.setMaterial(sharedMat);
			} else {
				geometry.setMaterial(tile.getMaterial());
			}

			// TODO handle the animated tile
			if (tile.isAnimated()) {
				
			} else {
				
			}
			
			tile.setVisual(geometry);
		}

	}

	/**
	 * This value used to generate ellipse mesh.
	 */
	private final static int ELLIPSE_POINTS = 36;

	/**
	 * Create the visual part for every ObjectNode in a ObjectLayer.
	 * 
	 * @param layer
	 */
	protected void createVisual(ObjectLayer layer) {
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

				Geometry back = new Geometry("rectangle",
						ObjectMesh.makeRectangle(obj.getWidth(),
						obj.getHeight()));
				back.setMaterial(bgMat);

				com.jme3.scene.Node visual = new com.jme3.scene.Node(
						obj.getName());
				visual.attachChild(back);
				visual.attachChild(border);
				visual.setQueueBucket(Bucket.Translucent);

				obj.setVisual(visual);
				break;
			}
			case Ellipse: {
				Geometry border = new Geometry("border",
						ObjectMesh.makeEllipseBorder(obj.getWidth(),
								obj.getHeight(), ELLIPSE_POINTS));
				border.setMaterial(mat);

				Geometry back = new Geometry("ellipse", ObjectMesh.makeEllipse(
						obj.getWidth(), obj.getHeight(), ELLIPSE_POINTS));
				back.setMaterial(bgMat);

				com.jme3.scene.Node visual = new com.jme3.scene.Node(
						obj.getName());
				visual.attachChild(back);
				visual.attachChild(border);
				visual.setQueueBucket(Bucket.Translucent);

				obj.setVisual(visual);
				break;
			}
			case Polygon: {
				Geometry border = new Geometry("border", ObjectMesh.makePolyline(obj.getPoints(), true));
				border.setMaterial(mat);

				Geometry back = new Geometry("polygon", ObjectMesh.makePolygon(obj.getPoints()));
				back.setMaterial(bgMat);

				com.jme3.scene.Node visual = new com.jme3.scene.Node(
						obj.getName());
				visual.attachChild(back);
				visual.attachChild(border);
				visual.setQueueBucket(Bucket.Translucent);

				obj.setVisual(visual);
				break;
			}
			case Polyline: {
				Geometry geom = new Geometry(obj.getName());
				geom.setMesh(ObjectMesh.makePolyline(obj.getPoints(), false));
				geom.setMaterial(mat);
				geom.setQueueBucket(Bucket.Translucent);

				obj.setVisual(geom);
				break;
			}
			case Image: {
				Geometry geom = new Geometry(obj.getName(), new Quad(
						(float) obj.getWidth(), (float) -obj.getHeight()));
				geom.setMaterial(obj.getMaterial());

				obj.setVisual(geom);
				break;
			}
			case Tile: {
				Spatial visual = obj.getTile().getVisual().clone();
				obj.setVisual(visual);
				break;
			}
			}
		}
	}

	public abstract Spatial createTileLayer(TileLayer layer);

	public abstract Spatial createObjectLayer(ObjectLayer layer);

	public abstract Spatial createImageLayer(ImageLayer layer);

	/**
	 * convert tile location to screen location
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public abstract Vector3f tileLoc2ScreenLoc(float x, float y);

	public abstract Vector2f screenLoc2TileLoc(Vector3f location);
}
