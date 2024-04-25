package io.github.jmecn.tiled.demo.control;

import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;
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

    private int count;// in case of multiple contacts
    private final Body body;
    private final String behavior;

    public SensorControl(Body body, String behavior) {
        this.body = body;
        this.behavior = behavior;
        this.count = 0;
    }

    @Override
    protected void controlUpdate(float tpf) {
        if (count > 0) {
            logger.info("object enter:{}", behavior);
        } else {
            logger.info("object leave:{}", behavior);
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
