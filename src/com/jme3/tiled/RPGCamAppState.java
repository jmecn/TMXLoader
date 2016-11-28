package com.jme3.tiled;

import com.jme3.app.Application;
import com.jme3.app.FlyCamAppState;
import com.jme3.app.state.BaseAppState;
import com.jme3.input.InputManager;
import com.jme3.input.Joystick;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;

/**
 * This RPG Camera AppState is for debug use.
 * Move camera with wsad keys or up/down/left/right keys.
 * @author yanmaoyuan
 *
 */
public class RPGCamAppState extends BaseAppState implements AnalogListener {

	public static String LEFT = "left";
	public static String RIGHT = "right";
	public static String UP = "up";
	public static String DOWN = "down";

	private static String[] mappings = new String[] { LEFT, RIGHT, UP, DOWN, };

	private float viewColumns = 8f;
	private Camera cam;
	private InputManager inputManager;
	protected float moveSpeed = 10f;

	@Override
	protected void initialize(Application app) {
		this.cam = app.getCamera();
		this.cam.lookAtDirection(Vector3f.UNIT_Z.negate(), Vector3f.UNIT_Y);
		this.inputManager = app.getInputManager();
		this.setParallelCamera(viewColumns);
	}

	@Override
	protected void cleanup(Application app) {
	}

	@Override
	protected void onEnable() {
		// disable flyCamAppState
		FlyCamAppState flyCamAppState = getStateManager().getState(FlyCamAppState.class);
		if (flyCamAppState != null) {
			flyCamAppState.setEnabled(false);
		}
		
		registerWithInput(inputManager);
	}

	@Override
	protected void onDisable() {
		// enable flyCamAppState
		FlyCamAppState flyCamAppState = getStateManager().getState(FlyCamAppState.class);
		if (flyCamAppState != null) {
			flyCamAppState.setEnabled(true);
		}
		
		unregisterInput();
	}
	
	/**
	 * 
	 * @param columns
	 *            How many tiles you want to see in a row?
	 */
	public void setParallelCamera(float columns) {
		
		this.viewColumns = columns;
		
		if (cam == null) {
			return;
		}
		
		float frustumSize = viewColumns * 0.5f;
		cam.setParallelProjection(true);
		float aspect = (float) cam.getHeight() / cam.getWidth();
		cam.setFrustum(-1000, 1000, - frustumSize, frustumSize, aspect * frustumSize, - aspect * frustumSize);
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

	protected void moveCamera(float value, boolean sideways) {
		Vector3f vel = new Vector3f();
		Vector3f pos = cam.getLocation().clone();

		if (sideways) {
			vel.set(1f, 0f, 0f);
		} else {
			vel.set(0f, -1f, 0f);
		}
		vel.multLocal(value * moveSpeed);

		pos.addLocal(vel);

		cam.setLocation(pos);
	}

	public void onAnalog(String name, float value, float tpf) {
		if (name.equals(UP)) {
			moveCamera(-tpf, false);
		} else if (name.equals(DOWN)) {
			moveCamera(tpf, false);
		} else if (name.equals(LEFT)) {
			moveCamera(-tpf, true);
		} else if (name.equals(RIGHT)) {
			moveCamera(tpf, true);
		}
	}
}
