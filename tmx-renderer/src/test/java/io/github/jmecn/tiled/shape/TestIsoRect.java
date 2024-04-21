package io.github.jmecn.tiled.shape;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.system.AppSettings;
import io.github.jmecn.tiled.TiledConst;
import io.github.jmecn.tiled.shape.debug.IsoGrid;
import io.github.jmecn.tiled.util.ObjectMesh;

public class TestIsoRect extends SimpleApplication {

    @Override
    public void simpleInitApp() {
        int tileWidth = 130;
        int tileHeight = 66;
        Mesh mesh = new Rect(66, 66, true);
        ObjectMesh.toIsometric(mesh, tileWidth, tileHeight);

        Material mat = new Material(assetManager, TiledConst.TILED_J3MD);
        mat.setColor("Color", ColorRGBA.Red);

        Geometry geom = new Geometry("rectangle", mesh);
        geom.move(tileWidth * 0.5f, 0, 0f);
        geom.setMaterial(mat);

        // grid
        IsoGrid grid = new IsoGrid(1, 1, tileWidth, tileHeight);
        Geometry gridGeom = new Geometry("grid", grid);
        Material gridMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        gridMat.setColor("Color", ColorRGBA.White);
        gridGeom.setMaterial(gridMat);
        gridGeom.move(0f, -0.01f, 0f);
        rootNode.attachChild(gridGeom);

        viewPort.setBackgroundColor(ColorRGBA.DarkGray);
        rootNode.attachChild(geom);
        rootNode.scale(1/32f);
        
        flyCam.setMoveSpeed(10);
    }

    public static void main(String[] args) {
        TestIsoRect app = new TestIsoRect();
        app.setSettings(new AppSettings(true));
        app.start();
    }

}
