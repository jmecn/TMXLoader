package io.github.jmecn.tiled.demo;

import com.jme3.app.DetailedProfilerState;
import com.jme3.app.SimpleApplication;
import com.jme3.app.StatsAppState;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import io.github.jmecn.tiled.TmxLoader;
import io.github.jmecn.tiled.core.MapObject;
import io.github.jmecn.tiled.core.ObjectGroup;
import io.github.jmecn.tiled.core.Tile;
import io.github.jmecn.tiled.core.TiledMap;
import io.github.jmecn.tiled.demo.control.BodyControl;
import io.github.jmecn.tiled.demo.state.PhysicsState;
import io.github.jmecn.tiled.demo.state.PlayerState;
import io.github.jmecn.tiled.demo.state.ViewState;
import io.github.jmecn.tiled.render.MapRenderer;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.dynamics.*;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public class Demo extends SimpleApplication {

    Demo() {
        super(new DetailedProfilerState(), new StatsAppState());
    }

    @Override
    public void simpleInitApp() {
        assetManager.registerLoader(TmxLoader.class, "tmx", "tsx");
        TiledMap tiledMap = (TiledMap) assetManager.loadAsset("Maps/forest.tmx");

        ViewState viewState = new ViewState(tiledMap, 12f);
        viewState.initialize(stateManager, this);
        stateManager.attach(viewState);

        PhysicsState physicsState = new PhysicsState();
        physicsState.initialize(stateManager, this);
        stateManager.attach(physicsState);

        PlayerState playerState = new PlayerState();
        playerState.initialize(stateManager, this);
        stateManager.attach(playerState);

        MapRenderer mapRenderer = viewState.getMapRenderer();

        physicsState.setBounds(mapRenderer.getMapDimensionF());

        ObjectGroup objectGroup = (ObjectGroup) tiledMap.getLayer("Collision");
        for (MapObject obj : objectGroup.getObjects()) {
            createBody(physicsState, obj);
        }

        ObjectGroup locations = (ObjectGroup) tiledMap.getLayer("Location");
        for (MapObject obj : locations.getObjects()) {
            if ("Start".equals(obj.getName())) {
                viewState.moveToPixel((float) obj.getX(), (float) obj.getY());

                Body body = createPlayBody(physicsState, obj.getX(), obj.getY());

                int index = tiledMap.getLayer("Trees").getIndex();

                Node node = new Node("player");
                node.move((float) obj.getX(), index, (float) obj.getY());
                node.setQueueBucket(RenderQueue.Bucket.Gui);
                node.addControl(new BodyControl(body));

                Tile tile = tiledMap.getTileForTileGID(115);// 115 is a flower
                Geometry player = mapRenderer.getSpriteFactory().newTileSprite(tile);
                player.move(-8, 0, -8);// center the player
                player.setQueueBucket(RenderQueue.Bucket.Gui);
                node.attachChild(player);

                mapRenderer.getRootNode().attachChild(node);

                playerState.setPosition((float) obj.getX(), (float) obj.getY());
                playerState.setBody(body);
                break;
            }
        }
    }

    private Body createBody(PhysicsState physicsState, MapObject obj) {
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.density = 1.0f;
        fixtureDef.friction = 0.0f;
        fixtureDef.restitution = 0.0f;

        PolygonShape shape = new PolygonShape();
        shape.setAsBox((float) obj.getWidth() / 2, (float) obj.getHeight() / 2);
        fixtureDef.shape = shape;

        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set((float) (obj.getX() + obj.getWidth() * 0.5f), (float) (obj.getY() + obj.getHeight() * 0.5f));
        bodyDef.type = BodyType.STATIC;

        Body body = physicsState.createBody(bodyDef);
        body.createFixture(fixtureDef);

        return body;
    }

    private Body createPlayBody(PhysicsState physicsState, double x, double y) {
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.density = 1.0f;
        fixtureDef.friction = 0.0f;
        fixtureDef.restitution = 0.0f;

        CircleShape shape = new CircleShape();
        shape.m_radius = 8;
        fixtureDef.shape = shape;

        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set((float) x, (float) y);
        bodyDef.type = BodyType.DYNAMIC;

        Body body = physicsState.createBody(bodyDef);
        body.createFixture(fixtureDef);

        return body;
    }
}
