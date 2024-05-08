package io.github.jmecn.tiled.demo;

import com.jme3.app.SimpleApplication;
import com.jme3.app.StatsAppState;
import com.jme3.app.state.ScreenshotAppState;
import com.jme3.asset.TextureKey;
import com.jme3.math.Vector2f;
import com.jme3.scene.Spatial;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import io.github.jmecn.tiled.TmxLoader;
import io.github.jmecn.tiled.animation.Animation;
import io.github.jmecn.tiled.animation.Frame;
import io.github.jmecn.tiled.core.*;
import io.github.jmecn.tiled.demo.control.CharacterAnimControl;
import io.github.jmecn.tiled.demo.control.BodyControl;
import io.github.jmecn.tiled.demo.control.SensorControl;
import io.github.jmecn.tiled.demo.control.YSortControl;
import io.github.jmecn.tiled.demo.state.PhysicsDebugState;
import io.github.jmecn.tiled.demo.state.PhysicsState;
import io.github.jmecn.tiled.demo.state.PlayerState;
import io.github.jmecn.tiled.demo.state.ViewState;
import io.github.jmecn.tiled.enums.ObjectType;
import io.github.jmecn.tiled.renderer.MapRenderer;
import io.github.jmecn.tiled.util.TileCutter;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static io.github.jmecn.tiled.demo.Const.*;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public class Demo extends SimpleApplication {

    static Logger logger = LoggerFactory.getLogger(Demo.class);

    Demo() {
        super(new StatsAppState(), new ScreenshotAppState());
    }

    @Override
    public void simpleInitApp() {
        TmxLoader.registerLoader(assetManager);
        TiledMap tiledMap = (TiledMap) assetManager.loadAsset("Maps/jungle.tmx");

        // Load tileset by code
        Tileset tileset = buildTileset();

        ViewState viewState = new ViewState();
        viewState.initialize(stateManager, this);
        stateManager.attach(viewState);

        PhysicsState physicsState = new PhysicsState();
        physicsState.initialize(stateManager, this);
        stateManager.attach(physicsState);

        PlayerState playerState = new PlayerState();
        playerState.initialize(stateManager, this);
        stateManager.attach(playerState);

        viewState.setMap(tiledMap);
        MapRenderer mapRenderer = viewState.getMapRenderer();
        mapRenderer.render();// prepare for physics

        PhysicsDebugState debugState = new PhysicsDebugState(mapRenderer.getSpriteFactory());
        debugState.initialize(stateManager, this);
        stateManager.attach(debugState);

        // generate collisions
        initPhysics(physicsState, mapRenderer);

        ObjectGroup locations = (ObjectGroup) tiledMap.getLayer("Location");
        for (MapObject obj : locations.getObjects()) {
            if ("Start Point".equals(obj.getName())) {

                float sx = (float) obj.getX();
                float sy = (float) obj.getY();

                int layerIndex = tiledMap.getLayer("Objects").getIndex();
                float y = mapRenderer.getLayerYIndex(layerIndex) + mapRenderer.getObjectTopDownYIndex(sy);

                // Create player
                Tile tile = buildAnimatedTile(tileset, CHAR_BOY);
                MapObject playObj = new MapObject();
                playObj.setShape(ObjectType.TILE);
                playObj.setTile(tile);
                Spatial player = mapRenderer.getSpriteFactory().newObjectSprite(playObj, null);
                player.setLocalTranslation(sx, y, sy);
                player.addControl(new CharacterAnimControl());
                player.addControl(new YSortControl(mapRenderer, layerIndex));
                mapRenderer.getRootNode().attachChild(player);

                Body body = createPlayBody(physicsState, obj.getX(), obj.getY(), tile.getWidth(), tile.getHeight());
                player.addControl(new BodyControl(body));

                playerState.setBody(body);
                playerState.setPlayer(player);
                break;
            }
        }
    }

    private void initPhysics(PhysicsState physicsState, MapRenderer mapRenderer) {

        // generate collisions
        for (Layer layer : mapRenderer.getSortedLayers()) {
            if (layer instanceof TileLayer) {
                TileLayer tileLayer = (TileLayer) layer;
                mapRenderer.visitTiles((x, y, z) -> {
                    Tile tile = tileLayer.getTileAt(x, y);
                    if (tile != null && tile.getCollisions() != null) {
                        Vector2f pos = mapRenderer.tileToPixelCoords(x, y);
                        Vector2f size = new Vector2f(tile.getWidth(), tile.getHeight());
                        for (MapObject obj : tile.getCollisions().getObjects()) {
                            createTileBody(physicsState, pos, size, obj);
                        }
                    }
                });
            }

            if (layer instanceof ObjectGroup) {
                ObjectGroup objGroup = (ObjectGroup) layer;
                for (MapObject obj : objGroup.getObjects()) {
                    if (obj.getShape() == ObjectType.TILE) {
                        Tile tile = obj.getTile();
                        if (tile != null && tile.getCollisions() != null) {
                            Vector2f size = new Vector2f(tile.getWidth(), tile.getHeight());
                            Vector2f pos = new Vector2f((float) obj.getX(), (float) obj.getY());
                            for (MapObject collision : tile.getCollisions().getObjects()) {

                                boolean isSensor = collision.getProperty("is_sensor", false, Boolean.class);
                                String sensorBehavior = collision.getProperty("sensor_behavior", String.class);
                                Body body = createObjectBody(physicsState, pos, size, collision, isSensor);
                                if (isSensor) {
                                    SensorControl control = new SensorControl(body, sensorBehavior);
                                    physicsState.addContactListener(control);
                                    Spatial spatial = mapRenderer.getMapObjectSprite(objGroup, obj);
                                    if (spatial != null) {
                                        spatial.addControl(control);
                                    }
                                }
                            }
                        }
                    } else {
                        String bodyType = obj.getProperty("body_type", String.class);
                        if (bodyType == null) {
                            // not a physics object
                            continue;
                        }
                        boolean isSensor = obj.getProperty("is_sensor", false, Boolean.class);
                        String sensorBehavior = obj.getProperty("sensor_behavior", String.class);
                        Vector2f size = new Vector2f((float) obj.getWidth(), (float) obj.getHeight());
                        Vector2f pos = new Vector2f((float) obj.getX(), (float) obj.getY());
                        logger.info("Create object {} body at: {}, {}", obj.getName(), pos, size);
                        Body body = createObjectBody(physicsState, obj, isSensor);
                        if (isSensor) {
                            SensorControl control = new SensorControl(body, sensorBehavior);
                            physicsState.addContactListener(control);
                        }
                    }
                }
            }
        }
    }

    /**
     * Tile coordinate origin is on left-top corner.
     * @param physicsState
     * @param pos
     * @param size
     * @param obj
     */
    private Body createTileBody(PhysicsState physicsState, Vector2f pos, Vector2f size, MapObject obj) {
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.density = 1.0f;
        fixtureDef.friction = 0.0f;
        fixtureDef.restitution = 0.0f;

        BodyDef bodyDef = new BodyDef();

        switch (obj.getShape()) {
            case RECTANGLE: {
                float hx = (float) (obj.getWidth() * 0.5);
                float hy = (float) (obj.getHeight() * 0.5);
                PolygonShape shape = new PolygonShape();
                shape.setAsBox(hx, hy, new Vec2(hx, hy), 0);
                fixtureDef.shape = shape;
                break;
            }
            case POLYGON: {
                List<Vec2> vertices = new ArrayList<>();
                for (Vector2f v : obj.getPoints()) {
                    vertices.add(new Vec2(v.x, v.y));
                }
                PolygonShape shape = new PolygonShape();
                shape.set(vertices.toArray(new Vec2[0]), vertices.size());
                fixtureDef.shape = shape;
                break;
            }
            case ELLIPSE: {// box2d dose not support ellipse, use circle instead
                float hx = (float) (obj.getWidth() * 0.5);
                float hy = (float) (obj.getHeight() * 0.5);
                CircleShape shape = new CircleShape();
                shape.m_radius = Math.min(hx, hy);
                shape.m_p.set(hx, -hy);
                fixtureDef.shape = shape;
                break;
            }
            default: {
                logger.warn("Unsupported tile collision shape: {}", obj.getShape());
                return null;
            }
        }

        bodyDef.position.set((float) (obj.getX() + pos.x), (float) (obj.getY() + pos.y));
        bodyDef.type = BodyType.STATIC;

        logger.info("Create body at: {}, {}", bodyDef.position.x, bodyDef.position.y);

        return physicsState.createBody(bodyDef, fixtureDef);
    }

    /**
     * MapObject coordinate origin is on left-bottom corner.
     * @param physicsState
     * @param pos
     * @param size
     * @param obj
     */
    private Body createObjectBody(PhysicsState physicsState, Vector2f pos, Vector2f size, MapObject obj, boolean isSensor) {
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.density = 1.0f;
        fixtureDef.friction = 0.0f;
        fixtureDef.restitution = 0.0f;
        fixtureDef.isSensor = isSensor;

        BodyDef bodyDef = new BodyDef();

        float delta = (float) (size.y - obj.getHeight());
        switch (obj.getShape()) {
            case RECTANGLE: {
                float hx = (float) (obj.getWidth() * 0.5);
                float hy = (float) (obj.getHeight() * 0.5);
                PolygonShape shape = new PolygonShape();
                shape.setAsBox(hx, hy, new Vec2(hx, -hy), 0);
                fixtureDef.shape = shape;
                break;
            }
            case POLYGON: {
                List<Vec2> vertices = new ArrayList<>();
                for (Vector2f v : obj.getPoints()) {
                    vertices.add(new Vec2(v.x, v.y));
                }
                PolygonShape shape = new PolygonShape();
                shape.set(vertices.toArray(new Vec2[0]), vertices.size());
                fixtureDef.shape = shape;
                break;
            }
            case ELLIPSE: {// box2d dose not support ellipse, use circle instead
                float hx = (float) (obj.getWidth() * 0.5);
                float hy = (float) (obj.getHeight() * 0.5);
                CircleShape shape = new CircleShape();
                shape.m_radius = Math.min(hx, hy);
                shape.m_p.set(hx, -hy);
                fixtureDef.shape = shape;
                break;
            }
            default: {
                logger.warn("Unsupported shape: {}", obj.getShape());
                return null;
            }
        }

        bodyDef.position.set((float) (pos.x + obj.getX()), (float) (pos.y + obj.getY() - delta));
        bodyDef.type = BodyType.STATIC;

        return physicsState.createBody(bodyDef, fixtureDef);
    }


    /**
     * MapObject coordinate origin is on left-bottom corner.
     * @param physicsState
     * @param obj
     */
    private Body createObjectBody(PhysicsState physicsState, MapObject obj, boolean isSensor) {
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.density = 1.0f;
        fixtureDef.friction = 0.0f;
        fixtureDef.restitution = 0.0f;
        fixtureDef.isSensor = isSensor;

        BodyDef bodyDef = new BodyDef();

        switch (obj.getShape()) {
            case RECTANGLE: {
                float hx = (float) (obj.getWidth() * 0.5);
                float hy = (float) (obj.getHeight() * 0.5);
                PolygonShape shape = new PolygonShape();
                shape.setAsBox(hx, hy, new Vec2(hx, hy), 0);
                fixtureDef.shape = shape;
                break;
            }
            case POLYGON: {
                List<Vec2> vertices = new ArrayList<>();
                for (Vector2f v : obj.getPoints()) {
                    vertices.add(new Vec2(v.x, v.y));
                }
                PolygonShape shape = new PolygonShape();
                shape.set(vertices.toArray(new Vec2[0]), vertices.size());
                fixtureDef.shape = shape;
                break;
            }
            case ELLIPSE: {// box2d dose not support ellipse, use circle instead
                float hx = (float) (obj.getWidth() * 0.5);
                float hy = (float) (obj.getHeight() * 0.5);
                CircleShape shape = new CircleShape();
                shape.m_radius = Math.min(hx, hy);
                shape.m_p.set(hx, -hy);
                fixtureDef.shape = shape;
                break;
            }
            default: {
                logger.warn("Unsupported shape: {}", obj.getShape());
                return null;
            }
        }

        bodyDef.position.set((float) (obj.getX()), (float) (obj.getY()));
        bodyDef.type = BodyType.STATIC;

        return physicsState.createBody(bodyDef, fixtureDef);
    }

    private Body createPlayBody(PhysicsState physicsState, double x, double y, float width, float height) {
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.density = 1.0f;
        fixtureDef.friction = 0.0f;
        fixtureDef.restitution = 0.0f;

        float hx = width / 2;
        float qx = width / 4;
        float qy = height / 4;

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(qx, qy, new Vec2(hx, -qy), 0);
        fixtureDef.shape = shape;

        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set((float) x, (float) y);
        bodyDef.type = BodyType.DYNAMIC;
        bodyDef.fixedRotation = true;

        return physicsState.createBody(bodyDef, fixtureDef);
    }

    public Tileset buildTileset() {
        String source = "characters.png";
        TextureKey texKey = new TextureKey(source, true);
        texKey.setGenerateMips(false);
        Texture2D texture = (Texture2D) assetManager.loadTexture(texKey);
        texture.setWrap(Texture.WrapMode.EdgeClamp);
        texture.setMagFilter(Texture.MagFilter.Nearest);

        TiledImage image = new TiledImage(source, null, "png", 192, 128);
        image.setTexture(texture);

        Tileset tileset = new Tileset();
        tileset.setName("player");
        tileset.setTileWidth(16);
        tileset.setTileHeight(16);
        tileset.setSpacing(0);
        tileset.setMargin(0);
        tileset.setImage(image);
        tileset.setImageSource(source);

        TileCutter cutter = new TileCutter(image.getWidth(), image.getHeight(), 16, 16, 0, 0);
        tileset.setColumns(cutter.getColumns());
        tileset.setTileCount(cutter.getTileCount());

        Tile tile = cutter.getNextTile();
        while (tile != null) {
            tileset.addNewTile(tile);
            tile = cutter.getNextTile();
        }

        return tileset;
    }

    public Tile buildAnimatedTile(Tileset tileset, int index) {
        Tile tile = tileset.getTile(index);

        String[] idles = {ANIM_IDLE_DOWN, ANIM_IDLE_LEFT, ANIM_IDLE_RIGHT, ANIM_IDLE_UP};
        for (int i = 0; i < 4; i++) {
            Animation animation = new Animation();
            animation.setName(idles[i]);
            animation.addFrame(new Frame(index + i * 12, 100));
            tile.addAnimation(animation);
        }

        String[] names = {ANIM_WALK_DOWN, ANIM_WALK_LEFT, ANIM_WALK_RIGHT, ANIM_WALK_UP};
        for (int i = 0; i < 4; i++) {// 4 rows, 4 directions
            Animation animation = new Animation();
            animation.setName(names[i]);
            int frameId = index + i * 12;
            animation.addFrame(new Frame(frameId - 1, 150));
            animation.addFrame(new Frame(frameId, 150));
            animation.addFrame(new Frame(frameId + 1, 150));
            animation.addFrame(new Frame(frameId, 150));
            tile.addAnimation(animation);
        }

        return tile;
    }
}
