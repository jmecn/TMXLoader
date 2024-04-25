package io.github.jmecn.tiled.demo;

import com.jme3.app.DetailedProfilerState;
import com.jme3.app.SimpleApplication;
import com.jme3.app.StatsAppState;
import com.jme3.app.state.ScreenshotAppState;
import com.jme3.asset.TextureKey;
import com.jme3.scene.Geometry;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import io.github.jmecn.tiled.TmxLoader;
import io.github.jmecn.tiled.animation.Animation;
import io.github.jmecn.tiled.animation.Frame;
import io.github.jmecn.tiled.core.*;
import io.github.jmecn.tiled.demo.control.AnimStateControl;
import io.github.jmecn.tiled.demo.control.BodyControl;
import io.github.jmecn.tiled.demo.control.YSortControl;
import io.github.jmecn.tiled.demo.state.PhysicsState;
import io.github.jmecn.tiled.demo.state.PlayerState;
import io.github.jmecn.tiled.demo.state.ViewState;
import io.github.jmecn.tiled.renderer.MapRenderer;
import io.github.jmecn.tiled.util.TileCutter;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.*;

import static io.github.jmecn.tiled.demo.Const.*;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public class Demo extends SimpleApplication {

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

        ObjectGroup objectGroup = (ObjectGroup) tiledMap.getLayer("Collision");
        for (MapObject obj : objectGroup.getObjects()) {
            createBody(physicsState, obj);
        }

        ObjectGroup locations = (ObjectGroup) tiledMap.getLayer("Location");
        for (MapObject obj : locations.getObjects()) {
            if ("Start".equals(obj.getName())) {

                float sx = (float) obj.getX();
                float sy = (float) obj.getY();

                Body body = createPlayBody(physicsState, obj.getX(), obj.getY());

                int layerIndex = tiledMap.getLayer("Plants").getIndex();
                float y = mapRenderer.getLayerBaseHeight(layerIndex) + mapRenderer.getTileHeight(sx, sy);

                // Create player
                Tile tile = buildAnimatedTile(tileset, CHAR_GIRL);
                Geometry player = mapRenderer.getSpriteFactory().newTileSprite(tile);
                player.setLocalTranslation(sx, y, sy);
                player.addControl(new AnimStateControl());
                player.addControl(new BodyControl(body));
                player.addControl(new YSortControl(mapRenderer, layerIndex));

                mapRenderer.getRootNode().attachChild(player);

                playerState.setBody(body);
                playerState.setPlayer(player);
                break;
            }
        }
    }

    private void createBody(PhysicsState physicsState, MapObject obj) {
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

    }

    private Body createPlayBody(PhysicsState physicsState, double x, double y) {
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.density = 1.0f;
        fixtureDef.friction = 0.0f;
        fixtureDef.restitution = 0.0f;

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(8, 8, new Vec2(8, 8), 0);
        fixtureDef.shape = shape;

        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set((float) x, (float) y);
        bodyDef.type = BodyType.DYNAMIC;
        bodyDef.fixedRotation = true;

        Body body = physicsState.createBody(bodyDef);
        body.createFixture(fixtureDef);

        return body;
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
