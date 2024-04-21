package io.github.jmecn.tiled.core;

import java.util.Properties;

/**
 * Wraps any number of custom properties. Can be used as a child of the map,
 * tile (when part of a tileset), layer, objectgroup and object elements.
 * 
 * The type of the property. Can be string (default), int, float, bool, color or
 * file (since 0.16, with color and file added in 0.17).
 * 
 * Boolean properties have a value of either "true" or "false".
 * 
 * Color properties are stored in the format #AARRGGBB.
 * 
 * File properties are stored as paths relative from the location of the map
 * file.
 * 
 * @author yanmaoyuan
 * 
 */
public class Base extends VisualSpatial {
    
    protected Properties properties;

    public boolean hasProperties() {
        return properties != null && !properties.isEmpty();
    }

    /**
     * <p>
     * Getter for the field <code>properties</code>.
     * </p>
     * 
     * @return the map properties
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     * <p>
     * Setter for the field <code>properties</code>.
     * </p>
     * 
     * @param properties
     *            a {@link java.util.Properties} object.
     */
    public void setProperties(Properties properties) {
        this.properties = properties;
    }
}
