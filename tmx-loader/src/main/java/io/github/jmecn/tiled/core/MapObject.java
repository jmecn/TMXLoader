package io.github.jmecn.tiled.core;

import java.util.List;

import com.jme3.math.Vector2f;
import io.github.jmecn.tiled.enums.ObjectType;

/**
 * An object occupying an {@link ObjectGroup}.
 * <p>
 * While tile layers are very suitable for anything repetitive aligned to the
 * tile grid, sometimes you want to annotate your map with other information,
 * not necessarily aligned to the grid. Hence the objects have their coordinates
 * and size in pixels, but you can still easily align that to the grid when you
 * want to.
 * </p>
 * <p>
 * You generally use objects to add custom information to your tile map, such as
 * spawn points, warps, exits, etc.
 * </p>
 * <p>
 * When the object has a gid set, then it is represented by the image of the
 * tile with that global ID. The image alignment currently depends on the map
 * orientation. In orthogonal orientation it's aligned to the bottom-left while
 * in isometric it's aligned to the bottom-center.
 * </p>
 * <p>
 * Can contain: properties, ellipse (since 0.9), polygon, polyline, image
 * </p>
 * @author yanmaoyuan
 * 
 */
public class MapObject extends Base {

    private ObjectGroup objectGroup;

    /**
     * Unique ID of the object. Each object that is placed on a map gets a
     * unique id. Even if an object was deleted, no object gets the same ID. Can
     * not be changed in Tiled Qt. (since Tiled 0.11)
     */
    private int id;

    /**
     * The name of the object. An arbitrary string.
     */
    private String name;

    /**
     * The class of the object. An arbitrary string.
     */
    private String clazz;
    private ObjectType shape = ObjectType.RECTANGLE;

    /**
     * The (x, y) coordinate of the object in pixels.
     */
    private double x;
    private double y;

    /**
     * The width and height of the object in pixels (defaults to 0).
     */
    private double width = 0;
    private double height = 0;

    /**
     * The rotation of the object in degrees clockwise (defaults to 0). (since 0.10)
     */
    private double rotation = 0f;

    private String template;

    /**
     * Whether the object is shown (1) or hidden (0). Defaults to 1. (since 0.9)
     */
    private boolean visible;

    
    // when ObjectGroupType == Tile
    /**
     * A reference to a tile (optional).
     * 
     * When the object has a gid set, then it is represented by the image of the
     * tile with that global ID. The image alignment currently depends on the
     * map orientation. In orthogonal orientation it's aligned to the
     * bottom-left while in isometric it's aligned to the bottom-center.
     */
    private int gid;// ObjectType.TILE
    private Tile tile;// ObjectType.TILE
    private List<Vector2f> points;// ObjectType.POLYGON, ObjectType.POLYLINE
    private TiledImage image;// ObjectType.IMAGE
    private ObjectText textData;// ObjectType.TEXT

    public MapObject() {
        // for serialization
    }

    public MapObject(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public ObjectGroup getObjectGroup() {
        return objectGroup;
    }

    public void setObjectGroup(ObjectGroup objectGroup) {
        this.objectGroup = objectGroup;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    public ObjectType getShape() {
        return shape;
    }

    public void setShape(ObjectType shape) {
        this.shape = shape;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public double getRotation() {
        return rotation;
    }

    public void setRotation(double rotation) {
        this.rotation = rotation;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public int getGid() {
        return gid;
    }

    public void setGid(int gid) {
        this.gid = gid;
    }

    public Tile getTile() {
        return tile;
    }

    public void setTile(Tile tile) {
        this.tile = tile;
    }

    public List<Vector2f> getPoints() {
        return points;
    }

    public void setPoints(List<Vector2f> points) {
        this.points = points;
    }

    public TiledImage getImage() {
        return image;
    }

    public void setImage(TiledImage image) {
        this.image = image;
    }

    public ObjectText getTextData() {
        return textData;
    }

    public void setTextData(ObjectText textData) {
        this.textData = textData;
    }

    @Override
    public String toString() {
        return "MapObject [id=" + id + ", name=" + name + ", shape=" + shape
                + ", x=" + x + ", y=" + y + ", width=" + width + ", height=" + height + "]";
    }

}
