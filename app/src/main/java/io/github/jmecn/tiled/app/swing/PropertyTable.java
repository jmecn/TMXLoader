package io.github.jmecn.tiled.app.swing;

import com.jme3.math.ColorRGBA;
import io.github.jmecn.tiled.core.Layer;
import io.github.jmecn.tiled.core.MapObject;
import io.github.jmecn.tiled.core.Tile;
import io.github.jmecn.tiled.core.TiledMap;
import io.github.jmecn.tiled.util.ColorUtil;

import javax.swing.*;
import java.awt.*;

/**
 *
 * @author yanmaoyuan
 */
public class PropertyTable extends JTable {

    private final PropertyModel model;
    public PropertyTable() {
        model = new PropertyModel();
        this.setModel(model);
        this.getTableHeader().setReorderingAllowed(false);
        this.getTableHeader().setPreferredSize(new Dimension(100, 20));
        this.setPreferredScrollableViewportSize(new Dimension(200, 0));
    }

    public void setMap(TiledMap map) {
        model.clear();
        if (map == null) {
            invalidate();
            repaint();
            return;
        }
        add("version", map.getVersion());
        add("tiledVersion", map.getTiledVersion());
        add("width", map.getWidth());
        add("height", map.getHeight());
        add("tileWidth", map.getTileWidth());
        add("tileHeight", map.getTileHeight());
        add("orientation", map.getOrientation().getValue());
        add("renderOrder", map.getRenderOrder().getValue());
        add("staggerAxis", map.getStaggerAxis().getValue());
        add("staggerIndex", map.getStaggerIndex().getValue());
        add("hexSideLength", map.getHexSideLength());
        add("backgroundColor", map.getBackgroundColor());
        add("parallaxOriginX", map.getParallaxOriginX());
        add("parallaxOriginY", map.getParallaxOriginY());
        add("infinite", map.isInfinite());
        add("class", map.getClazz());

        invalidate();
        repaint();
    }

    public void setLayer(Layer layer) {
        model.clear();
        if (layer == null) {
            invalidate();
            repaint();
            return;
        }
        add("id", layer.getId());
        add("name", layer.getName());
        add("width", layer.getWidth());
        add("height", layer.getHeight());
        add("x", layer.getX());
        add("y", layer.getY());
        add("visible", layer.isVisible());
        add("locked", layer.isLocked());
        add("tintColor", layer.getTintColor());
        add("opacity", layer.getOpacity());

        add("class", layer.getClazz());
        add("offsetX", layer.getOffsetX());
        add("offsetY", layer.getOffsetY());
        add("parallaxX", layer.getParallaxX());
        add("parallaxY", layer.getParallaxY());

        if (!layer.getProperties().isEmpty()) {
            layer.getProperties().forEach((k, v) -> add(k.toString(), v));
        }

        invalidate();
        repaint();
    }

    public void setTile(Tile tile) {
        model.clear();
        if (tile == null) {
            invalidate();
            repaint();
            return;
        }
        add("id", tile.getId());
        add("gid", String.format("0x%08x", tile.getGid()));
        add("x", tile.getX());
        add("y", tile.getY());
        add("width", tile.getWidth());
        add("height", tile.getHeight());
        if (tile.getTileset() != null) {
            add("tileset", tile.getTileset().getImageSource());
        }
        if (tile.getImage() != null) {
            add("image", tile.getImage().getSource());
        }
        add("probability", tile.getProbability());
        add("isAnimated", tile.isAnimated());
        add("flipHorizontally", tile.isFlippedHorizontally());
        add("flipVertically", tile.isFlippedVertically());
        add("flipDiagonally", tile.isFlippedAntiDiagonally());
        add("rotate120", tile.isRotatedHexagonal120());

        if (tile.getProperties() != null && !tile.getProperties().isEmpty()) {
            tile.getProperties().forEach((k, v) -> add(k.toString(), v));
        }

        invalidate();
        repaint();
    }


    public void setObject(MapObject object) {
        model.clear();
        if (object == null) {
            invalidate();
            repaint();
            return;
        }
        add("id", object.getId());
        add("name", object.getName());
        add("x", object.getX());
        add("y", object.getY());
        add("width", object.getWidth());
        add("height", object.getHeight());
        add("shape", object.getShape());
        if (object.getTile() != null) {
            add("gid", String.format("0x%08x", object.getGid()));
            add("isAnimated", object.getTile().isAnimated());
        }
        if (object.getImage() != null) {
            add("image", object.getImage().getSource());
        }
        if (object.getTextData() != null) {
            add("textData", object.getTextData().getText());
        }
        add("class", object.getClazz());
        add("rotation", object.getRotation());
        add("visible", object.isVisible());
        if (object.getTemplate() != null) {
            add("template", object.getTemplate());
        }

        if (object.getProperties() != null && !object.getProperties().isEmpty()) {
            object.getProperties().forEach((k, v) -> add(k.toString(), v));
        }

        invalidate();
        repaint();
    }

    public void add(String name, String value) {
        PropertyPair propertyPair = new PropertyPair();
        propertyPair.setName(name);
        propertyPair.setValue(value);
        propertyPair.setEditable(false);
        propertyPair.setType("string");
        model.addProperty(propertyPair);
    }
    public void add(String name, int value) {
        PropertyPair propertyPair = new PropertyPair();
        propertyPair.setName(name);
        propertyPair.setValue(value);
        propertyPair.setEditable(false);
        propertyPair.setType("int");
        model.addProperty(propertyPair);
    }

    public void add(String name, double value) {
        PropertyPair propertyPair = new PropertyPair();
        propertyPair.setName(name);
        propertyPair.setValue(Math.round(value * 10000.0) / 10000.0);// keep 4 decimal places
        propertyPair.setEditable(false);
        propertyPair.setType("float");
        model.addProperty(propertyPair);
    }

    public void add(String name, boolean value) {
        PropertyPair propertyPair = new PropertyPair();
        propertyPair.setName(name);
        propertyPair.setValue(value);
        propertyPair.setEditable(false);
        propertyPair.setType("boolean");
        model.addProperty(propertyPair);
    }

    public void add(String name, ColorRGBA value) {
        PropertyPair propertyPair = new PropertyPair();
        propertyPair.setName(name);
        if (value != null) {
            propertyPair.setValue(ColorUtil.toHex(value));
        }
        propertyPair.setEditable(false);
        propertyPair.setType("color");
        model.addProperty(propertyPair);
    }

    public void add(String name, Object value) {
        PropertyPair propertyPair = new PropertyPair();
        propertyPair.setName(name);
        propertyPair.setValue(value);
        propertyPair.setEditable(false);
        propertyPair.setType("object");
        model.addProperty(propertyPair);
    }

}
