package io.github.jmecn.tiled;

import com.jme3.app.Application;
import com.jme3.app.FlyCamAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.input.InputManager;
import com.jme3.input.Joystick;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.math.*;
import com.jme3.post.SceneProcessor;
import com.jme3.profile.AppProfiler;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.texture.FrameBuffer;
import io.github.jmecn.tiled.core.GroupLayer;
import io.github.jmecn.tiled.core.Layer;
import io.github.jmecn.tiled.math2d.Point;
import io.github.jmecn.tiled.core.TiledMap;
import io.github.jmecn.tiled.enums.ZoomMode;
import io.github.jmecn.tiled.renderer.*;
import io.github.jmecn.tiled.renderer.factory.DefaultMaterialFactory;
import io.github.jmecn.tiled.renderer.factory.DefaultMeshFactory;
import io.github.jmecn.tiled.renderer.factory.DefaultSpriteFactory;
import io.github.jmecn.tiled.renderer.factory.MaterialFactory;
import io.github.jmecn.tiled.renderer.queue.YAxisComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TiledMapAppState manage a {@link MapRenderer} for {@link TiledMap} and render it.
 * 
 * @author yanmaoyuan
 */
public class TiledMapAppState extends BaseAppState implements AnalogListener, ActionListener, SceneProcessor {

    static Logger logger = LoggerFactory.getLogger(TiledMapAppState.class);
    public static final String LEFT = "left";
    public static final String RIGHT = "right";
    public static final String UP = "up";
    public static final String DOWN = "down";
    public static final String DRAG = "dragAndDrop";
    public static final String ZOOM_IN = "zoom_in";
    public static final String ZOOM_OUT = "zoom_out";
    public static final String GRID = "grid";
    public static final String PARALLAX = "parallax";

    private static final String[] MAPPINGS = new String[] { LEFT, RIGHT, UP, DOWN, DRAG, ZOOM_IN, ZOOM_OUT, GRID, PARALLAX };

    // Tiled Map
    private TiledMap map;
    private MapRenderer mapRenderer;

    // The rootNode
    private final Node rootNode;

    // The grid
    private boolean isGridVisible = false;
    private final Node gridVisual;// for render grid
    private Material gridMaterial;// for render grid
    private boolean isGridUpdated = true;

    private boolean isCursorVisible = true;
    private Point currentTile;
    private Spatial gridCursor;
    private Material cursorMaterial;
    private boolean isCursorUpdated = true;

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
     * Constructor
     */
    public TiledMapAppState() {
        this(12f);
    }

    public TiledMapAppState(float viewColumns) {
        screenDimension = new Vector2f();
        mapDimension = new Vector2f();

        mapTranslation = new Vector3f();
        mapScale = 1f;

        rootNode = new Node("Tiled Map Root");

        gridVisual = new Node("Tiled Map Grid");
        gridVisual.setLocalTranslation(0f, 999f, 0f);

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

        // sort by y-axis
        viewPort.getQueue().setGeometryComparator(RenderQueue.Bucket.Opaque, new YAxisComparator());
        viewPort.addProcessor(this);

        float near = -1000f;
        float far = 1000f;
        float halfWidth = screenDimension.x * 0.5f;
        float halfHeight = screenDimension.y * 0.5f;
        cam.setFrustum(near, far, -halfWidth, halfWidth, halfHeight, -halfHeight);

        cam.setParallelProjection(true);
        cam.setLocation(new Vector3f(halfWidth, 0, halfHeight));
        cam.lookAtDirection(new Vector3f(0f, -1f, 0f), new Vector3f(0f, 0f, -1f));
        logger.info("cam: {}, direction:{}", cam.getLocation(), cam.getDirection());

        gridMaterial = materialFactory.newMaterial(ColorRGBA.DarkGray);
        cursorMaterial = materialFactory.newMaterial(MaterialConst.CURSOR_AVAILABLE_COLOR);
    }

    @Override
    protected void cleanup(Application app) {
        rootNode.detachAllChildren();
    }

    @Override
    protected void onEnable() {
        ((SimpleApplication) getApplication()).getRootNode().attachChild(rootNode);

        // disable flyCamAppState
        FlyCamAppState flyCamAppState = getStateManager().getState(
                FlyCamAppState.class);
        if (flyCamAppState != null) {
            flyCamAppState.setEnabled(false);
        }

        registerWithInput();
    }

    @Override
    protected void onDisable() {
        rootNode.removeFromParent();

        // enable flyCamAppState
        FlyCamAppState flyCamAppState = getStateManager().getState(
                FlyCamAppState.class);
        if (flyCamAppState != null) {
            flyCamAppState.setEnabled(true);
        }

        unregisterInput();
    }

    @Override
    public void update(float tpf) {
        Spatial spatial;
        if (mapRenderer != null) {
            spatial = mapRenderer.render();

            if (isGridUpdated) {
                createGird();
            }

            if (isCursorUpdated) {
                createCursor();
            }

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

            moveCursor();
        }
    }

    private void createGird() {
        gridVisual.getChildren().clear();
        mapRenderer.renderGrid(gridVisual, gridMaterial);
        if (gridVisual.getParent() != null) {
            gridVisual.removeFromParent();
            mapRenderer.getRootNode().attachChild(gridVisual);
        }
        isGridUpdated = false;
    }

    private void createCursor() {
        // remove old cursor
        if (gridCursor != null) {
            gridCursor.removeFromParent();
        }

        currentTile = null;
        cursorMaterial.setColor(MaterialConst.COLOR, MaterialConst.CURSOR_AVAILABLE_COLOR);
        gridCursor = mapRenderer.createTileGrid(cursorMaterial);
        mapRenderer.getRootNode().attachChild(gridCursor);
        isCursorUpdated = false;
    }
    /**
     * Set map. It will instance a new MapRenderer and create visual parts for this map.
     * 
     * @param map the tiled map
     */
    public void setMap(TiledMap map) {
        if (!isInitialized()) {
            throw new IllegalStateException("TiledMapAppState is not initialized.");
        }
        if (map == null) {
            return;
        }

        rootNode.detachAllChildren();
        
        viewPort.setBackgroundColor(map.getBackgroundColor());

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

        if (gridMaterial != null) {
            createGird();
        } else {
            isGridUpdated = true;
        }

        if (cursorMaterial != null) {
            createCursor();
        } else {
            isCursorUpdated = true;
        }
    }

    public TiledMap getMap() {
        return map;
    }

    public MapRenderer getMapRenderer() {
        return mapRenderer;
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

    public void registerWithInput() {
        if (inputManager == null) {
            return;
        }

        // keyboard only WASD for movement
        inputManager.addMapping(LEFT, new KeyTrigger(KeyInput.KEY_A), new KeyTrigger(KeyInput.KEY_LEFT));
        inputManager.addMapping(RIGHT, new KeyTrigger(KeyInput.KEY_D), new KeyTrigger(KeyInput.KEY_RIGHT));
        inputManager.addMapping(UP, new KeyTrigger(KeyInput.KEY_W), new KeyTrigger(KeyInput.KEY_UP));
        inputManager.addMapping(DOWN, new KeyTrigger(KeyInput.KEY_S), new KeyTrigger(KeyInput.KEY_DOWN));

        inputManager.addMapping(ZOOM_IN, new MouseAxisTrigger(MouseInput.AXIS_WHEEL, false));
        inputManager.addMapping(ZOOM_OUT, new MouseAxisTrigger(MouseInput.AXIS_WHEEL, true));
        inputManager.addMapping(DRAG, new MouseButtonTrigger(MouseInput.BUTTON_LEFT));

        inputManager.addMapping(GRID, new KeyTrigger(KeyInput.KEY_G));// add key mapping to show/hide grid
        inputManager.addMapping(PARALLAX, new KeyTrigger(KeyInput.KEY_P));// add key mapping to enable/disable parallax

        inputManager.addListener(this, MAPPINGS);

        Joystick[] joysticks = inputManager.getJoysticks();
        if (joysticks != null) {
            for (Joystick joystick : joysticks) {
                // Make the left stick move
                joystick.getXAxis().assignAxis(RIGHT, LEFT);
                joystick.getYAxis().assignAxis(DOWN, UP);
            }
        }
    }

    public void unregisterInput() {

        if (inputManager == null) {
            return;
        }

        for (String s : MAPPINGS) {
            if (inputManager.hasMapping(s)) {
                inputManager.deleteMapping(s);
            }
        }

        inputManager.removeListener(this);
    }

    public Point getCameraTileCoordinate() {
        Vector2f center = getCameraScreenCoordinate();
        center.subtractLocal(mapTranslation.x, mapTranslation.z).divideLocal(mapScale);
        return getMapRenderer().screenToTileCoords(center.x, center.y);
    }

    public Vector2f getCameraPixelCoordinate() {
        Vector2f center = getCameraScreenCoordinate();
        center.subtractLocal(mapTranslation.x, mapTranslation.z).divideLocal(mapScale);
        return getMapRenderer().screenToPixelCoords(center.x, center.y);
    }

    public Vector2f getCameraScreenCoordinate() {
        return new Vector2f(cam.getLocation().x, cam.getLocation().z);
    }

    /**
     * Get the cursor tile coordinate in the map
     *
     * @return The tile coordinate of the cursor
     */
    public Point getCursorTileCoordinate(Vector2f cursor) {
        Vector2f pixel = getCursorPixelCoordinate(cursor);
        return getMapRenderer().screenToTileCoords(pixel.x, pixel.y);
    }

    /**
     * Get the cursor pixel coordinate in the map
     *
     * @return The pixel coordinate of the cursor
     */
    public Vector2f getCursorPixelCoordinate(Vector2f cursor) {
        Vector3f worldPos = cam.getWorldCoordinates(cursor, 0);
        Vector2f pixel = new Vector2f(worldPos.x, worldPos.z);
        return pixel.subtractLocal(mapTranslation.x, mapTranslation.z).divideLocal(mapScale);
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

    /**
     * drag camera
     * 
     */
    private void drag() {
        if (map == null || mapRenderer.getRootNode() == null) {
            return;
        }

        // record the mouse position
        stopPos.set(inputManager.getCursorPosition());
        stopPos.subtractLocal(startPos);

        // move camera
        mapTranslation.set(startLoc.add(stopPos.x, 0, -stopPos.y));
        moveMapVisual();
    }

    private void moveCursor() {
        if (gridCursor != null) {
            Vector2f input = inputManager.getCursorPosition();
            Point cursor = getCursorTileCoordinate(input);
            if (currentTile == null) {
                currentTile = cursor;
            } else if (!currentTile.equals(cursor)) {
                currentTile.set(cursor.getX(), cursor.getY());
            } else {
                return;
            }
            Vector2f loc = mapRenderer.tileToScreenCoords(cursor.getX(), cursor.getY());
            gridCursor.setLocalTranslation(loc.x, 1000f, loc.y);
            if (map.contains(cursor.getX(), cursor.getY())) {
                cursorMaterial.setColor(MaterialConst.COLOR, MaterialConst.CURSOR_AVAILABLE_COLOR);
            } else {
                cursorMaterial.setColor(MaterialConst.COLOR, MaterialConst.CURSOR_UNAVAILABLE_COLOR);
            }
        }
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

    public void onAnalog(String name, float value, float tpf) {
        if (mapRenderer == null) {
            return;
        }
        switch (name) {
            case UP:
                move(-tpf, false);
                break;
            case DOWN:
                move(tpf, false);
                break;
            case LEFT:
                move(tpf, true);
                break;
            case RIGHT:
                move(-tpf, true);
                break;
            case DRAG:
                drag();
                break;
            case ZOOM_IN:
                zoomCamera(value);
                break;
            case ZOOM_OUT:
                zoomCamera(-value);
                break;
            default:
                break;
        }
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if (DRAG.equals(name)) {
            if (isPressed) {
                // record the mouse position
                startPos.set(inputManager.getCursorPosition());
                startLoc.set(mapTranslation);
            } else {
                drag();
            }
        } else if (GRID.equals(name) && isPressed) {
            isGridVisible = !isGridVisible;
            toggleGrid();
        } else if (PARALLAX.equals(name) && isPressed) {
            isParallaxEnabled = !isParallaxEnabled;
            calculateMapParallax();
        }
    }

    public boolean isGridVisible() {
        return isGridVisible;
    }

    public void setGridVisible(boolean visible) {
        isGridVisible = visible;

        if (gridVisual != null) {
            if (isGridVisible) {
                if (map != null && mapRenderer.getRootNode() != null) {
                    mapRenderer.getRootNode().attachChild(gridVisual);
                }
            } else {
                gridVisual.removeFromParent();
            }
        }
    }

    public boolean isCursorVisible() {
        return isCursorVisible;
    }

    public void setCursorVisible(boolean visible) {
        isCursorVisible = visible;
        if (gridCursor != null) {
            if (isCursorVisible && map != null && mapRenderer.getRootNode() != null) {
                mapRenderer.getRootNode().attachChild(gridCursor);
            } else {
                gridCursor.removeFromParent();
            }
        }
    }

    public boolean isParallaxEnabled() {
        return isParallaxEnabled;
    }

    public void setParallaxEnabled(boolean enabled) {
        isParallaxEnabled = enabled;
        calculateMapParallax();
    }

    /**
     * show/hide the grid
     */
    private void toggleGrid() {
        if (isGridVisible) {
            if (map != null && mapRenderer.getRootNode() != null) {
                mapRenderer.getRootNode().attachChild(gridVisual);
            }
        } else {
            gridVisual.removeFromParent();
        }
    }

    @Override
    public void initialize(RenderManager rm, ViewPort vp) {
        logger.info("initialize: {}", vp.getName());
    }

    @Override
    public void reshape(ViewPort vp, int w, int h) {
        logger.info("reshape: {}, {}", w, h);
        screenDimension.set(w, h);
        cam.setLocation(new Vector3f(w * 0.5f, 0, h * 0.5f));
        setMapScale(mapScale);
        getMapScale();
    }

    @Override
    public void rescale(ViewPort vp, float x, float y) {
        logger.info("rescale: {}, {}", x, y);
    }

    @Override
    public void preFrame(float tpf) {
        // nothing
    }

    @Override
    public void postQueue(RenderQueue rq) {
        // nothing
    }

    @Override
    public void postFrame(FrameBuffer out) {
        // nothing
    }

    @Override
    public void setProfiler(AppProfiler profiler) {
        // nothing
    }
}
