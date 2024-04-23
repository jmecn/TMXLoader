package io.github.jmecn.tiled.render.shape;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.system.AppSettings;
import io.github.jmecn.tiled.enums.StaggerAxis;
import io.github.jmecn.tiled.enums.StaggerIndex;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public class TestHexGrid extends SimpleApplication {

    @Override
    public void simpleInitApp() {
        HexGrid grid = new HexGrid(10, 10, 14, 12, 6, StaggerAxis.X, StaggerIndex.EVEN);

        Geometry gridGeom = new Geometry("grid", grid);
        Material gridMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        gridMat.setColor("Color", ColorRGBA.White);
        gridGeom.setMaterial(gridMat);
        rootNode.attachChild(gridGeom);

        viewPort.setBackgroundColor(ColorRGBA.DarkGray);
        rootNode.scale(1/32f);

        flyCam.setMoveSpeed(10);
    }

    public static void main(String[] args) {
        TestHexGrid app = new TestHexGrid();
        app.setSettings(new AppSettings(true));
        app.start();
    }

}
