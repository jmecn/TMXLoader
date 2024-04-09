# Introduction

TMXLoader is used for loading .tmx assets created by [Tiled Map Editor](http://www.mapeditor.org/). It's a plugin for [jMonkeyEngine 3.6](http://jmonkeyengine.org/).

You can download it here: [TmxLoader v0.3](https://github.com/jmecn/TMXLoader/releases/tag/v0.3).
Or use the [source](https://github.com/jmecn/TMXLoader).

[![Java CI with Gradle](https://github.com/jmecn/TMXLoader/actions/workflows/gradle.yml/badge.svg?branch=master)](https://github.com/jmecn/TMXLoader/actions/workflows/gradle.yml)

# How to use

	package net.jmecn.tmx;
	
	import com.jme3.app.SimpleApplication;
	import com.jme3.tmx.TiledMapAppState;
	import com.jme3.tmx.TmxLoader;
	import com.jme3.tmx.core.TiledMap;
	
	/**
	 * Test loading tmx assets with TmxLoader.
	 * @author yanmaoyuan
	 *
	 */
	public class TmxLoaderExample extends SimpleApplication {
	
		@Override
		public void simpleInitApp() {
			// register it
			assetManager.registerLoader(TmxLoader.class, "tmx", "tsx");
	
			// load tmx with it
			TiledMap map = (TiledMap) assetManager.loadAsset("Models/Examples/Desert/desert.tmx");
			assert map != null;
	
			// render it with TiledMapAppState
			stateManager.attach(new TiledMapAppState());
			
			TiledMapAppState tiledMap = stateManager.getState(TiledMapAppState.class);
			tiledMap.setMap(map);
		}
	
		public static void main(String[] args) {
			TmxLoaderExample app = new TmxLoaderExample();
			app.start();
		}
	
	}

# Screenshoots

* Orthogonal Map

<img src="https://hub.jmonkeyengine.org/uploads/default/original/2X/6/61ddca043c9bb0f537884127bd5d540dda5775f9.png" width="690" height="468">

* Iso map

<img src="https://hub.jmonkeyengine.org/uploads/default/original/2X/9/93c020c62019899610ec3643559ed10174e8e2db.png" width="690" height="447">
* Hex map
<img src="https://hub.jmonkeyengine.org/uploads/default/original/2X/a/afa5abeab823a25fb12f2d49180002cf83def3e9.png" width="635" height="500">

* Staggered map

<img src="https://hub.jmonkeyengine.org/uploads/default/original/2X/0/06941c964fa72209d669c01a89a7e88e3b18849c.png" width="690" height="366">

# The MapRenderer

If you don't want to make a renderer your self, I have created some MapRenderers for easy use.

* com.jme3.tmx.render.MapRenderer;
* com.jme3.tmx.render.HexagonalRenderer;
* com.jme3.tmx.render.IsometricRenderer;
* com.jme3.tmx.render.OrthogonalRenderer;
* com.jme3.tmx.render.StaggeredRenderer;

As there are 3 kind of layers in a TiledMap:

* TileLayer
* ObjectGroup
* ImageLayer

The MapRenderers provides 3 methods to make spatials for each layer.

	public Spatial render(TileLayer layer);
	public Spatial render(ObjectLayer layer);
	public Spatial render(ImageLayer layer);

Here it an example I wrote earlier.

	public void render(TiledMap map) {
		this.map = map;
		
		switch (map.getOrientation()) {
		case ORTHOGONAL:
			mapRender = new OrthogonalRenderer(map);
			break;
		case ISOMETRIC:
			mapRender = new IsometricRenderer(map);
			break;
		case HEXAGONAL:
			mapRender = new HexagonalRenderer(map);
			break;
		case STAGGERED:
			mapRender = new StaggeredRenderer(map);
			break;
		}

		mapRender.updateVisual();

		// background color
		if (map.getBackgroundColor() != null) {
			viewPort.setBackgroundColor(map.getBackgroundColor());
		} else {
			viewPort.setBackgroundColor(ColorRGBA.Black);
		}

		Node node = new Node("Tiled Map");
		int len = map.getLayerCount();
		
		int layerCnt = 0;
		
		for (int i = 0; i < len; i++) {
			Layer layer = map.getLayer(i);

			// skip invisible layer
			if (!layer.isVisible()) {
				continue;
			}

			Spatial visual = null;
			if (layer instanceof TileLayer) {
				visual = mapRender.render((TileLayer) layer);
			}

			if (layer instanceof ObjectLayer) {
				visual = mapRender.render((ObjectLayer) layer);
			}

			if (layer instanceof ImageLayer) {
				visual = mapRender.render((ImageLayer) layer);
			}

			if (visual != null) {
				node.attachChild(visual);
				
				// this is a little magic to make let top layer block off the
				// bottom layer
				visual.setLocalTranslation(0, layerCnt++, 0);
			}
		}
		
		// make the whole map thinner
		if (layerCnt > 0) {
			node.setLocalScale(1, 1f / layerCnt, 1);
		}

		rootNode.attachChild(node);
	}

The [com.jme3.tmx.TiledMapAppState](https://github.com/jmecn/TMXLoader/blob/master/src/main/java/com/jme3/tmx/TiledMapAppState.java) will help you to do all the mess.

# Changelog

v0.3

* Add Image Layer support
* Add Group Layer support
* Add Tinting Color support

v0.2
* Use Bucket.GUI instead of Bucket.Transparent, change whole scene from XOZ plane to XOY plane now.
* Removed RPGCameraState, use only TiledMapAppState now.
* Fixed issue #1 and #2: Removed BatchNode, use simple Node instead.
* Fixed issue #3: Now tiles with flip mask can display correctly.
* Add new feature  for animated tiles.

![animatedtile](https://cloud.githubusercontent.com/assets/5283598/21221336/f211c08e-c2f7-11e6-9de8-e3018fd65b07.gif)

v0.1
* First release

# TODO

* Fixed issue #4
* Add interface to let control tiles and objects in the scene.
* Handle layer offsets
* Add 2D physicals or leave it for monkeys.