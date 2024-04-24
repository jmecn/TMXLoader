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
import com.jme3.scene.Spatial;
import io.github.jmecn.tiled.animation.AnimatedTileControl;
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
    private final Vector2f velocity = new Vector2f(0f, 0f);
    private final float moveSpeed = 16f * 5;

    private Body body;
    private Spatial player;

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

        if (body != null) {
            body.setLinearVelocity(new Vec2(velocity.x, velocity.y));
            float x = body.getTransform().position.x;
            float y = body.getTransform().position.y;
            cam.setLocation(new Vector3f(x, 0, y));
        }

        if (player != null) {
            updatePlayerState();
        }
    }

    public static final int ANIM_IDLE = -1;
    public static final int ANIM_WALK_UP = 0;
    public static final int ANIM_WALK_LEFT = 1;
    public static final int ANIM_WALK_RIGHT = 2;
    public static final int ANIM_WALK_DOWN = 3;

    String currentAnim = "idle_down";
    private void updatePlayerState() {

        String anim;
        if (playerInput.lengthSquared() <= 0.01f) {
            if ("walk_left".equals(currentAnim)) {
                anim = "idle_left";
            } else if ("walk_right".equals(currentAnim)) {
                anim = "idle_right";
            } else if ("walk_up".equals(currentAnim)) {
                anim = "idle_up";
            } else if ("walk_down".equals(currentAnim)) {
                anim = "idle_down";
            } else {
                anim = currentAnim;// keep current animation
            }
        } else {
            if (Math.abs(playerInput.x) > Math.abs(playerInput.y)) {
                if (playerInput.x > 0) {
                    anim = "walk_right";
                } else {
                    anim = "walk_left";
                }
            } else {
                if (playerInput.y > 0) {
                    anim = "walk_down";
                } else {
                    anim = "walk_up";
                }
            }
        }
        if (anim.equals(currentAnim)) {
            return;
        }
        currentAnim = anim;
        setAnimation(anim);
    }

    private void setAnimation(String anim) {
        logger.info("Change animation to {}", anim);
        // nothing
        AnimatedTileControl control = player.getControl(AnimatedTileControl.class);
        if (control != null) {
            control.setAnim(anim);
        }
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
        velocity.set(playerInput.x, playerInput.y);
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

    public void setPlayer(Spatial player) {
        this.player = player;
        setAnimation("idle_down");
    }
}
