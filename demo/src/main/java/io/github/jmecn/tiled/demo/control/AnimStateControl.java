package io.github.jmecn.tiled.demo.control;

import com.jme3.math.Vector2f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;
import io.github.jmecn.tiled.animation.AnimatedTileControl;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public class AnimStateControl extends AbstractControl {

    private String currentAnim = "idle_down";

    @Override
    protected void controlUpdate(float tpf) {
        Vector2f velocity = spatial.getUserData("velocity");
        if (velocity == null) {
            return;
        }

        String anim;
        if (velocity.lengthSquared() <= 0.01f) {
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
            if (Math.abs(velocity.x) > Math.abs(velocity.y)) {
                if (velocity.x > 0) {
                    anim = "walk_right";
                } else {
                    anim = "walk_left";
                }
            } else {
                if (velocity.y > 0) {
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
        // nothing
        AnimatedTileControl control = spatial.getControl(AnimatedTileControl.class);
        if (control != null) {
            control.setAnim(anim);
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {

    }
}
