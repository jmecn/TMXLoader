package io.github.jmecn.tiled.camera;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.GeometryComparator;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.debug.WireFrustum;
import com.jme3.scene.shape.Quad;
import com.jme3.shadow.ShadowUtil;
import com.jme3.system.AppSettings;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import io.github.jmecn.tiled.renderer.factory.DefaultMaterialFactory;
import io.github.jmecn.tiled.renderer.factory.MaterialFactory;
import io.github.jmecn.tiled.renderer.shape.TileMesh;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public class TestLayerDistance extends SimpleApplication {

    public static final int TILE_SIZE = 10;
    // how many tiles?
    private int w = 16;
    private int h = 9;
    // the layer distance
    private float layerDistance = 20f;
    private float step = layerDistance / (w * h);

    public static void main(String[] args) {
        AppSettings settings = new AppSettings(true);
        settings.setSamples(4);

        TestLayerDistance app = new TestLayerDistance();
        app.setSettings(settings);
        app.start();
    }

    @Override
    public void simpleInitApp() {

        // setup off-screen camera and scene
        Node offScene = createScene();
        Camera offCamera = setupCamera();
        Texture offTex = setupOffTexture(offCamera, offScene);

        // display the offCamera
        Node scene = createScene();
        Geometry cameraFrustum = createCameraFrustum(offCamera);

        Node offRoot = new Node("OffRoot");
        offRoot.attachChild(scene);
        offRoot.attachChild(cameraFrustum);
        offRoot.scale(0.1f);// make smaller

        rootNode.attachChild(offRoot);

        Geometry display = displayOffTexture(offTex);
        rootNode.attachChild(display);

        flyCam.setMoveSpeed(10f);
    }

    private Geometry createCameraFrustum(Camera cam) {
        Vector3f[] points = new Vector3f[8];
        for (int i = 0; i < 8; i++) {
            points[i] = new Vector3f();
        }

        ShadowUtil.updateFrustumPoints2(cam, points);

        WireFrustum frustum = new WireFrustum(points);
        Geometry geom = new Geometry("frustum", frustum);

        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.White);
        geom.setMaterial(mat);
        return geom;
    }

    private Node createScene() {
        Node scene = new Node("Scene");
        scene.setQueueBucket(RenderQueue.Bucket.Opaque);

        MaterialFactory materialFactory = new DefaultMaterialFactory(assetManager);
        // the tile map
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                float r = (float) y / h;
                float g = (float) x / w;
                ColorRGBA color = new ColorRGBA(r, g, 1f, 1f);
                Material mat = materialFactory.newMaterial(color);

                // position
                float px = x * 10f;
                float py = y * 10f;
                float pz = getZIndex(x, y);// z-index in the layer

                TileMesh mesh = new TileMesh(new Vector2f(), new Vector2f(TILE_SIZE, TILE_SIZE), new Vector2f(), new Vector2f(0, TILE_SIZE));
                Geometry geom = new Geometry("tile#" + x + "," + y, mesh);
                geom.setMaterial(mat);
                scene.attachChild(geom);

                geom.move(px, pz, py);
            }
        }

        // the character
        Material mat = materialFactory.newMaterial(ColorRGBA.Green);

        TileMesh mesh = new TileMesh(new Vector2f(), new Vector2f(TILE_SIZE, TILE_SIZE), new Vector2f(), new Vector2f());
        Geometry geom = new Geometry("Character", mesh);
        geom.setMaterial(mat);
        scene.attachChild(geom);

        // position
        float x = 24f;
        float y = 24f;
        float z = getZIndexTiled(x, y);
        geom.move(x, z, y);

        scene.updateGeometricState();
        return scene;
    }

    private float getZIndexTiled(float tx, float ty) {
        float x = tx / TILE_SIZE;
        float y = ty / TILE_SIZE;
        return getZIndex(x, y);
    }

    private float getZIndex(float x, float y) {
        float z = (y * w + x) * step;
        return (z < 0f) ? 0f : Math.min(z, layerDistance);
    }

    private Geometry displayOffTexture(Texture offTex) {
        // create a quad to display the texture
        Quad quad = new Quad(16, 9);
        Geometry geom = new Geometry("quad", quad);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setTexture("ColorMap", offTex);
        geom.setMaterial(mat);

        return geom;
    }

    private Texture setupOffTexture(Camera camera, Node offScene) {
        ViewPort offView = renderManager.createPreView("off-screen", camera);

        // sort by y-axis
        offView.getQueue().setGeometryComparator(RenderQueue.Bucket.Opaque, new GeometryComparator() {
            @Override
            public int compare(Geometry o1, Geometry o2) {
                float y1 = o1.getWorldTranslation().getY();
                float y2 = o2.getWorldTranslation().getY();
                return Float.compare(y1, y2);
            }
            @Override
            public void setCamera(Camera cam) {
                // nothing
            }
        });

        offView.attachScene(offScene);// attach the scene to the viewport to be rendered
        offView.setClearFlags(true, true, true);
        offView.setBackgroundColor(ColorRGBA.DarkGray);

        //setup frame buffer's texture
        Texture2D offTex = new Texture2D(camera.getWidth(), camera.getHeight(), Image.Format.RGBA8);
        offTex.setMinFilter(Texture.MinFilter.Trilinear);
        offTex.setMagFilter(Texture.MagFilter.Nearest);

        // create off-screen frame buffer
        FrameBuffer offBuffer = new FrameBuffer(camera.getWidth(), camera.getHeight(), 1);

        //setup frame buffer to use texture
        offBuffer.addColorTarget(FrameBuffer.FrameBufferTarget.newTarget(offTex));

        //set viewport to render to off-screen frame buffer.
        offView.setOutputFrameBuffer(offBuffer);

        return offTex;
    }

    private Camera setupCamera() {
        float near = -20f;
        float far = 0f;
        float width = 160f;
        float height = 90f;
        float halfWidth = width * 0.5f;
        float halfHeight = height * 0.5f;

        Camera cam = new Camera(160,90);
        cam.setParallelProjection(true);
        cam.setFrustum(near, far, -halfWidth, halfWidth, halfHeight, -halfHeight);
        cam.setLocation(new Vector3f(halfWidth, 0, halfHeight));
        cam.lookAtDirection(new Vector3f(0f, -1f, 0f), new Vector3f(0f, 0f, -1f));

        return cam;
    }
}
