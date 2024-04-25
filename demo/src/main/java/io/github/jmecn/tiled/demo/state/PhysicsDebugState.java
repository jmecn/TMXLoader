package io.github.jmecn.tiled.demo.state;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import io.github.jmecn.tiled.demo.control.BodyControl;
import io.github.jmecn.tiled.renderer.factory.SpriteFactory;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.FixtureDef;

import java.util.ArrayList;
import java.util.List;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public class PhysicsDebugState extends BaseAppState {

    private final Node rootNode;

    private final SpriteFactory spriteFactory;

    public PhysicsDebugState(SpriteFactory spriteFactory) {
        this.spriteFactory = spriteFactory;
        this.rootNode = new Node("debug root");
    }

    @Override
    protected void initialize(Application app) {
        // nothing
    }

    @Override
    protected void cleanup(Application app) {
        // nothing
    }

    @Override
    protected void onEnable() {
        SimpleApplication simpleApp = (SimpleApplication) getApplication();
        simpleApp.getRootNode().attachChild(rootNode);
    }

    @Override
    protected void onDisable() {
        rootNode.removeFromParent();
    }

    public void addDebugShape(Body body, FixtureDef fixtureDef) {
        Node node = new Node("Body_" + System.currentTimeMillis());
        node.setLocalTranslation(0, 200, 0);
        node.addControl(new BodyControl(body));
        rootNode.attachChild(node);

        Mesh mesh = spriteFactory.getMeshFactory().rectangle(1f, 1f, false);
        Geometry geom = new Geometry("Body", mesh);
        Material mat = spriteFactory.newMaterial(ColorRGBA.Red);
        geom.setMaterial(mat);

        node.attachChild(geom);

        Shape shape = fixtureDef.shape;
        if (shape instanceof PolygonShape) {
            PolygonShape poly = (PolygonShape) shape;
            int count = poly.getVertexCount();
            List<Vector2f> vertices = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                Vec2 vec = poly.getVertex(i);
                Vector2f v = new Vector2f(vec.x, vec.y);
                vertices.add(v);
            }
            Mesh m = spriteFactory.getMeshFactory().polyline(vertices, true);
            Geometry geometry = new Geometry("Polygon", m);
            Material material = spriteFactory.newMaterial(ColorRGBA.Blue);
            geometry.setMaterial(material);

            node.attachChild(geometry);
        } else if (shape instanceof CircleShape) {
            CircleShape cir = (CircleShape) shape;
            float radius = cir.m_radius;
            Mesh m = spriteFactory.getMeshFactory().ellipse(radius * 2, radius * 2, false);
            Geometry geometry = new Geometry("Circle", m);
            Material material = spriteFactory.newMaterial(ColorRGBA.Blue);
            geometry.setMaterial(material);
            node.attachChild(geometry);
        }
    }
}
