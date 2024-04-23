package io.github.jmecn.tiled.demo.state;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.input.InputManager;
import com.jme3.math.*;
import com.jme3.renderer.Camera;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import io.github.jmecn.tiled.core.GroupLayer;
import io.github.jmecn.tiled.core.Layer;
import io.github.jmecn.tiled.math2d.Point;
import io.github.jmecn.tiled.core.TiledMap;
import io.github.jmecn.tiled.enums.ZoomMode;
import io.github.jmecn.tiled.render.*;
import io.github.jmecn.tiled.render.factory.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ViewState will create a Spatial for com.jme3.tmx.core.TiledMap
 * 
 * @author yanmaoyuan
 * 
 */
public class ViewState extends BaseAppState {

    static Logger logger = LoggerFactory.getLogger(ViewState.class);
    public static final String INIT_ERROR = "inputManager is null. Please initialize ViewState first.";

    // Tiled Map
    private TiledMap map;
    private MapRenderer mapRenderer;

    // The rootNode
    private final Node rootNode;
    private final Quaternion mapRotation;

    // The parallax
    private boolean isParallaxEnabled = true;
    private final Vector2f parallaxOrigin = new Vector2f(0, 0);
    private final Vector2f parallaxDistance = new Vector2f(0, 0);

    // The mapNode
    private final Vector3f mapTranslation;
    private float mapScale;

    // The camera
    private Camera cam;
    private ViewPort viewPort;
    private InputManager inputManager;
    private MaterialFactory materialFactory;
    private final Vector2f screenDimension;
    private final Vector2f mapDimension;

    // The
    private float viewColumns;
    protected float moveSpeed = 10f;// in tiles
    protected float zoomSpeed = 1f;// in tiles
    /**
     * update scene when the map updated
     */
    private boolean isMapUpdated = true;

    private ZoomMode zoomMode = ZoomMode.MAP;

    // variables used to drag map
    private final Vector3f startLoc = new Vector3f();
    private final Vector2f startPos = new Vector2f();
    private final Vector2f stopPos = new Vector2f();

    // variables used to move camera
    private final Vector3f vel = new Vector3f();
    private final Vector3f pos = new Vector3f();

    /**
     * Default constructor
     */
    public ViewState() {
        this(null);
    }

    /**
     * Constructor
     * 
     * @param map the tiled map
     */
    public ViewState(TiledMap map) {
        this(map, 12f);
    }

    public ViewState(TiledMap map, float viewColumns) {
        screenDimension = new Vector2f();
        mapDimension = new Vector2f();

        mapTranslation = new Vector3f();
        mapScale = 1f;

        rootNode = new Node("Tiled Map Root");
        rootNode.setQueueBucket(Bucket.Gui);

        // translate the scene from XOZ plane to XOY plane
        mapRotation = new Quaternion();
        mapRotation.fromAngles(FastMath.HALF_PI, 0, 0);
        rootNode.setLocalRotation(mapRotation);

        setMap(map);

        this.viewColumns = viewColumns;
    }

    @Override
    protected void initialize(Application app) {
        inputManager = app.getInputManager();
        AssetManager assetManager = app.getAssetManager();
        materialFactory = new DefaultMaterialFactory(assetManager);
        viewPort = app.getViewPort();
        cam = app.getCamera();

        screenDimension.set(cam.getWidth(), cam.getHeight());

        // move the rootNode to top-left corner of the screen
        rootNode.setLocalTranslation(0, screenDimension.y, 0);

        float near = -1000f;
        float far = 1000f;
        float halfWidth = screenDimension.x * 0.5f;
        float halfHeight = screenDimension.y * 0.5f;
        cam.setFrustum(near, far, -halfWidth, halfWidth, halfHeight, -halfHeight);

        cam.setParallelProjection(true);
        cam.lookAtDirection(new Vector3f(0f, -1f, 0f), new Vector3f(0f, 0f, -1f));
        cam.setLocation(new Vector3f(halfWidth, 0, halfHeight));
        logger.info("cam: {}, direction:{}", cam.getLocation(), cam.getDirection());

        if (this.map != null) {
            viewPort.setBackgroundColor(map.getBackgroundColor());
            mapRenderer.getSpriteFactory().setMaterialFactory(materialFactory);
        }
    }

    @Override
    protected void cleanup(Application app) {
        rootNode.detachAllChildren();
    }

    @Override
    protected void onEnable() {
        ((SimpleApplication) getApplication()).getRootNode().attachChild(rootNode);
    }

    @Override
    protected void onDisable() {
        rootNode.removeFromParent();
    }

    @Override
    public void update(float tpf) {
        Spatial spatial;
        if (mapRenderer != null) {
            spatial = mapRenderer.render();

            if (isMapUpdated) {
                // move it to the left bottom of screen space
                mapDimension.set(mapRenderer.getMapDimensionF());
                mapTranslation.set(screenDimension.x * 0.5f, 0, screenDimension.y * 0.5f);
                mapScale = getMapScale();
                spatial.setLocalTranslation(mapTranslation);
                spatial.setLocalScale(mapScale, 1f, mapScale);
                calculateMapParallax();
                
                isMapUpdated = false;
            }
        }
    }
    /**
     * Set map. It will instance a new MapRenderer and create visual parts for this map.
     * 
     * @param map the tiled map
     */
    public void setMap(TiledMap map) {
        if (map == null) {
            return;
        }

        rootNode.detachAllChildren();
        
        if (viewPort != null) {
            viewPort.setBackgroundColor(map.getBackgroundColor());
        }

        if (this.map != map) {
            this.map = map;
        }

        switch (map.getOrientation()) {
            case ORTHOGONAL:
                mapRenderer = new OrthogonalRenderer(map);
                break;
            case ISOMETRIC:
                mapRenderer = new IsometricRenderer(map);
                break;
            case HEXAGONAL:
                mapRenderer = new HexagonalRenderer(map);
                break;
            case STAGGERED:
                mapRenderer = new StaggeredRenderer(map);
                break;
            default:
                // use OrthogonalRenderer by default
                mapRenderer = new OrthogonalRenderer(map);
        }

        // new sprite factory. the materialFactory is set in initialize()
        DefaultSpriteFactory spriteFactory = new DefaultSpriteFactory();
        spriteFactory.setMeshFactory(new DefaultMeshFactory(map));
        spriteFactory.setMaterialFactory(materialFactory);

        mapRenderer.setSpriteFactory(spriteFactory);

        // create the visual part for the map
        rootNode.attachChild(mapRenderer.getRootNode());

        Vector2f loc = mapRenderer.pixelToScreenCoords(map.getParallaxOriginX(), map.getParallaxOriginY());
        mapTranslation.set(loc.x, 0, loc.y);
        isMapUpdated = true;
    }

    public TiledMap getMap() {
        return map;
    }

    public MapRenderer getMapRenderer() {
        return mapRenderer;
    }

    public Vector3f getLocation(float x, float y) {
        Vector2f loc = mapRenderer.tileToScreenCoords(x, y);
        return new Vector3f(loc.x, loc.y, 999f - map.getLayerCount());
    }

    public void moveToTile(float x, float y) {
        Vector2f tilePos = mapRenderer.tileToScreenCoords(x, y).multLocal(getMapScale());
        Vector2f camPos = new Vector2f(cam.getLocation().x, screenDimension.y - cam.getLocation().z);
        camPos.subtract(tilePos, tilePos);
        mapTranslation.set(tilePos.x, 0, tilePos.y);
        moveMapVisual();
    }

    public void moveToPixel(float x, float y) {
        Vector2f pixelPos = mapRenderer.pixelToScreenCoords(x, y).multLocal(getMapScale());
        Vector2f camPos = new Vector2f(cam.getLocation().x, screenDimension.y - cam.getLocation().z);
        camPos.subtract(pixelPos, pixelPos);
        mapTranslation.set(pixelPos.x, 0, pixelPos.y);
        moveMapVisual();
    }

    private void moveMapVisual() {
        float x = (float) Math.floor(mapTranslation.x);
        float y = (float) Math.floor(mapTranslation.y);
        float z = (float) Math.floor(mapTranslation.z);
        mapRenderer.getRootNode().setLocalTranslation(x, y, z);

        calculateMapParallax();
    }

    private void calculateMapParallax() {
        if (isParallaxEnabled) {
            // record the parallax origin
            parallaxOrigin.set(map.getParallaxOriginX(), map.getParallaxOriginY());
            Vector2f current = getCameraPixelCoordinate();
            current.subtract(parallaxOrigin, parallaxDistance);
        } else {
            parallaxDistance.set(0, 0);
        }

        applyParallax(parallaxDistance);
    }

    private void applyParallax(Vector2f distance) {
        for (Layer layer : mapRenderer.getSortedLayers()) {
            if (!layer.isVisible()) {
                continue;
            }
            applyParallax(layer, distance);
        }
    }

    private void applyParallax(Layer layer, Vector2f distance) {
        if (!layer.isVisible()) {
            return;
        }

        if (layer instanceof GroupLayer) {
            for (Layer child : ((GroupLayer) layer).getLayers()) {
                applyParallax(child, distance);
            }
        } else {
            // When the camera moves, the layer moves in relation to the camera by a factor of the parallax scrolling factor.
            // As move mapVisual means move the map, so we need to move the layer in the opposite direction, 1.0-parallaxFactor
            float x = (float)((1.0 - layer.getRenderParallaxX()) * distance.x);
            float y = (float)((1.0 - layer.getRenderParallaxY()) * distance.y);

            Node layerNode = mapRenderer.getLayerNode(layer);
            float z = layerNode.getLocalTranslation().y;
            layerNode.setLocalTranslation(x, z, y);
        }
    }

    public void setMapScale(float scale) {
        mapScale = scale;
        if (map != null) {
            viewColumns = screenDimension.x / (map.getTileWidth() * mapScale);
            mapRenderer.getRootNode().setLocalScale(mapScale, 1, mapScale);
            isMapUpdated = true;
        }
    }

    public float getMapScale() {
        if (map != null) {
            float pixel = map.getTileWidth() * viewColumns;
            mapScale = screenDimension.x / pixel;
            mapRenderer.getRootNode().setLocalScale(mapScale, 1, mapScale);
        }
        
        return mapScale;
    }

    public Vector3f getMapTranslation() {
        return mapTranslation;
    }

    public Quaternion getMapRotation() {
        return mapRotation;
    }

    /**
     * Set view columns. It changes the number of tiles you can see in a row.
     * 
     * @param columns column count in a row
     */
    public void setViewColumn(float columns) {
        this.viewColumns = columns;
        this.mapScale = getMapScale();
    }

    /**
     * Sets the move speed. The speed is given in world units per second.
     * 
     * @param moveSpeed move speed
     */
    public void setMoveSpeed(float moveSpeed) {
        this.moveSpeed = moveSpeed;
    }

    /**
     * Gets the move speed. The speed is given in world units per second.
     * 
     * @return moveSpeed
     */
    public float getMoveSpeed() {
        return moveSpeed;
    }

    public Point getCameraTileCoordinate() {
        if (inputManager == null) {
            throw new IllegalStateException(INIT_ERROR);
        }
        Vector2f center = new Vector2f(screenDimension.x * 0.5f, screenDimension.y * 0.5f);
        center = center.subtract(mapTranslation.x, mapTranslation.z).divideLocal(mapScale);
        return getMapRenderer().screenToTileCoords(center.x, center.y);
    }

    public Vector2f getCameraPixelCoordinate() {
        if (inputManager == null) {
            throw new IllegalStateException(INIT_ERROR);
        }
        Vector2f center = new Vector2f(screenDimension.x * 0.5f, screenDimension.y * 0.5f);
        center = center.subtract(mapTranslation.x, mapTranslation.z).divideLocal(mapScale);
        return getMapRenderer().screenToPixelCoords(center.x, center.y);
    }

    public Vector2f getCameraScreenCoordinate() {
        if (inputManager == null) {
            throw new IllegalStateException(INIT_ERROR);
        }
        return new Vector2f(cam.getLocation().x, cam.getLocation().z);
    }

    /**
     * Get the cursor tile coordinate in the map
     *
     * @return The tile coordinate of the cursor
     */
    public Point getCursorTileCoordinate() {
        if (inputManager == null) {
            throw new IllegalStateException(INIT_ERROR);
        }
        Vector2f cursor = getCursorPixelCoordinate();
        return getMapRenderer().screenToTileCoords(cursor.x, cursor.y);
    }

    /**
     * Get the cursor pixel coordinate in the map
     *
     * @return The pixel coordinate of the cursor
     */
    public Vector2f getCursorPixelCoordinate() {
        if (inputManager == null) {
            throw new IllegalStateException(INIT_ERROR);
        }
        Vector2f cursor = inputManager.getCursorPosition();
        cursor = new Vector2f(cursor.x, screenDimension.y - cursor.y);
        return cursor.subtractLocal(mapTranslation.x, mapTranslation.z).divideLocal(mapScale);
    }

    public Vector2f getCursorScreenCoordinate() {
        if (inputManager == null) {
            throw new IllegalStateException(INIT_ERROR);
        }
        Vector2f cursor = inputManager.getCursorPosition();
        return new Vector2f(cursor.x, cursor.y);
    }

    /**
     * move camera
     * 
     * @param value move value
     * @param sideways move up-down or right-left
     */
    public void move(float value, boolean sideways) {
        pos.set(mapTranslation);

        if (sideways) {
            vel.set(1f, 0f, 0f);
        } else {
            vel.set(0f, 0f, -1f);
        }
        vel.multLocal(value * moveSpeed * map.getTileWidth() * mapScale);

        pos.addLocal(vel);

        mapTranslation.set(pos);
        moveMapVisual();
    }

    public void setZoomMode(ZoomMode zoomMode) {
        this.zoomMode = zoomMode;
    }

    public ZoomMode getZoomMode() {
        return zoomMode;
    }

    /**
     * zoom camera
     * 
     * @param value zoom value
     */
    public void zoomCamera(float value) {
        // store the current position
        Vector2f pixel = null;
        if (zoomMode == ZoomMode.CAMERA) {
            pixel = getCameraPixelCoordinate();
        }

        viewColumns += zoomSpeed * value;

        // at less see 1 tile on screen
        if (viewColumns < 1f) {
            viewColumns = 1f;
        }

        setViewColumn(viewColumns);

        // restore the position
        if (zoomMode == ZoomMode.CAMERA && pixel != null) {
            moveToPixel(pixel.x, pixel.y);
        }
    }

    public boolean isParallaxEnabled() {
        return isParallaxEnabled;
    }

    public void setParallaxEnabled(boolean enabled) {
        isParallaxEnabled = enabled;
        calculateMapParallax();
    }

}