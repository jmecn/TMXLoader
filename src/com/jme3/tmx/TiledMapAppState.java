package com.jme3.tmx;

import java.util.logging.Logger;

import com.jme3.app.Application;
import com.jme3.app.FlyCamAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.input.InputManager;
import com.jme3.input.Joystick;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.tmx.core.ImageLayer;
import com.jme3.tmx.core.Layer;
import com.jme3.tmx.core.ObjectLayer;
import com.jme3.tmx.core.TileLayer;
import com.jme3.tmx.core.TiledMap;
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

	static Logger logger = Logger.getLogger(TiledMapAppState.class.getName());

	public static String LEFT = "left";
	public static String RIGHT = "right";
	public static String UP = "up";
	public static String DOWN = "down";
	public static String DRAG = "dragAndDrop";
	public static String ZOOMIN = "zoomin";
	public static String ZOOMOUT = "zoomout";

	private static String[] mappings = new String[] { LEFT, RIGHT, UP, DOWN,
			DRAG, ZOOMIN, ZOOMOUT };

	// Tiled Map
	private TiledMap map;
	private MapRenderer mapRenderer;

	// The rootNode
	private Node rootNode;
	private Quaternion localRotation;

	// The mapNode
	private Node mapNode;
	private Vector3f mapTranslation;
	private float mapScale;

	// The camera
	private Camera cam;
	private ViewPort viewPort;
	private InputManager inputManager;
	private Vector2f screenDimension;
	private Vector2f mapDimension;

	// The
	private float viewColumns;
	protected float moveSpeed = 10f;// in tiles
	protected float zoomSpeed = 1f;// in tiles
	/**
	 * update scene when the map updated
	 */
	private boolean isMapUpdated = true;

	/**
	 * Default constructor
	 * 
	 * @param map
	 */
	public TiledMapAppState() {
		this(null);
	}

	/**
	 * Constructor
	 * 
	 * @param map
	 */
	public TiledMapAppState(TiledMap map) {
		this(map, 12f);
	}

	public TiledMapAppState(TiledMap map, float viewColumns) {
		screenDimension = new Vector2f();
		mapDimension = new Vector2f();

		mapNode = new Node("TileMap");
		mapNode.setQueueBucket(Bucket.Gui);
		mapTranslation = new Vector3f();
		mapScale = 1f;

		rootNode = new Node("Tiled Map Root");
		rootNode.setQueueBucket(Bucket.Gui);

		// translate the scene from XOZ plane to XOY plane
		localRotation = new Quaternion();
		localRotation.fromAngles(FastMath.HALF_PI, 0, 0);
		rootNode.setLocalRotation(localRotation);

		rootNode.attachChild(mapNode);

		setMap(map);

		this.viewColumns = viewColumns;
	}

	@Override
	protected void initialize(Application app) {
		inputManager = app.getInputManager();
		viewPort = app.getViewPort();
		cam = app.getCamera();

		screenDimension.set(cam.getWidth(), cam.getHeight());

		// move the rootNode to top-left corner of the screen
		rootNode.setLocalTranslation(0, screenDimension.y, 0);

		float near = -1000f;
		float far = 1000f;
		float halfWidth = screenDimension.x * 0.5f;
		float halfHeight = screenDimension.y * 0.5f;
		cam.setFrustum(near, far, -halfWidth, halfWidth, halfHeight,
				-halfHeight);

		cam.setParallelProjection(true);
		cam.lookAtDirection(new Vector3f(0f, 0f, -1f), Vector3f.UNIT_Y);
		cam.setLocation(new Vector3f(halfWidth, halfHeight, 0));
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
		if (isMapUpdated) {
			render();
			isMapUpdated = false;
		}

		mapNode.setLocalTranslation(mapTranslation);
		mapNode.setLocalScale(mapScale, 1f, mapScale);
	}

	/**
	 * Set map. It will instance a new MapRenderer and create visual parts for
	 * this map.
	 * 
	 * @param map
	 */
	public void setMap(TiledMap map) {
		if (map == null)
			return;

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
			logger.warning("Unknown orientation:" + map.getOrientation()
					+ ". Use OrthogonalRender by default");
			mapRenderer = new OrthogonalRenderer(map);
		}

		// create visual part for the map;
		mapRenderer.updateVisual();
		isMapUpdated = true;
	}

	/**
	 * Render the tiled map
	 */
	public void render() {

		if (map == null) {
			return;
		}

		// set background color
		if (viewPort != null) {
			ColorRGBA bgColor = map.getBackgroundColor();
			if (bgColor == null) {
				bgColor = ColorRGBA.Black.clone();
			}
			viewPort.setBackgroundColor(bgColor);
		}

		mapNode.detachAllChildren();
		int len = map.getLayerCount();
		int layerCnt = 0;
		for (int i = 0; i < len; i++) {
			Layer layer = map.getLayer(i);

			// skip invisible layer
			if (!layer.isVisible()) {
				continue;
			}

			Spatial visual = null;
			if (layer instanceof TileLayer) {
				visual = mapRenderer.render((TileLayer) layer);
			}

			if (layer instanceof ObjectLayer) {
				visual = mapRenderer.render((ObjectLayer) layer);
			}

			if (layer instanceof ImageLayer) {
				visual = mapRenderer.render((ImageLayer) layer);
			}

			if (visual != null) {
				visual.setQueueBucket(Bucket.Gui);
				mapNode.attachChild(visual);

				// this is a little magic to make let top layer block off the
				// bottom layer
				visual.setLocalTranslation(0, layerCnt++, 0);
			}
		}

		// make the whole map thinner
		if (layerCnt > 0) {
			mapNode.setLocalScale(1, 1f / layerCnt, 1);
		}

		// move it to the left bottom of screen space
		mapDimension.set(mapRenderer.getMapDimension());
		mapTranslation.set(screenDimension.x * 0.5f, 0,
				screenDimension.y * 0.5f);
		mapScale = getMapScale();
	}

	public TiledMap getMap() {
		return map;
	}

	public MapRenderer getMapRenderer() {
		return mapRenderer;
	}

	public Vector3f getLocation(float x, float y) {
		Vector2f pos = mapRenderer.tileToScreenCoords(x, y);
		return new Vector3f(pos.x, pos.y, 999 - map.getLayerCount());
	}

	public void moveToTile(float x, float y) {
		mapRenderer.tileToScreenCoords(x, y);
	}

	private float getMapScale() {
		if (map != null) {
			float pixel = map.getTileWidth() * viewColumns;
			mapScale = screenDimension.x / pixel;
		}

		return mapScale;
	}

	/**
	 * Set view columns. It changes the number of tiles you can see in a row.
	 * 
	 * @param columns
	 */
	public void setViewColumn(float columns) {
		this.viewColumns = columns;
		this.mapScale = getMapScale();
	}

	/**
	 * Sets the move speed. The speed is given in world units per second.
	 * 
	 * @param moveSpeed
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

		inputManager.addListener(this, mappings);

		Joystick[] joysticks = inputManager.getJoysticks();
		if (joysticks != null && joysticks.length > 0) {
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

		for (String s : mappings) {
			if (inputManager.hasMapping(s)) {
				inputManager.deleteMapping(s);
			}
		}

		inputManager.removeListener(this);
	}

	/**
	 * move camera
	 * 
	 * @param value
	 * @param sideways
	 */
	public void move(float value, boolean sideways) {
		Vector3f vel = new Vector3f();
		Vector3f pos = mapTranslation.clone();

		if (sideways) {
			vel.set(1f, 0f, 0f);
		} else {
			vel.set(0f, 0f, -1f);
		}
		vel.multLocal(value * moveSpeed * map.getTileWidth() * mapScale);

		pos.addLocal(vel);

		mapTranslation.set(pos);
	}

	boolean isPressed = false;
	private Vector3f startLoc = new Vector3f();
	private Vector2f startPos = new Vector2f();
	private Vector2f stopPos = new Vector2f();

	/**
	 * drag camera
	 * 
	 * @param isPressed
	 */
	private void drag(boolean isPressed) {

		if (isPressed) {
			// recored the mouse position
			stopPos.set(inputManager.getCursorPosition());
			stopPos.subtractLocal(startPos);

			// move camera
			mapTranslation.set(startLoc.add(stopPos.x, 0, -stopPos.y));
		}
	}

	/**
	 * zoom camera
	 * 
	 * @param value
	 */
	public void zoomCamera(float value) {
		viewColumns += value;

		// at less see 1 tile on screen
		if (viewColumns < 1f) {
			viewColumns = 1f;
		}

		setViewColumn(viewColumns);
	}

	public void onAnalog(String name, float value, float tpf) {
		if (name.equals(UP)) {
			move(-tpf, false);
		} else if (name.equals(DOWN)) {
			move(tpf, false);
		} else if (name.equals(LEFT)) {
			move(tpf, true);
		} else if (name.equals(RIGHT)) {
			move(-tpf, true);
		} else if (name.equals(DRAG)) {
			drag(true);
		} else if (name.equals(ZOOMIN)) {
			zoomCamera(value);
		} else if (name.equals(ZOOMOUT)) {
			zoomCamera(-value);
		}
	}

	@Override
	public void onAction(String name, boolean isPressed, float tpf) {
		if (name.equals(DRAG)) {
			this.isPressed = isPressed;
			if (isPressed) {
				// recored the mouse position
				startPos.set(inputManager.getCursorPosition());
				startLoc.set(mapTranslation);
			} else {
				drag(true);
			}
		}

	}
}
