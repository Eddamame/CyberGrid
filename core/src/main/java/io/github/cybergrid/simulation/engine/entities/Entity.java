package io.github.cybergrid.simulation.engine.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;

import io.github.cybergrid.simulation.engine.EngineContext;
import io.github.cybergrid.simulation.engine.render.RenderComponent;

public abstract class Entity {
    protected final EngineContext context;
    protected int entityID;
    private final EntityType entityType;
    protected Texture entityTexture;
    private RenderComponent renderComponent;

    public Entity(EngineContext context, int entityID, EntityType entityType) {
        this.context = context;
        this.entityID = entityID;
        this.entityType = entityType;
        this.entityTexture = null;
    }

    public abstract void init();
    public abstract void update(float deltaTime);
    public abstract void destroy();
    public abstract Vector2 getPosition();
    public abstract void setPosition(Vector2 pos);

    public int getID() { return entityID; }
    public EntityType getType() { return entityType; }
    public String getTypeTag() { return entityType.getTag(); }
    public String getDisplayName() { return getClass().getSimpleName(); }
    public Texture getTexture() { return entityTexture; }
    public void setTexture(Texture texture) { this.entityTexture = texture; }

    public RenderComponent getRenderComponent() {
        return renderComponent;
    }

    public void setRenderComponent(RenderComponent renderComponent) {
        this.renderComponent = renderComponent;
    }
}