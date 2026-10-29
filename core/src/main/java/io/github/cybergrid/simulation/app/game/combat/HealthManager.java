package io.github.cybergrid.simulation.app.game.combat;

import io.github.cybergrid.simulation.app.game.events.GameEventBus;
import io.github.cybergrid.simulation.app.game.events.PlayerDamagedEvent;
import io.github.cybergrid.simulation.app.game.events.PlayerDeathEvent;
import io.github.cybergrid.simulation.engine.entities.Entity;
import io.github.cybergrid.simulation.engine.events.CollisionEvent;
import io.github.cybergrid.simulation.engine.interfaces.IEventListener;
import io.github.cybergrid.simulation.engine.managers.EventManager;

public class HealthManager implements IEventListener<CollisionEvent> {
    private static final Class<CollisionEvent> EVENT_TYPE = CollisionEvent.class;

    private final EventManager eventManager;
    private final GameEventBus gameEventBus;

    public HealthManager(EventManager eventManager, GameEventBus gameEventBus) {
        this.eventManager = eventManager;
        this.gameEventBus = gameEventBus;
        eventManager.subscribe(EVENT_TYPE, this);
    }

    @Override
    public void onEvent(CollisionEvent event) {
        Entity a = event.getEntityA();
        Entity b = event.getEntityB();
        PlayerHealth player = resolvePlayer(a, b);
        int collisionDamage = resolveCollisionDamage(a, b);

        if (player == null || collisionDamage <= 0 || player.getHealth() <= 0 || player.isDamageImmune()) {
            return;
        }

        int newHealth = Math.max(0, player.getHealth() - collisionDamage);
        player.setHealth(newHealth);

        if (newHealth <= 0) {
            gameEventBus.dispatch(new PlayerDeathEvent());
        } else {
            gameEventBus.dispatch(new PlayerDamagedEvent(newHealth));
        }
    }

    private PlayerHealth resolvePlayer(Entity a, Entity b) {
        if (a instanceof PlayerHealth) {
            return (PlayerHealth) a;
        }
        if (b instanceof PlayerHealth) {
            return (PlayerHealth) b;
        }
        return null;
    }

    private int resolveCollisionDamage(Entity a, Entity b) {
        int damage = 0;
        if (a instanceof CollisionDamageSource) {
            damage = Math.max(damage, ((CollisionDamageSource) a).getCollisionDamage());
        }
        if (b instanceof CollisionDamageSource) {
            damage = Math.max(damage, ((CollisionDamageSource) b).getCollisionDamage());
        }
        return damage;
    }

    public void dispose() {
        eventManager.unsubscribe(EVENT_TYPE, this);
    }
}
