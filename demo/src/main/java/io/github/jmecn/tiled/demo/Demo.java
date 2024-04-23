package io.github.jmecn.tiled.demo;

import com.jme3.app.DetailedProfilerState;
import com.jme3.app.SimpleApplication;
import com.jme3.app.StatsAppState;
import com.jme3.scene.Geometry;
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
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Rectangle;

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
            Body body = createBody(obj);
            physicsState.addBody(body);
        }

        ObjectGroup locations = (ObjectGroup) tiledMap.getLayer("Location");
        for (MapObject obj : locations.getObjects()) {
            if ("Start".equals(obj.getName())) {
                viewState.moveToPixel((float) obj.getX(), (float) obj.getY());

                Body body = createPlayBody();
                body.translate(obj.getX(), obj.getY());

                Tile tile = tiledMap.getTileForTileGID(115);
                Geometry player = mapRenderer.getSpriteFactory().newTileSprite(tile);
                player.move((float) obj.getX(), 2, (float) obj.getY());
                player.addControl(new BodyControl(body));

                mapRenderer.getRootNode().attachChild(player);

                playerState.setPosition((float) obj.getX(), (float) obj.getY());
                playerState.setBody(body);
                physicsState.addBody(body);
                break;
            }
        }
    }

    private Body createBody(MapObject obj) {
        Rectangle rect = new Rectangle(obj.getWidth(), obj.getHeight());
        BodyFixture fixture = new BodyFixture(rect);
        fixture.setFriction(0);

        Body body = new Body();
        body.addFixture(fixture);
        body.translate(obj.getX(), obj.getY());
        body.setMass(MassType.INFINITE);

        return body;
    }

    private Body createPlayBody() {
        Rectangle rect = new Rectangle(8, 8);
        BodyFixture fixture = new BodyFixture(rect);
        fixture.setFriction(0);

        Body body = new Body();
        body.addFixture(fixture);
        body.setMass(MassType.NORMAL);
        return body;
    }
}
