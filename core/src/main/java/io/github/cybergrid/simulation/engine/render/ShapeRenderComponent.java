package io.github.cybergrid.simulation.engine.render;

import java.util.Objects;
import java.util.function.BiConsumer;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import io.github.cybergrid.simulation.engine.entities.Entity;

public final class ShapeRenderComponent implements RenderComponent {
    private final BiConsumer<Entity, ShapeRenderer> drawer;
    private final boolean rendersLabel;

    public ShapeRenderComponent(BiConsumer<Entity, ShapeRenderer> drawer) {
        this(drawer, true);
    }

    public ShapeRenderComponent(BiConsumer<Entity, ShapeRenderer> drawer, boolean rendersLabel) {
        this.drawer = Objects.requireNonNull(drawer, "drawer");
        this.rendersLabel = rendersLabel;
    }

    @Override
    public void render(Entity entity, RenderContext context) {
        if (!context.isShapeLayer() || context.getShapeRenderer() == null) {
            return;
        }
        drawer.accept(entity, context.getShapeRenderer());
    }

    @Override
    public boolean rendersLabel() {
        return rendersLabel;
    }
}


