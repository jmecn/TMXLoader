package com.jme3.tmx;

import java.awt.Color;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import tiled.core.AnimatedTile;
import tiled.core.Map;
import tiled.core.MapLayer;
import tiled.core.MapObject;
import tiled.core.ObjectGroup;
import tiled.core.Tile;
import tiled.core.TileLayer;
import tiled.core.TileSet;
import tiled.util.Base64;
import tiled.util.BasicTileCutter;
import tiled.util.TransparentImageFilter;

import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetLoader;
import com.jme3.asset.AssetManager;
import com.jme3.asset.TextureKey;
import com.jme3.math.ColorRGBA;
import com.jme3.texture.Texture2D;
import com.sun.istack.internal.logging.Logger;

public class TmxLoader implements AssetLoader {

	static Logger logger = Logger.getLogger(TmxLoader.class);

	private AssetManager assetManager;
	private AssetKey key;

	private Map map;
	private String xmlPath;
	private TreeMap<Integer, TileSet> tilesetPerFirstGid;

	@Override
	public Object load(AssetInfo assetInfo) throws IOException {
		key = assetInfo.getKey();
		assetManager = assetInfo.getManager();

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		Document doc;
		try {
			factory.setIgnoringComments(true);
			factory.setIgnoringElementContentWhitespace(true);
			factory.setExpandEntityReferences(false);
			DocumentBuilder builder = factory.newDocumentBuilder();
			builder.setEntityResolver(new EntityResolver() {
				@Override
				public InputSource resolveEntity(String publicId, String systemId) {
					if (systemId.equals("http://mapeditor.org/dtd/1.0/map.dtd")) {
						return new InputSource(getClass().getResourceAsStream("resources/map.dtd"));
					}
					return null;
				}
			});
			InputSource insrc = new InputSource(assetInfo.openStream());
			insrc.setSystemId(key.getFolder());
			insrc.setEncoding("UTF-8");
			doc = builder.parse(insrc);
		} catch (SAXException e) {
			e.printStackTrace();
			throw new RuntimeException("Error while parsing map file: " + e.toString());
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			return null;
		}

		try {
			buildMap(doc);
		} catch (Exception e) {
			e.printStackTrace();
		}


		return map;
		
	}
	
	private void buildMap(Document doc) throws Exception {
		Node item, mapNode;

		mapNode = doc.getDocumentElement();

		if (!"map".equals(mapNode.getNodeName())) {
			throw new Exception("Not a valid tmx map file.");
		}

		// Get the map dimensions and create the map
		int mapWidth = getAttribute(mapNode, "width", 0);
		int mapHeight = getAttribute(mapNode, "height", 0);

		if (mapWidth > 0 && mapHeight > 0) {
			map = new Map(mapWidth, mapHeight);
		} else {
			// Maybe this map is still using the dimensions element
			NodeList l = doc.getElementsByTagName("dimensions");
			for (int i = 0; (item = l.item(i)) != null; i++) {
				if (item.getParentNode() == mapNode) {
					mapWidth = getAttribute(item, "width", 0);
					mapHeight = getAttribute(item, "height", 0);

					if (mapWidth > 0 && mapHeight > 0) {
						map = new Map(mapWidth, mapHeight);
					}
				}
			}
		}

		if (map == null) {
			logger.warning("Couldn't locate map dimensions.");
			throw new RuntimeException("Couldn't locate map dimensions.");
		}

		// Load other map attributes
		String orientation = getAttributeValue(mapNode, "orientation");
		int tileWidth = getAttribute(mapNode, "tilewidth", 0);
		int tileHeight = getAttribute(mapNode, "tileheight", 0);
		int hexsidelength = getAttribute(mapNode, "hexsidelength", 0);
		String staggerAxis = getAttributeValue(mapNode, "staggeraxis");
		String staggerIndex = getAttributeValue(mapNode, "staggerindex");

		if (orientation != null) {
			setOrientation(orientation.toUpperCase());
		} else {
			map.setOrientation(Map.Orientation.ORTHOGONAL);
		}

		if (tileWidth > 0) {
			map.setTileWidth(tileWidth);
		}
		if (tileHeight > 0) {
			map.setTileHeight(tileHeight);
		}
		if (hexsidelength > 0) {
			map.setHexSideLength(hexsidelength);
		}

		if (staggerAxis != null) {
			setStaggerAxis(staggerAxis.toUpperCase());
		}

		if (staggerIndex != null) {
			setStaggerIndex(staggerIndex.toUpperCase());
		}

		// Load properties
		readProperties(mapNode.getChildNodes(), map.getProperties());

		// Load tilesets first, in case order is munged
		tilesetPerFirstGid = new TreeMap<>();
		NodeList l = doc.getElementsByTagName("tileset");
		for (int i = 0; (item = l.item(i)) != null; i++) {
			map.addTileset(buildTileset(item));
		}

		// Load the layers and objectgroups
		for (Node sibs = mapNode.getFirstChild(); sibs != null; sibs = sibs.getNextSibling()) {
			if ("layer".equals(sibs.getNodeName())) {
				MapLayer layer = readLayer(sibs);
				if (layer != null) {
					map.addLayer(layer);
				}
			} else if ("objectgroup".equals(sibs.getNodeName())) {
				MapLayer layer = buildObjectGroup(sibs);
				if (layer != null) {
					map.addLayer(layer);
				}
			}
		}
		tilesetPerFirstGid = null;
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

	private void setOrientation(String orientation) {
		try {
			map.setOrientation(Map.Orientation.valueOf(orientation));
		} catch (IllegalArgumentException e) {
			logger.warning("Unknown orientation '" + orientation + "'");
		}
	}

	private void setStaggerAxis(String staggerAxis) {
		try {
			map.setStaggerAxis(Map.StaggerAxis.valueOf(staggerAxis));
		} catch (IllegalArgumentException e) {
			logger.warning("Unknown stagger axis '" + staggerAxis + "'");
		}
	}

	private void setStaggerIndex(String staggerIndex) {
		try {
			map.setStaggerIndex(Map.StaggerIndex.valueOf(staggerIndex));
		} catch (IllegalArgumentException e) {
			logger.warning("Unknown stagger index '" + staggerIndex + "'");
		}
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

	private static double getDoubleAttribute(Node node, String attribname,
			double def) {
		final String attr = getAttributeValue(node, attribname);
		if (attr != null) {
			return Double.parseDouble(attr);
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
			logger.warning("" + e1.getMessage(), e1);
		} catch (NoSuchMethodException e1) {
			logger.warning("" + e1.getMessage(), e1);
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
					logger.warning("" + e.getMessage(), e);
				}
			}
		}
		return o;
	}

	private Image unmarshalImage(Node t, String baseDir) throws IOException {
		Image img = null;

		String source = getAttributeValue(t, "source");

		if (source != null) {
			// TODO : replace java.awt.Image with com.jme3.texture.Image
			Texture2D tex = loadTexture2D(source);
			logger.info("tex:" + tex);
			
			AssetInfo info = assetManager.locateAsset(new AssetKey(source));
			img = ImageIO.read(info.openStream());
			logger.info("img:" + img);
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

						// TODO replace java.awt.Image with com.jme3.texture.Image
						Texture2D tex = loadTexture2D(imageData);
						logger.info("tex:" + tex);
						
						// for android
						// Bitmap bmp = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);

						// for desktop
						img = ImageIO.read(new ByteArrayInputStream(imageData));

						// Deriving a scaled instance, even if it has the same
						// size, somehow makes drawing of the tiles a lot
						// faster on various systems (seen on Linux, Windows
						// and MacOS X).
						img = img.getScaledInstance(img.getWidth(null),
								img.getHeight(null), Image.SCALE_FAST);
					}
					break;
				}
			}
		}

		return img;
	}

	private TileSet buildTileset(Node t) throws Exception {
		TileSet set = null;
		
		String source = getAttributeValue(t, "source");
		String basedir = key.getFolder();// getAttributeValue(t, "basedir");
		int firstGid = getAttribute(t, "firstgid", 1);
		
		if (source != null) {
			set = loadTileSet(source);
		} else {
			
			final int tileWidth = getAttribute(t, "tilewidth", map != null ? map.getTileWidth() : 0);
			final int tileHeight = getAttribute(t, "tileheight", map != null ? map.getTileHeight() : 0);
			final int tileSpacing = getAttribute(t, "spacing", 0);
			final int tileMargin = getAttribute(t, "margin", 0);
			final int tileCount = getAttribute(t, "tilecount", 0);
			final int columns = getAttribute(t, "columns", 0);// tilesPerRow

			final String name = getAttributeValue(t, "name");

			set = new TileSet();

			set.setName(name);
			set.setBaseDir(basedir);

			boolean hasTilesetImage = false;
			NodeList children = t.getChildNodes();

			for (int i = 0; i < children.getLength(); i++) {
				Node child = children.item(i);

				if (child.getNodeName().equalsIgnoreCase("image")) {
					if (hasTilesetImage) {
						logger.warning("Ignoring illegal image element after tileset image.");
						continue;
					}

					String imgSource = getAttributeValue(child, "source");
					String transStr = getAttributeValue(child, "trans");

					if (imgSource != null) {
						// Not a shared image, but an entire set in one image
						// file. There should be only one image element in this
						// case.
						hasTilesetImage = true;

						String sourcePath = toJmeAssetPath(imgSource);
						
						Texture2D tex = loadTexture2D(imgSource);
						logger.info("Texture:" + tex);
						
						Color transparentColor = null;
						if (transStr != null) {
							// #RRGGBB || RRGGBB
							if (transStr.startsWith("#")) {
								transStr = transStr.substring(1);
							}

							int rgb = Integer.parseInt(transStr, 16);
							transparentColor = new Color(rgb);
							set.setTransparentColor(transparentColor);
							
							// TODO use ColorRGBA
							int red = (rgb >> 16) & 0xFF;
							int green = (rgb >> 8) & 0xFF;
							int blue = (rgb >> 0) & 0xFF;
							float scalor = 1f / 255f;
							ColorRGBA transColor = new ColorRGBA(red * scalor, green * scalor, blue * scalor, 1f);
						}

						AssetInfo info = assetManager.locateAsset(new AssetKey(sourcePath));
						Image image = ImageIO.read(info.openStream());
				        if (image == null) {
				            throw new IOException("Failed to load " + sourcePath);
				        }

				        Toolkit tk = Toolkit.getDefaultToolkit();

				        if (transparentColor != null) {
				            int rgb = transparentColor.getRGB();
				            image = tk.createImage(
				                    new FilteredImageSource(image.getSource(),
				                            new TransparentImageFilter(rgb)));
				        }

				        BufferedImage buffered = new BufferedImage(
				                image.getWidth(null),
				                image.getHeight(null),
				                BufferedImage.TYPE_INT_ARGB);
				        buffered.getGraphics().drawImage(image, 0, 0, null);
				        
						set.importTileBitmap(buffered, new BasicTileCutter(
								tileWidth, tileHeight, tileSpacing, tileMargin));
					}
				} else if (child.getNodeName().equalsIgnoreCase("tile")) {
					Tile tile = unmarshalTile(set, child, basedir);
					if (!hasTilesetImage || tile.getId() > set.getMaxTileId()) {
						set.addTile(tile);
					} else {
						Tile myTile = set.getTile(tile.getId());
						myTile.setProperties(tile.getProperties());
						// TODO: there is the possibility here of overlaying
						// images, which some people may want
					}
				}
			}
		}
		
		if (set != null) {
			setFirstGidForTileset(set, firstGid);
		}
		
		return set;
	}
	
	private MapObject readMapObject(Node t) throws Exception {
		final String name = getAttributeValue(t, "name");
		final String type = getAttributeValue(t, "type");
		final String gid = getAttributeValue(t, "gid");
		final double x = getDoubleAttribute(t, "x", 0);
		final double y = getDoubleAttribute(t, "y", 0);
		final double width = getDoubleAttribute(t, "width", 0);
		final double height = getDoubleAttribute(t, "height", 0);

		MapObject obj = new MapObject(x, y, width, height);
		obj.setShape(obj.getBounds());
		if (name != null) {
			obj.setName(name);
		}
		if (type != null) {
			obj.setType(type);
		}
		if (gid != null) {
			Tile tile = getTileForTileGID(Integer.parseInt(gid));
			obj.setTile(tile);
		}

		NodeList children = t.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if ("image".equalsIgnoreCase(child.getNodeName())) {
				String source = getAttributeValue(child, "source");
				if (source != null) {
					if (!new File(source).isAbsolute()) {
						source = xmlPath + source;
					}
					obj.setImageSource(source);
				}
				break;
			} else if ("ellipse".equalsIgnoreCase(child.getNodeName())) {
				obj.setShape(new Ellipse2D.Double(x, y, width, height));
			} else if ("polygon".equalsIgnoreCase(child.getNodeName())
					|| "polyline".equalsIgnoreCase(child.getNodeName())) {
				Path2D.Double shape = new Path2D.Double();
				final String pointsAttribute = getAttributeValue(child,
						"points");
				StringTokenizer st = new StringTokenizer(pointsAttribute, ", ");
				boolean firstPoint = true;
				while (st.hasMoreElements()) {
					double pointX = Double.parseDouble(st.nextToken());
					double pointY = Double.parseDouble(st.nextToken());
					if (firstPoint) {
						shape.moveTo(x + pointX, y + pointY);
						firstPoint = false;
					} else {
						shape.lineTo(x + pointX, y + pointY);
					}
				}
				shape.closePath();
				obj.setShape(shape);
				obj.setBounds((Rectangle2D.Double) shape.getBounds2D());
			}
		}

		Properties props = new Properties();
		readProperties(children, props);

		obj.setProperties(props);
		return obj;
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

	private Tile unmarshalTile(TileSet set, Node t, String baseDir) throws Exception {
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
		} catch (Exception e) { // TODO: fix pok¨¦mon exception handling
			logger.warning("Failed creating tile: " + e.getLocalizedMessage(), e);
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

	private MapLayer buildObjectGroup(Node t) throws Exception {
		ObjectGroup og = null;
		try {
			og = (ObjectGroup) unmarshalClass(ObjectGroup.class, t);
		} catch (Exception e) {// todo: fix pok¨¦mon exception handling
			logger.warning("failed load object group", e);
			return og;
		}

		final int offsetX = getAttribute(t, "x", 0);
		final int offsetY = getAttribute(t, "y", 0);
		og.setOffset(offsetX, offsetY);

		// Add all objects from the objects group
		NodeList children = t.getChildNodes();

		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if ("object".equalsIgnoreCase(child.getNodeName())) {
				og.addObject(readMapObject(child));
			}
		}

		Properties props = new Properties();
		readProperties(children, props);
		og.setProperties(props);

		return og;
	}

	/**
	 * Loads a map layer from a layer node.
	 * 
	 * @param t
	 *            the node representing the "layer" element
	 * @return the loaded map layer
	 * @throws Exception
	 */
	private MapLayer readLayer(Node t) throws Exception {
		final int layerWidth = getAttribute(t, "width", map.getWidth());
		final int layerHeight = getAttribute(t, "height", map.getHeight());

		TileLayer ml = new TileLayer(layerWidth, layerHeight);

		final int offsetX = getAttribute(t, "x", 0);
		final int offsetY = getAttribute(t, "y", 0);
		final int visible = getAttribute(t, "visible", 1);
		String opacity = getAttributeValue(t, "opacity");

		ml.setName(getAttributeValue(t, "name"));

		if (opacity != null) {
			ml.setOpacity(Float.parseFloat(opacity));
		}

		readProperties(t.getChildNodes(), ml.getProperties());

		for (Node child = t.getFirstChild(); child != null; child = child
				.getNextSibling()) {
			String nodeName = child.getNodeName();
			if ("data".equalsIgnoreCase(nodeName)) {
				String encoding = getAttributeValue(child, "encoding");
				String comp = getAttributeValue(child, "compression");

				if ("base64".equalsIgnoreCase(encoding)) {
					Node cdata = child.getFirstChild();
					if (cdata != null) {
						char[] enc = cdata.getNodeValue().trim().toCharArray();
						byte[] dec = Base64.decode(enc);
						ByteArrayInputStream bais = new ByteArrayInputStream(dec);
						InputStream is;

						if ("gzip".equalsIgnoreCase(comp)) {
							final int len = layerWidth * layerHeight * 4;
							is = new GZIPInputStream(bais, len);
						} else if ("zlib".equalsIgnoreCase(comp)) {
							is = new InflaterInputStream(bais);
						} else if (comp != null && !comp.isEmpty()) {
							throw new IOException(
									"Unrecognized compression method \"" + comp
											+ "\" for map layer "
											+ ml.getName());
						} else {
							is = bais;
						}

						for (int y = 0; y < ml.getHeight(); y++) {
							for (int x = 0; x < ml.getWidth(); x++) {
								int tileId = 0;
								tileId |= is.read();
								tileId |= is.read() << 8;
								tileId |= is.read() << 16;
								tileId |= is.read() << 24;

								setTileAtFromTileId(ml, y, x, tileId);
							}
						}
					}
				} else if ("csv".equalsIgnoreCase(encoding)) {
					String csvText = child.getTextContent();

					if (comp != null && !comp.isEmpty()) {
						throw new IOException(
								"Unrecognized compression method \"" + comp
										+ "\" for map layer " + ml.getName()
										+ " and encoding " + encoding);
					}

					String[] csvTileIds = csvText.trim() // trim 'space', 'tab', 'newline'. pay attention to
														// additional unicode chars like \u2028, \u2029, \u0085 if necessary
							.split("[\\s]*,[\\s]*");

					if (csvTileIds.length != ml.getHeight() * ml.getWidth()) {
						throw new IOException(
								"Number of tiles does not match the layer's width and height");
					}

					for (int y = 0; y < ml.getHeight(); y++) {
						for (int x = 0; x < ml.getWidth(); x++) {
							String sTileId = csvTileIds[x + y * ml.getWidth()];
							int tileId = Integer.parseInt(sTileId);

							setTileAtFromTileId(ml, y, x, tileId);
						}
					}
				} else {
					int x = 0, y = 0;
					for (Node dataChild = child.getFirstChild(); dataChild != null; dataChild = dataChild
							.getNextSibling()) {
						if ("tile".equalsIgnoreCase(dataChild.getNodeName())) {
							int tileId = getAttribute(dataChild, "gid", -1);
							setTileAtFromTileId(ml, y, x, tileId);

							x++;
							if (x == ml.getWidth()) {
								x = 0;
								y++;
							}
							if (y == ml.getHeight()) {
								break;
							}
						}
					}
				}
			} else if ("tileproperties".equalsIgnoreCase(nodeName)) {
				for (Node tpn = child.getFirstChild(); tpn != null; tpn = tpn
						.getNextSibling()) {
					if ("tile".equalsIgnoreCase(tpn.getNodeName())) {
						int x = getAttribute(tpn, "x", -1);
						int y = getAttribute(tpn, "y", -1);

						Properties tip = new Properties();

						readProperties(tpn.getChildNodes(), tip);
						ml.setTileInstancePropertiesAt(x, y, tip);
					}
				}
			}
		}

		// This is done at the end, otherwise the offset is applied during
		// the loading of the tiles.
		ml.setOffset(offsetX, offsetY);

		// Invisible layers are automatically locked, so it is important to
		// set the layer to potentially invisible _after_ the layer data is
		// loaded.
		// todo: Shouldn't this be just a user interface feature, rather than
		// todo: something to keep in mind at this level?
		ml.setVisible(visible == 1);

		return ml;
	}

	/**
	 * Helper method to set the tile based on its global id.
	 * 
	 * @param ml
	 *            tile layer
	 * @param y
	 *            y-coordinate
	 * @param x
	 *            x-coordinate
	 * @param tileId
	 *            global id of the tile as read from the file
	 */
	private void setTileAtFromTileId(TileLayer ml, int y, int x, int tileId) {
		ml.setTileAt(x, y, getTileForTileGID(tileId));
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

	
	/**
	 * Load TileSet from a ".tsx" file. 
	 * @param source
	 * @return
	 */
	private TileSet loadTileSet(final String source) {
		String assetPath = toJmeAssetPath(source);

		// load it with assetManager
		TileSet ext = null;
		try {
			ext = (TileSet) assetManager.loadAsset(assetPath);
		} catch (Exception e) {
			logger.warning("Tileset " + source + " was not loaded correctly!", e);
		}

		return ext;
	}
	
	/**
	 * Load a Texture from source
	 * @param source
	 * @return
	 */
	private Texture2D loadTexture2D(final String source) {
		String assetPath = toJmeAssetPath(source);
		
		Texture2D tex = null;
		try {
			TextureKey texKey = new TextureKey(assetPath, false);
			texKey.setGenerateMips(false);
			tex = (Texture2D)assetManager.loadTexture(texKey);
			logger.info("Texture:" + tex);
		} catch (Exception e) {
			logger.warning("Can't load texture " + source, e);
		}
		
		return tex;
	}
	
	private Texture2D loadTexture2D(final byte[] data) {
		Class<?> LoaderClass = null;
		Object loaderInstance = null;
		Method loadMethod = null;
		
		try {
			// try Desktop first
			LoaderClass = Class.forName("com.jme3.texture.plugins.AWTLoader");
		} catch (ClassNotFoundException e) {
			logger.warning("Can't find AWTLoader.");
			
			try {
				// then try Android Native Image Loader
				LoaderClass = Class.forName("com.jme3.texture.plugins.AndroidNativeImageLoader");
			} catch (ClassNotFoundException e1) {
				logger.warning("Can't find AndroidNativeImageLoader.");
				
				try {
					// then try Android BufferImage Loader
					LoaderClass = Class.forName("com.jme3.texture.plugins.AndroidBufferImageLoader");
				} catch (ClassNotFoundException e2) {
					logger.warning("Can't find AndroidNativeImageLoader.");
				}
			}
		}
		
		if (LoaderClass == null) {
			return null;
		} else {
			// try Desktop first
			try {
				loaderInstance = LoaderClass.newInstance();
				loadMethod = LoaderClass.getMethod("load", AssetInfo.class);
			} catch (ReflectiveOperationException e) {
				logger.warning("Can't find AWTLoader.", e);
			}
		}
		
		TextureKey texKey = new TextureKey();
		AssetInfo info = new AssetInfo(assetManager, texKey) {
			public InputStream openStream() {
				return new ByteArrayInputStream(data);
			}
		};

		Texture2D tex = null;
		try {
			com.jme3.texture.Image img = (com.jme3.texture.Image)loadMethod.invoke(loaderInstance, info);
			
			tex = new Texture2D();
			tex.setAnisotropicFilter(texKey.getAnisotropy());
	        tex.setName(texKey.getName());
	        tex.setImage(img);
		} catch (Exception e) {
			logger.warning("Can't load texture from byte array", e);
		}
		
        return tex;
		
	}
	
	/**
	 * Utilities method to correct the asset path. 
	 * @param src
	 * @return
	 */
	private String toJmeAssetPath(final String src) {
		
		/*
		 * 1st: try to locate it with assetManager. No need to handle the src path unless
		 * assetManager can't locate it.
		 */
		if (assetManager.locateAsset(new AssetKey(src)) != null) {
			return src;
		}
		
		/*
		 * 2nd: In JME I suppose that all the files needed are in the same folder, 
		 * that's why I cut the filename and contact it to key.getFolder().
		 */
		String dest = src;
		int idx = src.lastIndexOf(File.separatorChar);
		if (idx >= 0) {
			dest = key.getFolder() + src.substring(idx + 1);
		} else {
			dest = key.getFolder() + dest;
		}
		
		/*
		 * 3rd: try locate it again.
		 */
		if (assetManager.locateAsset(new AssetKey(dest)) != null) {
			return dest;
		} else {
			throw new RuntimeException("Can't locate asset: " + src);
		}
	}
}
