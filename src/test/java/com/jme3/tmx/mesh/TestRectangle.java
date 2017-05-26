package com.jme3.tmx.mesh;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.system.AppSettings;
import com.jme3.tmx.util.ObjectMesh;

public class TestRectangle extends SimpleApplication {

	@Override
	public void simpleInitApp() {

		Mesh mesh = ObjectMesh.makeRectangleBorder(30, 22);
		
		Material mat = new Material(assetManager, "com/jme3/tmx/resources/Tiled.j3md");
		mat.setColor("Color", ColorRGBA.Red);

		Geometry geom = new Geometry("rectangle", mesh);
		geom.setMaterial(mat);
		
		viewPort.setBackgroundColor(ColorRGBA.DarkGray);
		rootNode.attachChild(geom.scale(1/32f));
		
		flyCam.setMoveSpeed(10);
	}

	public static void main(String[] args) {
		TestRectangle app = new TestRectangle();
		app.setSettings(new AppSettings(true));
		app.start();
	}

}
