package com.jme3.tmx;

import com.jme3.app.Application;
import com.jme3.app.FlyCamAppState;
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
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;

/**
 * This RPG Camera AppState is for debug use. It will disable the
 * FlyCamAppState. Move camera with wsad keys or up/down/left/right keys.
 * 
 * @author yanmaoyuan
 * 
 */
public class RPGCamAppState extends BaseAppState implements AnalogListener,
		ActionListener {

	public static String LEFT = "left";
	public static String RIGHT = "right";
	public static String UP = "up";
	public static String DOWN = "down";
	public static String DRAG = "dragAndDrop";
	public static String ZOOMIN = "zoomin";
	public static String ZOOMOUT = "zoomout";

	private static String[] mappings = new String[] { LEFT, RIGHT, UP, DOWN,
			DRAG, ZOOMIN, ZOOMOUT };

	/**
	 * record the orgin size of the camera
	 */
	private float width;
	private Vector3f dir = new Vector3f(0f, 0f, -1f);
	private Vector3f up = new Vector3f(0f, 1f, 0f);
	private boolean isParallelProjection;

	/**
	 * tile width in pixel
	 */
	private int tileWidth = 32;// default
	private float viewColumns;
	private Camera cam;
	private InputManager inputManager;
	protected float moveSpeed = 10f;// in tiles
	protected float zoomSpeed = 1f;// in tiles

	public RPGCamAppState() {
		this.viewColumns = 8f;
		this.moveSpeed = 10f;
	}

	public RPGCamAppState(float viewColumns) {
		this.viewColumns = viewColumns;
		this.moveSpeed = 10f;
	}

	@Override
	protected void initialize(Application app) {
		this.cam = app.getCamera();
		this.inputManager = app.getInputManager();

		// record the orgin setting of the camera
		width = cam.getWidth();
		cam.getDirection(dir);
		cam.getUp(up);
		isParallelProjection = cam.isParallelProjection();

		cam.setParallelProjection(true);
		cam.lookAtDirection(new Vector3f(0, -1, 0), new Vector3f(0, 0, -1));
		setViewColumn(viewColumns);
	}

	@Override
	protected void cleanup(Application app) {
		// recover camera
		cam.setParallelProjection(isParallelProjection);
		cam.lookAtDirection(dir, up);
		setViewColumn(width);
	}

	@Override
	protected void onEnable() {
		// disable flyCamAppState
		FlyCamAppState flyCamAppState = getStateManager().getState(
				FlyCamAppState.class);
		if (flyCamAppState != null) {
			flyCamAppState.setEnabled(false);
		}

		registerWithInput(inputManager);
	}

	@Override
	protected void onDisable() {
		// enable flyCamAppState
		FlyCamAppState flyCamAppState = getStateManager().getState(
				FlyCamAppState.class);
		if (flyCamAppState != null) {
			flyCamAppState.setEnabled(true);
		}

		unregisterInput();
	}

	public void setTileWidth(int tileWidth) {
		this.tileWidth = tileWidth;

		setViewColumn(viewColumns);
	}

	/**
	 * Set view columns. It changes the number of tiles you can see in a row.
	 * 
	 * @param columns
	 */
	public void setViewColumn(float columns) {

		this.viewColumns = columns;

		if (cam == null) {
			return;
		}

		float frustumSize = viewColumns * tileWidth * 0.5f;
		float aspect = (float) cam.getHeight() / cam.getWidth();
		cam.setFrustum(-1000, 1000, -frustumSize, frustumSize, aspect
				* frustumSize, -aspect * frustumSize);
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

	public void registerWithInput(InputManager inputManager) {
		this.inputManager = inputManager;

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
	protected void moveCamera(float value, boolean sideways) {
		Vector3f vel = new Vector3f();
		Vector3f pos = cam.getLocation().clone();

		if (sideways) {
			vel.set(1f, 0f, 0f);
		} else {
			vel.set(0f, 0f, -1f);
		}
		vel.multLocal(value * moveSpeed * tileWidth);

		pos.addLocal(vel);

		cam.setLocation(pos);
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
	private void dragCamera(boolean isPressed) {

		if (isPressed) {
			// recored the mouse position
			stopPos.set(inputManager.getCursorPosition());
			stopPos.subtractLocal(startPos);
			stopPos.multLocal(tileWidth * viewColumns / width);

			// move camera
			Vector3f loc = new Vector3f(startLoc);
			loc.addLocal(-stopPos.x, 0, stopPos.y);
			cam.setLocation(loc);
		}
	}

	/**
	 * zoom camera
	 * 
	 * @param value
	 */
	protected void zoomCamera(float value) {
		viewColumns += value;

		// at less see 1 tile on screen
		if (viewColumns < 1f) {
			viewColumns = 1f;
		}

		setViewColumn(viewColumns);
	}

	public void onAnalog(String name, float value, float tpf) {
		if (name.equals(UP)) {
			moveCamera(tpf, false);
		} else if (name.equals(DOWN)) {
			moveCamera(-tpf, false);
		} else if (name.equals(LEFT)) {
			moveCamera(-tpf, true);
		} else if (name.equals(RIGHT)) {
			moveCamera(tpf, true);
		} else if (name.equals(DRAG)) {
			dragCamera(true);
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
				startLoc.set(cam.getLocation());
			} else {
				dragCamera(true);
			}
		}

	}
}
