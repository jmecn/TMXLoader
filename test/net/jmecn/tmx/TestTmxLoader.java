package net.jmecn.tmx;

import java.util.List;

import tiled.core.Map;
import tiled.core.MapLayer;
import tiled.core.Tile;
import tiled.core.TileLayer;
import tiled.core.TileSet;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture;
import com.jme3.tmx.TmxLoader;

/**
 * test tmx loader
 * 
 * @author yanmaoyuan
 * 
 */
public class TestTmxLoader extends SimpleApplication {

	@Override
	public void simpleInitApp() {
		assetManager.registerLoader(TmxLoader.class, "tmx", "tsx");
		Map map = (Map) assetManager.loadAsset("Models/Examples/sewers.tmx");

		int z = 0;
		for (MapLayer layer : map) {
			if (layer instanceof TileLayer) {
				TileLayer tl = (TileLayer) layer;
				viewTileLayer(map, tl, z);
			}
			z++;
		}

		List<TileSet> tileSets = map.getTileSets();
		int len = tileSets.size();
		for (int i = 0; i < len; i++) {
			TileSet set = tileSets.get(0);
			//viewTileSet(set, i);
		}

		viewPort.setBackgroundColor(ColorRGBA.White);

		setCamera();
	}

	private void setCamera() {
		float frustumSize = 8f;
		// Setup first view
		cam.setParallelProjection(true);
		float aspect = (float) cam.getWidth() / cam.getHeight();
		cam.setFrustum(-1000, 1000, -aspect * frustumSize,
				aspect * frustumSize, frustumSize, -frustumSize);
		flyCam.setMoveSpeed(32);
	}

	private void viewTileSet(TileSet set, float zOrder) {
		float width = set.getTileHeight();
		float height = set.getTileWidth();
		Texture tex = set.getTexture();

		Quad quad = new Quad(width, height);
		Geometry geom = new Geometry("tmx", quad);
		Material mat = new Material(assetManager,
				"Common/MatDefs/Misc/Unshaded.j3md");
		mat.setTexture("ColorMap", tex);
		mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
		geom.setMaterial(mat);

		geom.setQueueBucket(Bucket.Transparent);
		geom.setLocalTranslation(0, 0, zOrder);
		rootNode.attachChild(geom);
	}

	private void viewTileLayer(Map map, TileLayer layer, float z) {
		z *= 0.01f;
		int mw = map.getWidth();
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
				
				int a = tile.getX();
				int b = tile.getY();
				
				
				TileSet tileSet = tile.getTileSet();
				Texture tex = tileSet.getTexture();
				float w = tex.getImage().getWidth();
				float h = tex.getImage().getHeight();
				int tw = tile.getWidth() - 1;
				int th = tile.getHeight() - 1;
				
				float u0 = (float)(a + 1) / w;
				float v0 = (float)(h - b - th) / h;
				float u1 = (float)(a + tw) / w;
				float v1 = (float)(h - b - 1) / h;
				
				System.out.println("leftBottom:(" + u0 + ", " + v0 + ") rightTop:(" + u1 + "," + v1 + ")");
				
				Mesh mesh = new Mesh();
				
		        mesh.setBuffer(Type.Position, 3, new float[]{
		        		x,		mh-y,			z,
                        x+1,	mh-y,			z,
                        x+1,	mh-y+1,	z,
                        x,		mh-y+1,	z
                        });
				mesh.setBuffer(Type.TexCoord, 2, new float[]{u0, v0,
			                            u1, v0,
			                            u1, v1,
			                            u0, v1});
				mesh.setBuffer(Type.Normal, 3, new float[]{0, 0, 1,
			                      0, 0, 1,
			                      0, 0, 1,
			                      0, 0, 1});
				mesh.setBuffer(Type.Index, 3, new short[]{0, 1, 2,
			                         0, 2, 3});
				
				mesh.updateBound();
				mesh.setStatic();
				
				Geometry geom = new Geometry("tmx", mesh);
				Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
				mat.setTexture("ColorMap", tex);
				mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
				geom.setMaterial(mat);

				geom.setQueueBucket(Bucket.Transparent);
				node.attachChild(geom);

			}
		}
		
		rootNode.attachChild(node);
	}

	public static void main(String[] args) {
		TestTmxLoader app = new TestTmxLoader();
		app.start();
	}

}
