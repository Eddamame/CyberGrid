package io.github.cybergrid.simulation.app.game.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;

import io.github.cybergrid.simulation.app.game.GameConstants;
import io.github.cybergrid.simulation.app.game.combat.Hittable;
import io.github.cybergrid.simulation.app.game.events.GameEventBus;
import io.github.cybergrid.simulation.app.game.events.BulletImpactEvent;
import io.github.cybergrid.simulation.engine.EngineContext;
import io.github.cybergrid.simulation.engine.collision.CollisionComponent;
import io.github.cybergrid.simulation.engine.entities.Entity;
import io.github.cybergrid.simulation.engine.entities.EntityType;
import io.github.cybergrid.simulation.engine.entities.ProjectileEntity;
import io.github.cybergrid.simulation.engine.render.TextureRenderComponent;

public class BulletEntity extends ProjectileEntity {
    private static final float WIDTH = 48f;
    private static final float HEIGHT = 48f;
    private static final Vector2 OFFSCREEN_POSITION = new Vector2(-9999f, -9999f);

    private final CollisionComponent activeCollisionComponent;
    private final CollisionComponent inactiveCollisionComponent;
    private final GameEventBus gameEventBus;
    private Vector2 direction = new Vector2(0f, 1f);
    private Vector2 originPosition = new Vector2(OFFSCREEN_POSITION);
    private boolean available = true;

    public BulletEntity(EngineContext context, int id, EntityType type, GameEventBus gameEventBus) {
        super(context, id, type);
        this.gameEventBus = gameEventBus;
        activeCollisionComponent = new CollisionComponent(WIDTH, HEIGHT, false, false, 0f, 0f);
        inactiveCollisionComponent = new CollisionComponent(0f, 0f, false, true, 0f, 0f);
        setTexture(new Texture("packet.png"));
        setRenderComponent(new TextureRenderComponent(51f, 51f));
        resetProjectile();
    }

    public void fire(Vector2 direction, Vector2 startPosition) {
        this.direction = direction.cpy().nor();
        this.originPosition.set(startPosition);
        setAvailable(false);
        context.getMovementManager().setPosition(entityID, startPosition);
        context.getMovementManager().applyImpulse(entityID, this.direction.cpy().scl(GameConstants.BULLET_SPEED));
        System.out.println("Bullet Fired");
    }

    public boolean isAvailable() {
        return available;
    }

    public Vector2 getDirection() {
        return direction.cpy();
    }

    public void setAvailable(boolean available) {
        this.available = available;
        setCollisionComponent(available ? inactiveCollisionComponent : activeCollisionComponent);
    }

    @Override
    public Texture getTexture() {
        return available ? null : super.getTexture();
    }

    @Override
    public void update(float delta) {
        if (available) {
            return;
        }

        if (getPosition().dst(originPosition) > GameConstants.BULLET_RANGE) {
            resetProjectile();
        }
    }

    @Override
    public void onCollision(Entity other) {
        if (available) {
            return;
        }

        if (other instanceof Hittable) {
            playBulletHitSfx();
            ((Hittable) other).onHitByBullet(direction.cpy());
            resetProjectile();
        }
    }

    private void playBulletHitSfx() {
        gameEventBus.dispatch(new BulletImpactEvent());
    }

    public void resetProjectile() {
        setAvailable(true);
        direction.set(0f, 1f);
        originPosition.set(OFFSCREEN_POSITION);
        context.getMovementManager().applyImpulse(entityID, new Vector2());
        context.getMovementManager().setPosition(entityID, OFFSCREEN_POSITION);
    }

    @Override
    public String getDisplayName() {
        return "";
    }
}
