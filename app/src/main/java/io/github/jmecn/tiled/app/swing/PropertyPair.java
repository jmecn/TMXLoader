package io.github.jmecn.tiled.app.swing;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public class PropertyPair {

    private String name;
    private String type;
    private Object value;
    private boolean isEditable = false;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public boolean isEditable() {
        return isEditable;
    }

    public void setEditable(boolean editable) {
        isEditable = editable;
    }
}
