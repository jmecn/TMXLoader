package io.github.jmecn.tiled.demo.state;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.callbacks.ContactListener;
import org.jbox2d.collision.Manifold;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.contacts.Contact;

import java.util.ArrayList;
import java.util.List;

public class PhysicsState extends BaseAppState implements ContactListener {

	protected World world;

	private List<ContactListener> listeners = new ArrayList<>();

	public PhysicsState() {
		world = new World(new Vec2(0,0), true);
		world.setContactListener(this);
	}

	@Override
	protected void initialize(Application app) {
		// do nothing
	}

	@Override
	public void update(float tpf) {
		world.step(tpf, 8, 3);
	}

	@Override
	protected void cleanup(Application app) {
		// do nothing
	}

	@Override
	protected void onEnable() {
		// do nothing
	}

	@Override
	protected void onDisable() {
		// do nothing
	}

	public Body createBody(BodyDef bodyDef, FixtureDef fixtureDef) {
		Body body = world.createBody(bodyDef);
		body.createFixture(fixtureDef);

		getStateManager().getState(PhysicsDebugState.class).addDebugShape(body, fixtureDef);
		return body;
	}

	public void addContactListener(ContactListener listener) {
		listeners.add(listener);
	}

	public void removeContactListener(ContactListener listener) {
		listeners.remove(listener);
	}

	@Override
	public void beginContact(Contact contact) {
		for (ContactListener listener : listeners) {
			listener.beginContact(contact);
		}
	}

	@Override
	public void endContact(Contact contact) {
		for (ContactListener listener : listeners) {
			listener.endContact(contact);
		}
	}

	@Override
	public void preSolve(Contact contact, Manifold manifold) {
		for (ContactListener listener : listeners) {
			listener.preSolve(contact, manifold);
		}
	}

	@Override
	public void postSolve(Contact contact, ContactImpulse contactImpulse) {
		for (ContactListener listener : listeners) {
			listener.postSolve(contact, contactImpulse);
		}
	}
}