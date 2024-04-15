package io.github.jmecn.tiled.enums;

/**
 * For staggered and hexagonal maps, determines whether the "even" or "odd"
 * indexes along the staggered axis are shifted. (since 0.11)
 */
public enum StaggerIndex {
    EVEN("even"), ODD("odd");
    final String value;
    StaggerIndex(String value) {
        this.value = value;
    }
    public String getValue() {
        return value;
    }

}