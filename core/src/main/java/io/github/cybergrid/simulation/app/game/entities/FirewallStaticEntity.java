package io.github.cybergrid.simulation.app.game.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;

import io.github.cybergrid.simulation.app.game.combat.Hittable;
import io.github.cybergrid.simulation.engine.EngineContext;
import io.github.cybergrid.simulation.engine.collision.CollisionComponent;
import io.github.cybergrid.simulation.engine.entities.Entity;
import io.github.cybergrid.simulation.engine.entities.EntityType;
import io.github.cybergrid.simulation.engine.entities.StaticEntity;
import io.github.cybergrid.simulation.engine.render.TextureRenderComponent;

public class FirewallStaticEntity extends StaticEntity implements Hittable {

    private static final float COLLISION_WIDTH = 160f; 
    private static final float COLLISION_HEIGHT = 30f;
    private static final float DRAW_WIDTH = 240f;
    private static final float DRAW_HEIGHT = 140f;

    public FirewallStaticEntity(EngineContext context, int id, EntityType type) {
        super(context, id, type);
        setCollisionComponent(new CollisionComponent(COLLISION_WIDTH, COLLISION_HEIGHT, true, true, 0f, 0f));
        setTexture(new Texture("firewall.png"));
        setRenderComponent(new TextureRenderComponent(DRAW_WIDTH, DRAW_HEIGHT));
    }

    @Override
    public void destroy() {
        if (getTexture() != null) {
            getTexture().dispose();
            setTexture(null);
        }
    }
    @Override
    public void onHitByBullet(Vector2 bulletDirection) {}

    @Override
    public void onCollision(Entity other) {}
}
