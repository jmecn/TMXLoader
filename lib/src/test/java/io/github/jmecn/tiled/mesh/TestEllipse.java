package io.github.jmecn.tiled.mesh;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.system.AppSettings;
import io.github.jmecn.tiled.render.shape.Ellipse;

public class TestEllipse extends SimpleApplication {

    @Override
    public void simpleInitApp() {

        Mesh mesh = new Ellipse(300, 200, 24, false);
        
        Material mat = new Material(assetManager, "com/jme3/tmx/resources/Tiled.j3md");
        mat.setColor("Color", ColorRGBA.Red);

        Geometry geom = new Geometry("ellipse", mesh);
        geom.setMaterial(mat);
        
        viewPort.setBackgroundColor(ColorRGBA.DarkGray);
        rootNode.attachChild(geom.scale(1/32f));
        
        flyCam.setMoveSpeed(10);
        
    }

    public static void main(String[] args) {
        TestEllipse app = new TestEllipse();
        app.setSettings(new AppSettings(true));
        app.start();
    }

}
