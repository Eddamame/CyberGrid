package io.github.cybergrid.simulation.engine.render;

import io.github.cybergrid.simulation.engine.entities.Entity;

public interface RenderComponent {
    void render(Entity entity, RenderContext context);

    default boolean rendersLabel() {
        return true;
    }
}


