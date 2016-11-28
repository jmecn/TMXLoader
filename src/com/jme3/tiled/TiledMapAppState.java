package com.jme3.tiled;

import java.util.List;
import java.util.logging.Logger;

import tiled.core.Map;
import tiled.core.MapLayer;
import tiled.core.Tile;
import tiled.core.TileLayer;
import tiled.core.TileSet;

import com.jme3.app.Application;
import com.jme3.app.FlyCamAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.light.AmbientLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.BatchNode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.texture.Texture;

public class TiledMapAppState extends BaseAppState {

	static Logger logger = Logger.getLogger(TiledMapAppState.class.getName());
	
	private final Map map;
	private Node rootNode;
	private AssetManager assetManager;
	private RPGCamAppState rpgCam;

	public TiledMapAppState(Map map) {
		this.map = map;
		this.rpgCam = new RPGCamAppState();
		rootNode = new Node("TileMapRoot");
	}

	public Map getMap() {
		return map;
	}
	
	public void setViewColumns(int viewColumn) {
		rpgCam.setParallelCamera(viewColumn);
	}
	
	@Override
	protected void initialize(Application app) {
		this.assetManager = getApplication().getAssetManager();

		rpgCam.setOrientation(map.getOrientation());
		
		createSpatials();
		
		rootNode.addLight(new AmbientLight());
	}

	@Override
	protected void cleanup(Application app) {
	}

	private FlyCamAppState flyCamAppState = null;
	@Override
	protected void onEnable() {
		((SimpleApplication) getApplication()).getRootNode().attachChild(
				rootNode);
		
		// disable flyCamAppState
		flyCamAppState = getStateManager().getState(FlyCamAppState.class);
		if (flyCamAppState != null) {
			getStateManager().detach(flyCamAppState);
		}
		
		if (rpgCam != null) {
			getStateManager().attach(rpgCam);
		}
	}

	@Override
	protected void onDisable() {
		rootNode.removeFromParent();
		
		if (flyCamAppState != null) {
			getStateManager().attach(flyCamAppState);
		}
		
		if (rpgCam != null) {
			getStateManager().detach(rpgCam);
		}
	}

	private void createSpatials() {
		createGeometryForTileSet();
		createGeometryForMaplayer();
	}
	
	/**
	 * Generate a quad mesh for every tile. All of them will share one
	 * Material. So we can use BatchNode for them.
	 */
	private void createGeometryForTileSet() {
		List<TileSet> sets = map.getTileSets();
		float mw = map.getTileWidth();
		float mh = map.getTileHeight();
		
		/**
		 * Generate a quad mesh for every tile. All of them will share one
		 * Material. So we can use BatchNode for them.
		 */
		int len = sets.size();
		for (int i = 0; i < len; i++) {
			TileSet set = sets.get(i);
			
			Texture tex = set.getTexture();
			
			Material mat = createMaterial(tex, true);
			ColorRGBA transColor = set.getTransparentColor();
			if (transColor != null) {
				mat.setColor("TransColor", transColor);
			}
	
			/**
			 * The unit size of each Quad is (1, 1).
			 * only if this TileSet's (tileWidth, tileHeight) equals 
			 * the Map's (tileWidth, tileHeight)
			 */
			float tw = set.getTileWidth();
			float th = set.getTileHeight();
			float qx = tw / mw;
			float qy = th / mh;
			
			/**
			 * Calculate texCoords for each tile, and create a Geometry for it.
			 */
			int tileSize = set.size();
			for (int j = 0; j < tileSize; j++) {
				Tile tile = set.getTile(j);
				// skip null tile
				if (tile == null) {
					continue;
				}

				Sprite sprite = new Sprite("tile#"+tile.getId());
				sprite.setSize(qx, qy);
				sprite.setTexCoordFromTile(tile);
				
				sprite.setMaterial(mat);
				sprite.setQueueBucket(Bucket.Translucent);
	
				tile.setGeom(sprite);
			}
		}
	}
	
	private void createGeometryForMaplayer() {
		int len = map.getLayerCount();
		for(int i=0; i<len; i++) {
			MapLayer layer = map.getLayer(i);
			
			// skip invisible layer
			if (!layer.isVisible()) {
				continue;
			}
			
			if (layer instanceof TileLayer) {
				viewTileLayer((TileLayer) layer, - len + i);
			}
		}
		
	}
	
	private void viewTileLayer(TileLayer layer, float z) {
		z *= 0.0f;
		int mh = map.getHeight();
		
		int width = layer.getWidth();
		int height = layer.getHeight();
		
		BatchNode bathNode = new BatchNode(layer.getName());
		for(int y=0; y<height; y++) {
			for(int x=0; x<width; x++) {
				final Tile tile = layer.getTileAt(x, y);
				if (tile == null) {
					continue;
				}
				
				Geometry tileGeom = tile.getGeom();
				Texture tileTex = tile.getTexture();
				
				if (tileGeom == null && tileTex == null) {
					logger.warning("Tile#" + tile.getId() + " has no texture.");
					continue;
				}
				
				Geometry geom = null;
				if (tileGeom != null) {
					geom = tileGeom.clone();
					
				} else {
					
					Material mat = createMaterial(tileTex, true);
					
					float tw = tile.getWidth() / map.getTileWidth();
					float th = tile.getHeight() / map.getTileWidth();
					
					Sprite sprite = new Sprite("tile#"+tile.getId());
					sprite.setSize(tw, th);
					sprite.setTexCoordFromTile(tile);
					
					sprite.setMaterial(mat);
					sprite.setQueueBucket(Bucket.Translucent);
					
					tile.setGeom(sprite);
					
					geom = sprite.clone();
				}
				
				geom.setLocalTranslation(x, mh-y, z);
				bathNode.attachChild(geom);
			}
		}
		bathNode.batch();
		
		rootNode.attachChild(bathNode);
	}
	
	private Material createMaterial(Texture tex, boolean useAlpha) {
		Material mat = new Material(assetManager, "Shader/TransColor.j3md");
		mat.setTexture("ColorMap", tex);
		
		if (useAlpha) {
			mat.setFloat("AlphaDiscardThreshold", 1f);
			mat.getAdditionalRenderState().setDepthWrite(true);
			mat.getAdditionalRenderState().setDepthTest(true);
			mat.getAdditionalRenderState().setColorWrite(true);
			mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
		}
		
		return mat;
	}
}
