package io.github.jmecn.tiled.demo.state;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import org.dyn4j.dynamics.Body;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public class PlayerState extends BaseAppState implements ActionListener {

    static Logger logger = LoggerFactory.getLogger(PlayerState.class);

    private static final String MOVE_LEFT     = "MOVE_LEFT";
    private static final String MOVE_RIGHT    = "MOVE_RIGHT";
    private static final String MOVE_FORWARD  = "MOVE_FORWARD";
    private static final String MOVE_BACKWARD = "MOVE_BACKWARD";

    private static final String[] MAPPINGS = new String[] { MOVE_LEFT, MOVE_RIGHT, MOVE_FORWARD, MOVE_BACKWARD, };

    private boolean left     = false;
    private boolean right    = false;
    private boolean forward  = false;
    private boolean backward = false;

    // playerInput
    private final Vector2f playerInput    = new Vector2f(0f, 0f);
    private final float    inputSensitive = 5f;

    // velocity
    private final Vector3f desiredVelocity = new Vector3f(0f, 0f, 0f);
    private final float    maxSpeed        = 16f * 5;
    private final float    maxAcceleration = 16f * 10;

    private final Vector3f displacement = new Vector3f();

    private Body body;

    private final Vector3f position = new Vector3f(0f, 0f, 0f);
    private final Vector3f velocity = new Vector3f(0f, 0f, 0f);

    private ViewState viewState;
    @Override
    protected void initialize(Application app) {
        InputManager inputManager = app.getInputManager();
        inputManager.addMapping("Left", new KeyTrigger(com.jme3.input.KeyInput.KEY_A));
        inputManager.addMapping("Right", new KeyTrigger(com.jme3.input.KeyInput.KEY_D));
        inputManager.addMapping("Up", new KeyTrigger(com.jme3.input.KeyInput.KEY_W));
        inputManager.addMapping("Down", new KeyTrigger(com.jme3.input.KeyInput.KEY_S));

        viewState = app.getStateManager().getState(ViewState.class);
        // input
        registerInput(inputManager);
    }

    public void registerInput(InputManager inputManager) {
        inputManager.addMapping(MOVE_LEFT, new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping(MOVE_RIGHT, new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping(MOVE_FORWARD, new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping(MOVE_BACKWARD, new KeyTrigger(KeyInput.KEY_S));

        inputManager.addListener(this, MAPPINGS);
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        switch (name) {
            case MOVE_LEFT:
                left = isPressed;
                break;
            case MOVE_RIGHT:
                right = isPressed;
                break;
            case MOVE_FORWARD:
                forward = isPressed;
                break;
            case MOVE_BACKWARD:
                backward = isPressed;
                break;
        }

    }

    @Override
    public void update(float tpf) {
        updateInput(tpf);
        body.setLinearVelocity(velocity.x, velocity.z);
        float x = (float) body.getTransform().getTranslationX();
        float y = (float) body.getTransform().getTranslationY();
        viewState.moveToPixel(x, y);
        // position.addLocal(displacement);
        // logger.info("position: {}", position);
        // viewState.moveToPixel(position.x, position.z);
    }

    public void updateInput(float tpf) {
        float step = tpf * inputSensitive;
        if (!left && !right) {
            playerInput.x = 0;
        } else {
            if (left) {
                playerInput.x -= step;
            }
            if (right) {
                playerInput.x += step;
            }
        }
        if (!forward && !backward) {
            playerInput.y = 0;
        } else {
            if (forward) {
                playerInput.y -= step;
            }
            if (backward) {
                playerInput.y += step;
            }
        }

        // ClampMagnitude
        float length = playerInput.length();
        if (length >= 1f) {
            playerInput.divideLocal(length);
        }

        // Velocity
        desiredVelocity.set(playerInput.x, 0f, playerInput.y);
        desiredVelocity.multLocal(maxSpeed);

        float maxSpeedChange = maxAcceleration * tpf;
        velocity.x = moveToward(velocity.x, desiredVelocity.x, maxSpeedChange);
        velocity.z = moveToward(velocity.z, desiredVelocity.z, maxSpeedChange);

        velocity.mult(tpf, displacement);
    }

    float moveToward(float start, float end, float step) {
        if (start < end) {
            start = Math.min(start + step, end);
        } else if (start > end) {
            start = Math.max(start - step, end);
        }

        return start;
    }

    @Override
    protected void cleanup(Application app) {

    }

    @Override
    protected void onEnable() {

    }

    @Override
    protected void onDisable() {

    }

    public void setPosition(float x, float y) {
        position.set(x, 0, y);
    }

    public void setBody(Body body) {
        this.body = body;
    }
}
