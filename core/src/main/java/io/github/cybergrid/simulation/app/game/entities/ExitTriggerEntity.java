package io.github.cybergrid.simulation.app.game.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import io.github.cybergrid.simulation.engine.EngineContext;
import io.github.cybergrid.simulation.engine.collision.CollisionComponent;
import io.github.cybergrid.simulation.engine.entities.Entity;
import io.github.cybergrid.simulation.engine.entities.EntityType;
import io.github.cybergrid.simulation.engine.entities.TriggerEntity;
import io.github.cybergrid.simulation.engine.render.ShapeRenderComponent;

public class ExitTriggerEntity extends TriggerEntity {
    private static final float WIDTH = 220f;
    private static final float HEIGHT = 150f;
    private static final Color ZONE_COLOR = new Color(0.22f, 0.55f, 0.82f, 0.30f);

    public ExitTriggerEntity(EngineContext context, int id, EntityType type) {
        super(context, id, type);
        setCollisionComponent(new CollisionComponent(WIDTH, HEIGHT, false, true, 0f, 0f));
        setRenderComponent(new ShapeRenderComponent((entity, renderer) -> {
            CollisionComponent collision = getCollisionComponent();
            if (collision == null) {
                return;
            }
            Rectangle bounds = collision.getBounds(getPosition());
            renderer.setColor(ZONE_COLOR);
            renderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
        }));
    }

    @Override
    public String getDisplayName() {
        return "";
    }

    @Override
    public void onCollision(Entity other) {
        if (other instanceof PlayerEntity) {
            activate(other);
        }
    }

    public float getWidth() {
        return WIDTH;
    }

    public float getHeight() {
        return HEIGHT;
    }

    public Vector2 getCenter() {
        return getPosition();
    }
}
