package io.github.jmecn.tiled.core;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public class ObjectTemplate {
    private String source;
    private Tileset tileset;
    private MapObject object;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Tileset getTileset() {
        return tileset;
    }

    public void setTileset(Tileset tileset) {
        this.tileset = tileset;
    }

    public MapObject getObject() {
        return object;
    }

    public void setObject(MapObject object) {
        this.object = object;
    }

    @Override
    public String toString() {
        return "ObjectTemplate{" +
                "source='" + source + '\'' +
                ", tileset=" + tileset +
                ", object=" + object +
                '}';
    }

    public void copyTo(MapObject obj) {
        obj.setName(object.getName());
        obj.setClazz(object.getClazz());
        obj.setWidth(object.getWidth());
        obj.setHeight(object.getHeight());
        obj.setRotation(object.getRotation());
        obj.setShape(object.getShape());
        obj.setGid(object.getGid());
        obj.setTile(object.getTile());
        obj.setPoints(object.getPoints());
        obj.setVisible(object.isVisible());
        obj.setTextData(object.getTextData());
        obj.setProperties(object.getProperties());
    }
}