package com.jme3.tmx;

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
import com.jme3.renderer.Camera;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.tmx.core.TiledMap;
import com.jme3.tmx.enums.ZoomMode;
import com.jme3.tmx.math2d.Point;
import com.jme3.tmx.render.HexagonalRenderer;
import com.jme3.tmx.render.IsometricRenderer;
import com.jme3.tmx.render.MapRenderer;
import com.jme3.tmx.render.OrthogonalRenderer;
import com.jme3.tmx.render.StaggeredRenderer;

/**
 * TiledMapAppState will create a Spatial for com.jme3.tmx.core.TiledMap
 * 
 * @author yanmaoyuan
 * 
 */
public class TiledMapAppState extends BaseAppState implements AnalogListener,
        ActionListener {

    public static final String INIT_ERROR = "inputManager is null. Please initialize TiledMapAppState first.";
    public static final String LEFT = "left";
    public static final String RIGHT = "right";
    public static final String UP = "up";
    public static final String DOWN = "down";
    public static final String DRAG = "dragAndDrop";
    public static final String ZOOMIN = "zoomin";
    public static final String ZOOMOUT = "zoomout";
    public static final String GRID = "grid";

    private static final String[] MAPPINGS = new String[] { LEFT, RIGHT, UP, DOWN, DRAG, ZOOMIN, ZOOMOUT, GRID };

    // Tiled Map
    private TiledMap map;
    private MapRenderer mapRenderer;

    // The rootNode
    private final Node rootNode;
    private final Quaternion mapRotation;

    // The grid
    private final Node gridVisual;// for render grid
    private Material gridMaterial;// for render grid
    private boolean isGridUpdated = true;

    // The mapNode
    private final Vector3f mapTranslation;
    private float mapScale;

    // The camera
    private Camera cam;
    private ViewPort viewPort;
    private InputManager inputManager;
    private AssetManager assetManager;
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
    public TiledMapAppState() {
        this(null);
    }

    /**
     * Constructor
     * 
     * @param map the tiled map
     */
    public TiledMapAppState(TiledMap map) {
        this(map, 12f);
    }

    public TiledMapAppState(TiledMap map, float viewColumns) {
        screenDimension = new Vector2f();
        mapDimension = new Vector2f();

        mapTranslation = new Vector3f();
        mapScale = 1f;

        rootNode = new Node("Tiled Map Root");
        rootNode.setQueueBucket(Bucket.Gui);

        gridVisual = new Node("Tiled Map Grid");
        gridVisual.setQueueBucket(Bucket.Gui);
        gridVisual.setLocalTranslation(0f, 999f, 0f);

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
        assetManager = app.getAssetManager();
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
        cam.lookAtDirection(new Vector3f(0f, 0f, -1f), Vector3f.UNIT_Y);
        cam.setLocation(new Vector3f(halfWidth, halfHeight, 0));

        gridMaterial = createGridMaterial();
        if (this.map != null) {
            viewPort.setBackgroundColor(map.getBackgroundColor());
        }
    }

    @Override
    protected void cleanup(Application app) {
        rootNode.detachAllChildren();
    }

    @Override
    protected void onEnable() {
        ((SimpleApplication) getApplication()).getRootNode().attachChild(
                rootNode);

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
                gridVisual.getChildren().clear();
                mapRenderer.renderGrid(gridVisual, gridMaterial);
                isGridUpdated = false;
            }

            if (isMapUpdated) {
                // move it to the left bottom of screen space
                mapDimension.set(mapRenderer.getMapDimension());
                mapTranslation.set(screenDimension.x * 0.5f, 0, screenDimension.y * 0.5f);
                mapScale = getMapScale();
                spatial.setLocalTranslation(mapTranslation);
                spatial.setLocalScale(mapScale, 1f, mapScale);
                
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
        if (map == null)
            return;

        rootNode.detachAllChildren();
        rootNode.attachChild(map.getVisual());
        
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

        Vector2f loc = mapRenderer.tileToScreenCoords(0, 0);
        mapTranslation.set(loc.x, 0, loc.y);
        isMapUpdated = true;

        gridVisual.getChildren().clear();
        mapRenderer.renderGrid(gridVisual, gridMaterial);
        if (gridVisual.getParent() != null) {
            gridVisual.removeFromParent();
            map.getVisual().attachChild(gridVisual);
        }
        isGridUpdated = false;
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
        Vector2f camPos = new Vector2f(cam.getLocation().x, screenDimension.y - cam.getLocation().y);
        camPos.subtract(tilePos, tilePos);
        mapTranslation.set(tilePos.x, 0, tilePos.y);
        moveMapVisual();
    }

    public void moveToPixel(float x, float y) {
        Vector2f pixelPos = mapRenderer.pixelToScreenCoords(x, y).multLocal(getMapScale());
        Vector2f camPos = new Vector2f(cam.getLocation().x, screenDimension.y - cam.getLocation().y);
        camPos.subtract(pixelPos, pixelPos);
        mapTranslation.set(pixelPos.x, 0, pixelPos.y);
        moveMapVisual();
    }

    private void moveMapVisual() {
        map.getVisual().setLocalTranslation((float) Math.floor(mapTranslation.x), (float) Math.floor(mapTranslation.y), (float)Math.floor(mapTranslation.z));
    }

    public float getMapScale() {
        if (map != null) {
            float pixel = map.getTileWidth() * viewColumns;
            mapScale = screenDimension.x / pixel;
            map.getVisual().setLocalScale(mapScale, 1, mapScale);
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

    public void registerWithInput() {
        if (inputManager == null) {
            return;
        }

        // keyboard only WASD for movement
        inputManager.addMapping(LEFT, new KeyTrigger(KeyInput.KEY_A),
                new KeyTrigger(KeyInput.KEY_LEFT));
        inputManager.addMapping(RIGHT, new KeyTrigger(KeyInput.KEY_D),
                new KeyTrigger(KeyInput.KEY_RIGHT));
        inputManager.addMapping(UP, new KeyTrigger(KeyInput.KEY_W),
                new KeyTrigger(KeyInput.KEY_UP));
        inputManager.addMapping(DOWN, new KeyTrigger(KeyInput.KEY_S),
                new KeyTrigger(KeyInput.KEY_DOWN));

        inputManager.addMapping(ZOOMIN, new MouseAxisTrigger(
                MouseInput.AXIS_WHEEL, false));
        inputManager.addMapping(ZOOMOUT, new MouseAxisTrigger(
                MouseInput.AXIS_WHEEL, true));
        inputManager.addMapping(DRAG, new MouseButtonTrigger(
                MouseInput.BUTTON_LEFT));

        // add key mapping to show/hide grid
        inputManager.addMapping(GRID, new KeyTrigger(KeyInput.KEY_G));

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
        return new Vector2f(cam.getLocation().x, cam.getLocation().y);
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
        pos.set(mapTranslation.clone());

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
        if (map == null || map.getVisual() == null) {
            return;
        }

        // record the mouse position
        stopPos.set(inputManager.getCursorPosition());
        stopPos.subtractLocal(startPos);

        // move camera
        mapTranslation.set(startLoc.add(stopPos.x, 0, -stopPos.y));
        map.getVisual().setLocalTranslation(mapTranslation);
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
            case ZOOMIN:
                zoomCamera(value);
                break;
            case ZOOMOUT:
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
            if (gridVisual.getParent() == null) {
                if (map != null && map.getVisual() != null) {
                    map.getVisual().attachChild(gridVisual);
                }
            } else {
                gridVisual.removeFromParent();
            }
        }
    }

    /**
     * for display the map grid
     * @return
     */
    private Material createGridMaterial() {
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Gray);
        mat.getAdditionalRenderState().setWireframe(true);
        mat.getAdditionalRenderState().setDepthTest(false);
        return mat;
    }
}
