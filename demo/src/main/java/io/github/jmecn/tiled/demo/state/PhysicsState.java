package io.github.jmecn.tiled.demo.state;

import com.jme3.math.Vector2f;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.joints.JointDef;

public class PhysicsState extends BaseAppState {

	protected World world;

	public PhysicsState() {
		world = new World(new Vec2(0,0), true);
	}

	@Override
	protected void initialize(Application app) {
	}

	@Override
	public void update(float tpf) {
		world.step(tpf, 8, 3);
	}

	@Override
	protected void cleanup(Application app) {
	}

	public void addBody(BodyDef bodyDef) {
		world.createBody(bodyDef);
	}

	public void addJoint(JointDef jointDef) {
		world.createJoint(jointDef);
	}

	@Override
	protected void onEnable() {
	}

	@Override
	protected void onDisable() {
	}

	public void setBounds(Vector2f mapDimensionF) {
	}

	public World getWorld() {
		return world;
	}

	public Body createBody(BodyDef bodyDef) {
		return world.createBody(bodyDef);
	}
}