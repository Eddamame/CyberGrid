package io.github.cybergrid.simulation.app.game.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;

import io.github.cybergrid.simulation.app.game.GameConstants;
import io.github.cybergrid.simulation.app.game.combat.Hittable;
import io.github.cybergrid.simulation.engine.EngineContext;
import io.github.cybergrid.simulation.engine.collision.CollisionComponent;
import io.github.cybergrid.simulation.engine.entities.AIEntity;
import io.github.cybergrid.simulation.engine.entities.Entity;
import io.github.cybergrid.simulation.engine.entities.EntityType;
import io.github.cybergrid.simulation.engine.movement.SteeringState;
import io.github.cybergrid.simulation.engine.render.TextureRenderComponent;

public class FirewallEntity extends AIEntity implements Hittable {

    private static final float COLLISION_WIDTH = 150f;
    private static final float COLLISION_HEIGHT = 30f;
    private static final float DRAW_WIDTH = 240f;
    private static final float DRAW_HEIGHT = 140f;

    private boolean pathInitialised = false;

    public FirewallEntity(EngineContext context, int id, EntityType type) {
        super(context, id, type);
        setCollisionComponent(new CollisionComponent(COLLISION_WIDTH, COLLISION_HEIGHT, true, true, GameConstants.FIREWALL_BOUNCE_FORCE, 0f));
        setTexture(new Texture("firewall.png"));
        setRenderComponent(new TextureRenderComponent(DRAW_WIDTH, DRAW_HEIGHT));
    }

    public void setLengthScale(float lengthScale) {
        setSizeScales(lengthScale, 1f);
    }

    public void setSizeScales(float lengthScale, float heightScale) {
        float clampedLength = Math.max(0.65f, Math.min(2.0f, lengthScale));
        float clampedHeight = Math.max(0.85f, Math.min(1.8f, heightScale));
        CollisionComponent collision = getCollisionComponent();
        if (collision != null) {
            collision.setWidth(COLLISION_WIDTH * clampedLength);
            collision.setHeight(COLLISION_HEIGHT * clampedHeight);
        }
        setRenderComponent(new TextureRenderComponent(DRAW_WIDTH * clampedLength, DRAW_HEIGHT * clampedHeight));
    }

    public void initPath(float worldWidth) {
        float y = getPosition().y;
        List<Vector2> waypoints = new ArrayList<>();
        waypoints.add(new Vector2(worldWidth - GameConstants.WORLD_MARGIN, y));
        waypoints.add(new Vector2(GameConstants.WORLD_MARGIN, y));
        context.getMovementManager().setWaypoints(entityID, waypoints);
        context.getMovementManager().setSteeringBehavior(entityID, SteeringState.FOLLOW_PATH);
        pathInitialised = true;
    }

    @Override
    public void update(float delta) {
        if (!pathInitialised) {
            return;
        }

        if (context.getMovementManager().getComponent(entityID).getSteeringBehavior() == SteeringState.IDLE) {
            List<Vector2> waypoints = context.getMovementManager().getComponent(entityID).getWaypoints();
            Collections.reverse(waypoints);
            context.getMovementManager().setWaypoints(entityID, waypoints);
            context.getMovementManager().setSteeringBehavior(entityID, SteeringState.FOLLOW_PATH);
        }
    }

    @Override
    public void onHitByBullet(Vector2 bulletDirection) {}

    @Override
    public void onCollision(Entity other) {
        if (other instanceof EnemyEntity) {
            Vector2 pushDir = other.getPosition().cpy().sub(getPosition()).nor();
            context.getMovementManager().applyImpulse(other.getID(), pushDir.scl(GameConstants.ENEMY_KNOCKBACK));
        }
    }

    @Override
    public String getDisplayName() {
        return "";
    }
}
