package io.github.jmecn.tiled.demo.state;

import com.jme3.math.Vector2f;
import org.dyn4j.collision.AxisAlignedBounds;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.joint.Joint;
import org.dyn4j.world.World;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;

public class PhysicsState extends BaseAppState {

	protected World<Body> world;

	public PhysicsState() {
		world = new World<>();
	}

	@Override
	protected void initialize(Application app) {
		AxisAlignedBounds aabb = new AxisAlignedBounds(9999, 9999);
		world.setBounds(aabb);
	}

	@Override
	public void update(float tpf) {
		world.update(tpf);
	}

	@Override
	protected void cleanup(Application app) {
		world.removeAllListeners();
		world.removeAllBodiesAndJoints();
	}

	public void addBody(Body body) {
		world.addBody(body);
	}

	public void addJoint(Joint joint) {
		world.addJoint(joint);
	}

	@Override
	protected void onEnable() {
	}

	@Override
	protected void onDisable() {
	}

	public void setBounds(Vector2f mapDimensionF) {
		world.setBounds(new AxisAlignedBounds(mapDimensionF.x, mapDimensionF.y));
	}
}