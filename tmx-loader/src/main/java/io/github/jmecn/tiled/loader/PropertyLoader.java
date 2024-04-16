package io.github.jmecn.tiled.loader;

import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetManager;
import io.github.jmecn.tiled.util.ColorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Properties;

import static io.github.jmecn.tiled.TiledConst.*;
import static io.github.jmecn.tiled.TiledConst.CLASS;
import static io.github.jmecn.tiled.loader.Utils.*;

/**
 * The property loader.
 *
 * @author yanmaoyuan
 */
public class PropertyLoader {

    private static final Logger logger = LoggerFactory.getLogger(PropertyLoader.class);

    /**
     * Reads properties from amongst the given children. When a "properties"
     * element is encountered, it recursively calls itself with the children of
     * this node. This function ensures backward compatibility with tmx version
     * 0.99a.
     * <p>
     * Support for reading property values stored as character data was added in
     * Tiled 0.7.0 (tmx version 0.99c).
     *
     * @param children the children amongst which to find properties
     * @return the properties
     */
    public Properties load(NodeList children) {
        Properties props = new Properties();

        Node propertiesNode = null;
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (PROPERTIES.equals(child.getNodeName())) {
                propertiesNode = child;
                break;
            }
        }

        if (propertiesNode == null) {
            return props;
        }

        NodeList nodeList = propertiesNode.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node child = nodeList.item(i);
            if (PROPERTY.equals(child.getNodeName())) {
                readProperty(child, props);
            }
        }
        return props;
    }

    /**
     * read every property in a properties
     *
     * @param child
     * @param props
     */
    private void readProperty(Node child, Properties props) {
        String keyName = getAttributeValue(child, NAME);
        String value = getAttributeValue(child, VALUE);
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
            final String type = getAttribute(child, TYPE, "string");
            Object val = convertPropertyValue(type, value);
            props.put(keyName, val);
        }
    }

    /**
     * type can be as follows:
     * file: stored as paths relative from the location of the map file. (since 0.17)
     *
     * object: can reference any object on the same map and are stored as an integer (the ID of
     * the referenced object, or 0 when no object is referenced). When used on objects in the
     * Tile Collision Editor, they can only refer to other objects on the same tile. (since 1.4)
     *
     * class: will have their member values stored in a nested &lt;properties&gt; element. Only the
     * actually set members are saved. When no members have been set the properties element is
     * left out entirely.(since 1.8)
     *
     * @param type the type of the property
     * @param value the value of the property
     */
    private Object convertPropertyValue(String type, String value) {
        Object val = value;
        switch (type) {
            // string (default) (since 0.16)
            case "string":
                break;
            // a int value (since 0.16)
            case "int":
                val = (int) Long.parseLong(value);
                break;
            // a float value (since 0.16)
            case "float":
                val = Float.parseFloat(value);
                break;
            // has a value of either "true" or "false". (since 0.16)
            case "bool":
                val = Boolean.parseBoolean(value);
                break;
            // stored in the format #AARRGGBB. (since 0.17)
            case COLOR:
                val = ColorUtil.toColorRGBA(value);
                break;
            // stored as paths relative from the location of the map file. (since 0.17)
            case "file":
                break;
            // can reference any object on the same map and are stored as an integer
            // (the ID of the referenced object, or 0 when no object is referenced).
            // When used on objects in the Tile Collision Editor, they can only refer
            // to other objects on the same tile. (since 1.4)
            case OBJECT:
                // Don't know the usage of this type, so I just convert that value to an int
                val = Integer.parseInt(value);
                break;
            // will have their member values stored in a nested <properties> element.
            // Only the actually set members are saved. When no members have been set
            // the properties element is left out entirely. (since 1.8)
            case CLASS:
                break;
            default:
                logger.warn("unknown type:{}", type);
                break;
        }

        return val;
    }

}
