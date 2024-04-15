package io.github.jmecn.tiled.enums;

/**
 * Whether the objects are drawn according to the order of appearance
 * ("index") or sorted by their y-coordinate ("topdown"). Defaults to
 * "topdown".
 */
public enum DrawOrder {
    INDEX("index"), TOPDOWN("topdown");
    private final String value;
    DrawOrder(String value) {
        this.value = value;
    }

    public static DrawOrder fromValue(String value) {
        for (DrawOrder c : DrawOrder.values()) {
            if (c.value.equals(value)) {
                return c;
            }
        }
        return TOPDOWN;
    }
}