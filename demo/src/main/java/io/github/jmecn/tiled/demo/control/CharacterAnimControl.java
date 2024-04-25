package io.github.jmecn.tiled.demo.control;

import com.jme3.math.Vector2f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;
import io.github.jmecn.tiled.animation.AnimatedTileControl;

import static io.github.jmecn.tiled.demo.Const.*;

/**
 * Character animation state machine.
 *
 * @author yanmaoyuan
 */
public class CharacterAnimControl extends AbstractControl {

    private String currentAnim = ANIM_IDLE_DOWN;

    @Override
    protected void controlUpdate(float tpf) {
        Vector2f velocity = spatial.getUserData(VELOCITY);
        if (velocity == null) {
            return;
        }

        if (velocity.lengthSquared() <= 0.01f) {
            idle();
        } else {
            walk(velocity);
        }
    }

    private void idle() {
        String anim;
        switch (currentAnim) {
            case ANIM_WALK_LEFT:
                anim = ANIM_IDLE_LEFT;
                break;
            case ANIM_WALK_RIGHT:
                anim = ANIM_IDLE_RIGHT;
                break;
            case ANIM_WALK_UP:
                anim = ANIM_IDLE_UP;
                break;
            case ANIM_WALK_DOWN:
                anim = ANIM_IDLE_DOWN;
                break;
            default:
                anim = currentAnim;// keep current animation
                break;
        }
        setAnimation(anim);
    }

    private void walk(Vector2f velocity) {
        String anim;
        if (Math.abs(velocity.x) > Math.abs(velocity.y)) {
            if (velocity.x > 0) {
                anim = ANIM_IDLE_RIGHT;
            } else {
                anim = ANIM_WALK_LEFT;
            }
        } else {
            if (velocity.y > 0) {
                anim = ANIM_WALK_DOWN;
            } else {
                anim = ANIM_WALK_RIGHT;
            }
        }
        setAnimation(anim);
    }

    private void setAnimation(String anim) {
        if (anim.equals(currentAnim)) {
            return;
        }
        currentAnim = anim;
        // nothing
        AnimatedTileControl control = spatial.getControl(AnimatedTileControl.class);
        if (control != null) {
            control.setAnim(anim);
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        // nothing
    }
}