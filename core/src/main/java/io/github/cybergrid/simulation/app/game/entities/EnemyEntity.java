package io.github.cybergrid.simulation.app.game.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.TimeUtils;

import io.github.cybergrid.simulation.app.game.combat.CollisionDamageSource;
import io.github.cybergrid.simulation.app.game.combat.Hittable;
import io.github.cybergrid.simulation.app.game.GameConstants;
import io.github.cybergrid.simulation.engine.EngineContext;
import io.github.cybergrid.simulation.engine.collision.CollisionComponent;
import io.github.cybergrid.simulation.engine.entities.AIEntity;
import io.github.cybergrid.simulation.engine.entities.Entity;
import io.github.cybergrid.simulation.engine.entities.EntityType;
import io.github.cybergrid.simulation.engine.managers.AudioManager;
import io.github.cybergrid.simulation.engine.render.TextureRenderComponent;

public class EnemyEntity extends AIEntity implements CollisionDamageSource, Hittable {

    private static final float COLLISION_SIZE = 64f;
    private static final float DRAW_BOX = 124f;
    private static final long COLLIDE_SOUND_COOLDOWN_NANOS = 300_000_000L;

    private long lastCollideSoundTimeNanos;
    private boolean enraged;

    public EnemyEntity(EngineContext context, int id, EntityType type) {
        super(context, id, type);
        setCollisionComponent(new CollisionComponent(COLLISION_SIZE, COLLISION_SIZE, true, false, GameConstants.ENEMY_KNOCKBACK, 5f));
        setTexture(new Texture("enemy.png"));
        setRenderComponent(new TextureRenderComponent(DRAW_BOX, DRAW_BOX));
        setSpeed(GameConstants.ENEMY_SPEED);
        lastCollideSoundTimeNanos = 0L;
        enraged = false;
    }

    public void enrage() {
        if (enraged) {
            return;
        }

        enraged = true;
        setSpeed(GameConstants.ENEMY_MAX_SPEED * GameConstants.ENEMY_ENRAGED_SPEED_MULTIPLIER);
    }

    public boolean isEnraged() {
        return enraged;
    }

    @Override
    public int getCollisionDamage() {
        return GameConstants.ENEMY_COLLISION_DAMAGE;
    }

    @Override
    public void onHitByBullet(Vector2 bulletDirection) {
        context.getMovementManager().applyImpulse(entityID, bulletDirection.scl(GameConstants.ENEMY_KNOCKBACK));
    }

    @Override
    public String getDisplayName() {
        return "AI";
    }

    @Override
    public void onCollision(Entity other) {
        if (other instanceof PlayerEntity) {
            long now = TimeUtils.nanoTime();
            if (now - lastCollideSoundTimeNanos >= COLLIDE_SOUND_COOLDOWN_NANOS) {
                AudioManager audioManager = context.getAudioManager();
                if (audioManager != null) {
                    audioManager.playSound("collide_sound.mp3");
                    lastCollideSoundTimeNanos = now;
                }
            }

            Vector2 awayFromPlayer = getPosition().cpy().sub(other.getPosition()).nor();
            Vector2 awayFromEnemy = other.getPosition().cpy().sub(getPosition()).nor();
            context.getMovementManager().applyImpulse(entityID, awayFromPlayer.scl(GameConstants.ENEMY_KNOCKBACK));
            context.getMovementManager().applyImpulse(other.getID(), awayFromEnemy.scl(GameConstants.PLAYER_KNOCKBACK));
            return;
        }

        if (other instanceof FirewallEntity) {
            Vector2 pushDir = getPosition().cpy().sub(other.getPosition()).nor();
            context.getMovementManager().applyImpulse(entityID, pushDir.scl(GameConstants.ENEMY_KNOCKBACK));
        }
    }
}
