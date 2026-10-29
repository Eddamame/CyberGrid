package io.github.cybergrid.simulation.app.game.entities;

import com.badlogic.gdx.graphics.Texture;

import io.github.cybergrid.simulation.engine.EngineContext;
import io.github.cybergrid.simulation.engine.collision.CollisionComponent;
import io.github.cybergrid.simulation.engine.entities.Entity;
import io.github.cybergrid.simulation.engine.entities.EntityType;
import io.github.cybergrid.simulation.engine.entities.TriggerEntity;
import io.github.cybergrid.simulation.engine.render.TextureRenderComponent;

public class ObfuscatedCodeEntity extends TriggerEntity {
    private static final float COLLISION_SIZE = 64f;
    private static final float DRAW_SIZE = 98f;

    public ObfuscatedCodeEntity(EngineContext context, int id, EntityType type) {
        super(context, id, type);
        setCollisionComponent(new CollisionComponent(COLLISION_SIZE, COLLISION_SIZE, false, true, 0f, 0f));
        setTexture(new Texture("obfuscated_code.png"));
        setRenderComponent(new TextureRenderComponent(DRAW_SIZE, DRAW_SIZE));
    }

    @Override
    public String getDisplayName() {
        return "ObfuscatedCode";
    }

    @Override
    public void onCollision(Entity other) {
        if (other instanceof PlayerEntity) {
            activate(other);
        }
    }

    @Override
    public void destroy() {
        if (getTexture() != null) {
            getTexture().dispose();
            setTexture(null);
        }
    }
}
