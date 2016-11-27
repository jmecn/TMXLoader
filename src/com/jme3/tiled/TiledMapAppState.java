package com.jme3.tiled;

import java.util.List;

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
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.texture.Texture;

public class TiledMapAppState extends BaseAppState {

	private final Map map;
	private float viewColumns = 24;

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
	
	@Override
	protected void initialize(Application app) {
		this.assetManager = getApplication().getAssetManager();

		rpgCam.setParallelCamera(viewColumns);
		rpgCam.setOrientation(map.getOrientation());
		
		createSpatials();
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
			Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
			mat.setTexture("ColorMap", tex);
			mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
	
			/**
			 * The unit size of each Quad is (1, 1).
			 * only if this TileSet's (tileWidth, tileHeight) equals 
			 * the Map's (tileWidth, tileHeight)
			 */
			float tw = set.getTileWidth();
			float th = set.getTileHeight();
			float qx = tw / mw;
			float qy = th / mh;
			
			float[] vertices = new float[] {
					0, 0, 0,
					qx, 0, 0,
					qx, qy, 0,
					0, qy, 0 };
			float[] normals = new float[] {
					0, 0, 1,
					0, 0, 1,
					0, 0, 1,
					0, 0, 1 };
			short[] indexes = new short[] {
					0, 1, 2,
					0, 2, 3 };
	
			/**
			 * Calculate texCoords for each tile, and create a Geometry for it.
			 */
			float imageWidth = tex.getImage().getWidth();
			float imageHeight = tex.getImage().getHeight();
			int tileSize = set.size();
			for (int j = 0; j < tileSize; j++) {
				Tile tile = set.getTile(j);
				// skip null tile
				if (tile == null) {
					continue;
				}
	
				float x = tile.getX();
				float y = tile.getY();
	
				float u0 = x / imageWidth;
				float v0 = (imageHeight - y - th + 1) / imageHeight;
				float u1 = (x + tw - 1) / imageWidth;
				float v1 = (imageHeight - y - 1) / imageHeight;
	
				float[] texCoord = new float[] {
						u0, v0,
						u1, v0,
						u1, v1,
						u0, v1 };
				
				Mesh mesh = new Mesh();
				mesh.setBuffer(Type.Position, 3, vertices);
				mesh.setBuffer(Type.TexCoord, 2, texCoord);
				mesh.setBuffer(Type.Normal, 3, normals);
				mesh.setBuffer(Type.Index, 3, indexes);
				mesh.updateBound();
				mesh.setStatic();
	
				Geometry geom = new Geometry("tile#" + tile.getId(), mesh);
				geom.setMaterial(mat);
				geom.setQueueBucket(Bucket.Transparent);
	
				tile.setGeom(geom);
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
		z *= 0.1f;
		int mh = map.getHeight();
		
		int width = layer.getWidth();
		int height = layer.getHeight();
		Node node = new Node();
		for(int y=0; y<height; y++) {
			for(int x=0; x<width; x++) {
				final Tile tile = layer.getTileAt(x, y);
				if (tile == null) {
					continue;
				}
				
				Geometry tileGeom = tile.getGeom();
				if (tileGeom != null) {
					Geometry geom = tileGeom.clone();
					geom.setLocalTranslation(x, mh-y, z);
					node.attachChild(geom);
				}
			}
		}
		
		rootNode.attachChild(node);
	}
}
