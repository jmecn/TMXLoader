package io.github.jmecn.tiled.demo.state;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
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
    private final Vector3f velocity = new Vector3f(0f, 0f, 0f);
    private final float moveSpeed = 16f * 5;

    private Body body;

    private Camera cam;
    @Override
    protected void initialize(Application app) {
        cam = app.getCamera();
        registerInput(app.getInputManager());
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
        body.setLinearVelocity(new Vec2(velocity.x, velocity.z));
        float x = body.getTransform().position.x;
        float y = body.getTransform().position.y;
        cam.setLocation(new Vector3f(x, 0, y));
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
        velocity.set(playerInput.x, 0f, playerInput.y);
        velocity.multLocal(moveSpeed);
    }

    @Override
    protected void cleanup(Application app) {
        // nothing
    }

    @Override
    protected void onEnable() {
        // nothing
    }

    @Override
    protected void onDisable() {
        // nothing
    }

    public void setBody(Body body) {
        this.body = body;
    }
}
