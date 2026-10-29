package io.github.cybergrid.simulation.app.game.entities;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;

import io.github.cybergrid.simulation.app.game.combat.PlayerHealth;
import io.github.cybergrid.simulation.app.game.GameConstants;
import io.github.cybergrid.simulation.engine.EngineContext;
import io.github.cybergrid.simulation.engine.collision.CollisionComponent;
import io.github.cybergrid.simulation.engine.entities.ControllableEntity;
import io.github.cybergrid.simulation.engine.entities.Entity;
import io.github.cybergrid.simulation.engine.entities.EntityType;
import io.github.cybergrid.simulation.engine.render.TextureRenderComponent;

public class PlayerEntity extends ControllableEntity implements PlayerHealth {

    private static final float COLLISION_SIZE = 64f;
    private static final float DRAW_BOX = 124f;
    private static final int MAX_HEALTH = GameConstants.PLAYER_MAX_HEALTH;

    private int health;
    private int bulletsRemaining = GameConstants.MAX_BULLETS;
    private float shootTimer = 0f;
    private float obfuscationRemainingSeconds = 0f;
    private List<Integer> projectileIds = new ArrayList<>();

    public PlayerEntity(EngineContext context, int id, EntityType type) {
        super(context, id, type);
        setCollisionComponent(new CollisionComponent(COLLISION_SIZE, COLLISION_SIZE, true, false, GameConstants.PLAYER_KNOCKBACK, 25f));
        setSpeed(GameConstants.PLAYER_SPEED);
        setTexture(new Texture("player.png"));
        setRenderComponent(new TextureRenderComponent(DRAW_BOX, DRAW_BOX));
        setHealth(MAX_HEALTH);
    }

    public static int getMaxHealth() {
        return MAX_HEALTH;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = Math.max(0, Math.min(MAX_HEALTH, health));
    }

    public void setProjectileIds(List<Integer> ids) {
        projectileIds = ids == null ? new ArrayList<>() : new ArrayList<>(ids);
    }

    public int getBulletsRemaining() {
        return bulletsRemaining;
    }

    @Override
    public void update(float delta) {
        shootTimer += delta;
        if (obfuscationRemainingSeconds > 0f) {
            obfuscationRemainingSeconds = Math.max(0f, obfuscationRemainingSeconds - delta);
        }
    }

    @Override
    public void onCollision(Entity other) {
        if (other instanceof FirewallEntity || other instanceof FirewallStaticEntity) {
            context.getMovementManager().applyImpulse(getID(), new Vector2());
        }
    }

    public void attemptShoot(Vector2 targetWorldPosition) {
        if (shootTimer < GameConstants.SHOOT_COOLDOWN || bulletsRemaining <= 0 || projectileIds.isEmpty()) {
            return;
        }

        if (targetWorldPosition == null) {
            return;
        }

        Vector2 direction = targetWorldPosition.cpy().sub(getPosition());
        if (direction.isZero()) {
            return;
        }
        direction.nor();

        for (int projectileId : projectileIds) {
            BulletEntity bullet = context.getEntityManager().getEntityAs(projectileId, BulletEntity.class);
            if (bullet == null || !bullet.isAvailable()) {
                continue;
            }

            Vector2 projectilePos = getPosition().cpy();
            context.getMovementManager().setPosition(projectileId, projectilePos);
            bullet.fire(direction, projectilePos);
            shootTimer = 0f;
            bulletsRemaining--;
            return;
        }
    }

    @Override
    public String getDisplayName() {
        return "Player";
    }

    public void activateObfuscation(float durationSeconds) {
        obfuscationRemainingSeconds = Math.max(obfuscationRemainingSeconds, durationSeconds);
    }

    public boolean isObfuscated() {
        return obfuscationRemainingSeconds > 0f;
    }

    public float getObfuscationRemainingSeconds() {
        return obfuscationRemainingSeconds;
    }

    @Override
    public boolean isDamageImmune() {
        return isObfuscated();
    }
}
