package net.jmecn.tmx;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import tiled.core.Map;
import tiled.core.MapLayer;
import tiled.core.TileLayer;
import tiled.view.MapRenderer;
import tiled.view.OrthogonalRenderer;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.jme3.texture.plugins.AWTLoader;
import com.jme3.tmx.TmxLoader;
import com.jme3.tmx.TsxLoader;

/**
 * test tmx loader
 * @author yanmaoyuan
 *
 */
public class TestTmxLoader extends SimpleApplication {

	@Override
	public void simpleInitApp() {
		assetManager.registerLoader(TmxLoader.class, "tmx");
		assetManager.registerLoader(TsxLoader.class, "tsx");
		Map map = (Map) assetManager.loadAsset("Models/Examples/csvmap.tmx");
		rootNode.attachChild(createMap(map));
	}

	private Geometry createMap(Map map) {
		MapRenderer renderer = createRenderer(map);
		
		int width = map.getWidth() * map.getTileWidth();
	    int height = map.getHeight() * map.getTileHeight();
	    
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	    final Graphics2D g2d = (Graphics2D) image.getGraphics();
	    final Rectangle clip = g2d.getClipBounds();
	
	    // Draw a gray background
	    g2d.setPaint(new Color(100, 100, 100));
	    g2d.fill(clip);
	
	    // Draw each tile map layer
	    for (MapLayer layer : map) {
	        if (layer instanceof TileLayer) {
	            renderer.paintTileLayer(g2d, (TileLayer) layer);
	        }
	    }
	    
	    com.jme3.texture.Image img = new AWTLoader().load(image, false);
	    Texture tex = new Texture2D();
        tex.setName("none");
        tex.setImage(img);
	    
	    Quad quad = new Quad(width, height);
	    Geometry geom = new Geometry("tmx", quad);
	    Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
	    mat.setTexture("ColorMap", tex);
	    geom.setMaterial(mat);
    
	    return geom;
	}
	
    private static MapRenderer createRenderer(Map map) {
        switch (map.getOrientation()) {
            case ORTHOGONAL:
                return new OrthogonalRenderer(map);
            default:
                return null;
        }
    }
    
	public static void main(String[] args) {
		TestTmxLoader app = new TestTmxLoader();
		app.start();
	}

}
