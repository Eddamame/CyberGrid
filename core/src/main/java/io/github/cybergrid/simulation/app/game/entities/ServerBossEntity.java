package io.github.cybergrid.simulation.app.game.entities;

import com.badlogic.gdx.graphics.Texture;

import com.badlogic.gdx.math.Vector2;

import io.github.cybergrid.simulation.app.game.GameConstants;
import io.github.cybergrid.simulation.app.game.combat.Hittable;
import io.github.cybergrid.simulation.engine.EngineContext;
import io.github.cybergrid.simulation.engine.collision.CollisionComponent;
import io.github.cybergrid.simulation.engine.entities.EntityType;
import io.github.cybergrid.simulation.engine.entities.Entity;
import io.github.cybergrid.simulation.engine.entities.StaticEntity;
import io.github.cybergrid.simulation.engine.render.TextureRenderComponent;

public class ServerBossEntity extends StaticEntity implements Hittable {
    private static final float COLLISION_SIZE = 112f;
    private static final float DRAW_SIZE = 250f;

    private int maxHealth = GameConstants.BOSS_MAX_HEALTH;
    private int health = GameConstants.BOSS_MAX_HEALTH;
    private boolean defeated;

    public ServerBossEntity(EngineContext context, int id, EntityType type) {
        super(context, id, type);
        setCollisionComponent(new CollisionComponent(COLLISION_SIZE, COLLISION_SIZE, false, true, 0f, 0f));
        setTexture(new Texture("vuln_server.png"));
        setRenderComponent(new TextureRenderComponent(DRAW_SIZE, DRAW_SIZE));
    }
    @Override
    public void onCollision(Entity other) {}

    @Override
    public void onHitByBullet(Vector2 bulletDirection) {
        takeDamage();
    }

    public void takeDamage() {
        if (defeated) {
            return;
        }

        health = Math.max(0, health - GameConstants.BULLET_DAMAGE);
        defeated = health <= 0;
    }

    public void setMaxHealth(int maxHealth) {
        this.maxHealth = Math.max(1, maxHealth);
        this.health = this.maxHealth;
        this.defeated = false;
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public float getHealthRatio() {
        return maxHealth <= 0 ? 0f : (float) health / (float) maxHealth;
    }

    public boolean isDefeated() {
        return defeated;
    }

    @Override
    public String getDisplayName() {
        return "";
    }

    @Override
    public void destroy() {
        if (getTexture() != null) {
            getTexture().dispose();
            setTexture(null);
        }
    }
}
