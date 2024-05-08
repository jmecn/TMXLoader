package io.github.jmecn.tiled.renderer;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.ViewPort;
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
import io.github.jmecn.tiled.core.TiledMap;
import io.github.jmecn.tiled.enums.RenderOrder;
import io.github.jmecn.tiled.renderer.factory.DefaultMaterialFactory;
import io.github.jmecn.tiled.renderer.factory.MaterialFactory;
import io.github.jmecn.tiled.renderer.queue.RenderOrderComparator;
import io.github.jmecn.tiled.renderer.queue.YAxisComparator;
import io.github.jmecn.tiled.renderer.shape.TileMesh;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public class TestRenderOrderSort extends SimpleApplication {

    // the layer distance
    private final RenderOrder renderOrder = RenderOrder.LEFT_DOWN;

    int w = 8;
    int h = 5;
    public static final int TILE_SIZE = 32;
    public static final float GRID_SIZE = 16f;
    TiledMap tiledMap;

    public static void main(String[] args) {
        AppSettings settings = new AppSettings(true);
        settings.setResolution(1280, 720);
        settings.setSamples(4);

        TestRenderOrderSort app = new TestRenderOrderSort();
        app.setSettings(settings);
        app.start();
    }

    TestRenderOrderSort() {
        tiledMap = new TiledMap(w, h);
        tiledMap.setTileWidth(TILE_SIZE);
        tiledMap.setTileHeight(TILE_SIZE);
        tiledMap.setRenderOrder(renderOrder);
    }

    @Override
    public void simpleInitApp() {
        cam.setLocation(new Vector3f(-4.364303f, 4.856053f, 18.433529f));
        cam.setRotation(new Quaternion(0.015008871f, 0.95650566f, -0.050019775f, 0.2870012f));

        OrthogonalRenderer renderer = new OrthogonalRenderer(tiledMap);
        renderer.setLayerDistance(20f);
        renderer.setLayerGap(1f);

        // setup off-screen camera and scene
        Node offScene = createScene(renderer, true);
        Camera offCamera = setupCamera();
        Texture offTex = setupOffTexture(offCamera, offScene);

        // display the offCamera
        Node scene = createScene(renderer, false);
        Geometry cameraFrustum = createCameraFrustum(offCamera);

        Node offRoot = new Node("OffRoot");
        offRoot.attachChild(scene);
        offRoot.attachChild(cameraFrustum);
        offRoot.scale(0.1f);// make smaller

        rootNode.attachChild(offRoot);

        Geometry display = displayOffTexture(offTex);
        display.move(0, 2f, 0);
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

    private Node createScene(OrthogonalRenderer renderer, boolean flag) {
        Node scene = new Node("Scene");
        scene.setQueueBucket(RenderQueue.Bucket.Opaque);

        MaterialFactory materialFactory = new DefaultMaterialFactory(assetManager);

        // the tile map
        renderer.visitTiles((x, y, z) -> {
            float r = (float) y / h;
            float g = (float) x / w;
            ColorRGBA color = new ColorRGBA(1-r, g, r * g, 1f);
            Material mat = materialFactory.newMaterial(color);

            // position
            float px = x * GRID_SIZE;
            float py = y * GRID_SIZE;
            float pz = renderer.getTileZAxis(x, y);
            if (flag) {
                pz = 0.5f;
            }
            // float pz = getZIndex(x, y);// z-index in the layer

            TileMesh mesh = new TileMesh(new Vector2f(), new Vector2f(TILE_SIZE, TILE_SIZE), new Vector2f(), new Vector2f(0, TILE_SIZE));
            Geometry geom = new Geometry("tile#" + x + "," + y, mesh);
            geom.setMaterial(mat);
            scene.attachChild(geom);

            geom.move(px, pz, py);
        });

        scene.updateGeometricState();
        return scene;
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
        offView.getQueue().setGeometryComparator(RenderQueue.Bucket.Opaque, new RenderOrderComparator(renderOrder));

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
