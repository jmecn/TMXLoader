package com.jme3.tmx;

import java.awt.Color;
import java.awt.Image;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;
import java.util.TreeMap;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import tiled.core.AnimatedTile;
import tiled.core.Map;
import tiled.core.Tile;
import tiled.core.TileSet;
import tiled.util.Base64;
import tiled.util.BasicTileCutter;

import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetLoader;
import com.jme3.asset.AssetManager;

public class TsxLoader implements AssetLoader {

	private AssetManager assetManager;
	private AssetKey key;
	
	private Map map;
	private String xmlPath;
	private String error;
	private TreeMap<Integer, TileSet> tilesetPerFirstGid;

	@Override
	public TileSet load(AssetInfo assetInfo) throws IOException {
		key = assetInfo.getKey();
		xmlPath = key.getFolder();
		assetManager = assetInfo.getManager();

		TileSet set = null;
		Node tsNode;

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document tsDoc = builder.parse(assetInfo.openStream(), ".");

			String xmlPathSave = xmlPath;

			NodeList tsNodeList = tsDoc.getElementsByTagName("tileset");

			// There can be only one tileset in a .tsx file.
			tsNode = tsNodeList.item(0);
			if (tsNode != null) {
				set = unmarshalTileset(tsNode);
				if (set.getSource() != null) {
					System.out.println("Recursive external tilesets are not supported.");
				}
				set.setSource(key.getName());
			}

			xmlPath = xmlPathSave;
		} catch (SAXException e) {
			error = "Failed while loading " + key.getName() + ": "
					+ e.getLocalizedMessage();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return set;

	}

	private static int reflectFindMethodByName(Class c, String methodName) {
		Method[] methods = c.getMethods();
		for (int i = 0; i < methods.length; i++) {
			if (methods[i].getName().equalsIgnoreCase(methodName)) {
				return i;
			}
		}
		return -1;
	}

	private void reflectInvokeMethod(Object invokeVictim, Method method,
			String[] args) throws Exception {
		Class[] parameterTypes = method.getParameterTypes();
		Object[] conformingArguments = new Object[parameterTypes.length];

		if (args.length < parameterTypes.length) {
			throw new Exception("Insufficient arguments were supplied");
		}

		for (int i = 0; i < parameterTypes.length; i++) {
			if ("int".equalsIgnoreCase(parameterTypes[i].getName())) {
				conformingArguments[i] = new Integer(args[i]);
			} else if ("float".equalsIgnoreCase(parameterTypes[i].getName())) {
				conformingArguments[i] = new Float(args[i]);
			} else if (parameterTypes[i].getName().endsWith("String")) {
				conformingArguments[i] = args[i];
			} else if ("boolean".equalsIgnoreCase(parameterTypes[i].getName())) {
				conformingArguments[i] = Boolean.valueOf(args[i]);
			} else {
				// Unsupported argument type, defaulting to String
				conformingArguments[i] = args[i];
			}
		}

		method.invoke(invokeVictim, conformingArguments);
	}

	private static String getAttributeValue(Node node, String attribname) {
		final NamedNodeMap attributes = node.getAttributes();
		String value = null;
		if (attributes != null) {
			Node attribute = attributes.getNamedItem(attribname);
			if (attribute != null) {
				value = attribute.getNodeValue();
			}
		}
		return value;
	}

	private static int getAttribute(Node node, String attribname, int def) {
		final String attr = getAttributeValue(node, attribname);
		if (attr != null) {
			return Integer.parseInt(attr);
		} else {
			return def;
		}
	}

	private Object unmarshalClass(Class reflector, Node node)
			throws InstantiationException, IllegalAccessException,
			InvocationTargetException {
		Constructor cons = null;
		try {
			cons = reflector.getConstructor((Class[]) null);
		} catch (SecurityException e1) {
			// todo: replace with log message
			e1.printStackTrace();
		} catch (NoSuchMethodException e1) {
			// todo: replace with log message
			e1.printStackTrace();
			return null;
		}
		Object o = cons.newInstance((Object[]) null);
		Node n;

		Method[] methods = reflector.getMethods();
		NamedNodeMap nnm = node.getAttributes();

		if (nnm != null) {
			for (int i = 0; i < nnm.getLength(); i++) {
				n = nnm.item(i);

				try {
					int j = reflectFindMethodByName(reflector,
							"set" + n.getNodeName());
					if (j >= 0) {
						reflectInvokeMethod(o, methods[j],
								new String[] { n.getNodeValue() });
					} else {
						System.out.println("Unsupported attribute '"
								+ n.getNodeName() + "' on <"
								+ node.getNodeName() + "> tag");
					}
				} catch (Exception e) { // todo: fix pok¨¦mon exception handling
					// todo: replace with log message
					e.printStackTrace();
				}
			}
		}
		return o;
	}

	private Image unmarshalImage(Node t, String baseDir) throws IOException {
		Image img = null;

		String source = getAttributeValue(t, "source");

		if (source != null) {
			// TODO : fix source
			AssetInfo info = assetManager.locateAsset(new AssetKey(source));
			img = ImageIO.read(info.openStream());
		} else {
			NodeList nl = t.getChildNodes();

			for (int i = 0; i < nl.getLength(); i++) {
				Node node = nl.item(i);
				if ("data".equals(node.getNodeName())) {
					Node cdata = node.getFirstChild();
					if (cdata != null) {
						String sdata = cdata.getNodeValue();
						char[] charArray = sdata.trim().toCharArray();
						byte[] imageData = Base64.decode(charArray);
						
						// for android 
						// Bitmap bmp = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
						
						// for desktop
						img = ImageIO.read(new ByteArrayInputStream(imageData));
					}
					break;
				}
			}
		}

		return img;
	}

	private TileSet unmarshalTileset(Node t) throws Exception {
		String source = getAttributeValue(t, "source");
		String basedir = getAttributeValue(t, "basedir");
		int firstGid = getAttribute(t, "firstgid", 1);

		String tilesetBaseDir = xmlPath;

		if (basedir != null) {
			tilesetBaseDir = basedir;
		}

		if (source != null) {
			String filename = tilesetBaseDir + source;

			TileSet ext = null;

			try {
				ext = (TileSet)assetManager.loadAsset(filename);
				setFirstGidForTileset(ext, firstGid);
			} catch (Exception e) {
				error = "Could not find external tileset file " + filename;
			}

			if (ext == null) {
				error = "Tileset " + source + " was not loaded correctly!";
			}

			return ext;
		} else {
			final int tileWidth = getAttribute(t, "tilewidth",
					map != null ? map.getTileWidth() : 0);
			final int tileHeight = getAttribute(t, "tileheight",
					map != null ? map.getTileHeight() : 0);
			final int tileSpacing = getAttribute(t, "spacing", 0);
			final int tileMargin = getAttribute(t, "margin", 0);

			final String name = getAttributeValue(t, "name");

			TileSet set = new TileSet();

			set.setName(name);
			set.setBaseDir(basedir);
			setFirstGidForTileset(set, firstGid);

			boolean hasTilesetImage = false;
			NodeList children = t.getChildNodes();

			for (int i = 0; i < children.getLength(); i++) {
				Node child = children.item(i);

				if (child.getNodeName().equalsIgnoreCase("image")) {
					if (hasTilesetImage) {
						System.out
								.println("Ignoring illegal image element after tileset image.");
						continue;
					}

					String imgSource = getAttributeValue(child, "source");
					String transStr = getAttributeValue(child, "trans");

					if (imgSource != null) {
						// Not a shared image, but an entire set in one image
						// file. There should be only one image element in this
						// case.
						hasTilesetImage = true;

						// FIXME: importTileBitmap does not fully support URLs
						String sourcePath = imgSource;
						if (!new File(imgSource).isAbsolute()) {
							sourcePath = tilesetBaseDir + imgSource;
						}

						if (transStr != null) {
							if (transStr.startsWith("#")) {
								transStr = transStr.substring(1);
							}

							int colorInt = Integer.parseInt(transStr, 16);
							Color color = new Color(colorInt);
							set.setTransparentColor(color);
						}

						set.importTileBitmap(sourcePath, new BasicTileCutter(
								tileWidth, tileHeight, tileSpacing, tileMargin));
					}
				} else if (child.getNodeName().equalsIgnoreCase("tile")) {
					Tile tile = unmarshalTile(set, child, tilesetBaseDir);
					if (!hasTilesetImage || tile.getId() > set.getMaxTileId()) {
						set.addTile(tile);
					} else {
						Tile myTile = set.getTile(tile.getId());
						myTile.setProperties(tile.getProperties());
						// TODO: there is the possibility here of overlaying
						// images,
						// which some people may want
					}
				}
			}

			return set;
		}
	}

	/**
	 * Reads properties from amongst the given children. When a "properties"
	 * element is encountered, it recursively calls itself with the children of
	 * this node. This function ensures backward compatibility with tmx version
	 * 0.99a.
	 * 
	 * Support for reading property values stored as character data was added in
	 * Tiled 0.7.0 (tmx version 0.99c).
	 * 
	 * @param children
	 *            the children amongst which to find properties
	 * @param props
	 *            the properties object to set the properties of
	 */
	private static void readProperties(NodeList children, Properties props) {
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if ("property".equalsIgnoreCase(child.getNodeName())) {
				final String key = getAttributeValue(child, "name");
				String value = getAttributeValue(child, "value");
				if (value == null) {
					Node grandChild = child.getFirstChild();
					if (grandChild != null) {
						value = grandChild.getNodeValue();
						if (value != null) {
							value = value.trim();
						}
					}
				}
				if (value != null) {
					props.setProperty(key, value);
				}
			} else if ("properties".equals(child.getNodeName())) {
				readProperties(child.getChildNodes(), props);
			}
		}
	}

	private Tile unmarshalTile(TileSet set, Node t, String baseDir)
			throws Exception {
		Tile tile = null;
		NodeList children = t.getChildNodes();
		boolean isAnimated = false;

		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if ("animation".equalsIgnoreCase(child.getNodeName())) {
				isAnimated = true;
				break;
			}
		}

		try {
			if (isAnimated) {
				tile = (Tile) unmarshalClass(AnimatedTile.class, t);
			} else {
				tile = (Tile) unmarshalClass(Tile.class, t);
			}
		} catch (Exception e) { // todo: fix pok¨¦mon exception handling
			error = "Failed creating tile: " + e.getLocalizedMessage();
			return tile;
		}

		tile.setTileSet(set);

		readProperties(children, tile.getProperties());

		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if ("image".equalsIgnoreCase(child.getNodeName())) {
				Image img = unmarshalImage(child, baseDir);
				tile.setImage(img);
			} else if ("animation".equalsIgnoreCase(child.getNodeName())) {
				// TODO: fill this in once TMXMapWriter is complete
			}
		}

		return tile;
	}

	/**
	 * Helper method to get the tile based on its global id
	 * 
	 * @param tileId
	 *            global id of the tile
	 * @return <ul>
	 *         <li>{@link Tile} object corresponding to the global id, if found</li>
	 *         <li><code>null</code>, otherwise</li>
	 *         </ul>
	 */
	private Tile getTileForTileGID(int tileId) {
		Tile tile = null;
		java.util.Map.Entry<Integer, TileSet> ts = findTileSetForTileGID(tileId);
		if (ts != null) {
			tile = ts.getValue().getTile(tileId - ts.getKey());
		}
		return tile;
	}

	/**
	 * Get the tile set and its corresponding firstgid that matches the given
	 * global tile id.
	 * 
	 * 
	 * @param gid
	 *            a global tile id
	 * @return the tileset containing the tile with the given global tile id, or
	 *         <code>null</code> when no such tileset exists
	 */
	private java.util.Map.Entry<Integer, TileSet> findTileSetForTileGID(int gid) {
		return tilesetPerFirstGid.floorEntry(gid);
	}

	private void setFirstGidForTileset(TileSet tileset, int firstGid) {
		tilesetPerFirstGid.put(firstGid, tileset);
	}

}
