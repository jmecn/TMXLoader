package net.jmecn.tmx;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Quad;
import com.jme3.system.AppSettings;
import com.jme3.tmx.util.ObjectTexture;

public class TestPainter extends SimpleApplication {

	@Override
	public void simpleInitApp() {

		ObjectTexture tex = new ObjectTexture(200, 200);
		tex.paintEllipse(100, 100, 80, 40);
		
		tex.paintRect(100, 100, 80, 40);
		
		tex.paintLine(3, 3, 20, 192, 2f, ColorRGBA.Red);
		
		Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		Geometry geom = new Geometry("~", new Quad(10, 10));
		geom.setMaterial(mat);
		geom.setQueueBucket(Bucket.Translucent);
		mat.setColor("Color", ColorRGBA.Red);
		mat.setTexture("ColorMap", tex.createTexture());
		
		viewPort.setBackgroundColor(ColorRGBA.DarkGray);
		rootNode.attachChild(geom);
	}

	public static void main(String[] args) {
		TestPainter app = new TestPainter();
		app.setSettings(new AppSettings(true));
		app.start();
	}

}
