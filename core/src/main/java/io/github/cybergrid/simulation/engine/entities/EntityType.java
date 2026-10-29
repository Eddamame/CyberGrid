package io.github.cybergrid.simulation.engine.entities;

public enum EntityType {
    CONTROLLABLE("controllable"),
    AI("ai"),
    STATIC("static"),
    PROJECTILE("projectile"),
    TRIGGER("trigger");

    private final String tag;

    EntityType(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }

    public static EntityType fromTag(String tag) {
        if (tag == null) {
            return null;
        }
        for (EntityType type : values()) {
            if (type.tag.equalsIgnoreCase(tag)) {
                return type;
            }
        }
        return null;
    }
}
