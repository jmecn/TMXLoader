package net.jmecn.tmx.mesh;

import java.util.ArrayList;
import java.util.List;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.system.AppSettings;
import com.jme3.tmx.util.ObjectMesh;

public class TestPolyline extends SimpleApplication {

	@Override
	public void simpleInitApp() {

		// points
		List<Vector2f> points = new ArrayList<Vector2f>();
		points.add(new Vector2f(0,0));
		points.add(new Vector2f(75,78));
		points.add(new Vector2f(133,82));
		points.add(new Vector2f(176,179));
		points.add(new Vector2f(274,183));
		
		Mesh mesh = ObjectMesh.makePolyline(points, false);
		
		Material mat = new Material(assetManager, "com/jme3/tmx/render/Tiled.j3md");
		mat.setColor("Color", ColorRGBA.Red);

		Geometry geom = new Geometry("polyline", mesh);
		geom.setMaterial(mat);
		
		viewPort.setBackgroundColor(ColorRGBA.DarkGray);
		rootNode.attachChild(geom.scale(1/32f));
		
		flyCam.setMoveSpeed(10);
	}
	
	public static void main(String[] args) {
		TestPolyline app = new TestPolyline();
		app.setSettings(new AppSettings(true));
		app.start();
	}

}
