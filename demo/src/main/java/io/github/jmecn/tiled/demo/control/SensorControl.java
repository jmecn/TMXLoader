package io.github.jmecn.tiled.demo.control;

import com.jme3.math.FastMath;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import io.github.jmecn.tiled.renderer.MaterialConst;
import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.callbacks.ContactListener;
import org.jbox2d.collision.Manifold;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.contacts.Contact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public class SensorControl extends AbstractControl implements ContactListener {
    Logger logger = LoggerFactory.getLogger(SensorControl.class);

    public static final String BEHAVIOR_HIDE = "hide";

    private int count;// in case of multiple contacts
    private final Body body;
    private final String behavior;
    private float opacity;

    public SensorControl(Body body, String behavior) {
        this.body = body;
        this.count = 0;
        if (behavior.startsWith(BEHAVIOR_HIDE)) {
            this.behavior = BEHAVIOR_HIDE;
            this.opacity = 0.5f;// default
            // "hide:0.5"
            String[] parts = behavior.split(":");
            if (parts.length == 2) {
                try {
                    this.opacity = FastMath.clamp(Float.parseFloat(parts[1]), 0.0f, 1.0f);
                } catch (NumberFormatException e) {
                    logger.error("Invalid behavior:{}", behavior);
                }
            }
        } else {
            this.opacity = 1.0f;
            this.behavior = behavior;
        }
    }

    @Override
    protected void controlUpdate(float tpf) {
        if (count > 0) {
            if (BEHAVIOR_HIDE.equals(behavior)) {
                setOpacity(opacity);
            }
        } else {
            if (BEHAVIOR_HIDE.equals(behavior)) {
                setOpacity(1.0f);
            }
        }
    }

    public void setOpacity(float opacity) {
        if (spatial instanceof Geometry) {
            Geometry geometry = (Geometry) spatial;
            geometry.getMaterial().setFloat(MaterialConst.OPACITY, opacity);
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        // nothing
    }

    @Override
    public void beginContact(Contact contact) {
        if (contact.getFixtureA().getBody() == body || contact.getFixtureB().getBody() == body) {
            count++;
            logger.info("object {}:ON, count:{}", behavior, count);
        }
    }

    @Override
    public void endContact(Contact contact) {
        if (contact.getFixtureA().getBody() == body || contact.getFixtureB().getBody() == body) {
            count--;
            logger.info("object {}:OFF, count:{}", behavior, count);
        }
    }

    @Override
    public void preSolve(Contact contact, Manifold manifold) {
        // nothing
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse contactImpulse) {
        // nothing
    }
}
