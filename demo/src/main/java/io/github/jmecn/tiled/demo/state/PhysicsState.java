package io.github.jmecn.tiled.demo.state;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.World;

public class PhysicsState extends BaseAppState {

	protected World world;

	public PhysicsState() {
		world = new World(new Vec2(0,0), true);
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

	public Body createBody(BodyDef bodyDef) {
		return world.createBody(bodyDef);
	}
}